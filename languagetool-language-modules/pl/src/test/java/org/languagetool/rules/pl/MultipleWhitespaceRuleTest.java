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
import org.languagetool.rules.MultipleWhitespaceRule;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MultipleWhitespaceRuleTest {

  @Test
  public void testRule() throws IOException {
    MultipleWhitespaceRule rule = new MultipleWhitespaceRule(TestTools.getEnglishMessages(), new Polish());
    JLanguageTool lt = new JLanguageTool(new Polish());
    assertEquals(0, getMatches("To jest test.", rule, lt));
    assertEquals(1, getMatches("To jest   test.", rule, lt));
  }

  private int getMatches(String input, MultipleWhitespaceRule rule, JLanguageTool lt) throws IOException {
    return rule.match(Collections.singletonList(lt.getAnalyzedSentence(input))).length;
  }
  
}
