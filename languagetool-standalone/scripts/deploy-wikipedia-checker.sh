#!/bin/sh

echo ""
echo "###"
echo "### Admin only - you will need the server password to deploy the code ###"
echo "### This will deploy code for automatic Wikipedia checking ###"
echo "### as seen on http://community.languagetool.org/corpusMatch/list?lang=en ###"
echo "### but it will not deploy the web-app ###"
echo "###"
echo ""
sleep 1

cd ../.. &&
  mvn --projects languagetool-wikipedia --also-make clean package -DskipTests &&
  scp languagetool-wikipedia/target/LanguageTool-wikipedia-[1-9].[0-9]*.zip languagetool@languagetool.org:/home/languagetool/ltcommunity/corpus/ &&
  cd - &&
  echo "###" &&
  echo "### NOTE: Now call these commands on the server (replacing X.Y): ###" &&
  echo ""
  echo "cd ~/ltcommunity/corpus/ && rm -rf LanguageTool_bak2 && mv LanguageTool_bak LanguageTool_bak2 && mv LanguageTool LanguageTool_bak" &&
  echo "unzip -d LanguageTool LanguageTool-wikipedia-X.Y-SNAPSHOT.zip && mv LanguageTool/LanguageTool-wikipedia-X.Y-SNAPSHOT/* LanguageTool/" &&
  echo "nohup ./update-all.sh &" &&
  echo ""
  echo "###" &&
  echo ""
