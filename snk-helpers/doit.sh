#!/bin/sh

set -e


# directory where the working tree is kept
LTDIR=/data/shared/projects/languagetool/languagetool/languagetool-standalone/target/LanguageTool-2.9-SNAPSHOT/LanguageTool-2.9-SNAPSHOT

# directory where languagetool sk resources are kept, absolute
SKDIR=/data/shared/projects/languagetool/git/languagetool/languagetool-language-modules/sk/src/main/resources/

cd `dirname $0`

tmp=`tempfile` || exit

trap "rm -f -- '$tmp'" EXIT

xzcat lemmaformtag.xz | ./filter_lft.py | sort -u > "$tmp"

head "$tmp"

cd $LTDIR

# Building the binary POS dictionary
java -cp languagetool.jar org.languagetool.dev.POSDictionaryBuilder "$tmp" $SKDIR/org/languagetool/resource/sk/slovak.info


# Building the binary synthesizer dictionary
java -cp languagetool.jar org.languagetool.dev.SynthDictionaryBuilder "$tmp" $SKDIR/org/languagetool/resource/sk/slovak_synth.info


rm -v -f -- "$tmp"
trap - EXIT
exit
