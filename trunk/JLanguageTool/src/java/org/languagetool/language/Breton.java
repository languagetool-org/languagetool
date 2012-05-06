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
import org.languagetool.Language;
import org.languagetool.rules.*;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.disambiguation.Disambiguator;
import org.languagetool.tagging.disambiguation.rules.br.BretonRuleDisambiguator;
import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.br.BretonWordTokenizer;
import org.languagetool.tagging.br.BretonTagger;
import org.languagetool.rules.br.TopoReplaceRule;
import java.util.*;

/** 
 * @author Dominique Pelle
 */
public class Breton extends Language {

  private Tagger tagger;
  private Tokenizer wordTokenizer;
  private Disambiguator disambiguator;

  @Override
  public Locale getLocale() {
    return new Locale("br");
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
    return "Breton";
  }

  @Override
  public String getShortName() {
    return "br";
  }

  @Override
  public String[] getCountryVariants() {
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
      disambiguator = new BretonRuleDisambiguator();
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
            UppercaseSentenceStartRule.class,
            WhitespaceRule.class,
            TopoReplaceRule.class
    );
  }

}
