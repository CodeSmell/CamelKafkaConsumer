package camel.kafka.route;

import camel.kafka.config.CamelRoutesConfig;
import camel.kafka.config.KafkaProperties;
import camel.kafka.mock.config.MockApplicationConfig;
import camel.kafka.service.FooBar;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MockApplicationConfig.class, CamelRoutesConfig.class })
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class KafkaConsumerRouteBuilderTest extends CamelTestSupport {

    private String kafkaEndpoint;
    
    private static final String MOCK_OUTGOING_ENDPOINT = "mock:end";
    private MockEndpoint mockOutgoingEndpoint;
    
    @Autowired
    private KafkaProperties kafkaProps;
    
    @Autowired
    private FooBar mockFooBar;
    
    @Autowired
    private RouteBuilder kafkaConsumingRoute;
    
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        kafkaEndpoint = kafkaProps.buildKafkaUrl();
        
        mockOutgoingEndpoint = getMockEndpoint(MOCK_OUTGOING_ENDPOINT);

        context.addRoutes(kafkaConsumingRoute);
        
        Mockito.reset(mockFooBar);
    }
    
    @Test
    public void test_successful_message_flow() throws Exception {
        this.interceptOutgoingEndpoint();
        
        // foo bar processing
        Mockito.when(mockFooBar.process(Mockito.anyString()))
            .thenReturn("barfoo");
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getIn().setBody("foobar");
        
        // put test data on the route
        ProducerTemplate producerTemplate = this.template();
        Exchange exchangeOut = producerTemplate.send(kafkaEndpoint, exchangeIn);
        
        // check that all routes processed as expected
        mockOutgoingEndpoint.expectedMessageCount(1);
        assertMockEndpointsSatisfied();
        
        // checking the exceptions
        Exception exception = exchangeOut.getException();
        assertNull(exception);
        
        // verify the body
        String outBody = exchangeOut.getIn().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("barfoo", outBody);
    }
    
    @Test
    public void test_error_message_flow() throws Exception {
        this.interceptOutgoingEndpoint();
        
        // simulate exception during route processing
        Mockito.when(mockFooBar.process(Mockito.anyString()))
            .thenThrow(new RuntimeException("boom!"));
        
        // setup the test data
        Exchange exchangeIn = new DefaultExchange(context);
        exchangeIn.getIn().setBody("foobar");
        
        // put test data on the route
        ProducerTemplate producerTemplate = this.template();
        Exchange exchangeOut = producerTemplate.send(kafkaEndpoint, exchangeIn);
        
        // check that all routes processed as expected
        mockOutgoingEndpoint.expectedMessageCount(0);
        assertMockEndpointsSatisfied();
        
        // checking the exceptions
        Exception exception = exchangeOut.getException();
        assertNotNull(exception);
        assertEquals("boom!", exception.getMessage());
        
        // verify the body
        String outBody = exchangeOut.getIn().getBody(String.class);
        assertNotNull(outBody);
        assertEquals("foobar", outBody);
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

}