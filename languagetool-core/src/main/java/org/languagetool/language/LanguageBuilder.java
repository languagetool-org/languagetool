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
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.chunking.Chunker;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Create a language by specifying the language's XML rule file.
 */
public final class LanguageBuilder {

  private LanguageBuilder() {
  }

  public static Language makeAdditionalLanguage(File file) throws InstantiationException, IllegalAccessException {
    return makeLanguage(file, true);
  }

  /**
   * Takes an XML file named <tt>rules-xx-language.xml</tt>,
   * e.g. <tt>rules-de-German.xml</tt> and builds
   * a Language object for that language.
   */
  private static Language makeLanguage(File file, boolean isAdditional) throws IllegalAccessException, InstantiationException {
    Objects.requireNonNull(file, "file cannot be null");
    if (!file.getName().endsWith(".xml")) {
      throw new RuleFilenameException(file);
    }
    String[] parts = file.getName().split("-");
    boolean startsWithRules = parts[0].equals("rules");
    boolean secondPartHasCorrectLength = parts.length == 3 &&
            (parts[1].length() == "en".length() || parts[1].length() == "ast".length() || parts[1].length() == "en_US".length());
    if (!startsWithRules || !secondPartHasCorrectLength) {
      throw new RuleFilenameException(file);
    }
    //TODO: when the XML file is mergeable with
    // other rules (check this in the XML Rule Loader by using rules[@integrate='add']?),
    // subclass the existing language,
    //and adjust the settings if any are set in the rule file default configuration set

    Language newLanguage;
    if (Languages.isLanguageSupported(parts[1])) {
      Language baseLanguage = Languages.getLanguageForShortCode(parts[1]).getClass().newInstance();
      newLanguage = new ExtendedLanguage(baseLanguage, parts[2].replace(".xml", ""), file);
    } else {
      newLanguage = new Language() {
        @Override
        public Locale getLocale() {
          return new Locale(getShortCode());
        }

        @Override
        public Contributor[] getMaintainers() {
          return null;
        }

        @Override
        public String getShortCode() {
          if (parts[1].length() == 2) {
            return parts[1];
          }
          return parts[1].split("_")[0]; //en as in en_US
        }

        @Override
        public String[] getCountries() {
          if (parts[1].length() == 2) {
            return new String[]{""};
          }
          return new String[]{parts[1].split("_")[1]}; //US as in en_US
        }

        @Override
        public String getName() {
          return parts[2].replace(".xml", "");
        }

        @Override
        public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) {
          return Collections.emptyList();
        }

        @Override
        public List<String> getRuleFileNames() {
          List<String> ruleFiles = new ArrayList<>();
          ruleFiles.add(file.getAbsolutePath());
          return ruleFiles;
        }

        @Override
        public boolean isExternal() {
          return isAdditional;
        }
      };
    }
    return newLanguage;
  }

  static class ExtendedLanguage extends Language {

    private final Language baseLanguage;
    private final String name;
    private final File ruleFile;

    ExtendedLanguage(Language baseLanguage, String name, File ruleFile) {
      this.baseLanguage = baseLanguage;
      this.name = name;
      this.ruleFile = ruleFile;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public List<String> getRuleFileNames() {
      List<String> ruleFiles = new ArrayList<>();
      ruleFiles.addAll(baseLanguage.getRuleFileNames());
      ruleFiles.add(ruleFile.getAbsolutePath());
      return ruleFiles;
    }

    @Override
    public boolean isExternal() {
      return true;
    }

    @Override
    public Locale getLocale() {
      return baseLanguage.getLocale();
    }

    @Override
    public Contributor[] getMaintainers() {
      return baseLanguage.getMaintainers();
    }

    @Override
    public String getShortCode() {
      return baseLanguage.getShortCode();
    }

    @Override
    public String[] getCountries() {
      return baseLanguage.getCountries();
    }

    @Override
    public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
      return baseLanguage.getRelevantRules(messages, null, motherTongue, altLanguages);
    }

    @Nullable @Override
    public String getVariant() {
      return baseLanguage.getVariant();
    }

    @Override
    public List<String> getDefaultEnabledRulesForVariant() {
      return baseLanguage.getDefaultEnabledRulesForVariant();
    }

    @Override
    public List<String> getDefaultDisabledRulesForVariant() {
      return baseLanguage.getDefaultDisabledRulesForVariant();
    }

    @Nullable
    @Override
    public LanguageModel getLanguageModel(File indexDir) throws IOException {
      return baseLanguage.getLanguageModel(indexDir);
    }

    @Override
    public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
      return baseLanguage.getRelevantLanguageModelRules(messages, languageModel, userConfig);
    }

    @Override
    public Locale getLocaleWithCountryAndVariant() {
      return baseLanguage.getLocaleWithCountryAndVariant();
    }

    @Nullable
    @Override
    public Language getDefaultLanguageVariant() {
      return baseLanguage.getDefaultLanguageVariant();
    }

    @Override
    public Disambiguator createDefaultDisambiguator() {
      return baseLanguage.createDefaultDisambiguator();
    }

    @NotNull
    @Override
    public Tagger createDefaultTagger() {
      return baseLanguage.createDefaultTagger();
    }

    @Override
    public SentenceTokenizer createDefaultSentenceTokenizer() {
      return baseLanguage.createDefaultSentenceTokenizer();
    }

    @Override
    public Tokenizer createDefaultWordTokenizer() {
      return baseLanguage.createDefaultWordTokenizer();
    }

    @Nullable
    @Override
    public Chunker createDefaultChunker() {
      return baseLanguage.createDefaultChunker();
    }

    @Nullable
    @Override
    public Chunker createDefaultPostDisambiguationChunker() {
      return baseLanguage.createDefaultPostDisambiguationChunker();
    }

    @Nullable
    @Override
    public Synthesizer createDefaultSynthesizer() {
      return baseLanguage.createDefaultSynthesizer();
    }

  }
}
