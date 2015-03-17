#!/bin/bash
# dnaber, 2015-03-17
# This script is running on community.languagetool.org.
# Re-deploy the command-line app to check Wikipedia Recent Changes.
# This is script is to be called by create-snapshot.sh.

WHOAMI=`whoami`
if [ $WHOAMI != "languagetool" ]
then
  echo "This script is supposed to be run as user 'languagetool'. Stopping."
  exit
fi

if [ $# -eq 1 ]
then
  DOWNLOAD_DATE=$1
else
  DOWNLOAD_DATE=`date +%Y%m%d`
fi
echo "Using snapshot date: $DOWNLOAD_DATE"

cd ~/feed-checker && \
  wget https://languagetool.org/download/snapshots/LanguageTool-wikipedia-$DOWNLOAD_DATE-snapshot.zip && \
  rm -r languagetool_bak ; \
  mv languagetool languagetool_bak && \
  unzip LanguageTool-wikipedia-$DOWNLOAD_DATE-snapshot.zip && \
  rm LanguageTool-wikipedia-$DOWNLOAD_DATE-snapshot.zip && \
  mv LanguageTool-wikipedia-*-SNAPSHOT languagetool && \
  echo "Restarting all command line feed checker apps..." && \
  ./restart-all.sh
echo "Done."
