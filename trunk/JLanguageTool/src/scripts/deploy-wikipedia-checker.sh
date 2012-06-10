#!/bin/sh

echo ""
echo "###"
echo "### Admin only - you will need the server password to deploy the code ###"
echo "###"
echo ""
sleep 1

ant dist-standalone && \
  scp -i /home/dnaber/.ssh/openthesaurus dist/LanguageTool-[1-9].[0-9]*.zip languagetool@83.169.5.38: && \
  ssh -i /home/dnaber/.ssh/openthesaurus languagetool@83.169.5.38 "unzip -d /home/languagetool/ltcommunity/corpus/code ~/LanguageTool-[1-9].[0-9]*.zip"
