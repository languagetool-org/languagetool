#!/usr/bin/env bash

# This program changes PoS tags to LT tags in all specified input files.
# It then concatenates all such files, creating one large file (word corpus)
# That large file can be used for LT routines
# that create POS dictionary and synth dictionary.

# Change to suit your needs

# Word files in POS dictionary format:
# <word><tab><lemma><tab><pos tag>
INPUT_DIR=/opt/app/s/smd/corpora

# Word files with tags suitable for LT
# <word><tab><lemma><tab><LT tag>
OUTPUT_DIR=/opt/app/s/smd/ltagged

# Deleting Serbian word corpus file
> ${OUTPUT_DIR}/serbian-corpus.txt

# Author of this script split giant Serbian corpus into smaller files.
# They are easier to manipulate.
# Each file contains words where lemma starts with file name part before "-".
# Example: words that start with letter "j" are in file ${INPUT_DIR}/je-words.txt

for file in a be ce ch de dje dzhe ef em en er es e ge ha i je ka lamda lje nje o pe sha te tshe u ve ze zhe
do
    for fext in words words-srp
    do
        FNAME=${file}-${fext}.txt
        INFILE=${INPUT_DIR}/${FNAME}

        if [ -s ${INFILE} ]; then
            echo "Processing ${FNAME} ..."
            ./pos2lt.py -i ${INFILE} -o ${OUTPUT_DIR}
            echo " "
            if [ $? -eq 0 ]; then
                cat ${OUTPUT_DIR}/${FNAME} >> ${OUTPUT_DIR}/serbian-corpus.txt
            fi
        fi
    done
done

echo "Serbian word corpus is ready in file '${OUTPUT_DIR}/serbian-corpus.txt'."
echo " "