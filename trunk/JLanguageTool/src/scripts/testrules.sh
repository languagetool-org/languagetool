#!/bin/sh

java -cp junit.jar:LanguageTool.jar de.danielnaber.languagetool.rules.patterns.PatternRuleTest $@
java -cp junit.jar:LanguageTool.jar de.danielnaber.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest $@
java -cp junit.jar:LanguageTool.jar de.danielnaber.languagetool.rules.bitext.BitextPatternRuleTest $@
java -cp junit.jar:LanguageTool.jar de.danielnaber.languagetool.ValidateXMLTest $@
