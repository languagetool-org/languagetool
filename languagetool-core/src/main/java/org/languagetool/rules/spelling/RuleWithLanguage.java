/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Daniel Naber (http://danielnaber.de)
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
package org.languagetool.rules.spelling;

import org.languagetool.Language;
import org.languagetool.rules.Rule;

import java.util.Objects;

/**
 * @since 4.4
 */
public class RuleWithLanguage {

  private final Rule rule;
  private final Language language;
  
  RuleWithLanguage(Rule rule, Language language) {
    this.rule = Objects.requireNonNull(rule);
    this.language = Objects.requireNonNull(language);
  }

  public Language getLanguage() {
    return language;
  }

  public Rule getRule() {
    return rule;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RuleWithLanguage that = (RuleWithLanguage) o;
    return Objects.equals(rule, that.rule) && Objects.equals(language, that.language);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rule, language);
  }
}
