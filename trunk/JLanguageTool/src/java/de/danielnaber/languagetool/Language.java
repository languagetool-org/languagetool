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

import java.util.Locale;

import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.de.GermanTagger;
import de.danielnaber.languagetool.tagging.en.EnglishTagger;
import de.danielnaber.languagetool.tagging.pl.PolishTagger;
import de.danielnaber.languagetool.tagging.fr.FrenchTagger;
import de.danielnaber.languagetool.tagging.es.SpanishTagger;
import de.danielnaber.languagetool.tagging.it.ItalianTagger;
import de.danielnaber.languagetool.tagging.xx.DemoTagger;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.pl.PolishSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.Tokenizer;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;

/**
 * Constants for supported languages (currently English, German, and Polish).
 * 
 * @author Daniel Naber
 */
public final class Language {

  // IMPORTANT: keep in sync with LANGUAGES array below:
  public static final Language ENGLISH = 
    new Language("English", "en", new Locale("en"), new EnglishTagger(), new SentenceTokenizer(), new WordTokenizer());
  
  public static final Language GERMAN = 
    new Language("German", "de", new Locale("de"), new GermanTagger(), new SentenceTokenizer(), new WordTokenizer());
  
  public static final Language POLISH = 
    new Language("Polish", "pl", new Locale("pl"), new PolishTagger(), new PolishSentenceTokenizer(), new WordTokenizer());
  
  public static final Language FRENCH = 
    new Language("French", "fr", new Locale("fr"), new FrenchTagger(), new SentenceTokenizer(), new WordTokenizer());
  
  public static final Language SPANISH = 
    new Language("Spanish", "es", new Locale("es"), new SpanishTagger(), new SentenceTokenizer(), new WordTokenizer());
  
  public static final Language ITALIAN = 
    new Language("Italian", "it", new Locale("it"), new ItalianTagger(), new SentenceTokenizer(), new WordTokenizer());
  
  public static final Language DEMO = 
    new Language("Testlanguage", "xx", new Locale("en"), new DemoTagger(), new SentenceTokenizer(), new WordTokenizer());

  private String name;
  private String shortForm;
  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  private Tokenizer wordTokenizer;
  private Locale locale;

  // IMPORTANT: keep in sync with objects above
  /**
   * All languages supported by LanguageTool.
   */
  public static final Language[] LANGUAGES = new Language[] {ENGLISH, GERMAN, POLISH, FRENCH, SPANISH, ITALIAN, DEMO};

  /**
   * Get the Language object for the given short language name.
   * 
   * @param shortLanguageCode e.g. <code>en</code> or <code>de</code>
   * @return a Language object or <code>null</code>
   */
  public static Language getLanguageForShortName(String shortLanguageCode) {
    if (shortLanguageCode == null)
      throw new NullPointerException("Language code cannot be null");
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
  public static Language getLanguageForName(String languageName) {
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      if (languageName.equals(Language.LANGUAGES[i].getName())) {
        return Language.LANGUAGES[i];
      }
    }
    return null;
  }

  private Language(String name, String shortForm, Locale locale, Tagger tagger, SentenceTokenizer sentenceTokenizer,
      Tokenizer wordTokenizer) {
    this.name = name;
    this.shortForm = shortForm;
    this.tagger = tagger;
    this.locale = locale;
    this.sentenceTokenizer = sentenceTokenizer;
    this.wordTokenizer = wordTokenizer;
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

  public SentenceTokenizer getSentenceTokenizer() {
    return sentenceTokenizer;
  }

  public Tokenizer getWordTokenizer() {
    return wordTokenizer;
  }

  public Locale getLocale() {
    return locale;
  }

}
