# CamelKafkaConsumer
Application built as part of a spike to test how Camel consumes messages from Kafka topics.
 
The application consists of a single Camel route that consumes from a Kafka topic. It will write the message to a file. The main goal is to assess how error handling and offset management work.

The application is build as a WAR file that can be deployed to Tomcat. 
The easiest way to start using the application is to use the command line.

Build the application and install the WAR in local maven repository 

	mvn clean install
	
Run the application on Tomcat

	mvn tomcat7:run

Prior to Camel 2.21, the capability to manually commit the offset did not exist. It also seems to have had some initial issues so using versions greater than 2.22.0 is recommended.

* [CAMEL-11933](https://issues.apache.org/jira/browse/CAMEL-11933)
* [CAMEL-12525](https://issues.apache.org/jira/browse/CAMEL-12525)  

TODO write up... 

seekTo to skip (req restart)
  
The kafka settings are stored in a config file in `src/main/resources`

| Property  | sample value | comment |
| ------------- | ------------- | ------------- |
| kafka.host  | localhost:9092  | Kafka bootstrap servers
| kafka.topic  | TestLog  | the topic being consumed from
| kafka.consumer.group  | kafkaGroup  | identifies consumer group
| kafka.consumer.consumerCount  | 1  | the number of consumers connected to Kafka
| kafka.consumer.autoOffsetReset | earliest | what to do when offset missing (latest, earliest, none)
| kafka.consumer.seekTo | beginning | moves offset to (beginning, end), leave empty to use offset
| kafka.consumer.maxPollRecords | 50 | max records consumed in single poll 
| kafka.consumer.autoCommitEnable | false | turn on/off auto commit of message after consuming
| kafka.consumer.allowManualCommit | true | turn on/off manual commits via KafkaManualCommit
