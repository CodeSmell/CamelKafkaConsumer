package camel.kafka.config;

import camel.kafka.service.DefaultFooBar;
import camel.kafka.service.FooBar;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "camel.kafka.config")
public class ApplicationConfig {
    
    @Bean
    public FooBar buildFooBar() {
        return new DefaultFooBar();
    }

}
