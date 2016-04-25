#!/bin/bash

set -e

./gradlew install

TIMEWARP_JAR=$(ls build/libs/timewarp-*.jar | grep -v javadoc | grep -v sources)

export _JAVA_OPTIONS="-javaagent:../$TIMEWARP_JAR \
-Xbootclasspath/a:../$TIMEWARP_JAR \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.port=10081 \
-Dcom.sun.management.jmxremote.local.only=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false"

echo "About to run timewarp test agent on port 10080, JMX port 10081"
(cd timewarp-test && ./gradlew jettyRunWar)
