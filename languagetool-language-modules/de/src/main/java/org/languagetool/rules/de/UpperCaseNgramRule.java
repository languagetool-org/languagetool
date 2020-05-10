/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;
import org.languagetool.rules.ngrams.Probability;

import java.util.*;

import static org.languagetool.tools.StringTools.*;

/**
 * Finds some(!) words written uppercase that should be spelled lowercase and vice versa.
 * @since 5.0
 */
public class UpperCaseNgramRule extends Rule {

  private static final int THRESHOLD = 50;
  private static final Set<String> relevantWords = new HashSet<>(Arrays.asList(
    "tage", "tagen",
    "Tage", "Tagen"
  ));

  private final LanguageModel lm;

  public UpperCaseNgramRule(ResourceBundle messages, LanguageModel lm, Language lang) {
    super(messages);
    /*
    TODO: potential extension - too many false alarms for now...
    String path = "/de/ambiguous_case.txt";
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(path);
    for (String line : lines) {
      if (line.isEmpty() || line.startsWith("#")) {
        continue;
      }
      if (startsWithLowercase(line)) {
        throw new RuntimeException("Only entries with uppercase first chars expected in " + path + ": " + line);
      }
      relevantWords.add(line);
      relevantWords.add(lowercaseFirstChar(line));
    }*/
    super.setCategory(Categories.CASING.getCategory(messages));
    this.lm = Objects.requireNonNull(lm);
    setDefaultTempOff();  // TODO
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("Die Suche endete nach 15 <marker>tagen</marker>."),
                   Example.fixed("Die Suche endete nach 15 <marker>Tagen</marker>."));
  }

  @Override
  public final String getId() {
    return "DE_UPPER_CASE_NGRAM";
  }

  @Override
  public String getDescription() {
    return "Prüft Wörter, ob sie fälschlich groß- oder fälschlich kleingeschrieben sind";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> matches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
    for (int i = 1; i < tokens.length; i++) {
      AnalyzedTokenReadings token = tokens[i];
      String tokenStr = token.getToken();
      if (i + 1 < tokens.length && relevantWords.contains(tokenStr) && !isAllUppercase(tokenStr)) {
        String ucToken = uppercaseFirstChar(tokenStr);
        String lcToken = lowercaseFirstChar(tokenStr);
        List<String> ucList = Arrays.asList(tokens[i - 1].getToken(), ucToken, tokens[i + 1].getToken());
        List<String> lcList = Arrays.asList(tokens[i - 1].getToken(), lcToken, tokens[i + 1].getToken());
        Probability ucProb = lm.getPseudoProbability(ucList);
        Probability lcProb = lm.getPseudoProbability(lcList);
        if (startsWithUppercase(tokenStr)) {
          double ratio = lcProb.getProb() / ucProb.getProb();
          if (ratio > THRESHOLD) {
            String msg = "Meinten Sie das Verb '" + lcToken + "'? Nur Nomen und Eigennamen werden großgeschrieben.";
            RuleMatch match = new RuleMatch(this, sentence, token.getStartPos(), token.getEndPos(), msg);
            match.setSuggestedReplacement(lcToken);
            matches.add(match);
          }
        } else {
          double ratio = ucProb.getProb() / lcProb.getProb();
          if (ratio > THRESHOLD) {
            String msg = "Meinten Sie das Nomen '" + ucToken + "'? Nomen und Eigennamen werden großgeschrieben.";
            RuleMatch match = new RuleMatch(this, sentence, token.getStartPos(), token.getEndPos(), msg);
            match.setSuggestedReplacement(ucToken);
            matches.add(match);
          }
        }
      }
    }
    return toRuleMatchArray(matches);
  }

}
