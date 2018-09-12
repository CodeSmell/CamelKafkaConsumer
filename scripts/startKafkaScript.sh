#! /bin/bash

# run zookeeper in background
# run kafka server in foreground
# cancel using ctrl-c
cd kafka1
./bin/zookeeper-server-start.sh config/zookeeper.properties & ./bin/kafka-server-start.sh config/server.properties && fg
