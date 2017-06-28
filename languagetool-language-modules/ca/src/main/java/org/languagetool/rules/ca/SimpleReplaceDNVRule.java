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
package org.languagetool.rules.ca;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;
import org.languagetool.synthesis.ca.CatalanSynthesizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * A rule that matches lemmas found only in DNV (AVL dictionary) and suggests
 * alternative words. 
 * 
 * Catalan implementations. Loads the
 * relevant lemmas from <code>rules/ca/replace_dnv.txt</code>.
 * 
 * @author Jaume Ortolà
 */
public class SimpleReplaceDNVRule extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> wrongLemmas = load("/ca/replace_dnv.txt");
  private static final Locale CA_LOCALE = new Locale("CA");
  private static final CatalanSynthesizer synth = new CatalanSynthesizer();

  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongLemmas;
  }
  
  public SimpleReplaceDNVRule(final ResourceBundle messages) throws IOException {
    super(messages);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    super.setLocQualityIssueType(ITSIssueType.Misspelling);
    this.setIgnoreTaggedWords();
  }  

  @Override
  public final String getId() {
    return "CA_SIMPLE_REPLACE_DNV";
  }

 @Override
  public String getDescription() {
    return "Detecta paraules admeses només per l'AVL i proposa suggeriments de canvi";
  }

  @Override
  public String getShort() {
    return "Paraula admesa només pel DNV (AVL).";
  }
  
  @Override
  public String getMessage(String tokenStr,List<String> replacements) {
	  return "Paraula admesa pel DNV (AVL), però no per altres diccionaris.";
  }
  
  @Override
  public boolean isCaseSensitive() {
    return false;
  }
  
  @Override
  public Locale getLocale() {
    return CA_LOCALE;
  }
  
  @Override
  public final RuleMatch[] match(final AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();

    for (int i=1; i<tokens.length; i++) {

      List<String> replacementLemmas = null; 
      String replacePOSTag = null;
      
      for (AnalyzedToken at: tokens[i].getReadings()){
    	  if (wrongLemmas.containsKey(at.getLemma())) {
    		  replacementLemmas = wrongLemmas.get(at.getLemma());
    		  replacePOSTag = at.getPOSTag();
    		  break;
    	  }
      }
      
      // The rule matches!
      if (replacementLemmas != null && replacePOSTag != null) {
        List<String> possibleReplacements = new ArrayList<>();
        String[] synthesized = null;
        // synthesize replacements
        for (String replacementLemma : replacementLemmas) {
          try {
            synthesized = synth.synthesize(new AnalyzedToken(replacementLemma, replacePOSTag, replacementLemma),
                replacePOSTag);
          } catch (IOException e) {
            throw new RuntimeException("Could not synthesize: " + replacementLemma + " with tag " + replacePOSTag, e);
          }
          possibleReplacements.addAll(Arrays.asList(synthesized));
        }
        if (possibleReplacements.size() > 0) {
          RuleMatch potentialRuleMatch = createRuleMatch(tokens[i], possibleReplacements);
          ruleMatches.add(potentialRuleMatch);
        }
      }

    }
    return toRuleMatchArray(ruleMatches);
  }
}
