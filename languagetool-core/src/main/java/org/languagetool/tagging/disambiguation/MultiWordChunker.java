/* LanguageTool, a natural language style checker
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.tagging.disambiguation;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tools.StringTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Multiword tagger-chunker.
 *
 * @author Marcin Miłkowski
 */
public class MultiWordChunker extends AbstractDisambiguator {

  private final String filename;
  private final boolean allowFirstCapitalized;
  private final boolean allowAllUppercase;
  private final boolean allowTitlecase;

  private volatile boolean initialized;
  private Map<String, Integer> mStartSpace;
  private Map<String, Integer> mStartNoSpace;
  private Map<String, AnalyzedToken> mFullSpace;
  private Map<String, AnalyzedToken> mFullNoSpace;

  private final static int MAX_TOKENS_IN_MULTIWORD = 20;

  private final static String DEFAULT_SEPARATOR = "\t";
  private String separator;
  private String defaultTag = null;

  private boolean addIgnoreSpelling = false;
  private boolean isRemovePreviousTags = false;

  public static String tagForNotAddingTags = "_NONE_";

  private static final Pattern GermanLineExpander = Pattern.compile("^.*/[ESN]+$");

  /**
   * @param filename file text with multiwords and tags
   */
  public MultiWordChunker(String filename) {
    this(filename, false, false, false);
  }

  /**
   * @param filename              file text with multiwords and tags
   * @param allowFirstCapitalized if set to {@code true}, first word of the
   *                              multiword can be capitalized
   * @param allowAllUppercase     if set to {@code true}, the all uppercase
   *                              version of the multiword is allowed
   * @param allowTitlecase        if set to {@code true}, titlecased variants
   *                              of multi-token words are accepted
   */
  public MultiWordChunker(String filename, boolean allowFirstCapitalized, boolean allowAllUppercase, boolean allowTitlecase) {
    this.filename = filename;
    this.allowFirstCapitalized = allowFirstCapitalized;
    this.allowAllUppercase = allowAllUppercase;
    this.allowTitlecase = allowTitlecase;
  }

  public MultiWordChunker(String filename, boolean allowFirstCapitalized, boolean allowAllUppercase, boolean allowTitlecase, String defaultTag) {
    this.filename = filename;
    this.allowFirstCapitalized = allowFirstCapitalized;
    this.allowAllUppercase = allowAllUppercase;
    this.allowTitlecase = allowTitlecase;
    this.defaultTag = defaultTag;
  }

  /*
   * Lazy init, thanks to Artur Trzewik
   */
  private void lazyInit() {
    if (initialized) {
      return;
    }

    synchronized (this) {
      if (initialized) return;

      Object2IntOpenHashMap<String> mStartSpace = new Object2IntOpenHashMap<>();
      Object2IntOpenHashMap<String> mStartNoSpace = new Object2IntOpenHashMap<>();
      Object2ObjectOpenHashMap<String, AnalyzedToken> mFullSpace = new Object2ObjectOpenHashMap<>();
      Object2ObjectOpenHashMap<String, AnalyzedToken> mFullNoSpace = new Object2ObjectOpenHashMap<>();

      fillMaps(mStartSpace, mStartNoSpace, mFullSpace, mFullNoSpace);

      mStartSpace.trim();
      mStartNoSpace.trim();
      mFullSpace.trim();
      mFullNoSpace.trim();

      this.mStartSpace = mStartSpace;
      this.mStartNoSpace = mStartNoSpace;
      this.mFullSpace = mFullSpace;
      this.mFullNoSpace = mFullNoSpace;
      initialized = true;
    }
  }

