/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Markus Brenneis
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.language.German;
import org.languagetool.rules.Category;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

import java.io.IOException;

/**
 * Simple agreement checker for German verbs and subject. Checks agreement in:
 * 
 * <ul>
 *  <li>VER:1:SIN w/o ich: e.g. "Max bin da." (incorrect) [same for VER:2:SIN w/o du, VER:1:PLU w/o wir]</li>
 *  <li>ich + VER:[123]:.* (not VER:1:SIN): e.g. "ich bist" (incorrect) [same for du, er, wir]</li> 
 * </ul>
 * 
 * TODO:
 * <ul>
 * <li>wenn nur ein mögliches finites Verb -> das nehmen (Max machen das.)
 * <li>Sie (i>1)
 * <li>bei ich/du/er/wir sofort prüfen, damit alle vorkommen geprüft werden (Ich geht jetzt nach Hause und dort gehe ich sofort unter die Dusche.) [aber: isNear]
 * <li>Alle Verbvorkommen merken (Er präsentieren wollte und Video hätte keine Pläne.)
 * </ul>
 * 
 * @author Markus Brenneis
 */
public class VerbAgreementRule extends GermanRule {
  
  // Words that prevent a rule match when they occur directly before "bin":
  private static final Set<String> BIN_IGNORE = new HashSet<>(Arrays.asList(
    "Mohamed",
    "Muhammad",
    "Muhammed",
    "Mohammed",
    "Mansour",
    "Qaboos",
    "Qabus",
    "Tamim",
    "Majid",
    "Salman",
    "Ghazi",
    "Mahathir",
    "Madschid",
    "Maktum",
    "al-Aziz",
    "Asis",
    "Numan",
    "Hussein",
    "Abdul",
    "Abdulla",
    "Abdullah",
    "Isa",
    "Osama",
    "Said",
    "Zayid",
    "Zayed",
    "Hamad",
    "Chalifa",
    "Raschid",
    "Turki",
    "/"
  ));
  
  private static final Set<String> QUOTATION_MARKS = new HashSet<>(Arrays.asList(
    "\"", "„"
  ));
  
  private final Language language;

  private AnalyzedTokenReadings finiteVerb;

  public VerbAgreementRule(final ResourceBundle messages, German language) {
    this.language = language;
    super.setCategory(new Category(messages.getString("category_grammar")));
    addExamplePair(Example.wrong("Ich <marker>bist</marker> über die Entwicklung sehr froh."),
                   Example.fixed("Ich <marker>bin</marker> über die Entwicklung sehr froh."));
  }
  
  @Override
  public String getId() {
    return "DE_VERBAGREEMENT";
  }
  
  @Override
  public String getDescription() {
    return "Kongruenz von Subjekt und Prädikat (nur 1. u. 2. Pers. od. m. Personalpronomen), z.B. 'Er bist (ist)'";
  }
  
