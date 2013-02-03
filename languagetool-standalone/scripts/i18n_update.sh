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
PASSWORD=fixme

rm -I i18n-temp
mkdir i18n-temp
cd i18n-temp

SOURCE=downloaded.tmp

# list of languages in the same order as on https://www.transifex.com/projects/p/languagetool/:
for lang in en ast be br ca zh da nl eo fr gl de el_GR it pl ru sl es tl uk ro sk sv is lt km pt_PT pt_BR
do
  shortCode=$(echo "$lang" | sed -e 's/_.*//')
  # download and hackish JSON cleanup:
  curl --user $USERNAME:$PASSWORD https://www.transifex.net/api/2/project/languagetool/resource/messagesbundleproperties/translation/$lang/?file >$SOURCE
  recode latin1..utf8 $SOURCE
  TARGET="../../../languagetool-language-modules/${shortCode}/src/main/resources/org/languagetool/MessagesBundle_${lang}.properties"
  SOURCE2=downloaded.tmp.ascii
  native2ascii $SOURCE >$SOURCE2
  echo "Moving $SOURCE2 to $TARGET"
  mv $SOURCE2 $TARGET
done

cd ..
rm -r i18n-temp
