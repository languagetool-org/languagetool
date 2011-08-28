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
package de.danielnaber.languagetool.language;

import java.util.*;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.*;
import de.danielnaber.languagetool.rules.patterns.Unifier;
import de.danielnaber.languagetool.rules.ru.RussianSimpleReplaceRule;
import de.danielnaber.languagetool.rules.ru.RussianCompoundRule;
import de.danielnaber.languagetool.rules.ru.RussianUnpairedBracketsRule;
import de.danielnaber.languagetool.synthesis.Synthesizer;
import de.danielnaber.languagetool.synthesis.ru.RussianSynthesizer;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.rules.ru.RussianRuleDisambiguator;
import de.danielnaber.languagetool.tagging.ru.RussianTagger;
import de.danielnaber.languagetool.tokenizers.SRXSentenceTokenizer;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

public class Russian extends Language {

  private static final Unifier RUSSIAN_UNIFIER = new Unifier();

  private Tagger tagger;
  private Disambiguator disambiguator;
  private Synthesizer synthesizer;
  private SentenceTokenizer sentenceTokenizer;

  @Override
  public Locale getLocale() {
    return new Locale(getShortName());
  }

  @Override
  public String getName() {
    return "Russian";
  }

  @Override
  public String getShortName() {
    return "ru";
  }

  @Override
  public String[] getCountryVariants() {
    return new String[] {"RU"};
  }
  
  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new RussianTagger();
    }
    return tagger;
  }

  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new RussianRuleDisambiguator();
    }
    return disambiguator;
  }
  
  @Override
  public Synthesizer getSynthesizer() {
    if (synthesizer == null) {
      synthesizer = new RussianSynthesizer();
    }
    return synthesizer;
  }

  @Override
  public SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
       sentenceTokenizer = new SRXSentenceTokenizer(getShortName());
    }
    return sentenceTokenizer;
  }

  @Override
  public Unifier getUnifier() {
    return RUSSIAN_UNIFIER;
  }

  @Override
  public Contributor[] getMaintainers() {
    final Contributor contributor = new Contributor("Yakov Reztsov");
    contributor.setUrl("http://myooo.ru/content/view/83/43/");
    return new Contributor[] { contributor };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            UppercaseSentenceStartRule.class,
            WordRepeatRule.class,
            WhitespaceRule.class,
            // specific to Russian :
            RussianUnpairedBracketsRule.class,
            RussianCompoundRule.class,
            RussianSimpleReplaceRule.class
    );
  }

}