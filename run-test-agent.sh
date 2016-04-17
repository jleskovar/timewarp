#!/bin/bash

set -e

./gradlew install

TIMEWARP_JAR=$(ls build/libs/timewarp-*.jar | grep -v javadoc | grep -v sources)
java -javaagent:$TIMEWARP_JAR \
-Xbootclasspath/a:$TIMEWARP_JAR \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=9010 \
-Dcom.sun.management.jmxremote.local.only=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false \
-cp build/classes/main:build/classes/test \
co.paralleluniverse.test.TimeTester &

JPID=$!
jconsole $JPID
kill -9 $JPID
