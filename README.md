# CamelKafkaConsumer
Application built as part of a spike to test how Camel consumes messages from Kafka topics.
The main goal is to assess how error handling and offset management work. 

The application is build as a WAR file that can be deployed to Tomcat. 
The easiest way to start using the application is to use the command line.

Build the application and install the WAR in local maven repository 

	mvn clean install
	
Run the application on Tomcat

	mvn tomcat7:run
  
The kafka settings are stored in a config file in `src/main/resources`

| Property  | sample value | comment |
| ------------- | ------------- | ------------- |
| kafka.host  | localhost:9092  | Kafka bootstrap servers
| kafka.topic  | TestLog  | the topic being consumed from
| kafka.consumer.group  | kafkaGroup  | identifies consumer group
| kafka.consumer.consumerCount  | 1  | the number of consumers connected to Kafka
| kafka.consumer.autoOffsetReset | largest | what to do when offset missing (smallest, largest, error)
| kafka.consumer.seekTo | beginning | moves offset to (beginning, end)
| kafka.consumer.maxPollRecords | 50 | max records consumed in single poll 
