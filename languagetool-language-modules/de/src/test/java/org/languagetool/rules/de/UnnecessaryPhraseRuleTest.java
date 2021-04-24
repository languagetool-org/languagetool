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
import org.languagetool.*;
import org.languagetool.rules.Rule;

/**
 * @author Fred Kruse
 */
public class UnnecessaryPhraseRuleTest {
  
  private Language lang = Languages.getLanguageForShortCode("de-DE");

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(lang);
    setUpRule(lt);

    assertEquals(4, lt.check("In diesem Zusammenhang ist es im Allgemeinen voll und ganz zumindest mehr oder weniger sinnvoll.").size());
    //  exclude direct speach:
    assertEquals(0, lt.check("„In diesem Zusammenhang ist es im Allgemeinen voll und ganz zumindest mehr oder weniger sinnvoll.“").size());
    assertEquals(0, lt.check("Es ist weniger sinnvoll.").size());
  }

  private void setUpRule(JLanguageTool lt) {
    for (Rule rule : lt.getAllRules()) {
      lt.disableRule(rule.getId());
    }
    UnnecessaryPhraseRule rule = new UnnecessaryPhraseRule(TestTools.getMessages(lang.getShortCode()),
        lang, new UserConfig());
    lt.addRule(rule);
    lt.enableRule(rule.getId());
  }

}
