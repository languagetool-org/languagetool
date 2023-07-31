/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.rules.CategoryId;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * For internal use only. Used as a key for caching check results.
 * @since 3.7
 */
class InputSentence {

  private final String text;
  private final Language lang;
  private final Language motherTongue;
  private final Set<String> disabledRules;
  private final Set<CategoryId> disabledRuleCategories;
  private final Set<String> enabledRules;
  private final Set<CategoryId> enabledRuleCategories;
  private final UserConfig userConfig;
  private final List<Language> altLanguages;
  private final JLanguageTool.Mode mode;
  private final JLanguageTool.Level level;
  private final Long textSessionID;
  private final Set<ToneTag> toneTags;

  InputSentence(String text, Language lang, Language motherTongue,
                Set<String> disabledRules, Set<CategoryId> disabledRuleCategories,
                Set<String> enabledRules, Set<CategoryId> enabledRuleCategories, UserConfig userConfig,
                List<Language> altLanguages, JLanguageTool.Mode mode, JLanguageTool.Level level, Long textSessionID, Set<ToneTag> toneTags) {
    this.text = Objects.requireNonNull(text);
    this.lang = Objects.requireNonNull(lang);
    this.motherTongue = motherTongue;
    this.disabledRules = disabledRules;
    this.disabledRuleCategories = disabledRuleCategories;
    this.enabledRules = enabledRules;
    this.enabledRuleCategories = enabledRuleCategories;
    this.userConfig = userConfig;
    this.textSessionID = textSessionID;
    this.altLanguages = altLanguages;
    this.mode = Objects.requireNonNull(mode);
    this.level = Objects.requireNonNull(level);
    this.toneTags = toneTags != null ? toneTags : Collections.emptySet();
  }

  InputSentence(String text, Language lang, Language motherTongue,
                Set<String> disabledRules, Set<CategoryId> disabledRuleCategories,
                Set<String> enabledRules, Set<CategoryId> enabledRuleCategories, UserConfig userConfig,
                List<Language> altLanguages, JLanguageTool.Mode mode, JLanguageTool.Level level, Set<ToneTag> toneTags) {
    this(text, lang, motherTongue, disabledRules, disabledRuleCategories,
      enabledRules, enabledRuleCategories, userConfig, altLanguages,
      mode, level, userConfig != null ? userConfig.getTextSessionId() : null, toneTags);
  }
  
  InputSentence(String text, Language lang, Language motherTongue,
                Set<String> disabledRules, Set<CategoryId> disabledRuleCategories,
                Set<String> enabledRules, Set<CategoryId> enabledRuleCategories, UserConfig userConfig,
                List<Language> altLanguages, JLanguageTool.Mode mode, JLanguageTool.Level level) {
    this(text, lang, motherTongue, disabledRules, disabledRuleCategories,
      enabledRules, enabledRuleCategories, userConfig, altLanguages,
      mode, level, userConfig != null ? userConfig.getTextSessionId() : null, null);
  }

  /** @since 4.1 */
  public String getText() {
    return text;
  }
  
  @Override
  public boolean equals(Object o) {
    if (o == null) return false;
    if (o == this) return true;
    if (o.getClass() != getClass()) return false;
    InputSentence other = (InputSentence) o;
    return Objects.equals(text, other.text) && 
           Objects.equals(lang, other.lang) &&
           Objects.equals(motherTongue, other.motherTongue) &&
           Objects.equals(disabledRules, other.disabledRules) &&
           Objects.equals(disabledRuleCategories, other.disabledRuleCategories) &&
           Objects.equals(enabledRules, other.enabledRules) &&
           Objects.equals(enabledRuleCategories, other.enabledRuleCategories) &&
           Objects.equals(userConfig, other.userConfig) &&
           Objects.equals(textSessionID, other.textSessionID) &&
           Objects.equals(altLanguages, other.altLanguages) &&
           mode == other.mode &&
           level == other.level &&
           Objects.equals(toneTags, other.toneTags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, lang, motherTongue, disabledRules, disabledRuleCategories,
            enabledRules, enabledRuleCategories, userConfig, textSessionID, altLanguages, mode, level, toneTags);
  }

  @Override
  public String toString() {
    return text;
  }
}
