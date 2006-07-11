#!/bin/sh

OUTPUT=resource/de/german.dict
cat resource/de/morphy.txt resource/de/morphy_additions.txt | sort -u | gawk -f s_fsa/morph_data.awk | s_fsa/fsa_ubuild -O -o $OUTPUT
echo "Output written to $OUTPUT"
