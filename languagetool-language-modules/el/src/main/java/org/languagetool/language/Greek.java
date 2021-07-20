/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.rules.el.GreekRedundancyRule;
import org.languagetool.rules.el.GreekWordRepeatBeginningRule;
import org.languagetool.rules.el.MorfologikGreekSpellerRule;
import org.languagetool.rules.el.NumeralStressRule;
import org.languagetool.rules.el.ReplaceHomonymsRule;
import org.languagetool.rules.el.GreekSpecificCaseRule;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.el.GreekSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tagging.el.GreekTagger;
import org.languagetool.tokenizers.*;
import org.languagetool.tokenizers.el.GreekWordTokenizer;

import java.io.IOException;
import java.util.*;

/**
 *
 * @author Panagiotis Minos (pminos@gmail.com)
 */
public class Greek extends Language {

  @Override
  public String getShortCode() {
    return "el";
  }

  @Override
  public String getName() {
    return "Greek";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"GR"};
  }

  @Override
  public Contributor[] getMaintainers() {
    return new Contributor[]{
            new Contributor("Panagiotis Minos")
    };
  }

  @Override
  public List<Rule> getRelevantRules(ResourceBundle messages, UserConfig userConfig, Language motherTongue, List<Language> altLanguages) throws IOException {
    return Arrays.asList(
            new CommaWhitespaceRule(messages, 
                    Example.wrong("Το κόμμα χωρίζει προτάσεις<marker> ,</marker> όρους προτάσεων και φράσεις."),
                    Example.fixed("Το κόμμα χωρίζει προτάσεις<marker>,</marker> όρους προτάσεων και φράσεις.")),
            new DoublePunctuationRule(messages),
            new GenericUnpairedBracketsRule("EL_UNPAIRED_BRACKETS", messages,
                    Arrays.asList("[", "(", "{", "“", "\"", "«"),
                    Arrays.asList("]", ")", "}", "”", "\"", "»")),
            new LongSentenceRule(messages, userConfig, 50),
            new MorfologikGreekSpellerRule(messages, this, userConfig, altLanguages),
            new UppercaseSentenceStartRule(messages, this,
                    Example.wrong("Η τελεία είναι σημείο στίξης. <marker>δείχνει</marker> το τέλος μίας πρότασης."),
                    Example.fixed("Η τελεία είναι σημείο στίξης. <marker>Δείχνει</marker> το τέλος μίας πρότασης.")),
            new MultipleWhitespaceRule(messages, this),
            new GreekWordRepeatBeginningRule(messages, this),
            new WordRepeatRule(messages, this),
            new ReplaceHomonymsRule(messages, this),
            new GreekSpecificCaseRule(messages),
            new NumeralStressRule(messages),
            new GreekRedundancyRule(messages, this)
    );
  }

  @NotNull
  @Override
  public Tagger createDefaultTagger() {
    return new GreekTagger();
  }

  @Override
  public SentenceTokenizer createDefaultSentenceTokenizer() {
    return new SRXSentenceTokenizer(this);
  }

  @Override
  public Tokenizer createDefaultWordTokenizer() {
    return new GreekWordTokenizer();
  }

  @Nullable
  @Override
  public Synthesizer createDefaultSynthesizer() {
    return new GreekSynthesizer(this);
  }

  @Override
  public Disambiguator createDefaultDisambiguator() {
    return new XmlRuleDisambiguator(this);
  }

  @Override
  public LanguageMaintainedState getMaintainedState() {
		return LanguageMaintainedState.ActivelyMaintained;
	}
}
