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
import org.languagetool.UserConfig;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.Rule;

/**
 * @author Fred Kruse
 */
public class GermanStyleRepeatedWordRuleTest {
  Language lang = new GermanyGerman();

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(lang);
    setUpRule(lt);

    assertEquals(2, lt.check("Der alte Mann wohnte in einem großen Haus. Es stand in einem großen Garten.").size());
    assertEquals(0, lt.check("Der alte Mann wohnte in einem großen Haus. Es stand in einem weitläufigen Garten.").size());
  }

  private void setUpRule(JLanguageTool lt) {
    for (Rule rule : lt.getAllRules()) {
      lt.disableRule(rule.getId());
    }
    GermanStyleRepeatedWordRule rule = new GermanStyleRepeatedWordRule(TestTools.getMessages(lang.getShortCode()),
        lang, new UserConfig());
    lt.addRule(rule);
    lt.enableRule(rule.getId());
  }

}
