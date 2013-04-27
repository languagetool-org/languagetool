#/bin/sh

function encode() {
    iconv -f utf-8 -t cp1251
#    cat
}

#MFL_CMD="java -jar ./mfl/morfologik-tools-*-standalone.jar"
MFL_CMD="mfl"

LANG=POSIX
#FSA_FLAGS="-f cfsa2"

grep -h "^[^#].*[a-z]" tagged.*.txt | encode | tr ' ' '\t' | sort -u > all.tagged.tmp
$MFL_CMD tab2morph -i all.tagged.tmp | \
$MFL_CMD fsa_build $FSA_FLAGS -o ukrainian.dict

echo "Generating synthesizer dictionary"

awk -F '\t' '{print $2"|"$3"\t"$1"\t"}' all.tagged.tmp | \
$MFL_CMD tab2morph | \
$MFL_CMD fsa_build $FSA_FLAGS -o ukrainian_synth.dict

rm -f all.tagged.tmp

grep "^[^#].*[a-z]" tagged.* | awk '{ print $3 }' | sort | uniq > ukrainian_tags.txt
