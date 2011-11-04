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
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.synthesis.Synthesizer;
import de.danielnaber.languagetool.synthesis.SynthesizerTools;

/**
 * Polish word form synthesizer. Based on project Morfologik.
 * 
 * @author Marcin Milkowski
 */

public class PolishSynthesizer implements Synthesizer {

  private static final String RESOURCE_FILENAME = "/pl/polish_synth.dict";

  private static final String TAGS_FILE_NAME = "/pl/polish_tags.txt";

  private static final String POTENTIAL_NEGATION_TAG = ":aff";
  private static final String NEGATION_TAG = ":neg";
  private static final String COMP_TAG = "comp";
  private static final String SUP_TAG = "sup";

  private IStemmer synthesizer;

  private ArrayList<String> possibleTags;
  
  @Override
  public final String[] synthesize(final AnalyzedToken token,
      final String posTag) throws IOException {
    if (posTag == null) {
      return null;
    }
    if (synthesizer == null) {
      final URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(RESOURCE_FILENAME);
      synthesizer = new DictionaryLookup(Dictionary.read(url));
    }
    boolean isNegated = false;
    if (token.getPOSTag() != null) {
      isNegated = posTag.indexOf(NEGATION_TAG) > 0
          || token.getPOSTag().indexOf(NEGATION_TAG) > 0
          && !(posTag.indexOf(COMP_TAG) > 0) && !(posTag.indexOf(SUP_TAG) > 0);
    }
    if (posTag.indexOf('+') > 0) {
      return synthesize(token, posTag, true);
    }
    final List<String> forms = getWordForms(token, posTag, isNegated);
    return forms.toArray(new String[forms.size()]);
  }

  @Override
  public final String[] synthesize(final AnalyzedToken token, final String pos,
      final boolean posTagRegExp) throws IOException {
    if (pos == null) {
      return null;
    }
    String posTag = pos;
    if (posTagRegExp) {
      if (possibleTags == null) {
        possibleTags = SynthesizerTools.loadWords(JLanguageTool.getDataBroker().
        		getFromResourceDirAsStream(TAGS_FILE_NAME));
      }
      if (synthesizer == null) {
        final URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(RESOURCE_FILENAME);
        synthesizer = new DictionaryLookup(Dictionary.read(url));
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

      final Pattern p = Pattern.compile(posTag.replace('+', '|').replaceAll(
          "m[1-3]", "m[1-3]?"));

      for (final String tag : possibleTags) {
        final Matcher m = p.matcher(tag);
        if (m.matches()) {
          final List<String> wordForms = getWordForms(token, tag, isNegated);
          if (wordForms != null) {
            results.addAll(wordForms);
          }
        }
      }
      return results.toArray(new String[results.size()]);
    }
    return synthesize(token, posTag);
  }

  @Override
  public final String getPosTagCorrection(final String posTag) {
    if (posTag.contains(".")) {
      final String[] tags = posTag.split(":");
      int pos = -1;
      for (int i = 0; i < tags.length; i++) {
        if (tags[i].matches(".*[a-z]\\.[a-z].*")) {
          tags[i] = "(.*" + tags[i].replace(".", ".*|.*") + ".*)";
          pos = i;
        }
      }
      if (pos == -1) {
        return posTag;
      }
      final StringBuilder sb = new StringBuilder();
      sb.append(tags[0]);
      for (int i = 1; i < tags.length; i++) {
        sb.append(':');
        sb.append(tags[i]);
      }
      return sb.toString();
    }
    return posTag;
  }

  private List<String> getWordForms(final AnalyzedToken token, final String posTag,
      final boolean isNegated) {
    final List<String> forms = new ArrayList<String>();
    final List<WordData> wordForms;
    if (isNegated) {
      wordForms = synthesizer.lookup(token.getLemma() + "|"
          + posTag.replaceFirst(NEGATION_TAG, POTENTIAL_NEGATION_TAG));
      if (wordForms != null) {                      
        for (WordData wd : wordForms) {
          forms.add("nie" + wd.getStem().toString());           
        }
      }                                      
    } else {
      wordForms = synthesizer.lookup(token.getLemma() + "|" + posTag);      
      for (WordData wd : wordForms) {
        forms.add(wd.getStem().toString());
      }      
    }
    return forms;
  }

}
