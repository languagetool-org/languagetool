#!/bin/sh

: ${2?"Usage: $0 <lang> <wikipediaXmlDump> (where <lang> is a language code like 'en' or 'de')"}

java -cp commons-lang-2.4.jar:bliki-3.0.3.jar:LanguageTool.jar de.danielnaber.languagetool.dev.wikipedia.CheckWikipediaDump - $1 $2
