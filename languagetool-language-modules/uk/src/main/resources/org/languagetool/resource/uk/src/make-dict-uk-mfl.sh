#/bin/sh

export PATH=$PATH:~/bin

# Use Oracle's JDK
export PATH=/usr/java/latest/bin:$PATH
export JAVA_HOME=/usr/java/latest

DICT_ENCODING=cp1251
#DICT_ENCODING=

function encode() {
  if [ "$DICT_ENCODING" != "" ]; then
    iconv -f utf-8 -t $DICT_ENCODING
  else
    cat
  fi
}

function decode() {
  if [ "$DICT_ENCODING" != "" ]; then
    iconv -f $DICT_ENCODING
  else
    cat
  fi
}

MFL_JAR_DIR="$HOME/work/ukr/spelling/grammar/morfologik-distribution-1.9.0"
MFL_JAR="morfologik-tools-*.jar"
#MFL_CP=`ls $MFL_JAR_DIR/morfologik-tools-?.?.?-SNAPSHOT/morfologik-tools-?.?.?-SNAPSHOT/lib/*.jar | tr '\n' ':' `
#MFL_CP="$MFL_CP."

MFL_CMD="java -jar $MFL_JAR_DIR/$MFL_JAR"
echo $MFL_CMD

#MFL_CMD="mfl"

export LANG=POSIX
FSA_FLAGS="-f cfsa2"

# grep -v ":bad"


if [ "$1" == "-c" ]; then

  TAGGED_DICT="tagged.corpus.dict.txt"
  OUT_TAG_DICT_FILE="ukrainian.corpus.dict"

else

  INPUT_FILE=tagged.main.txt
  TAGGED_DICT="tagged.main.txt"
  OUT_TAG_DICT_FILE="ukrainian.dict"

fi


if [ "$2" != "-x" ]; then

  echo -e "\nGenerating POS dictionary: $OUT_TAG_DICT_FILE"

  grep -h "^[^#].*[a-z]" $TAGGED_DICT | encode | tr ' ' '\t' | sort -u > all.tagged.tmp

  ( $MFL_CMD tab2morph -i all.tagged.tmp | \
  $MFL_CMD fsa_build $FSA_FLAGS -o $OUT_TAG_DICT_FILE 2>&1 | decode > fsa_build.out && \
  mv -f $OUT_TAG_DICT_FILE ../ ) &


  if [ "$1" == "-c" ]; then
    wait
    rm -f all.tagged.tmp
    exit 0
  fi


  echo -e "\nGenerating synthesizer dictionary"

  ( cat all.tagged.tmp | awk -F '\t' '{print $2"|"$3"\t"$1"\t"}' | \
  $MFL_CMD tab2morph | \
  $MFL_CMD fsa_build $FSA_FLAGS -o ukrainian_synth.dict 2>&1 > fsa_build2.out && \
  grep "^[^# ].*[a-z]" $TAGGED_DICT | awk '{ print $3 }' | sort | uniq > ukrainian_tags.txt && \
  mv -f ukrainian_synth.dict ukrainian_tags.txt ../ ) &

fi


if [ "$1" == "-f" ]; then
#    spell_uk_dir="$HOME/work/ukr/spelling/spell-uk"
#    cat $spell_uk_dir/test/all_aspell.srt | encode | LC_ALL=C sort -u > all.tagged.tmp && \

#    cat all_words.lst | encode | sort -u > all.tagged.tmp && \
#    cat all.tagged.tmp | $MFL_CMD fsa_build $FSA_FLAGS -o uk_UA.dict && \
#    mv uk_UA.dict ../hunspell/

    echo -e "\nGenerating spelling dictionary"
    
    BASE="../../../../../../../../../.."
    LT_DIR=`ls $BASE/languagetool-standalone/target/LanguageTool-?.*-SNAPSHOT`
    LIBDIR="$BASE/languagetool-standalone/target/$LT_DIR/$LT_DIR/libs"
    for i in `ls $LIBDIR/*.jar`; do
      LIBS=$LIBS:$i
    done
    LIBS=$BASE/languagetool-language-modules/uk/target/classes:$BASE/languagetool-core/target/classes:$LIBS

    #LT_STD_CP="$BASE/languagetool-standalone/target/$LT_DIR/$LT_DIR//languagetool.jar"
    LT_STD_CP=$BASE/languagetool-standalone/target/classes:$LIBS

    WORD_LIST=words.txt
    (cat $WORD_LIST | grep -v "\.$" | encode | sort -u > all.words.tmp && \
    java -cp $LT_STD_CP org.languagetool.dev.SpellDictionaryBuilder uk-UA all.words.tmp ../hunspell/uk_UA.info freq/uk_wordlist.xml -o ../hunspell/uk_UA.dict 2>&1 > fsa_spell.out) &

fi

echo "Waiting for all threads to finish"
wait

rm -f all.words.tmp
rm -f all.tagged.tmp
