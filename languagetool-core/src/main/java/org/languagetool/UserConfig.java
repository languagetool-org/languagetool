/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import lombok.Getter;
import org.languagetool.rules.Rule;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * User-specific configuration. So far, this contains a list of words and a settings map.
 * @since 4.2
 */
public class UserConfig {

  // don't do A/B tests in command line / GUI mode / tests, etc.; true when running as a server
  private static boolean abTestEnabled = false;
  public static void enableABTests() {
    abTestEnabled = true;
  }
  public static boolean hasABTestsEnabled() {
    return abTestEnabled;
  }

  private final List<String> userSpecificSpellerWords;
  private final Set<String> acceptedPhrases;
  private final List<Rule> userSpecificRules;
  private final int maxSpellingSuggestions;
  private final Long userDictCacheSize;
  private final String userDictName;
  private final Long premiumUid;
  private final Map<String, Object[]> configurableRuleValues = new HashMap<>();
  private final LinguServices linguServices;
  // needs to be in UserConfig so it is considered both in ResultCache and in PipelinePool
  private final boolean filterDictionaryMatches;
  private final boolean hidePremiumMatches;

  // partially indifferent for comparing UserConfigs (e.g. in PipelinePool)
  // provided to rules only for A/B tests
  private final Long textSessionId;
  private final List<String> abTest;
  private final String preferredLanguages;
  @Getter
  private boolean trustedSource;

  public UserConfig() {
    this(new ArrayList<>(), new HashMap<>());
  }

  public UserConfig(List<String> userSpecificSpellerWords) {
    this(userSpecificSpellerWords, new HashMap<>());
  }

  public UserConfig(Map<String, Object[]> ruleValues) {
    this(new ArrayList<>(), Objects.requireNonNull(ruleValues));
  }

  public UserConfig(Map<String, Object[]> ruleValues, LinguServices linguServices) {
    this(new ArrayList<>(), Objects.requireNonNull(ruleValues), 0, 0L, null, 0L, linguServices);
  }

  public UserConfig(List<String> userSpecificSpellerWords, Map<String, Object[]> ruleValues) {
    this(userSpecificSpellerWords, ruleValues, 0, null, null, null, null);
  }

  public UserConfig(List<String> userSpecificSpellerWords, Map<String, Object[]> ruleValues,
                    int maxSpellingSuggestions, Long premiumUid, String userDictName, Long userDictCacheSize,
                    LinguServices linguServices) {
    this(userSpecificSpellerWords, Collections.emptyList(), ruleValues, maxSpellingSuggestions, premiumUid, userDictName, userDictCacheSize, linguServices,
      false, null, null, false, null, false);
  }

  public UserConfig(List<String> userSpecificSpellerWords,
                    List<Rule> userSpecificRules,
                    Map<String, Object[]> ruleValues,
                    int maxSpellingSuggestions, Long premiumUid, String userDictName,
                    Long userDictCacheSize,
                    LinguServices linguServices, boolean filterDictionaryMatches,
                    @Nullable List<String> abTest, @Nullable Long textSessionId,
                    boolean hidePremiumMatches, List<String> preferredLanguages) {
    this(userSpecificSpellerWords, userSpecificRules, ruleValues, maxSpellingSuggestions, premiumUid, userDictName, userDictCacheSize, linguServices, filterDictionaryMatches, abTest, textSessionId, hidePremiumMatches, preferredLanguages, false);
  }

  public UserConfig(List<String> userSpecificSpellerWords,
                    List<Rule> userSpecificRules,
                    Map<String, Object[]> ruleValues,
                    int maxSpellingSuggestions, Long premiumUid, String userDictName,
                    Long userDictCacheSize,
                    LinguServices linguServices, boolean filterDictionaryMatches,
                    @Nullable List<String> abTest, @Nullable Long textSessionId,
                    boolean hidePremiumMatches, List<String> preferredLanguages,
                    boolean trustedSource) {
    this.userSpecificSpellerWords = Objects.requireNonNull(userSpecificSpellerWords);
    this.userSpecificRules = Objects.requireNonNull(userSpecificRules);
    for (Map.Entry<String, Object[]> entry : ruleValues.entrySet()) {
      this.configurableRuleValues.put(entry.getKey(), entry.getValue());
    }
    this.maxSpellingSuggestions = maxSpellingSuggestions;
    this.premiumUid = premiumUid;
    this.userDictName = userDictName == null ? "default" : userDictName;
    this.userDictCacheSize = userDictCacheSize;
    this.linguServices = linguServices;
    this.filterDictionaryMatches = filterDictionaryMatches;
    this.abTest = abTest;
    this.textSessionId = textSessionId;
    this.hidePremiumMatches = hidePremiumMatches;
    this.acceptedPhrases = buildAcceptedPhrases();
    this.preferredLanguages = removeAllButMainLanguagesAndSort(preferredLanguages);
    this.trustedSource = trustedSource;
  }

  private String removeAllButMainLanguagesAndSort(List<String> preferredLanguages) {
    List<String> cleanLangList = preferredLanguages != null ? new ArrayList<>(preferredLanguages) : Collections.emptyList();
    cleanLangList.removeIf(language -> {
      if (language.equals("de") || language.equals("en") || language.equals("es") || language.equals("fr") || language.equals("nl") || language.equals("pt")) {
        return false;
      } else {
        return true;
      }
    });
    Collections.sort(cleanLangList);
    return cleanLangList.size() >= 2 ? String.join(",", cleanLangList) : "";
  }

