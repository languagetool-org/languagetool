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
package de.danielnaber.languagetool.synthesis.pl;

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

/** Polish word form synthesizer.
 * Based on project Morfologik.
 * 
 * @author Marcin Milkowski
 */


public class PolishSynthesizer implements Synthesizer {

  private static final String RESOURCE_FILENAME = "/resource/pl/polish_synth.dict";
  
  private static final String TAGS_FILE_NAME = "/resource/pl/polish_tags.txt";
  
  private static final String POTENTIAL_NEGATION_TAG = ":aff";
  private static final String NEGATION_TAG = ":neg";
  private static final String COMP_TAG = "comp";
  private static final String SUP_TAG = "sup";

  private Lametyzator synthesizer = null;

  private ArrayList<String> possibleTags = null;

  private void setFileName() {
    System.setProperty(Lametyzator.PROPERTY_NAME_LAMETYZATOR_DICTIONARY, 
        RESOURCE_FILENAME);    
  }
  
  public String[] synthesize(final AnalyzedToken token, final String posTag) throws IOException {
    if (posTag == null) {
      return null;
    }
    if (synthesizer == null) {
      setFileName();
      synthesizer = 
        new Lametyzator();
    }
    boolean isNegated = false;
    if (token.getPOSTag() != null) {
      isNegated = posTag.indexOf(NEGATION_TAG) > 0 
      || token.getPOSTag().indexOf(NEGATION_TAG) > 0
      && !(posTag.indexOf(COMP_TAG) > 0)
      && !(posTag.indexOf(SUP_TAG) > 0);
    }
    if (posTag.indexOf('+') > 0) {      
      return synthesize(token, posTag, true);
    } else {
    String[] wordForms = null;
    if (isNegated) {
      wordForms = synthesizer.stem(token.getLemma() + "|" + posTag.replaceFirst(NEGATION_TAG, POTENTIAL_NEGATION_TAG));
      if (wordForms != null) {
        final String[] negForms = wordForms;
        for (int i = 0; i < wordForms.length; i++) {
          negForms[i] = "nie" + wordForms[i];
        }
        wordForms = negForms;
      }
    } else {
    wordForms = synthesizer.stem(token.getLemma() + "|" + posTag);
    }
    return wordForms;
    }
  }

  public String[] synthesize(final AnalyzedToken token, String posTag, final boolean posTagRegExp)
      throws IOException {
    if (posTag == null) {
      return null;
    }
    if (posTagRegExp) {
    if (possibleTags == null) {
      possibleTags = SynthesizerTools.loadWords(Tools.getStream(TAGS_FILE_NAME));
    }
    if (synthesizer == null) {
      setFileName();
      synthesizer = 
        new Lametyzator();
    }        
    final ArrayList<String> results = new ArrayList<String>();
    
    boolean isNegated = false;
    if (token.getPOSTag() != null) {
      isNegated = posTag.indexOf(NEGATION_TAG) > 0 
      || token.getPOSTag().indexOf(NEGATION_TAG) > 0
      && !(posTag.indexOf(COMP_TAG) > 0)
      && !(posTag.indexOf(SUP_TAG) > 0);
    }
    
    if (isNegated) {
      posTag = posTag.replaceAll(NEGATION_TAG, POTENTIAL_NEGATION_TAG + "?");
    }
    
    final Pattern p = Pattern.compile(
        posTag.replace('+', '|').
          replaceAll("m[1-5]", "m[1-5]?"));    
        
    for (final String tag : possibleTags) {
      final Matcher m = p.matcher(tag);
        if (m.matches()) {
          String[] wordForms = null;          
          if (isNegated) {
            wordForms = synthesizer.stem(token.getLemma() + "|" + tag.replaceAll(NEGATION_TAG, POTENTIAL_NEGATION_TAG));
            if (wordForms != null) {
              final String[] negForms = wordForms;
              for (int i = 0; i < wordForms.length; i++) {
                negForms[i] = "nie" + wordForms[i];
              }
              wordForms = negForms;
            }
          } else { 
          wordForms = synthesizer.stem(token.getLemma() + "|" + tag);
          }
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
    if (posTag.contains(".")) {         
    final String[] tags = posTag.split(":");
    int pos = -1;
    for (int i = 0; i < tags.length; i++) {
      if (tags[i].matches(".*[a-z]\\.[a-z].*")) {
        tags[i] = "(.*" 
          + tags[i].replace(".", ".*|.*")
          + ".*)";
        pos = i;
      }
    }
    if (pos == -1) {
      return posTag;
    } else {
    String s = tags[0];
    for (int i = 1; i < tags.length; i++) {
      s = s + ":" + tags[i];
      }
    //s = s + tags[tags.length - 1];
    return s;
    }
    } else {
      return posTag;
    }    
  }
  
}
