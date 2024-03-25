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
import java.util.regex.Pattern;

import static java.util.regex.Pattern.*;

public class MultitokenSpeller {

  private static final int MAX_LENGTH_DIFF = 3;
  private static final Pattern WHITESPACE_AND_SEP = compile("\\p{Zs}+");
  private static final Pattern DASH_SPACE = compile("- ");
  private static final Pattern SPACE_DASH = compile(" -");

  private final SpellingCheckRule spellingRule;
  private final Language language;

  private HashMap<Character, HashMap<String, List<String>>> suggestionsMap;
  private HashMap<String, List<String>> suggestionsMapNoSpacesKey;

  /*
   * Ultra-naive speller that provides spelling suggestions for multitoken words from a list of words.
   * The fastest method, so far.
   */

  public MultitokenSpeller(Language lang, List<String> filePaths) {
    language = lang;
    spellingRule = lang.getDefaultSpellingRule();
    initMultitokenSpeller(filePaths);
  }

  public List<String> getSuggestions(String originalWord) throws IOException {
    return getSuggestions(originalWord, false);
  }

  public List<String> getSuggestions(String originalWord, boolean areTokensAcceptedBySpeller) throws IOException {
    originalWord = WHITESPACE_AND_SEP.matcher(originalWord).replaceAll(" ");
    String word = DASH_SPACE.matcher(originalWord).replaceAll("-");
    word = SPACE_DASH.matcher(word).replaceAll("-");
    if (discardRunOnWords(word)) {
     return Collections.emptyList();
    }
    String normalizedWord = getNormalizeKey(word);
    List<WeightedSuggestion> weightedCandidates = new ArrayList<>();
    // try searching the key
    String normalizedWordNoSpaces = normalizedWord.replaceAll(" ","");
    if (suggestionsMapNoSpacesKey.containsKey(normalizedWordNoSpaces)) {
      List<String> candidates = suggestionsMapNoSpacesKey.get(normalizedWordNoSpaces);
      if (stopSearching(candidates, originalWord)) {
        return Collections.emptyList();
      }
      for (String candidate : candidates) {
        weightedCandidates.add(new WeightedSuggestion(candidate, 0));
      }
    }
    Character firstChar = normalizedWord.charAt(0);
    if (weightedCandidates.isEmpty() && suggestionsMap.containsKey(firstChar) ) {
      for (Map.Entry<String, List<String>> entry : suggestionsMap.get(firstChar).entrySet()) {
        String normalizedCandidate = entry.getKey();
        List<String> candidates = entry.getValue();
        if (stopSearching(candidates, originalWord)) {
          return Collections.emptyList();
        }
        if (Math.abs(normalizedCandidate.length() - word.length()) > MAX_LENGTH_DIFF) {
          continue;
        }
        String[] candidateParts = normalizedCandidate.split(" ");
        String[] wordParts = normalizedWord.split(" ");
        List<Integer> distances = distancesPerWord(candidateParts, wordParts, normalizedCandidate, normalizedWord);
        int totalDistance = distances.stream().reduce(0, Integer::sum);
        if (totalDistance < 1) {
          for (String candidate : candidates) {
            weightedCandidates.add(new WeightedSuggestion(candidate, 0));
          }
          // "continue" allows several candidates with different casing
          if (weightedCandidates.size() == 2) {
            break;
          }
          continue;
        }
        // for very short candidates, allow only distance=0 (casing and diacritics differences)
        if (normalizedCandidate.length() < 7) {
          continue;
        }
        boolean exceedsMaxDistancePerToken = false;
        for (int i=0; i<distances.size(); i++) {
          // usually 2, but 1 for short words
          int maxDistance = (wordParts[i].length() > 5 && candidateParts[i].length() > 4 ? 2: 1);
          if (distances.get(i) > maxDistance) {
            exceedsMaxDistancePerToken = true;
            break;
          }
        }
        if (exceedsMaxDistancePerToken) {
          continue;
        }
        if (totalDistance <= maxEditDistance(normalizedCandidate, normalizedWord)) {
          for (String candidate : candidates) {
            weightedCandidates.add(new WeightedSuggestion(candidate, totalDistance));
          }
        }
      }
    }
    if (weightedCandidates.isEmpty()) {
      return Collections.emptyList();
    }
    Collections.sort(weightedCandidates);
    List<String> results = new ArrayList<>();
    int weightFirstCandidate = weightedCandidates.get(0).getWeight();
    if (areTokensAcceptedBySpeller && weightedCandidates.get(0).getWord().toUpperCase().equals(originalWord)) {
      // don't correct all-upper case words accepted by the speller
      return Collections.emptyList();
    }
    if (areTokensAcceptedBySpeller && weightFirstCandidate > 1) {
      return Collections.emptyList();
    }
    for (WeightedSuggestion weightedCandidate : weightedCandidates) {
      // keep only cadidates with the distance of the first candidate
      if (weightedCandidate.getWeight() - weightFirstCandidate < 1) {
        results.add(weightedCandidate.getWord());
      }
    }
    return results;
  }

  private boolean stopSearching(List<String> candidates, String originalWord) {
    for (String candidate : candidates) {
      if (isException(originalWord, candidate)) {
        return true;
      }
      if (candidate.equals(originalWord)) {
        return true;
      }
    }
    for (String candidate : candidates) {
      if (candidate.equals(candidate.toLowerCase())
        && StringTools.convertToTitleCaseIteratingChars(candidate).equals(originalWord)) {
        return true;
      }
    }
    return false;
  }

