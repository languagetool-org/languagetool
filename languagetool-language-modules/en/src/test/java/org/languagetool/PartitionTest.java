package org.languagetool;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.English;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PartitionTest {

    @Test
    public void testCorrectGrammar() throws IOException {
        if (System.getProperty("disableHardcodedTests") == null) {
            JLanguageTool lt = new JLanguageTool(new AmericanEnglish());
            assertNoError("I was working late on my assignment.", lt);
        }
    }

    @Test
    public void testPresentAndPastGrammarError() throws IOException {
        if (System.getProperty("disableHardcodedTests") == null) {
            JLanguageTool lt = new JLanguageTool(new AmericanEnglish());
            assertOneError("I was worked and I was able to finish my assignment.", lt);
        }
    }

    @Test
    public void testYourGrammarError() throws IOException {
        //more error-free sentences to deal with possible regressions
        if (System.getProperty("disableHardcodedTests") == null) {
            JLanguageTool lt = new JLanguageTool(new AmericanEnglish());
            assertOneError("Your the best coder I have met.", lt);
        }
    }

    @Test
    public void testYouAreGrammarError() throws IOException {
        if (System.getProperty("disableHardcodedTests") == null) {
            JLanguageTool lt = new JLanguageTool(new AmericanEnglish());
            assertOneError("You're code was very neat and readable.", lt);
        }
    }
    
    private void assertNoError(String input, JLanguageTool lt) throws IOException {
        List<RuleMatch> matches = lt.check(input);
        assertEquals("Did not expect an error in test sentence: '" + input + "', but got: " + matches, 0, matches.size());
    }

    private void assertOneError(String input, JLanguageTool lt) throws IOException {
        List<RuleMatch> matches = lt.check(input);
        assertEquals("Did expect 1 error in test sentence: '" + input + "', but got: " + matches, 1, matches.size());
    }
}
