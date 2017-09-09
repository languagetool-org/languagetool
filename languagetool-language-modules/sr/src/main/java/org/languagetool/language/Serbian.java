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

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.LanguageMaintainedState;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.*;
import org.languagetool.rules.sr.MorfologikSerbianSpellerRule;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Support for Serbian language
 */
public class Serbian extends Language implements AutoCloseable {

  private SentenceTokenizer sentenceTokenizer;
  private static final Language SERBIA_SERBIAN = new SerbiaSerbian();
  
  // Grammar rules distributed over multiple .XML files
  // We want to keep our rules small and tidy.
  // TODO: Make names based on rules that will reside in these files
  private static final List<String> RULE_FILES = Arrays.asList(
          "grammar-spelling.xml",
          "grammar-grammar.xml",
          "grammar-barbarism.xml",
          "grammar-style.xml",
          "grammar-punctuation.xml"
  );

  public Serbian() {
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
    return new Contributor[]{new Contributor("Золтан Чала (Csala Zoltán)")};
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
            // TODO: Add Serbian-specific rules
            new MorfologikSerbianSpellerRule(messages, this)
    );
  }

  @Override
  public List<String> getRuleFileNames() {
    List<String> ruleFileNames = super.getRuleFileNames();
    // Load all grammar*.xml files
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    String dirBase = dataBroker.getRulesDir() + "/" + getShortCode() + "/";
    for (final String ruleFile : RULE_FILES) {
      if (dataBroker.ruleFileExists(ruleFile)) {
        ruleFileNames.add(dirBase + ruleFile);
      }
    }
    return ruleFileNames;
  }

  @Override
  public void close() throws Exception {
  }
}
