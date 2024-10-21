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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.AbstractStyleRepeatedWordRule;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleOption;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;
import org.languagetool.tools.StringTools;

import morfologik.speller.Speller;

/**
 * A rule checks the appearance of same words in a sentence or in two consecutive sentences.
 * Only substantive, verbs and adjectives are checked.
 * This rule detects no grammar error but a stylistic problem (default off)
 * @author Fred Kruse
 */
public class GermanStyleRepeatedWordRule extends AbstractStyleRepeatedWordRule {
  
  private static final String SYNONYMS_URL = "https://www.openthesaurus.de/synonyme/";
  private static final Pattern LETTERS = Pattern.compile("^[A-Za-zÄÖÜäöüß]+$");

  private Speller speller = null;
  private boolean testCompoundWords = false;

  public GermanStyleRepeatedWordRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
    super(messages, lang, userConfig);
    super.setCategory(Categories.STYLE.getCategory(messages));
    addExamplePair(Example.wrong("Ich gehe zum Supermarkt, danach <marker>gehe</marker> ich nach Hause."),
                   Example.fixed("Ich gehe zum Supermarkt, danach nach Hause."));
    if (userConfig != null) {
      Object[] cf = userConfig.getConfigValueByID(getId());
      if (cf != null && cf.length > 1) {
        testCompoundWords = (boolean) cf[1];
      }
    }
  }

  @Override
  public String getId() {
    return "STYLE_REPEATED_WORD_RULE_DE";
  }

  @Override
  public String getDescription() {
    return "Wiederholte Worte in aufeinanderfolgenden Sätzen";
  }
  
  @Override
  protected String messageSameSentence() {
    return "Mögliches Stilproblem: Das Wort wird noch einmal im selben Satz verwendet.";
  }
  
  @Override
  protected String messageSentenceBefore() {
    return "Mögliches Stilproblem: Das Wort wird bereits in einem vorhergehenden Satz verwendet.";
  }
  
  @Override
  protected String messageSentenceAfter() {
    return "Mögliches Stilproblem: Das Wort wird auch in einem nachfolgenden Satz verwendet.";
  }

  /**
   *  give the user the possibility to configure the function
   */
  @Override
  public RuleOption[] getRuleOptions() {
    RuleOption[] ruleOptions = { 
        new RuleOption(maxDistanceOfSentences, messages.getString("guiStyleRepeatedWordText"), 0, 5),
        new RuleOption(testCompoundWords, "Auch zusammengesetzte Wörter prüfen")
    };
    return ruleOptions;
  }

  /**
   * is a correct spelled word
   */
  private boolean isCorrectSpell(String word) {
    word = StringTools.uppercaseFirstChar(word);
    if (speller == null) {
      // speller can not initialized by constructor because of temporary initialization of LanguageTool in other rules,
      // which leads to problems in LO/OO extension
      speller = new Speller(MorfologikSpeller.getDictionaryWithCaching("/de/hunspell/de_DE.dict"));
    }
    if (speller != null) {
      return !speller.isMisspelled(word);
    } else if (linguServices != null) {
      return linguServices.isCorrectSpell(word, lang);
    }
    throw new IllegalStateException("LinguServices or Speller must be not null to check spelling in CompoundInfinitivRule");
  }

  /**
   * Is a unknown word (has only letters and no PosTag) 
   */
  private static boolean isUnknownWord(AnalyzedTokenReadings token) {
    return token.isPosTagUnknown() && token.getToken().length() > 2 && LETTERS.matcher(token.getToken()).matches();
  }

  /**
   * Only substantive, names, verbs and adjectives are checked
   */
  protected boolean isTokenToCheck(AnalyzedTokenReadings token) {
    return ((token.matchesPosTagRegex("(SUB|EIG|VER|ADJ):.*") 
        && !token.matchesPosTagRegex("(PRO|A(RT|DV)|VER:(AUX|MOD)):.*")
        || isUnknownWord(token))
        && !StringUtils.equalsAny(token.getToken(), "sicher", "weit", "Sie", "Ich", "Euch", "Eure", "Der", "all"));
  }

  /**
   * Pairs of substantive are excluded like "Arm in Arm", "Seite an Seite", etc.
   */
  protected boolean isTokenPair(AnalyzedTokenReadings[] tokens, int n, boolean before) {
    if (before) {
      if (n > 2 && n < tokens.length &&
          (tokens[n-2].hasPosTagStartingWith("SUB") && tokens[n-1].hasPosTagStartingWith("PRP")
              && tokens[n].hasPosTagStartingWith("SUB"))
          || (tokens[n-2].getToken().equals("hart") && tokens[n-1].getToken().equals("auf") && tokens[n].getToken().equals("hart"))
          || (tokens[n-2].getToken().equals("dicht") && tokens[n-1].getToken().equals("an") && tokens[n].getToken().equals("dicht"))
         ) {
        return true;
      }
    } else {
      if (n > 0 && n < tokens.length - 2 &&
          (tokens[n].hasPosTagStartingWith("SUB") && tokens[n+1].hasPosTagStartingWith("PRP")
              && tokens[n+2].hasPosTagStartingWith("SUB"))
          || (tokens[n].getToken().equals("hart") && tokens[n+1].getToken().equals("auf") && tokens[n+2].getToken().equals("hart"))
          || (tokens[n].getToken().equals("dicht") && tokens[n+1].getToken().equals("an") && tokens[n+2].getToken().equals("dicht"))
         ) {
        return true;
      }
    }
    return false;
  }

  private boolean isSecondPartofWord(String testTokenText, String tokenText) {
    if (testTokenText.length() - tokenText.length() < 3) {
      return false;
    }
    String lowerTokenText = StringTools.lowercaseFirstChar(tokenText);
    if (lowerTokenText.equals("frei")
        || (lowerTokenText.equals("alten") && testTokenText.endsWith("halten"))
        ) {
      return false;
    }
    if (StringTools.lowercaseFirstChar(testTokenText).startsWith(lowerTokenText)) {
      String word = testTokenText.substring(tokenText.length());
      if (isCorrectSpell(word)) {
        return true;
      } else if(word.startsWith("s")) {
        word = word.substring(1);
        if (isCorrectSpell(word)) {
          return true;
        }
      }
      return false;
    } else if (testTokenText.endsWith(lowerTokenText)) {
      String word = testTokenText.substring(0, testTokenText.length() - tokenText.length());
      if (isCorrectSpell(word)) {
        return true;
      } else if(word.endsWith("s")) {
        word = word.substring(word.length() - 1);
        if (isCorrectSpell(word)) {
          return true;
        }
      }
      return false;
    }
    return false;
  }
  
  @Override
  protected boolean isPartOfWord(String testTokenText, String tokenText) {
    if (!testCompoundWords || testTokenText.length() < 3 || tokenText.length() < 3) {
      return false;
    }
    if (testTokenText.length() > tokenText.length()) {
      return isSecondPartofWord(testTokenText, tokenText);
    } else {
      return isSecondPartofWord(tokenText, testTokenText);
    }
  }

  /* 
   *  true if is an exception of token pair
   *  note: method is called after two tokens are tested to share the same lemma
   */
  @Override
  protected boolean isExceptionPair(AnalyzedTokenReadings token1, AnalyzedTokenReadings token2) {
    if ((token1.hasLemma("nah") && token1.hasLemma("nächst") && !token2.hasLemma("nächst")) || 
        (token2.hasLemma("nah") && token2.hasLemma("nächst") && !token1.hasLemma("nächst"))) {
      return true;
    } else {
      if(token1.hasLemma("gut") && 
          ((token1.getToken().startsWith("gut") && !token2.getToken().startsWith("gut")) || 
           (token2.getToken().startsWith("gut") && !token1.getToken().startsWith("gut")))) {
        return true;
      }
    }
    return false;
  }

  /* 
   *  set an URL to the German openThesaurus
   */
  @Override
  protected URL setURL(AnalyzedTokenReadings token) throws MalformedURLException {
    if (token != null) {
      List<AnalyzedToken> readings = token.getReadings();
      List<String> lemmas = new ArrayList<>();
      for (AnalyzedToken reading : readings) {
        String lemma = reading.getLemma();
        if (lemma != null) {
          lemmas.add(lemma);
        }
      }
      if (lemmas.size() == 1) {
        return new URL(SYNONYMS_URL + lemmas.get(0));
      }
      return new URL(SYNONYMS_URL + token.getToken());
    }
    return null;
  }

}
