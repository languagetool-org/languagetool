/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.*;
import org.languagetool.rules.be.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.xx.DemoTagger;
import org.languagetool.tokenizers.be.BelarusianWordTokenizer;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Belarusian language declarations.
 *
 * Copyright (C) 2010 Alex Buloichik (alex73mail@gmail.com)
 * @deprecated this language is unmaintained in LT and might be removed in a future release if we cannot find contributors for it (deprecated since 3.6)
 */
@Deprecated
public class Belarusian extends Language implements AutoCloseable {

  private LanguageModel languageModel;

  @Override
  public Pattern getIgnoredCharactersRegex() {
    return Pattern.compile("[\u00AD\u0301\u0300]");
  }

  @Override
  public String getName() {
    return "Belarusian";
  }

  @Override
  public String getShortCode() {
    return "be";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"BY"};
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new DemoTagger();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new BelarusianWordTokenizer();
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] { new Contributor("Alex Buloichik") };
  }
  
  /** @since 5.1 */
  @Override
  public String getOpeningDoubleQuote() {
    return "«";
  }

  /** @since 5.1 */
  @Override
  public String getClosingDoubleQuote() {
    return "»";
  }
  
  /** @since 5.1 */
  @Override
  public String getOpeningSingleQuote() {
    return "‘";
  }

  /** @since 5.1 */
  @Override
  public String getClosingSingleQuote() {
    return "’";
  }
  
  /** @since 5.1 */
  @Override
  public boolean isAdvancedTypographyEnabled() {
    return true;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages),
            new DoublePunctuationRule(messages),
            new MorfologikBelarusianSpellerRule(messages, this, userConfig, altLanguages),
            new UppercaseSentenceStartRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
	          new SentenceWhitespaceRule(messages),
            new WhiteSpaceBeforeParagraphEnd(messages, this),  
            new WhiteSpaceAtBeginOfParagraph(messages),
            new LongSentenceRule(messages, userConfig, 50),
            new LongParagraphRule(messages, this, userConfig),
            new ParagraphRepeatBeginningRule(messages, this),   //re-activate rule, issue #3509
            //  new PunctuationMarkAtParagraphEnd(messages, this),
            new PunctuationMarkAtParagraphEnd2(messages, this),
            new SimpleReplaceRule(messages),
            new BelarusianSpecificCaseRule(messages)
    );
  }

  /** @since 3.1 */
  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    languageModel = initLanguageModel(indexDir, languageModel);
    return languageModel;
  }

  /**
   * Closes the language model, if any. 
   * @since 3.1
   */
  @Override
  public void close() throws Exception {
    if (languageModel != null) {
      languageModel.close();
    }
  }

  @Override
  protected int getPriorityForId(String id) {
    switch (id) {
      case "RUSSIAN_SIMPLE_REPLACE_RULE":   return 10;  // higher prio than spell checker
      case "BELARUSIAN_SPECIFIC_CASE":      return 9;   // higher prio than spell checker

      case "Word_root_repeat":              return -1;
      case "PUNCT_DPT_2":                   return -2;
      case "TOO_LONG_PARAGRAPH":            return -15;
    }
    return super.getPriorityForId(id);
  }

  @Nullable
  @Override
  protected SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new MorfologikBelarusianSpellerRule(messages, this, null, null);
  }
}
