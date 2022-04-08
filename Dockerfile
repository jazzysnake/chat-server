FROM gradle:7-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar --no-daemon

FROM openjdk:11
EXPOSE 9090:9090
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app
ENTRYPOINT ["java","-jar","/app/hu.fatbrains.chat-server-0.0.1-all.jar"]