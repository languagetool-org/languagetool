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

package org.languagetool.rules.ru;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Russian;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

public class RussianSimpleReplaceRuleTest {

  @Test
  public void testRule() throws IOException {
    RussianSimpleReplaceRule rule = new RussianSimpleReplaceRule(TestTools.getMessages("ru"));

    RuleMatch[] matches;
    JLanguageTool lt = new JLanguageTool(new Russian());

    // correct sentences:
    matches = rule.match(lt.getAnalyzedSentence("Рост кораллов тут самый быстрый,"));
    Assertions.assertEquals(0, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("Книга была порвана."));
    Assertions.assertEquals(0, matches.length);

    // incorrect sentences:
    matches = rule.match(lt.getAnalyzedSentence("Книга была порвата."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(1, matches[0].getSuggestedReplacements().size());
    Assertions.assertEquals("порвана", matches[0].getSuggestedReplacements().get(0));
  }

}
