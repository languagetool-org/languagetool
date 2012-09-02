#!/bin/sh

EXPECTED_ARGS=1
if [ $# -ne $EXPECTED_ARGS ]
then
  echo "Usage: `basename $0` <fsa_input_in_utf8>"
  echo "The input for this script is usually the output of prepare_fsa_format.sh"
  echo "A directory 's_fsa' must exist (or a link must exist) with the FSA from "
  echo "http://www.eti.pg.gda.pl/katedry/kiw/pracownicy/Jan.Daciuk/personal/fsa.html"
  exit 1
fi

LANG=POSIX
OUTPUT=src/resource/de/german.dict
cat $1 src/resource/de/added.txt | iconv -f utf8 -t latin1 | sort -u | gawk -f s_fsa/morph_data.awk | s_fsa/fsa_ubuild -O -o $OUTPUT
echo "Output written to $OUTPUT"
