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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;


public class SimpleReplaceRenamedRuleTest {

  @Test
  public void testRule() throws IOException {
    SimpleReplaceRenamedRule rule = new SimpleReplaceRenamedRule(TestTools.getEnglishMessages());

    RuleMatch[] matches;
    JLanguageTool lt = new JLanguageTool(new Ukrainian());

    // correct sentences:
    matches = rule.match(lt.getAnalyzedSentence("Київ."));
    Assertions.assertEquals(0, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("Дніпродзержинська"));
    Assertions.assertEquals(1, matches.length, Arrays.asList(matches).toString());
    Assertions.assertEquals(Arrays.asList("Кам'янське", "кам'янський"), matches[0].getSuggestedReplacements());
    Assertions.assertTrue(matches[0].getMessage().contains("2016"));

    matches = rule.match(lt.getAnalyzedSentence("дніпродзержинського."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(Collections.singletonList("кам'янський"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("Червонознам'янка."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(Arrays.asList("Знам'янка", "Знаменка"), matches[0].getSuggestedReplacements());
  }
}
