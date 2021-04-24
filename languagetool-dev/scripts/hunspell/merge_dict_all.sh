#!/bin/bash

echo "Needs to be executed in the root directory of the LanguageTool repository."
echo "Please build LanguageTool before executing this via 'mvn clean package -DskipTests'"
NAME=`basename $0`

if [ $# -lt 2 ]; then
    echo "Usage: $NAME <langCode> <countryCode1> [countryCode2 ...]"
    echo "Example: $NAME de DE AT CH"
    exit
fi

LANG_CODE=$1
shift

CONTENT_DIR=languagetool-language-modules/${LANG_CODE}/src/main/resources/org/languagetool/resource/$LANG_CODE/hunspell/
SCRIPT_DIR=languagetool-dev/scripts/hunspell/
SCRIPT=${SCRIPT_DIR}merge_dict.sh
SPELLING_FILE=${CONTENT_DIR}spelling.txt
MERGED_FILE=${CONTENT_DIR}spelling_merged.txt

# prepare backup files for reset
cp ${SPELLING_FILE}{,.backup}
cp ${MERGED_FILE}{,.backup}

# go through all arguments except the first
while test ${#} -gt 0;
do
    COUNTRY_CODE=$1
    shift

    # execute merge
    $SCRIPT $LANG_CODE $COUNTRY_CODE
    if [ $# -ne 0 ]; then
        # reset updated files for every run but the last
        cp ${SPELLING_FILE}{.backup,}
        cp ${MERGED_FILE}{.backup,}
    else
        # clean up
        rm ${SPELLING_FILE}.backup
        rm ${MERGED_FILE}.backup
    fi

done
