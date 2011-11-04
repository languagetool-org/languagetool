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
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.da.DanishTagger;
import de.danielnaber.languagetool.tokenizers.SRXSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

public class Danish extends Language {

  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  
  @Override
  public final Locale getLocale() {
    return new Locale(getShortName());
  }

  @Override
  public final String getName() {
    return "Danish";
  }

  @Override
  public final String getShortName() {
    return "da";
  }

  @Override
  public final String[] getCountryVariants() {
    return new String[]{"DK"};
  }
  
  @Override
  public String[] getUnpairedRuleStartSymbols() {
    return new String[]{ "[", "(", "{", "\"", "”" };
  }

  @Override
  public String[] getUnpairedRuleEndSymbols() {
    return new String[]{ "]", ")", "}", "\"", "”" };
  }
  
  @Override
  public final Tagger getTagger() {
    if (tagger == null) {
      tagger = new DanishTagger();
    }
    return tagger;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(getShortName());
    }
    return sentenceTokenizer;
  }

  @Override
  public final Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Esben Aaberg")};
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            GenericUnpairedBracketsRule.class,  // correction for genitive apostrophes eg. "Lis' hund" made in UnpairedQuotesBracketsRule
            UppercaseSentenceStartRule.class,  // abbreviation exceptions, done in DanishSentenceTokenizer
            // "WORD_REPEAT_RULE" implemented in grammar.xml
            WhitespaceRule.class
    );
  }

}
