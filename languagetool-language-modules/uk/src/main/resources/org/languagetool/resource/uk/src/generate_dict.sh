#!/bin/sh

spell_uk_dir="/home/arysin/work/ukr/spelling/spell-uk"
FLAGS="-f"

mv -f tagged.main.txt tagged.main.txt.bak
mv -f tagged.main.dups.txt tagged.main.dups.txt.bak

make -C $spell_uk_dir/src/Dictionary uk_words.tag && \
make -C $spell_uk_dir/src/Affix uk_affix.tag && \
./parse.py && \
./make-dict-uk-mfl.sh $FLAGS && \
mv -f *.dict ukrainian_tags.txt ../

sort tagged.main.txt | uniq -d > tagged.main.dups.txt

diff tagged.main.txt.bak tagged.main.txt > t.diff
diff tagged.main.dups.txt.bak tagged.main.dups.txt > td.diff
