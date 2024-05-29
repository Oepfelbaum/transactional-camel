FROM maven:3.9.7-eclipse-temurin-17 AS build  
COPY src /usr/src/app/src  
COPY pom.xml /usr/src/app  
RUN mvn -f /usr/src/app/pom.xml clean package

FROM gcr.io/distroless/java17-debian12
COPY --from=build /usr/src/app/target/transactional-camel-0.0.1-SNAPSHOT.jar /usr/app/transactional-camel-0.0.1-SNAPSHOT.jar 
EXPOSE 8080  
ENTRYPOINT ["java","-jar","/usr/app/transactional-camel-0.0.1-SNAPSHOT.jar"]  
