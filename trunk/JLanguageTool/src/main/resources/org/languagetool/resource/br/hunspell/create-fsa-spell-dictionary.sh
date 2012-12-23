#!/bin/sh

# Create the Breton FSA spelling dictionary from
# Hunspell dictionary.
#
# Run the script without argument, it will
# create the FSA spelling dictionary br_FR.dict.
#
# Author: Dominique Pelle <dominique.pelle@gmail.com>


if [ ! -f dict-br_0.10.oxt ]; then
  # Download the Breton Hunspell dictionary, if not already done.
  wget http://extensions.libreoffice.org/extension-center/an-drouizig-breton-spellchecker/pscreleasefolder.2012-08-22.5582266403/0.10/dict-br_0.10.oxt
fi

# We're only interested in 2 files (*.aff and *.dic) files in the *.oxt zip file. 
unzip -o dict-br_0.10.oxt dictionaries/br_FR.aff dictionaries/br_FR.dic

# File .info is used by morfologik.
ln -s br_FR.info .info

unmunch dictionaries/br_FR.dic dictionaries/br_FR.aff |
sed -e "s/'/’/g" |
LC_ALL=C sort |
java -jar morfologik-distribution-1.5.4/morfologik-tools-1.5.4-standalone.jar \
     fsa_build --sorted -f cfsa2 -o br_FR.dict
