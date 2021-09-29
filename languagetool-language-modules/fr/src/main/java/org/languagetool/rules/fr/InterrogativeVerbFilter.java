/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Jaume Ortolà
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
package org.languagetool.rules.fr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.French;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.FrenchSynthesizer;
import org.languagetool.tagging.fr.FrenchTagger;

/*
 * Get appropriate suggestions for French verbs in interrogative form (prérères-tu)
 * and imperative (dépêche-toi)
 */

public class InterrogativeVerbFilter extends RuleFilter {

  // private static final Pattern PronounSubject = Pattern.compile("R pers suj
  // ([123] [sp])");
  private static final FrenchSynthesizer synth = new FrenchSynthesizer(new French());

  private MorfologikFrenchSpellerRule morfologikRule;

  public InterrogativeVerbFilter() throws IOException {
    ResourceBundle messages = JLanguageTool.getDataBroker().getResourceBundle(JLanguageTool.MESSAGE_BUNDLE,
        new Locale("fr"));
    morfologikRule = new MorfologikFrenchSpellerRule(messages, new French(), null, Collections.emptyList());
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {
    
//    if (match.getSentence().getText().contains("intelligence-je")) {
//      int ii=0;
//      ii++;
//    }

    List<String> replacements = new ArrayList<>();
    String pronounFrom = getRequired("PronounFrom", arguments);
    String verbFrom = getRequired("VerbFrom", arguments);
    String desiredPostag = null;
    String[] extraSuggestions = new String[0];
    if (pronounFrom != null && verbFrom != null) {
      int posPronoun = Integer.parseInt(pronounFrom);
      if (posPronoun < 1 || posPronoun > patternTokens.length) {
        throw new IllegalArgumentException("ConfusionCheckFilter: Index out of bounds in " + match.getRule().getFullId()
            + ", PronounFrom: " + posPronoun);
      }
      int posVerb = Integer.parseInt(verbFrom);
      if (posVerb < 1 || posVerb > patternTokens.length) {
        throw new IllegalArgumentException(
            "ConfusionCheckFilter: Index out of bounds in " + match.getRule().getFullId() + ", VerbFrom: " + posVerb);
      }

      //AnalyzedTokenReadings atrVerb = patternTokens[posVerb - 1];
      AnalyzedTokenReadings atrPronoun = patternTokens[posPronoun - 1];
      
      // vous
      if (atrPronoun.matchesPosTagRegex("R pers obj 2 p")) {
        desiredPostag = "V.* (imp) [23] [sp]|V .*(ind|cond).* 2 p";
      }
      // nous
      else if (atrPronoun.matchesPosTagRegex("R pers obj 1 p")) {
        desiredPostag = "V.* (imp) .*|V .*(ind|cond).* 1 p";
      }
      // moi, toi, le, la, lui, nous, vous, les, leur
      else if (atrPronoun.matchesPosTagRegex("R pers obj.*")) {
        desiredPostag = "V.* (imp) .*";
      }
      else if (atrPronoun.matchesPosTagRegex(".* 1 s")) {
        desiredPostag = "V .*(ind|cond).* 1 s";
        AnalyzedTokenReadings atrVerb = patternTokens[posVerb - 1];
        AnalyzedToken reading = atrVerb.readingWithTagRegex("V ind pres 1 s");
        if (reading!=null) {
          desiredPostag="V ind pres 1 s";
          if (atrVerb.getToken().endsWith("e")) {
            extraSuggestions = synth.synthesize(reading, "V ppa [me] sp?|V ind pres 1 s", true);
          }
        }
      }
      else if (atrPronoun.matchesPosTagRegex(".* 2 s")) {
        desiredPostag = "V .*(ind|cond).* 2 s";
      }
      else if (atrPronoun.matchesPosTagRegex(".* 3( [mf])? s")) {
        desiredPostag = "V .*(ind|cond).* 3 s";
      }
      else if (atrPronoun.matchesPosTagRegex(".* 1 p")) {
        desiredPostag = "V .*(ind|cond).* 1 p";
      }
      else if (atrPronoun.matchesPosTagRegex(".* 2 p")) {
        desiredPostag = "V .*(ind|cond).* 2 p";
      }
      else if (atrPronoun.matchesPosTagRegex(".* 3( [mf])? p")) {
        desiredPostag = "V .*(ind|cond).* 3 p";
      }
      
      // add: trompè-je and trompé-je for original sentence "trompe-je"
      if (extraSuggestions.length > 0) {
        for (String extraSuggestion : extraSuggestions) {
          String completeSuggestion = extraSuggestion + atrPronoun.getToken();
          if (!replacements.contains(completeSuggestion) 
              && !completeSuggestion.endsWith("e-je")) { // exclude trompe-je
            replacements.add(completeSuggestion);
          } 
        }
      }
      else if (desiredPostag != null) {
        AnalyzedTokenReadings[] auxPatternTokens = new AnalyzedTokenReadings[1];
        if (patternTokens[posVerb - 1].isTagged()) {
          auxPatternTokens[0] = new AnalyzedTokenReadings(
              new AnalyzedToken(makeWrong(patternTokens[posVerb - 1].getToken()), null, null));
        } else {
          auxPatternTokens[0] = patternTokens[posVerb - 1];
        }
        AnalyzedSentence sentence = new AnalyzedSentence(auxPatternTokens);
        RuleMatch[] matches = morfologikRule.match(sentence);
        if (matches.length > 0) {
          List<String> suggestions = matches[0].getSuggestedReplacements();
          List<AnalyzedTokenReadings> analyzedSuggestions = FrenchTagger.INSTANCE.tag(suggestions);
          for (AnalyzedTokenReadings analyzedSuggestion : analyzedSuggestions) {
            if (analyzedSuggestion.matchesPosTagRegex(desiredPostag)) {
              String completeSuggestion = analyzedSuggestion.getToken() + atrPronoun.getToken();
              if (!replacements.contains(completeSuggestion) 
                  && !completeSuggestion.endsWith("e-je")) { // exclude trompe-je
                replacements.add(completeSuggestion);
              }
            }
          }
        }
      }
    }
   
    String message = match.getMessage();
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(),
        message, match.getShortMessage());
    ruleMatch.setType(match.getType());
    if (!replacements.isEmpty()) {
      ruleMatch.setSuggestedReplacements(replacements);
    }
    return ruleMatch;
  }

  /*
   * Invent a wrong word to find possible replacements. This is a hack to obtain
   * suggestions from the speller when the original word is a correct word.
   */
  private String makeWrong(String s) {
    /* this doesn't work for some cases: ex. tarta / trata */
    if (s.contains("a")) {return s.replace("a", "ä");}
    if (s.contains("e")) {return s.replace("e", "ë");}
    if (s.contains("i")) {return s.replace("i", "í");}
    if (s.contains("o")) {return s.replace("o", "ö");}
    if (s.contains("u")) {return s.replace("u", "ü");}
    if (s.contains("é")) {return s.replace("é", "ë");}
    if (s.contains("à")) {return s.replace("à", "ä");}
    if (s.contains("è")) {return s.replace("è", "ë");}
    if (s.contains("ù")) {return s.replace("ù", "ü");}
    if (s.contains("â")) {return s.replace("â", "ä");}
    if (s.contains("ê")) {return s.replace("ê", "ë");}
    if (s.contains("î")) {return s.replace("î", "ï");}
    if (s.contains("ô")) {return s.replace("ô", "ö");}
    if (s.contains("û")) {return s.replace("û", "ü");}
    return s + "-";
  }
}
