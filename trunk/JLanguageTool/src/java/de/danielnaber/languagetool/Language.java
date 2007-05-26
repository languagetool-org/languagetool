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

import de.danielnaber.languagetool.language.Czech;
import de.danielnaber.languagetool.language.Demo;
import de.danielnaber.languagetool.language.Dutch;
import de.danielnaber.languagetool.language.English;
import de.danielnaber.languagetool.language.French;
import de.danielnaber.languagetool.language.German;
import de.danielnaber.languagetool.language.Italian;
import de.danielnaber.languagetool.language.Lithuanian;
import de.danielnaber.languagetool.language.Polish;
import de.danielnaber.languagetool.language.Slovenian;
import de.danielnaber.languagetool.language.Spanish;
import de.danielnaber.languagetool.language.Ukrainian;
import de.danielnaber.languagetool.synthesis.Synthesizer;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.xx.DemoDisambiguator;
import de.danielnaber.languagetool.tagging.xx.DemoTagger;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.Tokenizer;
import de.danielnaber.languagetool.tokenizers.WordTokenizer;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Base class for any supported language (English, German, etc).
 * 
 * @author Daniel Naber
 */
public abstract class Language {

  // NOTE: keep in sync with array below!
  public final static Language CZECH = new Czech();
  public final static Language DUTCH = new Dutch();
  public final static Language ENGLISH = new English();
  public final static Language FRENCH = new French();
  public final static Language GERMAN = new German();
  public final static Language ITALIAN = new Italian();
  public final static Language LITHUANIAN = new Lithuanian();
  public final static Language POLISH = new Polish();
  public final static Language SLOVENIAN = new Slovenian();
  public final static Language SPANISH = new Spanish();
  public final static Language UKRAINIAN = new Ukrainian();
  
  public final static Language DEMO = new Demo();
  
  /**
   * All languages supported by LanguageTool.
   */
  public static final Language[] LANGUAGES = {
    ENGLISH, GERMAN, POLISH, FRENCH, SPANISH, ITALIAN, DUTCH, LITHUANIAN, UKRAINIAN, CZECH, SLOVENIAN, DEMO
    // FIXME: load dynamically from classpath
  };
  
  private final static Disambiguator DEMO_DISAMBIGUATOR = new DemoDisambiguator();
  private final static Tagger DEMO_TAGGER = new DemoTagger();
  private final static SentenceTokenizer SENTENCE_TOKENIZER = new SentenceTokenizer();
  private final static WordTokenizer WORD_TOKENIZERr = new WordTokenizer();

  /**
   * Get this language's two character code, e.g. <code>en</code> for English.
   */
  public abstract String getShortName();

  /**
   * Get this language's name in English, e.g. <code>English</code> or <code>German</code>.
   */
  public abstract String getName();
  
  /**
   * Get this language's Java locale.
   */
  public abstract Locale getLocale();

  /**
   * Get this language's part-of-speech disambiguator implementation.
   */
  public Disambiguator getDisambiguator() {
    return DEMO_DISAMBIGUATOR;
  }

  /**
   * Get this language's part-of-speech tagger implementation.
   */
  public Tagger getTagger() {
    return DEMO_TAGGER;
  }

  /**
   * Get this language's sentence tokenizer implementation.
   */
  public SentenceTokenizer getSentenceTokenizer() {
    return SENTENCE_TOKENIZER;
  }

  /**
   * Get this language's word tokenizer implementation.
   */
  public Tokenizer getWordTokenizer() {
    return WORD_TOKENIZERr;
  }

  /**
   * Get this language's part-of-speech synthesizer implementation or <code>null</code>.
   */
  public Synthesizer getSynthesizer() {
    return null;
  }

  /**
   * Get the name(s) of the maintainer(s) for this language or <code>null</code>.
   */
  public abstract String[] getMaintainers();

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
  
  public String toString() {
    return getName();
  }
  
}
