package camel.kafka.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.component.kafka.KafkaManualCommit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaOffsetManagerProcessor implements Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaOffsetManagerProcessor.class);

    @Override
    public void process(Exchange exchange) throws Exception {
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
    }

}
