/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.bigdata;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.en.GoogleStyleWordTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Prepare indexing the CommonCrawl-based data from http://data.statmt.org/ngrams/
 * to ngrams - result will still need to be aggregated and then indexed
 * with {@link AggregatedNgramToLucene}.
 * @since 3.2
 */
class CommonCrawlToNgram3 implements AutoCloseable {

  private static final int MAX_TOKEN_LENGTH = 20;
  private static final int MAX_SENTENCE_LENGTH = 50_000;
  private static final int CACHE_LIMIT = 1_000_000;  // max. number of trigrams in HashMap before we flush to Lucene

  private final File input;
  private final SentenceTokenizer sentenceTokenizer;
  private final Tokenizer wordTokenizer;
  private final Map<String, Long> unigramToCount = new HashMap<>();
  private final Map<String, Long> bigramToCount = new HashMap<>();
  private final Map<String, Long> trigramToCount = new HashMap<>();
  private final Map<Integer, FileWriter> ngramSizeToWriter = new HashMap<>();
  
  private long charCount = 0;
  private long lineCount = 0;

  CommonCrawlToNgram3(Language language, File input, File outputDir) throws IOException {
    this.input = input;
    this.sentenceTokenizer = language.getSentenceTokenizer();
    this.wordTokenizer = new GoogleStyleWordTokenizer();
    ngramSizeToWriter.put(1, new FileWriter(new File(outputDir, "unigrams.csv")));
    ngramSizeToWriter.put(2, new FileWriter(new File(outputDir, "bigrams.csv")));
    ngramSizeToWriter.put(3, new FileWriter(new File(outputDir, "trigrams.csv")));
  }

  @Override
  public void close() throws Exception {
    for (Map.Entry<Integer, FileWriter> entry : ngramSizeToWriter.entrySet()) {
      entry.getValue().close();
    }
  }

  private void indexInputFile() throws IOException, CompressorException {
    FileInputStream fin = new FileInputStream(input);
    BufferedInputStream in = new BufferedInputStream(fin);
    try (CompressorInputStream input = new CompressorStreamFactory().createCompressorInputStream(in)) {
      final byte[] buffer = new byte[8192];
      int n;
      while ((n = input.read(buffer)) != -1) {
        String buf = new String(buffer, 0, n);  // TODO: not always correct, we need to wait for line end first?
        String[] lines = buf.split("\n");
        indexLine(lines);
      }
    }
    writeToDisk(1, unigramToCount);
    writeToDisk(2, bigramToCount);
    writeToDisk(3, trigramToCount);
  }

  private void indexLine(String[] lines) throws IOException {
    for (String line : lines) {
      if (line.length() > MAX_SENTENCE_LENGTH) {
        System.out.println("Ignoring long line: " + line.length() + " bytes");
        continue;
      }
      if (lineCount++ % 50_000 == 0) {
        float mb = (float) charCount / 1000 / 1000;
        System.out.printf(Locale.ENGLISH, "Indexing line %d (%.2fMB)\n", lineCount, mb);
      }
      charCount += line.length();
      List<String> sentences = sentenceTokenizer.tokenize(line);
      for (String sentence : sentences) {
        indexSentence(sentence);
      }
    }
  }

  private void indexSentence(String sentence) throws IOException {
    List<String> tokens = wordTokenizer.tokenize(sentence);
    tokens.add(0, LanguageModel.GOOGLE_SENTENCE_START);
    tokens.add(LanguageModel.GOOGLE_SENTENCE_END);
    String prevPrev = null;
    String prev = null;
    for (String token : tokens) {
      if (token.trim().isEmpty()) {
        continue;
      }
      if (token.length() <= MAX_TOKEN_LENGTH) {
        unigramToCount.compute(token, (k, v) -> v == null ? 1 : v + 1);
      }
      if (prev != null) {
        if (token.length() <= MAX_TOKEN_LENGTH && prev.length() <= MAX_TOKEN_LENGTH) {
          String ngram = prev + " " + token;
          bigramToCount.compute(ngram, (k, v) -> v == null ? 1 : v + 1);
        }
      }
      if (prevPrev != null && prev != null) {
        if (token.length() <= MAX_TOKEN_LENGTH && prev.length() <= MAX_TOKEN_LENGTH && prevPrev.length() <= MAX_TOKEN_LENGTH) {
          String ngram = prevPrev + " " + prev + " " + token;
          trigramToCount.compute(ngram, (k, v) -> v == null ? 1 : v + 1);
        }
        if (unigramToCount.size() > CACHE_LIMIT) {
          writeToDisk(1, unigramToCount);
        }
        if (bigramToCount.size() > CACHE_LIMIT) {
          writeToDisk(2, bigramToCount);
        }
        if (trigramToCount.size() > CACHE_LIMIT) {
          writeToDisk(3, trigramToCount);
        }
      }
      prevPrev = prev;
      prev = token;
    }
  }

  private void writeToDisk(int ngramSize, Map<String, Long> ngramToCount) throws IOException {
    System.out.println("Writing " + ngramToCount.size() + " cached ngrams to disk (ngramSize=" + ngramSize + ")...");
    FileWriter writer = ngramSizeToWriter.get(ngramSize);
    for (Map.Entry<String, Long> entry : ngramToCount.entrySet()) {
      writer.write(entry.getKey() + "\t" + entry.getValue() + "\n");
    }
    writer.flush();
    ngramToCount.clear();
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      System.out.println("Usage: " + CommonCrawlToNgram3.class + " <langCode> <input.xz/bz2> <outputDir>");
      System.exit(1);
    }
    Language language = Languages.getLanguageForShortCode(args[0]);
    File input = new File(args[1]);
    File outputDir = new File(args[2]);
    try (CommonCrawlToNgram3 prg = new CommonCrawlToNgram3(language, input, outputDir)) {
      prg.indexInputFile();
    }
  }
  
}
