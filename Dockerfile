FROM gradle:7.4-jdk17 AS build
WORKDIR /notification-plugin
COPY . ./
RUN gradle clean jar

FROM eclipse-temurin:17-jre
WORKDIR /notification-plugin
COPY --from=build /notification-plugin/build/libs/artemis-notification-plugin-*.jar /notification-plugin/artemis-notification-plugin.jar

# In GitLab CI each stage needs a script, which is executed in the container.
# Therefore, we do not need a CMD or ENTRYPOINT in the Dockerfile, since we run the jar directly within the continuous integration system.
# c.f. https://gitlab.com/gitlab-org/gitlab/-/issues/19717
