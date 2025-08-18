#!/bin/sh
java -cp $HOME/hazelcast/hazelcast-5.1.3/lib/hazelcast-5.1.3.jar:./Mobile:. \
     -Dhazelcast.config=$HOME/hazelcast/hazelcast-5.1.3/config/hazelcast.xml \
     $1 $2 $3 $4 $5 $6
