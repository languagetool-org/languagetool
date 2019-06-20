/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Fred Kruse
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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.LinguServices;
import org.languagetool.UserConfig;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.Tools;

import morfologik.speller.Speller;
import morfologik.stemming.Dictionary;

/**
 * Checks the compound spelling of infinitive clause (Erweiterter Infinitiv mit zu)
 *
 * @author Fred Kruse
 * @since 4.4
 */
public class CompoundInfinitivRule extends Rule {
  
  private static Dictionary dict;
  
  private final LinguServices linguServices;
  private final Speller speller;
  private final Language lang;

  public CompoundInfinitivRule(ResourceBundle messages, Language lang, UserConfig userConfig) throws IOException {
    super.setCategory(Categories.COMPOUNDING.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("Er überprüfte die Rechnungen noch einmal, um ganz <marker>sicher zu gehen</marker>."),
                   Example.fixed("Er überprüfte die Rechnungen noch einmal, um ganz <marker>sicherzugehen</marker>."));
    this.lang = lang;
    if (userConfig != null) {
      linguServices = userConfig.getLinguServices();
    } else {
      linguServices = null;
    }
    if (linguServices == null) {
      speller = new Speller(getDictionary());
    } else {
      speller = null;
    }
    setUrl(Tools.getUrl("https://www.duden.de/sprachwissen/sprachratgeber/Infinitiv-mit-zu"));
  }

  @NotNull
  private static Dictionary getDictionary() throws IOException {
    if (dict == null) {
      // Dictionary is thread-safe, so we can re-use it (https://github.com/morfologik/morfologik-stemming/issues/69)
      dict = Dictionary.read(JLanguageTool.getDataBroker().getFromResourceDirAsUrl("/de/hunspell/de_DE.dict"));
    }
    return dict;
  }

  @Override
  public String getId() {
    return "COMPOUND_INFINITIV_RULE";
  }

  @Override
  public String getDescription() {
    return "Erweiterter Infinitiv mit zu (Zusammenschreibung)";
  }

  private static boolean isInfinitiv(AnalyzedTokenReadings token) {
    return token.hasPosTagStartingWith("VER:INF");
  }

  private boolean isMisspelled(String word) {
    if (linguServices == null && speller != null) {
      return speller.isMisspelled(word);
    } else if (linguServices != null) {
      return !linguServices.isCorrectSpell(word, lang);
    }
    return false;
  }

  private boolean isRelevant(AnalyzedTokenReadings token) {
    return token.hasPosTag("ZUS") && !"um".equalsIgnoreCase(token.getToken());
  }

  private String getLemma(AnalyzedTokenReadings token) {
    if (token != null) {
      List<AnalyzedToken> readings = token.getReadings();
      for (AnalyzedToken reading : readings) {
        String lemma = reading.getLemma();
        if (lemma != null) {
          return lemma;
        }
      }
    }
    return null;
  }
  
  private boolean isException(AnalyzedTokenReadings[] tokens, int n) {
    if (tokens[n - 2].hasPosTagStartingWith("VER")) {
      return true;
    }
    if ("sagen".equals(tokens[n + 1].getToken()) &&
            ("weiter".equals(tokens[n - 1].getToken()) || "dazu".equals(tokens[n - 1].getToken()))) {
      return true;
    }
    if (("tragen".equals(tokens[n + 1].getToken()) || "machen".equals(tokens[n + 1].getToken()))
            && "davon".equals(tokens[n - 1].getToken())) {
      return true;
    }
    if ("geben".equals(tokens[n + 1].getToken()) && "daran".equals(tokens[n - 1].getToken())) {
      return true;
    }
    if ("gehen".equals(tokens[n + 1].getToken()) && "ab".equals(tokens[n - 1].getToken())) {
      return true;
    }
    if ("errichten".equals(tokens[n + 1].getToken()) && "wieder".equals(tokens[n - 1].getToken())) {
      return true;
    }
    String verb = null;
    for (int i = n - 2; i > 0 && !isPunctuation(tokens[i].getToken()) && verb == null; i--) {
      if (tokens[i].hasPosTagStartingWith("VER:IMP")) {
        verb = StringUtils.lowerCase(getLemma(tokens[i]));
      } else if (tokens[i].hasPosTagStartingWith("VER")) {
        verb = tokens[i].getToken().toLowerCase();
      } else if ("Fang".equals(tokens[i].getToken())) {
        verb = "fangen";
      }
      if (verb != null) {
        if (!isMisspelled(tokens[n - 1].getToken() + verb)) {
          return true;
        } else {
          break;
        }
      }
    }
    if ("aus".equals(tokens[n - 1].getToken()) || "an".equals(tokens[n - 1].getToken())) {
      for (int i = n - 2; i > 0 && !isPunctuation(tokens[i].getToken()); i--) {
        if ("von".equals(tokens[i].getToken()) || "vom".equals(tokens[i].getToken())) {
          return true;
        }
      }
    }
    if ("her".equals(tokens[n - 1].getToken())) {
      for (int i = n - 2; i > 0 && !isPunctuation(tokens[i].getToken()); i--) {
        if ("vor".equals(tokens[i].getToken())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    for (int i = 2; i < tokens.length - 1; i++) {
      if ("zu".equals(tokens[i].getToken())
        && isInfinitiv(tokens[i + 1])
        && isRelevant(tokens[i - 1])
        && !isException(tokens, i)
        && !isMisspelled(tokens[i - 1].getToken() + tokens[i + 1].getToken())) {
        String msg = "Wenn der erweiterte Infinitv von dem Verb '" + tokens[i - 1].getToken() + tokens[i + 1].getToken()
                   + "' abgeleitet ist, muss er zusammengeschrieben werden";
        RuleMatch ruleMatch = new RuleMatch(this, sentence, tokens[i - 1].getStartPos(), tokens[i + 1].getEndPos(), msg);
        List<String> suggestions = new ArrayList<>();
        suggestions.add(tokens[i - 1].getToken() + tokens[i].getToken() + tokens[i + 1].getToken());
        ruleMatch.setSuggestedReplacements(suggestions);
        ruleMatches.add(ruleMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private boolean isPunctuation(String word) {
    return word != null
           && word.length() == 1
           && StringUtils.equalsAny(word, ".", "?", "!", "…", ":", ";", ",", "(", ")", "[", "]");
  }
}
