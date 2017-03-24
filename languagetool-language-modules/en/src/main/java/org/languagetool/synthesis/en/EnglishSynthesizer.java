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
package org.languagetool.synthesis.en;

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
import org.languagetool.rules.en.AvsAnRule;
import org.languagetool.synthesis.BaseSynthesizer;

/**
 * English word form synthesizer.
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

  // A special tag to add determiners.
  private static final String ADD_DETERMINER = "+DT";

  // A special tag to add only indefinite articles.
  private static final String ADD_IND_DETERMINER = "+INDT";

  private final AvsAnRule aVsAnRule = new AvsAnRule(JLanguageTool.getMessageBundle(Languages.getLanguageForShortCode("en")));

  public EnglishSynthesizer() {
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
    String aOrAn = aVsAnRule.suggestAorAn(token.getToken());
    if (ADD_DETERMINER.equals(posTag)) {
      return new String[] { aOrAn, "the " + token.getToken() };
    } else if (ADD_IND_DETERMINER.equals(posTag)) {
      return new String[] { aOrAn };
    }
    IStemmer synthesizer = createStemmer();
    List<WordData> wordData = synthesizer.lookup(token.getLemma() + "|" + posTag);
    List<String> wordForms = new ArrayList<>();
    for (WordData wd : wordData) {
      wordForms.add(wd.getStem().toString());
    }
    return wordForms.toArray(new String[wordForms.size()]);
  }

  /**
   * Special English regexp based synthesizer that allows adding articles
   * when the regexp-based tag ends with a special signature {@code \\+INDT} or {@code \\+DT}.
   * 
   * @since 2.5
   */
  @Override
  public String[] synthesize(AnalyzedToken token, String posTag,
      boolean posTagRegExp) throws IOException {

    if (posTag != null && posTagRegExp) {
      String myPosTag = posTag;
      String det = "";
      if (posTag.endsWith(ADD_IND_DETERMINER)) {
        myPosTag = myPosTag.substring(0, myPosTag.indexOf(ADD_IND_DETERMINER) - "\\".length());
        det = aVsAnRule.suggestAorAn(token.getLemma());
        det = det.substring(0, det.indexOf(' ') + " ".length());
      } else if (posTag.endsWith(ADD_DETERMINER)) {
        myPosTag = myPosTag.substring(0, myPosTag.indexOf(ADD_DETERMINER) - "\\".length());
        det = "the ";
      }

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


