#!/bin/sh


export PATH=$PATH:~/bin
DIR=`pwd`
BASE="../../../../../../../../../.."



cd $BASE/languagetool-language-modules/uk

if [ "$1" != "" ]; then
  FLAGS="-Dtest=$1"
fi

#FLAGS="$FLAGS -Dlogback.configurationFile=$DIR/logback-uk-debug.xml"

mvn $FLAGS compile test
cd $DIR
