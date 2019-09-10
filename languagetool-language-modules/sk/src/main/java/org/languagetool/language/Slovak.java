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

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.*;
import org.languagetool.rules.sk.CompoundRule;
import org.languagetool.rules.sk.MorfologikSlovakSpellerRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.sk.SlovakSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.sk.SlovakTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

public class Slovak extends Language {

  private static final List<String> RULE_FILES = Arrays.asList(
    "grammar-typography.xml"
  );

  private Tagger tagger;
  private SentenceTokenizer sentenceTokenizer;
  private Synthesizer synthesizer;

  @Override
  public String getName() {
    return "Slovak";
  }

  @Override
  public String getShortCode() {
    return "sk";
  }
  
  @Override
  public String[] getCountries() {
    return new String[]{"SK"};
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new SlovakTagger();
    }
    return tagger;
  }

  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new SlovakSynthesizer(this);
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
  public Contributor[] getMaintainers() {
    return new Contributor[] {
            new Contributor("Zdenko Podobný", "http://sk-spell.sk.cx")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule(messages,
                    Arrays.asList("[", "(", "{", "„", "»", "«", "\""),
                    Arrays.asList("]", ")", "}", "“", "«", "»", "\"")),
            new UppercaseSentenceStartRule(messages, this),
            new WordRepeatRule(messages, this),
            new MultipleWhitespaceRule(messages, this),
            // specific to Slovak:
            new CompoundRule(messages),
            new MorfologikSlovakSpellerRule(messages, this, userConfig, altLanguages)
            //new SlovakVesRule(messages)
    );
  }

  @Override
  public List<String> getRuleFileNames() {
    List<String> ruleFileNames = super.getRuleFileNames();
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    String dirBase = dataBroker.getRulesDir() + "/" + getShortCode() + "/";
    for (String ruleFile : RULE_FILES) {
      ruleFileNames.add(dirBase + ruleFile);
    }
    return ruleFileNames;
  }

}
