#!/bin/sh

LANG=POSIX
OUTPUT=resource/en/english.dict
gawk -f filter_infl.awk infl.txt >infl_filter.txt
gawk -f remap.awk infl_filter.txt part-of-speech.txt >penn.txt
cat penn.txt manually_added.txt | sort -u | gawk -f s_fsa/morph_data.awk | s_fsa/fsa_ubuild -O -o $OUTPUT
echo "Output written to $OUTPUT"
