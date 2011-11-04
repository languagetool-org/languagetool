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
package de.danielnaber.languagetool.rules.de;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * A rule that matches words for which two different spellings are used
 * throughout the document. Currently only implemented for German. Loads
 * the relevant word from <code>rules/de/coherency.txt</code>.
 * 
 * <p>Note that this should not be used for language variations like
 * American English vs. British English or German "alte Rechtschreibung"
 * vs. "neue Rechtschreibung" -- that's the task of a spell checker.
 * 
 * @author Daniel Naber
 */
public class WordCoherencyRule extends GermanRule {

  private static final String FILE_NAME = "/de/coherency.txt";
  private static final String FILE_ENCODING = "utf-8";
  
  private final Map<String, String> relevantWords;        // e.g. "aufwendig -> aufwändig"
  private Map<String, RuleMatch> shouldNotAppearWord = new HashMap<String, RuleMatch>();  // e.g. aufwändig -> RuleMatch of aufwendig

  private final GermanLemmatizer germanLemmatizer;
  
  public WordCoherencyRule(ResourceBundle messages) throws IOException {
    if (messages != null)
      super.setCategory(new Category(messages.getString("category_misc")));
    relevantWords = loadWords(JLanguageTool.getDataBroker().getFromRulesDirAsStream(FILE_NAME)); 
    germanLemmatizer = new GermanLemmatizer();
  }
  
  @Override
  public String getId() {
    return "DE_WORD_COHERENCY";
  }

  @Override
  public String getDescription() {
    return "Einheitliche Schreibweise für Wörter mit mehr als einer korrekten Schreibweise";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokens();
    int pos = 0;
    for (AnalyzedTokenReadings tmpToken : tokens) {
      //TODO: definitely should be changed
      //if the general lemmatizer is working
      //defaulting to the first element because the
      //general German lemmatizer is not (yet) there
      String token = tmpToken.getToken();
      if (tmpToken.isWhitespace()) {
        // ignore
      } else {
        final String origToken = token;
        final List<AnalyzedToken> readings = tmpToken.getReadings();
        // TODO: in theory we need to care about the other readings, too:
        if (readings != null && readings.size() > 0) {
          final String baseform = readings.get(0).getLemma();
          if (baseform != null) {
            token = baseform;
          } else {
            // not all words are known by the Tagger (esp. compounds), so use the
            // file lookup:
            final String manualLookup = germanLemmatizer.getBaseform(origToken);
            if (manualLookup != null)
              token = manualLookup;
          }
        }
        if (shouldNotAppearWord.containsKey(token)) {
          final RuleMatch otherMatch = shouldNotAppearWord.get(token);
          final String otherSpelling = otherMatch.getMessage();
          final String msg = "'" + token + "' und '" + otherSpelling +
                  "' sollten nicht gleichzeitig benutzt werden";
          final RuleMatch ruleMatch = new RuleMatch(this, pos, pos + origToken.length(), msg);
          ruleMatch.setSuggestedReplacement(otherSpelling);
          ruleMatches.add(ruleMatch);
        } else if (relevantWords.containsKey(token)) {
          final String shouldNotAppear = relevantWords.get(token);
          // only used to display this spelling variation if the other one really occurs:
          final RuleMatch potentialRuleMatch = new RuleMatch(this, pos, pos + origToken.length(), token);
          shouldNotAppearWord.put(shouldNotAppear, potentialRuleMatch);
        }
      }
      pos += tmpToken.getToken().length();
    }
    return toRuleMatchArray(ruleMatches);
  }

  private Map<String, String> loadWords(InputStream file) throws IOException {
    final Map<String, String> map = new HashMap<String, String>();
    InputStreamReader isr = null;
    BufferedReader br = null;
    try {
      isr = new InputStreamReader(file, FILE_ENCODING);
      br = new BufferedReader(isr);
      String line;
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.length() < 1) {
          continue;
        }
        if (line.charAt(0) == '#') {      // ignore comments
          continue;
        }
        final String[] parts = line.split(";");
        if (parts.length != 2) {
          throw new IOException("Format error in file " + JLanguageTool.getDataBroker().getFromRulesDirAsUrl(FILE_NAME) + ", line: " + line);
        }
        map.put(parts[0], parts[1]);
        map.put(parts[1], parts[0]);
      }
    } finally {
      if (br != null) br.close();
      if (isr != null) isr.close();
    }
    return map;
  }
  
  @Override
  public void reset() {
    shouldNotAppearWord = new HashMap<String, RuleMatch>();
  }

}
