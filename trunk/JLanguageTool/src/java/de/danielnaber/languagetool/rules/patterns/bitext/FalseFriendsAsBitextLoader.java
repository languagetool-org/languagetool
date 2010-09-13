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

package de.danielnaber.languagetool.rules.patterns.bitext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.patterns.FalseFriendRuleLoader;
import de.danielnaber.languagetool.rules.patterns.PatternRule;

/**
 * Loads the false friend rules as bitext pattern rules. Note that the resulting 
 * rules have suggestions that are not really customizable, in contradistinction
 * to the 'real' bitext pattern rules.
 * 
 * @author Marcin Miłkowski
 *
 */
public class FalseFriendsAsBitextLoader {
  
  public List<BitextPatternRule> getFalseFriendsAsBitext(final String filename,
      final Language motherTongue, final Language language) throws ParserConfigurationException, SAXException, IOException {
    final FalseFriendRuleLoader ruleLoader = new FalseFriendRuleLoader();
    List<BitextPatternRule> bRules = new ArrayList<BitextPatternRule>();
    List<PatternRule> rules1 =  
    ruleLoader.getRules(this.getClass().getResourceAsStream(filename),
          motherTongue, language);
    List<PatternRule> rules2 =  
      ruleLoader.getRules(this.getClass().getResourceAsStream(filename),
          language, motherTongue);
    HashMap<String, PatternRule> srcRules = new HashMap<String, PatternRule>();
    for (PatternRule rule : rules1) {
     srcRules.put(rule.getId(), rule);
    }    
    for (PatternRule rule : rules2) {
      if (srcRules.containsKey(rule.getId())) {
        BitextPatternRule bRule = new BitextPatternRule(
            srcRules.get(rule.getId()), rule); 
        bRule.setSourceLang(motherTongue);
        bRule.setCategory(rule.getCategory());
        bRules.add(bRule);
      }
    }
  return bRules;
  }

}

