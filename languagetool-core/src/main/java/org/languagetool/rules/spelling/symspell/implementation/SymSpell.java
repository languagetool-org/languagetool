package org.languagetool.rules.spelling.symspell.implementation;
//        MIT License
//
//        Copyright (c) 2018 Hampus Londögård
//
//        Permission is hereby granted, free of charge, to any person obtaining a copy
//        of this software and associated documentation files (the "Software"), to deal
//        in the Software without restriction, including without limitation the rights
//        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//        copies of the Software, and to permit persons to whom the Software is
//        furnished to do so, subject to the following conditions:
//
//        The above copyright notice and this permission notice shall be included in all
//        copies or substantial portions of the Software.
//
//        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//        SOFTWARE.

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymSpell implements Serializable {
  public enum Verbosity {
    Top,
    Closest,
    All
  }

  private static int defaultMaxEditDistance = 2;
  private static int defaultPrefixLength = 7;
  private static int defaultCountThreshold = 1;
  private static int defaultInitialCapacity = 16;
  private static int defaultCompactLevel = 5;
  private int initialCapacity;
  private int maxDictionaryEditDistance;
  private int prefixLength; //prefix length  5..7
  private long countThreshold; //a threshold might be specified, when a term occurs so frequently in the corpus that it is considered a valid word for spelling correction
  private int compactMask;
  private EditDistance.DistanceAlgorithm distanceAlgorithm = EditDistance.DistanceAlgorithm.Damerau;
  private int maxLength;
  private Map<Integer, String[]> deletes;
  // Dictionary of unique correct spelling words, and the frequency count for each word.
  private Map<String, Long> words;
  // Dictionary of unique words that are below the count threshold for being considered correct spellings.
  private Map<String, Long> belowThresholdWords = new HashMap<>();
  /// <summary>Spelling suggestion returned from lookup.</summary>

  /// <summary>Create a new instanc of SymSpell.SymSpell.</summary>
  /// <remarks>Specifying ann accurate initialCapacity is not essential,
  /// but it can help speed up processing by aleviating the need for
  /// data restructuring as the size grows.</remarks>
  /// <param name="initialCapacity">The expected number of words in dictionary.</param>
  /// <param name="maxDictionaryEditDistance">Maximum edit distance for doing lookups.</param>
  /// <param name="prefixLength">The length of word prefixes used for spell checking..</param>
  /// <param name="countThreshold">The minimum frequency count for dictionary words to be considered correct spellings.</param>
  /// <param name="compactLevel">Degree of favoring lower memory use over speed (0=fastest,most memory, 16=slowest,least memory).</param>
  public SymSpell(int initialCapacity, int maxDictionaryEditDistance, int prefixLength, int countThreshold)//,
  //byte compactLevel)
  {
    if (initialCapacity < 0) {
      initialCapacity = defaultInitialCapacity;
    }
    if (maxDictionaryEditDistance < 0) {
      maxDictionaryEditDistance = defaultMaxEditDistance;
    }
    if (prefixLength < 1 || prefixLength <= maxDictionaryEditDistance) {
      prefixLength = defaultPrefixLength;
    }
    if (countThreshold < 0) {
      countThreshold = defaultCountThreshold;
    }
//        compactLevel = (byte) defaultCompactLevel;   //TODO might be faulty...

    this.initialCapacity = initialCapacity;
    this.words = new HashMap<>(initialCapacity);
    this.maxDictionaryEditDistance = maxDictionaryEditDistance;
    this.prefixLength = prefixLength;
    this.countThreshold = countThreshold;
//        if (compactLevel > 16) compactLevel = 16;
    this.compactMask = (0xffffffff >> (3 + defaultCompactLevel)) << 2;
  }

  /// <summary>Create/Update an entry in the dictionary.</summary>
  /// <remarks>For every word there are deletes with an edit distance of 1..maxEditDistance created and added to the
  /// dictionary. Every delete entry has a suggestions list, which points to the original term(s) it was created from.
  /// The dictionary may be dynamically updated (word frequency and new words) at any time by calling createDictionaryEntry</remarks>
  /// <param name="key">The word to add to dictionary.</param>
  /// <param name="count">The frequency count for word.</param>
  /// <param name="staging">Optional staging object to speed up adding many entries by staging them to a temporary structure.</param>
  /// <returns>True if the word was added as a new correctly spelled word,
  /// or false if the word is added as a below threshold word, or updates an
  /// existing correctly spelled word.</returns>
  public boolean createDictionaryEntry(String key, long count, SuggestionStage staging) {
    if (count <= 0) {
      if (this.countThreshold > 0) {
        return false; // no point doing anything if count is zero, as it can't change anything
      }
      count = 0;
    }
    long countPrevious;

    // look first in below threshold words, update count, and allow promotion to correct spelling word if count reaches threshold
    // threshold must be >1 for there to be the possibility of low threshold words
    if (countThreshold > 1 && belowThresholdWords.containsKey(key)) {
      countPrevious = belowThresholdWords.get(key);
      // calculate new count for below threshold word
      count = (Long.MAX_VALUE - countPrevious > count) ? countPrevious + count : Long.MAX_VALUE;
      // has reached threshold - remove from below threshold collection (it will be added to correct words below)
      if (count >= countThreshold) {
        belowThresholdWords.remove(key);
      } else {
        belowThresholdWords.put(key, count); // = count;
        return false;
      }
    } else if (words.containsKey(key)) {
      countPrevious = words.get(key);
      // just update count if it's an already added above threshold word
      count = (Long.MAX_VALUE - countPrevious > count) ? countPrevious + count : Long.MAX_VALUE;
      words.put(key, count);
      return false;
    } else if (count < countThreshold) {
      // new or existing below threshold word
      belowThresholdWords.put(key, count);
      return false;
    }

    // what we have at this point is a new, above threshold word
    words.put(key, count);
    if (key.equals("can't")) {
      System.out.println("Added to words..!");
    }

    //edits/suggestions are created only once, no matter how often word occurs
    //edits/suggestions are created only as soon as the word occurs in the corpus,
    //even if the same term existed before in the dictionary as an edit from another word
    if (key.length() > maxLength) {
      maxLength = key.length();
    }

    //create deletes
    HashSet<String> edits = editsPrefix(key);

    // if not staging suggestions, put directly into main data structure
    if (staging != null) {
      edits.forEach(delete -> staging.add(getStringHash(delete), key));
    } else {
      if (deletes == null) {
        this.deletes = new HashMap<>(initialCapacity); //initialisierung
      }

      edits.forEach(delete -> {
        int deleteHash = getStringHash(delete);
        String[] suggestions;
        if (deletes.containsKey(deleteHash)) {
          suggestions = deletes.get(deleteHash);
          String[] newSuggestions = Arrays.copyOf(suggestions, suggestions.length + 1);
          deletes.put(deleteHash, newSuggestions);
          suggestions = newSuggestions;
        } else {
          suggestions = new String[1];
          deletes.put(deleteHash, suggestions);
        }
        suggestions[suggestions.length - 1] = key;
      });
    }
    return true;
  }

  /// <summary>Load multiple dictionary entries from a file of word/frequency count pairs</summary>
  /// <remarks>Merges with any dictionary data already loaded.</remarks>
  /// <param name="corpus">The path+filename of the file.</param>
  /// <param name="termIndex">The column position of the word.</param>
  /// <param name="countIndex">The column position of the frequency count.</param>
  /// <returns>True if file loaded, or false if file not found.</returns>
  public boolean loadDictionary(String corpus, int termIndex, int countIndex) {
    File file = new File(corpus);
    if (!file.exists()) {
      return false;
    }

    BufferedReader br = null;
    try {
      br = Files.newBufferedReader(Paths.get(corpus), StandardCharsets.UTF_8);
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
    if (br == null) {
      return false;
    }
    return loadDictionary(br, termIndex, countIndex);
  }

  /// <summary>Load multiple dictionary entries from an input stream of word/frequency count pairs</summary>
  /// <remarks>Merges with any dictionary data already loaded.</remarks>
  /// <remarks>This is useful for loading the dictionary data from an asset file in Android.</remarks>
  /// <param name="corpus">An input stream to dictionary data.</param>
  /// <param name="termIndex">The column position of the word.</param>
  /// <param name="countIndex">The column position of the frequency count.</param>
  /// <returns>True if file loaded, or false if file not found.</returns>
  public boolean loadDictionary(InputStream corpus, int termIndex, int countIndex) {
    if (corpus == null) {
      return false;
    }
    BufferedReader br = new BufferedReader(new InputStreamReader(corpus, StandardCharsets.UTF_8));
    return loadDictionary(br, termIndex, countIndex);
  }

  /// <summary>Load multiple dictionary entries from an buffered reader of word/frequency count pairs</summary>
  /// <remarks>Merges with any dictionary data already loaded.</remarks>
  /// <param name="corpus">An buffered reader to dictionary data.</param>
  /// <param name="termIndex">The column position of the word.</param>
  /// <param name="countIndex">The column position of the frequency count.</param>
  /// <returns>True if file loaded, or false if file not found.</returns>
  public boolean loadDictionary(BufferedReader br, int termIndex, int countIndex) {
    if (br == null) {
      return false;
    }

    SuggestionStage staging = new SuggestionStage(16384);
    try {
      for (String line; (line = br.readLine()) != null; ) {
        String[] lineParts = line.split("\\s");
        if (lineParts.length >= 2) {
          String key = lineParts[termIndex];
          long count;
          try {
            count = Long.parseLong(lineParts[countIndex]);
            //count = Long.parseUnsignedLong(lineParts[countIndex]);
            createDictionaryEntry(key, count, staging);
          } catch (NumberFormatException ex) {
            System.out.println(ex.getMessage());
          }
        }
      }
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }
    if (this.deletes == null) {
      this.deletes = new HashMap<>(staging.deleteCount());
    }
    commitStaged(staging);
    return true;
  }

  //create a frequency dictionary from a corpus (merges with any dictionary data already loaded)
  /// <summary>Load multiple dictionary words from a file containing plain text.</summary>
  /// <param name="corpus">The path+filename of the file.</param>
  /// <returns>True if file loaded, or false if file not found.</returns>
  public boolean createDictionary(String corpus) {
    File file = new File(corpus);
    if (!file.exists()) {
      return false;
    }

    SuggestionStage staging = new SuggestionStage(16384);
    try (BufferedReader br = Files.newBufferedReader(Paths.get(corpus))) {
      for (String line; (line = br.readLine()) != null; ) {
        Arrays.stream(parseWords(line)).forEach(key -> createDictionaryEntry(key, 1, staging));
      }
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    }

    if (this.deletes == null) {
      this.deletes = new HashMap<>(staging.deleteCount());
    }
    commitStaged(staging);
    return true;
  }

  public void purgeBelowThresholdWords() {
    belowThresholdWords = new HashMap<String, Long>();
  }

  /// <summary>Commit staged dictionary additions.</summary>
  /// <remarks>Used when you write your own process to load multiple words into the
  /// dictionary, and as part of that process, you first created a SuggestionsStage
  /// object, and passed that to createDictionaryEntry calls.</remarks>
  /// <param name="staging">The SymSpell.SuggestionStage object storing the staged data.</param>
  public void commitStaged(SuggestionStage staging) {
    if (this.deletes == null) {
      this.deletes = new HashMap<>(staging.deletes.size());
    }
    staging.commitTo(deletes);
  }

  /// <summary>Find suggested spellings for a given input word, using the maximum
  /// edit distance specified during construction of the SymSpell.SymSpell dictionary.</summary>
  /// <param name="input">The word being spell checked.</param>
  /// <param name="verbosity">The value controlling the quantity/closeness of the returned suggestions.</param>
  /// <returns>A List of SymSpell.SuggestItem object representing suggested correct spellings for the input word,
  /// sorted by edit distance, and secondarily by count frequency.</returns>
  public List<SuggestItem> lookup(String input, Verbosity verbosity) {
    return lookup(input, verbosity, maxDictionaryEditDistance);
  }

  /// <summary>Find suggested spellings for a given input word.</summary>
  /// <param name="input">The word being spell checked.</param>
  /// <param name="verbosity">The value controlling the quantity/closeness of the returned suggestions.</param>
  /// <param name="maxEditDistance">The maximum edit distance between input and suggested words.</param>
  /// <returns>A List of SymSpell.SuggestItem object representing suggested correct spellings for the input word,
  /// sorted by edit distance, and secondarily by count frequency.</returns>
  public List<SuggestItem> lookup(String input, Verbosity verbosity, int maxEditDistance) {
    //verbosity=Top: the suggestion with the highest term frequency of the suggestions of smallest edit distance found
    //verbosity=Closest: all suggestions of smallest edit distance found, the suggestions are ordered by term frequency
    //verbosity=All: all suggestions <= maxEditDistance, the suggestions are ordered by edit distance, then by term frequency (slower, no early termination)

    // maxEditDistance used in lookup can't be bigger than the maxDictionaryEditDistance
    // used to construct the underlying dictionary structure.
    if (maxEditDistance > maxDictionaryEditDistance) {
      throw new IllegalArgumentException("Dist to big: " + maxEditDistance);
    }

    List<SuggestItem> suggestions = new ArrayList<>();
    int inputLen = input.length();

    // early exit - word is too big to possibly match any words
    if (inputLen - maxEditDistance > maxLength) {
      return suggestions;
    }

    // deletes we've considered already
    HashSet<String> consideredDeletes = new HashSet<>();
    // suggestions we've considered already
    HashSet<String> consideredSuggestions = new HashSet<>();
    long suggestionCount;

    // quick look for exact match
    if (words.containsKey(input)) {
      suggestionCount = words.get(input);
      suggestions.add(new SuggestItem(input, 0, suggestionCount));
      // early exit - return exact match, unless caller wants all matches
      if (verbosity != Verbosity.All) {
        return suggestions;
      }
    }
    consideredSuggestions.add(input); // input considered in above.

    int maxEditDistance2 = maxEditDistance;
    int candidatePointer = 0;
    List<String> candidates = new ArrayList<>();

    //add original prefix
    int inputPrefixLen = inputLen;
    if (inputPrefixLen > prefixLength) {
      inputPrefixLen = prefixLength;
      candidates.add(input.substring(0, inputPrefixLen));
    } else {
      candidates.add(input);
    }

    EditDistance distanceComparer = new EditDistance(input, this.distanceAlgorithm);
    while (candidatePointer < candidates.size()) {
      String candidate = candidates.get(candidatePointer++);
      int candidateLen = candidate.length();
      int lengthDiff = inputPrefixLen - candidateLen;

      //early termination if distance higher than suggestion distance
      if (lengthDiff > maxEditDistance2) {
        // skip to next candidate if Verbosity.All, look no further if Verbosity.Top or Closest
        // (candidates are ordered by delete distance, so none are closer than current)
        if (verbosity == Verbosity.All) {
          continue;
        }
        break;
      }

      //read candidate entry from dictionary
      if (deletes.containsKey(getStringHash(candidate))) {
        String[] dictSuggestions = deletes.get(getStringHash(candidate));
        //iterate through suggestions (to other correct dictionary items) of delete item and add them to suggestion list
        for (String suggestion : dictSuggestions) {
          if (suggestion.equals(input)) {
            continue;
          }
          int suggestionLen = suggestion.length();

          if ((Math.abs(suggestionLen - inputLen) > maxEditDistance2) // input/suggestion diff > allowed/current best distance
            || (suggestionLen < candidateLen) // sugg must be for a different delete String, in same bin only because of hash collision
            || (suggestionLen == candidateLen && !suggestion.equals(candidate))) // if sugg len = delete len, then it either equals delete or is in same bin only because of hash collision
          {
            continue;
          }

          int suggPrefixLen = Math.min(suggestionLen, prefixLength);
          if (suggPrefixLen > inputPrefixLen && (suggPrefixLen - candidateLen) > maxEditDistance2) {
            continue;
          }

          //True Damerau-Levenshtein Edit Distance: adjust distance, if both distances > 0
          //We allow simultaneous edits (deletes) of maxEditDistance on on both the dictionary and the input term.
          //For replaces and adjacent transposes the resulting edit distance stays <= maxEditDistance.
          //For inserts and deletes the resulting edit distance might exceed maxEditDistance.
          //To prevent suggestions of a higher edit distance, we need to calculate the resulting edit distance, if there are simultaneous edits on both sides.
          //Example: (bank==bnak and bank==bink, but bank!=kanb and bank!=xban and bank!=baxn for maxEditDistance=1)
          //Two deletes on each side of a pair makes them all equal, but the first two pairs have edit distance=1, the others edit distance=2.
          int distance;
          int min = 0;
          if (candidateLen == 0) {
            //suggestions which have no common chars with input (inputLen<=maxEditDistance && suggestionLen<=maxEditDistance)
            distance = Math.max(inputLen, suggestionLen);
            if (distance > maxEditDistance2 || !consideredSuggestions.add(suggestion)) {
              continue;
            }
          } else if (suggestionLen == 1) {
            if (input.indexOf(suggestion.charAt(0)) < 0) {
              distance = inputLen;
            } else {
              distance = inputLen - 1;
            }
            if (distance > maxEditDistance2 || !consideredSuggestions.add(suggestion)) {
              continue;
            }
          } else
            //number of edits in prefix == maxeditdistance  && no identical suffix
            //, then editdistance > maxEditDistance and no need for Levenshtein calculation
            //      (inputLen >= prefixLength) && (suggestionLen >= prefixLength)
            if ((prefixLength - maxEditDistance == candidateLen)
              && (((min = Math.min(inputLen, suggestionLen) - prefixLength) > 1)
              && !(input.substring(inputLen + 1 - min).equals(suggestion.substring(suggestionLen + 1 - min))))
              || ((min > 0) && (input.charAt(inputLen - min) != suggestion.charAt(suggestionLen - min))
              && ((input.charAt(inputLen - min - 1) != suggestion.charAt(suggestionLen - min))
              || (input.charAt(inputLen - min) != suggestion.charAt(suggestionLen - min - 1))))) {
              continue;
            } else {
              // deleteInSuggestionPrefix is somewhat expensive, and only pays off when verbosity is Top or Closest.
              if ((verbosity != Verbosity.All && !deleteInSuggestionPrefix(candidate, candidateLen, suggestion, suggestionLen))
                || !consideredSuggestions.add(suggestion)) {
                continue;
              }
              distance = distanceComparer.compare(suggestion, maxEditDistance2);
              if (distance < 0) {
                continue;
              }
            }

          //save some time
          //do not process higher distances than those already found, if verbosity<All (note: maxEditDistance2 will always equal maxEditDistance when Verbosity.All)
          if (distance <= maxEditDistance2) {
            suggestionCount = words.get(suggestion);
            SuggestItem si = new SuggestItem(suggestion, distance, suggestionCount);
            if (suggestions.size() > 0) {
              switch (verbosity) {
                case Closest:
                  //we will calculate DamLev distance only to the smallest found distance so far
                  if (distance < maxEditDistance2) {
                    suggestions.clear();
                  }
                  break;
                case Top:
                  if (distance < maxEditDistance2 || suggestionCount > suggestions.get(0).count) {
                    maxEditDistance2 = distance;
                    suggestions.set(0, si);
                  }
                  continue;
              }
            }
            if (verbosity != Verbosity.All) {
              maxEditDistance2 = distance;
            }
            suggestions.add(si);
          }
        }
      }

      //add edits
      //derive edits (deletes) from candidate (input) and add them to candidates list
      //this is a recursive process until the maximum edit distance has been reached
      if ((lengthDiff < maxEditDistance) && (candidateLen <= prefixLength)) {
        //save some time
        //do not create edits with edit distance smaller than suggestions already found
        if (verbosity != Verbosity.All && lengthDiff >= maxEditDistance2) {
          continue;
        }

        for (int i = 0; i < candidateLen; i++) {
          StringBuilder sb = new StringBuilder(candidate);
          sb.deleteCharAt(i);
          String delete = sb.toString();

          if (consideredDeletes.add(delete)) {
            candidates.add(delete);
          }
        }
      }
    }

    //sort by ascending edit distance, then by descending word frequency
    if (suggestions.size() > 1) {
      Collections.sort(suggestions);
    }
    return suggestions;
  }

  public List<SuggestItem> lookupCompound(String input, int maxEditDistance) {
    //parse input String into single terms
    if (maxEditDistance > maxDictionaryEditDistance) {
      throw new IllegalArgumentException("Dist to big " + maxEditDistance);
    }
    String[] termList1 = parseWords(input);

    List<SuggestItem> suggestions; //suggestions for a single term
    List<SuggestItem> suggestionParts = new ArrayList<>(); // 1 line with separate parts
    List<SuggestItem> suggestionsCombi;
    EditDistance editDistance;

    //translate every term to its best suggestion, otherwise it remains unchanged
    boolean lastCombi = false;
    for (int i = 0; i < termList1.length; i++) {      // For each term do loop
      suggestions = lookup(termList1[i], Verbosity.Top, maxEditDistance); // Get the normal suggestions,
//            suggestions.forEach(it -> System.out.println("Suggestions: " + it.term));
      //combi check, always before split. i > 0 because we can't split on zero obviously.
      if ((i > 0) && !lastCombi) {
        suggestionsCombi = lookup(termList1[i - 1] + termList1[i], Verbosity.Top, maxEditDistance);

        if (!suggestionsCombi.isEmpty()) {
          SuggestItem best1 = suggestionParts.get(suggestionParts.size() - 1);    // Grabbing the currently last part of sentence (i-1)
          SuggestItem best2;
          if (!suggestions.isEmpty()) {
            best2 = suggestions.get(0);                 // Getting the best suggestion of term (i)
          } else {
            best2 = new SuggestItem(termList1[i], maxEditDistance + 1, 0); // No suggestion -> it might be correct? (i)
          }

          editDistance = new EditDistance(termList1[i - 1] + " " + termList1[i], EditDistance.DistanceAlgorithm.Damerau);
          if (suggestionsCombi.get(0).distance + 1 < editDistance.DamerauLevenshteinDistance(best1.term + " " + best2.term, maxEditDistance)) {
            suggestionsCombi.get(0).distance++;
            suggestionParts.set(suggestionParts.size() - 1, suggestionsCombi.get(0));   // Replacing value.
            lastCombi = true;
            continue;
          }
        }
      }

      lastCombi = false;

      //always split terms without suggestion / never split terms with suggestion ed=0 / never split single char terms
      if (!suggestions.isEmpty() && ((suggestions.get(0).distance == 0) || (termList1[i].length() == 1))) {
        //choose best suggestion
        suggestionParts.add(suggestions.get(0));
      } else {
        //if no perfect suggestion, split word into pairs
        List<SuggestItem> suggestionsSplit = new ArrayList<>();

        //add original term
        if (!suggestions.isEmpty()) {
          suggestionsSplit.add(suggestions.get(0));
        }

        if (termList1[i].length() > 1) {
          for (int j = 1; j < termList1[i].length(); j++) {      // Begin splitting! j=1 -> last. Shouldnt it be j.size - 1?
            String part1 = termList1[i].substring(0, j);
            String part2 = termList1[i].substring(j);
            SuggestItem suggestionSplit;
            List<SuggestItem> suggestions1 = lookup(part1, Verbosity.Top, maxEditDistance);

            if (!suggestions1.isEmpty()) {
              if (!suggestions.isEmpty() && (suggestions.get(0).equals(suggestions1.get(0)))) {
                continue; // suggestion top = split_1 suggestion top
              }
              List<SuggestItem> suggestions2 = lookup(part2, Verbosity.Top, maxEditDistance);

              if (!suggestions2.isEmpty()) {
                if (!suggestions.isEmpty() && (suggestions.get(0).equals(suggestions2.get(0)))) {
                  continue; //suggestion top = split_2 suggestion top
                }

                //select best suggestion for split pair
                String split = suggestions1.get(0).term + " " + suggestions2.get(0).term;
                editDistance = new EditDistance(termList1[i], EditDistance.DistanceAlgorithm.Damerau);
                suggestionSplit = new SuggestItem(split,
                  editDistance.DamerauLevenshteinDistance(split, maxEditDistance),
                  Math.min(suggestions1.get(0).count, suggestions2.get(0).count));
                if (suggestionSplit.distance >= 0) {
                  suggestionsSplit.add(suggestionSplit);
                }

                //early termination of split
                if (suggestionSplit.distance == 1) {
                  break;
                }
              }
            }
          }

          if (!suggestionsSplit.isEmpty()) {
            //select best suggestion for split pair
            Collections.sort(suggestionsSplit);
            suggestionParts.add(suggestionsSplit.get(0));
          } else {
            SuggestItem si = new SuggestItem(termList1[i], 0, maxEditDistance + 1);
            suggestionParts.add(si);
          }
        } else {
          SuggestItem si = new SuggestItem(termList1[i], 0, maxEditDistance + 1);
          suggestionParts.add(si);
        }
      }
    }

    SuggestItem suggestion = new SuggestItem("", Integer.MAX_VALUE, Long.MAX_VALUE);

    StringBuilder s = new StringBuilder();

    for (SuggestItem si : suggestionParts) {
      s.append(si.term).append(' ');
      suggestion.count = Math.min(suggestion.count, si.count);
    }

    suggestion.term = s.toString().replaceAll("\\s+$", "");
    editDistance = new EditDistance(suggestion.term, EditDistance.DistanceAlgorithm.Damerau);
    suggestion.distance = editDistance.DamerauLevenshteinDistance(input, maxDictionaryEditDistance);

    List<SuggestItem> suggestionsLine = new ArrayList<>();
    suggestionsLine.add(suggestion);
    return suggestionsLine;
  }

  //public bool enableCompoundCheck = true;
  //false: assumes input String as single term, no compound splitting / decompounding
  //true:  supports compound splitting / decompounding with three cases:
  //1. mistakenly inserted space into a correct word led to two incorrect terms
  //2. mistakenly omitted space between two correct words led to one incorrect combined term
  //3. multiple independent input terms with/without spelling errors

  public List<SuggestItem> lookupCompound(String input) {
    return lookupCompound(input, this.maxDictionaryEditDistance);
  }

  private boolean deleteInSuggestionPrefix(String delete, int deleteLen, String suggestion, int suggestionLen) {
    if (deleteLen == 0) {
      return true;
    }
    if (prefixLength < suggestionLen) {
      suggestionLen = prefixLength;
    }
    int j = 0;
    for (int i = 0; i < deleteLen; i++) {
      char delChar = delete.charAt(i);
      while (j < suggestionLen && delChar != suggestion.charAt(j)) {
        j++;
      }
      if (j == suggestionLen) {
        return false;
      }
    }
    return true;
  }

  private String[] parseWords(String text) {
    // \p{L} UTF-8 characters, plus "_", does not split words at apostrophes.
    Pattern pattern = Pattern.compile("['’\\p{L}-[_]]+");
    Matcher match = pattern.matcher(text.toLowerCase());
    List<String> matches = new ArrayList<>();
    while (match.find()) {
      matches.add(match.group());
    }
    String[] toreturn = new String[matches.size()];
    matches.toArray(toreturn);
    return toreturn;
  }

  private HashSet<String> edits(String word, int editDistance, HashSet<String> deleteWords) {
    editDistance++;
    if (word.length() > 1) {
      for (int i = 0; i < word.length(); i++) {
        StringBuilder sb = new StringBuilder(word);     //  word.Remove(i, 1);
        sb.deleteCharAt(i);
        String delete = sb.toString();
        if (deleteWords.add(delete)) {
          //recursion, if maximum edit distance not yet reached
          if (editDistance < maxDictionaryEditDistance) {
            edits(delete, editDistance, deleteWords);
          }
        }
      }
    }
    return deleteWords;
  }

  private HashSet<String> editsPrefix(String key) {
    HashSet<String> hashSet = new HashSet<>();
    if (key.length() <= maxDictionaryEditDistance) {
      hashSet.add("");
    }
    if (key.length() > prefixLength) {
      key = key.substring(0, prefixLength);
    }
    hashSet.add(key);
    return edits(key, 0, hashSet);
  }

  @SuppressWarnings("unchecked")
  private int getStringHash(String s) {
    int len = s.length();
    int lenMask = len;
    if (lenMask > 3) {
      lenMask = 3;
    }

    long hash = 2166136261L;
    for (int i = 0; i < len; i++) {
      hash ^= s.charAt(i);
      hash *= 16777619;
    }

    hash &= this.compactMask;
    hash |= lenMask;
    return (int) hash;
  }

  //######

  //WordSegmentation divides a String into words by inserting missing spaces at the appropriate positions
  //misspelled words are corrected and do not affect segmentation
  //existing spaces are allowed and considered for optimum segmentation

  //SymSpell.WordSegmentation uses a novel approach *without* recursion.
  //https://medium.com/@wolfgarbe/fast-word-segmentation-for-noisy-text-2c2c41f9e8da
  //While each String of length n can be segmentend in 2^n−1 possible compositions https://en.wikipedia.org/wiki/Composition_(combinatorics)
  //SymSpell.WordSegmentation has a linear runtime O(n) to find the optimum composition

  //number of all words in the corpus used to generate the frequency dictionary
  //this is used to calculate the word occurrence probability p from word counts c : p=c/N
  //N equals the sum of all counts c in the dictionary only if the dictionary is complete, but not if the dictionary is truncated or filtered
  private static long N = 1024908267229L;  // TODO make dynamic man.

  class SegmentedSuggestion {
    String segmentedString = "", correctedString = "";
    int distanceSum = 0;
    double probabilityLogSum = 0.0;

    SegmentedSuggestion() {
    }
  }

  /// <summary>Find suggested spellings for a multi-word input String (supports word splitting/merging).</summary>
  /// <param name="input">The String being spell checked.</param>
  /// <returns>The word segmented String,
  /// the word segmented and spelling corrected String,
  /// the Edit distance sum between input String and corrected String,
  /// the Sum of word occurrence probabilities in log scale (a measure of how common and probable the corrected segmentation is).</returns>
  public SegmentedSuggestion wordSegmentation(String input) {
    return wordSegmentation(input, this.maxDictionaryEditDistance, this.maxLength);
  }

  /// <summary>Find suggested spellings for a multi-word input String (supports word splitting/merging).</summary>
  /// <param name="input">The String being spell checked.</param>
  /// <param name="maxEditDistance">The maximum edit distance between input and corrected words
  /// (0=no correction/segmentation only).</param>
  /// <returns>The word segmented String,
  /// the word segmented and spelling corrected String,
  /// the Edit distance sum between input String and corrected String,
  /// the Sum of word occurrence probabilities in log scale (a measure of how common and probable the corrected segmentation is).</returns>
  public SegmentedSuggestion wordSegmentation(String input, int maxEditDistance) {
    return wordSegmentation(input, maxEditDistance, this.maxLength);
  }

  /// <summary>Find suggested spellings for a multi-word input String (supports word splitting/merging).</summary>
  /// <param name="input">The String being spell checked.</param>
  /// <param name="maxSegmentationWordLength">The maximum word length that should be considered.</param>
  /// <param name="maxEditDistance">The maximum edit distance between input and corrected words
  /// (0=no correction/segmentation only).</param>
  /// <returns>The word segmented String,
  /// the word segmented and spelling corrected String,
  /// the Edit distance sum between input String and corrected String,
  /// the Sum of word occurrence probabilities in log scale (a measure of how common and probable the corrected segmentation is).</returns>
  public SegmentedSuggestion wordSegmentation(String input, int maxEditDistance, int maxSegmentationWordLength) {
    if (input.isEmpty()) {
      return new SegmentedSuggestion();
    }
    int arraySize = Math.min(maxSegmentationWordLength, input.length());
    SegmentedSuggestion[] compositions = new SegmentedSuggestion[arraySize];
    for (int i = 0; i < arraySize; i++) {
      compositions[i] = new SegmentedSuggestion();
    }

    int circularIndex = -1;

    //outer loop (column): all possible part start positions
    for (int j = 0; j < input.length(); j++) {
      //inner loop (row): all possible part lengths (from start position): part can't be bigger than longest word in dictionary (other than long unknown word)
      int imax = Math.min(input.length() - j, maxSegmentationWordLength);
      for (int i = 1; i <= imax; i++) {
        //get top spelling correction/ed for part
        String part = input.substring(j, j + i);
        int separatorLength = 0;
        int topEd = 0;
        double topProbabilityLog;
        String topResult;

        if (Character.isWhitespace(part.charAt(0))) {
          //remove space for levensthein calculation
          part = part.substring(1);
        } else {
          //add ed+1: space did not exist, had to be inserted
          separatorLength = 1;
        }

        //remove space from part1, add number of removed spaces to topEd
        topEd += part.length();
        //remove space
        part = part.replace(" ", ""); //=System.Text.RegularExpressions.Regex.Replace(part1, @"\s+", "");
        //add number of removed spaces to ed
        topEd -= part.length();

        List<SuggestItem> results = this.lookup(part, SymSpell.Verbosity.Top, maxEditDistance);
        if (results.size() > 0) {
          topResult = results.get(0).term;
          topEd += results.get(0).distance;
          //Naive Bayes Rule
          //we assume the word probabilities of two words to be independent
          //therefore the resulting probability of the word combination is the product of the two word probabilities

          //instead of computing the product of probabilities we are computing the sum of the logarithm of probabilities
          //because the probabilities of words are about 10^-10, the product of many such small numbers could exceed (underflow) the floating number range and become zero
          //log(ab)=log(a)+log(b)
          topProbabilityLog = Math.log10((double) results.get(0).count / (double) N);
        } else {
          topResult = part;
          //default, if word not found
          //otherwise long input text would win as long unknown word (with ed=edmax+1 ), although there there should many spaces inserted
          topEd += part.length();
          topProbabilityLog = Math.log10(10.0 / (N * Math.pow(10.0, part.length())));
        }

        int destinationIndex = ((i + circularIndex) % arraySize);

        //set values in first loop
        if (j == 0) {
          compositions[destinationIndex].segmentedString = part;
          compositions[destinationIndex].correctedString = topResult;
          compositions[destinationIndex].distanceSum = topEd;
          compositions[destinationIndex].probabilityLogSum = topProbabilityLog;
        } else if ((i == maxSegmentationWordLength)
          //replace values if better probabilityLogSum, if same edit distance OR one space difference
          || (((compositions[circularIndex].distanceSum + topEd == compositions[destinationIndex].distanceSum) || (compositions[circularIndex].distanceSum + separatorLength + topEd == compositions[destinationIndex].distanceSum)) && (compositions[destinationIndex].probabilityLogSum < compositions[circularIndex].probabilityLogSum + topProbabilityLog))
          //replace values if smaller edit distance
          || (compositions[circularIndex].distanceSum + separatorLength + topEd < compositions[destinationIndex].distanceSum)) {
          compositions[destinationIndex].segmentedString = compositions[circularIndex].segmentedString + " " + part;
          compositions[destinationIndex].correctedString = compositions[circularIndex].correctedString + " " + topResult;
          compositions[destinationIndex].distanceSum = compositions[circularIndex].distanceSum + topEd;
          compositions[destinationIndex].probabilityLogSum = compositions[circularIndex].probabilityLogSum + topProbabilityLog;
        }
      }
      circularIndex++;
      if (circularIndex >= arraySize) {
        circularIndex = 0;
      }
    }
    return compositions[circularIndex];

  }
}

