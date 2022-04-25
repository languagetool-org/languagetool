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

import org.languagetool.*;
import org.languagetool.rules.en.AvsAnRule;
import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  private static final String SOR_FILE_NAME = "/en/en.sor";
  
  private static final List<String> exceptions = Arrays.asList("ne'er", "e'er", "o'er", "ol'", "ma'am", "n't", "informations");

  // A special tag to add determiners.
  private static final String ADD_DETERMINER = "+DT";

  // A special tag to add only indefinite articles.
  private static final String ADD_IND_DETERMINER = "+INDT";

  private final AvsAnRule aVsAnRule = new AvsAnRule(JLanguageTool.getMessageBundle(Languages.getLanguageForShortCode("en")));

  public EnglishSynthesizer(Language lang) {
    super(SOR_FILE_NAME, RESOURCE_FILENAME, TAGS_FILE_NAME, lang);
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
  public String[] synthesize(AnalyzedToken token, String posTag) throws IOException {
    if (posTag.startsWith(SPELLNUMBER_TAG)) {
      return super.synthesize(token, posTag);
    }
    String aOrAn = aVsAnRule.suggestAorAn(token.getToken());
    if (ADD_DETERMINER.equals(posTag)) {
      return new String[] { aOrAn, "the " + StringTools.lowercaseFirstCharIfCapitalized(token.getToken()) };
    } else if (ADD_IND_DETERMINER.equals(posTag)) {
      return new String[] { aOrAn };
    }
    return removeExceptions(super.synthesize(token, posTag));
  }

  /**
   * Special English regexp based synthesizer that allows adding articles
   * when the regexp-based tag ends with a special signature {@code \\+INDT} or {@code \\+DT}.
   * 
   * @since 2.5
   */
  @Override
  public String[] synthesize(AnalyzedToken token, String posTag, boolean posTagRegExp) throws IOException {
    if (posTag.startsWith(SPELLNUMBER_TAG)) {
      return synthesize(token, posTag);
    }
    if (posTagRegExp) {
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
        if (m.matches() && token.getLemma() != null) {
          lookup(token.getLemma(), tag, results, det);
        }
      }
      return removeExceptions(results.toArray(new String[0]));
    }
    return removeExceptions(synthesize(token, posTag));
  }

  private void lookup(String lemma, String posTag, List<String> results, String determiner) {
    List<String> lookup = super.lookup(lemma, posTag);
    for (String result : lookup) {
      //results.add(determiner + StringTools.lowercaseFirstCharIfCapitalized(result)); //why lowercase?
      results.add(determiner + result);
    }
  }
  
  @Override
  protected boolean isException(String w) {
    // remove: 've, 's, 're...
    return w.startsWith("'") || exceptions.contains(w);  
  }



}


