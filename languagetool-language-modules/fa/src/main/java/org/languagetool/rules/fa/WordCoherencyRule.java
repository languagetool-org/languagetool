/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.fa;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.Category;
import org.languagetool.rules.Example;
import org.languagetool.rules.RuleMatch;

/**
 * A rule that matches words for which two different spellings are used
 * throughout the document. Currently only implemented for German. Loads
 * the relevant words from <code>rules/fa/coherency.txt</code>.
 * 
 * <p>Note that this should not be used for language variations like
 * American English vs. British English or German "alte Rechtschreibung"
 * vs. "neue Rechtschreibung" -- that's the task of a spell checker.
 * 
 * @since 2.7
 */
public class WordCoherencyRule extends PersianRule {

  private static final String FILE_NAME = "/fa/coherency.txt";
  private static final String FILE_ENCODING = "utf-8";
  
  private final Map<String, String> relevantWords;        // e.g. "aufwendig -> aufwändig"
  private Map<String, RuleMatch> shouldNotAppearWord = new HashMap<>();  // e.g. aufwändig -> RuleMatch of aufwendig

  public WordCoherencyRule(ResourceBundle messages) throws IOException {
    if (messages != null) {
      super.setCategory(new Category(messages.getString("category_misc")));
    }
    relevantWords = loadWords(JLanguageTool.getDataBroker().getFromRulesDirAsStream(FILE_NAME));
    //addExamplePair(Example.wrong("من در <marker>اطاق</marker> تو را دیدم."),
    //               Example.fixed("من در <marker>اتاق</marker> تو را دیدم."));
  }
  
  @Override
  public String getId() {
    return "FA_WORD_COHERENCY";
  }

  @Override
  public String getDescription() {
    return "چند املا برای یک کلمه که یکی از آنها اولویت بیشتری دارد";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    int pos = 0;
    for (AnalyzedTokenReadings tmpToken : tokens) {
      String token = tmpToken.getToken();
      final String origToken = token;
      final List<AnalyzedToken> readings = tmpToken.getReadings();
      // TODO: in theory we need to care about the other readings, too (affects e.g. German "Schenke" as a noun):
      if (readings != null && readings.size() > 0) {
        final String baseform = readings.get(0).getLemma();
        if (baseform != null) {
          token = baseform;
        }
      }
      if (shouldNotAppearWord.containsKey(token)) {
        final RuleMatch otherMatch = shouldNotAppearWord.get(token);
        final String otherSpelling = otherMatch.getMessage();
        final String msg = "'" + token + "' و '" + otherSpelling +
                "' نباید در یک جا استفاده شوند";
        final RuleMatch ruleMatch = new RuleMatch(this, pos, pos + origToken.length(), msg);
        ruleMatch.setSuggestedReplacement(otherSpelling);
        ruleMatches.add(ruleMatch);
      } else if (relevantWords.containsKey(token)) {
        final String shouldNotAppear = relevantWords.get(token);
        // only used to display this spelling variation if the other one really occurs:
        final RuleMatch potentialRuleMatch = new RuleMatch(this, pos, pos + origToken.length(), token);
        shouldNotAppearWord.put(shouldNotAppear, potentialRuleMatch);
      }
      pos += tmpToken.getToken().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

  private Map<String, String> loadWords(InputStream stream) throws IOException {
    final Map<String, String> map = new HashMap<>();
    try (Scanner scanner = new Scanner(stream, FILE_ENCODING)) {
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine().trim();
        if (line.length() < 1) {
          continue;
        }
        if (line.charAt(0) == '#') {      // ignore comments
          continue;
        }
        final String[] parts = line.split(";");
        if (parts.length != 2) {
          throw new IOException("خطای ساختاری در پروندهٔ  " + JLanguageTool.getDataBroker().getFromRulesDirAsUrl(FILE_NAME) + "، خط: " + line);
        }
        map.put(parts[0], parts[1]);
        map.put(parts[1], parts[0]);
      }
    }
    return map;
  }
  
  @Override
  public void reset() {
    shouldNotAppearWord = new HashMap<>();
  }

}