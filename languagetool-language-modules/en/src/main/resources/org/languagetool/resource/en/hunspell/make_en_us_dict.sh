#!/bin/sh
# Get dictionaries from http://wordlist.aspell.net/dicts/
# Get en_us_wordlist.xml from https://github.com/mozilla-b2g/gaia/raw/master/apps/keyboard/js/imes/latin/dictionaries/en_us_wordlist.xml

unmunch en_US.dic en_US.aff > en_US1.txt
cat en_US1.txt | java -cp languagetool.jar:languagetool-dev-3.6-SNAPSHOT.jar org.languagetool.dev.WordTokenizer en | sort -u > en_US.txt
java -cp languagetool.jar org.languagetool.tools.SpellDictionaryBuilder -i en_US.txt -info en_US.info -freq en_us_wordlist.xml  -o en_US_spell.dict
