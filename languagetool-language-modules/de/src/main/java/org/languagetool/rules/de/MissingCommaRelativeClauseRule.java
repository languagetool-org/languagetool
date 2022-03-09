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

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.German;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.Category;
import org.languagetool.rules.Category.Location;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static org.languagetool.rules.patterns.PatternRuleBuilderHelper.*;
import org.languagetool.rules.patterns.PatternTokenBuilder;

/**
 * A rule checks a sentence for a missing comma before or after a relative clause (only for German language).
 * @author Fred Kruse
 */
public class MissingCommaRelativeClauseRule extends Rule {

  private static final Pattern MARKS_REGEX = Pattern.compile("[,;.:?•!-–—’'\"„“”…»«‚‘›‹()\\/\\[\\]]");

  private final boolean behind;

  private static final German GERMAN = new GermanyGerman();

  private static final List<DisambiguationPatternRule> ANTI_PATTERNS = makeAntiPatterns(Arrays.asList(
      Arrays.asList(
        regex("gerade|wenn"),
        token("das")
      ),
      Arrays.asList(
        token("anstelle"),
        regex("diese[rs]")
      ),
      Arrays.asList(
        csToken("mit"),
        regex("de[mr]"),
        regex("de[mrs]"),
        posRegex("SUB:.+"),
        csToken("verbindet")
      ),
      Arrays.asList(
        regex("eine"),
        csToken("menge"),
        posRegex("SUB:.+")
      ),
      Arrays.asList( // dass sich wie folgt formulieren lässt
        regex("wie"),
        csToken("folgt"),
        posRegex("VER:.+")
      ),
      Arrays.asList( // das sollte gut überlegt sein
        regex("gut"),
        csToken("überlegt"),
        csToken("sein")
      ),
      Arrays.asList( // samt Auftraggeber
        csToken("samt"),
        posRegex("SUB:DAT.*")
      ),
      Arrays.asList( // ... denen sie ausgesetzt sind.
        posRegex("PA2:PRD:GRU:VER"),
        csToken("sind"),
        posRegex("PKT")
      ),
      Arrays.asList(
        csToken("am"),
        pos("ADJ:PRD:SUP"),
        posRegex("PRP:.+"),
        regex("d(e[mnr]|ie|as|e([nr]|ss)en)")
      ),
      Arrays.asList( // Aber denen etwas beibringen zu müssen, die sich sträuben, das ist die wahre Hölle.
        pos("SENT_START"),
        token("Aber"),
        regex("der|die|denen|das|jenen|einigen|anderen|vielen|manchen|allen")
      ),
      Arrays.asList( // Aus diesem Grund sind die Wörter nicht direkt übersetzt, stattdessen wird der Zustand oder die Situation beschrieben in der die Wörter benutzt werden. 
        posRegex("PA2.*|VER:PA2.*"),
        token("werden"),
        regex("[\\.\\!\\?…\\:;]+")
      ),
      Arrays.asList( // Plan von Maßnahmen, mit denen das Ansteckungsrisiko während des Aufenthalts an einem Ort verringert werden soll
        token("werden"),
        new PatternTokenBuilder().posRegex("SENT_END").matchInflectedForms().tokenRegex("sollen|können|müssen").build()
      ),
      Arrays.asList(
        posRegex("VER:.*1:SIN:KJ1:.+"),
        posRegex("VER:MOD:[12]:.+"),
        posRegex("PKT|KON:NEB")
      )
  ), GERMAN);

  public MissingCommaRelativeClauseRule(ResourceBundle messages) {
    this(messages, false);
  }

  public MissingCommaRelativeClauseRule(ResourceBundle messages, boolean behind) {
    super(messages);
    super.setCategory(new Category(new CategoryId("HILFESTELLUNG_KOMMASETZUNG"),
        "Kommasetzung", Location.INTERNAL, true));
    this.behind = behind;
  }

  @Override
  public String getId() {
    return (behind ? "COMMA_BEHIND_RELATIVE_CLAUSE" : "COMMA_IN_FRONT_RELATIVE_CLAUSE");
  }

