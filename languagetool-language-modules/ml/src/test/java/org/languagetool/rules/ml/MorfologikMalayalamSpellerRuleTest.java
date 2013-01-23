package org.languagetool.rules.ml;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Malayalam;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MorfologikMalayalamSpellerRuleTest {

    @Test
    public void testMorfologikSpeller() throws IOException {
        final Malayalam language = new Malayalam();
        MorfologikMalayalamSpellerRule rule =
                new MorfologikMalayalamSpellerRule (TestTools.getMessages("Malayalam"), language);

        RuleMatch[] matches;
        JLanguageTool langTool = new JLanguageTool(language);

        // correct sentences:
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("എന്തുകൊണ്ട്‌ അംഗത്വം")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("എങ്ങനെ അംഗമാകാം?")).length);        
        //test for "LanguageTool":
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("LanguageTool")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence(",")).length);
        assertEquals(0, rule.match(langTool.getAnalyzedSentence("123454")).length);

        //incorrect sentences:

        matches = rule.match(langTool.getAnalyzedSentence("Zolw"));
        // check match positions:
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(4, matches[0].getToPos());
        assertEquals(matches[0].getSuggestedReplacements().isEmpty(), true);
        
        matches = rule.match(langTool.getAnalyzedSentence("എaങ്ങനെ"));
        assertEquals(1, matches.length);
        assertEquals(0, matches[0].getFromPos());
        assertEquals(7, matches[0].getToPos());
        assertEquals(matches[0].getSuggestedReplacements().get(0), "എങ്ങനെ");

        assertEquals(1, rule.match(langTool.getAnalyzedSentence("aõh")).length);
        assertEquals(1, rule.match(langTool.getAnalyzedSentence("a")).length);
    }

}
