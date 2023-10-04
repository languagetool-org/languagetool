/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Hiroshi Miura
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
import org.languagetool.chunking.Chunker;
import org.languagetool.language.Contributor;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.rules.RemoteRuleConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.Unifier;
import org.languagetool.rules.patterns.UnifierConfiguration;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author Hiroshi Miura
 */
public abstract class AbstractLanguageProxy extends Language implements AutoCloseable {
  private final Language impl;

  /**
   * Abstract base class for backward comaptible
   * org.langaugetool.langauge.(LANGUAGE) classes.
   * It depends on classes `org.languagetool.language.(ln).(LANGUAGE)
   * class extends org.languagetool.compat.LanguageCompat class.
   * @param className Language full qualified class name.
   */
  protected AbstractLanguageProxy(String className) {
    try {
      Class<?> clazz = Class.forName(className);
      impl = (Language) clazz.getDeclaredConstructor().newInstance();
    } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
             NoSuchMethodException e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  public AbstractLanguageProxy() {
    this("");
  }

  @Override
  public String getShortCode() {
    return impl.getShortCode();
  }

  @Override
  public String getName() {
    return impl.getName();
  }

  @Override
  public String[] getCountries() {
    return impl.getCountries();
  }

  @Nullable
  @Override
  public Contributor[] getMaintainers() {
    return impl.getMaintainers();
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return impl.getRelevantRules(messages, userConfig, motherTongue, altLanguages);
  }

  @Override
  public String getCommonWordsPath() {
    return impl.getCommonWordsPath();
  }

  @Override
  public String getVariant() {
    return impl.getVariant();
  }

  @Override
  public List<String> getDefaultEnabledRulesForVariant() {
    return impl.getDefaultEnabledRulesForVariant();
  }

  @Override
  public List<String> getDefaultDisabledRulesForVariant() {
    return impl.getDefaultDisabledRulesForVariant();
  }

  @Override
  public synchronized LanguageModel getLanguageModel(File indexDir) throws IOException {
    return impl.getLanguageModel(indexDir);
  }

  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, @Nullable LanguageModel languageModel,
                                                      UserConfig userConfig) throws IOException {
    return impl.getRelevantLanguageModelRules(messages, languageModel,  userConfig);
  }

  @Override
  public List<Rule> getRelevantLanguageModelCapableRules(ResourceBundle messages, @Nullable LanguageModel lm,
                                                         GlobalConfig globalConfig, UserConfig userConfig,
                                                         Language motherTongue, List<Language> altLanguages)
    throws IOException {
    return impl.getRelevantLanguageModelCapableRules(messages, lm, globalConfig, userConfig, motherTongue,
      altLanguages);
  }

  @Override
  public List<Rule> getRelevantRemoteRules(ResourceBundle messageBundle, List<RemoteRuleConfig> configs,
                                           GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue,
                                           List<Language> altLanguages, boolean inputLogging) throws IOException {
    return impl.getRelevantRemoteRules(messageBundle, configs, globalConfig, userConfig, motherTongue, altLanguages,
      inputLogging);
  }

/*
  @Override
  public Function<Rule, Rule> getRemoteEnhancedRules(
    ResourceBundle messageBundle, List<RemoteRuleConfig> configs, UserConfig userConfig,
    Language motherTongue, List<Language> altLanguages, boolean inputLogging) throws IOException {
    return impl.getRemoteEnhancedRules(messageBundle, configs, userConfig,motherTongue, altLanguages, inputLogging);
  }
*/

  @Override
  public List<Rule> getRelevantRulesGlobalConfig(ResourceBundle messages, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return impl.getRelevantRulesGlobalConfig(messages, globalConfig, userConfig, motherTongue, altLanguages);
  }

  @Nullable
  @Override
  public SpellingCheckRule getDefaultSpellingRule() {
    return impl.getDefaultSpellingRule();
  }

  @Override
  public Locale getLocale() {
    return impl.getLocale();
  }

  @Override
  public Locale getLocaleWithCountryAndVariant() {
    return impl.getLocaleWithCountryAndVariant();
  }

  @Override
  public List<String> getRuleFileNames() {
    return impl.getRuleFileNames();
  }

  @NotNull
  @Override
  public Language getDefaultLanguageVariant() {
    return impl.getDefaultLanguageVariant();
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return impl.createDefaultDisambiguator();
  }

  @Override
  public void setDisambiguator(Disambiguator disambiguator) {
    impl.setDisambiguator(disambiguator);
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return impl.createDefaultTagger();
  }

  @NotNull
  @Override
  public synchronized Tagger getTagger() {
    return impl.getTagger();
  }

  @Override
  public void setTagger(Tagger tagger) {
    impl.setTagger(tagger);
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return impl.createDefaultSentenceTokenizer();
  }

  @Override
  public synchronized SentenceTokenizer getSentenceTokenizer() {
    return impl.getSentenceTokenizer();
  }

  @Override
  public void setSentenceTokenizer(SentenceTokenizer tokenizer) {
    impl.setSentenceTokenizer(tokenizer);
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return impl.createDefaultWordTokenizer();
  }

