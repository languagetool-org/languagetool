#!/bin/bash

# german dictionary tracked in git
# english dictionaries from github.com/marcoagpinto/aoo-mozilla-en-dict

echo "Merge and truncate additional hunspell dictionary file and update binary morfologik spelling dictionary"
echo "This script assumes you have the full LanguageTool build environment"
echo "Please call this script from the LanguageTool top-level directory"
echo "Also ensure all files used are in UTF-8 encoding"
echo "Expects separate header files for .dic / spelling.txt"
echo ""

echo $#
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

CPATH=${REPO}/com/carrotsearch/hppc/0.7.1/hppc-0.7.1.jar:${REPO}/com/beust/jcommander/1.48/jcommander-1.48.jar:${REPO}/org/carrot2/morfologik-fsa-builders/2.1.2/morfologik-fsa-builders-2.1.2.jar:${REPO}/org/carrot2/morfologik-stemming/2.1.2/morfologik-stemming-2.1.2.jar:${REPO}/org/carrot2/morfologik-fsa/2.1.2/morfologik-fsa-2.1.2.jar:${REPO}/org/carrot2/morfologik-tools/2.1.2/morfologik-tools-2.1.2.jar:${REPO}/commons-cli/commons-cli/1.2/commons-cli-1.2.jar:languagetool-tools/target/languagetool-tools-${LT_VERSION}.jar
LANG_CODE=$1
COUNTRY_CODE=$2
PREFIX=${LANG_CODE}_${COUNTRY_CODE}
TOKENIZER_LANG=${LANG_CODE}-${COUNTRY_CODE}
CONTENT_DIR=languagetool-language-modules/${LANG_CODE}/src/main/resources/org/languagetool/resource/${LANG_CODE}/hunspell
INFO_FILE=${CONTENT_DIR}/${PREFIX}.info
DIC_NO_SUFFIX=${CONTENT_DIR}/${PREFIX}
DIC_FILE=${DIC_NO_SUFFIX}.dic
DIC_HEADER=${DIC_FILE}.header
ADDITIONAL_DIC_FILE=${CONTENT_DIR}/spelling.txt
ADDITIONAL_DIC_FILE_MERGED=${CONTENT_DIR}/spelling_merged.txt
ADDITIONAL_DIC_HEADER=${ADDITIONAL_DIC_FILE}.header
# get frequency data from https://github.com/mozilla-b2g/gaia/tree/master/apps/keyboard/js/imes/latin/dictionaries -
COUNTRY_CODE_LOWER=`echo ${COUNTRY_CODE} | tr '[:upper:]' '[:lower:]'`

if [ ! -f ${DIC_HEADER} ]; then
   echo "Header file for dictionary $DIC_HEADER does not exist; creating empty file"
   touch ${DIC_HEADER}
fi

if [ ! -f ${ADDITIONAL_DIC_HEADER} ]; then
   echo "Header file for additional dictionary $ADDITIONAL_DIC_HEADER does not exist; creating empty file"
   touch ${ADDITIONAL_DIC_HEADER}
fi

if [ ! -f ${ADDITIONAL_DIC_FILE_MERGED} ]; then
   echo "File for merged dictionary words $ADDITIONAL_DIC_FILE_MERGED does not exist; creating empty file"
   touch ${ADDITIONAL_DIC_FILE_MERGED}
fi

for file in "${CONTENT_DIR}/${LANG_CODE}_wordlist.xml" "CONTENT_DIR}/${LANG_CODE}_${COUNTRY_CODE}_wordlist.xml" "${CONTENT_DIR}/${LANG_CODE}_${COUNTRY_CODE_LOWER}_wordlist.xml";
do
    if [ -f ${file} ]; then
        FREQ_FILE="$file"
    fi
done


OUTPUT_FILE=${DIC_NO_SUFFIX}.dict

if [ ! -f ${ADDITIONAL_DIC_FILE} ]; then
    echo "File not found: $ADDITIONAL_DIC_FILE"
    exit
fi

