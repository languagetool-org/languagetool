/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.language;

import java.io.File;
import java.util.Locale;
import java.util.Set;

import de.danielnaber.languagetool.Language;

/**
 * Create a language by specifying the language's XML rule file.
 * 
 * @author Daniel Naber
 */
public class LanguageBuilder {

  private LanguageBuilder() {
  }

  /**
   * Accepts XML files named <tt>rules-xx-language.xml</tt>,
   * e.g. <tt>rules-de-German.xml</tt> and builds
   * a Language object for that language.
   */
  public static Language makeLanguage(final File file) {
    if (file!=null) {
    if (!file.getName().endsWith(".xml"))
      throw new RuleFilenameException(file);
    final String[] parts = file.getName().split("-");
    if (parts.length != 3 || !parts[0].equals("rules") || parts[1].length() != 2)
      throw new RuleFilenameException(file);
    
    Language newLanguage = new Language() {
      public Locale getLocale() {
        return new Locale(getShortName());
      }
      public Contributor[] getMaintainers() {
        return null;
      }
      public String getShortName() {
        return parts[1];
      }
      public String getName() {
        return parts[2].replace(".xml", "");
      }
      public Set<String> getRelevantRuleIDs() {
        return null;
      }
      public String getRuleFileName() {
        return file.getAbsolutePath();
      }
    };
    return newLanguage;
    } else {
      return null;
    }
  }
  
}
