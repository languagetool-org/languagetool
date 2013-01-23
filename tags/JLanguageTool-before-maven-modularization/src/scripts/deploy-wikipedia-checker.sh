#!/bin/sh

echo ""
echo "###"
echo "### Admin only - you will need the server password to deploy the code ###"
echo "### This will deploy code for automatic Wikipedia checking ###"
echo "### as seen on http://community.languagetool.org/corpusMatch/list?lang=en ###"
echo "### but it will not deploy the web-app ###"
echo "###"
echo ""
echo "### NOTE: You still need to call update-all.sh manually on the server ###"
echo ""
sleep 1

ant dist-standalone && \
  scp -i /home/dnaber/.ssh/openthesaurus dist/LanguageTool-[1-9].[0-9]*.zip languagetool@languagetool.org: && \
  ssh -i /home/dnaber/.ssh/openthesaurus languagetool@languagetool.org "unzip -d /home/languagetool/ltcommunity/corpus ~/LanguageTool-[1-9].[0-9]*.zip"
