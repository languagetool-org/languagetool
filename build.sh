#!/usr/bin/env bash

self=$(basename $0)

if [ $# -lt 2 ]; then
  echo "Helps building parts of LanguageTool - for a complete build, run mvn directly"
  echo "Usage: $self <project> <goals...>"
  echo "Examples:"
  echo "  ./$self languagetool-standalone clean package (will package the standalone module)"
  echo "  ./$self languagetool-standalone clean package -DskipTests (as above but without running tests)"
  echo "  ./$self en clean test (will test the English module)"
  exit 1
fi

module=$1

if [ ! -d $module ]; then
  module_path="languagetool-language-modules/$module"
else
  module_path="$module"
fi

command="mvn --projects $module_path --also-make ${@:2}"
echo "Running: $command"

$command
exitcode=$?

# these don't work on their own, so delete them to avoid confusion:
rm languagetool-standalone/target/languagetool-standalone-*.jar 2> /dev/null
rm languagetool-wikipedia/target/languagetool-wikipedia-*.jar 2> /dev/null
rm languagetool-commandline/target/languagetool-commandline-*.jar 2> /dev/null

exit $exitcode
