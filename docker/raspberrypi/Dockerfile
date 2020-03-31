FROM arm32v7/adoptopenjdk:11.0.6_10-jdk-hotspot-bionic
ADD DeliveryServiceChecker-*.jar DeliveryServiceChecker.jar
ENV JAVA_OPTS=""
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /DeliveryServiceChecker.jar" ]
