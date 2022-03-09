#!/bin/sh

VERSION=5.6

rm -r target/new.jar
rm -r target/temp
mvn clean package
cd target
unzip -d temp languagetool-client-example-2.4-jar-with-dependencies.jar
cd temp
cp ~/lt/git/languagetool/languagetool-standalone/target/LanguageTool-$VERSION/LanguageTool-$VERSION/META-INF/org/languagetool/language-module.properties META-INF/org/languagetool/
zip -r ../new.jar .
echo "Now test new.jar: cd target && java -jar new.jar"
