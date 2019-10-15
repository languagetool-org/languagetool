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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tools.StringTools;

/**
 * A rule that matches words which should not be used and suggests
 * correct ones instead. Loads the relevant words from
 * <code>rules/XX/replace.txt</code>, where XX is a code of the language.
 * 
 * @author Andriy Rysin
 */
public abstract class AbstractSimpleReplaceRule extends Rule {

  protected boolean ignoreTaggedWords = false;
  private boolean checkLemmas = true;

  protected abstract Map<String, List<String>> getWrongWords();

  protected static Map<String, List<String>> loadFromPath(String path) {
    return new SimpleReplaceDataLoader().loadWords(path);
  }

  /**
   * Indicates if the rule is case-sensitive. Default value is <code>true</code>.
   * 
   * @return true if the rule is case-sensitive, false otherwise.
   */
  public boolean isCaseSensitive() {
    return true;
  }

  /**
   * @return the locale used for case conversion when {@link #isCaseSensitive()}
   *         is set to <code>false</code>.
   */
  public Locale getLocale() {
    return Locale.getDefault();
  }

  /**
   * Skip words that are known in the POS tagging dictionary, assuming they
   * cannot be incorrect.
   * @since 2.3
   */
  public void setIgnoreTaggedWords() {
    ignoreTaggedWords = true;
  }

  public AbstractSimpleReplaceRule(ResourceBundle messages) {
    super.setCategory(Categories.MISC.getCategory(messages));
  }

  @Override
  public String getId() {
    return "SIMPLE_REPLACE";
  }

  @Override
  public String getDescription() {
    return "Checks for wrong words/phrases";
  }

  public String getMessage(String tokenStr, List<String> replacements) {
    return tokenStr + " is not valid. Use: "
        + String.join(", ", replacements) + ".";
  }

  public String getShort() {
    return "Wrong word";
  }

  private String cleanup(String word) {
    return isCaseSensitive() ? word : word.toLowerCase(getLocale()); 
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    for (AnalyzedTokenReadings tokenReadings : tokens) {
      // short for SENT_START
      if( JLanguageTool.SENTENCE_START_TAGNAME.equals(tokenReadings.getAnalyzedToken(0).getPOSTag()) ||
          tokenReadings.isImmunized() ||        //this rule is used mostly for spelling, so ignore both immunized
          tokenReadings.isIgnoredBySpeller() || //and speller-ignorable rules
          (ignoreTaggedWords && isTagged(tokenReadings))
      ) {
        continue;
      }
      List<RuleMatch> matchesForToken = findMatches(tokenReadings, sentence);
      ruleMatches.addAll( matchesForToken );
    }
    return toRuleMatchArray(ruleMatches);
  }

  protected List<RuleMatch> findMatches(AnalyzedTokenReadings tokenReadings, AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();

    String originalTokenStr = tokenReadings.getToken();
    String tokenString = cleanup(originalTokenStr);

    // try first with the original word, then with the all lower-case version
    List<String> possibleReplacements = getWrongWords().get(originalTokenStr);
    if (possibleReplacements == null) {
      possibleReplacements = getWrongWords().get(tokenString);
    }

    if (possibleReplacements == null && checkLemmas) {
      possibleReplacements = new ArrayList<>();

      List<String> lemmas = new ArrayList<>();
      for (AnalyzedToken analyzedToken : tokenReadings.getReadings()) {
        String lemma = analyzedToken.getLemma();
        if (lemma != null && getWrongWords().containsKey(lemma) && ! lemmas.contains(lemma) ) {
          lemmas.add(cleanup(lemma));
        }
      }

      for (String lemma: lemmas) {
        List<String> replacements = getWrongWords().get(lemma);
        if (replacements != null) {
          possibleReplacements.addAll(replacements);
        }
      }

      possibleReplacements = possibleReplacements.stream().distinct().collect(Collectors.toList());
    }

    if (possibleReplacements != null && possibleReplacements.size() > 0) {
      List<String> replacements = new ArrayList<>(possibleReplacements);
      replacements.remove(originalTokenStr);
      if (replacements.size() > 0) {
        RuleMatch potentialRuleMatch = createRuleMatch(tokenReadings, replacements, sentence);
        ruleMatches.add(potentialRuleMatch);
      }
    }
    
    return ruleMatches;
  }

  /**
   * This method allows to override which tags will mark token as tagged
   * @return returns true if token has valid tag
   */
  protected boolean isTagged(AnalyzedTokenReadings tokenReadings) {
    return tokenReadings.isTagged();
  }

  protected RuleMatch createRuleMatch(AnalyzedTokenReadings tokenReadings,
                                      List<String> replacements, AnalyzedSentence sentence) {
    String tokenString = tokenReadings.getToken();
    int pos = tokenReadings.getStartPos();

    RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, pos, pos
        + tokenString.length(), getMessage(tokenString, replacements), getShort());

    if (!isCaseSensitive() && StringTools.startsWithUppercase(tokenString)) {
      for (int i = 0; i < replacements.size(); i++) {
        replacements.set(i, StringTools.uppercaseFirstChar(replacements.get(i)));
      }
    }

    potentialRuleMatch.setSuggestedReplacements(replacements);

    return potentialRuleMatch;
  }

  /**
   * @since 2.5
   */
  public boolean isCheckLemmas() {
    return checkLemmas;
  }

  /**
   * Used to disable matching lemmas.
   * @since 2.5
   */
  public void setCheckLemmas(boolean checkLemmas) {
    this.checkLemmas = checkLemmas;
  }

}
