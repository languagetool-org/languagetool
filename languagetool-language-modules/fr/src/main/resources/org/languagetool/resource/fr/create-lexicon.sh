#!/bin/sh
#
# How to create the POS tag 'french.dict' dictionary:
#
# 1) Download morfologik-tools-1.7.1-standalone.jar.zip:
#    $ wget morfologik-tools-1.7.1-standalone.jar.zip
#    $ unzip morfologik-tools-1.7.1-standalone.jar.zip
# 2) Run the script:
#    $ ./create-lexicon.sh
#    This creates the POS tag dictionary 'french.dict' ad the
#    synthesizer dictionary french_synth.dict.
#
# Author: Dominique Pelle <dominique.pelle@gmail.com>
#

INPUT=lexique-dicollecte-fr-v6.1

if [ ! -f $INPUT.txt ]; then
  wget http://www.dicollecte.org/download/fr/$INPUT.zip
  unzip $INPUT.zip
fi

./dicollecte-to-lt.pl $INPUT.txt

# POS tag dictionary...
java -jar morfologik-tools-1.7.1-standalone.jar tab2morph \
     -i $INPUT.txt.LT.txt \
     -o output.txt
java -jar morfologik-tools-1.7.1-standalone.jar fsa_build \
     -i output.txt \
     -o french.dict

java -cp ../../../../../../../../../languagetool-tools/target/languagetool-tools-3.9-SNAPSHOT-jar-with-dependencies.jar \
     org.languagetool.tools.SynthDictionaryBuilder \
     -i $INPUT.txt.LT.txt \
     -info french_synth.info \
     -o french_synth.dict
mv -f /tmp/SynthDictionaryBuilder*.txt_tags.txt french_tags.txt
