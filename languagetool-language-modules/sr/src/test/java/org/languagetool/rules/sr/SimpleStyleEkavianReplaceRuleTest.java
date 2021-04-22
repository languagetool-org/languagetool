/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.sr;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Serbian;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.sr.ekavian.SimpleStyleEkavianReplaceRule;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class SimpleStyleEkavianReplaceRuleTest {

  @Test
  public void testRule() throws IOException {
    SimpleStyleEkavianReplaceRule rule = new SimpleStyleEkavianReplaceRule(TestTools.getEnglishMessages());
    RuleMatch[] matches;
    JLanguageTool lt = new JLanguageTool(new Serbian());

    // correct sentences:
    matches = rule.match(lt.getAnalyzedSentence("Он је добар."));
    assertEquals(0, matches.length);

    // incorrect sentences:
    matches = rule.match(lt.getAnalyzedSentence("Она је дебела."));
    assertEquals(1, matches.length);
    assertEquals(1, matches[0].getSuggestedReplacements().size());
    assertEquals(Arrays.asList("елегантно попуњена"), matches[0].getSuggestedReplacements());
  }
}