  @Override
  public RuleMatch[] match(final AnalyzedSentence sentence) {
    
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    
    if (tokens.length < 4) { // ignore one-word sentences (3 tokens: SENT_START, one word, SENT_END)
      return toRuleMatchArray(ruleMatches);
    }
    
    // position of the pronouns:
    int posIch = -1;
    int posDu = -1;
    int posEr = -1;
    int posWir = -1;
    // positions of verbs which do match in person and number, and do not match any other person nor number:
    int posVer1Sin = -1;
    int posVer2Sin = -1;
    int posVer1Plu = -1;
    /*int posVer2Plu = -1;*/
    // positions of verbs which do match in person and number:
    int posPossibleVer1Sin = -1;
    int posPossibleVer2Sin = -1;
    int posPossibleVer3Sin = -1;
    int posPossibleVer1Plu = -1;
    /*int posPossibleVer2Plu = -1;*/
    
    for (int i = 1; i < tokens.length; ++i) { // ignore SENT_START
      
      String strToken = tokens[i].getToken().toLowerCase();
      strToken = strToken.replace("‚", "");

      switch (strToken) {
        case "ich":
          posIch = i;
          break;
        case "du":
          posDu = i;
          break;
        case "er":
          posEr = i;
          break;
        case "wir":
          posWir = i;
          break;
      }
      
      if (tokens[i].hasPartialPosTag("VER")
          && (Character.isLowerCase(tokens[i].getToken().charAt(0)) || i == 1) ) {
        if (hasUnambiguouslyPersonAndNumber(tokens[i], "1", "SIN")
            && !(strToken.equals("bin") && (BIN_IGNORE.contains(tokens[i-1].getToken())
                  || (tokens.length != i + 1 && tokens[i+1].getToken().startsWith("Laden")) ))) {
          posVer1Sin = i;
        } 
        else if (hasUnambiguouslyPersonAndNumber(tokens[i], "2", "SIN")) {
          posVer2Sin = i;
        } else if (hasUnambiguouslyPersonAndNumber(tokens[i], "1", "PLU")) {
          posVer1Plu = i;
//      } else if (hasUnambiguouslyPersonAndNumber(tokens[i], "2", "PLU")) {
//        posVer2Plu = i;
        }
        
        if (tokens[i].hasPartialPosTag(":1:SIN")) {
          posPossibleVer1Sin = i;
        }
        if (tokens[i].hasPartialPosTag(":2:SIN")) {
          posPossibleVer2Sin = i;
        }
        if (tokens[i].hasPartialPosTag(":3:SIN")) {
          posPossibleVer3Sin = i;
        }
        if (tokens[i].hasPartialPosTag(":1:PLU")) {
          posPossibleVer1Plu = i;
        }
//      if (tokens[i].hasPartialPosTag(":2:PLU"))
//        posPossibleVer2Plu = i;
        
      }
           
    } // for each token
      
    // "ich", "du", and "wir" must be subject (no other interpretation possible)
    // "ich", "du", "er", and "wir" must have a matching verb
    
    if (posVer1Sin != -1 && posIch == -1 && !isQuotationMark(tokens[posVer1Sin-1])) { // 1st pers sg verb but no "ich"
      ruleMatches.add(ruleMatchWrongVerb(tokens[posVer1Sin]));
    } else if (posIch > 0 && !isNear(posPossibleVer1Sin, posIch) // check whether verb next to "ich" is 1st pers sg
               && (tokens[posIch].getToken().equals("ich") || tokens[posIch].getStartPos() == 0) // ignore "lyrisches Ich" etc.
               && !isQuotationMark(tokens[posIch-1])) {
      final int plus1 = ((posIch + 1) == tokens.length) ? 0 : +1; // prevent posIch+1 segfault
      if (!verbDoesMatchPersonAndNumber(tokens[posIch-1], tokens[posIch+plus1], "1", "SIN")) {
        if (!nextButOneIsModal(tokens, posIch)) {
          ruleMatches.add(ruleMatchWrongVerbSubject(tokens[posIch], finiteVerb, "1:SIN"));
        }
      }
    }
    
    if (posVer2Sin != -1 && posDu == -1 && !isQuotationMark(tokens[posVer2Sin-1])) {
      ruleMatches.add(ruleMatchWrongVerb(tokens[posVer2Sin]));
    } else if (posDu > 0 && !isNear(posPossibleVer2Sin, posDu) && !isQuotationMark(tokens[posDu-1])) {
      final int plus1 = ((posDu + 1) == tokens.length) ? 0 : +1;
      if (!verbDoesMatchPersonAndNumber(tokens[posDu-1], tokens[posDu+plus1], "2", "SIN") &&
          !tokens[posDu+plus1].hasPartialPosTag("VER:1:SIN:KJ2") && // "Wenn ich du wäre"
          !tokens[posDu+plus1].hasPartialPosTag("ADJ:") && // "dass du  billige Klamotten..."
          !tokens[posDu-1].hasPartialPosTag("VER:1:SIN:KJ2")) {
        if (!nextButOneIsModal(tokens, posDu)) {
          ruleMatches.add(ruleMatchWrongVerbSubject(tokens[posDu], finiteVerb, "2:SIN"));
        }
      }
    }
    
    if (posEr > 0 && !isNear(posPossibleVer3Sin, posEr) && !isQuotationMark(tokens[posEr-1])) {
      final int plus1 = ((posEr + 1) == tokens.length) ? 0 : +1;
      if (!verbDoesMatchPersonAndNumber(tokens[posEr-1], tokens[posEr+plus1], "3", "SIN") 
              && !nextButOneIsModal(tokens, posEr)
              && !"äußerst".equals(finiteVerb.getToken())
              && !"regen".equals(finiteVerb.getToken())) {  // "wo er regen Anteil nahm"
        ruleMatches.add(ruleMatchWrongVerbSubject(tokens[posEr], finiteVerb, "3:SIN"));
      }
    }
    
    if (posVer1Plu != -1 && posWir == -1 && !isQuotationMark(tokens[posVer1Plu-1])) {
      ruleMatches.add(ruleMatchWrongVerb(tokens[posVer1Plu]));
    } else if (posWir > 0 && !isNear(posPossibleVer1Plu, posWir) && !isQuotationMark(tokens[posWir-1])) {
      final int plus1 = ((posWir + 1) == tokens.length) ? 0 : +1;
      if (!verbDoesMatchPersonAndNumber(tokens[posWir-1], tokens[posWir+plus1], "1", "PLU") && !nextButOneIsModal(tokens, posWir)) {
        ruleMatches.add(ruleMatchWrongVerbSubject(tokens[posWir], finiteVerb, "1:PLU"));
      }
    }
    
    return toRuleMatchArray(ruleMatches);
  }

