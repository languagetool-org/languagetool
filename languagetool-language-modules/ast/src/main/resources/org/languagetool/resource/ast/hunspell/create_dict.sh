#!/bin/bash

# You need to have recode, hunspell and hunspell-tools installed.
# Set LT_VERSION to the version you are currently building on.
# Download or create the files for ADDITIONAL_DICT_FILE, UNKNOWN_TO_HUNSPELL and FREQ_FILE.

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
LT_VERSION=6.4-SNAPSHOT
# see https://sourceforge.net/projects/germandict/files/ (conversion to UTF-8 needed):
ADDITIONAL_DICT_FILE=/tmp/Addition_dict.txt
# see https://sourceforge.net/p/germandict/code/HEAD/tree/hunspell_words.txt?format=raw (conversion to UTF-8 needed):
UNKNOWN_TO_HUNSPELL=/tmp/Unknown_Hunspell.txt
# get frequency data from https://github.com/mozilla-b2g/gaia/tree/master/apps/keyboard/js/imes/latin/dictionaries -
# this is optional, remove "-freq $FREQ_FILE" below for not using frequencies:
FREQ_FILE=/tmp/empty
INPUT_ENCODING=utf8
OUTPUT_ENCODING=utf8
TEMP_FILE=/tmp/lt-dictionary.dump
FINAL_FILE=/tmp/lt-dictionary.new
OUTPUT_FILE=/tmp/ast_ES.dict

CPATH=$REPO/com/carrotsearch/hppc/0.8.2/hppc-0.8.2.jar:$REPO/org/carrot2/morfologik-fsa-builders/2.1.7/morfologik-fsa-builders-2.1.7.jar:$REPO/org/carrot2/morfologik-stemming/2.1.7/morfologik-stemming-2.1.7.jar:$REPO/org/carrot2/morfologik-fsa/2.1.7/morfologik-fsa-2.1.7.jar:$REPO/org/carrot2/morfologik-tools/2.1.7/morfologik-tools-2.1.7.jar:$REPO/commons-cli/commons-cli/1.4/commons-cli-1.4.jar:languagetool-tools/target/languagetool-tools-${LT_VERSION}.jar
LANG_CODE=$1
COUNTRY_CODE=$2
PREFIX=${LANG_CODE}_${COUNTRY_CODE}
TOKENIZER_LANG=${LANG_CODE}-${COUNTRY_CODE}
CONTENT_DIR=languagetool-language-modules/${LANG_CODE}/src/main/resources/org/languagetool/resource/$LANG_CODE/hunspell
INFO_FILE=${CONTENT_DIR}/${PREFIX}.info
DIC_NO_SUFFIX=$CONTENT_DIR/$PREFIX
DIC_FILE=$DIC_NO_SUFFIX.dic

if [ ! -f $ADDITIONAL_DICT_FILE ]; then
    echo "File not found: $ADDITIONAL_DICT_FILE"
    exit
fi
if [ ! -f $UNKNOWN_TO_HUNSPELL ]; then
    echo "File not found: $UNKNOWN_TO_HUNSPELL"
    exit
fi

echo "Using $CONTENT_DIR/$PREFIX.dic and affix $CONTENT_DIR/$PREFIX.aff..."

mvn clean package -DskipTests &&
 unmunch $DIC_FILE $CONTENT_DIR/$PREFIX.aff | \
 # unmunch doesn't properly work for languages with compounds, thus we filter
 # the result using hunspell:
 recode $INPUT_ENCODING..$OUTPUT_ENCODING | grep -v "^#" | hunspell -d $DIC_NO_SUFFIX -G -l >$TEMP_FILE

echo "Input sizes:"
wc -l $TEMP_FILE
wc -l $ADDITIONAL_DICT_FILE

sort $ADDITIONAL_DICT_FILE >/tmp/additional_dict_file_sorted
sort $UNKNOWN_TO_HUNSPELL >/tmp/unknown_to_hunspell_sorted
# remove the words that hunspell wouldn't accept (see https://github.com/languagetool-org/languagetool/issues/725#issuecomment-312961626):
comm -23 /tmp/additional_dict_file_sorted /tmp/unknown_to_hunspell_sorted >/tmp/additional_without_hunspell_unknown

cat /tmp/additional_without_hunspell_unknown $TEMP_FILE | sed 's/\r//' | sort | uniq >$FINAL_FILE
echo "Final size:"
wc -l $FINAL_FILE

java -cp $CPATH:languagetool-standalone/target/LanguageTool-$LT_VERSION/LanguageTool-$LT_VERSION/languagetool.jar:languagetool-standalone/target/LanguageTool-$LT_VERSION/LanguageTool-$LT_VERSION/libs/languagetool-tools.jar \
  org.languagetool.tools.SpellDictionaryBuilder -i $FINAL_FILE -info $INFO_FILE -o $OUTPUT_FILE #-freq $FREQ_FILE

rm $TEMP_FILE