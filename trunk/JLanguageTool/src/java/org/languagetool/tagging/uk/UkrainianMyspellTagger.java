/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.uk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tagging.Tagger;

/**
 * Ukrainian Part-of-speech tagger. This class uses myspell uk_UA.dic dictionary
 * file to assign tags to words. It only supports lemmas and three main parts of
 * speech: noun, verb and adjective
 * 
 * @author Adriy Rysin
 */
public class UkrainianMyspellTagger implements Tagger {

  private static final String RESOURCE_FILENAME = "/uk/ukrainian.dict";

  // private Lametyzator morfologik = null;
  private HashMap<String, String[]> wordsToPos;

 
  @Override
  public final List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens)
      throws IOException {

    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    // caching Lametyzator instance - lazy init
    // if (morfologik == null) {
    // File resourceFile = JLanguageTool.getAbsoluteFile(JLanguageTool.getDataBroker().getResourceDir() + RESOURCE_FILENAME);
    // morfologik = new
    // Lametyzator(Tools.getInputStream(resourceFile.getAbsolutePath()),
    // "utf-8", '+');
    // }
    if (wordsToPos == null) {
      wordsToPos = new HashMap<String, String[]>();
      final InputStream resourceFile = JLanguageTool.getDataBroker().getFromResourceDirAsStream(RESOURCE_FILENAME);
      // System.err.println("reading dict: " + resourceFile);

      final BufferedReader input = new BufferedReader(new InputStreamReader(
          resourceFile, Charset.forName("UTF-8")));

      final Pattern pattern1 = Pattern.compile("[abcdefghijklmnop]+");
      final Pattern pattern2 = Pattern.compile("[ABCDEFGHIJKLMN]+");
      final Pattern pattern3 = Pattern.compile("[BDFHJLN]+");
      final Pattern pattern4 = Pattern.compile("[UV]+");

      String line;
      while ((line = input.readLine()) != null) {
        line = line.trim();
        if (line.matches("^[0-9]") || line.length() == 0) {
          continue;
        }

        final String[] wrd = line.split("/");
        if (wrd.length > 1) {
          final String flags = wrd[1];
          final List<String> posTags = new ArrayList<String>();

          if (pattern1.matcher(flags).matches()) {
            posTags.add(IPOSTag.TAG_NOUN);
            if (flags.equals("b")) {
              posTags.add(IPOSTag.TAG_PLURAL);
            }
          } else if (pattern2.matcher(flags).matches()) {
            posTags.add(IPOSTag.TAG_VERB);
            if (pattern3.matcher(flags).matches()) {
              posTags.add(IPOSTag.TAG_REFL);
            }
          } else if (pattern4.matcher(flags).matches()) {
            posTags.add(IPOSTag.TAG_ADJ);
          }

          if (posTags.size() > 0) {
            wordsToPos.put(wrd[0], posTags.toArray(new String[0]));
          }
        }
      }
      // System.err.println("POSed words: " + wordsToPos.size());
      input.close();
    }

    for (final String word : sentenceTokens) {
      final List<AnalyzedToken> analyzedTokens = new ArrayList<AnalyzedToken>();

      final String[] posTags = wordsToPos.get(word);
      String[] lowerPosTags = null;

      if (posTags != null) {
        for (String posTag : posTags)
          analyzedTokens.add(new AnalyzedToken(word, posTag, word));
      } else {
        final String lowerWord = word.toLowerCase();
        if (!word.equals(lowerWord)) {
          lowerPosTags = wordsToPos.get(lowerWord);
          if (lowerPosTags != null) {
            for (String lowerPosTag : lowerPosTags) {
              analyzedTokens.add(new AnalyzedToken(word, lowerPosTag, lowerWord));
            }
          }
        }
        // else {
        // analyzedTokens.add(new AnalyzedToken(word, null, word));
        // }
      }

      if (posTags == null && lowerPosTags == null) {
        analyzedTokens.add(new AnalyzedToken(word, null, null));
      }

      tokenReadings.add(new AnalyzedTokenReadings(analyzedTokens
              .toArray(new AnalyzedToken[analyzedTokens.size()]), pos));
      pos += word.length();
    }

    return tokenReadings;
  }

  @Override
  public final AnalyzedTokenReadings createNullToken(final String token, final int startPos) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, null, null), startPos);
  }

  @Override
  public AnalyzedToken createToken(String token, String posTag) {
    return new AnalyzedToken(token, posTag, null);
  }

}
