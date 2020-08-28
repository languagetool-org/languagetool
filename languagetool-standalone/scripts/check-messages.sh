#!/bin/bash
# run message spell check, to be called via cronjob

target=/home/languagetool/regression-test/results/open-source/message-spelling
date=`date +%Y-%m-%d`

cd /home/languagetool/languagetool
mvn install -DskipTests
cd /home/languagetool/languagetool/languagetool-dev
mvn clean compile assembly:single

cp target/languagetool-dev-*-SNAPSHOT-jar-with-dependencies.jar /home/languagetool/regression-test/message-spelling/languagetool-dev.jar
cd /home/languagetool/regression-test/message-spelling

for LAN in de-DE de-AT de-CH en-US en-GB fr pl ca it br nl pt-PT pt-BR ru ast be zh da eo gl el ja km ro sk sl es sv tl uk fa ta ga ar de-DE-x-simple-language
do

    mv $target/${LAN}_new.txt $target/${LAN}_old.txt
    java -cp languagetool-dev.jar org.languagetool.dev.LTMessageChecker.LTMessageChecker $LAN >$target/$LAN.txt
    grep "LTM: " $target/$LAN.txt | sed 's/LTM: //' >$target/${LAN}_new.txt
    diff -u $target/${LAN}_old.txt $target/${LAN}_new.txt >$target/${LAN}_diff.txt
    vim -n $target/${LAN}_diff.txt -c TOhtml -c ":saveas vim.tmp" -c ":q" -c ":q"
    mkdir $target/result/$date
    mv vim.tmp $target/result/$date/${LAN}_diff.html
    sed -i 's/\*\*/<b>/' $target/result/$date/${LAN}_diff.html
    sed -i 's/\*\*/<\/b>/' $target/result/$date/${LAN}_diff.html
    sed -i 's/monospace; color: #....../monospace; color: #cccccc/' $target/result/$date/${LAN}_diff.html
    sed -i 's/Identifier { color: #....../Identifier { color: #000000/' $target/result/$date/${LAN}_diff.html
    sed -i 's/Statement { color: #....../Statement { color: #cccccc/' $target/result/$date/${LAN}_diff.html

done
