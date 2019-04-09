/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.FalseFriendRuleLoader;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FalseFriendExporter {

  private final static String filename = 
          "/home/dnaber/lt/git/languagetool/languagetool-core/src/main/resources/org/languagetool/rules/false-friends.xml";

  public static void main(String[] args) throws IOException {
    Language l1 = Languages.getLanguageForShortCode("en");
    Language l2 = Languages.getLanguageForShortCode("de");
    listRuleMessages(l1, l2);
    //listRuleMessages(l2, l1);
  }
  
  private static void listRuleMessages(Language l1, Language l2) throws IOException {
    FalseFriendRuleLoader ruleLoader = new FalseFriendRuleLoader(null);
    List<AbstractPatternRule> rules = ruleLoader.getRules(new File(filename), l1, l2);
    int i = 1;
    for (AbstractPatternRule rule : rules) {
      System.out.println(i + ". " + rule.getMessage().
              replaceFirst("Hinweis: ", "").replaceAll("<suggestion>", "'").replaceAll("</suggestion>", "'"));
      i++;
    }
  }
}