ADDITIONAL_DIC_FILE_FILTERED=/tmp/additional_dic_filtered
ADDITIONAL_DIC_FILE_REST=/tmp/additional_dic_rest
echo "Filtering $ADDITIONAL_DIC_FILE..."
fgrep -v " " ${ADDITIONAL_DIC_FILE} > ${ADDITIONAL_DIC_FILE_FILTERED}
cat ${ADDITIONAL_DIC_FILE_FILTERED} >>${ADDITIONAL_DIC_FILE_MERGED}
fgrep " " ${ADDITIONAL_DIC_FILE} | grep -v "^#" >${ADDITIONAL_DIC_FILE_REST}

echo "Merging filtered $ADDITIONAL_DIC_FILE and $DIC_FILE.."
export LC_ALL="$PREFIX"
tail -n +2 ${DIC_FILE} >/tmp/dic_no_count
cat /tmp/dic_no_count ${ADDITIONAL_DIC_FILE_FILTERED} | grep -v "^#" | sort | uniq >${TEMP_FILE}
cat ${TEMP_FILE} | wc -l >${WORD_COUNT}
cat ${WORD_COUNT} ${DIC_HEADER} ${TEMP_FILE} >${DIC_FILE}
cat ${ADDITIONAL_DIC_HEADER} ${ADDITIONAL_DIC_FILE_REST} >${ADDITIONAL_DIC_FILE}
echo "Saved result."

SUGGESTION_WORDS=${CONTENT_DIR}/suggestions.txt

echo "Unmunching and filtering..."

UNMUNCHED=/tmp/unmunch
unmunch ${DIC_NO_SUFFIX}.{dic,aff} >${UNMUNCHED}
EXTENDED_LIST=/tmp/extended
if [ -f ${SUGGESTION_WORDS} ]; then
    echo "Extending file with word list for suggestions"
    cat ${SUGGESTION_WORDS} ${UNMUNCHED} | grep -v "^#" | sort | uniq >${EXTENDED_LIST}
else
    echo "Continuining without extending word list for suggestions"
    cp ${UNMUNCHED} ${EXTENDED_LIST}
fi

TOKENIZED=/tmp/tokenized

if [ ${LANG_CODE} = "en" ]; then # list languagages that require tokenization here
    cat ${EXTENDED_LIST} | java -cp languagetool-standalone/target/LanguageTool-${LT_VERSION}/LanguageTool-${LT_VERSION}/languagetool.jar:languagetool-dev/target/languagetool-dev-${LT_VERSION}.jar org.languagetool.dev.archive.WordTokenizer ${LANG_CODE} | sort -u >${TOKENIZED}
else
    cp ${EXTENDED_LIST} ${TOKENIZED}
fi

if [ ${LANG_CODE} = "de" ]; then # list language that require filtering here
    cat ${TOKENIZED} | grep -v "^#" | hunspell -i utf8 -d ${DIC_NO_SUFFIX} -G -l >${FINAL_FILE}
else
    cat ${TOKENIZED} | grep -v "^#" >${FINAL_FILE}
fi

echo "Building morfologik dictionary..."

#mvn clean package -DskipTests
if [ -f ${FREQ_FILE} ]; then
  echo "Using frequency file..."
  java -cp ${CPATH}:languagetool-standalone/target/LanguageTool-${LT_VERSION}/LanguageTool-${LT_VERSION}/languagetool.jar:languagetool-standalone/target/LanguageTool-${LT_VERSION}/LanguageTool-${LT_VERSION}/libs/languagetool-tools.jar \
  org.languagetool.tools.SpellDictionaryBuilder -i ${FINAL_FILE} -info ${INFO_FILE} -o ${OUTPUT_FILE} -freq ${FREQ_FILE}
else
  echo "No frequency file found (expected @ $FREQ_FILE), continuing without it..."
  java -cp ${CPATH}:languagetool-standalone/target/LanguageTool-${LT_VERSION}/LanguageTool-${LT_VERSION}/languagetool.jar:languagetool-standalone/target/LanguageTool-${LT_VERSION}/LanguageTool-${LT_VERSION}/libs/languagetool-tools.jar \
  org.languagetool.tools.SpellDictionaryBuilder -i ${FINAL_FILE} -info ${INFO_FILE} -o ${OUTPUT_FILE}
fi

echo "Finished."
