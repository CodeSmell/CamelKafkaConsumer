package camel.kafka.config;

import camel.kafka.route.KafkaConsumerRouteBuilder;
import camel.kafka.route.KafkaOffsetManagerProcessor;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Arrays;
import java.util.List;

@Configuration
@PropertySource(value = "classpath:application.properties")
public class CamelConfig extends CamelConfiguration {

    /**
     * Camel routes
     */
    @Override
    public List<RouteBuilder> routes() {
        return Arrays.asList(kafkaConsumerRouteBuilder());
    }

    /**
     * This method configures camel routes
     *
     * @return the Camel route builder
     */
    @Bean
    public RouteBuilder kafkaConsumerRouteBuilder() {
        KafkaConsumerRouteBuilder kafkaRouteBuilder = new KafkaConsumerRouteBuilder();
        return kafkaRouteBuilder;
    }

    @Bean
    public KafkaProperties kafkaProps() {
        return new KafkaProperties();
    }
    
    @Bean("kafkaOffsetManager")
    public Processor buildKafkaOffsetManager() {
        return new KafkaOffsetManagerProcessor();
    }
}