  @NotNull
  public List<String> getAcceptedWords() {
    return userSpecificSpellerWords;
  }

  private Set<String> buildAcceptedPhrases() {
    HashSet<String> phrases = new HashSet<>();
    for (String wordOrPhrase : userSpecificSpellerWords) {
      if (wordOrPhrase.contains(" ")) {
        phrases.add(wordOrPhrase);
      }
    }
    return phrases;
  }

  @NotNull
  public Set<String> getAcceptedPhrases() {
    return acceptedPhrases;
  }

  /**
   * @since 5.7
   */
  @NotNull
  public List<Rule> getRules() {
    return userSpecificRules;
  }

  public int getMaxSpellingSuggestions() {
    return maxSpellingSuggestions;
  }

  public Map<String, Object[]> getConfigValues() {
    return configurableRuleValues;
  }
  
  public void insertConfigValues(Map<String, Object[]>  ruleValues) {
    for (Map.Entry<String, Object[]> entry : ruleValues.entrySet()) {
      this.configurableRuleValues.put(entry.getKey(), entry.getValue());
    }
  }
  
  public Object[] getConfigValueByID(String ruleID) {
    if (configurableRuleValues.containsKey(ruleID)) {
      return configurableRuleValues.get(ruleID);
    }
    return null;
  }
  
  public boolean hasLinguServices() {
    return linguServices != null;
  }
  
  public LinguServices getLinguServices() {
    return linguServices;
  }

  /**
   * for speedup, certain users custom dictionaries are cached.
   * Null if disabled, else max. number of words in cache
   * @since 4.3
   */
  public Long getUserDictCacheSize() {
    return userDictCacheSize;
  }

  /**
   * @since 4.3
   */
  public String getUserDictName() {
    return userDictName;
  }

  /**
   * @since 4.3
   */
  public Long getPremiumUid() {
    return premiumUid;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserConfig other = (UserConfig) o;
    // optimization: equals on userSpecificSpellerWords can be expensive with huge dictionaries
    // -> we use user id & dictionary names first
    // -> userSpecificSpellerWords still needs to be included,
    //  otherwise ResultCache can be used even when dictionaries have changed
    return new EqualsBuilder()
      .append(configurableRuleValues, other.configurableRuleValues)
      .append(userSpecificRules.stream().mapToLong(k -> k.getId().hashCode()).sum(),
              other.userSpecificRules.stream().mapToLong(k -> k.getId().hashCode()).sum())
      .append(premiumUid, other.premiumUid)
      .append(userDictName, other.userDictName)
      .append(userSpecificSpellerWords, other.userSpecificSpellerWords)
      .append(filterDictionaryMatches, other.filterDictionaryMatches)
      // omitting these distorts A/B tests, as UserConfig is cached by the pipeline pool
      // -> (cached) textSessionId on server may say group A, but ID on client (relevant for saved correction) says B
      // only group must match; keeps hit rate of pipeline cache up
      .append(abTest, other.abTest)
      .append(hidePremiumMatches, other.hidePremiumMatches)
      .append(preferredLanguages, other.preferredLanguages)
      .append(trustedSource, other.trustedSource)
      .isEquals();
  }

  @Override
  public int hashCode() {
    // not calculating userSpecificSpellerWords.hashCode(), can be expensive; premiumId + userDictName is close enough
    return new HashCodeBuilder(3, 11)
      .append(maxSpellingSuggestions)
      .append(userSpecificRules.stream()
              .mapToLong(k -> k.getId().hashCode()).sum())
      .append(premiumUid)
      .append(userDictName)
      .append(userDictCacheSize)
      .append(configurableRuleValues)
      .append(abTest)
      .append(filterDictionaryMatches)
      .append(hidePremiumMatches)
      .append(preferredLanguages)
      .append(trustedSource)
      .toHashCode();
  }

  @Override
  public String toString() {
    return "UserConfig{" +
      "dictionarySize=" + userSpecificSpellerWords.size() +
      ", maxSpellingSuggestions=" + maxSpellingSuggestions +
      ", userDictName='" + userDictName + '\'' +
      ", configurableRuleValues=" + configurableRuleValues +
      ", linguServices=" + linguServices +
      ", filterDictionaryMatches=" + filterDictionaryMatches +
      ", textSessionId=" + textSessionId +
      ", hidePremiumMatches=" + hidePremiumMatches +
      ", abTest='" + abTest + '\'' +
      '}';
  }

  public Long getTextSessionId() {
    return textSessionId;
  }

  public List<String> getAbTest() {
    return abTest;
  }

  public boolean filterDictionaryMatches() {
    return filterDictionaryMatches;
  }

  /** @since 5.5 */
  public boolean getHidePremiumMatches() {
    return hidePremiumMatches;
  }

  /**
   * This may not contain the full preferredLanguages list as it's  intended to only be used with ForeignLanguageChecker
   * @return 
   */
  @NotNull
  public List<String> getPreferredLanguages() {
    return Arrays.asList(preferredLanguages.split(","));
  }
}
