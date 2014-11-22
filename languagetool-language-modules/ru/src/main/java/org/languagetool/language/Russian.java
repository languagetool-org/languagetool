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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.rules.*;
import org.languagetool.rules.ru.RussianCompoundRule;
import org.languagetool.rules.ru.RussianSimpleReplaceRule;
import org.languagetool.rules.ru.RussianUnpairedBracketsRule;
import org.languagetool.rules.ru.RussianWordRepeatRule;
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
  public void setName(String name) {
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
    Contributor contributor = new Contributor("Yakov Reztsov");
    contributor.setUrl("http://myooo.ru/content/view/83/43/");
    return new Contributor[] { contributor };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages),
            new DoublePunctuationRule(messages),
            new UppercaseSentenceStartRule(messages, this),
            new MorfologikRussianSpellerRule(messages, this),
            new WordRepeatRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
            // specific to Russian :
            new RussianUnpairedBracketsRule(messages, this),
            new RussianCompoundRule(messages),
            new RussianSimpleReplaceRule(messages),
            new RussianWordRepeatRule(messages)
    );
  }

}