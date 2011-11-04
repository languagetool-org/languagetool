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
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;

/**
 * Create a language by specifying the language's XML rule file.
 * 
 * @author Daniel Naber
 */
public class LanguageBuilder {

  private LanguageBuilder() {
  }

  public static Language makeAdditionalLanguage(final File file) {
    return makeLanguage(file, true);
  }

  /**
   * Takes an XML file named <tt>rules-xx-language.xml</tt>,
   * e.g. <tt>rules-de-German.xml</tt> and builds
   * a Language object for that language.
   */
  private static Language makeLanguage(final File file, final boolean isAdditional) {
    if (file == null) {
      throw new NullPointerException("file argument cannot be null");
    }
    if (!file.getName().endsWith(".xml")) {
      throw new RuleFilenameException(file);
    }
    final String[] parts = file.getName().split("-");
    final boolean startsWithRules = parts[0].equals("rules");
    final boolean secondPartHasCorrectLength = parts[1].length() == 2 || parts[1].length() == 3;
    if (parts.length != 3 || !startsWithRules || !secondPartHasCorrectLength) {
      throw new RuleFilenameException(file);
    }
    
    final Language newLanguage = new Language() {
      @Override
      public Locale getLocale() {
        return new Locale(getShortName());
      }
      @Override
      public Contributor[] getMaintainers() {
        return null;
      }
      @Override
      public String getShortName() {
        return parts[1];
      }
      @Override
      public String[] getCountryVariants() {
        return new String[] {""};
      }
      @Override
      public String getName() {
        return parts[2].replace(".xml", "");
      }
      @Override
      public List<Class<? extends Rule>> getRelevantRules() {
        return Collections.emptyList();
      }
      @Override
      public String getRuleFileName() {
        return file.getAbsolutePath();
      }
      @Override
      public boolean isExternal() {
        return isAdditional;
      }
    };
    return newLanguage;
  }
  
}
