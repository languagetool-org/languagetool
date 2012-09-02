#!/bin/bash

EXPECTED_ARGS=1
if [ $# -ne $EXPECTED_ARGS ]
then
  echo "This script cleans up the Morphy export from the XML-like format to plain text."
  echo "Data source: http://www.danielnaber.de/morphologie/"
  echo "Call this script from the LanguageTool root directory."
  echo "Usage: `basename $0` {morpy_export_file}"
  exit 1
fi

cat $1 | 
  grep -v "^#" - | \
  grep -v "\*" - | \
  grep -v "\-\-" - | \
  sed 's/\r//' | \
  sed 's/\n\+/\n/' | \
  sed 's/<\/\?form>//g' | \
  sed 's/^\(.\+\)>\*\?\(.\+\)</\2 \1/' | \
  sed 's/<lemma //' | \
  sed 's/\/lemma>//' | \
  sed 's/\([a-z]\+\)=\([A-Z]\+\)/\2/g' | \
  sed 's/\([a-z]\+\)=\([A-Z0-9]\+\)/\2/g' | \
  sed 's/(//' | \
  sed 's/)//' | \
  awk -f src/resource/de/format_fsa.awk
