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

package de.danielnaber.languagetool.language;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.*;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tagging.disambiguation.Disambiguator;
import de.danielnaber.languagetool.tagging.disambiguation.rules.br.BretonRuleDisambiguator;
import de.danielnaber.languagetool.tokenizers.Tokenizer;
import de.danielnaber.languagetool.tokenizers.br.BretonWordTokenizer;
import de.danielnaber.languagetool.tagging.br.BretonTagger;
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
    return new Contributor[] { Contributors.DOMINIQUE_PELLE };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            UppercaseSentenceStartRule.class,
            WhitespaceRule.class
    );
  }

}
