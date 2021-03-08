#!/bin/sh
# Get dictionaries from http://wordlist.aspell.net/dicts/
# Get en_us_wordlist.xml from https://github.com/mozilla-b2g/gaia/raw/master/apps/keyboard/js/imes/latin/dictionaries/en_us_wordlist.xml

unmunch en_ZA.dic en_ZA.aff > en_ZA1.txt
cat en_ZA1.txt  spelling_merged.txt | java -cp languagetool.jar:languagetool-dev-5.3-SNAPSHOT.jar org.languagetool.dev.archive.WordTokenizer en | sort -u > en_ZA.txt
java -cp languagetool.jar org.languagetool.tools.SpellDictionaryBuilder -i en_ZA.txt -info en_ZA.info -freq en_us_wordlist.xml  -o en_ZA_spell.dict
