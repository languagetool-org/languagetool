#!/bin/sh


export PATH=$PATH:~/bin
PWD=`pwd`
BASE="../../../../../../../../../.."

cd $BASE/languagetool-language-modules/uk

if [ "$1" != "" ]; then
  FLAGS="-Dtest=$1"
fi


mvn $FLAGS compile test
cd $PWD
