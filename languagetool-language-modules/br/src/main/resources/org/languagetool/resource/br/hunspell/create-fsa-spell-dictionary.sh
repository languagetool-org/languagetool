#!/bin/sh

# Create the Breton FSA spelling dictionary from
# Hunspell dictionary.
# 
# Download morforlogik-distribution-1.5.4.zip at:
# http://sourceforge.net/projects/morfologik/files/morfologik-stemming/1.5.4/morfologik-distribution-1.5.4.zip/download?use_mirror=freefr
#
# Run: $ unzip morforlogik-distribution-1.5.4.zip
#
# Then run the script without argument, it will
# create the FSA spelling dictionary br_FR.dict.
#
# Author: Dominique Pelle <dominique.pelle@gmail.com>

file_oxt=difazier-an-drouizig-0_13.oxt
if [ ! -f $file_oxt ]; then
  # Download the Breton Hunspell dictionary, if not already done.
  wget http://extensions.libreoffice.org/extension-center/an-drouizig-breton-spellchecker/releases/0.13/$file_oxt
fi

# We're only interested in 2 files (*.aff and *.dic) files in the *.oxt zip file. 
unzip -o $file_oxt dictionaries/br_FR.aff dictionaries/br_FR.dic

# File .info is used by morfologik.
ln -sf br_FR.info .info

unmunch dictionaries/br_FR.dic dictionaries/br_FR.aff |
sed -e "s/'/’/g" |
LC_ALL=C sort |
java -jar morfologik-distribution-1.5.4/morfologik-tools-1.5.4-standalone.jar \
     fsa_build --sorted -f cfsa2 -o br_FR.dict
