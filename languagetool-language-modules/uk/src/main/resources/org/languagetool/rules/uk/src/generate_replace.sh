#!/bin/sh

spell_uk_dir="$HOME/work/ukr/spelling/spell-uk"
REPLACE_FILE="../replace.txt"

echo "# Simple replace table" > $REPLACE_FILE
echo "# Format: word=suggestion1|suggestion2|suggestion3..." >> $REPLACE_FILE
echo "" >> $REPLACE_FILE
echo "# TODO: add inflection support" >> $REPLACE_FILE
echo "" >> $REPLACE_FILE

grep " " $spell_uk_dir/src/Dictionary/twisters.lst | sed -r 's/^([^ \/]+)(\/[a-zA-Z0<>]+)? +(.*)$/\1=\3/' >> $REPLACE_FILE

grep "=" $REPLACE_FILE | wc -l
