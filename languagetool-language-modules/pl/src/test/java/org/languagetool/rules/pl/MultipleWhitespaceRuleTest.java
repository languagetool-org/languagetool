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

import static org.junit.Assert.assertEquals;

public class MultipleWhitespaceRuleTest {

  @Test
  public void testRule() throws IOException {
    final MultipleWhitespaceRule rule = new MultipleWhitespaceRule(TestTools.getEnglishMessages(), new Polish());
    final JLanguageTool langTool = new JLanguageTool(new Polish());
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("To jest test.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("To jest   test.")).length);
  }

}
