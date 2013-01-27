@echo off

SET CPATH=.;junit.jar;languagetool-core.jar;lucene-gosen-ipadic.jar;ictclas4j.jar;cjftransform.jar;languagetool-core-tests.jar;jwordsplitter.jar;commons-logging.jar;segment.jar;morfologik-fsa.jar;morfologik-speller.jar;morfologik-stemming.jar

java -cp %CPATH% org.languagetool.rules.patterns.PatternRuleTest %1
java -cp %CPATH% org.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest %1
java -cp %CPATH% org.languagetool.rules.bitext.BitextPatternRuleTest %1
java -cp %CPATH% org.languagetool.ValidateXMLTest %1
