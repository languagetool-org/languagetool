@echo off

SET CPATH=.;libs/junit.jar;libs/languagetool-core.jar;libs/lucene-gosen-ipadic.jar;libs/ictclas4j.jar;libs/cjftransform.jar;libs/languagetool-core-tests.jar;libs/jwordsplitter.jar;libs/commons-logging.jar;libs/segment.jar;libs/morfologik-fsa.jar;libs/morfologik-speller.jar;libs/morfologik-stemming.jar

java -cp %CPATH% org.languagetool.rules.patterns.PatternRuleTest %1
java -cp %CPATH% org.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest %1
java -cp %CPATH% org.languagetool.rules.bitext.BitextPatternRuleTest %1
java -cp %CPATH% org.languagetool.ValidateFalseFriendsXmlTest