  // avoid false alarm on 'wenn ich sterben sollte ...':
  private boolean nextButOneIsModal(AnalyzedTokenReadings[] tokens, int pos) {
    return pos < tokens.length - 2 && tokens[pos+2].hasPartialPosTag(":MOD:");
  }

  /**
   * @return true if |a - b| &lt; 5, and a != -1 
   */
  private boolean isNear(final int a, final int b) {
    return (Math.abs(a - b) < 5) && a != -1;
  }
  
  private boolean isQuotationMark(final AnalyzedTokenReadings token) {
    return QUOTATION_MARKS.contains(token.getToken());
  }
  
  /**
   * @return true if the verb @param token (if it is a verb) matches @param person and @param number, and matches no other person/number
   */
  private boolean hasUnambiguouslyPersonAndNumber(final AnalyzedTokenReadings tokenReadings, final String person, final String number) {
    if (tokenReadings.getToken().length() == 0
        || (Character.isUpperCase(tokenReadings.getToken().charAt(0)) && !(tokenReadings.getStartPos() == 0) )
        || !tokenReadings.hasPartialPosTag("VER")) {
      return false;
    }

    for (AnalyzedToken analyzedToken : tokenReadings) {
      final String postag = analyzedToken.getPOSTag();
      if (postag.contains("_END")) { // ignore SENT_END and PARA_END
        continue;
      }
      if (!postag.contains(":" + person + ":" + number)) {
        return false;
      }
    } // for each reading
    
    return true;
  }
  
  /**
   * @return true if @param token is a finite verb, and it is no participle, pronoun or number
   */
  private boolean isFiniteVerb(final AnalyzedTokenReadings token) {
    if (token.getToken().length() == 0
        || (Character.isUpperCase(token.getToken().charAt(0)) && token.getStartPos() != 0)
        || !token.hasPartialPosTag("VER")
        || token.hasPartialPosTag("PA2")
        || token.hasPartialPosTag("PRO:")
        || token.hasPartialPosTag("ZAL")
        || "einst".equals(token.getToken())) {
      return false;
    }
    return (token.hasPartialPosTag(":1:") || token.hasPartialPosTag(":2:") || token.hasPartialPosTag(":3:"));
  }
  
  /**
   * @return false if neither the verb @param token1 (if any) nor @param token2 match @param person and @param number, and none of them is "und" or ","
   * if a finite verb is found, it is saved in finiteVerb
   */
  private boolean verbDoesMatchPersonAndNumber(final AnalyzedTokenReadings token1, final AnalyzedTokenReadings token2,
                                               final String person, final String number) {
    if (token1.getToken().equals(",") || token1.getToken().equals("und") ||
        token2.getToken().equals(",") || token2.getToken().equals("und")) {
      return true;
    }
   
    boolean foundFiniteVerb = false;
    
    if (isFiniteVerb(token1)) {
      foundFiniteVerb = true;
      finiteVerb = token1;
      if (token1.hasPartialPosTag(":" + person + ":" + number)) {
        return true;
      }
    }
    
    if (isFiniteVerb(token2)) {
      foundFiniteVerb = true;
      finiteVerb = token2;
      if (token2.hasPartialPosTag(":" + person + ":" + number)) {
        return true;
      }
    }
    
    return !foundFiniteVerb;
  }
  
