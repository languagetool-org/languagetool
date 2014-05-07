#!/bin/bash
if [ ! $# -eq 2 ] && [ ! $# -eq 3 ]; then
  echo Usage: ./ltdiff.bash old_tag new_tag\|trunk [lang]
  echo e.g. ./ltdiff.bash v2.3.1 v2.4
  echo "     ./ltdiff.bash languagetool-2.2 languagetool-parent-2.3"
  echo "     ./ltdiff.bash V_2_0 languagetool-2.1"
  echo "     ./ltdiff.bash V_1_6 V_1_7"
  echo "     ./ltdiff.bash V_1_7 trunk en"
  echo "see https://github.com/languagetool-org/languagetool/tags and http://sourceforge.net/p/languagetool/code/HEAD/tree/tags for a list of available tags"
  exit -1
fi

path_old="https://raw.github.com/languagetool-org/languagetool/$1/languagetool-language-modules/en/src/main/resources/org/languagetool/rules"
if [ $2 == "trunk" ]; then
  path_new="https://raw.github.com/languagetool-org/languagetool/master/languagetool-language-modules/en/src/main/resources/org/languagetool/rules"
else
  path_new="https://raw.github.com/languagetool-org/languagetool/$2/languagetool-language-modules/en/src/main/resources/org/languagetool/rules"
fi

# check whether the path exists; if it's not the case, we probably have to use the old paths
response=`curl -o /dev/null --silent --head --write-out '%{http_code}\n' $path_old/en/grammar.xml`
if [[ $response == "404" || $response == "500" ]]; then
  path_old="http://sourceforge.net/p/languagetool/code/HEAD/tree/tags/$1/languagetool-language-modules/en/src/main/resources/org/languagetool/rules"
  response=`curl -o /dev/null --silent --head --write-out '%{http_code}\n' $path_old/en/grammar.xml`
  if [[ $response == "404" || $response == "500" ]]; then
    path_old="http://sourceforge.net/p/languagetool/code/HEAD/tree/tags/$1/src/main/resources/org/languagetool/rules"
    response=`curl -o /dev/null --silent --head --write-out '%{http_code}\n' $path_old/en/grammar.xml`
    if [[ $response == "404" || $response == "500" ]]; then
      path_old="http://sourceforge.net/p/languagetool/code/HEAD/tree/tags/$1/src/rules"
      response=`curl -o /dev/null --silent --head --write-out '%{http_code}\n' $path_old/en/grammar.xml`
      if [[ $response == "404" || $response == "500" ]]; then
        echo -e "\033[40;1;31m ERROR \033[0m Could not find valid old_path for $1"
        exit -2
      fi
    fi
  fi
fi

response=`curl -o /dev/null --silent --head --write-out '%{http_code}\n' $path_new/en/grammar.xml`
if [[ $response == "404" || $response == "500" ]]; then
  path_new="http://sourceforge.net/p/languagetool/code/HEAD/tree/tags/$2/languagetool-language-modules/en/src/main/resources/org/languagetool/rules"
  response=`curl -o /dev/null --silent --head --write-out '%{http_code}\n' $path_new/en/grammar.xml`
  if [[ $response == "404" || $response == "500" ]]; then
    path_new="http://sourceforge.net/p/languagetool/code/HEAD/tree/tags/$2/src/main/resources/org/languagetool/rules"
    response=`curl -o /dev/null --silent --head --write-out '%{http_code}\n' $path_new/en/grammar.xml`
    if [[ $response == "404" || $response == "500" ]]; then
      path_new="http://sourceforge.net/p/languagetool/code/HEAD/tree/tags/$2/src/rules"
      response=`curl -o /dev/null --silent --head --write-out '%{http_code}\n' $path_new/en/grammar.xml`
      if [[ $response == "404" || $response == "500" ]]; then
        echo -e "\033[40;1;31m ERROR \033[0m Could not find valid new_path for $2"
        exit -3
      fi
    fi
  fi
fi

cd ../../../../..
mvn compile
if [ ! $? -eq 0 ]; then
  echo -e "\033[40;1;31m ERROR \033[0m 'mvn compile' failed"
  exit 1
fi
cd -

oldv=`echo $1 | sed "s/_/./g" | sed "s/V.//" | sed "s/v//" | sed "s/languagetool-//g" | sed "s/parent-//g"`
newv=`echo $2 | sed "s/_/./g" | sed "s/V.//" | sed "s/v//" | sed "s/languagetool-//g" | sed "s/parent-//g"`

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

# find all currently supported languages if no lang parameter is given
if [ $# -eq 2 ]; then
  langs=`ls -d ../../../../../../languagetool-language-modules/*/ -l | awk -F / '{print $(NF-1)}'`
  for l in $langs
  do
    langs=$langs" "`ls -d ../../../../../../languagetool-language-modules/$l/src/main/resources/org/languagetool/rules/*/*/ -l 2> /dev/null | awk -F / '{print $(NF-2)"/"$(NF-1)}'` # country variants
  done
  langs=`echo "$langs" | tr " " "\n" | sort |tr "\n" " "` # sort
else
  langs=$3
fi

for l in $langs
do
  if [ $l = "all" ]; then
    continue
  fi
  
  l_base=`echo $l | sed "s/\/.*//g"`
  l_variant=`echo $l | sed "s/.*\///g"`
  
  echo $(tput setaf 2)------------------
  echo $l \($l_base / $l_variant\)
  echo ------------------$(tput sgr0)
  
  m_path_old=`echo $path_old | sed -e "s|/en/|/$l_base/|g"`
  m_path_new=`echo $path_new | sed -e "s|/en/|/$l_base/|g"`
  wget $m_path_old/$l/grammar.xml?format=raw -O old
  wget $m_path_new/$l/grammar.xml?format=raw -O new
  
  # remove xml comments
  gawk -v RS='<!--|-->' 'NR%2' old > old~
  gawk -v RS='<!--|-->' 'NR%2' new > new~
  mv old~ old
  mv new~ new
  
  cd ../..
  
  java -cp ../../../target/classes/ org.languagetool.dev.VersionDiffGenerator $l_variant
  
  if [ ! $? -eq 0 ]; then
    echo  "\033[40;1;31m ERROR \033[0m ltdiff failed"
    exit 2
  fi
  
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
