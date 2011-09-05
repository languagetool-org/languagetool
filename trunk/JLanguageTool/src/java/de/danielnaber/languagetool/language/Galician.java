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
import de.danielnaber.languagetool.rules.*;
import de.danielnaber.languagetool.rules.patterns.Unifier;
import de.danielnaber.languagetool.rules.gl.CastWordsRule;
import de.danielnaber.languagetool.rules.gl.SimpleReplaceRule;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.gl.GalicianTagger;
import de.danielnaber.languagetool.tokenizers.SRXSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.Tokenizer;
import de.danielnaber.languagetool.tokenizers.gl.GalicianWordTokenizer;

public class Galician extends Language {

  private Tagger tagger;
  private Tokenizer wordTokenizer;
  private SentenceTokenizer sentenceTokenizer;
  private static final Unifier GALICIAN_UNIFIER = new Unifier();

  @Override
  public final Locale getLocale() {
    return new Locale(getShortName());
  }

  @Override
  public final SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(getShortName());
    }
    return sentenceTokenizer;
  }
  
  @Override
  public final String getName() {
    return "Galician";
  }

  @Override
  public final String getShortName() {
    return "gl";
  }

  @Override
  public final String[] getCountryVariants() {
    return new String[]{"ES"};
  }
  
  @Override
  public String[] getUnpairedRuleStartSymbols() {
    return new String[]{ "[", "(", "{", "“", "«", "‘", "\"", "'" };
  }

  @Override
  public String[] getUnpairedRuleEndSymbols() {
    return new String[]{ "]", ")", "}", "”", "»", "’", "\"", "'" };
  }
  
  @Override
  public final Tagger getTagger() {
    if (tagger == null) {
      tagger = new GalicianTagger();
    }
    return tagger;
  }

  @Override
  public final Tokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new GalicianWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public Unifier getUnifier() {
    return GALICIAN_UNIFIER;
  }

  @Override
  public Contributor[] getMaintainers() {
    final Contributor contributor = new Contributor("Susana Sotelo Docío");
    contributor.setUrl("http://www.linguarum.net/projects/languagetool-gl");
    return new Contributor[] { contributor };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            GenericUnpairedBracketsRule.class,
            UppercaseSentenceStartRule.class,
            // WordRepeatRule.class,
            WhitespaceRule.class,
            // Specific to Galician
            SimpleReplaceRule.class,
            CastWordsRule.class
    );
  }

}
