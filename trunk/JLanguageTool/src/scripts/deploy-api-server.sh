#!/bin/sh

echo ""
echo "###"
echo "### Admin only - you will need the server password to deploy the code ###"
echo "### This will deploy the code for e.g. https://languagetool.org:8081/?text=foo&language=en-US ###"
echo "###"
echo ""
sleep 1

ant dist-standalone && \
  scp -i /home/dnaber/.ssh/openthesaurus dist/LanguageTool-[1-9].[0-9]-dev.zip languagetool@languagetool.org: && \
  ssh -i /home/dnaber/.ssh/openthesaurus languagetool@languagetool.org "unzip -d /home/languagetool/api ~/LanguageTool-[1-9].[0-9]-dev.zip && cp -r /home/languagetool/api/LanguageTool/* /home/languagetool/api/ && rm -rf /home/languagetool/api/LanguageTool/ && cd /home/languagetool/ && ./restart-api-server.sh"
