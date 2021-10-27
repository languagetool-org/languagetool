/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.AbstractCompoundRuleTest;

import java.io.IOException;

public class SwissCompoundRuleTest extends AbstractCompoundRuleTest {

  @Test
  public void testRule() throws IOException {
    lt = new JLanguageTool(Languages.getLanguageForShortCode("de-DE"));
    rule = new SwissCompoundRule(TestTools.getMessages("de"), Languages.getLanguageForShortCode("de-DE"), null);
    //check(1, "Ente süss-sauer");
    check(1, "Ente süß-sauer");
    //check(1, "Ente süss sauer");
    check(1, "Ente süß sauer");
  }

}
