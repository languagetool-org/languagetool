#!/bin/sh

echo "This script is deprecated - please see http://wiki.languagetool.org/developing-a-tagger-dictionary instead. Stopping."
exit

#LANG=POSIX
#LOCALE=POSIX
JAVA_OPTS="-Xmx2048m"
FSA="/usr/local/bin/morfologik-tools-1.5.2-standalone.jar"


echo -n ">> Compiling dictionary...    "
java ${JAVA_OPTS} -jar ${FSA} tab2morph -i el.txt -o _output.txt
java ${JAVA_OPTS} -jar ${FSA} fsa_build -i _output.txt -o el.dict
rm -f _output.txt
gawk -f synthesis.awk el.txt >output.txt
java ${JAVA_OPTS} -jar ${FSA} tab2morph -i output.txt -o encoded.txt
java ${JAVA_OPTS} -jar ${FSA} fsa_build -i encoded.txt -o el_synth.dict
rm -f output.txt
rm -f encoded.txt

echo "[ok]"
