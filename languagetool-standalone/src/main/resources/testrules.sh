#!/bin/sh

CPATH=.:libs/junit.jar:libs/languagetool-core.jar:libs/lucene-gosen-ipadic.jar:libs/ictclas4j.jar:libs/cjftransform.jar:libs/languagetool-core-tests.jar:libs/jwordsplitter.jar:libs/commons-logging.jar:libs/segment.jar:libs/morfologik-fsa.jar:libs/morfologik-speller.jar:libs/morfologik-stemming.jar

java -cp $CPATH org.languagetool.rules.patterns.PatternRuleTest $@
java -cp $CPATH org.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest $@
java -cp $CPATH org.languagetool.rules.bitext.BitextPatternRuleTest $@
java -cp $CPATH org.languagetool.ValidateFalseFriendsXmlTest
