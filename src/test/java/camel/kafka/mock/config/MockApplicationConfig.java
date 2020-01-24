package camel.kafka.mock.config;

import camel.kafka.config.KafkaProperties;
import camel.kafka.service.FooBar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import static org.mockito.Mockito.mock;

@Configuration
@PropertySource(value = "classpath:application.test.properties")
public class MockApplicationConfig {

    @Bean
    public FooBar buildFooBar() {
        return mock(FooBar.class);
    }
    
    @Bean
    public KafkaProperties kafkaProps() {
        return new KafkaProperties();
    }

}
