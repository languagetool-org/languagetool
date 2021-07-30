#!/bin/sh
# fast way to build the ZIP for testing an deployment

mvn -pl '!languagetool-http-client,!languagetool-wikipedia,!languagetool-office-extension,!languagetool-dev,!languagetool-rpm-package' clean package -DskipTests
