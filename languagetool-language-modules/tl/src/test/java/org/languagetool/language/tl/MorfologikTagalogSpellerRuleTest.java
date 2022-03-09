/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language.tl;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Tagalog;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MorfologikTagalogSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    Tagalog language = new Tagalog();
    MorfologikTagalogSpellerRule rule = new MorfologikTagalogSpellerRule(TestTools.getMessages("en"), language, null, Collections.emptyList());
    JLanguageTool lt = new JLanguageTool(language);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("Ang talatang ito ay nagpapakita ng ng kakayahan ng LanguageTool at halimbawa kung paano ito gamitin.")).length);

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Ang talatang ito ay nagpapakita ng ng kakayahan ng LanguageTool at hinahalimbawa kung paano ito gamitin."));
    assertEquals(1, matches.length);
    assertEquals(67, matches[0].getFromPos());
    assertEquals(80, matches[0].getToPos());
    assertEquals("hina halimbawa", matches[0].getSuggestedReplacements().get(0));
  }

}
