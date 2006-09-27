#!/bin/sh

LANG=POSIX
OUTPUT=english.dict
gawk -f filter_infl.awk infl.txt part-of-speech.txt| gawk -f remap.awk >penn.txt 
#test
cat penn.txt manually_added.txt |gawk -f get_unc.awk | gawk -f test_dict.awk 
cat penn.txt manually_added.txt |gawk -f get_unc.awk | sort -u | gawk -f morph_data.awk | fsa_ubuild -O -o $OUTPUT
echo "Output written to $OUTPUT"
