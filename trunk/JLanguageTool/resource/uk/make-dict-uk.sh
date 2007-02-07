#!/bin/sh

LANG=POSIX
OUTPUT=ukrainian.dict
cat manually_added.txt | fsa_ubuild -O -o $OUTPUT
echo "Output written to $OUTPUT"
