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


import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.sr.jekavian.MorfologikJekavianSpellerRule;
import org.languagetool.rules.sr.jekavian.SimpleGrammarJekavianReplaceRule;
import org.languagetool.rules.sr.jekavian.SimpleStyleJekavianReplaceRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.sr.JekavianSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.sr.JekavianTagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * Class modelling Serbian Jekavian dialect
 *
 * @since 4.0
 */
public class JekavianSerbian extends Serbian {

  private Synthesizer synthesizer;
  private Tagger tagger;

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new JekavianTagger();
    }
    return tagger;
  }

  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new JekavianSynthesizer();
    }
    return synthesizer;
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, List<Language> altLanguages) throws IOException {
    List<Rule> rules = new ArrayList<>(getBasicRules(messages));
    // Rules specific for Jekavian Serbian
    rules.add(new MorfologikJekavianSpellerRule(messages, this, userConfig, altLanguages));
    rules.add(new SimpleGrammarJekavianReplaceRule(messages));
    rules.add(new SimpleStyleJekavianReplaceRule(messages));
    return rules;
  }
}
