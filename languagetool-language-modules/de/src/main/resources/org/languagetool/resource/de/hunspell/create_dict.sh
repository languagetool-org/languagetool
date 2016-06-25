#!/bin/bash

echo "Create morfologik spelling dictionary, based on Hunspell dictionary"
echo "This script assumes you have the full LanguageTool build environment"
echo "Please call this script from the LanguageTool top-level directory"
echo ""

if [ $# -ne 2 ]
then
  SCRIPT=`basename $0`
  echo "Usage: $SCRIPT <langCode> <countryCode>"
  echo "  For example: $SCRIPT de AT"
  exit 1
fi

REPO=/home/dnaber/.m2/repository
LT_VERSION=3.3-SNAPSHOT
# get frequency data from https://github.com/mozilla-b2g/gaia/tree/master/apps/keyboard/js/imes/latin/dictionaries -
# this is optional, remove "-freq $FREQ_FILE" below for not using frequencies:
FREQ_FILE=de_wordist.xml
INPUT_ENCODING=latin1
OUTPUT_ENCODING=utf8
TEMP_FILE=/tmp/lt-dictionary.dump
OUTPUT_FILE=/tmp/out.dict

CPATH=$REPO/com/carrotsearch/hppc/0.5.3/hppc-0.5.3.jar:$REPO/org/carrot2/morfologik-stemming/1.9.0/morfologik-stemming-1.9.0.jar:$REPO/org/carrot2/morfologik-fsa/1.9.0/morfologik-fsa-1.9.0.jar:$REPO/org/carrot2/morfologik-tools/1.9.0/morfologik-tools-1.9.0.jar:$REPO/commons-cli/commons-cli/1.2/commons-cli-1.2.jar:languagetool-tools/target/languagetool-tools-${LT_VERSION}.jar
LANG_CODE=$1
COUNTRY_CODE=$2
PREFIX=${LANG_CODE}_${COUNTRY_CODE}
TOKENIZER_LANG=${LANG_CODE}-${COUNTRY_CODE}
CONTENT_DIR=languagetool-language-modules/${LANG_CODE}/src/main/resources/org/languagetool/resource/$LANG_CODE/hunspell
INFO_FILE=${CONTENT_DIR}/${PREFIX}.info
DIC_NO_SUFFIX=$CONTENT_DIR/$PREFIX
DIC_FILE=$DIC_NO_SUFFIX.dic

echo "Using $CONTENT_DIR/$PREFIX.dic and affix $CONTENT_DIR/$PREFIX.aff..."

mvn clean package -DskipTests &&
 unmunch $DIC_FILE $CONTENT_DIR/$PREFIX.aff | \
 # unmunch doesn't properly work for languages with compounds, thus we filter
 # the result using hunspell:
 recode $INPUT_ENCODING..$OUTPUT_ENCODING | grep -v "^#" | hunspell -d $DIC_NO_SUFFIX -G -l >$TEMP_FILE

java -cp $CPATH:languagetool-standalone/target/LanguageTool-*/LanguageTool-*/languagetool.jar \
  org.languagetool.tools.SpellDictionaryBuilder -i $TEMP_FILE -info $INFO_FILE -o $OUTPUT_FILE -freq $FREQ_FILE

rm $TEMP_FILE
