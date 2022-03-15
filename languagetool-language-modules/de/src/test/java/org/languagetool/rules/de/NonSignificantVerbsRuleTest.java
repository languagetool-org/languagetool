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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.*;
import org.languagetool.rules.Rule;

import java.io.IOException;

/**
 * @author Fred Kruse
 */
public class NonSignificantVerbsRuleTest {
  
  private Language lang = Languages.getLanguageForShortCode("de-DE");

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(lang);
    setUpRule(lt);

    Assertions.assertEquals(3, lt.check("Wenn man das machen kann, sollte man das tun. Das ist so.").size());
    Assertions.assertEquals(0, lt.check("Der Vorgang war abgeschlossen. Das hatte er nicht bedacht.").size());
  }

  private void setUpRule(JLanguageTool lt) {
    for (Rule rule : lt.getAllRules()) {
      lt.disableRule(rule.getId());
    }
    NonSignificantVerbsRule rule = new NonSignificantVerbsRule(TestTools.getMessages(lang.getShortCode()),
        lang, new UserConfig());
    lt.addRule(rule);
    lt.enableRule(rule.getId());
  }

}
