#!/bin/sh
./unmunch en-GB.dic en-GB.aff  >  en_GB1.txt
cat en_GB1.txt | java -cp languagetool.jar:languagetool-dev-3.5-SNAPSHOT.jar org.languagetool.dev.WordTokenizer en | sort -u > en_GB.txt
java -cp languagetool.jar org.languagetool.tools.SpellDictionaryBuilder -i en_GB.txt -info en_GB.info -freq en_gb_wordlist.xml  -o en_GB_spell.dict
