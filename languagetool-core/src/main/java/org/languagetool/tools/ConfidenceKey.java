/* LanguageTool, a natural language style checker
 * Copyright (C) 2024 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tools;

import org.languagetool.Language;
import java.util.Objects;

public class ConfidenceKey {

  private final Language lang;
  private final String ruleId;

  public ConfidenceKey(Language lang, String ruleId) {
    this.lang = Objects.requireNonNull(lang);
    this.ruleId = Objects.requireNonNull(ruleId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ConfidenceKey that = (ConfidenceKey) o;
    return Objects.equals(lang, that.lang) && Objects.equals(ruleId, that.ruleId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(lang, ruleId);
  }

  @Override
  public String toString() {
    return lang.getShortCodeWithCountryAndVariant() + "/" + ruleId;
  }
}
