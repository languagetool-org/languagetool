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

import java.util.Arrays;
import java.util.List;

import org.languagetool.Language;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.DoublePunctuationRule;
import org.languagetool.rules.GenericUnpairedBracketsRule;
import org.languagetool.rules.Rule;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.WhitespaceRule;
import org.languagetool.rules.fr.QuestionWhitespaceRule;
import org.languagetool.rules.spelling.hunspell.HunspellNoSuggestionRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.fr.FrenchHybridDisambiguator;
import org.languagetool.tagging.fr.FrenchTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

public class French extends Language {

  private SentenceTokenizer sentenceTokenizer;
  private Tagger tagger;
  private Disambiguator disambiguator;

  @Override
  public final SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public String getName() {
    return "French";
  }

  @Override
  public String getShortName() {
    return "fr";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"FR", "", "BE", "CH", "CA", "LU", "MC", "CM",
            "CI", "HI", "ML", "SN", "CD", "MA", "RE"};
  }

  @Override
  public String[] getUnpairedRuleStartSymbols() {
    return new String[]{ "[", "(", "{", /*"«", "‘"*/ };
  }

  @Override
  public String[] getUnpairedRuleEndSymbols() {
    return new String[]{ "]", ")", "}",
                         /*"»", French dialog can contain multiple sentences. */
                         /*"’" used in "d’arm" and many other words */ };
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new FrenchTagger();
    }
    return tagger;
  }

  @Override
  public Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new FrenchHybridDisambiguator();
    }
    return disambiguator;
  }

  @Override
  public Contributor[] getMaintainers() {
    final Contributor hVoisard = new Contributor("Hugo Voisard");
    hVoisard.setRemark("2006-2007");
    return new Contributor[] {
        Contributors.DOMINIQUE_PELLE,
        new Contributor("Agnes Souque"),
        hVoisard
    };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            GenericUnpairedBracketsRule.class,
            HunspellNoSuggestionRule.class,
            UppercaseSentenceStartRule.class,
            WhitespaceRule.class,
            // specific to French:
            QuestionWhitespaceRule.class
    );
  }

}
