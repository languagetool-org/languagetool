/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.AbstractFindSuggestionsFilter;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tagging.ca.CatalanTagger;

public class FindSuggestionsFilter extends AbstractFindSuggestionsFilter {

  protected static final String DICT_FILENAME = "/ca/ca-ES.dict";
  protected static MorfologikSpeller speller;
  /* lemma exceptions */
  public static final String[] LemmasToIgnore =  new String[] {"enterar", "sentar", "conseguir", "alcançar"};
  public static final String[] LemmasToAllow =  new String[] {"enter", "sentir"};
  
  public FindSuggestionsFilter() throws IOException {
    // lazy init
    if (speller == null) {
      if (JLanguageTool.getDataBroker().resourceExists(DICT_FILENAME)) {
        speller = new MorfologikSpeller(DICT_FILENAME);
      }
    }
  }

  @Override
  protected Tagger getTagger() {
    return CatalanTagger.INSTANCE_CAT;
  }

  @Override
  protected List<String> getSpellingSuggestions(AnalyzedTokenReadings atr) throws IOException {
    return speller.findSimilarWords(atr.getToken());
  }

  @Override
  protected boolean isSuggestionException(AnalyzedTokenReadings analyzedSuggestion) {
    return analyzedSuggestion.hasAnyLemma(LemmasToIgnore) && !analyzedSuggestion.hasAnyLemma(LemmasToAllow);
  };

  private static final Pattern ELA_GEMINADA = Pattern.compile("(l)[\\.\u2022\u22C5\u2219\uF0D7\\-](l)",Pattern.CASE_INSENSITIVE);

  @Override
  protected String preProcessWrongWord (String word) {
    word = word.replace(" ","");
    word = ELA_GEMINADA.matcher(word).replaceAll("$1·$2");
    return word;
  }
  
//  @Override
//  protected Synthesizer getSynthesizer() {
//    return CatalanSynthesizer.INSTANCE;
//  }

}
