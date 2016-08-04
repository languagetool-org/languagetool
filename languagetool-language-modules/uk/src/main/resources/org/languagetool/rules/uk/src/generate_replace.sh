#!/bin/sh

spell_uk_dir="$HOME/work/ukr/spelling/dict_uk"
REPLACE_FILE="../replace.txt"

echo "# Simple replace table" > $REPLACE_FILE
echo "# Format: word=suggestion1|suggestion2|suggestion3..." >> $REPLACE_FILE
echo "" >> $REPLACE_FILE
echo "# TODO: add inflection support for suggestions" >> $REPLACE_FILE
echo "" >> $REPLACE_FILE

#grep " [^^:a-z]" $spell_uk_dir/src/Dictionary/twisters.lst | sed -r 's/^([^ \/]+)(\/[a-zA-Z0-9<>]+)?( +[a-z^:_]+)? +(.*)$/\1=\4/' >> $REPLACE_FILE
grep -h " #>" $spell_uk_dir/data/dict/twisters.lst | sed -r "s/^ \+cs=//" | sed -r "s/^([а-яіїєґ'-]+).*#> *(.*)(#ok:.*)?/\1=\2/i" >> $REPLACE_FILE

grep "=" $REPLACE_FILE | wc -l


REPLACE_FILE_2="../replace_soft.txt"

echo "# Simple replace table for soft suggestions" > $REPLACE_FILE_2
echo "# Format: word=suggestion1|suggestion2|suggestion3..." >> $REPLACE_FILE_2
echo "" >> $REPLACE_FILE_2

ls -1 $spell_uk_dir/data/dict/*.lst | grep -v twisters | xargs cat | grep -h " #>" | sed -r "s/^([а-яіїєґ'-]+).*#> *(.*)/\1=\2/i; s/ *[;,] */|/g" | sort >> $REPLACE_FILE_2

grep "=" $REPLACE_FILE_2 | wc -l


