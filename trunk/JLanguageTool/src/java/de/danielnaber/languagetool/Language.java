/* JLanguageTool, a natural language style checker 
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
package de.danielnaber.languagetool;

import java.util.HashMap;
import java.util.Map;

/**
 * Constants for supported languages (currently English and German only).
 * 
 * @author Daniel Naber
 */
public class Language {

  public static final Language ENGLISH = new Language("English", "en");
  public static final Language GERMAN = new Language("German", "de");
  
  // IMPORTANT: keep in sync with objects above
  /**
   * Maps all languages from their two-character String code to their constant 
   * (e.g. <code>en</code> -> <code>Language.ENGLISH</code>).
   */
  public static final Map LANGUAGES = new HashMap();
  static {
    LANGUAGES.put("en", ENGLISH);
    LANGUAGES.put("de", GERMAN);
  }

  private String name;
  private String shortForm;

  private Language(String name, String shortForm) {
    this.name = name;
    this.shortForm = shortForm;
  }

  public String toString() {
    return name;
  }

  public String getShortForm() {
    return shortForm;
  }

}
