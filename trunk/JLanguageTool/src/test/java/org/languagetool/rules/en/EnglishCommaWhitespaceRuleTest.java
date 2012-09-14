/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import org.languagetool.TestTools;
import org.languagetool.rules.CommaWhitespaceRuleTest;

import java.io.IOException;

public class EnglishCommaWhitespaceRuleTest extends CommaWhitespaceRuleTest {

  @Override
  public void setUp() throws IOException {
    super.setUp();
    rule = new EnglishCommaWhitespaceRule(TestTools.getEnglishMessages());
  }

  @Override
  public void testSpecialCaseForEnglish() throws IOException {
    assertMatches("Ellipsis . . . as suggested by The Chicago Manual of Style", 0);
    assertMatches("Ellipsis . . . as suggested . But this is wrong.", 1);
    assertMatches("Ellipsis . . . . as suggested by The Chicago Manual of Style", 0);
    assertMatches("Ellipsis . . . . as suggested . But this is wrong.", 1);
    assertMatches("Ellipsis . . . ", 0);
    assertMatches("Ellipsis . . . . ", 0);
    assertMatches("Ellipsis . . .", 0);
    assertMatches("Ellipsis . . . .", 0);
  }

}
