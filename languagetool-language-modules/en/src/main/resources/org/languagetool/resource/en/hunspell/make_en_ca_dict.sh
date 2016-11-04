#!/bin/sh
# Get dictionaries from http://wordlist.aspell.net/dicts/
# Get en_us_wordlist.xml from https://github.com/mozilla-b2g/gaia/raw/master/apps/keyboard/js/imes/latin/dictionaries/en_us_wordlist.xml

unmunch en_CA.dic en_CA.aff > en_CA1.txt
cat en_CA1.txt | java -cp languagetool.jar:languagetool-dev-3.6-SNAPSHOT.jar org.languagetool.dev.WordTokenizer en | sort -u > en_CA.txt
java -cp languagetool.jar org.languagetool.tools.SpellDictionaryBuilder -i en_CA.txt -info en_CA.info -freq en_us_wordlist.xml  -o en_CA_spell.dict
