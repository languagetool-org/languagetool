/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.*;
import org.languagetool.rules.sr.MorfologikSerbianSpellerRule;
import org.languagetool.rules.sr.SimpleGrammarReplaceRule;
import org.languagetool.rules.sr.SimpleStyleReplaceRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.sr.SerbianSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.sr.SerbianTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Support for Serbian language
 * @since 4.0
 */
public class Serbian extends Language {

  private static final Language SERBIA_SERBIAN = new SerbiaSerbian();
  
  private SentenceTokenizer sentenceTokenizer;
  private Tagger tagger;
  private Synthesizer synthesizer;

  // Grammar rules distributed over multiple .XML files
  // We want to keep our rules small and tidy.
  // TODO: Make names based on rules that will reside in these files
  private static final List<String> RULE_FILES = Arrays.asList(
          // grammar.xml will be added "by default" by method "getRuleFileNames"
          "grammar-barbarism.xml",
          "grammar-logical.xml",
          "grammar-punctuation.xml",
          "grammar-spelling.xml",
          "grammar-style.xml"
  );

  public Serbian() {
  }

  @Override
  public String getName() {
    return "Serbian";
  }

  @Override
  public String getShortCode() {
    return "sr";
  }

  @Override
  public String[] getCountries() {
    return new String[]{};
  }

  @Override
  public Language getDefaultLanguageVariant() {
    return SERBIA_SERBIAN;
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[]{
            new Contributor("Золтан Чала (Csala Zoltán)")
    };
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new SerbianTagger();
    }
    return tagger;
  }

  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new SerbianSynthesizer();
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
  public LanguageMaintainedState getMaintainedState() {
    return LanguageMaintainedState.ActivelyMaintained;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages)
          throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages,
                    Example.wrong("Није шија<marker> ,</marker> него врат."),
                    Example.fixed("Није шија<marker>,</marker> него врат.")),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule(messages,
                    Arrays.asList("[", "(", "{", "„", "„", "\""),
                    Arrays.asList("]", ")", "}", "”", "“", "\"")),
            new UppercaseSentenceStartRule(messages, this,
                    Example.wrong("Почела је школа. <marker>деца</marker> су поново села у клупе."),
                    Example.fixed("Почела је школа. <marker>Деца</marker> су поново села у клупе.")),
            new MultipleWhitespaceRule(messages, this),
            new SentenceWhitespaceRule(messages),
            new WordRepeatRule(messages, this),
            // Serbian-specific rules
            new MorfologikSerbianSpellerRule(messages, this),
            new SimpleGrammarReplaceRule(messages),
            new SimpleStyleReplaceRule(messages)
    );
  }

  @Override
  public List<String> getRuleFileNames() {
    List<String> ruleFileNames = super.getRuleFileNames();
    // Load all grammar*.xml files
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    final String shortCode = getShortCode();
    final String dirBase = dataBroker.getRulesDir();

    for (final String ruleFile : RULE_FILES) {
      final String rulePath = shortCode + "/" + ruleFile;
      if (dataBroker.ruleFileExists(rulePath)) {
        ruleFileNames.add(dirBase + "/" + rulePath);
      }
    }
    return ruleFileNames;
  }

}
