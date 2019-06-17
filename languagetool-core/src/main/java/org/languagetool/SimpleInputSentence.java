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

import java.util.Objects;

/**
 * For internal use only. Used as a key for caching analysis results.
 * @since 3.7
 */
class SimpleInputSentence {

  private final String text;
  private final Language lang;
  
  SimpleInputSentence(String text, Language lang) {
    this.text = Objects.requireNonNull(text);
    this.lang = Objects.requireNonNull(lang);
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
    SimpleInputSentence other = (SimpleInputSentence) o;
    return Objects.equals(text, other.text) && 
           Objects.equals(lang, other.lang);
  }

  @Override
  public int hashCode() {
    return Objects.hash(text, lang);
  }

  @Override
  public String toString() {
    return text;
  }
}
