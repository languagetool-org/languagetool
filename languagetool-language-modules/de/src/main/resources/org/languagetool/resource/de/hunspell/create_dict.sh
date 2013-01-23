#!/bin/sh

echo "Create morfologik spelling dictionary, based on Hunspell dictionary"
echo "Please call this script from the LT top-level directory"
echo ""

: ${3? "Usage: create_dict.sh <langCode> <countryCode> <morfoLibPath> - example: 'create_dict.sh de AT de_DE /my/path/to/lib/morfologik-tools-1.5.2.jar'"}

LANG_CODE=$1
COUNTRY_CODE=$2
MORFO_LIB=$3

PREFIX=${LANG_CODE}_${COUNTRY_CODE}
TOKENIZER_LANG=${LANG_CODE}-${COUNTRY_CODE}
CONTENT_DIR=src/main/resources/org/languagetool/resource/$LANG_CODE/hunspell

echo "Using $CONTENT_DIR/$PREFIX.dic and affix $CONTENT_DIR/$PREFIX.aff..."
 
ant wtokenizer && \
 unmunch $CONTENT_DIR/$PREFIX.dic $CONTENT_DIR/$PREFIX.aff | \
 # unmunch doesn't properly work for languages with compounds, thus we filter
 # the result using grep:
 grep -v "^#" | grep -v "/" | grep -v "-" | recode latin1..utf8 | \
 java -jar dist/wordtokenizer.jar $TOKENIZER_LANG | \
 java -jar $MORFO_LIB fsa_build -f cfsa2 -o $CONTENT_DIR/$PREFIX.dict
 
echo "Done:"
ls -l $CONTENT_DIR/$PREFIX.dict
