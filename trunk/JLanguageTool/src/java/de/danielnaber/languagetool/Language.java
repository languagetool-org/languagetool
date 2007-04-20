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

import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.pl.PolishChunker;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.de.GermanTagger;
import de.danielnaber.languagetool.tagging.en.EnglishTagger;
import de.danielnaber.languagetool.tagging.pl.PolishTagger;
import de.danielnaber.languagetool.tagging.cs.CzechTagger;
import de.danielnaber.languagetool.tagging.fr.FrenchTagger;
import de.danielnaber.languagetool.tagging.es.SpanishTagger;
import de.danielnaber.languagetool.tagging.it.ItalianTagger;
import de.danielnaber.languagetool.tagging.nl.DutchTagger;
import de.danielnaber.languagetool.tagging.uk.UkrainianTagger;
import de.danielnaber.languagetool.tagging.xx.DemoTagger;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.de.GermanSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.en.EnglishWordTokenizer;
import de.danielnaber.languagetool.tokenizers.nl.DutchSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.pl.PolishSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.cs.CzechSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.Tokenizer;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Constants for supported languages (English, German, etc).
 * 
 * @author Daniel Naber
 */
public final class Language {

  // IMPORTANT: keep these in sync with LANGUAGES array below:
  
  public static final Language ENGLISH = 
    new Language("English", "en", new Locale("en"), new DemoDisambiguator(), new EnglishTagger(),
        new SentenceTokenizer(), new EnglishWordTokenizer(), "Marcin Miłkowski, Daniel Naber");

  public static final Language GERMAN = 
    new Language("German", "de", new Locale("de"), new DemoDisambiguator(), new GermanTagger(),
        new GermanSentenceTokenizer(), new WordTokenizer(), "Daniel Naber");
  
  public static final Language POLISH = 
    new Language("Polish", "pl", new Locale("pl"), new PolishChunker(), new PolishTagger(),
        new PolishSentenceTokenizer(), new WordTokenizer(), "Marcin Miłkowski");
  
  public static final Language FRENCH = 
    new Language("French", "fr", new Locale("fr"), new DemoDisambiguator(), new FrenchTagger(),
        new SentenceTokenizer(), new WordTokenizer(), null);
  
  public static final Language SPANISH = 
    new Language("Spanish", "es", new Locale("es"), new DemoDisambiguator(), new SpanishTagger(),
        new SentenceTokenizer(), new WordTokenizer(), null);
  
  public static final Language ITALIAN = 
    new Language("Italian", "it", new Locale("it"), new DemoDisambiguator(), new ItalianTagger(),
        new SentenceTokenizer(), new WordTokenizer(), null);
  
  public static final Language DUTCH = 
    new Language("Dutch", "nl", new Locale("nl"), new DemoDisambiguator(), new DutchTagger(),
        new DutchSentenceTokenizer(), new WordTokenizer(), "Ruud Baars");

  public static final Language LITHUANIAN =
    new Language("Lithuanian", "lt", new Locale("lt"), new DemoDisambiguator(), new DemoTagger(),
        new SentenceTokenizer(), new WordTokenizer(), "Mantas Kriaučiūnas");
  
  public static final Language UKRAINIAN =
    new Language("Ukrainian", "uk", new Locale("uk"), new DemoDisambiguator(), new UkrainianTagger(),
        new SentenceTokenizer(), new WordTokenizer(), "Andriy Rysin");
  
  public static final Language CZECH = 
    new Language("Czech", "cs", new Locale("cs"), new DemoDisambiguator(), new CzechTagger(),
        new CzechSentenceTokenizer(), new WordTokenizer(), "Jozef Ličko");

  public static final Language SLOVENIAN = 
    new Language("Slovenian", "sl", new Locale("sl"), new DemoDisambiguator(), new DemoTagger(),
      new SentenceTokenizer(), new WordTokenizer(), "Martin Srebotnjak");
  

  public static final Language DEMO = 
    new Language("Testlanguage", "xx", new Locale("en"), new DemoDisambiguator(), new DemoTagger(),
        new SentenceTokenizer(), new WordTokenizer(), null);

  private String name;
  private String shortForm;
  private Disambiguator disambiguator;
  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  private Tokenizer wordTokenizer;
  private Locale locale;
  private String maintainers;

  // IMPORTANT: keep in sync with objects above
  /**
   * All languages supported by LanguageTool.
   */
  public static final Language[] LANGUAGES = {
    ENGLISH, GERMAN, POLISH, FRENCH, SPANISH, ITALIAN, DUTCH, LITHUANIAN, UKRAINIAN, CZECH, SLOVENIAN, DEMO
  };

  /**
   * Get the Language object for the given short language name.
   * 
   * @param shortLanguageCode e.g. <code>en</code> or <code>de</code>
   * @return a Language object or <code>null</code>
   */
  public static Language getLanguageForShortName(final String shortLanguageCode) {
    StringTools.assureSet(shortLanguageCode, "shortLanguageCode");
    if (shortLanguageCode.length() != "xx".length())
      throw new IllegalArgumentException("'" + shortLanguageCode + "' isn't a two-character code");
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
   * @param languageName e.g. <code>English</code> or <code>German</code> (case is significant)
   * @return a Language object or <code>null</code>
   */
  public static Language getLanguageForName(final String languageName) {
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      if (languageName.equals(Language.LANGUAGES[i].getName())) {
        return Language.LANGUAGES[i];
      }
    }
    return null;
  }

  private Language(final String name, final String shortForm, final Locale locale, final Disambiguator disambiguator,
		  final Tagger tagger, final SentenceTokenizer sentenceTokenizer, final Tokenizer wordTokenizer,
      final String maintainers) {
    StringTools.assureSet(name, "name");
    StringTools.assureSet(shortForm, "shortForm");
    if (disambiguator == null)
      throw new NullPointerException("disambiguator cannot be null");
    if (tagger == null)
      throw new NullPointerException("tagger cannot be null");
    if (locale == null)
      throw new NullPointerException("locale cannot be null");
    if (sentenceTokenizer == null)
      throw new NullPointerException("sentenceTokenizer cannot be null");
    if (wordTokenizer == null)
      throw new NullPointerException("wordTokenizer cannot be null");
    this.name = name;
    this.shortForm = shortForm;
    this.disambiguator = disambiguator;
    this.tagger = tagger;
    this.locale = locale;
    this.sentenceTokenizer = sentenceTokenizer;
    this.wordTokenizer = wordTokenizer;
    this.maintainers = maintainers;
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
   * Get this language's part-of-speech disambiguator implemenation.
   */
  public Disambiguator getDisambiguator() {
    return disambiguator;
  }
  
  /**
   * Get this language's part-of-speech tagger implemenation.
   */
  public Tagger getTagger() {
    return tagger;
  }

  /**
   * Get this language's sentence tokenizer implemenation.
   */
  public SentenceTokenizer getSentenceTokenizer() {
    return sentenceTokenizer;
  }

  /**
   * Get this language's word tokenizer implemenation.
   */
  public Tokenizer getWordTokenizer() {
    return wordTokenizer;
  }

  public Locale getLocale() {
    return locale;
  }

  /**
   * Get the name(s) of the maintainer(s) for this language.
   */
  public String getMaintainers() {
    return maintainers;
  }

}
