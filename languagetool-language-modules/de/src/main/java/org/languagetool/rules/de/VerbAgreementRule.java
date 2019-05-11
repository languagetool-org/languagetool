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

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.language.German;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
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
 * <li>wenn nur ein mögliches finites Verb -&gt; das nehmen (Max machen das.)
 * <li>Sie (i&gt;1)
 * <li>bei ich/du/er/wir sofort prüfen, damit alle vorkommen geprüft werden (Ich geht jetzt nach Hause und dort gehe ich sofort unter die Dusche.) [aber: isNear]
 * <li>Alle Verbvorkommen merken (Er präsentieren wollte und Video hätte keine Pläne.)
 * </ul>
 * 
 * @author Markus Brenneis
 */
public class VerbAgreementRule extends TextLevelRule {

  private static final List<List<PatternToken>> ANTI_PATTERNS = Arrays.asList(
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("die|welche").build(),
      new PatternTokenBuilder().tokenRegex(".*").build(),
      new PatternTokenBuilder().tokenRegex("mehr|weniger").build(),
      new PatternTokenBuilder().token("als").build(),
      new PatternTokenBuilder().tokenRegex("ich|du|e[rs]|sie").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("wenn").build(),
      new PatternTokenBuilder().token("du").build(),
      new PatternTokenBuilder().token("anstelle").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("das").build(),
      new PatternTokenBuilder().csToken("Du").build(),
      new PatternTokenBuilder().token("anbieten").matchInflectedForms().build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token(",").build(),
      new PatternTokenBuilder().posRegex("VER:MOD:2:.*").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("Soll").build(),
      new PatternTokenBuilder().token("ich").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("Solltest").build(),
      new PatternTokenBuilder().token("du").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().csToken("Sollte").build(),
      new PatternTokenBuilder().tokenRegex("er|sie").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().pos(JLanguageTool.SENTENCE_START_TAGNAME).build(),  // "Bin gleich wieder da"
      new PatternTokenBuilder().csToken("Bin").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token(",").build(),  // "..., hast aber keine Ahnung!"
      new PatternTokenBuilder().tokenRegex("bin|hast").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("er").build(),  // "egal, was er sagen wird, ..."
      new PatternTokenBuilder().posRegex("VER:.*").build(),
      new PatternTokenBuilder().token("wird").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("wie|als").build(),  // "Ein Mann wie ich braucht einen Hut"
      new PatternTokenBuilder().token("ich").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().tokenRegex("ich").build(),  // "Ich weiß, was ich tun werde, falls etwas geschehen sollte."
      new PatternTokenBuilder().pos("VER:INF:NON").build(),
      new PatternTokenBuilder().token("werde").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().pos("VER:IMP:SIN:SFT").build(),  // "Kümmere du dich mal nicht darum!"
      new PatternTokenBuilder().token("du").build(),
      new PatternTokenBuilder().token("dich").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("sei").build(),
      new PatternTokenBuilder().token("du").build(),
      new PatternTokenBuilder().token("selbst").build()
    ),
    Arrays.asList(
      new PatternTokenBuilder().token("als").build(),  // "Du bist in dem Moment angekommen, als ich gegangen bin."
      new PatternTokenBuilder().token("ich").build(),
      new PatternTokenBuilder().posRegex("PA2:.*").build(),
      new PatternTokenBuilder().token("bin").build()
    ),
    Arrays.asList(
     new PatternTokenBuilder().token("als").build(),
     new PatternTokenBuilder().tokenRegex("du|e[rs]|sie|ich").build(),
     new PatternTokenBuilder().token("sein").matchInflectedForms().build(),
     new PatternTokenBuilder().tokenRegex("[\\.,]").build()
    ),
    Arrays.asList( // Musst du gehen?
     new PatternTokenBuilder().tokenRegex("D[au]rf.*|Muss.*").build(),
     new PatternTokenBuilder().posRegex("PRO:PER:NOM:.+").build(),
     new PatternTokenBuilder().posRegex("VER:INF:.+").build(),
     new PatternTokenBuilder().pos("PKT").build(),
     new PatternTokenBuilder().tokenRegex("(?!die).+").build()
    ),
    Arrays.asList(
     new PatternTokenBuilder().csToken("(").build(),
     new PatternTokenBuilder().posRegex("VER:2:SIN:.+").build(),
     new PatternTokenBuilder().csToken(")").build()
    ),
    Arrays.asList(
     new PatternTokenBuilder().posRegex("VER:MOD:1:PLU:.+").build(),
     new PatternTokenBuilder().csToken("wir").build(),
     new PatternTokenBuilder().csToken("bitte").build()
    )
  );

  // Words that prevent a rule match when they occur directly before "bin":
  private static final Set<String> BIN_IGNORE = new HashSet<>(Arrays.asList(
    "Suleiman",
    "Mohamed",
    "Muhammad",
    "Muhammed",
    "Mohammed",
    "Mohammad",
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
  
  private static final Set<String> CONJUNCTIONS = new HashSet<>(Arrays.asList(
    "weil",
    "obwohl",
    "dass",
    "indem",
    "sodass"/*,
    "damit",
    "wenn"*/
  ));

  private static final Set<String> QUOTATION_MARKS = new HashSet<>(Arrays.asList(
    "\"", "„"
  ));
  
  private final German language;

  public VerbAgreementRule(ResourceBundle messages, German language) {
    this.language = language;
    super.setCategory(Categories.GRAMMAR.getCategory(messages));
    addExamplePair(Example.wrong("Ich <marker>bist</marker> über die Entwicklung sehr froh."),
                   Example.fixed("Ich <marker>bin</marker> über die Entwicklung sehr froh."));
  }
  
  @Override
  public String getId() {
    return "DE_VERBAGREEMENT";
  }
  
  @Override
  public String getDescription() {
    return "Kongruenz von Subjekt und Prädikat (nur 1. u. 2. Person oder m. Personalpronomen), z.B. 'Er bist (ist)'";
  }
  
  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      int idx = 0;
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      AnalyzedSentence partialSentence;
      for(int i = 2; i < tokens.length; i++) {
        if(",".equals(tokens[i-2].getToken()) && CONJUNCTIONS.contains(tokens[i].getToken())) {
          partialSentence = new AnalyzedSentence(Arrays.copyOfRange(tokens, idx, i));
          ruleMatches.addAll(match(partialSentence, pos));
          idx = i;
        }
      }
      partialSentence = new AnalyzedSentence(Arrays.copyOfRange(tokens, idx, tokens.length));
      ruleMatches.addAll(match(partialSentence, pos));
      pos += sentence.getText().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

  private List<RuleMatch> match(AnalyzedSentence sentence, int pos) {

    AnalyzedTokenReadings finiteVerb = null;
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    
    if (tokens.length < 4) { // ignore one-word sentences (3 tokens: SENT_START, one word, SENT_END)
      return ruleMatches;
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

      if (tokens[i].isImmunized()) {
        continue;
      }

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
          && (Character.isLowerCase(tokens[i].getToken().charAt(0)) || i == 1 || isQuotationMark(tokens[i-1])) ) {
        if (hasUnambiguouslyPersonAndNumber(tokens[i], "1", "SIN")
            && !(strToken.equals("bin") && (BIN_IGNORE.contains(tokens[i-1].getToken())
                  || (tokens.length != i + 1 && tokens[i+1].getToken().startsWith("Laden")) ))) {
          posVer1Sin = i;
        } 
        else if (hasUnambiguouslyPersonAndNumber(tokens[i], "2", "SIN") && !"Probst".equals(tokens[i].getToken())) {
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
      ruleMatches.add(ruleMatchWrongVerb(tokens[posVer1Sin], pos, sentence));
    } else if (posIch > 0 && !isNear(posPossibleVer1Sin, posIch) // check whether verb next to "ich" is 1st pers sg
               && (tokens[posIch].getToken().equals("ich") || tokens[posIch].getStartPos() <= 1) // ignore "lyrisches Ich" etc.
               && (!isQuotationMark(tokens[posIch-1])  || posIch < 3 || (posIch > 1 && tokens[posIch-2].getToken().equals(":")))) {
      int plus1 = ((posIch + 1) == tokens.length) ? 0 : +1; // prevent posIch+1 segfault
      BooleanAndFiniteVerb check = verbDoesMatchPersonAndNumber(tokens[posIch - 1], tokens[posIch + plus1], "1", "SIN", finiteVerb);
      if (!check.verbDoesMatchPersonAndNumber && !nextButOneIsModal(tokens, posIch) && !"äußerst".equals(check.finiteVerb.getToken())) {
        ruleMatches.add(ruleMatchWrongVerbSubject(tokens[posIch], check.finiteVerb, "1:SIN", pos, sentence));
      }
    }
    
    if (posVer2Sin != -1 && posDu == -1 && !isQuotationMark(tokens[posVer2Sin-1])) {
      ruleMatches.add(ruleMatchWrongVerb(tokens[posVer2Sin], pos, sentence));
    } else if (posDu > 0 && !isNear(posPossibleVer2Sin, posDu)
               &&(!isQuotationMark(tokens[posDu-1]) || posDu < 3 || (posDu > 1 && tokens[posDu-2].getToken().equals(":")))) {
      int plus1 = ((posDu + 1) == tokens.length) ? 0 : +1;
      BooleanAndFiniteVerb check = verbDoesMatchPersonAndNumber(tokens[posDu - 1], tokens[posDu + plus1], "2", "SIN", finiteVerb);
      if (!check.verbDoesMatchPersonAndNumber &&
          !tokens[posDu+plus1].hasPosTagStartingWith("VER:1:SIN:KJ2") && // "Wenn ich du wäre"
          !(tokens[posDu+plus1].hasPosTagStartingWith("ADJ:") && !tokens[posDu+plus1].hasPosTag("ADJ:PRD:GRU"))&& // "dass du billige Klamotten..."
          !tokens[posDu-1].hasPosTagStartingWith("VER:1:SIN:KJ2") &&
          !nextButOneIsModal(tokens, posDu)) {
        ruleMatches.add(ruleMatchWrongVerbSubject(tokens[posDu], check.finiteVerb, "2:SIN", pos, sentence));
      }
    }
    
    if (posEr > 0 && !isNear(posPossibleVer3Sin, posEr)
        && (!isQuotationMark(tokens[posEr-1])  || posEr < 3 || (posEr > 1 && tokens[posEr-2].getToken().equals(":")))) {
      int plus1 = ((posEr + 1) == tokens.length) ? 0 : +1;
      BooleanAndFiniteVerb check = verbDoesMatchPersonAndNumber(tokens[posEr - 1], tokens[posEr + plus1], "3", "SIN", finiteVerb);
      if (!check.verbDoesMatchPersonAndNumber 
              && !nextButOneIsModal(tokens, posEr)
              && !"äußerst".equals(check.finiteVerb.getToken())
              && !"regen".equals(check.finiteVerb.getToken())) {  // "wo er regen Anteil nahm"
        ruleMatches.add(ruleMatchWrongVerbSubject(tokens[posEr], check.finiteVerb, "3:SIN", pos, sentence));
      }
    }
    
    if (posVer1Plu != -1 && posWir == -1 && !isQuotationMark(tokens[posVer1Plu-1])) {
      ruleMatches.add(ruleMatchWrongVerb(tokens[posVer1Plu], pos, sentence));
    } else if (posWir > 0 && !isNear(posPossibleVer1Plu, posWir) && !isQuotationMark(tokens[posWir-1])) {
      int plus1 = ((posWir + 1) == tokens.length) ? 0 : +1;
      BooleanAndFiniteVerb check = verbDoesMatchPersonAndNumber(tokens[posWir - 1], tokens[posWir + plus1], "1", "PLU", finiteVerb);
      if (!check.verbDoesMatchPersonAndNumber && !nextButOneIsModal(tokens, posWir)) {
        ruleMatches.add(ruleMatchWrongVerbSubject(tokens[posWir], check.finiteVerb, "1:PLU", pos, sentence));
      }
    }
    
    return ruleMatches;
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return makeAntiPatterns(ANTI_PATTERNS, language);
  }

  // avoid false alarm on 'wenn ich sterben sollte ...':
  private boolean nextButOneIsModal(AnalyzedTokenReadings[] tokens, int pos) {
    return pos < tokens.length - 2 && tokens[pos+2].hasPartialPosTag(":MOD:");
  }

  /**
   * @return true if |a - b| < 5, and a != -1 
   */
  private boolean isNear(int a, int b) {
    return a != -1 && (Math.abs(a - b) < 5);
  }
  
  private boolean isQuotationMark(AnalyzedTokenReadings token) {
    return QUOTATION_MARKS.contains(token.getToken());
  }
  
  /**
   * @return true if the verb @param token (if it is a verb) matches @param person and @param number, and matches no other person/number
   */
  private boolean hasUnambiguouslyPersonAndNumber(AnalyzedTokenReadings tokenReadings, String person, String number) {
    if (tokenReadings.getToken().length() == 0
        || (Character.isUpperCase(tokenReadings.getToken().charAt(0)) && tokenReadings.getStartPos() != 0)
        || !tokenReadings.hasPosTagStartingWith("VER")) {
      return false;
    }
    for (AnalyzedToken analyzedToken : tokenReadings) {
      String postag = analyzedToken.getPOSTag();
      if (postag == null || postag.endsWith("_END")) { // ignore SENT_END and PARA_END
        continue;
      }
      if (!postag.contains(":" + person + ":" + number)) {
        return false;
      }
    }
    return true;
  }
  
  /**
   * @return true if @param token is a finite verb, and it is no participle, pronoun or number
   */
  private boolean isFiniteVerb(AnalyzedTokenReadings token) {
    if (token.getToken().length() == 0
        || (Character.isUpperCase(token.getToken().charAt(0)) && token.getStartPos() != 0)
        || !token.hasPosTagStartingWith("VER")
        || token.hasAnyPartialPosTag("PA2", "PRO:", "ZAL")
        || "einst".equals(token.getToken())) {
      return false;
    }
    return token.hasAnyPartialPosTag(":1:", ":2:", ":3:");
  }
  
  /**
   * @return false if neither the verb @param token1 (if any) nor @param token2 match @param person and @param number, and none of them is "und" or ","
   * if a finite verb is found, it is saved in finiteVerb
   */
  private BooleanAndFiniteVerb verbDoesMatchPersonAndNumber(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2,
                                               String person, String number, AnalyzedTokenReadings finiteVerb) {
    if (StringUtils.equalsAny(token1.getToken(), ",", "und","sowie") ||
    		StringUtils.equalsAny(token2.getToken(), ",", "und","sowie")) {
      return new BooleanAndFiniteVerb(true, finiteVerb);
    }
   
    boolean foundFiniteVerb = false;
    
    if (isFiniteVerb(token1)) {
      foundFiniteVerb = true;
      finiteVerb = token1;
      if (token1.hasPartialPosTag(":" + person + ":" + number)) {
        return new BooleanAndFiniteVerb(true, finiteVerb);
      }
    }
    
    if (isFiniteVerb(token2)) {
      foundFiniteVerb = true;
      finiteVerb = token2;
      if (token2.hasPartialPosTag(":" + person + ":" + number)) {
        return new BooleanAndFiniteVerb(true, finiteVerb);
      }
    }
    
    return new BooleanAndFiniteVerb(!foundFiniteVerb, finiteVerb);
  }
  
  /**
   * @return a list of forms of @param verb which match @param expectedVerbPOS (person:number)
   * @param toUppercase true when the suggestions should be capitalized
   */
  private List<String> getVerbSuggestions(AnalyzedTokenReadings verb, String expectedVerbPOS, boolean toUppercase) {
    // find the first verb reading
    AnalyzedToken verbToken = new AnalyzedToken("", "", "");
    for (AnalyzedToken token : verb.getReadings()) {
      //noinspection ConstantConditions
      if (token.getPOSTag().startsWith("VER:")) {
        verbToken = token;
        break;
      }
    }
    
    try {
      String[] synthesized = language.getSynthesizer().synthesize(verbToken, "VER.*:"+expectedVerbPOS+".*", true);
      Set<String> suggestionSet = new HashSet<>(Arrays.asList(synthesized));  // remove duplicates
      List<String> suggestions = new ArrayList<>(suggestionSet);
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
  private List<String> getPronounSuggestions(AnalyzedTokenReadings verb, boolean toUppercase) {
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
  
  private RuleMatch ruleMatchWrongVerb(AnalyzedTokenReadings token, int pos, AnalyzedSentence sentence) {
    String msg = "Möglicherweise fehlende grammatische Übereinstimmung zwischen Subjekt und Prädikat (" +
      token.getToken() + ") bezüglich Person oder Numerus (Einzahl, Mehrzahl - Beispiel: " +
      "'Max bist' statt 'Max ist').";
    return new RuleMatch(this, sentence, pos+token.getStartPos(), pos+token.getEndPos(), msg);
  }
  
  private RuleMatch ruleMatchWrongVerbSubject(AnalyzedTokenReadings subject, AnalyzedTokenReadings verb, String expectedVerbPOS, int pos, AnalyzedSentence sentence) {
    String msg = "Möglicherweise fehlende grammatische Übereinstimmung zwischen Subjekt (" + subject.getToken() +
      ") und Prädikat (" + verb.getToken() + ") bezüglich Person oder Numerus (Einzahl, Mehrzahl - Beispiel: " +
      "'ich sind' statt 'ich bin').";
    
    List<String> suggestions = new ArrayList<>();
    List<String> verbSuggestions = new ArrayList<>();
    List<String> pronounSuggestions = new ArrayList<>();
    
    RuleMatch ruleMatch;
    if (subject.getStartPos() < verb.getStartPos()) {
      ruleMatch = new RuleMatch(this, sentence, pos+subject.getStartPos(), pos+verb.getStartPos()+verb.getToken().length(), msg);
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
      ruleMatch = new RuleMatch(this, sentence, pos+verb.getStartPos(), pos+subject.getStartPos()+subject.getToken().length(), msg);
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
  
  static class BooleanAndFiniteVerb {
    boolean verbDoesMatchPersonAndNumber;
    AnalyzedTokenReadings finiteVerb;
    private BooleanAndFiniteVerb(boolean verbDoesMatchPersonAndNumber, AnalyzedTokenReadings finiteVerb) {
      this.verbDoesMatchPersonAndNumber = verbDoesMatchPersonAndNumber;
      this.finiteVerb = finiteVerb;
    }
  }
}