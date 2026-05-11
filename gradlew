#!/bin/sh

APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$JAR" ]; then
  echo "Missing gradle/wrapper/gradle-wrapper.jar. Generate it with a local Gradle install or download the official wrapper jar." >&2
  exit 1
fi

exec java -jar "$JAR" "$@"
