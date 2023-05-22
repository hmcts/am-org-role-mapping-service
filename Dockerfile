# Keep hub.Dockerfile aligned to this file as far as possible
ARG JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"
# renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.4.12

# Application image

FROM hmctspublic.azurecr.io/base/java:17-distroless
USER hmcts
LABEL maintainer="https://github.com/hmcts/org-role-mapping-service"

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/am-org-role-mapping-service.jar /opt/app/

EXPOSE 4098
CMD [ "am-org-role-mapping-service.jar" ]