  @Override
  public String getDescription() {
    return (behind ? "Fehlendes Komma nach Relativsatz" : "Fehlendes Komma vor Relativsatz");
  }

  /**
   * is a separator
   */
  private static boolean isSeparator(String token) {
    return (MARKS_REGEX.matcher(token).matches() || token.equals("und") || token.equals("oder"));
  }

  /**
   * get the position of the next separator
   */
  private static int nextSeparator(AnalyzedTokenReadings[] tokens, int start) {
    for(int i = start; i < tokens.length; i++) {
      if(isSeparator(tokens[i].getToken())) {
        return i;
      }
    }
    return tokens.length - 1;
  }

  /**
   * is preposition
   */
  private static boolean isPrp(AnalyzedTokenReadings token) {
    return token.hasPosTagStartingWith("PRP:") && !token.isImmunized();
  }

  /**
   * is a potential verb used in sentence or subclause
   */
  private static boolean isVerb(AnalyzedTokenReadings[] tokens, int n) {
    return (tokens[n].matchesPosTagRegex("(VER:[1-3]:|VER:.*:[1-3]:).*")
        && !tokens[n].matchesPosTagRegex("(ZAL|AD[JV]|ART|SUB|PRO:POS).*")
        && (!tokens[n].hasPosTagStartingWith("VER:INF:") || !tokens[n-1].getToken().equals("zu"))
        && !tokens[n].isImmunized()
      );
  }

  /**
   * is any verb but not an "Infinitiv mit zu"
   */
  private static boolean isAnyVerb(AnalyzedTokenReadings[] tokens, int n) {
    return tokens[n].hasPosTagStartingWith("VER:")
        || (n < tokens.length - 1
            && ((tokens[n].getToken().equals("zu") && tokens[n+1].hasPosTagStartingWith("VER:INF:"))
             || (tokens[n].hasPosTag("NEG") && tokens[n+1].hasPosTagStartingWith("VER:"))));
  }

  /**
   * is a verb after sub clause
   */
  static boolean isVerbBehind(AnalyzedTokenReadings[] tokens, int end) {
    return (end < tokens.length - 1 && tokens[end].getToken().equals(",") && tokens[end+1].hasPosTagStartingWith("VER:"));
  }

  /**
   * gives the positions of verbs in a subclause
   */
  private static List<Integer> verbPos(AnalyzedTokenReadings[] tokens, int start, int end) {
    List<Integer>verbs = new ArrayList<>();
    for(int i = start; i < end; i++) {
      if(isVerb(tokens, i)) {
        if(tokens[i].hasPosTagStartingWith("PA")) {
          String gender = getGender(tokens[i]);
          String sStr = "(ADJ|PA[12]):.*" + gender +".*";
          int j;
          for(j = i + 1; j < end && tokens[j].matchesPosTagRegex(sStr); j++);
          if(!tokens[j].matchesPosTagRegex("(SUB|EIG):.*" + gender +".*") && !tokens[j].isPosTagUnknown()) {
            verbs.add(i);
          }
        } else {
          verbs.add(i);
        }
      }
    }
    return verbs;
  }

  /**
   * first token initiate a subclause
   */
  private static boolean isKonUnt(AnalyzedTokenReadings token) {
    return (token.hasPosTag("KON:UNT")
        || StringUtils.equalsAnyIgnoreCase(token.getToken(), "wer", "wo", "wohin"));
  }


