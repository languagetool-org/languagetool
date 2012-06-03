/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà i Font
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
package org.languagetool.synthesis.ca;

import morfologik.stemming.WordData;
import org.languagetool.AnalyzedToken;
import org.languagetool.JLanguageTool;
import org.languagetool.synthesis.BaseSynthesizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Catalan word form synthesizer.
 * 
 * There is a special addition:
 * "add_DT" tag adds "el, la, l', els, les" according to the gender  
 * and the number of the word and the Catalan rules for apostrophation (l').
 *
 * @author Jaume Ortolà i Font
 */
public class CatalanSynthesizer extends BaseSynthesizer {

  private static final String RESOURCE_FILENAME = "/ca/catalan_synth.dict";
  private static final String TAGS_FILE_NAME = "/ca/catalan_tags.txt";

  /** A special tag to add determiner (el, la, l', els, les). **/
  private static final String ADD_DETERMINER = "+DT";
  
  /** Patterns for number and gender **/
  private static final Pattern pMS = Pattern.compile("(N|A.).[MC][SN].*|V.P.*[SN][MC]");
  private static final Pattern pFS = Pattern.compile("(N|A.).[FC][SN].*|V.P.*[SN][FC]");
  private static final Pattern pMP = Pattern.compile("(N|A.).[MC][PN].*|V.P.*[PN][MC]");
  private static final Pattern pFP = Pattern.compile("(N|A.).[FC][PN].*|V.P.*[PN][FC]");
  
  /** Patterns for apostrophation **/
  private static final Pattern pMascYes = Pattern.compile("h?[aeiouàèéíòóú].*");
  private static final Pattern pMascNo = Pattern.compile("h?[ui][aeioàèéóò].+");
  private static final Pattern pFemYes = Pattern.compile("h?[aeoàèéíòóú].*|h?[ui][^aeiouàèéíòóúüï]+[aeiou][ns]?|urbs");
  private static final Pattern pFemNo = Pattern.compile("host|ira|inxa");
    
  public CatalanSynthesizer() {
    super(JLanguageTool.getDataBroker().getResourceDir() + RESOURCE_FILENAME, 
    		JLanguageTool.getDataBroker().getResourceDir() + TAGS_FILE_NAME);
  }

  @Override
  public String[] synthesize(final AnalyzedToken token, final String posTag) throws IOException {
    initSynthesizer();
    initPossibleTags();
    final Pattern p;
    final boolean addDt = posTag.equals(ADD_DETERMINER);
    if (addDt) {
      p = Pattern.compile("N.*|A.*|V.P.*");
    } else {
      p = Pattern.compile(posTag);
    }
    final ArrayList<String> results = new ArrayList<String>();
    for (final String tag : possibleTags) {
      final Matcher m = p.matcher(tag);
      if (m.matches()) {
        if (addDt) {
          lookupWithEl(token.getLemma(), tag, results);
        } else {
          lookup(token.getLemma(), tag, results);
        }
      }
    }
    return results.toArray(new String[results.size()]);
  }

  /**
   * Lookup the inflected forms of a lemma defined by a part-of-speech tag.
   * Adds determiner "el" properly inflected.
   * @param lemma the lemma to be inflected.
   * @param posTag the desired part-of-speech tag.
   * @param results the list to collect the inflected forms.
   */
  private void lookupWithEl(String lemma, String posTag, List<String> results) {
    final List<WordData> wordForms = synthesizer.lookup(lemma + "|" + posTag);
    final Matcher mMS = pMS.matcher(posTag);
    final Matcher mFS = pFS.matcher(posTag);
    final Matcher mMP = pMP.matcher(posTag);
    final Matcher mFP = pFP.matcher(posTag);
    for (WordData wd : wordForms) {
      final String word = wd.getStem().toString();
    	if (mMS.matches()) {
    		final Matcher mMascYes = pMascYes.matcher(word);
    		final Matcher mMascNo = pMascNo.matcher(word);
    		if (mMascYes.matches() && !mMascNo.matches()) {
    			results.add("l'" + word);
    		}	else {
    			results.add("el " + word);
    		}
    	}
    	if (mFS.matches()) {
    		final Matcher mFemYes = pFemYes.matcher(word);
    		final Matcher mFemNo = pFemNo.matcher(word);
    		if (mFemYes.matches() && !mFemNo.matches()) {
    			results.add("l'" + word);
    		}	else {
    			results.add("la " + word);
    		}
    	}
    	if (mMP.matches()) {
    		results.add("els " + word);
    	}
    	if (mFP.matches()) {
    		results.add("les " + word);
    	}
    }
  }

}
