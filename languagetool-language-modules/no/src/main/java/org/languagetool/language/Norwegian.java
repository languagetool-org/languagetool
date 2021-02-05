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

import org.jetbrains.annotations.NotNull;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.EmptyLineRule;
import org.languagetool.rules.Example;
import org.languagetool.rules.LongParagraphRule;
import org.languagetool.rules.LongSentenceRule;
import org.languagetool.rules.MultipleWhitespaceRule;
import org.languagetool.rules.ParagraphRepeatBeginningRule;
import org.languagetool.rules.PunctuationMarkAtParagraphEnd;
import org.languagetool.rules.PunctuationMarkAtParagraphEnd2;
import org.languagetool.rules.Rule;
import org.languagetool.rules.SentenceWhitespaceRule;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.WhiteSpaceAtBeginOfParagraph;
import org.languagetool.rules.WhiteSpaceBeforeParagraphEnd;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.no.NorwegianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class Norwegian extends Language {

  @Override
  public String getName() {
    return "Norwegian";
  }

  @Override
  public String getShortCode() {
    return "no";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"NO"};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new NorwegianTagger();
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return super.createDefaultWordTokenizer();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new XmlRuleDisambiguator(this);
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {new Contributor("Adthena")};
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) {
    return Arrays.asList(
      new CommaWhitespaceRule(messages,
        Example.wrong("We had coffee<marker> ,</marker> cheese and crackers and grapes."),
        Example.fixed("We had coffee<marker>,</marker> cheese and crackers and grapes.")),
      new DoublePunctuationRule(messages),
      new UppercaseSentenceStartRule(messages, this,
        Example.wrong("This house is old. <marker>it</marker> was built in 1950."),
        Example.fixed("This house is old. <marker>It</marker> was built in 1950.")),
      new MultipleWhitespaceRule(messages, this),
      new SentenceWhitespaceRule(messages),
      new WhiteSpaceBeforeParagraphEnd(messages, this),
      new WhiteSpaceAtBeginOfParagraph(messages),
      new EmptyLineRule(messages, this),
      new LongSentenceRule(messages, userConfig, 33, true, true),
      new LongParagraphRule(messages, this, userConfig),
      new ParagraphRepeatBeginningRule(messages, this),
      new PunctuationMarkAtParagraphEnd(messages, this),
      new PunctuationMarkAtParagraphEnd2(messages, this)
    );
  }
}
