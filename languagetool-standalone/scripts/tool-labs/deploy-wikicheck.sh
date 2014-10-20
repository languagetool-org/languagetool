#!/bin/sh
# dnaber, 2014-06-27
# This script is running on WikiCheck Tool Labs.
# Re-deploy the WikiCheck wep app.
# This is script is to be called by create-snapshot.sh from the main LanguageTool server.

WHOAMI=`whoami`
if [ $WHOAMI != "tools.languagetool" ]
then
  echo "This script is supposed to be run on Tool Labs as user 'languagetool'. Stopping."
  exit
fi

cd /data/project/languagetool

./update-config.sh

echo "This file will be deployed to Tomcat:"
ls -l languagetool-wikicheck-0.1.war

echo "Deployment will start in 5 seconds, press Ctrl-C now to abort..."
sleep 5

cd && ~/webservice -tomcat stop && rm -rf public_tomcat/webapps/* && mv languagetool-wikicheck-0.1.war public_tomcat/webapps/languagetool.war && ~/webservice -tomcat start
