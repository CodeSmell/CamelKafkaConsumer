package camel.kafka.config;

import camel.kafka.camel.KafkaConsumerRouteBuilder;
import org.apache.camel.CamelContext;
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

    @Override
    protected void setupCamelContext(CamelContext camelContext) throws Exception {
        super.setupCamelContext(camelContext);
    }

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
}