/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.dumpcheck;

import org.languagetool.Language;
import org.tukaani.xz.XZInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Provides access to the sentences of a Wikipedia XML dump. Note that
 * conversion exceptions are logged to STDERR and are otherwise ignored.
 * 
 * Get data from http://data.statmt.org/ngrams/deduped/
 * @since 4.1
 */
class CommonCrawlSentenceSource extends SentenceSource {

  private static final int MIN_LENGTH = 15;
  private static final int MAX_LENGTH = 250;
  
  private final List<CommonCrawlSentence> sentences;
  private final XZInputStream xzIn;

  private int tooShort = 0;
  private int tooLong = 0;
  private int empty = 0;
  private int wrongStartChar = 0;
  private int wrongEndChar = 0;
  private int count = 0;
  private int lineCount = 0;

  CommonCrawlSentenceSource(InputStream input, Language language, Pattern filter) throws IOException {
    super(language, filter);
    sentences = new ArrayList<>();
    xzIn = new XZInputStream(input);
  }

  @Override
  public boolean hasNext() {
    fillSentences();
    return sentences.size() > 0;
  }

  @Override
  public Sentence next() {
    fillSentences();
    if (sentences.isEmpty()) {
      throw new NoSuchElementException();
    }
    CommonCrawlSentence ccSentence = sentences.remove(0);
    return new Sentence(ccSentence.sentence, getSource(), null, null, ccSentence.articleCount);
  }

  @Override
  public String getSource() {
    return "commoncrawl";
  }

  private void fillSentences() {
    byte[] buffer = new byte[8192];
    int n;
    try {
      while (sentences.isEmpty() && (n = xzIn.read(buffer)) != -1) {
        String buf = new String(buffer, 0, n);  // TODO: not always correct, we need to wait for line end first?
        String[] lines = buf.split("\n");
        for (String line : lines) {
          lineCount++;
          line = line.trim();
          if (line.isEmpty()) {
            empty++;
            continue;
          }
          boolean startLower = Character.isLowerCase(line.charAt(0));
          if (startLower) {
            //System.out.println("IGNORING, lc start: " + line);
            wrongStartChar++;
          } else if (line.length() < MIN_LENGTH) {
            //System.out.println("IGNORING, too short: " + line);
            tooShort++;
          } else if (line.length() > MAX_LENGTH) {
            //System.out.println("IGNORING, too long (" + line.length() + "): " + line);
            tooLong++;
          } else if (line.endsWith(".") || line.endsWith("!") || line.endsWith("?") || line.endsWith(":")) {
            //System.out.println(line);
            sentences.add(new CommonCrawlSentence(line, count++));
          } else {
            wrongEndChar++;
            // well, this way we also miss headlines and the sentences that are split
            // over more than one line, but I see now simple way to merge them...
            //System.out.println("IGNORING, wrong end char: " + line);
          }
        }
      }
    } catch (IOException e) {
      printStats();
      throw new RuntimeException(e);
    }
  }

  private void printStats() {
    System.out.println("lines            : " + lineCount);
    System.out.println("indexed sentences: " + count);
    System.out.println("tooShort         : " + tooShort);
    System.out.println("tooLong          : " + tooLong);
    System.out.println("empty            : " + empty);
    System.out.println("wrongStartChar   : " + wrongStartChar);
    System.out.println("wrongEndChar     : " + wrongEndChar);
  }

  private static class CommonCrawlSentence {
    final String sentence;
    final int articleCount;
    CommonCrawlSentence(String sentence, int articleCount) {
      this.sentence = sentence;
      this.articleCount = articleCount;
    }
  }
}
