/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Ebrahim Byagowi <ebrahim@gnu.org>
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
package org.languagetool.rules.fa;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class PersianSpaceBeforeRuleTest {

  private PersianSpaceBeforeRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws IOException {
    rule = new PersianSpaceBeforeRule(TestTools.getEnglishMessages(), TestTools.getDemoLanguage());
    lt = new JLanguageTool(TestTools.getDemoLanguage());
  }

  @Test
  public void testRules() throws IOException {
    assertMatches("به اینجا", 1);
    assertMatches("من به اینجا", 0);
    assertMatches("(به اینجا", 0);
  }

  private void assertMatches(String text, int expectedMatches) throws IOException {
    assertEquals(expectedMatches, rule.match(lt.getAnalyzedSentence(text)).length);
  }

}
