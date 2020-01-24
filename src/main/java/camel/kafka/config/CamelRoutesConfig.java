package camel.kafka.config;

import camel.kafka.route.KafkaConsumerRouteBuilder;
import camel.kafka.route.KafkaOffsetManagerProcessor;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * Configure the Routes for Camel
 *
 */
@Configuration
public class CamelRoutesConfig { //extends CamelConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelRoutesConfig.class);


    @Bean("kafkaConsumerRouteBuilder")
    public RouteBuilder kafkaConsumerRouteBuilder() {
        LOGGER.info("building KafkaConsumerRouteBuilder...");
        return new KafkaConsumerRouteBuilder();
    }

    @Bean("kafkaOffsetManager")
    public Processor buildKafkaOffsetManager() {
        LOGGER.info("building KafkaOffsetManagerProcessor...");
        return new KafkaOffsetManagerProcessor();
    }
    
}