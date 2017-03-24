#!/bin/sh


export PATH=$PATH:~/bin
PWD=`pwd`
BASE="../../../../../../../../../.."

cd $BASE/languagetool-language-modules/uk

if [ "$1" != "" ]; then
  FLAGS="-Dtest=$1"
fi

#FLAGS="$FLAGS -Dorg.languagetool.rules.uk.TokenInflectionAgreementRule.debug=true"
#FLAGS="$FLAGS -Dorg.languagetool.rules.uk.TokenVerbAgreementRule.debug=true"

mvn $FLAGS compile test
cd $PWD
