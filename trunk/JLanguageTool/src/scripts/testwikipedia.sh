#!/bin/sh

: ${3?"Usage: $0 <lang> <wikipediaXmlDump> <ruleIds> (where <lang> is a language code like 'en' or 'de', <ruleIds> is a comma-separated list of rules to be activated or '-' for the default rules)"}

java -Xmx512M -cp commons-lang-2.4.jar:bliki-3.0.3.jar:LanguageTool.jar org.languagetool.dev.wikipedia.CheckWikipediaDump - - $1 $2 $3
