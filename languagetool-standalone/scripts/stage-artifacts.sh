#!/bin/bash
# helper to stage LT releases to oss.sonatype.org
# See https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide
# Written because the automatic process didn't work: https://issues.sonatype.org/browse/OSSRH-7363

# set to 1 to list what will be signed and uploaded, set to 0 to actually sign and upload:
DRY_RUN=0
# set this to the version you want to release:
VERSION=5.6

CURRENT_DIR=`pwd`
CURRENT_BASE=`basename $CURRENT_DIR`
if [ "$(basename $CURRENT_DIR)" != 'scripts' ]; then
    echo "Error: Please start this script from inside the 'scripts' directory";
    exit 1;
fi

echo -n "Enter your gpg passphrase: "
read -s PASSPHRASE
echo

if [ $DRY_RUN -eq 1 ]
then
    echo "dry run, would upload parent pom: ../../pom.xml"
else
    mvn gpg:sign-and-deploy-file -Dgpg.passphrase=$PASSPHRASE -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=../../pom.xml -Dfile=../../pom.xml
fi

# The list of projects was copied from the top-level pom.xml:
# Note that we don't stage: languagetool-office-extension, languagetool-standalone, languagetool-commandline
for PROJECT in languagetool-core en fr de pl ca it br nl pt ru ast be zh da eo gl el ja km ro sk sl es sv tl uk fa ta ga ar de-DE-x-simple-language all languagetool-gui-commons languagetool-wikipedia languagetool-server languagetool-http-client
do

    if [ -d "../../languagetool-language-modules/$PROJECT" ]
    then
        echo "Running on languagetool-language-modules/$PROJECT..."
        JAR_FILE=../../languagetool-language-modules/$PROJECT/target/language-$PROJECT-$VERSION.jar
        SOURCE_FILE=../../languagetool-language-modules/$PROJECT/target/language-$PROJECT-$VERSION-sources.jar
        JAVADOC_FILE=../../languagetool-language-modules/$PROJECT/target/language-$PROJECT-$VERSION-javadoc.jar
        POM_FILE=../../languagetool-language-modules/$PROJECT/pom.xml
    else
        echo "Running on $PROJECT..."
        JAR_FILE=../../$PROJECT/target/$PROJECT-$VERSION.jar
        SOURCE_FILE=../../$PROJECT/target/$PROJECT-$VERSION-sources.jar
        JAVADOC_FILE=../../$PROJECT/target/$PROJECT-$VERSION-javadoc.jar
        POM_FILE=../../$PROJECT/pom.xml
    fi

    if [ ! -f $JAR_FILE ]
    then
        echo "the file does not exist, stopping: $JAR_FILE"
        exit
    fi
    if [ ! -f $POM_FILE ]
    then
        echo "the file does not exist, stopping: $POM_FILE"
        exit
    fi

    if [ $PROJECT != "all" ]
    then
        if [ ! -f $SOURCE_FILE ]
        then
            echo "the file does not exist, stopping: $SOURCE_FILE"
            exit
        else
            if [ $DRY_RUN -eq 1 ]
            then
                echo "dry run, would upload $SOURCE_FILE"
            else
                mvn gpg:sign-and-deploy-file -Dgpg.passphrase=$PASSPHRASE -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$POM_FILE -Dfile=$SOURCE_FILE -Dclassifier=sources
            fi
        fi
    fi
    
    if [ $PROJECT != "all" ]
    then
        if [ ! -f $JAVADOC_FILE ]
        then
            echo "the file does not exist, stopping: $JAVADOC_FILE"
            exit
        else
            if [ $DRY_RUN -eq 1 ]
            then
                echo "dry run, would upload $JAVADOC_FILE"
            else
                mvn gpg:sign-and-deploy-file -Dgpg.passphrase=$PASSPHRASE -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$POM_FILE -Dfile=$JAVADOC_FILE -Dclassifier=javadoc
            fi
        fi
    fi

    if [ $DRY_RUN -eq 1 ]
    then
        echo "dry run, would upload $JAR_FILE"
    else
        mvn gpg:sign-and-deploy-file -Dgpg.passphrase=$PASSPHRASE -Durl=https://oss.sonatype.org/service/local/staging/deploy/maven2/ -DrepositoryId=sonatype-nexus-staging -DpomFile=$POM_FILE -Dfile=$JAR_FILE
    fi

done
