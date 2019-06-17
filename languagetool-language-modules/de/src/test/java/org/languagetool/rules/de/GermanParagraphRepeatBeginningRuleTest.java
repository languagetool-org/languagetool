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
package org.languagetool.rules.de;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.Rule;

/**
 * @author Fred Kruse
 */
public class GermanParagraphRepeatBeginningRuleTest {
  
  private final Language lang = new GermanyGerman();

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(lang);
    setUpRule(lt);

    assertEquals(2, lt.check("Der Hund spazierte über die Straße.\n\nDer Hund ignorierte den Verkehr.").size());
    assertEquals(0, lt.check("Der Hund spazierte über die Straße.\n\nDas Tier ignorierte den Verkehr.").size());
    assertEquals(2, lt.check("Peter spazierte über die Straße.\n\nPeter ignorierte den Verkehr.").size());
    assertEquals(0, lt.check("Peter spazierte über die Straße.\n\nDer Junge ignorierte den Verkehr.").size());
    assertEquals(2, lt.check("»Peter spazierte über die Straße.«\n\n»Peter ignorierte den Verkehr.«").size());
  }

  private void setUpRule(JLanguageTool lt) {
    for (Rule rule : lt.getAllRules()) {
      lt.disableRule(rule.getId());
    }
    GermanParagraphRepeatBeginningRule rule = new GermanParagraphRepeatBeginningRule(TestTools.getMessages(lang.getShortCode()), lang);
    lt.addRule(rule);
    lt.enableRule(rule.getId());
  }

}
