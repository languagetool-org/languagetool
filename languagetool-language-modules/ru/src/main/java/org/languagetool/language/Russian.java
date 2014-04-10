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
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.WhitespaceRule;
import org.languagetool.rules.WordRepeatRule;
import org.languagetool.rules.ru.RussianCompoundRule;
import org.languagetool.rules.ru.RussianSimpleReplaceRule;
import org.languagetool.rules.ru.RussianUnpairedBracketsRule;
import org.languagetool.rules.ru.RussianWordRepeatRule;
//import org.languagetool.rules.spelling.hunspell.HunspellNoSuggestionRule;
import org.languagetool.rules.ru.MorfologikRussianSpellerRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ru.RussianSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.ru.RussianHybridDisambiguator;
import org.languagetool.tagging.ru.RussianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

public class Russian extends Language {

  private Tagger tagger;
  private Disambiguator disambiguator;
  private Synthesizer synthesizer;
  private SentenceTokenizer sentenceTokenizer;
  private String name ="Russian";

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
    return "ru";
  }

  @Override
  public String[] getCountries() {
    return new String[] {"RU"};
  }
  
  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new RussianTagger();
    }
    return tagger;
  }

  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new RussianHybridDisambiguator();
    }
    return disambiguator;
  }
  
  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new RussianSynthesizer();
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
    final Contributor contributor = new Contributor("Yakov Reztsov");
    contributor.setUrl("http://myooo.ru/content/view/83/43/");
    return new Contributor[] { contributor };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            UppercaseSentenceStartRule.class,
  //          HunspellNoSuggestionRule.class,
            MorfologikRussianSpellerRule.class,
            WordRepeatRule.class,
            WhitespaceRule.class,
            // specific to Russian :
            RussianUnpairedBracketsRule.class,
            RussianCompoundRule.class,
            RussianSimpleReplaceRule.class,
            RussianWordRepeatRule.class
    );
  }

}