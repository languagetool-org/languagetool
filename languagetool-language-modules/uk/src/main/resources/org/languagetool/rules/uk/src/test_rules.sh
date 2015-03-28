#!/bin/sh

./generate_replace.sh

export PATH=$PATH:~/bin
PWD=`pwd`
BASE="../../../../../../../../../.."
#cd $BASE
#./build.sh uk package -DskipTests
#./build.sh uk test
#export PATH=/usr/java/jdk1.7.0_75/bin:$PATH
cd $BASE/languagetool-language-modules/uk

if [ "$1" != "" ]; then
  FLAGS="-Dtest=$1"
fi


mvn $FLAGS compile test
cd $PWD
