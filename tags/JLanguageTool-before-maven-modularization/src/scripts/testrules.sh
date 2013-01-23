#!/bin/sh

java -cp junit.jar:LanguageTool.jar org.languagetool.rules.patterns.PatternRuleTest $@
java -cp junit.jar:LanguageTool.jar org.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest $@
java -cp junit.jar:LanguageTool.jar org.languagetool.rules.bitext.BitextPatternRuleTest $@
java -cp junit.jar:LanguageTool.jar org.languagetool.ValidateXMLTest $@
