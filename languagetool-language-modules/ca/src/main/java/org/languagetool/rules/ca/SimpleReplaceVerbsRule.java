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
package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.*;
import org.languagetool.synthesis.ca.CatalanSynthesizer;

import static org.languagetool.synthesis.ca.VerbSynthesizer.pVerb;

/**
 * A rule that matches incorrect verbs (including all inflected forms) and
 * suggests correct ones instead.
 * Loads the relevant words from <code>rules/ca/replace_verbs.txt</code>.
 *
 * @author Jaume Ortolà
 */
public class SimpleReplaceVerbsRule extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> wrongWords = loadFromPath("/ca/replace_verbs.txt");
  private static final Locale CA_LOCALE = new Locale("CA");
  private static final Map<String, String> argumentsMap = Map.of("actions", "None");
  private static final ChunkTag incorrectVerbChunk = new ChunkTag("_incorrect_verb_");

  @Override
  public Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  private final CatalanSynthesizer synth;

  public SimpleReplaceVerbsRule(final ResourceBundle messages, Language language) {
    super(messages, language);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    super.setLocQualityIssueType(ITSIssueType.Grammar);
    super.setIgnoreTaggedWords();
    synth = (CatalanSynthesizer) language.getSynthesizer();
    super.useSubRuleSpecificIds();
  }

  @Override
  public final String getId() {
    return "CA_SIMPLE_REPLACE_VERBS";
  }

  @Override
  public String getDescription() {
    return "Verb incorrecte: $match";
  }

  @Override
  public String getShort() {
    return "Verb incorrecte";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    return "Verb incorrecte.";
  }

  @Override
  public Locale getLocale() {
    return CA_LOCALE;
  }

  @Override
  public final RuleMatch[] match(final AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    for (int index = 1; index < tokens.length; index++) {
      if (!tokens[index].getChunkTags().contains(incorrectVerbChunk) || tokens[index].hasPosTagStartingWith("N")
        || tokens[index].hasPosTagStartingWith("A")) {
        continue;
      }
      // synthesize replacements
      AnalyzedToken at = tokens[index].readingWithTagRegex(pVerb);
      if (at == null) {
        continue;
      }
      List<String> replacementInfinitives = wrongWords.get(at.getLemma());
      if (replacementInfinitives == null) {
        continue;
      }
      RuleMatch potentialRuleMatch = createRuleMatch(tokens[index], replacementInfinitives, sentence, at.getLemma());
      RuleMatch finalMatch;
      AdjustVerbSuggestionsFilter filter = new AdjustVerbSuggestionsFilter();
      filter.setLanguage(getLanguage());
      try {
        finalMatch = filter.acceptRuleMatch(potentialRuleMatch, argumentsMap, 0, null, null);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      if (finalMatch != null) {
        ruleMatches.add(finalMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

}
