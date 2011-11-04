/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.synthesis.en;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.rules.en.AvsAnRule;
import de.danielnaber.languagetool.synthesis.BaseSynthesizer;

/**
 * English word form synthesizer. <br/>
 * Based on part-of-speech lists in Public Domain. See readme.txt for details,
 * the POS tagset is described in tagset.txt.
 * 
 * There are to special additions:
 * <ol>
 * <li>+DT - tag that adds "a" or "an" (according to the way the word is
 * pronounced) and "the"</li>
 * <li>+INDT - a tag that adds only "a" or "an"</li>
 * </ol>
 * 
 * @author Marcin Miłkowski
 */

public class EnglishSynthesizer extends BaseSynthesizer {

  private static final String RESOURCE_FILENAME = "/en/english_synth.dict";

  private static final String TAGS_FILE_NAME = "/en/english_tags.txt";

  /** A special tag to add determiners. **/
  private static final String ADD_DETERMINER = "+DT";

  /** A special tag to add only indefinite articles. **/
  private static final String ADD_IND_DETERMINER = "+INDT";
  
  public EnglishSynthesizer() {
    super(JLanguageTool.getDataBroker().getResourceDir() + RESOURCE_FILENAME, 
    		JLanguageTool.getDataBroker().getResourceDir() + TAGS_FILE_NAME);
  }

  /**
   * Get a form of a given AnalyzedToken, where the form is defined by a
   * part-of-speech tag.
   * 
   * @param token
   *          AnalyzedToken to be inflected.
   * @param posTag
   *          A desired part-of-speech tag.
   * @return String value - inflected word.
   */
  @Override
  public String[] synthesize(final AnalyzedToken token, final String posTag)
      throws IOException {
    if (ADD_DETERMINER.equals(posTag)) {
      final AvsAnRule rule = new AvsAnRule(null);
      return new String[] { rule.suggestAorAn(token.getToken()),
          "the " + token.getToken() };
    } else if (ADD_IND_DETERMINER.equals(posTag)) {
      final AvsAnRule rule = new AvsAnRule(null);
      return new String[] { rule.suggestAorAn(token.getToken()) };
    } else {
      if (synthesizer == null) {
        final URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(RESOURCE_FILENAME);
        synthesizer = new DictionaryLookup(Dictionary.read(url));
      }
      final List<WordData> wordData = synthesizer.lookup(token.getLemma() + "|" + posTag);
      final List<String> wordForms = new ArrayList<String>();
      for (WordData wd : wordData) {
        wordForms.add(wd.getStem().toString());
      }
      return wordForms.toArray(new String[wordForms.size()]);
    }
  }  

}
