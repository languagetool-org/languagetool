#!/bin/sh

echo ""
echo "###"
echo "### Admin only - you will need the server password to deploy the code ###"
echo "### This will deploy the code for e.g. https://languagetool.org:8081/?text=foo&language=en-US ###"
echo "###"
echo ""
sleep 1

cd .. &&
  mvn clean package -DskipTests &&
  cd - &&
  scp target/LanguageTool-[1-9].[0-9]*.zip languagetool@languagetool.org: &&
  ssh languagetool@languagetool.org "unzip -d /home/languagetool/api ~/LanguageTool-[1-9].[0-9]*.zip && cp -r /home/languagetool/api/LanguageTool-[1-9].[0-9]*/* /home/languagetool/api/ && rm -rf /home/languagetool/api/LanguageTool-[1-9].[0-9]*/ && cd /home/languagetool/ && ./restart-api-server.sh"
