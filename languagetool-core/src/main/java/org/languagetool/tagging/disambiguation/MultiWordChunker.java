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

import gnu.trove.THashMap;

import org.apache.commons.lang3.StringUtils;
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

/**
 * Multiword tagger-chunker.
 *
 * @author Marcin Miłkowski
 */
public class MultiWordChunker extends AbstractDisambiguator {

  private final String filename;
  private final boolean allowFirstCapitalized;
  private final boolean allowAllUppercase;

  private volatile boolean initialized;
  private Map<String, Integer> mStartSpace;
  private Map<String, Integer> mStartNoSpace;
  private Map<String, AnalyzedToken> mFull;
  
  private final static int MAX_TOKENS_IN_MULTIWORD = 20;
  
  private final static String DEFAULT_SEPARATOR = "\t";
  private String separator;

  /**
   * @param filename file text with multiwords and tags
   */
  public MultiWordChunker(String filename) {
    this(filename, false, false);
  }

  /**
   * @param filename              file text with multiwords and tags
   * @param allowFirstCapitalized if set to {@code true}, first word of the
   *                              multiword can be capitalized
   * @param allowAllUppercase     if set to {@code true}, the all uppercase
   *                              version of the multiword is allowed
   */
  public MultiWordChunker(String filename, boolean allowFirstCapitalized, boolean allowAllUppercase) {
    this.filename = filename;
    this.allowFirstCapitalized = allowFirstCapitalized;
    this.allowAllUppercase = allowAllUppercase;
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

      THashMap<String, Integer> mStartSpace = new THashMap<>();
      THashMap<String, Integer> mStartNoSpace = new THashMap<>();
      THashMap<String, AnalyzedToken> mFull = new THashMap<>();

      fillMaps(mStartSpace, mStartNoSpace, mFull);

      mStartSpace.trimToSize();
      mStartNoSpace.trimToSize();
      mFull.trimToSize();

      this.mStartSpace = mStartSpace;
      this.mStartNoSpace = mStartNoSpace;
      this.mFull = mFull;
      initialized = true;
    }
  }

  private void fillMaps(Map<String, Integer> mStartSpace, Map<String, Integer> mStartNoSpace, Map<String, AnalyzedToken> mFull) {
    Map<String, String> interner = new HashMap<>();
    try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(filename)) {
      List<String> posTokens = loadWords(stream);
      for (String posToken : posTokens) {
        String[] tokenAndTag = posToken.split(separator);
        if (tokenAndTag.length != 2) {
          throw new RuntimeException(
              "Invalid format in " + filename + ": '" + posToken + "', expected two tab-separated parts");
        }
        List<String> tokens = new ArrayList<>();
        String originalToken = interner.computeIfAbsent(tokenAndTag[0], Function.identity());
        String tag = interner.computeIfAbsent(tokenAndTag[1], Function.identity());
        tokens.add(originalToken);
        if (allowFirstCapitalized) {
          String tokenFirstCapitalized = StringTools.uppercaseFirstChar(originalToken);
          if (!mFull.containsKey(tokenFirstCapitalized) && !originalToken.equals(tokenFirstCapitalized)) {
            tokens.add(tokenFirstCapitalized);
          }
        }
        if (allowAllUppercase) {
          String tokenAllUppercase = originalToken.toUpperCase();
          if (!mFull.containsKey(tokenAllUppercase) && !originalToken.equals(tokenAllUppercase)) {
            tokens.add(tokenAllUppercase);
          }
        }
        for (String token : tokens) {
          boolean containsSpace = token.indexOf(' ') > 0;
          String firstToken;
          String[] firstTokens;
          if (!containsSpace) {
            firstTokens = new String[tokenAndTag[0].length()];
            firstToken = token.substring(0, 1);
            for (int i = 1; i < token.length(); i++) {
              firstTokens[i] = token.substring(i - 1, i);
            }
            if (mStartNoSpace.containsKey(firstToken)) {
              if (mStartNoSpace.get(firstToken) < firstTokens.length) {
                mStartNoSpace.put(firstToken, firstTokens.length);
              }
            } else {
              mStartNoSpace.put(firstToken, firstTokens.length);
            }
          } else {
            firstTokens = token.split(" ");
            firstToken = firstTokens[0];

            if (mStartSpace.containsKey(firstToken)) {
              if (mStartSpace.get(firstToken) < firstTokens.length) {
                mStartSpace.put(firstToken, firstTokens.length);
              }
            } else {
              mStartSpace.put(firstToken, firstTokens.length);
            }
          }
          mFull.put(token, new AnalyzedToken(token, tag, originalToken));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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
      // If the second token is not whitespace, concatenate it
      if (i + 1 < anTokens.length && !anTokens[i + 1].isWhitespace()) {
        tok = tok + output[i + 1].getToken();
      }

      if (checkCanceled != null && checkCanceled.checkCancelled()) {
        break;
      }

      StringBuilder tokens = new StringBuilder();
      int finalLen = 0;
      if (mStartSpace.containsKey(tok)) {
        int len = mStartSpace.get(tok);
        int j = i;
        int lenCounter = 0;
        while (j < anTokens.length  && j - i < MAX_TOKENS_IN_MULTIWORD) {
          if (!anTokens[j].isWhitespace()) {
            tokens.append(anTokens[j].getToken());
            String toks = tokens.toString();
            if (mFull.containsKey(toks)) {
              output[i] = prepareNewReading(toks, output[i].getToken(), output[i], false);
              output[finalLen] = prepareNewReading(toks, anTokens[finalLen].getToken(), output[finalLen], true);
            }
          } else {
            if (j > 1 && !anTokens[j - 1].isWhitespace()) { // avoid multiple whitespaces
              tokens.append(' ');
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
        while (j < anTokens.length && !anTokens[j].isWhitespace() && j - i < MAX_TOKENS_IN_MULTIWORD) {
          tokens.append(anTokens[j].getToken());
          String toks = tokens.toString();
          if (mFull.containsKey(toks)) {
            output[i] = prepareNewReading(toks, anTokens[i].getToken(), output[i], false);
            output[j] = prepareNewReading(toks, anTokens[j].getToken(), output[j], true);
          }
          j++;
        }
      }
    }
    return new AnalyzedSentence(output);
  }

  private AnalyzedTokenReadings prepareNewReading(String tokens, String tok, AnalyzedTokenReadings token,
      boolean isLast) {
    StringBuilder sb = new StringBuilder();
    sb.append('<');
    if (isLast) {
      sb.append('/');
    }
    sb.append(mFull.get(tokens).getPOSTag());
    sb.append('>');
    AnalyzedToken tokenStart = new AnalyzedToken(tok, sb.toString(), mFull.get(tokens).getLemma());
    return setAndAnnotate(token, tokenStart);
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
        lines.add(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return lines;
  }

}
