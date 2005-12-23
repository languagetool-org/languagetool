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
package de.danielnaber.languagetool;

import de.danielnaber.languagetool.tagging.de.GermanTagger;
import de.danielnaber.languagetool.tagging.en.EnglishTagger;
import de.danielnaber.languagetool.tagging.xx.DemoTagger;
import de.danielnaber.languagetool.tagging.Tagger;

/**
 * Constants for supported languages (currently English and German only).
 * 
 * @author Daniel Naber
 */
public class Language {

  public static final Language ENGLISH = new Language("English", "en", new EnglishTagger());
  public static final Language GERMAN = new Language("German", "de", new GermanTagger());
  public static final Language DEMO = new Language("Testlanguage", "xx", new DemoTagger());

  private String name;
  private String shortForm;
  private Tagger tagger;

  // IMPORTANT: keep in sync with objects above
  /**
   * All languages supported by JLanguageTool.
   */
  public static final Language[] LANGUAGES = new Language[] {ENGLISH, GERMAN, DEMO};

  /**
   * Get the Language object for the given short language name.
   * 
   * @param shortLanguageCode e.g. <code>en</code> or <code>de</code>
   * @return a Language object or <code>null</code>
   */
  public static Language getLanguageforShortName(String shortLanguageCode) {
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      if (shortLanguageCode.equals(Language.LANGUAGES[i].getShortName())) {
        return Language.LANGUAGES[i];
      }
    }
    return null;
  }

  /**
   * Get the Language object for the given language name.
   * 
   * @param languageName e.g. <code>English</code> or <code>German</code>
   * @return a Language object or <code>null</code>
   */
  public static Language getLanguageforName(String languageName) {
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      if (languageName.equals(Language.LANGUAGES[i].getName())) {
        return Language.LANGUAGES[i];
      }
    }
    return null;
  }

  private Language(String name, String shortForm, Tagger tagger) {
    this.name = name;
    this.shortForm = shortForm;
    this.tagger = tagger;
  }

  public String toString() {
    return name;
  }

  /**
   * Get this language's name, e.g. <code>English</code> or <code>German</code>.
   */
  public String getName() {
    return name;
  }

  /**
   * Get this language's two character code, e.g. <code>en</code> for English.
   */
  public String getShortName() {
    return shortForm;
  }

  /**
   * Get this language's part-of-speech tagger implemenation.
   */
  public Tagger getTagger() {
    return tagger;
  }

}
