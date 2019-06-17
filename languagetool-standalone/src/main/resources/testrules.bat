@echo off

SET CPATH=.;libs/logback-core.jar;libs/logback-classic.jar;libs/kryo.jar;libs/slf4j-api.jar;libs/xgboost-predictor.jar;libs/xgboost4j.jar;libs/commons-pool2.jar;libs/commons-text.jar;libs/xgboost-predictor.jar;libs/morfologik-ukrainian-lt.jar;libs/catalan-pos-dict.jar;libs/jackson-databind.jar;libs/jackson-core.jar;libs/jackson-annotations.jar;libs/german-pos-dict.jar;libs/morphology-el.jar;libs/openregex.jar;libs/guava.jar;libs/hppc.jar;libs/junit.jar;libs/languagetool-core.jar;libs/lucene-gosen-ipadic.jar;libs/ictclas4j.jar;libs/cjftransform.jar;libs/languagetool-core-tests.jar;libs/jwordsplitter.jar;libs/commons-lang3.jar;libs/commons-logging.jar;libs/segment.jar;libs/morfologik-fsa-builders.jar;libs/morfologik-fsa.jar;libs/morfologik-speller.jar;libs/morfologik-stemming.jar;libs/opennlp-chunk-models.jar;libs/opennlp-maxent.jar;libs/opennlp-postag-models.jar;libs/opennlp-tokenize-models.jar;libs/opennlp-tools.jar;libs/aho-corasick-double-array-trie.jar;libs/indriya.jar;libs/unit-api.jar;libs/uom-lib-common.jar;libs/jaxb-api.jar;libs/jaxb-core.jar;libs/jaxb-runtime.jar

java -cp %CPATH% org.languagetool.rules.patterns.PatternRuleTest %1
java -cp %CPATH% org.languagetool.tagging.disambiguation.rules.DisambiguationRuleTest %1
java -cp %CPATH% org.languagetool.rules.bitext.BitextPatternRuleTest %1
java -cp %CPATH% org.languagetool.ValidateFalseFriendsXmlTest
