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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morfologik.stemmers.Lametyzator;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.rules.en.AvsAnRule;
import de.danielnaber.languagetool.synthesis.Synthesizer;

/** English word form synthesizer. <br/>
 * Based on part-of-speech lists in Public Domain.
 * See readme.txt for details, the POS tagset is
 * described in tagset.txt.
 * 
 * There are to special additions:
 * <ol>
 *  <li>+DT - tag that adds "a" or "an" (according to the
 *  way the word is pronounced) and "the"</li>
 *  <li>+INDT - a tag that adds only "a" or "an"</li>
 *  </ol>
 * 
 * @author Marcin Miłkowski
 */


public class EnglishSynthesizer implements Synthesizer {

  private static final String RESOURCE_FILENAME = "/resource/en/english_synth.dict";
  
  private static final String TAGS_FILE_NAME = "/resource/en/english_tags.txt";
  
  /** A special tag to add determiners. **/   
  private static final String ADD_DETERMINER = "+DT";

  /** A special tag to add only indefinite articles. **/   
  private static final String ADD_IND_DETERMINER = "+INDT";
  
  private Lametyzator synthesizer = null;

  private ArrayList<String> possibleTags = null;
  
  private void setFileName() {
    System.setProperty(Lametyzator.PROPERTY_NAME_LAMETYZATOR_DICTIONARY, 
        RESOURCE_FILENAME);    
  }
  /**
   * Get a form of a given AnalyzedToken, where the
   * form is defined by a part-of-speech tag.
   * @param token AnalyzedToken to be inflected.
   * @param posTag A desired part-of-speech tag.
   * @return String value - inflected word.
   */
  public String[] synthesize(final AnalyzedToken token, final String posTag) throws IOException {
    if (ADD_DETERMINER.equals(posTag)) {
      final AvsAnRule rule = new AvsAnRule(null);
      return new String[] {rule.suggestAorAn(token.getToken()), "the " + token.getToken()};
    } else if (ADD_IND_DETERMINER.equals(posTag)) { 
      final AvsAnRule rule = new AvsAnRule(null);
      return new String[] {rule.suggestAorAn(token.getToken())};
    } else {
      if (synthesizer == null) {
        setFileName();
        synthesizer = 
          new Lametyzator();
      }
      String[] wordForms = null;
      wordForms = synthesizer.stem(token.getLemma() + "|" + posTag);
      return wordForms;
    }
  }

  public String[] synthesize(final AnalyzedToken token, final String posTag, final boolean posTagRegExp)
      throws IOException {
    
    if (posTagRegExp) {
    if (possibleTags == null) {
      possibleTags = loadWords(this.getClass().getResourceAsStream(TAGS_FILE_NAME));
    }
    if (synthesizer == null) {
      setFileName();
      synthesizer = 
        new Lametyzator();
    }    
    final Pattern p = Pattern.compile(posTag);
    final ArrayList<String> results = new ArrayList<String>();
    for (final String tag : possibleTags) {
      final Matcher m = p.matcher(tag);
        if (m.matches()) {
          String[] wordForms = null;
          wordForms = synthesizer.stem(token.getLemma() + "|" + tag);
          if (wordForms != null) {
            results.addAll(Arrays.asList(wordForms));
          }
      }
    }
       return results.toArray(new String[results.size()]);    
    } else {
      return synthesize(token, posTag);
    }    
  }

  public String getPosTagCorrection(final String posTag) {
    return posTag;
  }
  
  private ArrayList<String> loadWords(final InputStream file) throws IOException {
    final ArrayList<String> set = new ArrayList<String>();
    InputStreamReader isr = null;
    BufferedReader br = null;
    try {
      isr = new InputStreamReader(file);
      br = new BufferedReader(isr);
      String line;
      
      while ((line = br.readLine()) != null) {
        line = line.trim();
        if (line.length() < 1) {
          continue;
        }
        if (line.charAt(0) == '#') {      // ignore comments
          continue;
        }        
        set.add(line);
      }
      
    } finally {
      if (br != null) {
        br.close();
      }
      if (isr != null) {
        isr.close();
      }
    }
    return set;
  }
  
}
