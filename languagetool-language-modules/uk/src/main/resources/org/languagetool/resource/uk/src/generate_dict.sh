#!/bin/sh

spell_uk_dir=$HOME"/work/ukr/spelling/spell-uk"
FLAGS="-f"

function regtest() {
 echo "Running regression diffs..."
 sort tagged.main.txt > tagged.main.sorted.tmp
 uniq -d tagged.main.sorted.tmp > tagged.main.dups.txt && diff tagged.main.dups.txt.bak tagged.main.dups.txt > td.diff
 uniq -u tagged.main.sorted.tmp > tagged.main.uniq.txt && diff tagged.main.uniq.txt.bak tagged.main.uniq.txt > tu.diff
}

#[ -s tagged.main.txt ] && mv -f tagged.main.txt tagged.main.txt.bak
#[ -s tagged.main.dups.txt ] && mv -f tagged.main.dups.txt tagged.main.dups.txt.bak
#[ -s tagged.main.uniq.txt ] && mv -f tagged.main.uniq.txt tagged.main.uniq.txt.bak

make -C $spell_uk_dir/src/Dictionary uk_words.tag && \
make -C $spell_uk_dir/src/Affix uk_affix.tag && \
time $spell_uk_dir/bin/tag/mk_pos_dict.py

if [ "$?" == "0" ]; then
  regtest &
  ./make-dict-uk-mfl.sh $FLAGS
fi

#
# Generates list of uniq tags used in the dictionary and (if old file found generates the difference)
#

if [ "$?" == "0" ]; then

mv -f temp/all_tags.txt temp/all_tags.txt.old
cat  ../ukrainian_tags.txt | sed -r "s/:/\n/g" | sed -r "s/\&//g" | sort | uniq > temp/all_tags.txt
diff temp/all_tags.txt.old temp/all_tags.txt > temp/all_tags.diff
echo "Tagset diff:"
cat temp/all_tags.diff
echo "-------"

fi

wait
rm -f tagged.main.sorted.tmp
