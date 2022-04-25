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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * A rule that detects redundant modal and auxiliary verbs.
 * @author Fred Kruse
 * @since 5.5
 */
public class RedundantModalOrAuxiliaryVerb extends Rule {

  private static final String VERB_TEXT = " scheint redundant zu sein. Prüfen Sie, ob es gelöscht oder der Satz umformuliert werden kann.";
  private static final String SUB_TEXT = "Der Satzteil scheint redundant zu sein. Prüfen Sie, ob es gelöscht oder der Satz umformuliert werden kann.";
  private static final Pattern MARKS_REGEX = Pattern.compile("[,;.:?!-–—’'\"„“”»«‚‘›‹()\\[\\]]");

  public RedundantModalOrAuxiliaryVerb(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    setDefaultOff();
  }

  @Override
  public String getId() {
    return "REDUNDANT_MODAL_VERB";
  }

  @Override
  public String getDescription() {
    return "Redundantes Modal- oder Hilfsverb";
  }
  
  private static boolean isBreakToken (String sToken) {
    return MARKS_REGEX.matcher(sToken).matches() || sToken.equals("und") || sToken.equals("oder") || sToken.equals("sowie");
  }

  private int hasParticipleAt(int nConjunction, int nStart, AnalyzedTokenReadings[] tokens) {
    if (tokens[nConjunction - 1].hasPosTagStartingWith("PA2")) {
      String sParticiple = tokens[nConjunction - 1].getToken();
      for (int i = nStart; i < tokens.length; i++) {
        String sToken = tokens[i].getToken();
        if (isBreakToken (sToken)) {
          return -1;
        } else if (sToken.equals(sParticiple)) {
          if (i == tokens.length - 1 || isBreakToken (tokens[i + 1].getToken())) {
            return i;
          }
          return (-1);
        }
      }
    }
    return -1;
  }
  
  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    int nt;
    int nVerb;
    boolean doBreak;
    for (nt = 2; nt < tokens.length; nt++) {
      boolean isModVerb = tokens[nt].hasPosTagStartingWith("VER:MOD");
      if ((isModVerb || tokens[nt].hasPosTagStartingWith("VER:AUX")) 
          && nt + 1 < tokens.length && !tokens[nt - 1].getToken().equals(tokens[nt + 1].getToken())) {
        String sVerb = tokens[nt].getToken();
        nVerb = nt;
        doBreak = false;
        String suggestion = null;
        for (nt++; nt < tokens.length; nt++) {
          String sToken = tokens[nt].getToken();
          if (MARKS_REGEX.matcher(sToken).matches()) {
            break;
          }
          if (sToken.equals("und") || sToken.equals("oder") || sToken.equals("sowie")) {
            int nConjunction = nt;
            doBreak = true;
            for (nt++; nt < tokens.length; nt++) {
              sToken = tokens[nt].getToken();
              if (isBreakToken(sToken)) {
                break;
              }
              if (sToken.equals(sVerb)) {
                RuleMatch ruleMatch = null;
                if (nt - 1 == nConjunction) {
                  if (nVerb == nConjunction - 1) {
                    break;
                  }
                  int n;
                  for (n = 1; nt + n < tokens.length && tokens[nt + n].getToken().equalsIgnoreCase(tokens[nVerb + n].getToken()); n++);
                  if (n > 1) {
                    if (nVerb + n == nConjunction) {
                      break;
                    }
                    String msg = SUB_TEXT;
                    ruleMatch = new RuleMatch(this, sentence, tokens[nt - 1].getEndPos(), tokens[nt + n - 1].getEndPos(), msg);
                  } else {
                    String msg = "Das " + (isModVerb ? "Modalverb" : "Hilfsverb") + VERB_TEXT;
                    ruleMatch = new RuleMatch(this, sentence, tokens[nt - 1].getEndPos(), tokens[nt].getEndPos(), msg);
                  }
                } else if (tokens[nt - 1].getToken().equalsIgnoreCase(tokens[nVerb - 1].getToken()) 
                    && tokens[nt - 1].hasPosTagStartingWith("PRO:PER") && !tokens[nt - 1].hasPosTagStartingWith("ART")) {
                  String msg = SUB_TEXT;
                  if (nVerb == nConjunction - 1) {
                    ruleMatch = new RuleMatch(this, sentence, tokens[nVerb - 2].getEndPos(), tokens[nVerb].getEndPos(), msg);
                  } else {
                    ruleMatch = new RuleMatch(this, sentence, tokens[nt - 2].getEndPos(), tokens[nt].getEndPos(), msg);
                  }
                } else if (nt + 1 < tokens.length && tokens[nt + 1].getToken().equalsIgnoreCase(tokens[nVerb + 1].getToken()) 
                    && (tokens[nt + 1].hasPosTagStartingWith("PRO:IND") 
                        || (tokens[nt + 1].hasPosTagStartingWith("PRO:PER") && !tokens[nt + 1].getToken().equals("Sie") 
                            && !tokens[nt + 1].hasPosTagStartingWith("ART") ))) {
                  if (tokens[nt + 1].hasPosTagStartingWith("PRO:PER:AKK") && tokens[nt].matchesPosTagRegex("VER:(AUX|MOD):.*KJ1")) {
                    String msg = "Das " + (isModVerb ? "Modalverb" : "Hilfsverb") + VERB_TEXT;
                    ruleMatch = new RuleMatch(this, sentence, tokens[nt - 1].getEndPos(), tokens[nt].getEndPos(), msg);
                  } else {
                    String msg = SUB_TEXT;
                    ruleMatch = new RuleMatch(this, sentence, tokens[nt - 1].getEndPos(), tokens[nt + 1].getEndPos(), msg);
                  }
                } else {
                  if (tokens[nt - 1].hasPosTagStartingWith("PRO:PER")  
                      || tokens[nt - 1].getToken().equals("da") || tokens[nt - 1].getToken().equals("zu")
                      || tokens[nVerb + 1].getToken().equals(tokens[nt - 1].getToken())
                      || nt + 1 < tokens.length && (tokens[nt + 1].hasPosTagStartingWith("PRO:PER")
                          || tokens[nt - 1].getToken().equals(tokens[nt + 1].getToken())
                          || tokens[nt - 1].getToken().equals(tokens[nt + 1].getToken())
                          || tokens[nVerb - 1].getToken().equals(tokens[nt + 1].getToken())
                          || (tokens[nVerb + 1].hasPosTagStartingWith("VER:MOD") && tokens[nt + 1].hasPosTagStartingWith("VER:MOD"))
                          || (nVerb == nConjunction - 1 && !isBreakToken(tokens[nt + 1].getToken())))
                      || (nVerb < nConjunction - 1 && (nt + 1 == tokens.length || isBreakToken(tokens[nt + 1].getToken()))) ) {
                    break;
                  }
                  if (nVerb == nConjunction - 1) {
                    int n;
                    for (n = 1; nVerb - n > 0 && nt - n > nConjunction && tokens[nVerb - n].getToken().equals(tokens[nt - n].getToken()); n++);
                    if (n > 1) {
                      String msg = SUB_TEXT;
                      ruleMatch = new RuleMatch(this, sentence, tokens[nVerb - n].getEndPos(), tokens[nVerb].getEndPos(), msg);
                    } else {
                      String msg = "Das " + (isModVerb ? "Modalverb" : "Hilfsverb") + VERB_TEXT;
                      ruleMatch = new RuleMatch(this, sentence, tokens[nVerb - 1].getEndPos(), tokens[nVerb].getEndPos(), msg);
                    }
                  } else {
                    int paAt = hasParticipleAt(nConjunction, nt + 1, tokens);
                    if (paAt > 0) {
                      String msg = SUB_TEXT;
                      ruleMatch = new RuleMatch(this, sentence, tokens[nt - 1].getEndPos(), tokens[paAt].getEndPos(), msg);
                      suggestion = "";
                      for (int i = nt + 1; i < paAt; i++) {
                        suggestion += (" " + tokens[i].getToken());
                      }
                    } else {
                      int n;
                      for (n = 1; n + nVerb < nConjunction && n + nt < tokens.length && tokens[nVerb + n].getToken().equals(tokens[nt + n].getToken()); n++);
                      if (n + nVerb == nConjunction) {
                        String msg = SUB_TEXT;
                        ruleMatch = new RuleMatch(this, sentence, tokens[nt - 1].getEndPos(), tokens[nt + n - 1].getEndPos(), msg);
                      } else {
                        String msg = "Das " + (isModVerb ? "Modalverb" : "Hilfsverb") + VERB_TEXT;
                        ruleMatch = new RuleMatch(this, sentence, tokens[nt - 1].getEndPos(), tokens[nt].getEndPos(), msg);
                      }
                    }
                  }
                }
                if (ruleMatch != null) {
                  List<String> suggestions = new ArrayList<>();
                  if (suggestion == null) {
                    suggestions.add("");
                  } else {
                    suggestions.add(suggestion);
                  }
                  ruleMatch.setSuggestedReplacements(suggestions);
                  ruleMatches.add(ruleMatch);
                }
                break;
              }
            }
          }
          if (doBreak) {
            break;
          }
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

}
