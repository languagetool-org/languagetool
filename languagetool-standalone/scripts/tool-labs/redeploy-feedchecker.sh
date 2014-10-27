#!/bin/bash
# dnaber, 2014-10-20
# This script is running on WikiCheck Tool Labs.
# Re-deploy the command-line app to check Wikipedia Recent Changes.
# This is script is to be called by create-snapshot.sh from the main LanguageTool server.

WHOAMI=`whoami`
if [ $WHOAMI != "tools.languagetool" ]
then
  echo "This script is supposed to be run on Tool Labs as user 'languagetool'. Stopping."
  exit
fi

DOWNLOAD_DATE=`date +%Y%m%d`
#DOWNLOAD_DATE=20141019

cd ~/feedchecker && \
  wget https://languagetool.org/download/snapshots/LanguageTool-wikipedia-$DOWNLOAD_DATE-snapshot.zip && \
  rm -r LanguageTool-wikipedia_bak ; \
  mv LanguageTool-wikipedia LanguageTool-wikipedia_bak && \
  unzip LanguageTool-wikipedia-$DOWNLOAD_DATE-snapshot.zip && \
  rm LanguageTool-wikipedia-$DOWNLOAD_DATE-snapshot.zip && \
  mv LanguageTool-wikipedia-*-SNAPSHOT LanguageTool-wikipedia && \
  echo "Currently running jobs:" && \
  qstat && \
  echo "Restarting all command line feed checker apps..." && \
  ./restart-all.sh
qstat
echo "Done."
