#!/bin/bash

WORK_HOME=$(cd "$(dirname "$0")"; pwd)
APP_NAME="zk-viewer"
JAVA_CMD="java"
CLASSPATH="${WORK_HOME}/lib/*:${CLASSPATH}"
MAIN_CLASS="com.github.johnsonmoon.zk.client.ZKViewer"
JAVA_OPTS="-Dapp.name=${APP_NAME} -Dwork.home=${WORK_HOME}"
JAVA_OPTS="${JAVA_OPTS} -Xmx512m -Xms256m -Xss256K -XX:MaxMetaspaceSize=256m"
JAVA_OPTS="${JAVA_OPTS} -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled -XX:+ExplicitGCInvokesConcurrent"

${JAVA_CMD} ${JAVA_OPTS} -classpath ${CLASSPATH} ${MAIN_CLASS}