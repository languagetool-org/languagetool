#/bin/sh

export PATH=$PATH:~/bin

function encode() {
    iconv -f utf-8 -t cp1251
#    cat
}

#MFL_CMD="java -jar ./mfl/morfologik-tools-*-standalone.jar"
MFL_CMD="mfl"

LANG=POSIX
#FSA_FLAGS="-f cfsa2"

# grep -v ":bad"

if [ "$2" != "-x" ]; then

echo "Generating POS dictionary"

grep -h "^[^#].*[a-z]" tagged.*.txt | encode | tr ' ' '\t' | sort -u > all.tagged.tmp
$MFL_CMD tab2morph -i all.tagged.tmp | \
$MFL_CMD fsa_build $FSA_FLAGS -o ukrainian.dict

echo "Generating synthesizer dictionary"

cat all.tagged.tmp | awk -F '\t' '{print $2"|"$3"\t"$1"\t"}' | \
$MFL_CMD tab2morph | \
$MFL_CMD fsa_build $FSA_FLAGS -o ukrainian_synth.dict

rm -f all.tagged.tmp

grep "^[^#].*[a-z]" tagged.* | awk '{ print $3 }' | sort | uniq > ukrainian_tags.txt

fi


if [ "$1" == "-f" ]; then
    spell_uk_dir="/home/arysin/work/ukr/spelling/spell-uk"
    
    if [ "$2" != "-x" ]; then
        make -C $spell_uk_dir regtest
    fi
    
#    cat all_words.lst | encode | sort -u > all.tagged.tmp && \
    cat $spell_uk_dir/test/all_aspell.srt | encode | sort -u > all.tagged.tmp && \
    cat  all.tagged.tmp | #$MFL_CMD tab2morph -i all.tagged.tmp | \
    $MFL_CMD fsa_build $FSA_FLAGS -o uk_UA.dict && \
    mv uk_UA.dict ../hunspell/
    rm -f all.tagged.tmp
fi
