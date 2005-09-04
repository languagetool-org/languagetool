/* JLanguageTool, a natural language style checker 
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
package de.danielnaber.languagetool.rules.de;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tagging.de.AnalyzedGermanToken;
import de.danielnaber.languagetool.tagging.de.GermanToken.POSType;

/**
 * Simple agreement checker for German noun phrases. Checks agreement in:
 * 
 * <ul>
 *  <li>DET NOUN: e.g. "der Mann", "die Frau" (correct), "die Haus" (incorrect)</li>
 *  <li>DET ADJ NOUN: e.g. "der riesige Tisch" (correct), "die riesigen Tisch" (incorrect)</li> 
 * </ul>
 * 
 * Note that this rule only checks agreement inside the noun phrase, not whether
 * e.g. the correct case is used. For example, "Es ist das Haus dem Mann" is not
 * detected as incorrect. 
 *  
 * @author Daniel Naber
 */
public class AgreementRule extends GermanRule {

  public String getId() {
    return "GERMAN_AGREEMENT";
  }

  public String getDescription() {
    return "Check some (not all) agreement rules of German";
  }

  public RuleMatch[] match(AnalyzedSentence text) {
    List ruleMatches = new ArrayList();
    AnalyzedToken[] tokens = text.getTokensWithoutWhitespace();
    int pos = 0;
    for (int i = 0; i < tokens.length; i++) {
      String posToken = tokens[i].getPOSTag();
      AnalyzedGermanToken analyzedToken = 
        new AnalyzedGermanToken(tokens[i].getToken(), posToken, tokens[i].getStartPos());
      if (analyzedToken.hasReadingOfType(POSType.DETERMINER)) {
        int tokenPos = i + 1; 
        if (tokenPos >= tokens.length)
          break;
        AnalyzedGermanToken nextToken = new AnalyzedGermanToken(tokens[tokenPos].getToken(),
            tokens[tokenPos].getPOSTag(), tokens[tokenPos].getStartPos());
        if (nextToken.hasReadingOfType(POSType.ADJEKTIV)) {
          tokenPos = i + 2; 
          if (tokenPos >= tokens.length)
            break;
          AnalyzedGermanToken nextNextToken = new AnalyzedGermanToken(tokens[tokenPos].getToken(),
              tokens[tokenPos].getPOSTag(), tokens[tokenPos].getStartPos());
          if (nextNextToken.hasReadingOfType(POSType.NOMEN)) {
            RuleMatch ruleMatch = checkDetAdjNounAgreement(tokens[i], tokens[i+1], tokens[i+2]);
            if (ruleMatch != null)
              ruleMatches.add(ruleMatch);
          }
        } else if (nextToken.hasReadingOfType(POSType.NOMEN)) {
          RuleMatch ruleMatch = checkDetNounAgreement(tokens[i], tokens[i+1]);
          if (ruleMatch != null)
            ruleMatches.add(ruleMatch);
        }
      }
      pos += tokens[i].getToken().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

  private RuleMatch checkDetNounAgreement(AnalyzedToken token1, AnalyzedToken token2) {
    RuleMatch ruleMatch = null;
    Set set1 = getAgreementCategories(token1.getPOSTag());
    if (set1 == null)
      return null;  // word not known, assume it's correct
    Set set2 = getAgreementCategories(token2.getPOSTag());
    if (set2 == null)
      return null;
    //System.err.println(token1 + "<-->" + token2);
    set1.retainAll(set2);
    if (set1.size() == 0) {
      // TODO: better error message than just 'agreement error'
      String msg = "Agreement error";
      ruleMatch = new RuleMatch(this, token1.getStartPos(), 
          token2.getStartPos()+token2.getToken().length(), msg);
    }
    return ruleMatch;
  }

  private RuleMatch checkDetAdjNounAgreement(AnalyzedToken token1, AnalyzedToken token2, AnalyzedToken token3) {
    RuleMatch ruleMatch = null;
    Set set1 = getAgreementCategories(token1.getPOSTag());
    if (set1 == null)
      return null;  // word not known, assume it's correct
    //Set set1Orig = getAgreementCategories(term1);
    Set set2 = getAgreementCategories(token2.getPOSTag());
    if (set2 == null)
      return null;
    Set set3 = getAgreementCategories(token3.getPOSTag());
    if (set3 == null)
      return null;
    set1.retainAll(set2);
    set1.retainAll(set3);
    if (set1.size() == 0) {          
      String msg = "Agreement error";
      ruleMatch = new RuleMatch(this, token1.getStartPos(), 
          token3.getStartPos()+token3.getToken().length(), msg);
    }
    return ruleMatch;
  }

  /** Return Kasus, Numerus, Genus */
  private Set getAgreementCategories(String posTagInformation) {
    Set set = new HashSet();
    String[] parts = posTagInformation.split(",");
    for (int i = 0; i < parts.length; i++) {
      String allCats = parts[i].trim();
      int delim = allCats.indexOf(" ");       // cut off type information
      String cats = allCats.substring(delim);
      if (cats.startsWith("["))
        cats = cats.substring(1);
      if (cats.endsWith("]"))
        cats = cats.substring(0, cats.length()-1);
      set.add(cats.trim());
    }
    return set;
  }

  public void reset() {
  }

}
