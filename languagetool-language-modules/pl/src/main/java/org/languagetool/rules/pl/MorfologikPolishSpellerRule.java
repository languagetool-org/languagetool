/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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
package org.languagetool.rules.pl;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.spelling.morfologik.MorfologikSpellerRule;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

public final class MorfologikPolishSpellerRule extends MorfologikSpellerRule {

  private static final String RESOURCE_FILENAME = "/pl/hunspell/pl_PL.dict";

  private static final Pattern POLISH_TOKENIZING_CHARS = Pattern.compile("(?:[Qq]uasi|[Nn]iby)-");

    /**
     * The set of prefixes that are not allowed to be split in the suggestions.
     */
    private static final Set<String> prefixes;

    //Polish prefixes that should never be used to
    //split parts of words
    static {
        final Set<String> tempSet = new HashSet<>();
        tempSet.add("arcy");  tempSet.add("neo");
        tempSet.add("pre");   tempSet.add("anty");
        tempSet.add("eks");   tempSet.add("bez");
        tempSet.add("beze");  tempSet.add("ekstra");
        tempSet.add("hiper"); tempSet.add("infra");
        tempSet.add("kontr"); tempSet.add("maksi");
        tempSet.add("midi");  tempSet.add("między");
        tempSet.add("mini");  tempSet.add("nad");
        tempSet.add("nade");  tempSet.add("około");
        tempSet.add("ponad"); tempSet.add("post");
        tempSet.add("pro");   tempSet.add("przeciw");
        tempSet.add("pseudo"); tempSet.add("super");
        tempSet.add("śród");  tempSet.add("ultra");
        tempSet.add("wice");  tempSet.add("wokół");
        tempSet.add("wokoło");
        prefixes = Collections.unmodifiableSet(tempSet);
    }

    /**
   * non-word suffixes that should not be suggested (only morphological endings, never after a space)
   */
    private static final Set<String> bannedSuffixes;

    static {
      final Set<String> tempSet = new HashSet<>();
      tempSet.add("ami");
      tempSet.add("ach");
      tempSet.add("e");
      tempSet.add("ego");
      tempSet.add("em");
      tempSet.add("emu");
      tempSet.add("ie");
      tempSet.add("im");
      tempSet.add("m");
      tempSet.add("om");
      tempSet.add("owie");
      tempSet.add("owi");
      tempSet.add("ze");
      bannedSuffixes = Collections.unmodifiableSet(tempSet);
    }

  private final UserConfig userConfig;

  public MorfologikPolishSpellerRule(ResourceBundle messages,
                                     Language language, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    super(messages, language, userConfig, altLanguages);
    setCategory(Categories.TYPOS.getCategory(messages));
    addExamplePair(Example.wrong("To jest zdanie z <marker>bledem</marker>"),
                   Example.fixed("To jest zdanie z <marker>błędem</marker>."));
    this.userConfig = userConfig;
  }

  @Override
  public String getFileName() {
    return RESOURCE_FILENAME;
  }

  @Override
  public String getId() {
    return "MORFOLOGIK_RULE_PL_PL";
  }

  @Override
  public Pattern tokenizingPattern() {
    return POLISH_TOKENIZING_CHARS;
  }

  @Override
  protected List<RuleMatch> getRuleMatches(String word, int startPos, AnalyzedSentence sentence, List<RuleMatch> ruleMatchesSoFar)
          throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    if (isMisspelled(speller1, word) && isNotCompound(word)) {
      RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, startPos
              + word.length(), messages.getString("spelling"),
              messages.getString("desc_spelling_short"));
      //If lower case word is not a misspelled word, return it as the only suggestion
      boolean createSuggestions = userConfig == null || userConfig.getMaxSpellingSuggestions() == 0 || ruleMatchesSoFar.size() <= userConfig.getMaxSpellingSuggestions();
      if (!isMisspelled(speller1, word.toLowerCase(conversionLocale))) {
        if (createSuggestions) {
          List<String> suggestion = Arrays.asList(word.toLowerCase(conversionLocale));
          ruleMatch.setSuggestedReplacements(suggestion);
        } else {
          // limited to save CPU
          ruleMatch.setSuggestedReplacement(messages.getString("too_many_errors"));
        }
        ruleMatches.add(ruleMatch);
        return ruleMatches;
      }
      if (createSuggestions) {
        List<String> suggestions = speller1.getSuggestions(word);
        suggestions.addAll(0, getAdditionalTopSuggestions(suggestions, word));
        suggestions.addAll(getAdditionalSuggestions(suggestions, word));
        if (!suggestions.isEmpty()) {
          ruleMatch.setSuggestedReplacements(pruneSuggestions(orderSuggestions(suggestions,word)));
        }
      } else {
        // limited to save CPU
        ruleMatch.setSuggestedReplacement(messages.getString("too_many_errors"));
      }
      ruleMatches.add(ruleMatch);
    }
    return ruleMatches;
  }

  /**
   * Check whether the word is a compound adjective or contains a non-splitting prefix.
   * Used to suppress false positives.
   *
   * @param word Word to be checked.
   * @return True if the word is not a compound.
   * @since 2.5
   */
  private boolean isNotCompound(String word) throws IOException {
    List<String> probablyCorrectWords = new ArrayList<>();
    List<String> testedTokens = new ArrayList<>(2);
    for (int i = 2; i < word.length(); i++) {
      // chop from left to right
      String first = word.substring(0, i);
      String second = word.substring(i);
      if (prefixes.contains(first.toLowerCase(conversionLocale))
              && !isMisspelled(speller1, second)
              && second.length() > first.length()) { // but not for short words such as "premoc"
        // ignore this match, it's fine
        probablyCorrectWords.add(word); // FIXME: some strange words are being accepted, like prekupa
      } else {
        testedTokens.clear();
        testedTokens.add(first);
        testedTokens.add(second);
        List<AnalyzedTokenReadings> taggedToks =
                language.getTagger().tag(testedTokens);
        if (taggedToks.size() == 2
                // "białozielony", trzynastobitowy
                && (taggedToks.get(0).hasPosTag("adja")
                || (taggedToks.get(0).hasPosTag("num:comp")
                   && !taggedToks.get(0).hasPosTag("adv")))
                && taggedToks.get(1).hasPartialPosTag("adj:")) {
          probablyCorrectWords.add(word);
        }
      }
    }
    if (!probablyCorrectWords.isEmpty()) {
      addIgnoreTokens(probablyCorrectWords);
      return false;
    }
    return true;
  }

  /**
   * Remove suggestions -- not really runon words using a list of non-word suffixes
   * @return A list of pruned suggestions.
   */
    private List<String> pruneSuggestions(List<String> suggestions) {
      List<String> prunedSuggestions = new ArrayList<>(suggestions.size());
      for (String suggestion : suggestions) {
        if (suggestion.indexOf(' ') == -1) {
          prunedSuggestions.add(suggestion);
        } else {
          String[] complexSug = suggestion.split(" ");
          if (!bannedSuffixes.contains(complexSug[1])) {
            prunedSuggestions.add(suggestion);
          }
        }
      }
      return prunedSuggestions;
    }
}
