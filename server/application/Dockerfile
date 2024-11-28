FROM amazoncorretto:21-alpine3.20
EXPOSE 8080:8080
RUN mkdir /app
COPY ./build/libs/*-all.jar /app/neon-server.jar
ENTRYPOINT ["java","-jar","/app/neon-server.jar"]