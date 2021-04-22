#!/bin/sh

LANG=POSIX
TMP_OUTPUT=inp1.txt
TMP_GET=input.txt
OUTPUT=russian.dict
OUTPUT_SYNTH=russian_synth.dict
rm $TMP_OUTPUT 
cat  $TMP_GET | sort -u >$TMP_OUTPUT
#create normal dictionary 
gawk -f morph_data.awk $TMP_OUTPUT | ./fsa_ubuild -O -o $OUTPUT
echo "Output written to $OUTPUT"
#create synthesis dictionary
gawk -f synteza.awk $TMP_OUTPUT | gawk -f morph_data.awk | sort -u | ./fsa_ubuild -O -o $OUTPUT_SYNTH
gawk -f tags.awk $TMP_OUTPUT |sort -u >tags_russian.txt
echo "Output written to $OUTPUT_SYNTH"
