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
package org.languagetool.rules.nl;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Dutch;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UppercaseSentenceStartRuleTest {

  @Test
  public void testDutchSpecialCases() throws IOException {
    final JLanguageTool lt = new JLanguageTool(new Dutch() {
      @Override
      protected synchronized List<AbstractPatternRule> getPatternRules() {
        return Collections.emptyList();
      }
    });
    
    assertEquals(1, lt.check("A sentence.").size());
    assertEquals(0, lt.check("'s Morgens...").size());

    assertEquals(2, lt.check("a sentence.").size());
    assertEquals(1, lt.check("'s morgens...").size());
    assertEquals(2, lt.check("s sentence.").size());
    assertEquals(1, lt.check("'t").size());
  }
  
}
