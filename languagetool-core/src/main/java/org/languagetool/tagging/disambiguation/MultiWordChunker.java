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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tools.StringTools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Multiword tagger-chunker.
 *
 * @author Marcin Miłkowski
 */
public class MultiWordChunker implements Disambiguator {

  private final String filename;

  private Map<String, Integer> mStartSpace;
  private Map<String, Integer> mStartNoSpace;
  private Map<String, String> mFull;
  
  private boolean bAllowFirstCapitalized=false;
  
  /**
   * @param filename
   *          file text with multiwords and tags
   */
  public MultiWordChunker(final String filename) {
    super();
    this.filename = filename;
  }
  
  /**
   * @param filename
   *          file text with multiwords and tags
   * @param bAllowFirstUpperCase
   *          if set to {@code true}, first word of the multiword can be capitalized
   */
  public MultiWordChunker(final String filename, boolean allowFirstCapitalized) {
    super();
    this.filename = filename;
    bAllowFirstCapitalized = allowFirstCapitalized;
  }
  
  /*
   * Lazy init, thanks to Artur Trzewik
   */
  private void lazyInit() {

    if (mStartSpace != null) {
      return;
    }

    Map<String, Integer> mStartSpace = new HashMap<>();
    Map<String, Integer> mStartNoSpace = new HashMap<>();
    Map<String, String> mFull = new HashMap<>();

    final List<String> posTokens = loadWords(JLanguageTool.getDataBroker().getFromResourceDirAsStream(filename));
    for (String posToken : posTokens) {
      final String[] tokenAndTag = posToken.split("\t");
      if (tokenAndTag.length != 2) {
        throw new RuntimeException("Invalid format in " + filename + ": '" + posToken + "', expected two tab-separated parts");
      }
      final boolean containsSpace = tokenAndTag[0].indexOf(' ') > 0;
      String firstToken;
      final String[] firstTokens;
      if (!containsSpace) {
        firstTokens = new String[tokenAndTag[0].length()];
        firstToken = tokenAndTag[0].substring(0, 1);
        for (int i = 1; i < tokenAndTag[0].length(); i++) {
          firstTokens[i] = tokenAndTag[0].substring(i - 1, i);
        }
        if (mStartNoSpace.containsKey(firstToken)) {
          if (mStartNoSpace.get(firstToken) < firstTokens.length) {
            mStartNoSpace.put(firstToken, firstTokens.length);
          }
        } else {
          mStartNoSpace.put(firstToken, firstTokens.length);
        }
      } else {
        firstTokens = tokenAndTag[0].split(" ");
        firstToken = firstTokens[0];

        if (mStartSpace.containsKey(firstToken)) {
          if (mStartSpace.get(firstToken) < firstTokens.length) {
            mStartSpace.put(firstToken, firstTokens.length);
          }
        } else {
          mStartSpace.put(firstToken, firstTokens.length);
        }
      }
      mFull.put(tokenAndTag[0], tokenAndTag[1]);
    }
    
    this.mStartSpace = mStartSpace;
    this.mStartNoSpace = mStartNoSpace;
    this.mFull = mFull;
  }

  /**
   * Implements multiword POS tags, e.g., &lt;ELLIPSIS&gt; for ellipsis (...)
   * start, and &lt;/ELLIPSIS&gt; for ellipsis end.
   *
   * @param input The tokens to be chunked.
   * @return AnalyzedSentence with additional markers.
   */
  @Override
  public final AnalyzedSentence disambiguate(final AnalyzedSentence input) {

    lazyInit();

    final AnalyzedTokenReadings[] anTokens = input.getTokens();
    final AnalyzedTokenReadings[] output = anTokens;

    for (int i = 0; i < anTokens.length; i++) {
      String tok = output[i].getToken();
      if (tok.length()<1) {
        continue;
      }
      // If the second token is not whitespace, concatenate it
      if (i + 1 < anTokens.length && !anTokens[i+1].isWhitespace()) {
        tok=tok.concat(output[i+1].getToken());
      }
      // If it is a capitalized word, the second time try with lowercase word.
      int myCount = 0;
      while (myCount < 2) {
        final StringBuilder tokens = new StringBuilder();
        int finalLen = 0;
        if (mStartSpace.containsKey(tok)) {
          final int len = mStartSpace.get(tok);
          int j = i;
          int lenCounter = 0;
          while (j < anTokens.length) {
            if (!anTokens[j].isWhitespace()) {
              if ((j == i) && (myCount == 1)) {
                tokens.append(anTokens[j].getToken().toLowerCase());
              } else {
                tokens.append(anTokens[j].getToken());
              }
              final String toks = tokens.toString();
              if (mFull.containsKey(toks)) {
                output[i] = prepareNewReading(toks, output[i].getToken(), output[i], false);
                output[finalLen] = prepareNewReading(toks,
                    anTokens[finalLen].getToken(), output[finalLen], true);
              }
            } else {
              tokens.append(' ');
              lenCounter++;
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
          while (j < anTokens.length && !anTokens[j].isWhitespace()) {
            if ((j == i) && (myCount == 1)) {
              tokens.append(anTokens[j].getToken().toLowerCase());
            } else {
              tokens.append(anTokens[j].getToken());
            }
            final String toks = tokens.toString();
            if (mFull.containsKey(toks)) {
              output[i] = prepareNewReading(toks, anTokens[i].getToken(),
                  output[i], false);
              output[j] = prepareNewReading(toks, anTokens[j].getToken(),
                  output[j], true);
            }
            j++;
          }
        }
        // If it is a capitalized word, try with lowercase word.
        myCount++;
        if (bAllowFirstCapitalized && StringTools.isCapitalizedWord(tok) 
            && myCount == 1) {
            tok = tok.toLowerCase();
        } else {
          myCount = 2;
        }
      }
    }
    return new AnalyzedSentence(output);
  }

  private AnalyzedTokenReadings prepareNewReading(final String tokens, final String tok, final AnalyzedTokenReadings token, final boolean isLast) {
    final StringBuilder sb = new StringBuilder();
    sb.append('<');
    if (isLast) {
      sb.append('/');
    }
    sb.append(mFull.get(tokens));
    sb.append('>');
    final AnalyzedToken tokenStart = new AnalyzedToken(tok, sb.toString(), tokens);
    return setAndAnnotate(token, tokenStart);
  }

  private AnalyzedTokenReadings setAndAnnotate(final AnalyzedTokenReadings oldReading, final AnalyzedToken newReading) {
    final String old = oldReading.toString();
    final String prevAnot = oldReading.getHistoricalAnnotations();
    final AnalyzedTokenReadings newAtr = new AnalyzedTokenReadings(oldReading.getReadings(),
            oldReading.getStartPos());
    newAtr.setWhitespaceBefore(oldReading.isWhitespaceBefore());
    newAtr.addReading(newReading);
    newAtr.setHistoricalAnnotations(
            annotateToken(prevAnot, old, newAtr.toString()));
    newAtr.setChunkTags(oldReading.getChunkTags());
    return newAtr;
  }
  
  private String annotateToken(final String prevAnot, final String oldReading, final String newReading) {
    return prevAnot + "\nMULTIWORD_CHUNKER: " + oldReading + " -> " + newReading;
  }

  private List<String> loadWords(final InputStream stream) {
    final List<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) {
          continue;
        }
        if (line.charAt(0) == '#') { // ignore comments
          continue;
        }
        lines.add(line);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return lines;
  }

}