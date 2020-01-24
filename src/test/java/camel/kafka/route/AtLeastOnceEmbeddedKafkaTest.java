package camel.kafka.route;

import camel.kafka.config.CamelRoutesConfig;
import camel.kafka.config.KafkaProperties;
import camel.kafka.service.DefaultFooBar;
import camel.kafka.service.FooBar;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.BaseEmbeddedKafkaTest;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * 
 * copied over the Camel embedded Kafka classes 
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AtLeastOnceEmbeddedKafkaTest.EmbeddedApplicationConfig.class, CamelRoutesConfig.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AtLeastOnceEmbeddedKafkaTest extends BaseEmbeddedKafkaTest {

    private static final String TOPIC = "EmbeddedTestLog";
    private static final String MOCK_OUTGOING_ENDPOINT = "mock:end";
    
    private org.apache.kafka.clients.producer.KafkaProducer<String, String> producer;
    
    @Autowired
    @Qualifier("evaluateException")
    private Processor evaluateExceptionProcessor;
    
    @Autowired
    private RouteBuilder kafkaConsumingRoute;
    
    private MockEndpoint mockOutgoingEndpoint;
    
    private CountDownLatch latchOne;
    private CountDownLatch latchTwo;
    private CountDownLatch latchThree;
    
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        
        Properties props = getDefaultProperties();
        producer = new org.apache.kafka.clients.producer.KafkaProducer<>(props);
        
        mockOutgoingEndpoint = getMockEndpoint(MOCK_OUTGOING_ENDPOINT);
        
        latchOne = new CountDownLatch(1);
        latchTwo = new CountDownLatch(2);
        latchThree = new CountDownLatch(3);
        
        this.overridePropertiesForEmbeddedTest();
        context.addRoutes(kafkaConsumingRoute);
    }
    
    @After
    public void after() {
        if (producer != null) {
            producer.close();
        }
    }
    
    @Test
    public void testCamelConsumeHandlePoisonPillWithoutLoss() throws Exception {
        EvaluateException evalErrors = (EvaluateException)evaluateExceptionProcessor;
        
        interceptOutgoingEndpoint();
        
        //**************************************************
        // publish messages to Kafka - all succeed
        //**************************************************
        
        // first 3 messages should go through
        mockOutgoingEndpoint.expectedBodiesReceived("0-egassem", "1-egassem", "2-egassem");
        mockOutgoingEndpoint.expectedMessageCount(3);
        
        // publish to Kafka
        producer.send(new ProducerRecord<>(TOPIC, null, "message-0"));
        producer.send(new ProducerRecord<>(TOPIC, null, "message-1"));
        producer.send(new ProducerRecord<>(TOPIC, null, "message-2"));
        
        // check it processed as expected
        mockOutgoingEndpoint.assertIsSatisfied(3000);
        assertEquals(0, evalErrors.numberOfExceptions());
        
        
        //**************************************************
        // publish another set of messages to Kafka - with one that causes error 
        //************************************************** 
        
        // reset 
        mockOutgoingEndpoint.reset();
        
        // 2 messages should go through
        // message 6 should be a poison message and get stuck
        // based on current configuration
        mockOutgoingEndpoint.expectedBodiesReceived("4-egassem", "5-egassem");
        mockOutgoingEndpoint.expectedMessageCount(2);
        
        producer.send(new ProducerRecord<>(TOPIC, null, "message-4"));
        producer.send(new ProducerRecord<>(TOPIC, null, "message-5"));
        producer.send(new ProducerRecord<>(TOPIC, null, "error-message-6"));
        producer.send(new ProducerRecord<>(TOPIC, null, "message-7"));
        
        // check it processed as expected
        mockOutgoingEndpoint.assertIsSatisfied(3000);
        
        List<Exchange> exchanges = mockOutgoingEndpoint.getExchanges();
        Exchange lastExchange = exchanges.get(exchanges.size()-1);
        assertNotNull(lastExchange);
        assertEquals("5-egassem", lastExchange.getIn().getBody());
        
        latchOne.await();
        assertEquals(1, evalErrors.numberOfExceptions());
        assertEquals("error-message-6", evalErrors.getMessageBody());
        
        
        //**************************************************
        // let's wait and see that message 6 is still being consumed
        //************************************************** 
        
        // reset
        mockOutgoingEndpoint.reset();
        
        // should not get any more messages coming thru successfully
        // message 6 is still stuck
        mockOutgoingEndpoint.expectedMessageCount(0);
        mockOutgoingEndpoint.assertIsSatisfied(3000);
        
        latchTwo.await();
        assertEquals(2, evalErrors.numberOfExceptions());
        assertEquals("error-message-6", evalErrors.getMessageBody());
        
        //**************************************************
        // let's restart route and check one more time
        //************************************************** 
        
        // reset
        mockOutgoingEndpoint.reset();

        // should not get any more messages coming thru successfully
        // message 6 is still stuck
        mockOutgoingEndpoint.expectedMessageCount(0);

        // Restart endpoint
        context.getRouteController().stopRoute(KafkaConsumerRouteBuilder.ROUTE_ID);
        context.getRouteController().startRoute(KafkaConsumerRouteBuilder.ROUTE_ID);
        
        // check it processed as expected
        mockOutgoingEndpoint.assertIsSatisfied(3000);
        
        latchThree.await();
        assertEquals(3, evalErrors.numberOfExceptions());
        assertEquals("error-message-6", evalErrors.getMessageBody());
    }
    
    private void interceptOutgoingEndpoint() throws Exception {
        context.getRouteDefinition(KafkaConsumerRouteBuilder.ROUTE_ID)
            .adviceWith(context, new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    interceptSendToEndpoint(KafkaConsumerRouteBuilder.OUTGOING_ENDPOINT)
                        .skipSendToOriginalEndpoint()
                        .to(mockOutgoingEndpoint);
                }
            });
    }
    
    /**
     * 
     * mimics ApplicationConfig class
     *
     */
    @Configuration
    @PropertySource(value = "classpath:application.embedded.properties")
    static class EmbeddedApplicationConfig {
        
        @Bean
        public FooBar buildFooBar() {
            return new DefaultFooBar();
        }
        
        @Bean
        public KafkaProperties kafkaProps() {
            return new KafkaProperties();
        }
    }
    
    private void overridePropertiesForEmbeddedTest() {
        EvaluateException evalErrors = (EvaluateException)evaluateExceptionProcessor;
        evalErrors.setCountDownOne(latchOne);
        evalErrors.setCountDownTwo(latchTwo);
        evalErrors.setCountDownThree(latchThree);
        
        Field fieldProps = ReflectionUtils.findField(KafkaConsumerRouteBuilder.class, "kafkaProps");
        fieldProps.setAccessible(true);
        KafkaProperties props = (KafkaProperties) ReflectionUtils.getField(fieldProps, kafkaConsumingRoute);
        
        // overwrite the brokers w/ embedded values
        Field fieldBrokers = ReflectionUtils.findField(KafkaProperties.class, "hostWithPort");
        fieldBrokers.setAccessible(true);
        ReflectionUtils.setField(fieldBrokers, props, kafkaBroker.getBrokerList());
    }
}
