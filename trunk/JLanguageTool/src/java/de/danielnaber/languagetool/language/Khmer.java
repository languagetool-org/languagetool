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

import java.util.*;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Rule;
import de.danielnaber.languagetool.rules.km.KhmerSimpleReplaceRule;
import de.danielnaber.languagetool.rules.km.KhmerWordRepeatRule;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.km.KhmerTagger;
import de.danielnaber.languagetool.tokenizers.SRXSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.rules.km.KhmerRuleDisambiguator;
import de.danielnaber.languagetool.tokenizers.Tokenizer;
import de.danielnaber.languagetool.tokenizers.km.KhmerWordTokenizer;

public class Khmer extends Language {

  private final Tagger tagger = new KhmerTagger();
  private final Tokenizer wordTokenizer = new KhmerWordTokenizer();
  private final SentenceTokenizer sentenceTokenizer = new SRXSentenceTokenizer(getShortName());
  private final Disambiguator disambiguator = new KhmerRuleDisambiguator();
  
  @Override
  public Locale getLocale() {
    return new Locale("km");
  }

  @Override
  public String getName() {
    return "Khmer";
  }

  @Override
  public String getShortName() {
    return "km";
  }

  @Override
  public String[] getCountryVariants() {
    return new String[]{"KM"};
  }
  
  @Override
  public Tagger getTagger() {
    return tagger;
  }
  
  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    return sentenceTokenizer;
  }

  @Override
  public Tokenizer getWordTokenizer() {
    return wordTokenizer;
  }
  
  @Override
  public Disambiguator getDisambiguator() {
    return disambiguator;
  }
  
  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Nathan Wells")};
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
      // specific to Khmer:
      KhmerSimpleReplaceRule.class,
      KhmerWordRepeatRule.class
    );
  }

}
