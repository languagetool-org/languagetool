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
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class BaseSynthesizer implements Synthesizer {

  public final String SPELLNUMBER_TAG = "_spell_number_";

  protected volatile List<String> possibleTags;

  private final String tagFileName;
  private final String resourceFileName;
  private final IStemmer stemmer;
  private final ManualSynthesizer manualSynthesizer;
  private final ManualSynthesizer removalSynthesizer;
  private final ManualSynthesizer removalSynthesizer2;
  private final String sorosFileName;
  private final Soros numberSpeller;
  
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
      String removalPath2 = "/" + lang.getShortCode() + "/do-not-synthesize.txt";
      if (JLanguageTool.getDataBroker().resourceExists(removalPath2)) {
        try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(removalPath2)) {
          this.removalSynthesizer2 = new ManualSynthesizer(stream);
        }
      } else {
        this.removalSynthesizer2 = null;
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
          dictionary = dict = MorfologikSpeller.getDictionaryWithCaching(resourceFileName);
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
    Soros s;
    try {
      URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(sorosFileName);
      BufferedReader f = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
      StringBuffer st = new StringBuffer();
      String line;
      while ((line = f.readLine()) != null) {
        st.append(line);
        st.append('\n');
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
   */
  protected List<String> lookup(String lemma, String posTag) {
    List<String> results = new ArrayList<>();
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
    if (removalSynthesizer2 != null) {
      List<String> removeForms = removalSynthesizer2.lookup(lemma, posTag);
      if (removeForms != null) {
        results.removeAll(removeForms);
      }
    }
    return results;
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
      return new String[] {getSpelledNumber(token.getToken())};
    }
    List<String> wordForms = lookup(token.getLemma(), posTag);
    return removeExceptions(wordForms.toArray(new String[0]));
  }

  @Override
  public String[] synthesize(AnalyzedToken token, String posTag, boolean posTagRegExp) throws IOException {
    if (posTagRegExp) {
      try {
        Pattern p = Pattern.compile(posTag);
        return synthesizeForPosTags(token.getLemma(), tag -> p.matcher(tag).matches());
      } catch (PatternSyntaxException e) {
        throw new RuntimeException("Error trying to synthesize POS tag " + posTag +
                " (posTagRegExp: " + posTagRegExp + ") from token " + token.getToken(), e);
      }
    }
    return removeExceptions(synthesize(token, posTag));
  }

  /**
   * Synthesize forms for the given lemma and for all POS tags satisfying the given predicate.
   * @since 5.3
   */
  public String[] synthesizeForPosTags(String lemma, Predicate<String> acceptTag) throws IOException {
    initPossibleTags();
    List<String> results = new ArrayList<>();
    for (String tag : possibleTags) {
      if (acceptTag.test(tag)) {
        results.addAll(lookup(lemma, tag));
      }
    }
    return removeExceptions(results.toArray(new String[0]));
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
    if (possibleTags == null) {
      synchronized (this) {
        if (possibleTags == null) {
          possibleTags = loadTags();
        }
      }
    }
  }

  private List<String> loadTags() throws IOException {
    List<String> tags;
    try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(tagFileName)) {
      tags = SynthesizerTools.loadWords(stream);
    }
    if (manualSynthesizer != null) {
      for (String tag : manualSynthesizer.getPossibleTags()) {
        if (!tags.contains(tag)) {
          tags.add(tag);
        }
      }
    }
    return tags;
  }

  @Override
  public String getSpelledNumber(String arabicNumeral) {
    if (numberSpeller != null) {
      return numberSpeller.run(arabicNumeral);
    }
    return arabicNumeral;
  }

  protected boolean isException(String w) {
    return false;  
  }

  protected String[] removeExceptions(String[] words) {
    List<String> results = new ArrayList<>();
    for (String word : words) {
      if (!isException(word)) {
        results.add(word);
      }
    }
    return results.toArray(new String[0]);
  }
  

}
