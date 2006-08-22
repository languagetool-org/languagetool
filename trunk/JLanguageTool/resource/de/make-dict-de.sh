#!/bin/sh

LANG=POSIX
OUTPUT=resource/de/german.dict
cat resource/de/morphy_fsa.txt resource/de/added.txt | sort -u | gawk -f ~/fsa/morph_data.awk | fsa_ubuild -O -o $OUTPUT
echo "Output written to $OUTPUT"
