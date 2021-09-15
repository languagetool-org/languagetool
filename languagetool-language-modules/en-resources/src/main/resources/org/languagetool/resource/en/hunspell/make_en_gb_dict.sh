#!/bin/sh
# Get dictionaries from https://github.com/marcoagpinto/aoo-mozilla-en-dict
# Get en_us_wordlist.xml from https://github.com/mozilla-b2g/gaia/raw/master/apps/keyboard/js/imes/latin/dictionaries/en_gb_wordlist.xml

unmunch en-GB.dic en-GB.aff  >  en_GB1.txt
cat en_GB1.txt spelling_merged.txt| java -cp languagetool.jar:languagetool-dev-5.3-SNAPSHOT.jar org.languagetool.dev.archive.WordTokenizer en | sort -u > en_GB.txt
java -cp languagetool.jar org.languagetool.tools.SpellDictionaryBuilder -i en_GB.txt -info en_GB.info -freq en_gb_wordlist.xml  -o en_GB.dict
