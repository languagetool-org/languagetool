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
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Rule;
import org.languagetool.rules.sr.ekavian.SimpleGrammarEkavianReplaceRule;
import org.languagetool.rules.sr.ekavian.MorfologikEkavianSpellerRule;
import org.languagetool.rules.sr.ekavian.SimpleStyleEkavianReplaceRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.sr.EkavianSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.sr.EkavianTagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Support for Serbian language spoken in Serbia
 *
 * @author Zoltán Csala
 *
 * @since 4.0
 */
public class SerbianSerbian extends Serbian {

  private Tagger tagger;
  private Synthesizer synthesizer;

  // Grammar rules distributed over multiple .XML files
  // We want to keep our rules small and tidy.
  // Rules specific to dialect
  private static final List<String> RULE_FILES = Arrays.asList(
          "grammar.xml"
  );

  @Override
  public String[] getCountries() {
    return new String[]{"RS"};
  }

  @Override
  public String getName() {
    return "Serbian (Serbia)";
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new EkavianTagger();
    }
    return tagger;
  }

  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new EkavianSynthesizer();
    }
    return synthesizer;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages) throws IOException {
    List<Rule> rules = new ArrayList<>();
    rules.addAll(super.getRelevantRules(messages));
    rules.add(new MorfologikEkavianSpellerRule(messages, this));
    rules.add(new SimpleGrammarEkavianReplaceRule(messages));
    rules.add(new SimpleStyleEkavianReplaceRule(messages));
    return rules;
  }

  @Override
  public List<String> getRuleFileNames() {
    List<String> ruleFileNames = super.getRuleFileNames();
    // Load all grammar*.xml files
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    final String shortCode = getShortCode();
    final String dirBase = dataBroker.getRulesDir();

    for (final String ruleFile : RULE_FILES) {
      final String rulePath = shortCode + "/ekavian/" + ruleFile;
      if (dataBroker.ruleFileExists(rulePath)) {
        ruleFileNames.add(dirBase + "/" + rulePath);
      } else {
        System.out.println("Skipping file " + dirBase + "/" + rulePath);
      }
    }
    return ruleFileNames;
  }
}
