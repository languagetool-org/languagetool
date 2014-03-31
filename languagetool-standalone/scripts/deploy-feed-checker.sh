#!/bin/sh

CURRENT_DIR=`pwd`
CURRENT_BASE=`basename $CURRENT_DIR`
if [ "$(basename $CURRENT_DIR)" != 'scripts' ]; then
    echo "Error: Please start this script from inside the 'scripts' directory";
    exit 1;
fi

BASE_NAME=LanguageTool-wikipedia-2.6-SNAPSHOT
TARGET_FILE=$BASE_NAME.zip

cd ../..
mvn clean package -DskipTests
scp languagetool-wikipedia/target/$TARGET_FILE languagetool@languagetool.org:feed-checker/languagetool/
echo "Now log on to the server and execute:"
echo "  cd /home/languagetool/feed-checker/languagetool && rm -r LanguageTool-wikipedia_bak ; mv LanguageTool-wikipedia LanguageTool-wikipedia_bak && unzip $TARGET_FILE && mv $BASE_NAME LanguageTool-wikipedia"
echo "Then kill the three running processes:"
echo "  ps aux | grep java | grep AtomFeedCheckerCmd"
echo "  => Kill those PIDs"
echo "Restart the processes:"
echo "  cd /home/languagetool/feed-checker"
echo "  nohup ./check-wikipedia-feed-en.sh &"
echo "  nohup ./check-wikipedia-feed-de.sh &"
echo "  nohup ./check-wikipedia-feed-fr.sh &"
