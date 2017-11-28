#!/usr/bin/env bash

# This program changes PoS tags to LT tags in all specified input files.
# It then concatenates all such files, creating one large file (word corpus)
# That large file can be used for LT routines
# that create POS dictionary and synth dictionary.

# Change to suit your needs
BASE_DIR=/opt/app/s/smd

# Word files in POS dictionary format:
# <word><tab><lemma><tab><pos tag>
INPUT_DIR=${BASE_DIR}/pos

# Word files with tags suitable for LT
# <word><tab><lemma><tab><LT tag>
OUTPUT_DIR=${BASE_DIR}/lt

# Directory with SED commands and our files
# with dialect-specific words
STATIC_DIR=${BASE_DIR}/static

# Author of this script split giant Serbian word corpus into smaller files.
# They are easier to manipulate.
# Each file contains words where lemma starts with file name part before "-".
# Example: words that start with letter "j" are in file ${INPUT_DIR}/je-words.txt

for file in a be ce ch de dje dzhe ef em en er es e ge ha i je ka ell lje nje o pe sha te tshe u ve ze zhe misc
do
    # Author keeps his added words in separate files with suffix "-srp"
    for fext in words names
    do
        FNAME=${file}-${fext}.txt
        INFILE=${INPUT_DIR}/${file}/${FNAME}

        if [ -s ${INFILE} ]; then
            echo "Tagging ${FNAME} with LT tags ..."
            ./pos2lt.py -i ${INPUT_DIR}/${file}/${FNAME} -o ${OUTPUT_DIR}
            RETVAL=$?

            if [ ${RETVAL} -eq 0 ]; then
                echo "Adding ${FNAME} to word corpus ..."
                cat ${OUTPUT_DIR}/${FNAME} >> ${OUTPUT_DIR}/serbian-corpus.tmp
                cut -f1 ${OUTPUT_DIR}/${FNAME} >> ${OUTPUT_DIR}/hunspell-serbian-corpus.tmp
            fi
            echo " "
        fi
    done
done

echo "Sorting corpuses ..."
sort -u ${OUTPUT_DIR}/serbian-corpus.tmp > ${OUTPUT_DIR}/serbian-corpus.txt
sort -u ${OUTPUT_DIR}/hunspell-serbian-corpus.tmp > ${OUTPUT_DIR}/hunspell-serbian-corpus.txt
rm -f ${OUTPUT_DIR}/serbian-corpus.tmp ${OUTPUT_DIR}/hunspell-serbian-corpus.tmp

echo "Serbian word corpus is ready in file '${OUTPUT_DIR}/serbian-corpus.txt'."
echo "Serbian word corpus for Hunspell is ready in file '${OUTPUT_DIR}/hunspell-serbian-corpus.txt'."
echo " "