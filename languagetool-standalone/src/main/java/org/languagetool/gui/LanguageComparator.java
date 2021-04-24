/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import java.util.Comparator;
import java.util.ResourceBundle;
import org.languagetool.Language;

/**
 * Comparator class for sorting Language by locale name
 *
 * @author Panagiotis Minos
 */
class LanguageComparator implements Comparator<Language> {

  private final ResourceBundle messages;
  private final String extLangSuffix;

  LanguageComparator(ResourceBundle messages, String extLangSuffix) {
    this.messages = messages;
    this.extLangSuffix = extLangSuffix;
  }

  @Override
  public int compare(Language o1, Language o2) {
    return getTranslatedName(o1).compareTo(getTranslatedName(o2));
  }

  private String getTranslatedName(Language language) {
    if (language.isExternal()) {
      return language.getName() + extLangSuffix;
    } else {
      return language.getTranslatedName(messages);
    }
  }
}
