/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.rules.Rule;
import org.languagetool.rules.SentenceWhitespaceRule;
import org.languagetool.rules.UppercaseSentenceStartRule;
import org.languagetool.rules.WhitespaceRule;
import org.languagetool.rules.br.TopoReplaceRule;
import org.languagetool.rules.br.MorfologikBretonSpellerRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.br.BretonTagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.XmlRuleDisambiguator;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.br.BretonWordTokenizer;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

/** 
 * @author Dominique Pelle
 */
public class Breton extends Language {

  private SentenceTokenizer sentenceTokenizer;
  private Tagger tagger;
  private Tokenizer wordTokenizer;
  private Disambiguator disambiguator;
  private String name = "Breton";

  @Override
  public final SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public final Tokenizer getWordTokenizer() {
    if (wordTokenizer == null) {
      wordTokenizer = new BretonWordTokenizer();
    }
    return wordTokenizer;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(final String name) {
    this.name = name;
  }

  @Override
  public String getShortName() {
    return "br";
  }

  @Override
  public String[] getCountries() {
    return new String[] {"FR"};
  }
  
  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new BretonTagger();
    }
    return tagger;
  }

  @Override
  public final Disambiguator getDisambiguator() {
    if (disambiguator == null) {
      disambiguator = new XmlRuleDisambiguator(new Breton());
    }
    return disambiguator;
  }

  @Override
  public Contributor[] getMaintainers() {
    final Contributor contributorFulup = new Contributor("Fulup Jakez");
    return new Contributor[] {
        Contributors.DOMINIQUE_PELLE, contributorFulup
    };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            MorfologikBretonSpellerRule.class,
            UppercaseSentenceStartRule.class,
            WhitespaceRule.class,
            SentenceWhitespaceRule.class,
            TopoReplaceRule.class
    );
  }

}