  /**
   * checks to what position a test of relative clause should done
   * return -1 if no potential relative clause is assumed
   */
  private static int hasPotentialSubclause(AnalyzedTokenReadings[] tokens, int start, int end) {
    List<Integer> verbs = verbPos(tokens, start, end);
    if(verbs.size() == 1 && end < tokens.length - 2 && verbs.get(0) == end - 1) {
      int nextEnd = nextSeparator(tokens, end + 1);
      List<Integer> nextVerbs = verbPos(tokens, end + 1, nextEnd);
      if(isKonUnt(tokens[start])) {
        if(nextVerbs.size() > 1 || (nextVerbs.size() == 1 && nextVerbs.get(0) == end - 1)) {
          return verbs.get(0);
        }
      } else if(nextVerbs.size() > 0) {
        return verbs.get(0);
      }
      return -1;
    }
    if(verbs.size() == 2) {
      if(tokens[verbs.get(0)].matchesPosTagRegex("VER:(MOD|AUX):.*") && tokens[verbs.get(1)].hasPosTagStartingWith("VER:INF:")) {
        return verbs.get(0);
      }
      if(tokens[verbs.get(0)].hasPosTagStartingWith("VER:AUX:") && tokens[verbs.get(1)].hasPosTagStartingWith("VER:PA2:")) {
        return -1;
      }
      if(end == tokens.length - 1 && verbs.get(0) == end - 2
          && tokens[verbs.get(0)].hasPosTagStartingWith("VER:INF:") && tokens[verbs.get(1)].hasPosTagStartingWith("VER:MOD:")) {
        return -1;
      }
    }
    if(verbs.size() == 3) {
      if(tokens[verbs.get(0)].hasPosTagStartingWith("VER:MOD:")
          && ((tokens[verbs.get(2) - 1].matchesPosTagRegex("VER:(INF|PA2):.*") && tokens[verbs.get(2)].hasPosTagStartingWith("VER:INF:"))
              || (tokens[verbs.get(1) - 1].getToken().equals("weder") && tokens[verbs.get(1)].hasPosTagStartingWith("VER:INF:")
                  && tokens[verbs.get(2) - 1].getToken().equals("noch") && tokens[verbs.get(1)].hasPosTagStartingWith("VER:INF:")))
        ) {
        return -1;
      }
    }
    if(verbs.size() > 1) {
      return verbs.get(verbs.size() - 1);
    }
    return -1;
  }

  /**
   * is potential relative pronoun
   */
  private static boolean isPronoun(AnalyzedTokenReadings[] tokens, int n) {
    return (tokens[n].getToken().matches("(d(e[mnr]|ie|as|e([nr]|ss)en)|welche[mrs]?|wessen|was)")
            && !tokens[n - 1].getToken().equals("sowie"));
  }

  /**
   * get the gender of of a token
   */
  private static String getGender(AnalyzedTokenReadings token) {
    int nMatches = 0;
    String ret = "";
    if(token.matchesPosTagRegex(".*:SIN:FEM.*")) {
      ret += "SIN:FEM";
      nMatches++;
    }
    if(token.matchesPosTagRegex(".*:SIN:MAS.*")) {
      if(nMatches > 0) {
        ret += "|";
      }
      ret += "SIN:MAS";
      nMatches++;
    }
    if(token.matchesPosTagRegex(".*:SIN:NEU.*")) {
      if(nMatches > 0) {
        ret += "|";
      }
      ret += "SIN:NEU";
      nMatches++;
    }
    if(token.matchesPosTagRegex(".*:PLU.*")) {
      if(nMatches > 0) {
        ret += "|";
      }
      ret += "PLU";
      nMatches++;
    }
    if(nMatches > 1) {
      ret = "(" + ret + ")";
    }
    return ret;
  }

  /**
   * does the gender match with a subject or name?
   */
  private static boolean matchesGender(String gender, AnalyzedTokenReadings[] tokens, int from, int to) {
    String mStr;
    if(gender.isEmpty()) {
      mStr = "PRO:DEM:.*SIN:NEU.*";
    } else {
      mStr = "(SUB|EIG):.*" + gender +".*";
    }
    for (int i = to - 1; i >= from; i-- ) {
      if(tokens[i].matchesPosTagRegex(mStr) && (i != 1 || !tokens[i].hasPosTagStartingWith("VER:"))) {
        return true;
      }
    }
    return false;
  }

  /**
   * is the token a potential article without a noun
   */
  private static boolean isArticleWithoutSub(String gender, AnalyzedTokenReadings[] tokens, int n) {
    if(gender.isEmpty()) {
      return false;
    }
    return tokens[n].hasPosTagStartingWith("VER:") && tokens[n - 1].matchesPosTagRegex("(ADJ|PRO:POS):.*" + gender + ".*");
  }

