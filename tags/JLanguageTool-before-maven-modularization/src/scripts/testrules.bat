@echo off
java -cp junit.jar;LanguageTool.jar org.languagetool.rules.patterns.PatternRuleTest %1
java -cp junit.jar;LanguageTool.jar org.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest %1
java -cp junit.jar;LanguageTool.jar org.languagetool.rules.bitext.BitextPatternRuleTest %1
java -cp junit.jar;LanguageTool.jar org.languagetool.ValidateXMLTest %1
