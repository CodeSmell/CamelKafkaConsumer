# CamelKafkaConsumer
This application is built as part of a spike to test how Camel consumes messages from Kafka topics.
 
The application consists of a single Camel route that consumes from a Kafka topic. It will write the message to a file. The main goal is to assess how error handling and offset management work.

The application is built as a WAR file that can be deployed to Tomcat. 

## Building and starting the application
The easiest way to start using the application is to use the command line.

Build the application and install the WAR in local maven repository 

	mvn clean install
	
Run the application on Tomcat

	mvn tomcat7:run -Dmaven.tomcat.port=8083

## Starting Kafka
Before starting the application, one should start Zookeeper and Kafka.
There is a start up script that can be run from the Kafka main directory.

	./startKafkaScript.sh
	
If you installed Kafka using HomeBrew the following commands can be used
	
	brew services start zookeeper
	brew services start kafka
	
## Creating a Kafka topic
Create a Kafka topic 

	bin/kafka-topics.sh --create 
		--replication-factor 1 --partitions 13 
		--topic TestLog 
		--zookeeper localhost:2181
		
Verify that the topic is created

	 bin/kafka-topics.sh --list --zookeeper localhost:2181

## Starting Kafka Producer script
The easiest way to test the application is to use the Kafka producer script:

	bin/kafka-console-producer.sh --broker-list localhost:9092 --topic TestLog

This allows simple messages to be published to a Kafka topic, in this case the messages are `foobar`, `test me`, and `error 123`.

	SLF4J: Class path contains multiple SLF4J bindings.
	SLF4J: Found binding in [jar:file:/Volumes/dev/sw/kafka_2.11-1.0.0/libs/kafka-connect-cassandra-1.0.0-1.0.0-all.jar!/org/slf4j/impl/StaticLoggerBinder.class]
	SLF4J: Found binding in [jar:file:/Volumes/dev/sw/kafka_2.11-1.0.0/libs/slf4j-log4j12-1.7.25.jar!/org/slf4j/impl/StaticLoggerBinder.class]
	SLF4J: See http://www.slf4j.org/codes.html#multiple_bindings for an explanation.
	SLF4J: Actual binding is of type [org.slf4j.impl.Log4jLoggerFactory]
	>foobar
	>test me
	>error 123
	
If you want to pass a key on the message start the producer as follows:

	bin/kafka-console-producer.sh --broker-list localhost:9092 --topic TestLog 
		--property "parse.key=true" --property "key.separator=:"

## Checking on the Consumer Group
To verify how things are going for a consumer group, the following command can be run:

	./bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group kafkaGroup --describe
	
The results will look something like this:

| TOPIC | PARTITION | CURRENT-OFFSET | LOG-END-OFFSET | LAG | CONSUMER-ID
| --- | --- | --- | --- | --- | --- 
| TestLog | 0  | 92  | 97 | 5 | consumer-1-f5f51ed6-c47a-4f2e-bd62-85d623379f86

## Skipping a message on the topic
If a message is causing a failure, the current configuration will not move past it until it is processed successfully. If a message needs to be skipped, one can manage that via the Kafka command line. The following command will set the current offset to 93. That will result in the application skipping the offending message at offset 92.

    ./bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group kafkaGroup --topic TestLog --reset-offsets --to-offset 93 --execute

## Notes on achieving "at least once" semantics
Prior to Camel 2.21, the capability to manually commit the offset did not exist. It also seems to have had some initial issues so using versions greater than 2.22.0 is recommended.

* [CAMEL-11933](https://issues.apache.org/jira/browse/CAMEL-11933)
* [CAMEL-12525](https://issues.apache.org/jira/browse/CAMEL-12525)  

An "at least once" semantics when setup will cause Camel to re-consume and re-process a message when there is an issue. Of course the result of this approach is that no other messages in the partition will be able to be processed (or seen) until the application can successfully process the message. In the case of a failing service, this is probably the desired result until the service is back up.
  
The kafka settings are stored in a config file in `src/main/resources`

| Property  | sample value | comment |
| ------------- | ------------- | ------------- |
| kafka.host  | localhost:9092  | Kafka bootstrap servers
| kafka.topic  | TestLog  | the topic being consumed from
| kafka.consumer.group  | kafkaGroup  | identifies consumer group
| kafka.consumer.consumerCount  | 1  | the number of consumers connected to Kafka
| kafka.consumer.autoCommitEnable | false | turn on/off auto commit of the offset. To get "at least once" we need to turn off auto committing of offsets so we can use manual commits.
| kafka.consumer.allowManualCommit | true | turn on/off manual commits via KafkaManualCommit. This needs to be set to true so we can perform manual commits, giving us access to the KafkaManualCommit capability.
| kafka.consumer.autoOffsetReset | earliest | what to do when offset missing (latest, earliest, none). Using earliest will cause the consumer to read from the current offset when there is a difference between the current offset and the offset marking the end of the partition.
| kafka.consumer.seekTo |  | moves offset to (beginning, end), leave empty to use offset
| kafka.consumer.breakOnFirstError | true | turn on/off whether Camel stops when it encounters an error. When this is true, the route will stop processing the rest of the messages in the batch received on last poll of the topic. If this is false, the route will process all of the messages in the batch. If the last message is successful, the offset would be committed and the other messages would not be re-processed even if there was an error. 
| kafka.consumer.maxPollRecords | 3 | max records consumed in single poll. It is probably a good idea to keep this set to a low number, since issues w/ a message in the batch would cause all of the messages in the batch to be re-processed.

