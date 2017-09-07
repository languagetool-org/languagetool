#!/usr/bin/env bash

# This script contains command for quick POS dictionary and SYNTH_ dictionary generation.
CORPUS_DIR=/opt/app/s/smd/ltagged

echo "Generating POS Serbian dictionary ..."

ltposdic -freq serbian-wordlist.xml -i ${CORPUS_DIR}/serbian-corpus.txt -info serbian.info -o serbian.dict

echo "Generating SYNTH Serbian dictionary ..."

ltsyndic -i ${CORPUS_DIR}/serbian-corpus.txt -info serbian-synth.info -o serbian_synth.info

# echo "Exporting dictionary for HunSpell ..."

# ltexpdic -i serbian.dict -info serbian.info -o /tmp/dictionary.txt