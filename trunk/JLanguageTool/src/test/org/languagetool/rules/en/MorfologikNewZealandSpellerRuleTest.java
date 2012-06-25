package org.languagetool.rules.en;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

public class MorfologikNewZealandSpellerRuleTest {

    @Test
    public void testMorfologikSpeller() throws IOException {
        MorfologikNewZealandSpellerRule rule =
                new MorfologikNewZealandSpellerRule (TestTools.getMessages("English"), Language.NEW_ZEALAND_ENGLISH);

        RuleMatch[] matches;
        JLanguageTool langTool = new JLanguageTool(Language.NEW_ZEALAND_ENGLISH);


        // correct sentences:
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("This is an example: we get behaviour as a dictionary word.")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Why don't we speak today.")).length);
        //with doesn't
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("He doesn't know what to do.")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence(",")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("123454")).length);

        //special New Zealand content:
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("Abercrombie")).length);        
        
        //incorrect sentences:

        matches = rule.match(langTool.getAnalyzedSentence("behavior"));
        // check match positions:
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(8, matches[0].getToPos());
        assertEquals("behaviour", matches[0].getSuggestedReplacements().get(0));

        assertEquals(1, rule.match(langTool.getAnalyzedSentence("aõh")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("a")).length);

    }

}
