#!/bin/sh
#
# How to create the POS tag 'french.dict' dictionary:
#
# 1) Download morfologik-stemming-1.4.0.zip from
#    http://sourceforge.net/projects/morfologik/files/morfologik-stemming/1.4.0/
#    $ unzip morfologik-stemming-1.4.0.zip
#    This creates morfologik-stemming-nodict-1.4.0.jar
# 2) Run the script:
#    $ ./create-lexicon.sh
#    This creates the POS tag dictionary 'french.dict' ad the
#    synthesizer dictionary french_synth.dict.
#
# Author: Dominique Pelle <dominique.pelle@gmail.com>
#

INPUT=lexique-dicollecte-fr-v6.0.2

if [ ! -f $INPUT.txt ]; then
  wget http://www.dicollecte.org/download/fr/$INPUT.zip
  unzip $INPUT.zip
fi

./dicollecte-to-lt.pl $INPUT.txt

# POS tag dictionary...
java -jar morfologik-stemming-nodict-1.4.0.jar tab2morph \
     -i $INPUT.txt.LT.txt \
     -o output.txt
java -jar morfologik-stemming-nodict-1.4.0.jar fsa_build \
     -i output.txt \
     -o french.dict

# Synthesizer dictionary:
# The Java program outputs temporary files in /tmp which is not 
# convenient (it would be better to indicate the location of output files).
rm -f /tmp/SynthDictionaryBuilder*.txt_tags.txt
rm -f /tmp/DictionaryBuilder*.dict

java -cp ../../../../../../../../../languagetool-tools/target/languagetool-tools-3.7-SNAPSHOT-jar-with-dependencies.jar \
     org.languagetool.tools.SynthDictionaryBuilder \
     -i $INPUT.txt.LT.txt \
     -info french_synth.info \
     -o french_synth.dict
mv -f /tmp/SynthDictionaryBuilder*.txt_tags.txt french_tags.txt
