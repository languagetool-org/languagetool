package de.danielnaber.languagetool.synthesis;

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

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.tools.Tools;

public class BaseSynthesizer implements Synthesizer {
 
  protected IStemmer synthesizer;

  private ArrayList<String> possibleTags;
  private final String tagFileName;
  private final String resourceFileName;
  
  public BaseSynthesizer(final String resourceFileName, final String tagFileName) {
    this.resourceFileName = resourceFileName;  
    this.tagFileName = tagFileName;
  }
  
  /**
   * Get a form of a given AnalyzedToken, where the form is defined by a
   * part-of-speech tag.
   * 
   * @param token
   *          AnalyzedToken to be inflected.
   * @param posTag
   *          The desired part-of-speech tag.
   * @return String value - inflected word.
   */
  @Override
  public String[] synthesize(final AnalyzedToken token, final String posTag) throws IOException {
    initSynthesizer();
    final List<WordData> wordData = synthesizer.lookup(token.getLemma() + "|" + posTag);
    final List<String> wordForms = new ArrayList<String>();
    for (WordData wd : wordData) {
      wordForms.add(wd.getStem().toString());
    }
    return wordForms.toArray(new String[wordForms.size()]);
  }
      
  @Override
  public String[] synthesize(final AnalyzedToken token, final String posTag,
      final boolean posTagRegExp) throws IOException {
    if (posTagRegExp) {
      if (possibleTags == null) {
        possibleTags = SynthesizerTools.loadWords(Tools.getStream(tagFileName));
      }
      initSynthesizer();
      final Pattern p = Pattern.compile(posTag);
      final ArrayList<String> results = new ArrayList<String>();
      for (final String tag : possibleTags) {
        final Matcher m = p.matcher(tag);
        if (m.matches()) {
          final List<WordData> wordForms = synthesizer.lookup(token.getLemma() + "|" + tag);
          for (WordData wd : wordForms) {
            results.add(wd.getStem().toString());
          }
        }
      }
      return results.toArray(new String[results.size()]);
    }
    return synthesize(token, posTag);
  }

  private void initSynthesizer() throws IOException {
    if (synthesizer == null) {
      final URL url = this.getClass().getResource(resourceFileName);
      synthesizer = new DictionaryLookup(Dictionary.read(url));
    }
  }

  @Override
  public String getPosTagCorrection(final String posTag) {
    return posTag;
  }

}
