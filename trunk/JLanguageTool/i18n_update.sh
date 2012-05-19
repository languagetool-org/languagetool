#!/bin/sh
# Download latest translations from Transifex and copy them over the existing local files.

# Transifex username and password
USERNAME=dnaber
PASSWORD=59,,867dfr

rm -I i18n-temp
mkdir i18n-temp
cd i18n-temp

for lang in ast be br ca cs da de eo es fr gl is it km lt nl pl ro ru sk sl sv tl uk zh
do
  SOURCE=downloaded.tmp
  # download and hackish JSON cleanup:
  curl --user $USERNAME:$PASSWORD https://www.transifex.net/api/2/project/languagetool/resource/messagesbundleproperties/translation/$lang/?file >$SOURCE
  recode latin1..utf8 $SOURCE
  TARGET="../src/java/org/languagetool/MessagesBundle_${lang}.properties"
  SOURCE2=downloaded.tmp.ascii
  native2ascii $SOURCE >$SOURCE2
  echo "Moving $SOURCE2 to $TARGET"
  mv $SOURCE2 $TARGET
done

cd ..
rm -r i18n-temp
