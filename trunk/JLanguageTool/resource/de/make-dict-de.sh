#!/bin/sh

LANG=POSIX
OUTPUT=resource/de/german.dict
cat resource/de/morphy_fsa.txt resource/de/added.txt | sort -u | gawk -f s_fsa/morph_data.awk | s_fsa/fsa_ubuild -O -o $OUTPUT
echo "Output written to $OUTPUT"
