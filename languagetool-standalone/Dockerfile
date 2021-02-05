FROM openjdk:8-jre-alpine

ENV LANGUAGETOOL_VERSION=5.3-SNAPSHOT

RUN apk update \
    && apk add \
    bash \
    libgomp \
    gcompat \
    hunspell

WORKDIR /LanguageTool

COPY target/LanguageTool-${LANGUAGETOOL_VERSION}/LanguageTool-${LANGUAGETOOL_VERSION} .

RUN mkdir /nonexistent && touch /nonexistent/.languagetool.cfg

RUN addgroup -S languagetool && adduser -S languagetool -G languagetool

COPY --chown=languagetool start.sh start.sh

COPY --chown=languagetool config.properties config.properties

USER languagetool

CMD [ "bash", "start.sh" ]

EXPOSE 8010
