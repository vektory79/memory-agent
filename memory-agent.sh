#!/usr/bin/env bash

# Setup the JVM
if [ "x$JAVA_HOME" = x ]; then
   fail_java_home () {
        echo "JAVA_HOME is not set. Unable to locate the jars needed to run memory agent."
        exit 2
   }

   JAVA_PATH=`which java` || fail_java_home
   which readlink || fail_java_home # make sure readlink is present
   JAVA_TEST=`readlink "$JAVA_PATH"`
   while [ x"$JAVA_TEST" != x ]; do
      JAVA_PATH="$JAVA_TEST"
      JAVA_TEST=`readlink "$JAVA_PATH"`
   done
   JAVA_HOME=`dirname "$JAVA_PATH"`
   JAVA_HOME=`dirname "$JAVA_HOME"`
fi

java -cp $JAVA_HOME/lib/tools.jar:./target/memory-agent.jar ru.vektory79.memoryagent.MemoryTool $@