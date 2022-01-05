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
import org.languagetool.Language;
import org.languagetool.rules.AbstractSimpleReplaceRule;
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
 * Adds simple replacement using lemmas
 * 
 * @author Jaume Ortolà
 */
public abstract class AbstractSimpleReplaceLemmasRule extends AbstractSimpleReplaceRule {

  protected Map<String, List<String>> wrongLemmas = null;
  private static final Locale CA_LOCALE = new Locale("CA");
  private CatalanSynthesizer synth;
  
  public AbstractSimpleReplaceLemmasRule(final ResourceBundle messages, Language language) throws IOException {
    super(messages);
    //this.setIgnoreTaggedWords();
    synth = (CatalanSynthesizer) language.getSynthesizer();
  }  
  
  @Override
  protected Map<String, List<String>> getWrongWords() {
    return wrongLemmas;
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
    String originalLemma = null;
    for (int i=1; i<tokens.length; i++) {
      List<String> replacementLemmas = null; 
      String replacePOSTag = null;
      boolean bRuleMatches = false;
      for (AnalyzedToken at: tokens[i].getReadings()){
        if (getWrongWords().containsKey(at.getLemma())) {
          replacementLemmas = getWrongWords().get(at.getLemma());
          replacePOSTag = at.getPOSTag();
          bRuleMatches = true;
          originalLemma=at.getLemma();
          break;
        }
      }
      // find suggestions
      List<String> possibleReplacements = new ArrayList<>();
      if (replacementLemmas != null && replacePOSTag != null) {     
        String[] synthesized = null;
        // synthesize replacements
        for (String replacementLemma : replacementLemmas) {
          try {
            synthesized = synth.synthesize(new AnalyzedToken(replacementLemma, replacePOSTag, replacementLemma),
                replacePOSTag);
          } catch (IOException e) {
            throw new RuntimeException("Could not synthesize: " + replacementLemma + " with tag " + replacePOSTag, e);
          } // try with another gender
          if (synthesized.length == 0) {
            try {
              String replacePOSTag2 = replacePOSTag.replaceAll("[MFC]S",".S").replaceAll("[MFC]P",".P");
              synthesized = synth.synthesize(new AnalyzedToken(replacementLemma, replacePOSTag, replacementLemma), replacePOSTag2);
            } catch (IOException e) {
              throw new RuntimeException("Could not synthesize: " + replacementLemma + " with tag " + replacePOSTag, e);
            }   
          } // add the suggestion without inflection
          if (synthesized.length == 0 && replacementLemma.length()>1) {
            possibleReplacements.add(replacementLemma);
          } else {
            possibleReplacements.addAll(Arrays.asList(synthesized));
          }
        }
      }
      if (bRuleMatches) {
        RuleMatch potentialRuleMatch = createRuleMatch(tokens[i], possibleReplacements, sentence, originalLemma);
        ruleMatches.add(potentialRuleMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }
}
