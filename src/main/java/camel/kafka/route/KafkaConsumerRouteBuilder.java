package camel.kafka.route;

import camel.kafka.config.KafkaProperties;
import camel.kafka.service.FooBar;
import org.apache.camel.Exchange;
import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.KafkaManualCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class KafkaConsumerRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerRouteBuilder.class);

    public static final String ROUTE_ID = "consumeFromKafka";
    public static final String OUTGOING_ENDPOINT = "file:///Volumes/dev/CamelKafkaConsumer/files";
    
    @Autowired
    KafkaProperties kafkaProps;
    
    @Autowired
    FooBar fooBarService;

    @Override
    public void configure() throws Exception {

        String kafkaUrl = kafkaProps.buildKafkaUrl();
        LOGGER.info("building camel route to consume from kafka: {}", kafkaUrl);

        onException(Exception.class)
            .handled(false)
            .log(LoggingLevel.WARN, "${exception.message}");

        from(kafkaUrl)
            .routeId(ROUTE_ID)
            .process(exchange -> {
                LOGGER.info(this.dumpKafkaDetails(exchange));
            })
            .process(exchange -> {
                // do something interesting
            })
            .process(exchange -> {
                // simple approach to generating errors
                String body = exchange.getIn().getBody(String.class);
                if (body.startsWith("error")) {
                    throw new RuntimeException("can't handle the message");
                }
            })
            .process(exchange -> {
                // do something interesting
                String in = exchange.getIn().getBody(String.class);
                String out = fooBarService.process(in);
                exchange.getIn().setBody(out);
            })
            .process(exchange -> {
                exchange.setProperty(Exchange.FILE_NAME, UUID.randomUUID().toString() + ".txt");
            })
            .to(OUTGOING_ENDPOINT)
            .process(exchange -> {
                // manually commit offset if last in batch
                Boolean lastOne = exchange.getIn().getHeader(KafkaConstants.LAST_RECORD_BEFORE_COMMIT, Boolean.class);

                if (lastOne != null && lastOne) {
                    KafkaManualCommit manual =
                            exchange.getIn().getHeader(KafkaConstants.MANUAL_COMMIT, KafkaManualCommit.class);
                    if (manual != null) {
                        LOGGER.info("manually committing the offset for batch");
                        manual.commitSync();
                    }
                } else {
                    LOGGER.info("NOT time to commit the offset yet");
                }
            });
    }

    private String dumpKafkaDetails(Exchange exchange) {
        StringBuilder sb = new StringBuilder();
        sb.append("Message Received from topic:").append(exchange.getIn().getHeader(KafkaConstants.TOPIC));
        sb.append("\r\n");
        sb.append("Message Received from partition:").append(exchange.getIn().getHeader(KafkaConstants.PARTITION));
        sb.append(" with partition key:").append(exchange.getIn().getHeader(KafkaConstants.PARTITION_KEY));
        sb.append("\r\n");
        sb.append("Message offset:").append(exchange.getIn().getHeader(KafkaConstants.OFFSET));
        sb.append("\r\n");
        sb.append("Message last record:").append(exchange.getIn().getHeader(KafkaConstants.LAST_RECORD_BEFORE_COMMIT));
        sb.append("\r\n");
        sb.append("Message Received:").append(exchange.getIn().getBody());
        sb.append("\r\n");

        return sb.toString();
    }

}
