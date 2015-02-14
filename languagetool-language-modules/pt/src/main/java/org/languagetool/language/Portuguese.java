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

import org.languagetool.Language;
import org.languagetool.rules.*;
import org.languagetool.rules.pt.PreReformPortugueseCompoundRule;
import org.languagetool.rules.spelling.hunspell.HunspellNoSuggestionRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.pt.PortugueseTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Pre-spelling-reform Portuguese.
 */
public class Portuguese extends Language {

  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  private String name = "Portuguese";

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
    return "pt";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"AO", "MZ"};
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return new PortugalPortuguese();
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Marco A.G. Pinto", "http://www.marcoagpinto.com/")
    };
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new PortugueseTagger();
    }
    return tagger;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule(messages, this),
            new HunspellNoSuggestionRule(messages, this),
            new UppercaseSentenceStartRule(messages, this),
            new WordRepeatRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
            new SentenceWhitespaceRule(messages),
            //Specific to Portuguese:
            new PreReformPortugueseCompoundRule(messages)
    );
  }

}
