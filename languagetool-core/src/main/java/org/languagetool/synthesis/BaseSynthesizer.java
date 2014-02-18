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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

import org.languagetool.AnalyzedToken;
import org.languagetool.JLanguageTool;

public class BaseSynthesizer implements Synthesizer {

  protected List<String> possibleTags;

  private final String tagFileName;
  private final String resourceFileName;

  private Dictionary dictionary;

  private final IStemmer stemmer;

  /**
   * @param resourceFileName The dictionary file name.
   * @param tagFileName The name of a file containing all possible tags.
   */
  public BaseSynthesizer(final String resourceFileName, final String tagFileName) {
    this.resourceFileName = resourceFileName;
    this.tagFileName = tagFileName;
    this.stemmer = createStemmer();
  }

  /**
   * Returns the {@link Dictionary} used for this synthesizer.
   * The dictionary file can be defined in the {@link #BaseSynthesizer(String, String) constructor}.
   * 
   * @throws IOException In case the dictionary cannot be loaded.
   */
  protected Dictionary getDictionary() throws IOException {
    if (this.dictionary == null) {
      synchronized (this) {
        if (this.dictionary == null) {
          final URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(resourceFileName);
          this.dictionary = Dictionary.read(url);
        }
      }
    }
    return this.dictionary;
  }

  /**
   * Creates a new {@link IStemmer} based on the configured {@link #getDictionary() dictionary}.
   * The result must not be shared among threads.
   * 
   * @since 2.3
   */
  protected IStemmer createStemmer() {
    try {
      final Dictionary dict = getDictionary();
      return new DictionaryLookup(dict);
    } catch (IOException e) {
      throw new RuntimeException("Could not load dictionary", e);
    }
  }

  /**
   * Lookup the inflected forms of a lemma defined by a part-of-speech tag.
   * @param lemma the lemma to be inflected.
   * @param posTag the desired part-of-speech tag.
   * @param results the list to collect the inflected forms.
   */
  protected void lookup(String lemma, String posTag, List<String> results) {
    final List<WordData> wordForms = stemmer.lookup(lemma + "|" + posTag);
    for (WordData wd : wordForms) {
      results.add(wd.getStem().toString());
    }
  }

  /**
   * Get a form of a given AnalyzedToken, where the form is defined by a
   * part-of-speech tag.
   * 
   * @param token AnalyzedToken to be inflected.
   * @param posTag The desired part-of-speech tag.
   * @return inflected words, or an empty array if no forms were found
   */
  @Override
  public String[] synthesize(final AnalyzedToken token, final String posTag) throws IOException {
    initSynthesizer();
    final List<String> wordForms = new ArrayList<>();
    lookup(token.getLemma(), posTag, wordForms);
    return wordForms.toArray(new String[wordForms.size()]);
  }

  @Override
  public String[] synthesize(final AnalyzedToken token, final String posTag,
      final boolean posTagRegExp) throws IOException {
    if (posTagRegExp) {
      initSynthesizer();
      initPossibleTags();
      final Pattern p = Pattern.compile(posTag);
      final List<String> results = new ArrayList<>();

      for (final String tag : possibleTags) {
        final Matcher m = p.matcher(tag);
        if (m.matches()) {
          lookup(token.getLemma(), tag, results);
        }
      }
      return results.toArray(new String[results.size()]);
    }
    return synthesize(token, posTag);
  }

  @Override
  public String getPosTagCorrection(final String posTag) {
    return posTag;
  }

  /**
   * @since 2.5
   * 
   * @return the stemmer interface to be used.
   */
  public IStemmer getStemmer() {
    return stemmer;
  }

  protected void initPossibleTags() throws IOException {
    if (possibleTags == null) {
      synchronized (this) {
        if (this.possibleTags == null) {
          possibleTags = SynthesizerTools.loadWords(JLanguageTool.getDataBroker().getFromResourceDirAsStream(tagFileName));
        }
      }
    }
  }

  // TODO: mark as @deprecated or simply remove?
  protected void initSynthesizer() throws IOException {
    // The base implementation does no longer need this method, but extended classes may still rely on it.
    // Dictionary-loading is implemented in getDictionary().
  }

}
