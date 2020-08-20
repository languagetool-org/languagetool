#!/bin/bash
# run message spell check, to be called via cronjob

target=/home/languagetool/regression-test/results/open-source/message-spelling

cd /home/languagetool/languagetool
mvn install -DskipTests
cd /home/languagetool/languagetool/languagetool-dev
mvn clean compile assembly:single

cp target/languagetool-dev-*-SNAPSHOT-jar-with-dependencies.jar /home/languagetool/regression-test/message-spelling/languagetool-dev.jar
cd /home/languagetool/regression-test/message-spelling

for LANG in en fr de pl ca it br nl pt ru ast be zh da eo gl el ja km ro sk sl es sv tl uk fa ta ga ar de-DE-x-simple-language
do

    mv $target/$LANG.txt $target/${LANG}_old.txt
    java -cp languagetool-dev.jar org.languagetool.dev.LTMessageChecker.LTMessageChecker $LANG >$target/$LANG.txt
    diff -u $target/${LANG}_old.txt $target/${LANG}.txt >$target/${LANG}_diff.txt

done
