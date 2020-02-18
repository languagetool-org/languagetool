/* LanguageTool, a natural language style checker
 * Copyright (C) 2009 Marcin Miłkowski
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
package org.languagetool.synthesis;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;
import org.languagetool.AnalyzedToken;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseSynthesizer implements Synthesizer {

  protected volatile List<String> possibleTags;

  private final String tagFileName;
  private final String resourceFileName;
  private final IStemmer stemmer;
  private final ManualSynthesizer manualSynthesizer;
  private final ManualSynthesizer removalSynthesizer;
  private final String sorosFileName;
  private final Soros numberSpeller;
  
  public final String SPELLNUMBER_TAG = "_spell_number_";
  
  private volatile Dictionary dictionary;

  /**
   * @param resourceFileName The dictionary file name.
   * @param tagFileName The name of a file containing all possible tags.
   */
  public BaseSynthesizer(String sorosFileName, String resourceFileName, String tagFileName, Language lang) {
    this.resourceFileName = resourceFileName;
    this.tagFileName = tagFileName;
    this.stemmer = createStemmer();
    this.sorosFileName = sorosFileName;
    this.numberSpeller = createNumberSpeller(lang.getShortCode());
    try {
      String path = "/" + lang.getShortCode() + "/added.txt";
      if (JLanguageTool.getDataBroker().resourceExists(path)) {
        try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path)) {
          this.manualSynthesizer = new ManualSynthesizer(stream);
        }
      } else {
        this.manualSynthesizer = null;
      }
      String removalPath = "/" + lang.getShortCode() + "/removed.txt";
      if (JLanguageTool.getDataBroker().resourceExists(removalPath)) {
        try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(removalPath)) {
          this.removalSynthesizer = new ManualSynthesizer(stream);
        }
      } else {
        this.removalSynthesizer = null;
      }
      
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public BaseSynthesizer(String resourceFileName, String tagFileName, Language lang) {
    this(null, resourceFileName, tagFileName, lang);
    
  }

  /**
   * Returns the {@link Dictionary} used for this synthesizer.
   * The dictionary file can be defined in the {@link #BaseSynthesizer(String, String, Language) constructor}.
   * @throws IOException In case the dictionary cannot be loaded.
   */
  protected Dictionary getDictionary() throws IOException {
    Dictionary dict = this.dictionary;
    if (dict == null) {
      synchronized (this) {
        dict = this.dictionary;
        if (dict == null) {
          URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(resourceFileName);
          this.dictionary = dict = Dictionary.read(url);
        }
      }
    }
    return dict;
  }

  /**
   * Creates a new {@link IStemmer} based on the configured {@link #getDictionary() dictionary}.
   * The result must not be shared among threads.
   * @since 2.3
   */
  protected IStemmer createStemmer() {
    try {
      return new DictionaryLookup(getDictionary());
    } catch (IOException e) {
      throw new RuntimeException("Could not load dictionary", e);
    }
  }
  
  private Soros createNumberSpeller(String langcode) {
    Soros s = null;
    try {
      URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(sorosFileName);
      BufferedReader f = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
      StringBuffer st = new StringBuffer();
      String line = null;
      while ((line = f.readLine()) != null) {
        st.append(line);
        st.append("\n");
      }
      s = new Soros(new String(st), langcode);
    } catch (Exception e) {
      return null;
    }
    return s;
  }

  /**
   * Lookup the inflected forms of a lemma defined by a part-of-speech tag.
   * @param lemma the lemma to be inflected.
   * @param posTag the desired part-of-speech tag.
   * @param results the list to collect the inflected forms.
   */
  protected void lookup(String lemma, String posTag, List<String> results) {
    synchronized (this) { // the stemmer is not thread-safe
      List<WordData> wordForms = stemmer.lookup(lemma + "|" + posTag);
      for (WordData wd : wordForms) {
        results.add(wd.getStem().toString());
      }
    }
    if (manualSynthesizer != null) {
      List<String> manualForms = manualSynthesizer.lookup(lemma, posTag);
      if (manualForms != null) {
        results.addAll(manualForms);
      }
    }
    if (removalSynthesizer != null) {
      List<String> removeForms = removalSynthesizer.lookup(lemma, posTag);
      if (removeForms != null) {
        results.removeAll(removeForms);
      }
    }
  }

  /**
   * Get a form of a given AnalyzedToken, where the form is defined by a
   * part-of-speech tag.
   * @param token AnalyzedToken to be inflected.
   * @param posTag The desired part-of-speech tag.
   * @return inflected words, or an empty array if no forms were found
   */
  @Override
  public String[] synthesize(AnalyzedToken token, String posTag) throws IOException {
    if (posTag.equals(SPELLNUMBER_TAG)) {
      String[] strArray = new String[] {getSpelledNumber(token.getToken())};
      return strArray;
    }
    List<String> wordForms = new ArrayList<>();
    lookup(token.getLemma(), posTag, wordForms);
    return wordForms.toArray(new String[0]);
  }

  @Override
  public String[] synthesize(AnalyzedToken token, String posTag, boolean posTagRegExp) throws IOException {
    if (posTagRegExp) {
      initPossibleTags();
      Pattern p = Pattern.compile(posTag);
      List<String> results = new ArrayList<>();
      for (String tag : possibleTags) {
        Matcher m = p.matcher(tag);
        if (m.matches()) {
          lookup(token.getLemma(), tag, results);
        }
      }
      return results.toArray(new String[0]);
    }
    return synthesize(token, posTag);
  }

  @Override
  public String getPosTagCorrection(String posTag) {
    return posTag;
  }

  /**
   * @since 2.5
   * @return the stemmer interface to be used.
   */
  public IStemmer getStemmer() {
    return stemmer;
  }

  protected void initPossibleTags() throws IOException {
    List<String> tags = possibleTags;
    if (tags == null) {
      synchronized (this) {
        tags = possibleTags;
        if (tags == null) {
          try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(tagFileName)) {
            possibleTags = SynthesizerTools.loadWords(stream);
          }
        }
        // needed to be moved into synchronized block, should fix ConcurrentModificationException in synthesize
        if (manualSynthesizer != null) {
          for (String tag : manualSynthesizer.getPossibleTags()) {
            if (!possibleTags.contains(tag)) {
              possibleTags.add(tag);
            }
          }
        }
      }
    }
  }

  @Override
  public String getSpelledNumber(String arabicNumeral) {
    if (numberSpeller != null) {
      return numberSpeller.run(arabicNumeral);
    }
    return arabicNumeral;
  }

}