  /**
   * @return a list of forms of @param verb which match @param expectedVerbPOS (person:number)
   * @param toUppercase true when the suggestions should be capitalized
   */
  private List<String> getVerbSuggestions(final AnalyzedTokenReadings verb, final String expectedVerbPOS, final boolean toUppercase) {
    // find the first verb reading
    AnalyzedToken verbToken = new AnalyzedToken("","","");
    for (AnalyzedToken token : verb.getReadings()) {
      if (token.getPOSTag().startsWith("VER:")) {
        verbToken = token;
        break;
      }
    }
    
    try {
      String[] synthesized = language.getSynthesizer().synthesize(verbToken, "VER.*:"+expectedVerbPOS+".*", true);
      // remove duplicates
      Set<String> suggestionSet = new HashSet<>();
      suggestionSet.addAll(Arrays.asList(synthesized));
      List<String> suggestions = new ArrayList<>();
      suggestions.addAll(suggestionSet);
      if (toUppercase) {
        for (int i = 0; i < suggestions.size(); ++i) {
          suggestions.set(i, StringTools.uppercaseFirstChar(suggestions.get(i)));
        }
      }
      Collections.sort(suggestions);
      return suggestions;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  /**
   * @return a list of pronouns which match the person and number of @param verb
   * @param toUppercase true when the suggestions should be capitalized
   */
  private List<String> getPronounSuggestions(final AnalyzedTokenReadings verb, final boolean toUppercase) {
    List<String> result = new ArrayList<>();
    if (verb.hasPartialPosTag(":1:SIN")) {
      result.add("ich");
    }
    if (verb.hasPartialPosTag(":2:SIN")) {
      result.add("du");
    }
    if (verb.hasPartialPosTag(":3:SIN")) {
      result.add("er");
      result.add("sie");
      result.add("es");
    }
    if (verb.hasPartialPosTag(":1:PLU")) {
      result.add("wir");
    }
    if (verb.hasPartialPosTag(":2:PLU")) {
      result.add("ihr");
    }
    if (verb.hasPartialPosTag(":3:PLU") && !result.contains("sie")) { // do not add "sie" twice
      result.add("sie");
    }
    if (toUppercase) {
      for (int i = 0; i < result.size(); ++i) {
        result.set(i, StringTools.uppercaseFirstChar(result.get(i)));
      }
    }
    return result;
  }
  
  private RuleMatch ruleMatchWrongVerb(final AnalyzedTokenReadings token) {
    final String msg = "Möglicherweise fehlende grammatische Übereinstimmung zwischen Subjekt und Prädikat (" +
      token.getToken() + ") bezüglich Person oder Numerus (Einzahl, Mehrzahl - Beispiel: " +
      "'Max bist' statt 'Max ist').";
    return new RuleMatch(this, token.getStartPos(), token.getStartPos() + token.getToken().length(), msg);
  }
  
  private RuleMatch ruleMatchWrongVerbSubject(final AnalyzedTokenReadings subject, final AnalyzedTokenReadings verb, final String expectedVerbPOS) {
    final String msg = "Möglicherweise fehlende grammatische Übereinstimmung zwischen Subjekt (" + subject.getToken() +
      ") und Prädikat (" + verb.getToken() + ") bezüglich Person oder Numerus (Einzahl, Mehrzahl - Beispiel: " +
      "'ich sind' statt 'ich bin').";
    
    List<String> suggestions = new ArrayList<>();
    List<String> verbSuggestions = new ArrayList<>();
    List<String> pronounSuggestions = new ArrayList<>();
    
    RuleMatch ruleMatch;
    if (subject.getStartPos() < verb.getStartPos()) {
      ruleMatch = new RuleMatch(this, subject.getStartPos(), verb.getStartPos() + verb.getToken().length(), msg);
      verbSuggestions.addAll(getVerbSuggestions(verb, expectedVerbPOS, false));
      for (String verbSuggestion : verbSuggestions) {
        suggestions.add(subject.getToken() + " " + verbSuggestion);
      }
      pronounSuggestions.addAll(getPronounSuggestions(verb, Character.isUpperCase(subject.getToken().charAt(0))));
      for (String pronounSuggestion : pronounSuggestions) {
        suggestions.add(pronounSuggestion + " " + verb.getToken());
      }
      ruleMatch.setSuggestedReplacements(suggestions);
    } else {
      ruleMatch = new RuleMatch(this, verb.getStartPos(), subject.getStartPos() + subject.getToken().length(), msg);
      verbSuggestions.addAll(getVerbSuggestions(verb, expectedVerbPOS, Character.isUpperCase(verb.getToken().charAt(0))));
      for (String verbSuggestion : verbSuggestions) {
        suggestions.add(verbSuggestion + " " + subject.getToken());
      }
      pronounSuggestions.addAll(getPronounSuggestions(verb, false));
      for (String pronounSuggestion : pronounSuggestions) {
        suggestions.add(verb.getToken() + " " + pronounSuggestion);
      }
      ruleMatch.setSuggestedReplacements(suggestions);
    }
    
    return ruleMatch;
  }
  
  @Override
  public void reset() {
    finiteVerb = null;
  }

}
