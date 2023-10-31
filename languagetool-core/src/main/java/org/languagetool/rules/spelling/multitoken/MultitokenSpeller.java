/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Jaume Ortolà
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
package org.languagetool.rules.spelling.multitoken;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.rules.spelling.morfologik.WeightedSuggestion;
import org.languagetool.tools.StringTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MultitokenSpeller {

  private final int MAX_LENGTH_DIFF = 3;
  private HashMap<String, String> oneSpace;
  private HashMap<String, String> twoSpaces;
  private HashMap<String, String> threeSpaces;
  private HashMap<String, String> hyphenated;
  private SpellingCheckRule spellingRule;
  private Language language;

  /*
   * Ultra-naive speller that provides spelling suggestions for multitoken words from a list of words.
   * The fastest method, so far.
   */

  public MultitokenSpeller(Language lang, List<String> filePaths) {
    language = lang;
    spellingRule = lang.getDefaultSpellingRule();
    initMultitokenSpeller(filePaths);
  }

  public List<String> getSuggestions(String word) throws IOException {
    word = word.replaceAll("\\p{Zs}+", " ");
    if (discardRunOnWords(word)) {
     return Collections.emptyList();
    }
    String wordLowercase = StringTools.removeDiacritics(word.toLowerCase());
    int numSpaces = StringTools.numberOf(word, " ");
    int numHyphens = StringTools.numberOf(word, "-");
    HashMap<String, String> set = chooseHashMap(word, numSpaces, numHyphens);
    List<WeightedSuggestion> weightedCandidates = new ArrayList<>();
    String firstChar = StringTools.removeDiacritics(word.substring(0,1).toLowerCase());
    for (Map.Entry<String, String> entry : set.entrySet()) {
      String candidateLowercase = entry.getValue();
      String candidate = entry.getKey();
      // require that the first letter is correct to speed up the generation of suggestions even more
      if (!candidateLowercase.substring(0,1).equals(firstChar)) {
        continue;
      }
      if (Math.abs(candidateLowercase.length() - word.length()) > MAX_LENGTH_DIFF) {
        continue;
      }
      if (candidate.equals(candidate.toLowerCase())
        && StringTools.convertToTitleCaseIteratingChars(candidate).equals(word)) {
        return Collections.emptyList();
      }
      List<Integer> distances = distancesPerWord(candidateLowercase, wordLowercase);
      int distance = distances.stream().reduce(0, Integer::sum);
      if (distance < 1) {
        weightedCandidates.clear();
        weightedCandidates.add(new WeightedSuggestion(candidate, 0));
        break;
      }
      if (distances.stream().anyMatch(j -> j > 2)) {
        continue;
      }
      if (distance <= maxEditDistance(candidateLowercase, wordLowercase)) {
        weightedCandidates.add(new WeightedSuggestion(candidate, distance));
      }
    }
    if (weightedCandidates.isEmpty()) {
      return Collections.emptyList();
    }
    Collections.sort(weightedCandidates);
    List<String> results = new ArrayList<>();
    int weightFirstCandidate = weightedCandidates.get(0).getWeight();
    for (WeightedSuggestion weightedCandidate : weightedCandidates) {
      if (weightedCandidate.getWeight() - weightFirstCandidate < 1) {
        results.add(weightedCandidate.getWord());
      }
    }
    return results;
  }

  private int maxEditDistance(String candidateLowercase, String wordLowercase) {
    int totalLength = wordLowercase.length();
    int correctLength = totalLength - numberOfCorrectChars(candidateLowercase, wordLowercase);
    if (correctLength <= 7) {
      return 2;
    }
    return (int) (2 + 0.25 * (correctLength - 7));
  }

  private List<Integer> distancesPerWord(String s1, String s2) {
    List<Integer> distances = new ArrayList<>();
    String parts1[] = s1.split("[ -]");
    String parts2[] = s2.split("[ -]");
    if (parts1.length == parts2.length && parts1.length > 1) {
      for (int i=0; i<parts1.length; i++) {
        distances.add(levenshteinDistance(parts1[i], parts2[i]));
      }
    } else {
      distances.add(levenshteinDistance(s1, s2));
    }
    return distances;
  }

  private int levenshteinDistance(String s1, String s2) {
    return LevenshteinDistance.getDefaultInstance().apply(s1, s2);
  }

  private int numberOfCorrectTokens(String s1, String s2) {
    String parts1[] = s1.split(" ");
    String parts2[] = s2.split(" ");
    int correctTokens = 0;
    if (parts1.length == parts2.length && parts1.length > 1) {
      for (int i=0; i<parts1.length; i++) {
        if (parts1[i].equals(parts2[i])) {
          correctTokens++;
        }
      }
    }
    return correctTokens;
  }

  private int numberOfCorrectChars(String s1, String s2) {
    String parts1[] = s1.split(" ");
    String parts2[] = s2.split(" ");
    int correctTokens = 0;
    if (parts1.length == parts2.length && parts1.length > 1) {
      for (int i=0; i<parts1.length; i++) {
        if (parts1[i].equals(parts2[i])) {
          correctTokens += parts1[i].length();
        }
      }
    }
    return correctTokens;
  }

  private void initMultitokenSpeller(List<String> filePaths) {
    if (oneSpace != null) {
      return;
    }
    oneSpace = new HashMap<>();
    twoSpaces = new HashMap<>();
    threeSpaces = new HashMap<>();
    hyphenated = new HashMap<>();
    for (String filePath : filePaths) {
      try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(filePath);
           BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.isEmpty() || line.charAt(0) == '#') { // ignore comments
            continue;
          }
          line = language.prepareLineForSpeller(StringUtils.substringBefore(line, "#").trim());
          if (line.isEmpty()) {
            continue;
          }
          int numSpaces = StringTools.numberOf(line, " ");
          int numHyphens = StringTools.numberOf(line, "-");
          if (numSpaces==1) {
            oneSpace.put(line, StringTools.removeDiacritics(line.toLowerCase()));
          } else if (numSpaces==2) {
            twoSpaces.put(line, StringTools.removeDiacritics(line.toLowerCase()));
          } else if (numSpaces>=3) {
            threeSpaces.put(line, StringTools.removeDiacritics(line.toLowerCase()));
          } else if (numSpaces==0 && numHyphens==1) {
            hyphenated.put(line, StringTools.removeDiacritics(line.toLowerCase()));
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private HashMap<String, String> chooseHashMap(String word, int numSpaces, int numHyphens) {
    if (numSpaces==1) {
      return oneSpace;
    } else if (numSpaces==2) {
      return twoSpaces;
    } else if (numSpaces>=3) {
      return threeSpaces;
    } else if (numSpaces==0 && numHyphens==1) {
      return hyphenated;
    }
    return new HashMap<>();
  }

  private boolean discardRunOnWords(String underlinedError) throws IOException {
    String parts[] = underlinedError.split(" ");
    if (parts.length == 2) {
      if (StringTools.isCapitalizedWord(parts[1])) {
        return false;
      }
      String sugg1a = parts[0].substring(0, parts[0].length() - 1);
      String sugg1b = parts[0].substring(parts[0].length() - 1) + parts[1];
      if (!spellingRule.isMisspelled(sugg1a) && !spellingRule.isMisspelled(sugg1b)) {
        return true;
      }
      String sugg2a = parts[0] + parts[1].substring(0,1);
      String sugg2b = parts[1].substring(1, parts[1].length());
      if (!spellingRule.isMisspelled(sugg2a) && !spellingRule.isMisspelled(sugg2b)) {
        return true;
      }
    }
    return false;
  }

}
