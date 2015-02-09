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

MFL_JAR_DIR="$HOME/work/ukr/spelling/grammar/morfologik-stemming/morfologik-tools/target"
MFL_JAR="morfologik-tools-*-standalone.jar"

MFL_CMD="java -jar $MFL_JAR_DIR/$MFL_JAR"

#MFL_CMD="mfl"

export LANG=POSIX
FSA_FLAGS="-f cfsa2"

# grep -v ":bad"


sed -r "/adjp/ s/:&adj//" tagged.main.txt | sed -r "/adjp/ s/adjp:....:..../adj/" > tagged.main.txt2

TAGGED_DICT="tagged.main.txt2"
#TAGGED_DICT="tagged.main.txt"


if [ "$2" != "-x" ]; then

  echo -e "\nGenerating POS dictionary"

  grep -h "^[^#].*[a-z]" $TAGGED_DICT | encode | tr ' ' '\t' | sort -u > all.tagged.tmp

  ( $MFL_CMD tab2morph -i all.tagged.tmp | \
  $MFL_CMD fsa_build $FSA_FLAGS -o ukrainian.dict 2>&1 | decode && \
  mv -f ukrainian.dict ../ ) 2>&1 > fsa_build.out &

  echo -e "\nGenerating synthesizer dictionary"

  ( cat all.tagged.tmp | awk -F '\t' '{print $2"|"$3"\t"$1"\t"}' | \
  $MFL_CMD tab2morph | \
  $MFL_CMD fsa_build $FSA_FLAGS -o ukrainian_synth.dict 2>&1 && \
  grep "^[^# ].*[a-z]" $TAGGED_DICT | awk '{ print $3 }' | sort | uniq > ukrainian_tags.txt && \
  mv -f ukrainian_synth.dict ukrainian_tags.txt ../ ) 2>&1 > fsa_build2.out  &

fi


if [ "$1" == "-f" ]; then
#    spell_uk_dir="$HOME/work/ukr/spelling/spell-uk"
#    cat $spell_uk_dir/test/all_aspell.srt | encode | LC_ALL=C sort -u > all.tagged.tmp && \

#    cat all_words.lst | encode | sort -u > all.tagged.tmp && \
#    cat all.tagged.tmp | $MFL_CMD fsa_build $FSA_FLAGS -o uk_UA.dict && \
#    mv uk_UA.dict ../hunspell/
    
    BASE="../../../../../../../../../.."
    LT_DIR=`ls $BASE/languagetool-standalone/target/LanguageTool-?.*-SNAPSHOT`
    LIBDIR="$BASE/languagetool-standalone/target/$LT_DIR/$LT_DIR/libs"
    for i in `ls $LIBDIR/*.jar`; do
      LIBS=$LIBS:$i
    done
    LIBS=$BASE/languagetool-language-modules/uk/target/classes:$BASE/languagetool-core/target/classes:$LIBS

    #LT_STD_CP="$BASE/languagetool-standalone/target/$LT_DIR/$LT_DIR//languagetool.jar"
    LT_STD_CP=$BASE/languagetool-standalone/target/classes:$LIBS

    (cat all_words.lst | encode | sort -u > all.words.tmp && \
    java -cp $LT_STD_CP org.languagetool.dev.SpellDictionaryBuilder uk-UA all.words.tmp ../hunspell/uk_UA.info freq/uk_wordlist.xml -o uk_UA.dict 2>&1 > fsa_spell.out && \
    mv uk_UA.dict ../hunspell/ ) &

fi

echo "Waiting for all threads to finish"
wait

rm -f all.words.tmp
rm -f all.tagged.tmp
#rm -f all_words.lst
rm -f tagged.main.txt2
