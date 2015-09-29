/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging;

/**
 * Result of {@link WordTagger}.
 * @since 2.8
 */
public final class TaggedWord {

  private final String lemma;
  private final String posTag;

  public TaggedWord(String lemma, String posTag) {
    this.lemma = lemma;
    this.posTag = posTag;
  }

  public String getLemma() {
    return lemma;
  }

  public String getPosTag() {
    return posTag;
  }

  @Override
  public String toString() {
    return lemma + "/" + posTag;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TaggedWord that = (TaggedWord) o;
    if (lemma != null ? !lemma.equals(that.lemma) : that.lemma != null) return false;
    if (posTag != null ? !posTag.equals(that.posTag) : that.posTag != null) return false;
    return true;
  }

  @Override
  public int hashCode() {
    int result = lemma != null ? lemma.hashCode() : 0;
    result = 31 * result + (posTag != null ? posTag.hashCode() : 0);
    return result;
  }
}
