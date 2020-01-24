package camel.kafka.config;

import camel.kafka.route.EvaluateExceptionProcessor;
import camel.kafka.route.KafkaConsumerRouteBuilder;
import camel.kafka.route.KafkaOffsetManagerProcessor;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 
 * Configure the Routes for Camel
 *
 */
@Configuration
public class CamelRoutesConfig { 


    @Bean("kafkaConsumerRouteBuilder")
    public RouteBuilder kafkaConsumerRouteBuilder() {
        return new KafkaConsumerRouteBuilder();
    }

    @Bean("kafkaOffsetManager")
    public Processor buildKafkaOffsetManager() {
        return new KafkaOffsetManagerProcessor();
    }
    
    @Bean("evaluateException")
    public Processor evaluateException() {
        return new EvaluateExceptionProcessor();
    }
    
}