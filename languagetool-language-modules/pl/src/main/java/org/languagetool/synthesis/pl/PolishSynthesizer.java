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
package org.languagetool.synthesis.pl;

import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;
import org.languagetool.AnalyzedToken;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.SynthesizerTools;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Polish word form synthesizer. Based on project Morfologik.
 * 
 * @author Marcin Milkowski
 */

public class PolishSynthesizer extends BaseSynthesizer implements Synthesizer {

  private static final String RESOURCE_FILENAME = "/pl/polish_synth.dict";
  private static final String TAGS_FILE_NAME = "/pl/polish_tags.txt";

  private static final String POTENTIAL_NEGATION_TAG = ":aff";
  private static final String NEGATION_TAG = ":neg";
  private static final String COMP_TAG = "com";
  private static final String SUP_TAG = "sup";

  private List<String> possibleTags;

  public static final PolishSynthesizer INSTANCE = new PolishSynthesizer();

  /** @deprecated use {@link #INSTANCE} */
  public PolishSynthesizer(Language lang) {
    this();
  }
  private PolishSynthesizer() {
    super(RESOURCE_FILENAME, TAGS_FILE_NAME, "pl");
  }

  @Override
  public final String[] synthesize(AnalyzedToken token, String posTag) throws IOException {
    if (posTag == null) {
      return null;
    }
    IStemmer synthesizer = new DictionaryLookup(getDictionary());
    boolean isNegated = false;
    if (token.getPOSTag() != null) {
      isNegated = posTag.indexOf(NEGATION_TAG) > 0
          || token.getPOSTag().indexOf(NEGATION_TAG) > 0
          && !(posTag.indexOf(COMP_TAG) > 0) && !(posTag.indexOf(SUP_TAG) > 0);
    }
    if (posTag.indexOf('+') > 0) {
      return synthesize(token, posTag, true);
    }
    List<String> forms = getWordForms(token, posTag, isNegated, synthesizer);
    return forms.toArray(new String[0]);
  }

  @Override
  public final String[] synthesize(AnalyzedToken token, String pos, boolean posTagRegExp) throws IOException {
    if (pos == null) {
      return null;
    }
    String posTag = pos;
    if (posTagRegExp) {
      if (possibleTags == null) {
        try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(TAGS_FILE_NAME)) {
          possibleTags = SynthesizerTools.loadWords(stream);
        }
      }
      IStemmer synthesizer = new DictionaryLookup(getDictionary());
      List<String> results = new ArrayList<>();

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

      try {
        Pattern p = Pattern.compile(posTag.replace('+', '|'));
        for (String tag : possibleTags) {
          Matcher m = p.matcher(tag);
          if (m.matches()) {
            List<String> wordForms = getWordForms(token, tag, isNegated, synthesizer);
            if (wordForms != null) {
              results.addAll(wordForms);
            }
          }
        }
      } catch (PatternSyntaxException e) {
        // catch this rare error which I couldn't fix yet (https://github.com/languagetool-org/languagetool/issues/1651):
        //java.util.regex.PatternSyntaxException: Unclosed group near index 17
        //(.*)(sg|pl.*acc.*
        //                 ^
        e.printStackTrace();
      }
      //remove duplicates
      Set<String> hs = new HashSet<>(results);
      results.clear();
      results.addAll(hs);     
      
      return results.toArray(new String[0]);
    }
    return synthesize(token, posTag);
  }

  @Override
  public final String getPosTagCorrection(String posTag) {
    if (posTag.contains(".")) {
      String[] tags = posTag.split(":");
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
      StringBuilder sb = new StringBuilder();
      sb.append(tags[0]);
      for (int i = 1; i < tags.length; i++) {
        sb.append(':');
        sb.append(tags[i]);
      }
      return sb.toString();
    }
    return posTag;
  }

  private List<String> getWordForms(AnalyzedToken token, String posTag,
      boolean isNegated, IStemmer synthesizer) {
    List<String> forms = new ArrayList<>();
    List<WordData> wordForms;
    if (isNegated) {
      wordForms = synthesizer.lookup(token.getLemma() + "|"
          + posTag.replaceFirst(NEGATION_TAG, POTENTIAL_NEGATION_TAG));
      if (wordForms != null) {                      
        for (WordData wd : wordForms) {
          forms.add("nie" + wd.getStem());
        }
      }
    } else {
      wordForms = synthesizer.lookup(token.getLemma() + "|" + posTag);
      for (WordData wd : wordForms) {
        if (wd.getStem() != null) {
          forms.add(wd.getStem().toString());
        }
      }      
    }
    return forms;
  }

}
