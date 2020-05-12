/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.rules.es;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Spanish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Jaume Ortolà
 */
public class SimpleReplaceVerbsRuleTest {

  private SimpleReplaceVerbsRule rule;
  private JLanguageTool langTool;

  @BeforeEach
  public void setUp() throws Exception {
    rule = new SimpleReplaceVerbsRule(TestTools.getMessages("ca"), new Spanish());
    langTool = new JLanguageTool(new Spanish());
  }

  @Test
  public void testRule() throws IOException {

    // correct sentences:

    // incorrect sentences:
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("sanitizaban"));
    assertEquals(1, matches.length);
    assertEquals("desinfectaban", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(langTool.getAnalyzedSentence("saniticen"));
    assertEquals(1, matches.length);
    assertEquals("desinfecten", matches[0].getSuggestedReplacements().get(0));
    
    //matches = rule.match(langTool.getAnalyzedSentence("sanitícenla"));
    //assertEquals(1, matches.length);
    //assertEquals("desinféctenla", matches[0].getSuggestedReplacements().get(0));
  }

}
