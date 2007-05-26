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
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.cs.CzechTagger;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.cs.CzechSentenceTokenizer;

public class Czech extends Language {

  private Tagger tagger = new CzechTagger();
  private SentenceTokenizer sentenceTokenizer = new CzechSentenceTokenizer();

  public Locale getLocale() {
    return new Locale(getShortName());
  }

  public String getName() {
    return "Czech";
  }

  public String getShortName() {
    return "cs";
  }

  public Tagger getTagger() {
    return tagger;
  }

  public SentenceTokenizer getSentenceTokenizer() {
    return sentenceTokenizer;
  }

  public String[] getMaintainers() {
    return new String[]{"Jozef Ličko"};
  }

}
