#!/bin/sh
# Download latest translations from Transifex and copy them over the existing local files.

# Transifex username and password
USERNAME=dnaber
PASSWORD=fixme

rm -I i18n-temp
mkdir i18n-temp
cd i18n-temp

# list of languages in the same order as on https://www.transifex.com/projects/p/languagetool/:
for lang in en ast be br ca zh da nl eo fr gl de el_GR it pl ru sl es tl uk ro sk cs sv is lt km
do
  SOURCE=downloaded.tmp
  # download and hackish JSON cleanup:
  curl --user $USERNAME:$PASSWORD https://www.transifex.net/api/2/project/languagetool/resource/messagesbundleproperties/translation/$lang/?file >$SOURCE
  recode latin1..utf8 $SOURCE
  TARGET="../src/main/resources/org/languagetool/MessagesBundle_${lang}.properties"
  SOURCE2=downloaded.tmp.ascii
  native2ascii $SOURCE >$SOURCE2
  echo "Moving $SOURCE2 to $TARGET"
  mv $SOURCE2 $TARGET
done

cd ..
rm -r i18n-temp
