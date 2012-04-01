#!/bin/bash
javac ltdiff.java -Xlint:deprecation

if [ ! $# -eq 2 ]; then
  echo Usage: ./ltdiff.bash old_branch new_branch
  echo e.g. ./ltdiff.bash V_1_6 V_1_7
  exit -1
fi

i=1
while read line
do
  string[$i]=$line
  i=$((i+1));
done<gen.txt

cat changes_a.html | sed "s/1title/${string[1]}/g" | sed "s/2intro/${string[2]}/g" | sed "s/3nothing/${string[3]}/g" > changes.html

mkdir changes

for l in `ls -d ../src/rules/*/ -l | awk -F / '{print $(NF-1)}'`
# for l in de en
do
  echo $(tput setaf 2)------------------
  echo $l
  echo ------------------$(tput sgr0)
  
  wget http://languagetool.svn.sourceforge.net/viewvc/languagetool/branches/$1/src/rules/$l/grammar.xml -O old
  wget http://languagetool.svn.sourceforge.net/viewvc/languagetool/branches/$2/src/rules/$l/grammar.xml -O new
  diff -c old new > grammar_$l.xml.diff
  
  java ltdiff $l
  
  # read translated strings
  i=1
  if [ -f $l.txt ]; then
    tf=$l.txt
  else
    tf=gen.txt
  fi
  
  while read line
  do
    string[$i]=$line
    i=$((i+1));
  done<$tf
  
  new_count=`grep "4NEWRULE" changes_$l.html | wc -l`
  removed_count=`grep "5REMOVEDRULE" changes_$l.html | wc -l`
  
  mv changes_$l.html changes_$l.html~
  cat changes_a.html | sed "s/1title/${string[1]}/g" | sed "s/2intro/${string[2]}/g" | sed "s/3nothing/${string[3]}/g" > changes_$l.html
  cat changes_$l.html~ | sed "s/4NEWRULE/${string[4]}/g" | sed "s/5REMOVEDRULE/${string[5]}/g" >> changes_$l.html
  cat changes_b.html >> changes_$l.html
  
  if [ ! $new_count -eq 0 ]; then
    new_count="<b>$new_count</b>"
  fi
  if [ ! $removed_count -eq 0 ]; then
    removed_count="<b>$removed_count</b>"
  fi
  echo "<tr class=\"lang\"><td><a href=\"changes_$l.html\">$l</a></td><td>$new_count new rules, $removed_count rules removed</td></tr>" >> changes.html
  
  rm changes_$l.html~
  rm old
  rm new
  rm grammar_$l.xml.diff
  
  mv changes_$l.html changes
done

cat changes_b.html >> changes.html

mv changes.html changes/index.html
cp ltdiff.css changes
rm -r ../website/www/changes
mv -i changes ../website/www

exit 0
