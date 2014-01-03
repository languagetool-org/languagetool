#!/bin/sh

CURRENT_DIR=`pwd`
CURRENT_BASE=`basename $CURRENT_DIR`
if [ "$(basename $CURRENT_DIR)" != 'scripts' ]; then
    echo "Error: Please start this script from inside the 'scripts' directory";
    exit 1;
fi

TARGET_FILE=LanguageTool-wikipedia-2.5-SNAPSHOT.zip

cd ../..
mvn clean package -DskipTests
scp languagetool-wikipedia/target/$TARGET_FILE languagetool@languagetool.org:feed-checker/languagetool/
echo "Now log on to the server and unzip feed-checker/languagetool/$TARGET_FILE"
