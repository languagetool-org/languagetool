#!/bin/sh

spell_uk_dir="/home/arysin/work/ukr/spelling/spell-uk"
FLAGS="-f"

make -C $spell_uk_dir/src/Dictionary uk_words.tag && \
make -C $spell_uk_dir/src/Affix uk_affix.tag && \
./parse.py && \
./make-dict-uk-mfl.sh $FLAGS && \
mv -f *.dict ukrainian_tags.txt ../

