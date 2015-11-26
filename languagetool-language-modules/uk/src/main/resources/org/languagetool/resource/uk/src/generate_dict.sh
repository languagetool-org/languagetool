#!/bin/sh

FLAGS="-f"

BASE="../../../../../../../../../../.."

if [ "$1" == "-corp" ]; then
    DICT=$BASE/../dict_uk/out/prev/dict_corp_lt.txt
else
    DICT=$BASE/../dict_uk/out/prev/dict_rules_lt.txt
fi

ln -sf $DICT tagged.main.txt
ln -sf $BASE/../dict_uk/out/prev/words_spell.txt words.txt

./make-dict-uk-mfl.sh $FLAGS

wait
