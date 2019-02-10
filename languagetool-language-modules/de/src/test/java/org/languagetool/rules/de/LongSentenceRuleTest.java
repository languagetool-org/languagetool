/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://danielnaber.de/)
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
package org.languagetool.rules.de;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.UserConfig;
import org.languagetool.language.GermanyGerman;

public class LongSentenceRuleTest extends org.languagetool.rules.LongSentenceRuleTest {
  @Test
  public void testMatch() throws Exception {
    JLanguageTool lt = new JLanguageTool(new GermanyGerman());
    
    LongSentenceRule rule = new LongSentenceRule(TestTools.getMessages("de"), new UserConfig(), 6, true);
    
    assertNoMatch("Eins zwei drei vier fünf sechs.", rule, lt);
    //  Words after colon are treated like a separate sentence
    assertNoMatch("Ich zähle jetzt: \"Eins zwei drei vier fünf sechs.\"", rule, lt);
    
    assertMatch("Eins zwei drei vier fünf sechs sieben.", 31, 37, rule, lt);
    assertMatch("Eins zwei drei vier fünf (sechs sieben) acht.", 32, 38, rule, lt);
    assertMatch("Ich zähle jetzt: Eins zwei drei vier fünf sechs sieben.", 48, 54, rule, lt);

  }
}
