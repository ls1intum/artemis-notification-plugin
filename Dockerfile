FROM gradle:7.4-jdk8-focal AS build
WORKDIR /notification-plugin
COPY . ./
RUN gradle clean jar

FROM eclipse-temurin:8-jre
WORKDIR /notification-plugin
COPY --from=build /notification-plugin/artemis-notification-plugin-*.jar /notification-plugin/artemis-notification-plugin.jar

# In GitLab CI each stage needs a script, which is executed in the container.
# Therefore, we do not need a CMD or ENTRYPOINT in the Dockerfile, since we execute gradle run directly.
# c.f. https://gitlab.com/gitlab-org/gitlab/-/issues/19717