  private void fillMaps(Map<String, Integer> mStartSpace, Map<String, Integer> mStartNoSpace, Map<String,
    AnalyzedToken> mFullSpace, Map<String, AnalyzedToken> mFullNoSpace) {
    Map<String, String> interner = new HashMap<>();
    try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(filename)) {
      List<String> lines = loadWords(stream);
      for (String line : lines) {
        String[] stringAndTag = line.split(separator);
        if (stringAndTag.length != 2 && defaultTag == null) {
          throw new RuntimeException(
              "Invalid format in " + filename + ": '" + line + "', expected two tab-separated parts");
        }
        if (stringAndTag.length != 1 && defaultTag != null) {
          throw new RuntimeException(
            "Invalid format in " + filename + ": '" + line + "', expected one element with no separator");
        }
        List<String> casingVariants = new ArrayList<>();
        String originalString = interner.computeIfAbsent(stringAndTag[0], Function.identity());
        String tag = interner.computeIfAbsent((defaultTag != null ? defaultTag:stringAndTag[1]), Function.identity());
        boolean containsSpace = originalString.indexOf(' ') > 0;
        casingVariants.add(originalString);
        if (containsSpace) {
          casingVariants.addAll(getTokenLettercaseVariants(originalString, mFullSpace));
        } else {
          casingVariants.addAll(getTokenLettercaseVariants(originalString, mFullNoSpace));
        }
        for (String casingVariant : casingVariants) {
          if (!containsSpace) {
            String firstChar = casingVariant.substring(0, 1);
            if (mStartNoSpace.containsKey(firstChar)) {
              if (mStartNoSpace.get(firstChar) < casingVariant.length()) {
                mStartNoSpace.put(firstChar, casingVariant.length());
              }
            } else {
              mStartNoSpace.put(firstChar, casingVariant.length());
            }
            mFullNoSpace.put(casingVariant, new AnalyzedToken(casingVariant, tag, originalString));
          } else {
            String[] tokens = casingVariant.split(" ");
            String firstToken = tokens[0];
            if (mStartSpace.containsKey(firstToken)) {
              if (mStartSpace.get(firstToken) < tokens.length) {
                mStartSpace.put(firstToken, tokens.length);
              }
            } else {
              mStartSpace.put(firstToken, tokens.length);
            }
            mFullSpace.put(casingVariant, new AnalyzedToken(casingVariant, tag, originalString));
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<String> getTokenLettercaseVariants(String originalToken, Map<String, AnalyzedToken> tokenMap) {
    List<String> newTokens = new ArrayList<>();
    if (allowAllUppercase && !StringTools.isCamelCase(originalToken)) {
      // do not capitalize iPad, iPhone, etc. (single tokens)
      String tokenAllUppercase = originalToken.toUpperCase();
      if (!tokenMap.containsKey(tokenAllUppercase) && !originalToken.equals(tokenAllUppercase)) {
        newTokens.add(tokenAllUppercase);
      }
    }
    if (allowFirstCapitalized) {
      String tokenFirstCapitalized = StringTools.uppercaseFirstChar(originalToken);
      if (!tokenMap.containsKey(tokenFirstCapitalized) && !originalToken.equals(tokenFirstCapitalized)) {
        newTokens.add(tokenFirstCapitalized);
      }
      // Titlecasing is only relevant for multi-token entries, and only done for expressions that are entirely lowercase
      // It is also limited to when first-letter capitalisation is allowed.
      if (allowTitlecase && originalToken.split(" ").length > 1 && StringTools.allStartWithLowercase(originalToken)) {
        String tokenNaivelyTitlecased = WordUtils.capitalize(originalToken);
        if (!tokenNaivelyTitlecased.equals(tokenFirstCapitalized) && !originalToken.equals(tokenNaivelyTitlecased)) {
          newTokens.add(tokenNaivelyTitlecased);
        }
        String tokenSmartlyTitlecased = StringTools.titlecaseGlobal(originalToken);
        if (!tokenSmartlyTitlecased.equals(tokenFirstCapitalized) && !tokenSmartlyTitlecased.equals(tokenNaivelyTitlecased) &&
          !originalToken.equals(tokenSmartlyTitlecased)) {
          newTokens.add(tokenSmartlyTitlecased);
        }
      }
    }
    return newTokens;
  }

  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence input) throws IOException {
    return disambiguate(input, null);
  }

  /**
   * Implements multiword POS tags, e.g., &lt;ELLIPSIS&gt; for ellipsis (...)
   * start, and &lt;/ELLIPSIS&gt; for ellipsis end.
   *
   * @param input The tokens to be chunked.
   * @return AnalyzedSentence with additional markers.
   */
  @Override
  public final AnalyzedSentence disambiguate(AnalyzedSentence input, @Nullable JLanguageTool.CheckCancelledCallback checkCanceled) throws IOException {

    lazyInit();

    AnalyzedTokenReadings[] anTokens = input.getTokens();
    AnalyzedTokenReadings[] output = anTokens;

    for (int i = 0; i < anTokens.length; i++) {
      String tok = output[i].getToken();
      if (tok.length() < 1) {
        continue;
      }
      // If the next token is not whitespace, concatenate it
      int k = i + 1;
      while (k < anTokens.length && !anTokens[k].isWhitespace()) {
        tok = tok + output[k].getToken();
        k++;
      }
      if (checkCanceled != null && checkCanceled.checkCancelled()) {
        break;
      }
      if (mStartSpace.containsKey(tok)) {
        int finalLen = 0;
        StringBuilder keyBuilder = new StringBuilder();
        int len = mStartSpace.get(tok);
        int j = i;
        int lenCounter = 0;
        while (j < anTokens.length  && j - i < MAX_TOKENS_IN_MULTIWORD) {
          if (!anTokens[j].isWhitespace()) {
            keyBuilder.append(anTokens[j].getToken());
            String keyStr = keyBuilder.toString();
            AnalyzedToken at = mFullSpace.get(keyStr);
            if (at != null) {
              if (!at.getPOSTag().equals(tagForNotAddingTags)) {
                if (finalLen == 0) { // the key has only one token
                  output[i] = setAndAnnotate(output[i], new AnalyzedToken(anTokens[j].getToken(), at.getPOSTag(), at.getLemma()));
                } else {
                  output[i] = prepareNewReading(at, output[i].getToken(), output[i], false);
                  output[finalLen] = prepareNewReading(at, anTokens[finalLen].getToken(), output[finalLen], true);
                }
              }
              if (addIgnoreSpelling) {
                if (finalLen == 0) {
                  output[i].ignoreSpelling();
                } else {
                  for (int m = i; m <= finalLen; m++) {
                    output[m].ignoreSpelling();
                  }
                }
              }
            }
          } else {
            if (j > 1 && !anTokens[j - 1].isWhitespace()) { // avoid multiple whitespaces
              keyBuilder.append(' ');
              lenCounter++;
            }
            if (lenCounter == len) {
              break;
            }
          }
          j++;
          finalLen = j;
        }
      }
      if (mStartNoSpace.containsKey(tok.substring(0, 1))) {
        int j = i;
        StringBuilder keyBuilder = new StringBuilder();
        while (j < anTokens.length && !anTokens[j].isWhitespace() && j - i < MAX_TOKENS_IN_MULTIWORD) {
          keyBuilder.append(anTokens[j].getToken());
          String keyStr = keyBuilder.toString();
          AnalyzedToken at = mFullNoSpace.get(keyStr);
          if (at != null) {
            if (!at.getPOSTag().equals(tagForNotAddingTags)) {
              if (i == j) {
                String postag = at.getPOSTag();
                if (!isLowPriorityTag(postag) || !output[i].hasReading() || output[i].isPosTagUnknown()) {
                  output[i] = setAndAnnotate(output[i], new AnalyzedToken(anTokens[j].getToken(), postag, at.getLemma()));
                }
              } else {
                output[i] = prepareNewReading(at, anTokens[i].getToken(), output[i], false);
                output[j] = prepareNewReading(at, anTokens[j].getToken(), output[j], true);
              }
            }
            if (addIgnoreSpelling) {
              for (int m = i; m <= j; m++) {
                output[m].ignoreSpelling();
              }
            }
          }
          j++;
        }
      }
    }
    if (isRemovePreviousTags) {
      return new AnalyzedSentence(removePreviousTags(output));
    }
    return new AnalyzedSentence(output);
  }

  private AnalyzedTokenReadings prepareNewReading(AnalyzedToken at, String token, AnalyzedTokenReadings atrs,
      boolean isLast) {
    StringBuilder sb = new StringBuilder();
    sb.append('<');
    if (isLast) {
      sb.append('/');
    }
    sb.append(at.getPOSTag());
    sb.append('>');
    return setAndAnnotate(atrs, new AnalyzedToken(token, sb.toString(), at.getLemma()));
  }

  private AnalyzedTokenReadings setAndAnnotate(AnalyzedTokenReadings oldReading, AnalyzedToken newReading) {
    AnalyzedTokenReadings newAtr = oldReading;
    newAtr.addReading(newReading, "MULTIWORD_CHUNKER");
    return newAtr;
  }

  private List<String> loadWords(InputStream stream) {
    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      String line;
      separator = DEFAULT_SEPARATOR;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.startsWith("#separatorRegExp=")) {
          separator = line.replace("#separatorRegExp=", "");
        }
        if (line.isEmpty() || line.charAt(0) == '#') { // ignore comments
          continue;
        }
        line = StringUtils.substringBefore(line, "#").trim();
        if (GermanLineExpander.matcher(line).matches()) {
          //German special case
          String [] parts = line.split("/");
          lines.add(parts[0].trim());
          if (parts[1].contains("E")) {
            lines.add(parts[0].trim()+"e");
          }
          if (parts[1].contains("S")) {
            lines.add(parts[0].trim()+"s");
          }
          if (parts[1].contains("N")) {
            lines.add(parts[0].trim()+"n");
          }
        } else {
          lines.add(line);
        }

      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return lines;
  }

  /* set the ignorespelling attribute for the multi-token phrases*/
  public void setIgnoreSpelling(boolean ignoreSpelling) {
    addIgnoreSpelling = ignoreSpelling;
  }
  public void  setRemovePreviousTags (boolean removePreviousTags) {
    isRemovePreviousTags = removePreviousTags;
  }

  /* Put the results of the MultiWordChunker in a more appropriate and useful way
      <NP..></NP..> becomes NP.. NP..
      For ES, PT, CA <NCMS000></NCMS000> becomes NCMS000 AQ0MS0
      The individual original tags are removed */
  private AnalyzedTokenReadings[] removePreviousTags(AnalyzedTokenReadings[] aTokens) {
    int i=0;
    String POSTag = "";
    String lemma = "";
    String nextPOSTag = "";
    AnalyzedToken analyzedToken = null;
    while (i < aTokens.length) {
      if (!aTokens[i].isWhitespace()) {
        if (!nextPOSTag.isEmpty()) {
          AnalyzedToken newAnalyzedToken = new AnalyzedToken(aTokens[i].getToken(), nextPOSTag, lemma);
          if (aTokens[i].hasPosTagAndLemma("</" + POSTag + ">", lemma)) {
            nextPOSTag = "";
            lemma = "";
          }
          aTokens[i] = new AnalyzedTokenReadings(aTokens[i], Arrays.asList(newAnalyzedToken),
            "HybridDisamb");
        } else if ((analyzedToken = getMultiWordAnalyzedToken(aTokens, i)) != null) {
          POSTag = analyzedToken.getPOSTag().substring(1, analyzedToken.getPOSTag().length() - 1);
          lemma = analyzedToken.getLemma();
          if (aTokens[i].hasPosTagAndLemma("</" + POSTag + ">", lemma)) {
            // it is only one token
            aTokens[i].removeReading(aTokens[i].readingWithTagRegex("</" + POSTag + ">"), "HybridDisamb");
            aTokens[i].removeReading(aTokens[i].readingWithTagRegex("<" + POSTag + ">"), "HybridDisamb");
            aTokens[i].addReading(new AnalyzedToken(analyzedToken.getToken(), POSTag, lemma), "HybridDisamb");
            nextPOSTag = "";
            lemma = "";
          } else {
            AnalyzedToken newAnalyzedToken = new AnalyzedToken(analyzedToken.getToken(), POSTag, lemma);
            aTokens[i] = new AnalyzedTokenReadings(aTokens[i], Arrays.asList(newAnalyzedToken), "HybridDisamb");
            nextPOSTag = getNextPosTag(POSTag);
          }
        }
      }
      i++;
    }
    return aTokens;
  }

  private AnalyzedToken getMultiWordAnalyzedToken(AnalyzedTokenReadings[] aTokens, Integer i) {
    List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
    for (AnalyzedToken reading : aTokens[i]) {
      String POSTag = reading.getPOSTag();
      if (POSTag != null) {
        if (POSTag.startsWith("<") && POSTag.endsWith(">") && !POSTag.startsWith("</")) {
          l.add(reading);
        }
      }
    }
    // choose the longest one
    if (l.size() > 0) {
      AnalyzedToken selectedAT = null;
      int maxDistance = 0;
      for (AnalyzedToken at : l) {
        String tag = "</" + at.getPOSTag().substring(1);
        String cleanTag = at.getPOSTag().substring(1, at.getPOSTag().length() - 2);
        String lemma = at.getLemma();
        int distance = 1;
        while (i + distance < aTokens.length) {
          if (aTokens[i + distance].hasPosTagAndLemma(tag, lemma)) {
            if (distance > maxDistance) {
              maxDistance = distance;
              selectedAT = at;
            }
            if (distance == maxDistance && !isLowPriorityTag(cleanTag)) {
              maxDistance = distance;
              selectedAT = at;
            }
            break;
          }
          distance++;
        }
      }
      return selectedAT;
    }
    return null;
  }

  private String getNextPosTag(String postag) {
    if (postag.startsWith("NC")) {
      // for ES, PT, CA
      return "AQ0" + postag.substring(2, 4) + "0";
    } else if (postag.startsWith("N ")) {
      // French
      return "J " + postag.substring(2);
    }
    return postag;
  }

  private boolean isLowPriorityTag(String tag) {
    // CA, ES
    return tag.equals("NPCN000");
  }

}
