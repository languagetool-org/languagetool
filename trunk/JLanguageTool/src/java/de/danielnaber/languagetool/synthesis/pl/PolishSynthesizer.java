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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dawidweiss.stemmers.Lametyzator;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.synthesis.Synthesizer;

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
  
  public String[] synthesize(AnalyzedToken token, String posTag) throws IOException {
    if (posTag == null) {
      return null;
    }
    if (synthesizer == null) {
      synthesizer = 
        new Lametyzator(this.getClass().getResourceAsStream(RESOURCE_FILENAME),
          "iso8859-2", '+');
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
        String[] negForms = wordForms;
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

  public String[] synthesize(AnalyzedToken token, String posTag, boolean posTagRegExp)
      throws IOException {
    if (posTag == null) {
      return null;
    }
    if (posTagRegExp) {
    if (possibleTags == null) {
      possibleTags = loadWords(this.getClass().getResourceAsStream(TAGS_FILE_NAME));
    }
    if (synthesizer == null) {
      synthesizer = 
        new Lametyzator(this.getClass().getResourceAsStream(RESOURCE_FILENAME),
          "iso8859-2", '+');
    }        
    ArrayList<String> results = new ArrayList<String>();
    
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
    
    final Pattern p = Pattern.compile(posTag.replace('+', '|'));    
        
    for (String tag : possibleTags) {
      final Matcher m = p.matcher(tag);
        if (m.matches()) {
          String[] wordForms = null;          
          if (isNegated) {
            wordForms = synthesizer.stem(token.getLemma() + "|" + tag.replaceAll(NEGATION_TAG, POTENTIAL_NEGATION_TAG));
            if (wordForms != null) {
              String[] negForms = wordForms;
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
       return (String[]) results.toArray(new String[results.size()]);    
    } else {
      return synthesize(token, posTag);
    }    
  }

  private ArrayList<String> loadWords(InputStream file) throws IOException {
    ArrayList<String> set = new ArrayList<String>();
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
      if (br != null) br.close();
      if (isr != null) isr.close();
    }
    return set;
  }
  
}
