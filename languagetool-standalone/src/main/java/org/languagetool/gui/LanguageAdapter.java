/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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

/**
 * Helper class that can store a Language or a String.
 *
 * @author Panagiotis Minos
 * @since 3.4
 */
class LanguageAdapter {

  private final Language language;
  private final String value;

  LanguageAdapter(String value) {
    this.value = value;
    this.language = null;
  }

  LanguageAdapter(Language language) {
    this.language = language;
    this.value = null;
  }

  String getValue() {
    return value;
  }

  Language getLanguage() {
    return language;
  }

}