  private int maxEditDistance(String normalizedCandidate, String normalizedWord) {
    int totalLength = normalizedWord.length();
    int correctLength = totalLength - numberOfCorrectChars(normalizedCandidate, normalizedWord);
    float firstCharWrong = firstCharacterDistances(normalizedCandidate, normalizedWord).stream().reduce((float) 0, Float::sum);
    if (correctLength <= 7) {
      return (int) (2 - firstCharWrong);
    }
    return (int) (2 + 0.25 * (correctLength - 7) - 0.6 * firstCharWrong);
  }

  private List<Integer> distancesPerWord(String[] parts1, String[] parts2, String s1, String s2) {
    List<Integer> distances = new ArrayList<>();
    if (parts1.length == parts2.length && parts1.length > 1) {
      for (int i=0; i<parts1.length; i++) {
        distances.add(levenshteinDistance(parts1[i], parts2[i]));
      }
    } else {
      distances.add(levenshteinDistance(s1, s2));
    }
    return distances;
  }

  private List<Float> firstCharacterDistances(String s1, String s2) {
    List<Float> distances = new ArrayList<>();
    String[] parts1 = s1.split(" ");
    String[] parts2 = s2.split(" ");
    // for now, only phrase with two tokens
    if (parts1.length == parts2.length && parts1.length == 2) {
      for (int i=0; i<parts1.length; i++) {
        distances.add(charDistance(parts1[i].charAt(0), parts2[i].charAt(0)));
      }
    } else {
      distances.add((float) 0);
    }
    return distances;
  }

  private float charDistance (char a, char b) {
    //TODO: full keyboard distances
    if (a == b) {
      return 0;
    }
    if (a == 's' && b == 'z' || a == 'z' && b == 's') {
      return 0.2F;
    }
    if (a == 'b' && b == 'v' || a == 'v' && b == 'b') {
      return 0.2F;
    }
    if (a == 'i' && b == 'y' || a == 'y' && b == 'i') {
      return 0;
    }
    return 1;
  }

  private int levenshteinDistance(String s1, String s2) {
    if (s1.replaceAll(" ","").equals(s2.replaceAll(" ",""))) {
      return 0;
    }
    int distance = LevenshteinDistance.getDefaultInstance().apply(s1, s2);
    String ns1= normalizeSimilarChars(s1);
    String ns2= normalizeSimilarChars(s2);
    if (!s1.equals(ns1) || !s2.equals(ns2)) {
      distance = Math.min(distance, LevenshteinDistance.getDefaultInstance().apply(normalizeSimilarChars(s1), normalizeSimilarChars(s2)));
    }
    // consider transpositions without having a Damerau-Levenshtein method
    if (distance > 1 && StringTools.isAnagram(s1,s2)) {
      distance--;
    }
    if (distance > 0 && s1.length()==s2.length() && StringTools.isAnagram(s1,s2)) {
      distance = 1;
    }
    return distance;
  }

  private String normalizeSimilarChars(String s) {
    return s.replaceAll("y", "i").replaceAll("ko", "co").replaceAll("ka", "ca");
  }

  private int numberOfCorrectChars(String s1, String s2) {
    String[] parts1 = s1.split(" ");
    String[] parts2 = s2.split(" ");
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
    if (suggestionsMap != null) {
      return;
    }
    suggestionsMap = new HashMap<>();
    suggestionsMapNoSpacesKey = new HashMap<>();
    for (String filePath : filePaths) {
      try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(filePath);
           BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
        String lineOriginal;
        while ((lineOriginal = reader.readLine()) != null) {
          if (lineOriginal.isEmpty() || lineOriginal.charAt(0) == '#') { // ignore comments
            continue;
          }
          for (String line : language.prepareLineForSpeller(StringUtils.substringBefore(lineOriginal, "#").trim())) {
            if (line.isEmpty()) {
              continue;
            }
            String normalizedKey = getNormalizeKey(line);
            if (!normalizedKey.contains(" ")) {
              //Ignore one-token suggestions. They are provided by the spelling rule, or other rules
              continue;
            }
            Character firstChar = normalizedKey.charAt(0);
            HashMap<String, List<String>> suggestionsMapByChar = suggestionsMap.computeIfAbsent(firstChar, k -> new HashMap<>());
            addToMap(suggestionsMapByChar, normalizedKey, line);
            addToMap(suggestionsMapNoSpacesKey, normalizedKey.replaceAll(" ",""), line);
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static void addToMap(Map<String, List<String>> map, String key, String value) {
    List<String> list = map.computeIfAbsent(key, k -> new ArrayList<>());
    if (!list.contains(value)) {
      list.add(value);
    }
  }

  private static String getNormalizeKey(String word) {
    return StringTools.removeDiacritics(word.toLowerCase()).replaceAll("-", " ");
  }

  private boolean discardRunOnWords(String underlinedError) throws IOException {
    String[] parts = underlinedError.split(" ");
    if (parts.length == 2) {
      if (StringTools.isCapitalizedWord(parts[1])) {
        return false;
      }
      if (parts[0].isEmpty() || parts[1].isEmpty()) {
        // probably emojis, or mal-formed chars
        return true;
      }
      String sugg1a = parts[0].substring(0, parts[0].length() - 1);
      String sugg1b = parts[0].substring(parts[0].length() - 1) + parts[1];
      if (!spellingRule.isMisspelled(sugg1a) && !spellingRule.isMisspelled(sugg1b)) {
        return true;
      }
      String sugg2a = parts[0] + parts[1].charAt(0);
      String sugg2b = parts[1].substring(1);
      return !spellingRule.isMisspelled(sugg2a) && !spellingRule.isMisspelled(sugg2b);
    }
    return false;
  }

  protected boolean isException(String original, String candidate) {
    return false;
  }

}
