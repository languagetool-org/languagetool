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
package org.languagetool.synthesis.ar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

import org.languagetool.AnalyzedToken;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
//import org.languagetool.rules.ar.GenderRule;
import org.languagetool.synthesis.BaseSynthesizer;

/**
 * Arabic word form synthesizer.
 * Based on part-of-speech lists in Public Domain. See readme.txt for details,
 * the POS tagset is described in tagset.txt.
 * 
 * There are two special additions:
 * <ol>
 * <li>+GF - tag that adds  feminine gender to word</li>
 * <li>+GM - a tag that adds masculine gender to word</li>
 * </ol>
 * 
 * @author Taha Zerrouki
 */
public class ArabicSynthesizer extends BaseSynthesizer {

  private static final String RESOURCE_FILENAME = "/ar/arabic_synth.dict";
  private static final String TAGS_FILE_NAME = "/ar/arabic_tags.txt";

  public ArabicSynthesizer() {
    super(RESOURCE_FILENAME, TAGS_FILE_NAME);
  }

  /**
   * Get a form of a given AnalyzedToken, where the form is defined by a
   * part-of-speech tag.
   * 
   * @param token AnalyzedToken to be inflected.
   * @param posTag A desired part-of-speech tag.
   * @return String value - inflected word.
   */
  @Override
  public String[] synthesize(AnalyzedToken token, String posTag)
      throws IOException {
    /*String mOrF = genderRule.suggestMorF(token.getToken());
    if (ADD_FEMININ_DETERMINER.equals(posTag)) {
      return new String[] { mOrF, "the " + token.getToken() };
    } else if (ADD_MASCULIN_DETERMINER.equals(posTag)) {
      return new String[] { mOrF };
    }*/
    IStemmer synthesizer = createStemmer();
    List<WordData> wordData = synthesizer.lookup(token.getLemma() + "|" + posTag);
    List<String> wordForms = new ArrayList<>();
    for (WordData wd : wordData) {
      wordForms.add(wd.getStem().toString());
    }
    return wordForms.toArray(new String[wordForms.size()]);
  }

  /**
   * Special Arabic regexp based synthesizer that allows adding articles
   * when the regexp-based tag ends with a special signature {@code \\+GM} or {@code \\+GF}.
   * 
   * @since 2.5
   */
  @Override
  public String[] synthesize(AnalyzedToken token, String posTag,
      boolean posTagRegExp) throws IOException {

    if (posTag != null && posTagRegExp) {
      String myPosTag = posTag;
      String det = "";
 /*     if (posTag.endsWith(ADD_MASCULIN_DETERMINER)) {
        myPosTag = myPosTag.substring(0, myPosTag.indexOf(ADD_MASCULIN_DETERMINER) - "\\".length());
        det = genderRule.suggestMOrF(token.getLemma());
        det = det.substring(0, det.indexOf(' ') + " ".length());
      } else if (posTag.endsWith(ADD_FEMININ_DETERMINER)) {
        myPosTag = myPosTag.substring(0, myPosTag.indexOf(ADD_FEMININ_DETERMINER) - "\\".length());
        det = "the ";
      }
*/
      initPossibleTags();
      Pattern p = Pattern.compile(myPosTag);
      List<String> results = new ArrayList<>();

      for (String tag : possibleTags) {
        Matcher m = p.matcher(tag);
        if (m.matches()) {
          lookup(token.getLemma(), tag, results, det);
        }
      }
      return results.toArray(new String[results.size()]);
    }

    return synthesize(token, posTag);
  }

  private void lookup(String lemma, String posTag, List<String> results, String determiner) {
    synchronized (this) { // the stemmer is not thread-safe
      List<WordData> wordForms = getStemmer().lookup(lemma + "|" + posTag);
      for (WordData wd : wordForms) {
        results.add(determiner + wd.getStem());
      }
    }
  }

}


