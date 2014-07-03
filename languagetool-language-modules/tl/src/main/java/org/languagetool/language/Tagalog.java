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
import org.languagetool.rules.*;
import org.languagetool.rules.spelling.hunspell.HunspellRule;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.tl.TagalogTagger;
import org.languagetool.tokenizers.SRXSentenceTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;

/** 
 * @author Nathaniel Oco
 */
public class Tagalog extends Language {

  private SentenceTokenizer sentenceTokenizer;
  private Tagger tagger;
  private String name = "Tagalog";

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
    return "tl";
  }

  @Override
  public String[] getCountries() {
    return new String[] {"PH"};
  }

  @Override
  public final SentenceTokenizer getSentenceTokenizer() {
    if (sentenceTokenizer == null) {
      sentenceTokenizer = new SRXSentenceTokenizer(this);
    }
    return sentenceTokenizer;
  }

  @Override
  public Tagger getTagger() {
    if (tagger == null) {
      tagger = new TagalogTagger();
    }
    return tagger;
  }

  @Override
  public Contributor[] getMaintainers() {
    final Contributor contributor1 = new Contributor("Nathaniel Oco");
    contributor1.setUrl("http://www.dlsu.edu.ph/research/centers/adric/nlp/");
    final Contributor contributor2 = new Contributor("Allan Borra");
    contributor2.setUrl("http://www.dlsu.edu.ph/research/centers/adric/nlp/faculty/borra.asp");
    return new Contributor[] { contributor1, contributor2 };
  }

  @Override
  public List<Class<? extends Rule>> getRelevantRules() {
    return Arrays.asList(
            CommaWhitespaceRule.class,
            DoublePunctuationRule.class,
            GenericUnpairedBracketsRule.class,
            HunspellRule.class,
            UppercaseSentenceStartRule.class,
            MultipleWhitespaceRule.class
    );
  }

}
