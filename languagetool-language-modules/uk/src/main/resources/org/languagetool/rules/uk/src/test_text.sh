#!/bin/sh

BASE="../../../../../../../../../.."
BASE1="$BASE/languagetool-commandline/target/classes"
BASE2="$BASE/languagetool-core/target/classes"
BASE3="$BASE/languagetool-language-modules/en/target/classes"
BASE4="$BASE/languagetool-language-modules/uk/target/classes"


LT_DIR=`ls $BASE/languagetool-standalone/target/LanguageTool-?.*-SNAPSHOT`
LIBDIR="$BASE/languagetool-standalone/target/$LT_DIR/$LT_DIR/libs"
#LIBS=`ls $LIBDIR | tr '\n' ':'`

CPATH=$LIBDIR/lucene-gosen-ipadic.jar:$LIBDIR/ictclas4j.jar:$LIBDIR/cjftransform.jar:$LIBDIR/jwordsplitter.jar:$LIBDIR/commons-logging.jar:$LIBDIR/segment.jar:$LIBDIR/morfologik-fsa.jar:$LIBDIR/morfologik-speller.jar:$LIBDIR/morfologik-stemming.jar:$LIBDIR/commons-lang.jar

# Profiling

# For JVisualVM
#JAVA_OPTS="-Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=y"
#JAVA_OPTS="-Xverify:none"

# For JMC
#JAVA_OPTS="-XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:+UnlockCommercialFeatures -XX:+FlightRecorder -XX:FlightRecorderOptions=defaultrecording=true,dumponexit=true,dumponexitpath=dumponexit.jfr"
#export PATH=/usr/java/latest/bin:$PATH

THREAD_CNT=`more /proc/cpuinfo | grep processor| wc -l`
(( THREAD_CNT += 2 ))
echo "Using $THREAD_CNT threads"
JAVA_OPTS="$JAVA_OPTS -Dorg.languagetool.thread_count_internal=$THREAD_CNT"


#RULES_TO_FIX="UPPERCASE_SENTENCE_START,DOUBLE_PUNCTUATION"
#RULES_TO_IGNORE="MORFOLOGIK_RULE_UK_UA,COMMA_PARENTHESIS_WHITESPACE,WHITESPACE_RULE,EUPHONY,UK_MIXED_ALPHABETS,UK_SIMPLE_REPLACE"
RULES_TO_IGNORE="MORFOLOGIK_RULE_UK_UA,COMMA_PARENTHESIS_WHITESPACE,WHITESPACE_RULE,UK_MIXED_ALPHABETS,UK_SIMPLE_REPLACE,UK_SIMPLE_REPLACE_SOFT,EUPHONY,INVALID_DATE,YEAR_20001,DATE_WEEKDAY1"
RULES_TO_IGNORE_FOR_GROUPED="MORFOLOGIK_RULE_UK_UA,COMMA_PARENTHESIS_WHITESPACE,WHITESPACE_RULE,UK_MIXED_ALPHABETS,UK_SIMPLE_REPLACE,UK_SIMPLE_REPLACE_SOFT,INVALID_DATE,YEAR_20001,DATE_WEEKDAY1"
#RULES_DONE="BILSHE_WITH_NUMERICS,COMMA_BEFORE_BUT,INSERTED_WORDS_NO_COMMA"

if echo "$@" | grep -q "\--group-rules"; then
  GROUP_RULES=1
fi

function run_lt() 
{
  SRC="$1"
  ID="$2"
    java $JAVA_OPTS -cp $BASE1:$BASE2:$BASE3:$BASE4:$CPATH -Dorg.languagetool.tagging.uk.UkrainianTagger.debugCompounds=false \
       org.languagetool.commandline.Main -l uk -d $RULES_TO_IGNORE $SRC | \
      sed -r "s/^[0-9]+\.\) //" | grep -vE "^Line|Suggestion" > checked$ID.out

#    java -cp $BASE1:$BASE2:$BASE3:$BASE4:$CPATH org.languagetool.commandline.Main -l uk -d $RULES_TO_IGNORE,$RULES_TO_FIX,$RULES_DONE $SRC | \
#       grep -vE "Suggestion|Expected text|Working on" | sed -r "s/.*Rule ID:/#/g" | tr '\n' '@' | tr '#' '\n' | sort | sed -r "s/@@/@/g" | tr '@' '\n' > checked$ID.sorted.txt
}

function run_lt_grouped() 
{
  SRC="$1"
  ID="$2"
  java -cp $BASE1:$BASE2:$BASE3:$BASE4:$CPATH org.languagetool.commandline.Main -l uk -d $RULES_TO_IGNORE_FOR_GROUPED,$RULES_TO_FIX,$RULES_DONE $SRC | \
      grep -vE "Suggestion|Expected text|Working on" | sed -r "s/.*Rule ID:/#/g" | tr '\n' '@' | tr '#' '\n' | sort | sed -r "s/@@/@/g" | tr '@' '\n' > checked$ID.grouped.txt
}

function run_full_test()
{
  SRC="$1"
  ID="$2"

  if [ "$GROUP_RULES" = "1" ]; then
    run_lt_grouped $SRC $ID
  else
    run_lt $SRC $ID
    diff checked$ID.out.bak checked$ID.out > checked$ID.out.diff
    [ -f compounds-unknown.txt ] && {
      mv compounds-unknown.txt tools/compounds-unknown.$ID.txt
      mv compounds-tagged.txt tools/compounds-tagged.$ID.txt
    }
  fi
  echo "Done [$ID]"
}


SRC_BASE="$HOME/work/ukr/spelling/media"
SRCS="tyzhden/td.txt um/um.txt dt.txt.clean vz/vz.txt ukrlit/ukr_lit.txt mandela/mandela.txt"

IDS_TO_CHECK="$@"

#if [ "$SRC" == "" ] || [ "$ID" == "" ]; then
#    echo "Usage $0 <file> <id>"
#    exit 1
#fi

echo $BASE
echo $LIBS

#mv -f checked.out checked.out.bak

if [ "$IDS_TO_CHECK" == "0" ]; then
    ID=$IDS_TO_CHECK
    SRC=text4.txt
    echo "Checking $SRC [$ID]"

    RULES_TO_IGNORE="MORFOLOGIK_RULE_UK_UA,COMMA_PARENTHESIS_WHITESPACE" #,WHITESPACE_RULE,UK_SIMPLE_REPLACE"

    run_lt $SRC $ID

    diff checked$ID.out.bak checked$ID.out > checked$ID.out.diff
    exit
fi


ID=1
for src_file in $SRCS; do

  if [ "$IDS_TO_CHECK" == "" ] || (echo $IDS_TO_CHECK | grep -q $ID) ; then

    SRC=$SRC_BASE/$src_file
    
    echo "Checking $SRC [$ID]"

    run_full_test $SRC $ID

  fi

  (( ID = ID + 1))
done

#echo "Waiting for all jobs to finish..."
#wait
