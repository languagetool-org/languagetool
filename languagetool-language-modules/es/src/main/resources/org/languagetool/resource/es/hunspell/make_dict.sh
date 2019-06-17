#!/bin/sh
# Get dictionary from https://extensions.libreoffice.org/extensions/spanish-dictionaries and unzip the *.oxt
# Get es_wordlist.xml from https://github.com/mozilla-b2g/gaia/raw/master/apps/keyboard/js/imes/latin/dictionaries/

# shaded JAR wih all dependencies (i.e. run `mvn package` first):
LTDEV=/languagetool/languagetool-dev/target/languagetool-dev-4.6-SNAPSHOT-shaded.jar
LTTOOL=/languagetool/languagetool-tools/target/languagetool-tools-4.6-SNAPSHOT-jar-with-dependencies.jar

unmunch es_ANY.dic es_ANY.aff > words.txt
hunspell -d es_ANY -G words.txt | java -cp $LTDEV org.languagetool.dev.archive.WordTokenizer en | sort -u > words_clean.txt
java -cp $LTTOOL org.languagetool.tools.SpellDictionaryBuilder -i words_clean.txt -info es_ES.info -freq es_wordlist.xml -o es_ES.dict
