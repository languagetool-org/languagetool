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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.language.German;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.Element;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.tagging.Tagger;
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
public class MissingVerbRule extends GermanRule {

  private static final int MIN_TOKENS_FOR_ERROR = 5;

  private final Language german = new German();
  private final Tagger tagger = german.getTagger();
  private final PatternRule rule1 = new PatternRule("internal", german, Arrays.asList(
          new Element("Vielen", true, false, false),
          new Element("Dank", true, false, false)), "", "", "");
  private final PatternRule rule2 = new PatternRule("internal", german, Arrays.asList(
          new Element("Herzlichen", true, false, false),
          new Element("Glückwunsch", true, false, false)), "", "", "");

  public MissingVerbRule(ResourceBundle messages) {
    setDefaultOff();
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
    if (!isRealSentence(sentence)) {
      return new RuleMatch[0];
    }
    if (isSpecialCase(sentence)) {
      return new RuleMatch[0];
    }
    boolean verbFound = false;
    AnalyzedTokenReadings lastToken = null;
    int i = 0;
    for (AnalyzedTokenReadings readings : sentence.getTokensWithoutWhitespace()) {
      if (readings.hasPartialPosTag("VER") || (!readings.isTagged() && !StringTools.isCapitalizedWord(readings.getToken()))) {  // ignore unknown words to avoid false alarms
        verbFound = true;
        break;
      } else if (i == 1 && verbAtSentenceStart(readings)) {
        verbFound = true;
        break;
      }
      lastToken = readings;
      i++;
    }
    if (!verbFound && lastToken != null && i - 1 >= MIN_TOKENS_FOR_ERROR) {
      RuleMatch match = new RuleMatch(this, 0, lastToken.getStartPos() + lastToken.getToken().length(), "Dieser Satz scheint kein Verb zu enthalten");
      return new RuleMatch[]{ match };
    }
    return new RuleMatch[0];
  }

  // we want to ignore headlines, and these usually don't end with [.?!]
  private boolean isRealSentence(AnalyzedSentence sentence) {
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    if (tokens.length > 0) {
      AnalyzedTokenReadings lastToken = tokens[tokens.length - 1];
      String lastTokenStr = lastToken.getToken();
      if (lastTokenStr.equals(".") || lastTokenStr.equals("?") || lastTokenStr.equals("!")) {
        return true;
      }
    }
    return false;
  }

  private boolean isSpecialCase(AnalyzedSentence sentence) throws IOException {
    return rule1.match(sentence).length > 0 || rule2.match(sentence).length > 0;
  }

  private boolean verbAtSentenceStart(AnalyzedTokenReadings readings) throws IOException {
    // start of sentence is mis-tagged because of the uppercase first character, work around that:
    String lowercased = StringTools.lowercaseFirstChar(readings.getToken());
    List<AnalyzedTokenReadings> lcReadings = tagger.tag(Collections.singletonList(lowercased));
    if (lcReadings.size() > 0 && lcReadings.get(0).hasPartialPosTag("VER")) {
      return true;
    }
    // our dictionary doesn't know some imperative forms like "erzähl", but it knows "erzähle", so let's try that:
    if (!lowercased.endsWith("e")) {
      List<AnalyzedTokenReadings> lcImperativeReadings = tagger.tag(Collections.singletonList(lowercased + "e"));
      if (lcImperativeReadings.size() > 0 && lcImperativeReadings.get(0).hasPartialPosTag("VER")) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void reset() {
  }
}
