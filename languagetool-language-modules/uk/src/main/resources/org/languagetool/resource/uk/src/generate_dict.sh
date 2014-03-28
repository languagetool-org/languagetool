#!/bin/sh

spell_uk_dir="/home/arysin/work/ukr/spelling/spell-uk"
FLAGS="-f"

mv -f tagged.main.txt tagged.main.txt.bak
mv -f tagged.main.dups.txt tagged.main.dups.txt.bak
mv -f tagged.main.uniq.txt tagged.main.uniq.txt.bak

make -C $spell_uk_dir/src/Dictionary uk_words.tag && \
make -C $spell_uk_dir/src/Affix uk_affix.tag && \
./parse.py && \
./make-dict-uk-mfl.sh $FLAGS && \
mv -f *.dict ukrainian_tags.txt ../

sort tagged.main.txt | uniq -d > tagged.main.dups.txt
sort tagged.main.txt | uniq -u > tagged.main.uniq.txt

diff tagged.main.uniq.txt.bak tagged.main.uniq.txt > tu.diff
diff tagged.main.dups.txt.bak tagged.main.dups.txt > td.diff
