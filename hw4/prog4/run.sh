#!/bin/sh

java -cp $HOME/hazelcast/hazelcast-5.1.3/lib/hazelcast-5.1.3.jar:. -Dhazelcast.config=$HOME/hazelcast/hazelcast-5.1.3/config/hazelcast.xml $1 $2 $3
