/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

public abstract class AbstractRepeatedWordsRule extends TextLevelRule {

  protected abstract Map<String, List<String>> getWordsToCheck();

  protected abstract Synthesizer getSynthesizer();

  @Override
  public int minToCheckParagraph() {
    return 1;
  }

  protected int maxWordsDistance() {
    return 200;
  }

  protected abstract String getMessage();

  protected abstract String getShortMessage();

  @Override
  public abstract String getId();

  @Override
  public abstract String getDescription();

  public AbstractRepeatedWordsRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
  }

  protected String adjustPostag(String postag) {
    return postag;
  }

  protected abstract boolean isException(AnalyzedTokenReadings[] tokens, int i, boolean sentStart,
      boolean isCapitalized, boolean isAllUppercase);

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> matches = new ArrayList<>();
    // int sentenceNumber = 0;
    int wordNumber = 0;
    Map<String, Integer> wordsLastSeen = new HashMap<>();
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      // sentenceNumber++;
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      boolean sentStart = true;
      List<String> lemmasInSentece = new ArrayList<>();
      int i = -1;
      for (AnalyzedTokenReadings atrs : tokens) {
        wordNumber++;
        String token = atrs.getToken();
        boolean isCapitalized = StringTools.isCapitalizedWord(token);
        boolean isAllUppercase = StringTools.isAllUppercase(token);
        i++;
        boolean isException = token.isEmpty() || isException(tokens, i, sentStart, isCapitalized, isAllUppercase);
        if (sentStart && !token.isEmpty() && !token.matches("\\p{P}")) {
          sentStart = false;
        }
        if (isException) {
          continue;
        }
        for (AnalyzedToken atr : atrs) {
          String lemma = atr.getLemma();
          Integer seenInWordPosition = wordsLastSeen.get(lemma);
          if (seenInWordPosition != null && !lemmasInSentece.contains(lemma)
              && (seenInWordPosition - wordNumber) <= maxWordsDistance()) {
            // create match
            RuleMatch rulematch = new RuleMatch(this, sentence, pos + atrs.getStartPos(), pos + atrs.getEndPos(),
                getMessage(), getShortMessage());
            List<String> replacementLemmas = getWordsToCheck().get(lemma);
            for (String replacementLemma : replacementLemmas) {
              String[] replacements = getSynthesizer().synthesize(
                  new AnalyzedToken(token, atr.getPOSTag(), replacementLemma), adjustPostag(atr.getPOSTag()), true);
              for (String r : replacements) {
                if (isAllUppercase) {
                  r = r.toUpperCase();
                } else if (isCapitalized) {
                  r = StringTools.uppercaseFirstChar(r);
                }
                rulematch.addSuggestedReplacement(r);
              }
            }
            matches.add(rulematch);
          }
          if (getWordsToCheck().containsKey(lemma)) {
            wordsLastSeen.put(lemma, wordNumber);
            lemmasInSentece.add(lemma);
          }
        }
      }
      pos += sentence.getText().length();
    }
    return toRuleMatchArray(matches);
  }

  private static final String FILE_ENCODING = "utf-8";

  protected static Map<String, List<String>> loadWords(String path) {
    final InputStream inputStream = JLanguageTool.getDataBroker().getFromRulesDirAsStream(path);
    final Map<String, List<String>> map = new HashMap<>();
    try (Scanner scanner = new Scanner(inputStream, FILE_ENCODING)) {
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine().replaceFirst("#.*", "").trim();
        if (line.isEmpty()) {
          continue;
        }
        final String[] mainParts = line.split("=");
        String[] parts = null;
        final String word;
        if (mainParts.length == 2) {
          parts = mainParts[1].split(";");
          word = mainParts[0];
        } else if (mainParts.length == 1) {
          parts = line.split(";");
          word = "";
        } else {
          throw new RuntimeException("Format error in file " + path + ", line: " + line);
        }
        if (word.isEmpty() && parts.length < 2 || !word.isEmpty() && parts.length < 1) {
          throw new RuntimeException("Format error in file " + path + ", line: " + line);
        }
        if (!word.isEmpty()) {
          if (!map.containsKey(word)) {
            map.put(word, Arrays.asList(parts));
          } else {
            throw new RuntimeException("Word found in more than one line. \"" + word + "\" in line: " + line);
          }
        } else {
          for (String key : parts) {
            List<String> values = new ArrayList<>();
            for (String value : parts) {
              if (!value.equals(key)) {
                values.add(value);
              }
            }
            if (!map.containsKey(key)) {
              map.put(key, values);
            } else {
              throw new RuntimeException("Word found in more than one line. \"" + key + "\" in line: " + line);
            }
          }
        }
      }
    }
    return map;
  }

}
