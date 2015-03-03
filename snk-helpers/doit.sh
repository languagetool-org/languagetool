#!/bin/sh


set -e

# file with the lemma-form-tag information
LFT_FILE=/tmp/lemmaformtag.gz

if [ ! -f $LFT_FILE ]; then
  wget http://www.juls.savba.sk/~garabik/ma/lemmaformtag.txt.gz -O $LFT_FILE
fi

cd `dirname $0`

# working directory where this script is run from
WORKDIR=`pwd`

# directory where the working tree is kept
LTDIR=$WORKDIR/../languagetool-standalone/target/LanguageTool-2.9-SNAPSHOT/LanguageTool-2.9-SNAPSHOT
LTDIR=`readlink -m $LTDIR`

# directory where languagetool sk resources are kept
SKDIR=$WORKDIR/../languagetool-language-modules/sk/src/main/resources/
SKDIR=`readlink -m $SKDIR`


tmp=`tempfile` || exit

trap "rm -f -- '$tmp'" EXIT

zcat $LFT_FILE | ./filter_lft.py | sort -u --parallel=4 > "$tmp"

head "$tmp"

cd $LTDIR

# Synthesizer dictionary:
# The Java program outputs temporary files in /tmp which sucks
rm -f /tmp/SynthDictionaryBuilder*.txt_tags.txt
rm -f /tmp/DictionaryBuilder*.dict


# Building the binary POS dictionary
java -cp languagetool.jar org.languagetool.dev.POSDictionaryBuilder "$tmp" $SKDIR/org/languagetool/resource/sk/slovak.info

mv -v /tmp/DictionaryBuilder*.dict $SKDIR/org/languagetool/resource/sk/slovak.dict

# Building the binary synthesizer dictionary
java -cp languagetool.jar org.languagetool.dev.SynthDictionaryBuilder "$tmp" $SKDIR/org/languagetool/resource/sk/slovak_synth.info


mv -v /tmp/DictionaryBuilder*.dict $SKDIR/org/languagetool/resource/sk/slovak_synth.dict
mv -v /tmp/SynthDictionaryBuilder*.txt_tags.txt $SKDIR/org/languagetool/resource/sk/slovak_tags.txt




export MAVEN_OPTS='-Xmx512m -XX:MaxPermSize=256m'

#cd $WORKDIR/../
#mvn clean package

rm -v -f -- "$tmp"
trap - EXIT
exit
