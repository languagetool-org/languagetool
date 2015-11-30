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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.dev.eval.SimpleCorpusEvaluator;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.en.GoogleStyleWordTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.tukaani.xz.XZInputStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Experimental version with use MapDB. You probably want to use
 * CommonCrawlToNgram instead.
 * Indexing the CommonCrawl-based data from http://data.statmt.org/ngrams/
 * to ngrams.
 * 
 * @since 3.2
 */
class CommonCrawlToNgram2 implements AutoCloseable {

  private static final double THRESHOLD = 0.0000001;
  private static final int MAX_TOKEN_LENGTH = 20;
  
  private final File input;
  private final File indexTopDir;
  private final File evalFile;
  private final SentenceTokenizer sentenceTokenizer;
  private final Tokenizer wordTokenizer;

  // Indexing line 4460001 (20.02MB) - took 117ms, avg=254.57
  private final DB db = DBMaker.fileDB(new File("/tmp/mapdb")).fileMmapEnable().asyncWriteEnable().cacheSize(100_000).cacheLRUEnable().make();

  // Indexing line 4460001 (20.02MB) - took 154ms, avg=313.46
  //DB db = DBMaker.fileDB(new File("/tmp/mapdb")).transactionDisable().fileMmapEnable().asyncWriteEnable().cacheSize(100_000).cacheLRUEnable().make();

  // Indexing line 4460001 (20.02MB) - took 707ms, avg=326.21:
  //DB db = DBMaker.fileDB(new File("/tmp/mapdb")).transactionDisable().fileMmapEnable().asyncWriteEnable().cacheSize(100_000).cacheLRUEnable().compressionEnable().make();

  // Indexing line 4460001 (20.02MB) - took 229ms, avg=384.39
  //DB db = DBMaker.fileDB(new File("/tmp/mapdb")).transactionDisable().fileMmapEnable().asyncWriteEnable().cacheSize(100_000).make();

  // much slower (800+ on average):
  //DB db = DBMaker.fileDB(new File("/tmp/mapdb")).transactionDisable().asyncWriteEnable().cacheSize(100_000).cacheLRUEnable().make();

  private final HTreeMap<String , Long> unigramToCount = db.hashMap("map1");
  private final HTreeMap<String , Long> bigramToCount = db.hashMap("map2");
  private final HTreeMap<String , Long> trigramToCount = db.hashMap("map3");

  private final FieldType fieldType = new FieldType();

  private final Map<Integer, LuceneLiveIndex> indexes = new HashMap<>();
  private long lastTime = System.currentTimeMillis();
  private long totalTime = 0;
  private long totalTimeCount = 0;
  private long trigramToCounter = 0;
  
  private int cacheLimit = 1_000_000;  // max. number of trigrams in HashMap before we flush to Lucene and run evaluation
  private long charCount = 0;
  private long lineCount = 0;

  CommonCrawlToNgram2(Language language, File input, File indexTopDir, File evalFile) throws IOException {
    this.input = input;
    this.indexTopDir = indexTopDir;
    this.evalFile = evalFile;
    this.sentenceTokenizer = language.getSentenceTokenizer();
    this.wordTokenizer = new GoogleStyleWordTokenizer();
    indexes.put(1, new LuceneLiveIndex(new File(indexTopDir, "1grams")));
    indexes.put(2, new LuceneLiveIndex(new File(indexTopDir, "2grams")));
    indexes.put(3, new LuceneLiveIndex(new File(indexTopDir, "3grams")));
    fieldType.setStored(true);
    fieldType.setOmitNorms(true);
    fieldType.setNumericType(FieldType.NumericType.LONG);
    fieldType.setDocValuesType(DocValuesType.NUMERIC);
  }
  
  @Override
  public void close() throws IOException {
    for (LuceneLiveIndex index : indexes.values()) {
      index.close();
    }
  }

  void setCacheLimit(int cacheLimit) {
    this.cacheLimit = cacheLimit;
  }
  
  void indexInputFile() throws IOException {
    writeAndEvaluate();  // run now so we have a baseline
    FileInputStream fin = new FileInputStream(input);
    BufferedInputStream in = new BufferedInputStream(fin);
    try (XZInputStream xzIn = new XZInputStream(in)) {
      final byte[] buffer = new byte[8192];
      int n;
      while ((n = xzIn.read(buffer)) != -1) {
        String buf = new String(buffer, 0, n);  // TODO: not always correct, we need to wait for line end first?
        String[] lines = buf.split("\n");
        indexLines(lines);
      }
    }
    writeAndEvaluate();
  }

