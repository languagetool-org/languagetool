
/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Jaume Ortolà (http://www.languagetool.org)
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


package org.languagetool.rules.it;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Italian;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MorfologikItalianSpellerRuleTest {

    @Test
    public void testMorfologikSpeller() throws IOException {
        MorfologikItalianSpellerRule rule =
                new MorfologikItalianSpellerRule (TestTools.getMessages("it"), new Italian(), null, Collections.emptyList());

        RuleMatch[] matches;
        JLanguageTool lt = new JLanguageTool(new Italian());
        
        matches = rule.match(lt.getAnalyzedSentence("esmpio"));
        assertEquals(1, matches.length);
        // TODO: another solution: remove capitalized words from the speller binary dictionary
        assertEquals("[empio, esempio, espio, espiò]", matches[0].getSuggestedReplacements().toString());
        
    }
}

