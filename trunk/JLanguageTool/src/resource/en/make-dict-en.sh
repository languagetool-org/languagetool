#!/bin/sh

LANG=POSIX
TMP_OUTPUT=english.txt
OUTPUT=english.dict
OUTPUT_SYNTH=english-synth.dict
gawk -f filter_out.awk infl.txt part-of-speech.txt| gawk -f remap.awk >penn.txt 
#test
cat penn.txt manually_added.txt |gawk -f get_unc.awk | gawk -f test_dict.awk 
cat penn.txt manually_added.txt |gawk -f get_unc.awk | sort -u >$TMP_OUTPUT
gawk -f morph_data.awk $TMP_OUTPUT | fsa_ubuild -O -o $OUTPUT
echo "Output written to $OUTPUT"
#create synthesis dictionary
gawk -f synteza.awk $TMP_OUTPUT | gawk -f morph_data.awk | sort -u | fsa_ubuild -O -o $OUTPUT_SYNTH
gawk -f tags.awk $TMP_OUTPUT |sort -u >english-tags.txt
echo "Output written to $OUTPUT_SYNTH"