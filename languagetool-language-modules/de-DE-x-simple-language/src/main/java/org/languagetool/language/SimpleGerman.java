/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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

import org.jetbrains.annotations.Nullable;
import org.languagetool.GlobalConfig;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.JLanguageTool;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.de.LongSentenceRule;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Simple German (see e.g. <a href="https://de.wikipedia.org/wiki/Leichte_Sprache">Wikipedia</a>)
 * that only support rules specific to this variant, not the other German rules.
 */
public class SimpleGerman extends GermanyGerman {

  @Override
  public boolean isVariant() {
    return true;
  }

  @Override
  public String getName() {
    return "Simple German";
  }

  @Override
  public String getShortCode() {
    return "de-DE-x-simple-language";  // a "private use tag" according to http://tools.ietf.org/html/bcp47
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[] {
        new Contributor("Annika Nietzio")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>();
    LongSentenceRule lengthRule = new LongSentenceRule(messages, userConfig, 12);
    rules.add(lengthRule);
    return rules;
  }
  
  @Override
  public LanguageModel getLanguageModel(File indexDir) throws IOException {
    return null;
  }

  @Override
  public List<Rule> getRelevantLanguageModelRules(ResourceBundle messages, LanguageModel languageModel, UserConfig userConfig) throws IOException {
    return Collections.emptyList();
  }

  @Override
  public List<Rule> getRelevantLanguageModelCapableRules(ResourceBundle messages, @Nullable LanguageModel languageModel, GlobalConfig globalConfig, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Collections.emptyList();
  }

  @Override
  protected int getPriorityForId(String id) {
    if (id.equals("TOO_LONG_SENTENCE")) {
      return 10;
    } else if (id.equals("LANGES_WORT")) {
      return -1;
    }
    return super.getPriorityForId(id);
  }

  @Override
  public List<String> getRuleFileNames() {
    List<String> ruleFileNames = new ArrayList<>();
    String dirBase = JLanguageTool.getDataBroker().getRulesDir() + "/" + getShortCode() + "/";
    ruleFileNames.add(dirBase + "grammar.xml");
    return ruleFileNames;
  }

}
