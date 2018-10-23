#!/bin/bash

echo "Merge and truncate additional hunspell dictionary file and update binary morfologik spelling dictionary"
echo "This script assumes you have the full LanguageTool build environment"
echo "Please call this script from the LanguageTool top-level directory"
echo "Also ensure all files used are in UTF-8 encoding"
echo "Expects separate header files for .dic / spelling.txt"
echo ""

if [ $# -ne 2 ]
then
  SCRIPT=`basename $0`
  echo "Usage: $SCRIPT <langCode> <countryCode>"
  echo "  For example: $SCRIPT de AT"
  exit 1
fi

REPO="$HOME/.m2/repository"
LT_VERSION=4.4-SNAPSHOT
TEMP_FILE=/tmp/lt-dictionary.dump
WORD_COUNT=/tmp/wordcount
FINAL_FILE=/tmp/lt-dictionary.new

CPATH=$REPO/com/carrotsearch/hppc/0.7.1/hppc-0.7.1.jar:$REPO/com/beust/jcommander/1.48/jcommander-1.48.jar:$REPO/org/carrot2/morfologik-fsa-builders/2.1.2/morfologik-fsa-builders-2.1.2.jar:$REPO/org/carrot2/morfologik-stemming/2.1.2/morfologik-stemming-2.1.2.jar:$REPO/org/carrot2/morfologik-fsa/2.1.2/morfologik-fsa-2.1.2.jar:$REPO/org/carrot2/morfologik-tools/2.1.2/morfologik-tools-2.1.2.jar:$REPO/commons-cli/commons-cli/1.2/commons-cli-1.2.jar:languagetool-tools/target/languagetool-tools-${LT_VERSION}.jar
LANG_CODE=$1
COUNTRY_CODE=$2
PREFIX=${LANG_CODE}_${COUNTRY_CODE}
TOKENIZER_LANG=${LANG_CODE}-${COUNTRY_CODE}
CONTENT_DIR=languagetool-language-modules/${LANG_CODE}/src/main/resources/org/languagetool/resource/$LANG_CODE/hunspell
INFO_FILE=${CONTENT_DIR}/${PREFIX}.info
DIC_NO_SUFFIX=$CONTENT_DIR/$PREFIX
DIC_FILE=$DIC_NO_SUFFIX.dic
DIC_HEADER=${DIC_FILE}.header
ADDITIONAL_DIC_FILE=${CONTENT_DIR}/spelling.txt
ADDITIONAL_DIC_HEADER=${ADDITIONAL_DIC_FILE}.header
# get frequency data from https://github.com/mozilla-b2g/gaia/tree/master/apps/keyboard/js/imes/latin/dictionaries -
FREQ_FILE=${CONTENT_DIR}/${LANG_CODE}_wordlist.xml
OUTPUT_FILE=${DIC_NO_SUFFIX}.dict

if [ ! -f $ADDITIONAL_DIC_FILE ]; then
    echo "File not found: $ADDITIONAL_DIC_FILE"
    exit
fi

echo "Merging $ADDITIONAL_DIC_FILE and $DIC_FILE.."
export LC_ALL="$PREFIX"
cat $DIC_FILE $ADDITIONAL_DIC_FILE | grep -v "^#" | sort | uniq >$TEMP_FILE
cat $TEMP_FILE | wc -l >$WORD_COUNT
cat $WORD_COUNT $DIC_HEADER $TEMP_FILE >$DIC_FILE
cp $ADDITIONAL_DIC_HEADER $ADDITIONAL_DIC_FILE
echo "Saved result."

echo "Unmunching and filtering..."
unmunch ${DIC_NO_SUFFIX}.{dic,aff} | hunspell -d $DIC_NO_SUFFIX -G -l >$FINAL_FILE

echo "Building morfologik dictionary..."

mvn clean package -DskipTests
if [ -f $FREQ_FILE ]; then
  echo "Using frequency file..."
  java -cp $CPATH:languagetool-standalone/target/LanguageTool-$LT_VERSION/LanguageTool-$LT_VERSION/languagetool.jar:languagetool-standalone/target/LanguageTool-$LT_VERSION/LanguageTool-$LT_VERSION/libs/languagetool-tools.jar \
  org.languagetool.tools.SpellDictionaryBuilder -i $FINAL_FILE -info $INFO_FILE -o $OUTPUT_FILE -freq $FREQ_FILE
else
  echo "No frequency file found (expected @ $FREQ_FILE), continuing without it..."
  java -cp $CPATH:languagetool-standalone/target/LanguageTool-$LT_VERSION/LanguageTool-$LT_VERSION/languagetool.jar:languagetool-standalone/target/LanguageTool-$LT_VERSION/LanguageTool-$LT_VERSION/libs/languagetool-tools.jar \
  org.languagetool.tools.SpellDictionaryBuilder -i $FINAL_FILE -info $INFO_FILE -o $OUTPUT_FILE
fi

echo "Finished."
