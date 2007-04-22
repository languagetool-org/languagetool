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
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.dawidweiss.stemmers.Lametyzator;

import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.synthesis.Synthesizer;

/** English word form synthesizer.
 * Based on part-of-speech lists in Public Domain.
 * see readme.txt for details, the POS tagset is
 * described in tagset.txt
 * 
 * @author Marcin Milkowski
 */


public class EnglishSynthesizer implements Synthesizer {

  private static final String RESOURCE_FILENAME = "/resource/en/english_synth.dict";
  
  private static final String TAGS_FILE_NAME = "/resource/en/english_tags.txt";

  private Lametyzator synthesizer = null;

  private ArrayList<String> possibleTags = null;
  
  public String[] synthesize(String lemma, String posTag) throws IOException {
    if (synthesizer == null) {
      synthesizer = 
        new Lametyzator(this.getClass().getResourceAsStream(RESOURCE_FILENAME),
          "iso8859-1", '+');
    }
    String[] wordForms = null;
    wordForms = synthesizer.stem(lemma + "|" + posTag);
    return wordForms;
  }

  public String[] synthesize(String lemma, String posTag, boolean posTagRegExp)
      throws IOException {
    
    if (posTagRegExp) {
    if (possibleTags == null) {
      possibleTags = loadWords(this.getClass().getResourceAsStream(TAGS_FILE_NAME));
    }
    if (synthesizer == null) {
      synthesizer = 
        new Lametyzator(this.getClass().getResourceAsStream(RESOURCE_FILENAME),
          "iso8859-1", '+');
    }    
    Pattern p = Pattern.compile(posTag);
    ArrayList<String> results = new ArrayList<String>();
    for (String tag : possibleTags) {
      Matcher m = p.matcher(tag);
        if (m.matches()) {
          String[] wordForms = null;
          wordForms = synthesizer.stem(lemma + "|" + tag);
          if (wordForms != null) {
            results.addAll(Arrays.asList(wordForms));
          }
      }
    }
       return (String[]) results.toArray(new String[results.size()]);    
    } else {
      return synthesize(lemma, posTag);
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
