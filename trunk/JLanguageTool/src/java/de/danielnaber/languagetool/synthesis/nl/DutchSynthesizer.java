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
package de.danielnaber.languagetool.synthesis.nl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morfologik.stemmers.Lametyzator;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.synthesis.Synthesizer;
import de.danielnaber.languagetool.synthesis.SynthesizerTools;
import de.danielnaber.languagetool.tools.Tools;

/** Dutch word form synthesizer. <br/>
 * 
 * @author Marcin Miłkowski
 */


public class DutchSynthesizer implements Synthesizer {

  private static final String RESOURCE_FILENAME = "/resource/nl/dutch_synth.dict";
  
  private static final String TAGS_FILE_NAME = "/resource/nl/dutch_tags.txt";
  
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
    if (synthesizer == null) {
        setFileName();
        synthesizer = 
          new Lametyzator();
      }
      String[] wordForms = null;
      wordForms = synthesizer.stem(token.getLemma() + "|" + posTag);
      return wordForms;
    }
  

  // TODO: avoid code duplicattion with EnglishSynthesizer
  public String[] synthesize(final AnalyzedToken token, final String posTag, final boolean posTagRegExp)
      throws IOException {
    
    if (posTagRegExp) {
    if (possibleTags == null) {
      possibleTags = SynthesizerTools.loadWords(Tools.getStream(TAGS_FILE_NAME));
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
  
}
