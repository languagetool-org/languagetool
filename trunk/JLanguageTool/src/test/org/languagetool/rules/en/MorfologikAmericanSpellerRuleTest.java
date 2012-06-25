/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
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
