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
package org.languagetool.rules.pl;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Polish;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.WordRepeatRule;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class WordRepeatRuleTest {

  @Test
  public void testRulePolish() throws IOException {
    Polish polish = new Polish();
    WordRepeatRule rule = new WordRepeatRule(TestTools.getEnglishMessages(), polish);
    RuleMatch[] matches;
    JLanguageTool lt = new JLanguageTool(polish);
    // correct sentences:
    matches = rule.match(lt.getAnalyzedSentence("To jest zdanie."));
    assertEquals(0, matches.length);
    // with immunized words:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("W w. XVI język jest jak kipiący kocioł.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Co jeszcze było smutniejsze, to to, że im się jeść chciało potężnie.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Tra ta ta!")).length);
    // incorrect sentences:
    matches = rule.match(lt.getAnalyzedSentence("To jest jest zdanie."));
    assertEquals(1, matches.length);
  }

}
