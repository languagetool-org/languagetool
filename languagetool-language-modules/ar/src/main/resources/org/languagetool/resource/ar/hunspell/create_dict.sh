#!/bin/bash

echo "Create morfologik spelling dictionary, based on Hunspell dictionary"
echo "This script assumes you have the full LanguageTool build environment"
echo "Please call this script from the LanguageTool top-level directory"
echo "This script is still under experimentations"


REPO=${HOME}/.m2/repository
LT_VERSION=3.8-SNAPSHOT
LANG_CODE=ar
FILE=arabic


TEMP_FILE=/tmp/lt-arabic-dictionary.dump
CPATH=$REPO/com/carrotsearch/hppc/0.7.1/hppc-0.7.1.jar:$REPO/com/beust/jcommander/1.48/jcommander-1.48.jar:$REPO/org/carrot2/morfologik-fsa-builders/2.1.2/morfologik-fsa-builders-2.1.2.jar:$REPO/org/carrot2/morfologik-stemming/2.1.2/morfologik-stemming-2.1.2.jar:$REPO/org/carrot2/morfologik-fsa/2.1.2/morfologik-fsa-2.1.2.jar:$REPO/org/carrot2/morfologik-tools/2.1.2/morfologik-tools-2.1.2.jar:$REPO/commons-cli/commons-cli/1.2/commons-cli-1.2.jar:languagetool-tools/target/languagetool-tools-${LT_VERSION}.jar

CONTENT_DIR=languagetool-language-modules/${LANG_CODE}/src/main/resources/org/languagetool/resource/$LANG_CODE
OUTPUT_FILE=${CONTENT_DIR}/${FILE}.dict
INFO_FILE=${CONTENT_DIR}/${FILE}.info
DIC_NO_SUFFIX=$CONTENT_DIR/hunspell/$LANG_CODE
DIC_FILE=$DIC_NO_SUFFIX.dic
AFF_FILE=$DIC_NO_SUFFIX.aff

echo "Using $DIC_FILE and affix $AFF_FILE ..."

unmunch $DIC_FILE $AFF_FILE | \
 # unmunch doesn't properly work for languages with compounds, thus we filter
 # the result using hunspell:
 grep -v "^#" | hunspell -d $DIC_NO_SUFFIX -G -l >$TEMP_FILE

java -cp $CPATH:languagetool-standalone/target/LanguageTool-*/LanguageTool-*/languagetool.jar \
  org.languagetool.tools.SpellDictionaryBuilder -i $TEMP_FILE -info $INFO_FILE -o $OUTPUT_FILE 

rm $TEMP_FILE
