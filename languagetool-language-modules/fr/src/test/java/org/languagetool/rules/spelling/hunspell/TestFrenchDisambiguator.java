package org.languagetool.rules.spelling.hunspell;

import org.languagetool.AnalyzedSentence;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tagging.disambiguation.rules.DisambiguationRuleLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;


public class TestFrenchDisambiguator implements Disambiguator {

    @Override
    public AnalyzedSentence disambiguate(AnalyzedSentence input) throws IOException {
        AnalyzedSentence sentence = input;
        List<DisambiguationPatternRule> disambiguationRules = null;
        InputStream inputStream = null;
        final String filePath = "/disambiguator.xml";
        try {
            final DisambiguationRuleLoader ruleLoader = new DisambiguationRuleLoader();
            inputStream = getClass().getResourceAsStream(filePath);
            disambiguationRules = ruleLoader.getRules(inputStream);
            for (final DisambiguationPatternRule patternRule : disambiguationRules) {
                sentence = patternRule.replace(sentence);
            }

        } catch (Exception e) {
            throw new RuntimeException("Problems with loading disambiguation file: " + filePath, e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    // nothing to do
                }
            }
        }

        return sentence;
    }

}
