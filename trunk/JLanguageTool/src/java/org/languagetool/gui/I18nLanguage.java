/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.gui;

import org.languagetool.Language;

import java.util.ResourceBundle;

/**
 * An item in the language selection combo box.
 */
class I18nLanguage implements Comparable<I18nLanguage> {

  private final Language language;
  private final ResourceBundle messages;

  I18nLanguage(Language language, ResourceBundle messages) {
    this.language = language;
    this.messages = messages;
  }

  Language getLanguage() {
    return language;
  }

  // used by the GUI:
  @Override
  public String toString() {
    if (language.isExternal()) {
      return language.getName() + Main.EXTERNAL_LANGUAGE_SUFFIX;
    } else {
      return language.getTranslatedName(messages);
    }
  }

  @Override
  public int compareTo(I18nLanguage o) {
    return toString().compareTo(o.toString());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final I18nLanguage other = (I18nLanguage) o;
    return language.toString().equals(other.toString()) && language.isExternal() == other.language.isExternal();
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}

