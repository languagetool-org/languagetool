#!/usr/bin/env bash

oldDirectory=$(ls -1d LanguageTool*)
if [ $(echo $oldDirectory | wc -l) == 1 ]; then
  echo "Remove old release directory $oldDirectory"
  rm -rf $(echo $oldDirectory)
fi

if [ ! $(ls -1 languagetool-standalone/target/LanguageTool*.zip | wc -l) == 1 ]; then
  echo "No release package exist."
  exit 1
fi

unzip -oq $(ls -1 languagetool-standalone/target/LanguageTool*.zip)

releaseDirectory=$(ls -1d LanguageTool*)
if [ ! $(echo $releaseDirectory | wc -l) == 1 ]; then
  echo "No (or more than one) release directory exist"
  exit 1
fi