  @Override
  public synchronized Tokenizer getWordTokenizer() {
    return impl.getWordTokenizer();
  }

  @Override
  public void setWordTokenizer(Tokenizer tokenizer) {
    impl.setWordTokenizer(tokenizer);
  }

  @Nullable
  @Override
  public Chunker createDefaultChunker() {
    return impl.createDefaultChunker();
  }

  @Override
  public synchronized Chunker getChunker() {
    return impl.getChunker();
  }

  @Override
  public void setChunker(Chunker chunker) {
    impl.setChunker(chunker);
  }

  @Override
  public Chunker createDefaultPostDisambiguationChunker() {
    return impl.createDefaultPostDisambiguationChunker();
  }

  @Nullable
  @Override
  public synchronized Chunker getPostDisambiguationChunker() {
    return impl.getPostDisambiguationChunker();
  }

  @Override
  public void setPostDisambiguationChunker(Chunker chunker) {
    impl.setPostDisambiguationChunker(chunker);
  }

  @Override
  public JLanguageTool createDefaultJLanguageTool() {
    return impl.createDefaultJLanguageTool();
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return impl.createDefaultSynthesizer();
  }

  @Nullable
  @Override
  public synchronized Synthesizer getSynthesizer() {
    return impl.getSynthesizer();
  }

  @Override
  public void setSynthesizer(Synthesizer synthesizer) {
    impl.setSynthesizer(synthesizer);
  }

  @Override
  public Unifier getUnifier() {
    return impl.getUnifier();
  }

  @Override
  public Unifier getDisambiguationUnifier() {
    return impl.getDisambiguationUnifier();
  }

  @Override
  public UnifierConfiguration getUnifierConfiguration() {
    return impl.getUnifierConfiguration();
  }

  @Override
  public UnifierConfiguration getDisambiguationUnifierConfiguration() {
    return impl.getDisambiguationUnifierConfiguration();
  }

  @Override
  public boolean isVariant() {
    return impl.isVariant();
  }

  @Override
  public boolean isExternal() {
    return impl.isExternal();
  }

  public boolean equalsConsiderVariantsIfSpecified(Language otherLanguage) {
    return impl.equalsConsiderVariantsIfSpecified(otherLanguage);
  }

  @Override
  public Pattern getIgnoredCharactersRegex() {
    return impl.getIgnoredCharactersRegex();
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
    return impl.getMaintainedState();
  }

  @Override
  public boolean isHiddenFromGui() {
    return impl.isHiddenFromGui();
  }

  @Override
  public int getRulePriority(Rule rule) {
    return impl.getRulePriority(rule);
  }

  @Override
  public boolean isSpellcheckOnlyLanguage() {
    return impl.isSpellcheckOnlyLanguage();
  }

  @Override
  public boolean hasNGramFalseFriendRule(Language motherTongue) {
    return impl.hasNGramFalseFriendRule(motherTongue);
  }

  @Override
  public String getOpeningDoubleQuote() {
    return impl.getOpeningDoubleQuote();
  }

  @Override
  public String getClosingDoubleQuote() {
    return impl.getClosingDoubleQuote();
  }

  @Override
  public String getOpeningSingleQuote() {
    return impl.getOpeningSingleQuote();
  }

  @Override
  public String getClosingSingleQuote() {
    return impl.getClosingSingleQuote();
  }

  @Override
  public boolean isAdvancedTypographyEnabled() {
    return impl.isAdvancedTypographyEnabled();
  }

  @Override
  public List<RuleMatch> adaptSuggestions(List<RuleMatch> ruleMatches, Set<String> enabledRules) {
    return impl.adaptSuggestions(ruleMatches, enabledRules);
  }

  // XXX

  @Override
  public void close() throws Exception {
    if (impl instanceof AutoCloseable) {
      ((AutoCloseable) impl).close();
    }
  }

  @Override
  public Function<Rule, Rule> getRemoteEnhancedRules(ResourceBundle messageBundle, List<RemoteRuleConfig> configs, UserConfig userConfig, Language motherTongue, List<Language> altLanguages, boolean inputLogging) throws IOException {
    return impl.getRemoteEnhancedRules(messageBundle, configs, userConfig, motherTongue, altLanguages, inputLogging);
  }

  @Override
  public String toAdvancedTypography(String input) {
    return impl.toAdvancedTypography(input);
  }

  @Override
  public boolean hasMinMatchesRules() {
    return impl.hasMinMatchesRules();
  }

  @Override
  public String adaptSuggestion(String s) {
    return impl.adaptSuggestion(s);
  }

  @Override
  public String getConsistencyRulePrefix() {
    return impl.getConsistencyRulePrefix();
  }

  @Override
  public RuleMatch adjustMatch(RuleMatch rm, List<String> features) {
    return impl.adjustMatch(rm, features);
  }

  @Override
  public List<RuleMatch> mergeSuggestions(List<RuleMatch> ruleMatches, AnnotatedText text, Set<String> enabledRules) {
    return impl.mergeSuggestions(ruleMatches, text, enabledRules);
  }
}
