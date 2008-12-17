#!/bin/sh

LANG=POSIX
TMP_OUTPUT=eng1.txt
TMP_GET_UNC=english.txt
TMP_FINAL=eng2.txt
OUTPUT=english.dict
OUTPUT_SYNTH=english_synth.dict
rm $TMP_OUTPUT 
rm $TMP_GET_UNC
rm $TMP_FINAL
gawk -f filter_out.awk infl.txt part-of-speech.txt| gawk -f remap.awk >penn.txt 
cat penn.txt manually_added.txt | sort -u >$TMP_GET_UNC
cp $TMP_GET_UNC $TMP_FINAL
gawk -f get_unc.awk $TMP_FINAL |sort -u > $TMP_OUTPUT
#test
gawk -f test_dict.awk $TMP_OUTPUT
#create normal dictionary
cp $TMP_OUTPUT 
gawk -f morph_data.awk $TMP_OUTPUT | fsa_ubuild -O -o $OUTPUT
echo "Output written to $OUTPUT"
#create synthesis dictionary
gawk -f synteza.awk $TMP_OUTPUT | gawk -f morph_data.awk | sort -u | fsa_ubuild -O -o $OUTPUT_SYNTH
gawk -f tags.awk $TMP_OUTPUT |sort -u >english_tags.txt
echo "Output written to $OUTPUT_SYNTH"