FROM openjdk:15
ADD build/libs/nitunes-0.0.1-SNAPSHOT.jar nitunes.jar
ENTRYPOINT ["java", "-jar", "nitunes.jar"]