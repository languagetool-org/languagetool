@echo off
java -cp junit.jar;LanguageTool.jar de.danielnaber.languagetool.rules.patterns.PatternRuleTest %1
java -cp junit.jar;LanguageTool.jar de.danielnaber.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest %1
java -cp junit.jar;LanguageTool.jar de.danielnaber.languagetool.rules.bitext.BitextPatternRuleTest %1
java -cp junit.jar;LanguageTool.jar de.danielnaber.languagetool.ValidateXMLTest %1
