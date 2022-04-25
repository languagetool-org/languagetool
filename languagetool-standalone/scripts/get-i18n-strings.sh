#!/bin/bash
# prints the i18n strings for one language as (mostly) plain text, e.g. to run it through LT

if [ $# -eq 0 ]
  then
    echo "No arguments supplied, expected language code"
    exit
fi

# for English: ../../languagetool-core/src/main/resources/org/languagetool/MessagesBundle.properties
native2ascii -reverse ../../languagetool-language-modules/$1/src/main/resources/org/languagetool/MessagesBundle_$1.properties | \
  sed 's/.*= //' | grep -v "^#" | awk ' {print;} { print ""; }'| sed 's/&//' | sed 's/\\n/\n/g'
