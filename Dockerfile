FROM openjdk:21-jdk-jdk

WORKDIR /app

COPY target/ai_mysql-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8090

ENTRYPOINT ["java","-jar","/app/app.jar"] 