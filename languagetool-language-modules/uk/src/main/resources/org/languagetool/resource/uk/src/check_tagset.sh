#!/bin/sh
#
# Generates list of uniq tags used in the dictionary and (if old file found generates the difference)
#

mv -f all_tags.txt all_tags.txt.old
sed -r "s/:/\n/g" ../ukrainian_tags.txt | sed -r "s/\&//g" | sort | uniq > all_tags.txt
diff all_tags.txt.old all_tags.txt > all_tags.diff
