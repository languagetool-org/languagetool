#!/bin/bash
# checks if <url>s are still valid (i.e. if html response code is 200)
if [ ! $# -eq 1 ]; then
  echo Usage:
  echo ./checkurl.bash lang\|all
  echo e.g. ./checkurl.bash en
  exit 0
fi

if [ $1 == "all" ]; then
  langs=`ls -d ../../../../../languagetool-language-modules/*/src/main/resources/org/languagetool/rules/*/ -l | awk -F / '{print $(NF-1)}'`
  langs=$langs" "`ls -d ../../../../../languagetool-language-modules/*/src/main/resources/org/languagetool/rules/*/*/ -l | awk -F / '{print $(NF-2)"/"$(NF-1)}'` # country variants
  langs=`echo "$langs" | tr " " "\n" | sort |tr "\n" " "` # sort
else
  langs=$1
fi

for lang in $langs
do
  i=0

  for url in `grep \<url\> ../../../../../languagetool-language-modules/*/src/main/resources/org/languagetool/rules/$lang/grammar.xml | sed -r "s/.*<url>(.*)<\/url>/\1/g"`
  do
    response=`curl -o /dev/null --silent --head --write-out '%{http_code}\n' $url`
    if [ ! $response == "200" ]; then
      echo response for $url was $response
      i=$(($i+1))
    fi
  done

  echo $i of `grep \<url\> ../../../../../languagetool-language-modules/*/src/main/resources/org/languagetool/rules/$lang/grammar.xml | wc -l` \<url\>s in $lang/grammar.xml are problematic
done

exit 0
