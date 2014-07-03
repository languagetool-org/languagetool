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
package org.languagetool.language;

import java.util.Arrays;
import java.util.List;

import org.languagetool.Language;
import org.languagetool.rules.*;
import org.languagetool.rules.sk.CompoundRule;
import org.languagetool.rules.sk.MorfologikSlovakSpellerRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.sk.SlovakSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.sk.SlovakTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

public class Slovak extends Language {

  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  private Synthesizer synthesizer;
  private String name = "Slovak";

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String getShortName() {
    return "sk";
  }
  
  @Override
  public String[] getCountries() {
    return new String[]{"SK"};
  }

  @Override
  public String[] getUnpairedRuleStartSymbols() {
    return new String[]{ "[", "(", "{", "„", "»", "«", "\"" };
  }

  @Override
  public String[] getUnpairedRuleEndSymbols() {
    return new String[]{ "]", ")", "}", "“", "«", "»", "\"" };
  }
  
  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new SlovakTagger();
    }
    return tagger;
  }

  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new SlovakSynthesizer();
    }
    return synthesizer;
  }
  
  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }
  
  @Override
  public Contributor[] getMaintainers() {
    final Contributor contributor = new Contributor("Zdenko Podobný");
    contributor.setUrl("http://sk-spell.sk.cx");
    return new Contributor[] { contributor };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            GenericUnpairedBracketsRule.class,            
            UppercaseSentenceStartRule.class,
            WordRepeatRule.class,
            MultipleWhitespaceRule.class,
            // specific to Slovak:
            CompoundRule.class,
            MorfologikSlovakSpellerRule.class
            //SlovakVesRule.class
    );
  }

}
