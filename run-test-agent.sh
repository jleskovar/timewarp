#!/bin/bash

set -e

./gradlew install

TIMEWARP_JAR=$(ls build/libs/timewarp-*.jar | grep -v javadoc | grep -v sources)
java -javaagent:$TIMEWARP_JAR \
-Xbootclasspath/a:$TIMEWARP_JAR \
-cp build/classes/main:build/classes/test \
co.paralleluniverse.test.TimeTester &

JPID=$!
jconsole $JPID
kill -9 $JPID
