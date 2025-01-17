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
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.languagetool.*;
import org.languagetool.rules.Rule;

/**
 * @author Fred Kruse
 */
public class GermanFillerWordsRuleTest {
  
  private final Language lang = Languages.getLanguageForShortCode("de-DE");

  @Test
  public void testRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(lang);
    setUpRule(lt, null);

    //  more than 8% filler words (default)
    assertEquals(1, lt.check("Der Satz enthält augenscheinlich ein Füllwort.").size());
    assertEquals(2, lt.check("Der Satz enthält augenscheinlich relativ viele Füllwörter.").size());
    //  less than 8% filler words - don't show them
    assertEquals(0, lt.check("Der Satz enthält augenscheinlich ein Füllwort, aber es sind nicht genug um angezeigt zu werden.").size());
    //  direct speech or citation - don't show filler words
    assertEquals(0, lt.check("»Der Satz enthält augenscheinlich ein Füllwort«").size());
    //  less than 8% filler words - but three in one sentence (show 3 instead of 4) <- don't show them per default
    assertEquals(0, lt.check("Der Text enthält zu wenige Füllwörter, daher werden sie nicht angezeigt. Was sich an diesem Satz mit diesem relativ einfachen Füllwort zeigt. Dazu müssen noch eine Reihe von Sätzen geschrieben werden, um die Anzahl der Wörter zu erhöhen. Langsam sollten die Anzahl der Worte für das Drücken unter die kritische Grenze reichen. Jetzt schreibe ich allerdings einen Satz, der drei Füllwörter enthält, was allemal ziemlich ausreichend ist.").size());
    //  less than 8% filler words - but two consecutively (show 2 instead of 3) <- don't show them per default
    assertEquals(0, lt.check("Der Text enthält zu wenige Füllwörter, daher werden sie nicht angezeigt. Was sich an diesem Satz mit diesem relativ einfachen Füllwort zeigt. Dazu müssen noch eine Reihe von Sätzen geschrieben werden, um die Anzahl der Wörter zu erhöhen. Langsam sollten die Anzahl der Worte für das Drücken unter die kritische Grenze reichen. Jetzt schreibe ich einen Satz, der zwei Füllwörter hintereinander enthält, was allemal ziemlich ausreichend ist.").size());
    
    //  percentage set to zero - show all filler words
    Map<String, Object[]> ruleValues = new HashMap<>();
    Object[] o = { 0 };  
    ruleValues.put("FILLER_WORDS_DE", o);
    UserConfig userConfig = new UserConfig(ruleValues);
    setUpRule(lt, userConfig);
    assertEquals(1, lt.check("»Der Satz enthält augenscheinlich ein Füllwort«").size());
    assertEquals(1, lt.check("Der Satz enthält augenscheinlich ein Füllwort, aber es sind nicht genug um angezeigt zu werden.").size());

    Object[] o1 = { 8, true, true, true };  
    ruleValues.put("FILLER_WORDS_DE", o1);
    userConfig = new UserConfig(ruleValues);
    setUpRule(lt, userConfig);
    //  direct speech or citation - show filler words because of option set
    assertEquals(1, lt.check("»Der Satz enthält augenscheinlich ein Füllwort«").size());
    //  less than 8% filler words - but three in one sentence (show 3 instead of 4)
    assertEquals(4, lt.check("Der Text enthält zu wenige Füllwörter, daher werden sie nicht angezeigt. Was sich an diesem Satz mit diesem relativ einfachen Füllwort zeigt. Dazu müssen noch eine Reihe von Sätzen geschrieben werden, um die Anzahl der Wörter zu erhöhen. Langsam sollten die Anzahl der Worte für das Drücken unter die kritische Grenze reichen. Jetzt schreibe ich allerdings einen Satz, der vier Füllwörter enthält, was allemal ziemlich ausreichend ist.").size());
    //  less than 8% filler words - but two consecutively (show 2 instead of 3)
    assertEquals(3, lt.check("Der Text enthält zu wenige Füllwörter, daher werden sie nicht angezeigt. Was sich an diesem Satz mit diesem relativ einfachen Füllwort zeigt. Dazu müssen noch eine Reihe von Sätzen geschrieben werden, um die Anzahl der Wörter zu erhöhen. Langsam sollten die Anzahl der Worte für das Drücken unter die kritische Grenze reichen. Jetzt schreibe ich einen Satz, der zwei Füllwörter hintereinander enthält, was allemal ziemlich ausreichend ist.").size());
    

  }

  private void setUpRule(JLanguageTool lt, UserConfig userConfig) {
    for (Rule rule : lt.getAllRules()) {
      lt.disableRule(rule.getId());
    }
    GermanFillerWordsRule rule = 
        new GermanFillerWordsRule(TestTools.getMessages(lang.getShortCode()), lang, userConfig);
    lt.addRule(rule);
    lt.enableRule(rule.getId());
  }

}
