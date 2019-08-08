package camel.kafka.config;

import camel.kafka.service.FooBar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class MockApplicationConfig {

    @Bean
    public FooBar buildFooBar() {
        return mock(FooBar.class);
    }

}
