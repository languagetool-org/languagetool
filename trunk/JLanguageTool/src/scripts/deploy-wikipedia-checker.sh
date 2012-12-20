#!/bin/sh

echo ""
echo "###"
echo "### Admin only - you will need the server password to deploy the code ###"
echo "###"
echo ""
sleep 1

ant dist-standalone && \
  scp -i /home/dnaber/.ssh/openthesaurus dist/LanguageTool-[1-9].[0-9]-dev.zip languagetool@languagetool.org: && \
  ssh -i /home/dnaber/.ssh/openthesaurus languagetool@languagetool.org "unzip -d /home/languagetool/ltcommunity/corpus ~/LanguageTool-[1-9].[0-9]-dev.zip"
