#!/bin/sh
SRC="compounds-unknown.?.txt"

ls $SRC > /dev/null || exit 1

cat $SRC | grep -Eiv "[а-яіїєґ][A-Z]|[A-Z][а-яіїєґ]|№" | sort > all.srt.tmp
cat all.srt.tmp | grep -v "anim-" | uniq -c > compounds-unknown.txt
cat all.srt.tmp | grep " anim-" | awk '{print $1}' | uniq -c > compounds-unknown-anim-inanim.txt
cat all.srt.tmp | grep " inanim-" | awk '{print $1}' | uniq -c > compounds-unknown-inanim-anim.txt
rm all.srt.tmp
diff compounds-unknown.txt.bak compounds-unknown.txt > compounds-unknown.diff

SRC2="compounds-tagged.?.txt"
cat $SRC2 | sort | uniq > compounds-tagged.txt
diff compounds-tagged.txt.bak compounds-tagged.txt > compounds-tagged.diff