  private void indexLines(String[] lines) throws IOException {
    for (String line : lines) {
      if (lineCount++ % 10_000 == 0) {
        float mb = (float) charCount / 1000 / 1000;
        totalTimeCount++;
        long thisTime = System.currentTimeMillis()-lastTime;
        totalTime += thisTime;
        float avgTime = (float)totalTime/totalTimeCount;
        System.out.printf(Locale.ENGLISH, "Indexing line %d (%.2fMB) - took %dms, avg=%.2f\n", lineCount, mb, thisTime, avgTime);
        lastTime = System.currentTimeMillis();
      }
      if (++lineCount % 100_000 == 0) {
        System.out.println("commit");
        db.commit();
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
        increaseValueByOne(unigramToCount, token);
      }
      if (prev != null) {
        if (token.length() <= MAX_TOKEN_LENGTH && prev.length() <= MAX_TOKEN_LENGTH) {
          String ngram = prev + " " + token;
          increaseValueByOne(bigramToCount, ngram);
        }
      }
      if (prevPrev != null && prev != null) {
        if (token.length() <= MAX_TOKEN_LENGTH && prev.length() <= MAX_TOKEN_LENGTH && prevPrev.length() <= MAX_TOKEN_LENGTH) {
          String ngram = prevPrev + " " + prev + " " + token;
          increaseValueByOne(trigramToCount, ngram);
          trigramToCounter++;
        }
        if (++trigramToCounter % cacheLimit == 0) {
          writeAndEvaluate();
          trigramToCounter = 0;
        }
      }
      prevPrev = prev;
      prev = token;
    }
  }

  private void increaseValueByOne(Map<String, Long> map, String token) {
    Long val = map.get(token);
    if (val != null) {
      map.put(token, val + 1);
    } else {
      map.put(token, 1L);
    }
  }

  private void writeAndEvaluate() throws IOException {
    writeToLucene(1, unigramToCount);
    writeToLucene(2, bigramToCount);
    writeToLucene(3, trigramToCount);
    if (evalFile != null) {
      System.out.println("Running evaluation...");
      long startTime = System.currentTimeMillis();
      SimpleCorpusEvaluator evaluator = new SimpleCorpusEvaluator(indexTopDir);
      evaluator.run(evalFile, THRESHOLD);
      System.out.println("Eval time: " + (System.currentTimeMillis()-startTime) + "ms");
    } else {
      System.out.println("Skipping evaluation, no evaluation file specified");
    }
  }
  
  private void writeToLucene(int ngramSize, Map<String, Long> ngramToCount) throws IOException {
    System.out.println("Indexing " + ngramToCount.size() + " items...");
    LuceneLiveIndex index = indexes.get(ngramSize);
    index.indexWriter.deleteAll();
    index.indexWriter.commit();
    long startTime = System.currentTimeMillis();
    for (Map.Entry<String, Long> entry : ngramToCount.entrySet()) {
      Document doc = getDoc(entry.getKey(), entry.getValue());
      index.indexWriter.addDocument(doc);
    }
    if (ngramSize == 1) {
      // TODO: runtime code will crash if there are more than 1000 of these docs, so update instead of delete
      long total = ngramToCount.values().stream().mapToLong(Number::longValue).sum();
      System.out.println("Adding totalTokenCount doc: " + total);
      addTotalTokenCountDoc(total, index.indexWriter);
    }
    index.indexWriter.commit();
    index.reader = DirectoryReader.open(index.indexWriter, true);
    index.searcher = new IndexSearcher(index.reader);
    System.out.println("Commit done, indexing " + ngramToCount.size() + " items took " + (System.currentTimeMillis()-startTime)/1000 + "s");
  }

  @NotNull
  private Document getDoc(String ngram, long count) {
    Document doc = new Document();
    doc.add(new Field("ngram", ngram, StringField.TYPE_NOT_STORED));
    doc.add(getCountField(count));
    return doc;
  }

  @NotNull
  private LongField getCountField(long count) {
    return new LongField("count", count, fieldType);
  }

  private void addTotalTokenCountDoc(long totalTokenCount, IndexWriter writer) throws IOException {
    FieldType fieldType = new FieldType();
    fieldType.setIndexOptions(IndexOptions.DOCS);
    fieldType.setStored(true);
    fieldType.setOmitNorms(true);
    Field countField = new Field("totalTokenCount", String.valueOf(totalTokenCount), fieldType);
    Document doc = new Document();
    doc.add(countField);
    writer.addDocument(doc);
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 4) {
      System.out.println("Usage: " + CommonCrawlToNgram2.class + " <langCode> <input.xz> <ngramIndexDir> <simpleEvalFile>");
      System.out.println(" <simpleEvalFile> a plain text file with simple error markup");
      System.exit(1);
    }
    Language language = Languages.getLanguageForShortName(args[0]);
    File input = new File(args[1]);
    File outputDir = new File(args[2]);
    File evalFile = new File(args[3]);
    try (CommonCrawlToNgram2 prg = new CommonCrawlToNgram2(language, input, outputDir, evalFile)) {
      prg.indexInputFile();
    }
  }
  
  class LuceneLiveIndex {

    private final Directory directory;
    private final IndexWriter indexWriter;

    private DirectoryReader reader;
    private IndexSearcher searcher;

    LuceneLiveIndex(File dir) throws IOException {
      Analyzer analyzer = new StandardAnalyzer();
      IndexWriterConfig config = new IndexWriterConfig(analyzer);
      directory = FSDirectory.open(dir.toPath());
      indexWriter = new IndexWriter(directory, config);
      reader = DirectoryReader.open(indexWriter, false);
      searcher = new IndexSearcher(reader);
    }
    
    void close() throws IOException {
      reader.close();
      indexWriter.close();
      directory.close();
    }

  }
}
