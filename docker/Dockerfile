FROM openjdk:11.0.6-jdk-slim
ADD DeliveryServiceChecker-*.jar DeliveryServiceChecker.jar
ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar /DeliveryServiceChecker.jar" ]
