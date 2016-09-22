#!/bin/sh
./unmunch en-GB.dic en-GB.aff > en_GB.txt
java -cp languagetool.jar org.languagetool.tools.SpellDictionaryBuilder -i en_GB.txt  -info en_GB.info -freq en_gb_wordlist.xml  -o en_GB_spell.dict
