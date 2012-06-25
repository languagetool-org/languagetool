package org.languagetool.rules.en;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

public class MorfologikAmericanSpellerRuleTest {

    @Test
    public void testMorfologikSpeller() throws IOException {
        MorfologikAmericanSpellerRule rule =
                new MorfologikAmericanSpellerRule (TestTools.getMessages("English"), Language.AMERICAN_ENGLISH);

        RuleMatch[] matches;
        JLanguageTool langTool = new JLanguageTool(Language.AMERICAN_ENGLISH);


        // correct sentences:
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("This is an example: we get behavior as a dictionary word.")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Why don't we speak today.")).length);
        //with doesn't
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("He doesn't know what to do.")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence(",")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("123454")).length);

        //incorrect sentences:

        matches = rule.match(langTool.getAnalyzedSentence("behaviour"));
        // check match positions:
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(9, matches[0].getToPos());
        assertEquals("behavior", matches[0].getSuggestedReplacements().get(0));

        assertEquals(1, rule.match(langTool.getAnalyzedSentence("aõh")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("a")).length);

    }

}
