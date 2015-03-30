#!/bin/sh

set -e

# to build the dictionary:
# 1) get the file with lemma form tag information (TAB separated) from http://korpus.sk
# 2) place it into /tmp/ma.txt.xz
# 3) run this script

# file with the lemma-form-tag information
MA_FILE=/tmp/ma.txt.xz

cd `dirname $0`

# working directory where this script is run from
WORKDIR=`pwd`

# directory where the working tree is kept
LTDIR=$WORKDIR/../../../../../../../../../languagetool-standalone/target/LanguageTool-2.9-SNAPSHOT/LanguageTool-2.9-SNAPSHOT
LTDIR=`readlink -m $LTDIR`

# directory where languagetool sk resources are kept
SKDIR=$WORKDIR/../../../../../../../../../languagetool-language-modules/sk/src/main/resources/
SKDIR=`readlink -m $SKDIR`

tmp=`tempfile` || exit

trap "rm -f -- '$tmp'" EXIT

xzcat $MA_FILE | bin/filter_lft.py | sort -u --parallel=4 > "$tmp"

head "$tmp"

cd $LTDIR

# Synthesizer dictionary:
# The Java program outputs temporary files in /tmp which sucks
rm -v -f /tmp/SynthDictionaryBuilder*.txt_tags.txt
rm -v -f /tmp/DictionaryBuilder*.dict


# Building the binary POS dictionary
java -cp languagetool.jar org.languagetool.dev.POSDictionaryBuilder "$tmp" $SKDIR/org/languagetool/resource/sk/slovak.info

mv -v /tmp/DictionaryBuilder*.dict $SKDIR/org/languagetool/resource/sk/slovak.dict

# Building the binary synthesizer dictionary
java -cp languagetool.jar org.languagetool.dev.SynthDictionaryBuilder "$tmp" $SKDIR/org/languagetool/resource/sk/slovak_synth.info


mv -v /tmp/DictionaryBuilder*.dict $SKDIR/org/languagetool/resource/sk/slovak_synth.dict
mv -v /tmp/SynthDictionaryBuilder*.txt_tags.txt $SKDIR/org/languagetool/resource/sk/slovak_tags.txt


export MAVEN_OPTS='-Xmx512m -XX:MaxPermSize=256m'

# now you can build it with:
#cd $WORKDIR/../
#mvn clean package

rm -v -f -- "$tmp"
trap - EXIT
exit
