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
package org.languagetool.rules.de;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.language.German;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Checks that a sentence contains at least one verb. Ignores very short sentences.
 * Note that this is off by default, as there are quite some "sentences"
 * without a verb, e.g. "Nix für ungut!", "Bis nächste Woche.", "Nicht schon wieder!",
 * "Und deine Schwester?", "Wie dumm von mir!", and book references like "Andreas Fecker: Fluglotsen."
 * @since 2.7
 */
public class MissingVerbRule extends Rule {

  private static final int MIN_TOKENS_FOR_ERROR = 5;

  private final PatternRule rule1;
  private final PatternRule rule2;
  private final Language language;

  public MissingVerbRule(ResourceBundle messages, German language) {
    this.language = language;
    rule1 = new PatternRule("internal", language, Arrays.asList(
            new PatternToken("Vielen", true, false, false),
            new PatternToken("Dank", true, false, false)), "", "", "");
    rule2 = new PatternRule("internal", language, Arrays.asList(
            new PatternToken("Herzlichen", true, false, false),
            new PatternToken("Glückwunsch", true, false, false)), "", "", "");
    super.setCategory(Categories.GRAMMAR.getCategory(messages));
    setDefaultOff();
    addExamplePair(Example.wrong("<marker>In diesem Satz kein Wort.</marker>"),
                   Example.fixed("In diesem Satz <marker>fehlt</marker> kein Wort."));
  }

  @Override
  public String getId() {
    return "MISSING_VERB";
  }

  @Override
  public String getDescription() {
    return "Satz ohne Verb";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    if (!isRealSentence(sentence) || isSpecialCase(sentence)) {
      return new RuleMatch[0];
    }
    boolean verbFound = false;
    AnalyzedTokenReadings lastToken = null;
    int i = 0;
    for (AnalyzedTokenReadings readings : sentence.getTokensWithoutWhitespace()) {
      if (readings.hasPosTagStartingWith("VER")
          || (!readings.isTagged() && !StringTools.isCapitalizedWord(readings.getToken()) // ignore unknown words to avoid false alarms
          || (i == 1 && verbAtSentenceStart(readings)))) {
        verbFound = true;
        break;
      }
      lastToken = readings;
      i++;
    }
    if (!verbFound && lastToken != null && sentence.getTokensWithoutWhitespace().length >= MIN_TOKENS_FOR_ERROR) {
      RuleMatch match = new RuleMatch(this, sentence, 0, lastToken.getStartPos() + lastToken.getToken().length(), "Dieser Satz scheint kein Verb zu enthalten");
      return new RuleMatch[]{ match };
    }
    return new RuleMatch[0];
  }

  // we want to ignore headlines, and these usually don't end with [.?!]
  private boolean isRealSentence(AnalyzedSentence sentence) {
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    if (tokens.length > 0) {
      AnalyzedTokenReadings lastToken = tokens[tokens.length - 1];
      return lastToken.hasPosTag("PKT") && StringUtils.equalsAny(lastToken.getToken(), ".", "?", "!");
    }
    return false;
  }

  private boolean isSpecialCase(AnalyzedSentence sentence) throws IOException {
    return rule1.match(sentence).length > 0 || rule2.match(sentence).length > 0;
  }

  private boolean verbAtSentenceStart(AnalyzedTokenReadings readings) throws IOException {
    // start of sentence is mis-tagged because of the uppercase first character, work around that:
    String lowercased = StringTools.lowercaseFirstChar(readings.getToken());
    List<AnalyzedTokenReadings> lcReadings = language.getTagger().tag(Collections.singletonList(lowercased));
    return lcReadings.size() > 0 && lcReadings.get(0).hasPosTagStartingWith("VER");
  }

}
