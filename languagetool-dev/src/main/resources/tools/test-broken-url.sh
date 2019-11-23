#!/bin/bash

# This script checks whether links (<url>...</url>) in grammar.xml files
# are valid or broken.
#
# Author: Dominique Pellé <dominique.pelle@gmail.com>

# Array of URL already checked, to avoid checking the same URL multiple times.
declare -A urls

# Count number of distinct URL checked,
url_count=0

# Count of broken URL found.
broken_url_count=0

# Loop on all grammar.xml files...
for grammar_xml in $(find languagetool-language-modules/*/src -name grammar.xml); do
  printf "\rChecking [$grammar_xml]...\n"

  # Loop on all <url>...</url> tag in grammar file...
  for url in $(xmlstarlet c14n --without-comments $grammar_xml |
               egrep '<url>.*</url>' |
               sed -e 's:<url>::' \
                   -e 's:</url>::' \
                   -e 's:&amp;:\&:' \
                   -e 's:\\n::'); do

    # Have we checked this URL already?
    if [[ ${urls[$url]} != 1 ]]; then
      urls[$url]=1
      url_count=$((url_count + 1))

      # Use --user-agent or else a few website do not respond to wget.
      if wget --timeout 30 --quiet --user-agent firefox -O /dev/null $url; then
        # Show the number of URL checked so far as progress, since checking
        # many URL may be slow.
        printf "\r$url_count"
      else
        printf "\rFOUND BROKEN URL [%s]\n" "$url"
        broken_url_count=$((broken_url_count + 1))
      fi
    fi
  done
done

printf "\rSummary: checked [$url_count] URL, found [$broken_url_count] broken URL\n"
