#!/usr/bin/env bash

# This script contains command for quick POS dictionary and SYNTH_ dictionary generation.
CORPUS_FILE=/opt/app/s/smd/ltagged/serbian-corpus.txt

if [ ! -f ${CORPUS_FILE} ]; then
    echo "Serbian word corpus file ${CORPUS_FILE} does not exist, aborting ..."
    exit 1
fi

echo "Generating POS Serbian dictionary ..."
ltposdic -freq serbian-wordlist.xml -i ${CORPUS_FILE} -info serbian.info -o serbian.dict

echo "Generating SYNTH Serbian dictionary ..."
ltsyndic -i ${CORPUS_FILE} -info serbian_synth.info -o serbian_synth.dict

# echo "Exporting dictionary for HunSpell ..."
# ltexpdic -i serbian.dict -info serbian.info -o /tmp/dictionary.txt