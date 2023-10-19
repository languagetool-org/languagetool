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
import org.languagetool.Language;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

public abstract class AbstractRepeatedWordsRule extends TextLevelRule {

  protected abstract Map<String, SynonymsData> getWordsToCheck();

  protected abstract Synthesizer getSynthesizer();

  @Override
  public int minToCheckParagraph() {
    return 1;
  }

  protected int maxWordsDistance() {
    return 150;
  }

  protected abstract String getMessage();

  protected abstract String getShortMessage();

  @Override
  public String getId() {
    return ruleId;
  }

  private String ruleId;
  
  @Override
  public abstract String getDescription();

  public AbstractRepeatedWordsRule(ResourceBundle messages, Language language) {
    super(messages);
    super.setCategory(Categories.REPETITIONS_STYLE.getCategory(messages));
    super.setLocQualityIssueType(ITSIssueType.Style);
    ruleId = language.getShortCode().toUpperCase() + "_" + "REPEATEDWORDS";
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
    int prevSentenceLength = 0;
    for (AnalyzedSentence sentence : sentences) {
      // sentenceNumber++;
      AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();
      pos += prevSentenceLength;
      prevSentenceLength = sentence.getText().length();
      // ignore sentences not ending in period
      String lastToken = tokens[tokens.length-1].getToken();
      if (!lastToken.equals(".") && !lastToken.equals("!") && !lastToken.equals("?")) {
        continue;
      }
      boolean sentStart = true;
      List<String> lemmasInSentece = new ArrayList<>();
      int i = -1;
      for (AnalyzedTokenReadings atrs : tokens) {
        if (atrs.isImmunized()) {
          continue;
        }
        String token = atrs.getToken();
        if (!token.isEmpty()) {
          wordNumber++;
        }
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
        List<String> lemmas = new ArrayList<>();
        for (AnalyzedToken atr : atrs) {
          String lemma = atr.getLemma();
          lemmas.add(lemma);
          Integer seenInWordPosition = wordsLastSeen.get(lemma);
          if (seenInWordPosition != null && !lemmasInSentece.contains(lemma)
              && (wordNumber - seenInWordPosition) <= maxWordsDistance()) {
            boolean createMatch = true;
            String postag = getWordsToCheck().get(lemma).getPostag();
            if (postag != null && !atr.getPOSTag().matches(postag)) {
              createMatch = false;
            }
            String chunk = getWordsToCheck().get(lemma).getChunk();
            if (chunk != null && !atrs.matchesChunkRegex(chunk)) {
              createMatch = false;
            }
            // create match
            if (createMatch) {
              RuleMatch rulematch = new RuleMatch(this, sentence, pos + atrs.getStartPos(), pos + atrs.getEndPos(),
                  getMessage(), getShortMessage());
              rulematch.setSpecificRuleId(ruleId + "_" + StringTools.toId(lemma));
              List<String> replacementLemmas = getWordsToCheck().get(lemma).getSynonyms();
              for (String replacementLemma : replacementLemmas) {
                String[] replacements = getSynthesizer().synthesize(
                    new AnalyzedToken(token, atr.getPOSTag(), replacementLemma), adjustPostag(atr.getPOSTag()), true);
                // if there is no result from the synthesizer, use the lemma as it is (it can be a multiword)
                if (replacements.length == 0) {
                  replacements =  new String[]{replacementLemma};
                }
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
              break;
            }
          }
        }
        // count even if postag/chunk don't match
        for (String lemma : lemmas) {
          if (getWordsToCheck().containsKey(lemma)) {
            wordsLastSeen.put(lemma, wordNumber);
            lemmasInSentece.add(lemma);
          }
        }
      }
    }
    return toRuleMatchArray(matches);
  }

  private static final String FILE_ENCODING = "utf-8";

  protected static Map<String, SynonymsData> loadWords(String path) {
    final InputStream inputStream = JLanguageTool.getDataBroker().getFromRulesDirAsStream(path);
    final Map<String, SynonymsData> map = new HashMap<>();
    try (Scanner scanner = new Scanner(inputStream, FILE_ENCODING)) {
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine().replaceFirst("#.*", "").trim();
        if (line.isEmpty()) {
          continue;
        }
        final String[] mainParts = line.split("=");
        String[] parts = null;
        String postag = null;
        String chunk = null;
        String word;
        if (mainParts.length == 2) {
          parts = mainParts[1].split(";");
          word = mainParts[0];
          String[] wordPosChunk = word.split("/");
          word = wordPosChunk[0];
          if (wordPosChunk.length > 1) {
            postag = wordPosChunk[1];
          }
          if (wordPosChunk.length > 2) {
            chunk = wordPosChunk[2];
          }
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
            SynonymsData synonymsData = new SynonymsData(Arrays.asList(parts), postag, chunk);
            map.put(word, synonymsData);
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
              SynonymsData synonymsData = new SynonymsData(values, postag, chunk);
              map.put(key, synonymsData);
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
