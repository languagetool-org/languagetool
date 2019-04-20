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
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.lt.MorfologikLithuanianSpellerRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.xx.DemoTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

/**
 * @deprecated this language hasn't been maintained for years, it will be removed from LanguageTool after release 3.6
 */
@Deprecated
public class Lithuanian extends Language {

  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;

  @Override
  public String getName() {
    return "Lithuanian";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"LT"};
  }
  
  @Override
  public String getShortCode() {
    return "lt";
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new DemoTagger();
    }
    return tagger;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Mantas Kriaučiūnas")};
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule(messages),
            new MorfologikLithuanianSpellerRule(messages, this, userConfig, altLanguages),
            new UppercaseSentenceStartRule(messages, this),
            new MultipleWhitespaceRule(messages, this)
    );
  }

}
