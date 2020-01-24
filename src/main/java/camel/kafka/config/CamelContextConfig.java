package camel.kafka.config;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.spring.javaconfig.CamelConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CamelContextConfig extends CamelConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelContextConfig.class);

    @Autowired
    @Qualifier("kafkaConsumerRouteBuilder")
    RouteBuilder kafkaConsumerRouteBuilder;
    
    /**
     * This method configures camel routes
     *
     * @return the Camel route builder
     */
    @Override
    public List<RouteBuilder> routes() {
        LOGGER.info("adding routes to camel context...");

        return Arrays.asList(kafkaConsumerRouteBuilder);
    }
}
