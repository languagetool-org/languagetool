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

LANG_CODE=$1
COUNTRY_CODE=$2
TEMP_FILE=/tmp/lt-dictionary.dump

PREFIX=${LANG_CODE}_${COUNTRY_CODE}
TOKENIZER_LANG=${LANG_CODE}-${COUNTRY_CODE}
CONTENT_DIR=languagetool-language-modules/${LANG_CODE}/src/main/resources/org/languagetool/resource/$LANG_CODE/hunspell
INFO_FILE=${CONTENT_DIR}/${PREFIX}.info

echo "Using $CONTENT_DIR/$PREFIX.dic and affix $CONTENT_DIR/$PREFIX.aff..."

mvn clean package -DskipTests &&
 unmunch $CONTENT_DIR/$PREFIX.dic $CONTENT_DIR/$PREFIX.aff | \
 # unmunch doesn't properly work for languages with compounds, thus we filter
 # the result using grep:
 grep -v "^#" | grep -v "/" | grep -v "-" | recode latin1..utf8 >$TEMP_FILE

java -cp languagetool-standalone/target/LanguageTool-*/LanguageTool-*/languagetool.jar org.languagetool.dev.SpellDictionaryBuilder ${LANG_CODE}-${COUNTRY_CODE} $TEMP_FILE $INFO_FILE

rm $TEMP_FILE
