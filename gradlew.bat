@echo off
set APP_HOME=%~dp0
set JAR=%APP_HOME%gradle\wrapper\gradle-wrapper.jar

if not exist "%JAR%" (
  echo Missing gradle\wrapper\gradle-wrapper.jar. Generate it with a local Gradle install or download the official wrapper jar.
  exit /b 1
)

java -jar "%JAR%" %*
