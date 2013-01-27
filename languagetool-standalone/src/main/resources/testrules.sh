#!/bin/sh

CPATH=.:junit.jar:languagetool-core.jar:lucene-gosen-ipadic.jar:ictclas4j.jar:cjftransform.jar:languagetool-core-tests.jar:jwordsplitter.jar:commons-logging.jar:segment.jar:morfologik-fsa.jar:morfologik-speller.jar:morfologik-stemming.jar

java -cp $CPATH org.languagetool.rules.patterns.PatternRuleTest $@
java -cp $CPATH org.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest $@
java -cp $CPATH org.languagetool.rules.bitext.BitextPatternRuleTest $@
