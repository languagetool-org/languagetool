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

import org.jetbrains.annotations.Nullable;
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.UserConfig;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.SentenceWhitespaceRule;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.WhiteSpaceAtBeginOfParagraph;
import org.languagetool.rules.WhiteSpaceBeforeParagraphEnd;
import org.languagetool.rules.crh.MorfologikCrimeanTatarSpellerRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.crh.CrimeanTatarSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.crh.CrimeanTatarTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tokenizers.crh.CrimeanTatarWordTokenizer;

/**
 * Support for CrimeanTatar
 */
public class CrimeanTatar extends Language {

  private SentenceTokenizer sentenceTokenizer;
  private WordTokenizer wordTokenizer;
  private CrimeanTatarTagger tagger;

  public CrimeanTatar() {
  }


  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public String getName() {
    return "Crimean Tatar";
  }

  @Override
  public String getShortCode() {
    return "crh";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"UA"};
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new CrimeanTatarTagger();
    }
    return tagger;
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return CrimeanTatarSynthesizer.INSTANCE;
  }

  @Override
  public WordTokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new CrimeanTatarWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Andriy Rysin") };
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  
  
  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language languagees, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
        new CommaWhitespaceRule(messages,
                Example.wrong("Özü bir çaqrım uzaqta<marker> ,</marker> demiryol keçidi yanında yaşay."),
                Example.fixed("Özü bir çaqrım uzaqta<marker>,</marker> demiryol keçidi yanında yaşay.")),
        new DoublePunctuationRule(messages),
        new UppercaseSentenceStartRule(messages, this,
                Example.wrong("Meclis Haberleri 10.09.2003. <marker>qırımtatar</marker> Milliy Meclisiniñ 120-cı toplaşuvı olıp keçti"),
                Example.fixed("Meclis Haberleri 10.09.2003. <marker>Qırımtatar</marker> Milliy Meclisiniñ 120-cı toplaşuvı olıp keçti")),
        new MultipleWhitespaceRule(messages, this),
        new SentenceWhitespaceRule(messages),
        new WhiteSpaceBeforeParagraphEnd(messages, this),
        new WhiteSpaceAtBeginOfParagraph(messages),

        new MorfologikCrimeanTatarSpellerRule(messages, this, userConfig, altLanguages)

    );
  }


}
