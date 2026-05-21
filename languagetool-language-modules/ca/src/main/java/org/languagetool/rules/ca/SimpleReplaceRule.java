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

import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.rules.AbstractSimpleReplaceRule;
import org.languagetool.rules.Categories;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.*;

/**
 * A rule that matches words which should not be used and suggests correct ones
 * instead.
 * 
 * Catalan implementations. Loads the relevant words from
 * <code>rules/ca/replace.txt</code>.
 * 
 * @author Jaume Ortolà
 */
public class SimpleReplaceRule extends AbstractSimpleReplaceRule {

  private static final Map<String, List<String>> wrongWords = loadFromPath("/ca/replace.txt", "/ca/replace_custom.txt");
  private static final Locale CA_LOCALE = new Locale("CA");

  @Override
  public Map<String, List<String>> getWrongWords() {
    return wrongWords;
  }

  public SimpleReplaceRule(ResourceBundle messages, Language language) throws IOException {
    super(messages, language);
    super.setCategory(Categories.TYPOS.getCategory(messages));
    super.setLocQualityIssueType(ITSIssueType.Grammar);
    this.setIgnoreTaggedWords();
    this.setCheckLemmas(false);
    super.useSubRuleSpecificIds();
  }

  @Override
  public final String getId() {
    return "CA_SIMPLE_REPLACE_SIMPLE";
  }

  @Override
  public String getDescription() {
    return "Paraula incorrecta: $match";
  }

  @Override
  public String getShort() {
    return "Paraula incorrecta";
  }

  @Override
  public String getMessage(String tokenStr, List<String> replacements) {
    if (replacements.size() > 0) {
      return "¿Volíeu dir «" + replacements.get(0) + "»?";
    }
    return getShort();
  }

  private static final Map<String, String> argumentsMap = Map.of("lemmaSelect", "[NA].*");

  private final Set<String> exceptions = Set.of("perquè", "què", "per què");

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    RuleMatch[] potentialMatches = super.match(sentence);
    ConvertToGenderAndNumberFilter filter = new ConvertToGenderAndNumberFilter();
    filter.setLanguage(getLanguage());
    List<RuleMatch> ruleMatches = new ArrayList<>();
    // Adjust determinants and adjectives
    for (RuleMatch potentialRuleMatch : potentialMatches) {
      if (potentialRuleMatch.getSuggestedReplacements().stream()
        .map(String::toLowerCase)
        .anyMatch(exceptions::contains)) {
        ruleMatches.add(potentialRuleMatch);
        continue;
      }
      RuleMatch finalMatch = filter.acceptRuleMatch(potentialRuleMatch, argumentsMap, 0, null, null);
      if (finalMatch != null) {
        ruleMatches.add(finalMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public boolean isCaseSensitive() {
    return false;
  }

  @Override
  public Locale getLocale() {
    return CA_LOCALE;
  }

}
