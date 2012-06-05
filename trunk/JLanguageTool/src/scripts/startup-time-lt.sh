#!/bin/sh
#
# This script measures how long it takes to start up
# LanguageTool with an empty input for each language.
# 3 samples are measured to avoid outliers.
# It also prints the number of XML rules for each language.
# 
# Example:
#
# $ cd languagetool/src/scripts
# $ ./startup-time-lt.sh
# 
# or to measure timing with a sentence "foo bar":
#
# $ ./startup-time-lt.sh "foo bar"
#
# Author: Dominique Pelle <dominique.pelle@gmail.com>

# An optional input sentence for measuring startup time.
# (empty sentence by default)
sentence="$1"

echo 'lang | #rules | startup time in sec (3 samples)'
echo '-----+--------+--------------------------------'

for l in ast be br ca cs da de nl el en eo es fr gl \
         is it km lt ml pl ro ru sk sl sv tl uk zh
do
  # count the number of rules.
  rule_count=$(grep -c '</rule>' ../../dist/rules/$l/grammar.xml)

  # measure startup time of LanguageTool with the
  # give sentence (empty by default).  3 samples
  # are measured to avoid outliers.
  startup_time=""
  for i in 1 2 3
  do
    startup_time="$startup_time $(echo $sentence | \
      time -p \
      java -jar ../../dist/LanguageTool.jar \
           -c utf-8 -l $l - 2>&1 | \
      awk '/^real/ {print $2}')"
  done
  printf "%4s | %6s | $startup_time\n" $l $rule_count
done
