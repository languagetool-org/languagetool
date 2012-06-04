#!/bin/bash
if [ ! $# -eq 2 ] && [ ! $# -eq 3 ]; then
  echo Usage: ./ltdiff.bash old_branch new_branch [lang]
  echo e.g. ./ltdiff.bash V_1_6 V_1_7
  echo "     ./ltdiff.bash V_1_7 trunk en"
  exit -1
fi

path_old="http://languagetool.svn.sourceforge.net/viewvc/languagetool/branches/$1/src/rules"
if [ $2 == "trunk" ]; then
  path_new="http://languagetool.svn.sourceforge.net/viewvc/languagetool/trunk/JLanguageTool/src/rules"
else
  path_new="http://languagetool.svn.sourceforge.net/viewvc/languagetool/branches/$2/src/rules"
fi

javac VersionDiffGenerator.java -Xlint:deprecation

if [ ! $? -eq 0 ]; then
  echo javac failed
  exit 1
fi

oldv=`echo $1 | sed "s/_/./g" | sed "s/V.//g"`
newv=`echo $2 | sed "s/_/./g" | sed "s/V.//g"`

folder=${1}_to_${2}

i=1
while read line
do
  string[$i]=$line
  i=$((i+1));
done<gen.txt

cat changes_a.html | sed "s/1title/${string[1]}/g" | sed "s/2intro/${string[2]}/g" | sed "s/3nothing/${string[3]}/g" | sed "s/0VERSION/$newv/g" > changes.html

rm -r $folder~
mv $folder $folder~
mkdir $folder

if [ $# -eq 2 ]; then
  langs=`ls -d ../../../rules/*/ -l | awk -F / '{print $(NF-1)}'`
  langs=$langs" "`ls -d ../../../rules/*/*/ -l | awk -F / '{print $(NF-2)"/"$(NF-1)}'` # country variants
  langs=`echo "$langs" | tr " " "\n" | sort |tr "\n" " "` # sort
else
  langs=$3
fi

for l in $langs
do
  l_base=`echo $l | sed "s/\/.*//g"`
  l_variant=`echo $l | sed "s/.*\///g"`
  
  echo $(tput setaf 2)------------------
  echo $l \($l_base / $l_variant\)
  echo ------------------$(tput sgr0)
  
  wget $path_old/$l/grammar.xml -O old
  wget $path_new/$l/grammar.xml -O new
  
  # remove xml comments
  gawk -v RS='<!--|-->' 'NR%2' old > old~
  gawk -v RS='<!--|-->' 'NR%2' new > new~
  mv old~ old
  mv new~ new
  
  cd ../..
  
  java tools.ltdiff.VersionDiffGenerator $l_variant
  
  mv changes_$l_variant.html tools/ltdiff/
  cd tools/ltdiff
  
  # read translated strings
  i=1
  if [ -f $l.txt ]; then
    tf=$l.txt
  elif [ -f $l_base.txt ]; then
    tf=$l_base.txt
  else
    tf=gen.txt
  fi
  
  while read line
  do
    string[$i]=$line
    i=$((i+1));
  done<$tf
  
  new_count=`grep "4NEWRULE" changes_$l_variant.html | wc -l`
  removed_count=`grep "5REMOVEDRULE" changes_$l_variant.html | wc -l`
  improved_count=`grep "6IMPROVEDRULE" changes_$l_variant.html | wc -l`
  
  mv changes_$l_variant.html changes_$l_variant.html~
  cat changes_a.html | sed "s/1title/${string[1]}/g" | sed "s/2intro/${string[2]}/g" | sed "s/3nothing/${string[3]}/g" | sed "s/0VERSION/$newv/g" > changes_$l_variant.html
  cat changes_$l_variant.html~ | sed "s/4NEWRULE/${string[4]}/g" | sed "s/5REMOVEDRULE/${string[5]}/g" | sed "s/6IMPROVEDRULE/${string[6]}/g" | sed "s/7FINDERR/${string[7]}/g" | sed "s/8FINDNOTERR/${string[8]}/g" >> changes_$l_variant.html
  cat changes_b.html >> changes_$l_variant.html
  
  if [ ! $new_count -eq 0 ]; then
    new_count="<b>$new_count</b>"
  fi
  if [ ! $removed_count -eq 0 ]; then
    removed_count="<b>$removed_count</b>"
  fi
  if [ ! $improved_count -eq 0 ]; then
    improved_count="<b>$improved_count</b>"
  fi
  echo "<tr class=\"lang\"><td><a href=\"changes_$l_variant.html\">$l_variant</a></td><td>$new_count new, $improved_count improved, $removed_count removed</td></tr>" >> changes.html
  
  rm changes_$l_variant.html~
  rm old
  rm new
  
  mv changes_$l_variant.html $folder
done

cat changes_b.html | sed "s\</div>\</div><div class=\"gray\">new: The rule did not exist in version $oldv, but does in version $newv. Examples of errors which the new rule can detect are shown while hovering over its name.<br/>improved: The rule in version $newv has more examples than the rule in version $oldv. The new examples are shown while hovering over the name of the rule.<br>removed: The rule did exist in version $oldv, but does not exist in version $newv. Usually this means that the error is now detected by a more general rule.</div>\g" >> changes.html

mv changes.html $folder/index.html
cp ltdiff.css $folder

exit 0
