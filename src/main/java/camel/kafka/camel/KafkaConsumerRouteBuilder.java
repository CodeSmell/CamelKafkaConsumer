package camel.kafka.camel;

import camel.kafka.config.KafkaProperties;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class KafkaConsumerRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerRouteBuilder.class);

    @Autowired
    KafkaProperties kafkaProps;

    @Override
    public void configure() throws Exception {

        String kafkaUrl = kafkaProps.buildKafkaUrl();
        LOGGER.info("building camel route to consume from kafka: {}", kafkaUrl);

        from(kafkaUrl)
            .routeId("consumeFromKafka")
            .process(exchange -> {
                System.out.println("Message Received:"+ exchange.getIn().getBody());
            })
            .process(exchange -> {
                // call Appointment DAO
            })
            .process(exchange -> {
                exchange.setProperty(Exchange.FILE_NAME, UUID.randomUUID().toString() + ".txt");
            })
            .to("file:///Volumes/dev/CamelKafkaConsumer/files");
    }

}
