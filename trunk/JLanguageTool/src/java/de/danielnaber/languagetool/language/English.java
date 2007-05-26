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

import java.util.Locale;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.synthesis.Synthesizer;
import de.danielnaber.languagetool.synthesis.en.EnglishSynthesizer;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.en.EnglishTagger;
import de.danielnaber.languagetool.tokenizers.Tokenizer;
import de.danielnaber.languagetool.tokenizers.en.EnglishWordTokenizer;

public class English extends Language {

  private Tagger tagger = new EnglishTagger();
  private Tokenizer wordTokenizer = new EnglishWordTokenizer();
  private Synthesizer synthesizer = new EnglishSynthesizer();

  public Locale getLocale() {
    return new Locale(getShortName());
  }

  public String getName() {
    return "English";
  }

  public String getShortName() {
    return "en";
  }

  public Tagger getTagger() {
    return tagger;
  }

  public Tokenizer getWordTokenizer() {
    return wordTokenizer;
  }

  public Synthesizer getSynthesizer() {
    return synthesizer;
  }

  public String[] getMaintainers() {
    return new String[]{"Marcin Miłkowski", "Daniel Naber"};
  }

}
