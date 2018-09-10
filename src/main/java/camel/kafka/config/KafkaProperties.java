package camel.kafka.config;

import org.springframework.beans.factory.annotation.Value;

public class KafkaProperties {

    @Value("${kafka.consumer.group}")
    private String consumerGroupId;
    @Value("${kafka.topic}")
    private String topic;
    @Value("${kafka.host}")
    private String hostWithPort;
    @Value("${kafka.consumer.seekTo}")
    private String seekTo;
    @Value("${kafka.consumer.maxPollRecords}")
    private Integer maxPollRecords;
    @Value("${kafka.consumer.consumerCount}")
    private Integer consumerCount;
    @Value("${kafka.consumer.autoOffsetReset}")
    private String autoOffsetReset;

    public String buildKafkaUrl() {
        StringBuilder sb = new StringBuilder();
        sb.append("kafka:").append(topic)
            .append("?brokers=").append(hostWithPort)
            .append("&groupId=").append(consumerGroupId)
            .append("&maxPollRecords=").append(maxPollRecords)
            .append("&consumersCount=").append(consumerCount)
            .append("&autoOffsetReset=").append(autoOffsetReset)
            .append("&seekTo=").append(seekTo);

        return sb.toString();
    }

}
