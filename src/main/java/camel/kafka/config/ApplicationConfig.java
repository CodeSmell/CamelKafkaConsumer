package camel.kafka.config;

import camel.kafka.service.DefaultFooBar;
import camel.kafka.service.FooBar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@Configuration
@ComponentScan(basePackages = "camel.kafka.config")
@PropertySource(value = "classpath:application.properties")
public class ApplicationConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

    @Bean
    public FooBar buildFooBar() {
        LOGGER.info("building FooBar...");
        return new DefaultFooBar();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public KafkaProperties kafkaProps() {
        LOGGER.info("kafka properties using classpath:application.properties...");
        return new KafkaProperties();
    }
}
