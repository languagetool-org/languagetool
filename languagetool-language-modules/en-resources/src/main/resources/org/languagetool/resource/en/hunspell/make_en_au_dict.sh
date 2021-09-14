#!/bin/sh
# Get dictionaries from http://wordlist.aspell.net/dicts/
# Get en_us_wordlist.xml from https://github.com/mozilla-b2g/gaia/raw/master/apps/keyboard/js/imes/latin/dictionaries/en_us_wordlist.xml

unmunch en_AU.dic en_AU.aff > en_AU1.txt
cat en_AU1.txt spelling_merged.txt | java -cp languagetool.jar:languagetool-dev-5.3-SNAPSHOT.jar org.languagetool.dev.archive.WordTokenizer en | sort -u > en_AU.txt
java -cp languagetool.jar org.languagetool.tools.SpellDictionaryBuilder -i en_AU.txt -info en_AU.info -freq en_us_wordlist.xml  -o en_AU_spell.dict