  /**
   * skip tokens till the next noun
   * check for e.g. "das in die dunkle Garage fahrende Auto" -> "das" is article
   */
  private static int skipSub(AnalyzedTokenReadings[] tokens, int n, int to) {
    String gender = getGender(tokens[n]);
    for(int i = n + 1; i < to; i++) {
      if(tokens[i].matchesPosTagRegex("(SUB|EIG):.*" + gender + ".*")) {
        return i;
      }
    }
    return -1;
  }

  /**
   * skip tokens till the next noun
   * check for e.g. "das in die dunkle Garage fahrende Auto" -> "das" is article
   */
  private static int skipToSub(String gender, AnalyzedTokenReadings[] tokens, int n, int to) {
    if(tokens[n+1].matchesPosTagRegex("PA[12]:.*" + gender + ".*")) {
      return n+1;
    }
    for(int i = n + 1; i < to; i++) {
      if(tokens[i].matchesPosTagRegex("(ADJ|PA[12]):.*" + gender + ".*") || tokens[i].isPosTagUnknown()) {
        return i;
      }
      if(tokens[i].hasPosTagStartingWith("ART")) {
        i = skipSub(tokens, i, to);
        if (i < 0) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * check if token is potentially an article
   */
  private static boolean isArticle(String gender, AnalyzedTokenReadings[] tokens, int from, int to) {
    if(gender.isEmpty()) {
      return false;
    }
    String sSub = "(SUB|EIG):.*" + gender +".*";
    String sAdj = "(ZAL|PRP:|KON:|ADV:|ADJ:PRD:|(ADJ|PA[12]|PRO:(POS|DEM|IND)):.*" + gender +").*";
    for (int i = from + 1; i < to; i++ ) {
      if(tokens[i].matchesPosTagRegex(sSub) || tokens[i].isPosTagUnknown()) {
        return true;
      }
      if((tokens[i].hasPosTagStartingWith("ART")) || !tokens[i].matchesPosTagRegex(sAdj)) {
        if(isArticleWithoutSub(gender, tokens, i)) {
          return true;
        }
        int skipTo = skipToSub(gender, tokens, i, to);
        if(skipTo > 0) {
          i = skipTo;
        } else {
          return false;
        }
      }
    }
    if(to < tokens.length && isArticleWithoutSub(gender, tokens, to)) {
      return true;
    }
    return false;
  }

  /**
   * gives back position where a comma is missed
   * PRP has to be treated separately
   */
  private static int missedCommaInFront(AnalyzedTokenReadings[] tokens, int start, int end, int lastVerb) {
    for(int i = start; i < lastVerb - 1; i++) {
      if (tokens[i].isImmunized()) {
        continue;
      }
      if(isPronoun(tokens, i)) {
        String gender = getGender(tokens[i]);
        if(gender != null && !isAnyVerb(tokens, i + 1)
            && matchesGender(gender, tokens, start, i) && !isArticle(gender, tokens, i, lastVerb)) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * is a special combination of two verbs combination
   */
  private static boolean isTwoCombinedVerbs(AnalyzedTokenReadings first, AnalyzedTokenReadings second) {
    return first.matchesPosTagRegex("(VER:.*INF|.*PA[12]:).*") && second.hasPosTagStartingWith("VER:");
  }

  /**
   * is a special combination of three verbs combination
   */
  private static boolean isThreeCombinedVerbs(AnalyzedTokenReadings[] tokens, int first, int last) {
    return tokens[first].matchesPosTagRegex("VER:(AUX|INF|PA[12]).*") && tokens[first + 1].matchesPosTagRegex("VER:(.*INF|PA[12]).*")
        && tokens[last].matchesPosTagRegex("VER:(MOD|AUX).*");
  }

  /**
   * is a special combination of four verbs combination
   */
  private static boolean isFourCombinedVerbs(AnalyzedTokenReadings[] tokens, int first, int last) {
    return tokens[first].hasPartialPosTag("KJ2") && tokens[first + 1].hasPosTagStartingWith("PA2")
        && tokens[first + 2].matchesPosTagRegex("VER:(.*INF|PA[12]).*")
        && tokens[last].matchesPosTagRegex("VER:(MOD|AUX).*");
  }

  /**
   * is participle
   */
  private static boolean isPar(AnalyzedTokenReadings token) {
    return token.hasPosTagStartingWith("PA2:");
  }

  /**
   * is participle plus special combination of two verbs combination
   */
  private static boolean isInfinitivZu(AnalyzedTokenReadings[] tokens, int last) {
    return tokens[last - 1 ].getToken().equals("zu")&& tokens[last].matchesPosTagRegex("VER:.*INF.*");
  }

  /**
   * is verb plus special combination of two verbs combination
   */
  private static boolean isTwoPlusCombinedVerbs(AnalyzedTokenReadings[] tokens, int first, int last) {
    return tokens[first].matchesPosTagRegex(".*PA[12]:.*") && tokens[last-1].matchesPosTagRegex("VER:.*INF.*");
  }

  /**
   * conjunction follows last verb
   */
  private static boolean isKonAfterVerb(AnalyzedTokenReadings[] tokens, int start, int end) {
    if(tokens[start].matchesPosTagRegex("VER:(MOD|AUX).*") && tokens[start + 1].matchesPosTagRegex("(KON|PRP).*")) {
      if(start + 3 == end) {
        return true;
      }
      for(int i = start + 2; i < end; i++) {
        if(tokens[i].matchesPosTagRegex("(SUB|PRO:PER).*")) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * two infinitive verbs as pair
   */
  private static boolean isSpecialPair(AnalyzedTokenReadings[] tokens, int first, int second) {
    if(first + 3 >= second && tokens[first].matchesPosTagRegex("VER:.*INF.*")
        && StringUtils.equalsAny(tokens[first+1].getToken(), "als", "noch")
        && tokens[first + 2].matchesPosTagRegex("VER:.*INF.*")) {
      if(first + 2 == second) {
        return true;
      }
      return isTwoCombinedVerbs(tokens[second - 1], tokens[second]);
    }
    return false;
  }

  /**
   * is a pair of verbs to build the perfect
   */
  private static boolean isPerfect(AnalyzedTokenReadings[] tokens, int first, int second) {
    return tokens[first].hasPosTagStartingWith("VER:AUX:") && tokens[second].matchesPosTagRegex("VER:.*(INF|PA2).*");
  }

  /**
   * is Infinitive in combination with substantiv
   */
  private static boolean isSpecialInf(AnalyzedTokenReadings[] tokens, int first, int second, int start) {
    if(!tokens[first].hasPosTagStartingWith("VER:INF")) {
      return false;
    }
    for(int i = first - 1; i > start; i--) {
      if(tokens[i].hasPosTagStartingWith("ART")) {
        i = skipSub(tokens, i, second);
        return i > 0;
      }
    }
    return false;
  }

  /**
   * is a pair of verbs to build the perfect
   */
  private static boolean isPerfect(AnalyzedTokenReadings[] tokens, int first, int second, int third) {
    return tokens[second].matchesPosTagRegex("VER:.*INF.*") && isPerfect(tokens, first, third);
  }

  /**
   * is separator or VER:INF
   */
  private static boolean isSeparatorOrInf(AnalyzedTokenReadings[] tokens, int n) {
    return isSeparator(tokens[n].getToken()) || tokens[n].hasPosTagStartingWith("VER:INF")
        || (tokens.length > n + 1 && tokens[n].getToken().equals("zu") && tokens[n + 1].matchesPosTagRegex("VER:.*INF.*"));
  }

  /**
   * gives back position where a comma is missed
   */
  private static int getCommaBehind(AnalyzedTokenReadings[] tokens, List<Integer> verbs, int start, int end) {
    if(verbs.size() == 1) {
      if(isSeparator(tokens[verbs.get(0) + 1].getToken())) {
        return -1;
      }
      return verbs.get(0);
    } else if(verbs.size() == 2) {
      if(isSpecialPair(tokens, verbs.get(0), verbs.get(1))) {
        if(isSeparatorOrInf(tokens, verbs.get(1) + 1)) {
          return -1;
        }
        return verbs.get(1);
      } else if(verbs.get(0) + 1 == verbs.get(1)) {
        if(isTwoCombinedVerbs(tokens[verbs.get(0)], tokens[verbs.get(1)])) {
          if(isSeparatorOrInf(tokens, verbs.get(1) + 1) || isKonAfterVerb(tokens, verbs.get(1), end)) {
            return -1;
          }
          return verbs.get(1);
        }
      } else if(verbs.get(0) + 2 == verbs.get(1)) {
        if(isThreeCombinedVerbs(tokens, verbs.get(0), verbs.get(1))) {
          if(isSeparatorOrInf(tokens, verbs.get(1) + 1)) {
            return -1;
          }
          return verbs.get(1);
        }
      }
      if(isPar(tokens[verbs.get(0)]) || isPerfect(tokens, verbs.get(0), verbs.get(1))
          || isInfinitivZu(tokens, verbs.get(1)) || isSpecialInf(tokens, verbs.get(0), verbs.get(1), start)) {
        if(isSeparatorOrInf(tokens, verbs.get(1) + 1)) {
          return -1;
        }
        return verbs.get(1);
      }
    } else if(verbs.size() == 3) {
      if(isTwoPlusCombinedVerbs(tokens, verbs.get(0), verbs.get(2))) {
        if(isSeparatorOrInf(tokens, verbs.get(2) + 1)) {
          return -1;
        }
        return verbs.get(2);
      } else if(verbs.get(0) + 2 == verbs.get(2)) {
        if(verbs.get(0) + 1 == verbs.get(1) && isThreeCombinedVerbs(tokens, verbs.get(0), verbs.get(2))) {
          if(isSeparatorOrInf(tokens, verbs.get(2) + 1)) {
            return -1;
          }
          return verbs.get(2);
        }
      } else if(verbs.get(0) + 3 == verbs.get(2) && isFourCombinedVerbs(tokens, verbs.get(0), verbs.get(2))) {
        if(isSeparatorOrInf(tokens, verbs.get(2) + 1)) {
          return -1;
        }
        return verbs.get(2);
      } else if(tokens[verbs.get(2)].hasPosTagStartingWith("VER:MOD:")
          && isSpecialPair(tokens, verbs.get(0), verbs.get(1))) {
        if(isSeparatorOrInf(tokens, verbs.get(2) + 1)) {
          return -1;
        }
        return verbs.get(2);
      }
      if(isPerfect(tokens, verbs.get(0), verbs.get(1), verbs.get(2))) {
        if(isSeparatorOrInf(tokens, verbs.get(2) + 1)) {
          return -1;
        }
        return verbs.get(1);
      }
    }
    return verbs.get(0);
  }

  /**
   * gives back position where a comma is missed
   * PRP has to be treated separately
   */
  private static int missedCommaBehind(AnalyzedTokenReadings[] tokens, int inFront, int start, int end) {
    for (int i = start; i < end; i++) {
      if(isPronoun(tokens, i)) {
        List<Integer> verbs = verbPos(tokens, i, end);
        if(verbs.size() > 0) {
          String gender = getGender(tokens[i]);
          if(gender != null && !isAnyVerb(tokens, i + 1)
              && matchesGender(gender, tokens, inFront, i - 1) && !isArticle(gender, tokens, i, verbs.get(verbs.size() - 1))) {
            return getCommaBehind(tokens, verbs, i, end);
          }
        }
      }
    }
    return -1;
  }

  /**
   * checks if personal pronoun is singular or plural
   * gives back null if token is not a  personal pronoun
   */
  private static String getSinOrPluOfPro(AnalyzedTokenReadings token) {
    if(!token.hasPartialPosTag("PRO:PER:") && !token.hasPosTagStartingWith("IND:")) {
      return null;
    }
    String ret = "";
    int nMatches = 0;
    if(token.matchesPosTagRegex(".*:SIN.*")) {
      ret += "SIN";
      nMatches++;
    }
    if(token.matchesPosTagRegex(".*:PLU.*")) {
      if(!ret.isEmpty()) {
        ret += "|";
      }
      ret += "PLU";
      nMatches++;
    }
    if(nMatches > 1) {
      ret = "(" + ret + ")";
    }
    return ret;
  }

  private static boolean isVerbProPair(AnalyzedTokenReadings[] tokens, int n) {
    String sinOrPlu = getSinOrPluOfPro(tokens[n+1]);
    if(sinOrPlu == null) {
      return false;
    }
    return tokens[n].matchesPosTagRegex("VER:.*" + sinOrPlu + ".*");
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    if(tokens.length <= 1) {
      return toRuleMatchArray(ruleMatches);
    }
    int subStart = 1;
    if(isSeparator(tokens[subStart].getToken())) {
      subStart++;
    }
    if(behind) {
      int subInFront = subStart;
      subStart = nextSeparator(tokens, subInFront) + 1;
      while (subStart < tokens.length) {
        int subEnd = nextSeparator(tokens, subStart);
        int lastVerb = hasPotentialSubclause(tokens, subStart, subEnd);
        if(lastVerb > 0) {
          int nToken = missedCommaBehind(tokens, subInFront, subStart, subEnd);
          if( nToken > 0) {
            if(isVerbProPair(tokens, nToken)) {
              RuleMatch match = new RuleMatch(this, sentence, tokens[nToken - 1].getStartPos(), tokens[nToken + 1].getEndPos(),
                  "Sollten Sie hier ein Komma einfügen oder zwei?");
              List<String>suggestedReplacements = new ArrayList<>();
              suggestedReplacements.add(tokens[nToken - 1].getToken() + ", "
                                  + tokens[nToken].getToken() + " " + tokens[nToken + 1].getToken() + ",");
              suggestedReplacements.add(tokens[nToken - 1].getToken() + " "
                                  + tokens[nToken].getToken() + " " + tokens[nToken + 1].getToken() + ",");
              suggestedReplacements.add(tokens[nToken - 1].getToken() + " "
                                  + tokens[nToken].getToken() + ", " + tokens[nToken + 1].getToken());
              match.setSuggestedReplacements(suggestedReplacements);
              ruleMatches.add(match);
            } else {
              RuleMatch match = new RuleMatch(this, sentence, tokens[nToken].getStartPos(), tokens[nToken + 1].getEndPos(),
                  "Sollten Sie hier ein Komma einfügen?");
              match.setSuggestedReplacement(tokens[nToken].getToken() + ", " + tokens[nToken + 1].getToken());
              ruleMatches.add(match);
            }
          }
        }
        subInFront = subStart;
        subStart = subEnd + 1;
      }
    } else {
      while (subStart < tokens.length) {
        int subEnd = nextSeparator(tokens, subStart);
        int lastVerb = hasPotentialSubclause(tokens, subStart, subEnd);
        if(lastVerb > 0) {
          int nToken = missedCommaInFront(tokens, subStart, subEnd, lastVerb);
          if( nToken > 0) {
            int startToken = nToken - (isPrp(tokens[nToken - 1]) ? 2 : 1);
            RuleMatch match = new RuleMatch(this, sentence, tokens[startToken].getStartPos(), tokens[nToken].getEndPos(),
                "Sollten Sie hier ein Komma einfügen?");
            if(nToken - startToken > 1) {
              match.setSuggestedReplacement(tokens[startToken].getToken() + ", " + tokens[nToken - 1].getToken() + " " + tokens[nToken].getToken());
            } else {
              match.setSuggestedReplacement(tokens[startToken].getToken() + ", " + tokens[nToken].getToken());
            }
            ruleMatches.add(match);
          }
        }
        subStart = subEnd + 1;
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public List<DisambiguationPatternRule> getAntiPatterns() {
    return ANTI_PATTERNS;
  }
}
