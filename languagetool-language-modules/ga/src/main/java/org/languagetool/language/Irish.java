/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.rules.*;
import org.languagetool.rules.ga.*;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ga.IrishSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.ga.IrishHybridDisambiguator;
import org.languagetool.tagging.ga.IrishTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 * @since 4.9
 */
public class Irish extends LanguageWithModel {

  private static final String LANGUAGE_SHORT_CODE = "ga";

  private static volatile Throwable instantiationTrace;

  public Irish() {
    Throwable trace = instantiationTrace;
    if (trace != null) {
      throw new RuntimeException("Language was already instantiated, see the cause stacktrace below.", trace);
    }
    instantiationTrace = new Throwable();
  }

  @Override
  public String getName() {
    return "Irish";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"IE"};
  }
  
  @Override
  public String getShortCode() {
    return "ga";
  }

  @Override
  @NotNull
  public Language getDefaultLanguageVariant() {
    return getInstance();
  }
  
  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
      new Contributor("Jim O'Regan"),
      new Contributor("Emily Barnes"),
      new Contributor("Mícheál J. Ó Meachair"),
      new Contributor("Seanán Ó Coistín")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
      new CommaWhitespaceRule(messages),
	    new GenericUnpairedBracketsRule(messages,
                    Arrays.asList("[", "(", "{", "\"", "“"),
                    Arrays.asList("]", ")", "}", "\"", "”")),
      new DoublePunctuationRule(messages),
      new UppercaseSentenceStartRule(messages, this),
      new LongSentenceRule(messages, userConfig, 50),
      new LongParagraphRule(messages, this, userConfig),
      new UppercaseSentenceStartRule(messages, this),
      new MultipleWhitespaceRule(messages, this),
      new SentenceWhitespaceRule(messages),
      new WhiteSpaceBeforeParagraphEnd(messages, this),
      new WhiteSpaceAtBeginOfParagraph(messages),
      new ParagraphRepeatBeginningRule(messages, this),
      new WordRepeatRule(messages, this),
      new MorfologikIrishSpellerRule(messages, this, userConfig, altLanguages),
      new LogainmRule(messages, this),
      new PeopleRule(messages, this),
      new SpacesRule(messages, this),
      new CompoundRule(messages, this, userConfig),
      new PrestandardReplaceRule(messages, this),
      new IrishReplaceRule(messages, this),
      new IrishFGBEqReplaceRule(messages, this),
      new EnglishHomophoneRule(messages, this),
      new DhaNoBeirtRule(messages),
      new DativePluralStandardReplaceRule(messages, this),
      new IrishSpecificCaseRule(messages)
    );
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new IrishTagger();
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return IrishSynthesizer.INSTANCE;
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new IrishHybridDisambiguator();
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new WordTokenizer();
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  protected int getPriorityForId(String id) {
    switch (id) {
      case "TOO_LONG_PARAGRAPH": return -15;
    }
    return super.getPriorityForId(id);
  }

  @Nullable
  @Override
  protected SpellingCheckRule createDefaultSpellingRule(ResourceBundle messages) throws IOException {
    return new MorfologikIrishSpellerRule(messages, this, null, null);
  }

  public static @NotNull Irish getInstance() {
    Language language = Objects.requireNonNull(Languages.getLanguageForShortCode(LANGUAGE_SHORT_CODE));
    if (language instanceof Irish irish) {
      return irish;
    }
    throw new RuntimeException("Irish language expected, got " + language);
  }
}
