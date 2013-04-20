#/bin/sh

function encode() {
    iconv -f utf-8 -t cp1251
#    cat
}

MFL_JAR="./mfl/morfologik-tools-*-standalone.jar"

LANG=POSIX
#FSA_FLAGS="-f cfsa2"

grep -h "^[^#].*[a-z]" tagged.*.txt | encode | tr ' ' '\t' | sort -u > all.tagged.tmp
java -jar $MFL_JAR tab2morph -i all.tagged.tmp | \
java -jar $MFL_JAR fsa_build $FSA_FLAGS -o ukrainian.dict

echo "Generating synthesizer dictionary"

awk -F '\t' '{print $2"|"$3"\t"$1"\t"}' all.tagged.tmp | \
java -jar $MFL_JAR tab2morph | \
java -jar $MFL_JAR fsa_build $FSA_FLAGS -o ukrainian_synth.dict

rm -f all.tagged.tmp

grep "^[^#].*[a-z]" tagged.* | awk '{ print $3 }' | sort | uniq > ukrainian_tags.txt
