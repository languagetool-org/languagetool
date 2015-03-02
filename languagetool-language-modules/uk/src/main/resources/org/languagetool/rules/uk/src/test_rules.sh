#!/bin/sh

./generate_replace.sh

export PATH=$PATH:~/bin
PWD=`pwd`
BASE="../../../../../../../../../.."
#cd $BASE
#./build.sh uk package -DskipTests
#./build.sh uk test
cd $BASE/languagetool-language-modules/uk
mvn compile test
cd $PWD
