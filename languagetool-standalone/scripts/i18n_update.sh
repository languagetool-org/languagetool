#!/bin/bash
# Download latest translations from Transifex and copy them over the existing local files.

CURRENT_DIR=`pwd`
CURRENT_BASE=`basename $CURRENT_DIR`
if [ "$(basename $CURRENT_DIR)" != 'scripts' ]; then
    echo "Error: Please start this script from inside the 'scripts' directory";
    exit 1;
fi

# Transifex username and password
USERNAME=dnaber
PASSWORD=`cat ~/.transifex_password`

rm -I i18n-temp
mkdir i18n-temp
cd i18n-temp

SOURCE=downloaded.tmp

# List of languages in the same order as on https://www.transifex.com/projects/p/languagetool/:
# Do not list 'en', it's the source and taken from git. 
for lang in ast be br ca zh da nl eo fr gl de el_GR it pl ru sl es tl uk ro sk sv is lt km pt_PT pt_BR ta fa sr
do
  shortCode=$(echo "$lang" | sed -e 's/_.*//')
  curl --user $USERNAME:$PASSWORD https://www.transifex.com/api/2/project/languagetool/resource/messagesbundleproperties/translation/$lang/?file >$SOURCE
  sed -i 's/\\\\/\\/g' $SOURCE
  recode latin1..utf8 $SOURCE
  TARGET="../../../languagetool-language-modules/${shortCode}/src/main/resources/org/languagetool/MessagesBundle_${lang}.properties"
  SOURCE2=downloaded.tmp.ascii
  native2ascii -encoding utf-8 $SOURCE >$SOURCE2  
  # ignore new strings not translated yet (Transifex adds them, but commented out):
  modified_lines=`diff $TARGET $SOURCE2 | grep "^[<>]" | grep "^[<>] [a-zA-Z]"|wc -l`
  if [ $modified_lines -ne "0" ]; then
    # fix the comment for English, which doesn't make sense for the translations:
    sed -i "s/^# English translation of LanguageTool/# DO NOT MODIFY MANUALLY - all changes are done at https:\/\/www.transifex.com\/projects\/p\/languagetool\//" $SOURCE2
    sed -i "s/^# Copyright (C).*/# Copyright (C) 2006-2015 the LanguageTool team (http:\/\/www.languagetool.org)/" $SOURCE2
    echo "Moving $SOURCE2 to $TARGET ($modified_lines lines modified)"
    mv $SOURCE2 $TARGET
  else
    echo "No real modification in $lang"
    rm $SOURCE
    rm $SOURCE2
  fi
done

cp ../../../languagetool-core/src/main/resources/org/languagetool/MessagesBundle.properties ../../../languagetool-core/src/main/resources/org/languagetool/MessagesBundle_en.properties

cd ..
rm -r i18n-temp
