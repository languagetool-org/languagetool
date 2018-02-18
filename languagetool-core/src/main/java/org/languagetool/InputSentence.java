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

import java.util.Objects;
import java.util.Set;

/**
 * For internal use only. Used as a key for caching check results.
 * @since 3.7
 */
@Experimental
class InputSentence {

  private final String text;
  private final Language lang;
  private final Language motherTongue;
  private final Set<String> disabledRules;
  private final Set<CategoryId> disabledRuleCategories;
  private final Set<String> enabledRules;
  private final Set<CategoryId> enabledRuleCategories;
  
  InputSentence(String text, Language lang, Language motherTongue,
                       Set<String> disabledRules, Set<CategoryId> disabledRuleCategories,
                       Set<String> enabledRules, Set<CategoryId> enabledRuleCategories) {
    this.text = Objects.requireNonNull(text);
    this.lang = Objects.requireNonNull(lang);
    this.motherTongue = motherTongue;
    this.disabledRules = disabledRules;
    this.disabledRuleCategories = disabledRuleCategories;
    this.enabledRules = enabledRules;
    this.enabledRuleCategories = enabledRuleCategories;
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
           Objects.equals(enabledRuleCategories, other.enabledRuleCategories);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, lang, motherTongue, disabledRules, disabledRuleCategories, enabledRules, enabledRuleCategories);
  }

  @Override
  public String toString() {
    return text;
  }
}
