/*
 * LanguageTool, a natural language style checker 
 * Copyright (c) 2022.  Stefan Viol (https://stevio.de)
 *  
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool.remote.multiLang;

public class InjectedSentence {
  
  private final String language;
  private final String text;

  public InjectedSentence(String language, String text) {
    this.language = language;
    this.text = text;
  }

  public String getLanguage() {
    return language;
  }

  public String getText() {
    return text.trim();
  }

  @Override
  public String toString() {
    return "Sentence: " +
            "language='" + language + '\'' +
            ", text='" + text + '\'';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    InjectedSentence that = (InjectedSentence) o;

    if (!getLanguage().equals(that.getLanguage())) return false;
    if (!getText().equals(that.getText())) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = getLanguage().hashCode();
    result = 31 * result + getText().hashCode();
    return result;
  }
}
