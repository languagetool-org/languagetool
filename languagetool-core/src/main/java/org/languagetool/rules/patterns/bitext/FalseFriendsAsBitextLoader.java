/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Marcin Miłkowski (www.languagetool.org)
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

package org.languagetool.rules.patterns.bitext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.FalseFriendRuleLoader;
import org.xml.sax.SAXException;

/**
 * Loads the false friend rules as bitext pattern rules. Note that the resulting
 * rules have suggestions that are not really customizable, in contradistinction
 * to the 'real' bitext pattern rules.
 * 
 * @author Marcin Miłkowski
 */
public class FalseFriendsAsBitextLoader {

  public List<BitextPatternRule> getFalseFriendsAsBitext(
          String filename, Language motherTongue,
          Language language) throws ParserConfigurationException,
          SAXException, IOException {
    FalseFriendRuleLoader ruleLoader = new FalseFriendRuleLoader(motherTongue);
    List<BitextPatternRule> bRules = new ArrayList<>();
    List<AbstractPatternRule> rules1 = ruleLoader.getRules(
            JLanguageTool.getDataBroker().getFromRulesDirAsStream(filename),
            motherTongue, language);
    List<AbstractPatternRule> rules2 = ruleLoader.getRules(
            JLanguageTool.getDataBroker().getFromRulesDirAsStream(filename),
            language, motherTongue);
    Map<String, AbstractPatternRule> srcRules = new HashMap<>();
    for (AbstractPatternRule rule : rules1) {
      srcRules.put(rule.getId(), rule);
    }
    for (AbstractPatternRule rule : rules2) {
      if (srcRules.containsKey(rule.getId())) {
        BitextPatternRule bRule = new BitextPatternRule(
                srcRules.get(rule.getId()), rule);
        bRule.setSourceLanguage(motherTongue);
        bRule.setCategory(rule.getCategory());
        bRules.add(bRule);
      }
    }
    return bRules;
  }

}
