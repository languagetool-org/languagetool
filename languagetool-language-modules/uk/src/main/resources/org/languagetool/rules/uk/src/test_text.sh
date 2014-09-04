#!/bin/sh

BASE="../../../../../../../../../../"
BASE1="$BASE/languagetool-commandline/target/classes"
BASE2="$BASE/languagetool-core/target/classes"
BASE3="$BASE/languagetool-language-modules/en/target/classes"
BASE4="$BASE/languagetool-language-modules/uk/target/classes"

#LIBDIR="$BASE/languagetool-standalone/target/LanguageTool-2.2-SNAPSHOT/LanguageTool-2.2-SNAPSHOT/libs"
#LIBS=`ls $LIBDIR | tr '\n' ':'`

CPATH=.libs/lucene-gosen-ipadic.jar:libs/ictclas4j.jar:libs/cjftransform.jar:libs/jwordsplitter.jar:libs/commons-logging.jar:libs/segment.jar:libs/morfologik-fsa.jar:libs/morfologik-speller.jar:libs/morfologik-stemming.jar:libs/commons-lang.jar


#RULES_TO_FIX="UPPERCASE_SENTENCE_START,DOUBLE_PUNCTUATION"
RULES_TO_IGNORE="MORFOLOGIK_RULE_UK_UA,COMMA_PARENTHESIS_WHITESPACE,WHITESPACE_RULE,EUPHONY,UK_MIXED_ALPHABETS,UK_SIMPLE_REPLACE"
#RULES_DONE="BILSHE_WITH_NUMERICS,COMMA_BEFORE_BUT,INSERTED_WORDS_NO_COMMA"

function run_lt() 
{
  SRC="$1"
    java -cp $BASE1:$BASE2:$BASE3:$BASE4:$CPATH org.languagetool.commandline.Main -l uk -d $RULES_TO_IGNORE,$RULES_TO_FIX,$RULES_DONE $SRC | \
      sed -r "s/^[0-9]+\.\) //" | grep -vE "^Line|Suggestion" > checked$ID.out

#    java -cp $BASE1:$BASE2:$BASE3:$BASE4:$CPATH org.languagetool.commandline.Main -l uk -d $RULES_TO_IGNORE,$RULES_TO_FIX,$RULES_DONE $SRC | \
#       grep -vE "Suggestion|Expected text|Working on" | sed -r "s/.*Rule ID:/#/g" | tr '\n' '@' | tr '#' '\n' | sort | sed -r "s/@@/@/g" | tr '@' '\n' > checked$ID.sorted.txt
}


SRC_BASE="$HOME/work/ukr/spelling/media"
SRCS="dt.txt.clean tyzhden/td.txt um/um.txt vz/vz.txt"

ID_TO_CHECK="$1"

#if [ "$SRC" == "" ] || [ "$ID" == "" ]; then
#    echo "Usage $0 <file> <id>"
#    exit 1
#fi

echo $BASE
echo $LIBS

#mv -f checked.out checked.out.bak

if [ "$ID_TO_CHECK" == "0" ]; then
    ID=$ID_TO_CHECK
    SRC=text4.txt
    echo "Checking $SRC [$ID]"

    run_lt $SRC

    diff checked$ID.out.bak checked$ID.out > checked$ID.out.diff
    exit
fi


ID=1
for src_file in $SRCS; do

  if [ "$ID_TO_CHECK" == "" ] || [ "$ID" == "$ID_TO_CHECK" ]; then

    SRC=$SRC_BASE/$src_file
    
    echo "Checking $SRC [$ID]"

    run_lt $SRC

    diff checked$ID.out.bak checked$ID.out > checked$ID.out.diff

  fi

  (( ID = ID + 1))
done
