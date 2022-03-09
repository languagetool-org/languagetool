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

package org.languagetool.rules.uk;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SimpleReplaceRenamedRuleTest {

  @Test
  public void testRule() throws IOException {
    SimpleReplaceRenamedRule rule = new SimpleReplaceRenamedRule(TestTools.getEnglishMessages());

    RuleMatch[] matches;
    JLanguageTool lt = new JLanguageTool(new Ukrainian());

    // correct sentences:
    matches = rule.match(lt.getAnalyzedSentence("Київ."));
    assertEquals(0, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("Дніпродзержинська"));
    assertEquals(Arrays.asList(matches).toString(), 1, matches.length);
    assertEquals(Arrays.asList("Кам'янське", "кам'янський"), matches[0].getSuggestedReplacements());
    assertTrue(matches[0].getMessage().contains("2016"));

    matches = rule.match(lt.getAnalyzedSentence("дніпродзержинського."));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("кам'янський"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("Червонознам'янка."));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("Знам'янка", "Знаменка"), matches[0].getSuggestedReplacements());
  }
}
