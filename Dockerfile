FROM ghcr.io/navikt/baseimages/temurin:17

ENV JAVA_OPTS="-XX:-OmitStackTraceInFastThrow \
               -Xms768m -Xmx1280m"

COPY /build/libs/hm-delbestilling-api-fat.jar app.jar
