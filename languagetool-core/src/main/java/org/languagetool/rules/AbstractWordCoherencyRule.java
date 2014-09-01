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
package org.languagetool.rules;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * A rule that matches words for which two different spellings are used
 * throughout the document.
 * 
 * <p>Note that this should not be used for language variations like
 * American English vs. British English or German "alte Rechtschreibung"
 * vs. "neue Rechtschreibung" -- that's the task of a spell checker.
 * 
 * @author Daniel Naber
 * @since 2.7
 */
public abstract class AbstractWordCoherencyRule extends Rule {

  private static final String FILE_ENCODING = "utf-8";
  
  /**
   * Get a path to a UTF-8 encoded coherency file, e.g. {@code de/coherency.txt}. This will
   * be loaded with {@link org.languagetool.databroker.ResourceDataBroker#getFromRulesDirAsStream}.
   */
  protected abstract String getFilePath();

  /**
   * Get the message shown to the user if the rule matches.
   */
  protected abstract String getMessage(String word1, String word2);
  
  private final Map<String, String> relevantWords;        // e.g. "aufwendig -> aufwändig"
  private final Map<String, RuleMatch> shouldNotAppearWord = new HashMap<>();  // e.g. aufwändig -> RuleMatch of aufwendig

  public AbstractWordCoherencyRule(ResourceBundle messages) throws IOException {
    if (messages != null) {
      super.setCategory(new Category(messages.getString("category_misc")));
    }
    relevantWords = loadWords(JLanguageTool.getDataBroker().getFromRulesDirAsStream(getFilePath()));
  }
  
  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
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
        final String msg = getMessage(token, otherSpelling);
        final RuleMatch ruleMatch = new RuleMatch(this, tmpToken.getStartPos(), tmpToken.getStartPos() + origToken.length(), msg);
        ruleMatch.setSuggestedReplacement(otherSpelling);
        ruleMatches.add(ruleMatch);
      } else if (relevantWords.containsKey(token)) {
        final String shouldNotAppear = relevantWords.get(token);
        final RuleMatch potentialRuleMatch = new RuleMatch(this, tmpToken.getStartPos(), tmpToken.getStartPos() + origToken.length(), token);
        shouldNotAppearWord.put(shouldNotAppear, potentialRuleMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private Map<String, String> loadWords(InputStream stream) throws IOException {
    final Map<String, String> map = new HashMap<>();
    try (
      InputStreamReader reader = new InputStreamReader(stream, FILE_ENCODING);
      BufferedReader br = new BufferedReader(reader)
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.length() < 1 || line.charAt(0) == '#') {   // ignore comments
          continue;
        }
        final String[] parts = line.split(";");
        if (parts.length != 2) {
          throw new IOException("Format error in file " + JLanguageTool.getDataBroker().getFromRulesDirAsUrl(getFilePath()) + ", line: " + line);
        }
        map.put(parts[0], parts[1]);
        map.put(parts[1], parts[0]);
      }
    }
    return map;
  }
  
  @Override
  public void reset() {
    shouldNotAppearWord.clear();
  }

}
