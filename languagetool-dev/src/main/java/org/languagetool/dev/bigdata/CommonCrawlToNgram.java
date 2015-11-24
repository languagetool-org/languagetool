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
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.dev.eval.SimpleCorpusEvaluator;
import org.languagetool.tokenizers.Tokenizer;
import org.tukaani.xz.XZInputStream;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Indexing the CommonCrawl-based data from http://data.statmt.org/ngrams/
 * to ngrams.
 * 
 * @since 3.2
 */
class CommonCrawlToNgram implements AutoCloseable {
  
  private final Language language;
  private final File input;
  private final File indexTopDir;
  private final File evalFile;
  private final Map<String, Long> unigramToCount = new HashMap<>();
  private final Map<String, Long> bigramToCount = new HashMap<>();
  private final Map<String, Long> trigramToCount = new HashMap<>();
  private final Map<Integer, LuceneLiveIndex> indexes = new HashMap<>();
  
  private int limit = 1000;

  CommonCrawlToNgram(Language language, File input, File indexTopDir, File evalFile) throws IOException {
    this.language = language;
    this.input = input;
    this.indexTopDir = indexTopDir;
    this.evalFile = evalFile;
    indexes.put(1, new LuceneLiveIndex(new File(indexTopDir, "1grams")));
    indexes.put(2, new LuceneLiveIndex(new File(indexTopDir, "2grams")));
    indexes.put(3, new LuceneLiveIndex(new File(indexTopDir, "3grams")));
  }
  
  @Override
  public void close() throws IOException {
    for (LuceneLiveIndex index : indexes.values()) {
      index.close();
    }
  }

  void setLimit(int limit) {
    this.limit = limit;
  }
  
  void indexInputFile() throws IOException {
    FileInputStream fin = new FileInputStream(input);
    BufferedInputStream in = new BufferedInputStream(fin);
    Tokenizer wordTokenizer = language.getWordTokenizer();  // TODO: use a more Google-like tokenizer
    try (XZInputStream xzIn = new XZInputStream(in)) {
      final byte[] buffer = new byte[8192];
      int n;
      while ((n = xzIn.read(buffer)) != -1) {
        String buf = new String(buffer, 0, n);  // TODO: not always correct, we need to wait for line end first?
        String[] lines = buf.split("\n");
        indexLine(wordTokenizer, lines);
      }
    }
  }

  private void indexLine(Tokenizer wordTokenizer, String[] lines) throws IOException {
    for (String line : lines) {
      List<String> tokens = wordTokenizer.tokenize(line);
      //System.out.println("L: " + tokens);
      String prevPrev = null;
      String prev = null;
      // TODO: add start and end tokens
      for (String token : tokens) {
        if (token.trim().isEmpty()) {
          continue;
        }
        unigramToCount.compute(token, (k, v) ->  v == null ? 1 : v + 1);
        if (prev != null) {
          String ngram = prev + " " + token;
          bigramToCount.compute(ngram, (k, v) ->  v == null ? 1 : v + 1);
        }
        if (prevPrev != null && prev != null) {
          String ngram = prevPrev + " " + prev + " " + token;
          trigramToCount.compute(ngram, (k, v) ->  v == null ? 1 : v + 1);
          if (trigramToCount.size() > limit) {
            writeAndEvaluate();
          }
        }
        prevPrev = prev;
        prev = token;
      }
    }
    writeAndEvaluate();
  }

  private void writeAndEvaluate() throws IOException {
    writeToLucene(1, unigramToCount);
    writeToLucene(2, bigramToCount);
    writeToLucene(3, trigramToCount);
    if (evalFile != null) {
      SimpleCorpusEvaluator evaluator = new SimpleCorpusEvaluator(indexTopDir);
      evaluator.run(evalFile);
    } else {
      System.out.println("Skipping evaluation, no evaluation file specified");
    }
  }

  private void writeToLucene(int ngramSize, Map<String, Long> ngramToCount) throws IOException {
    //System.out.println("WRITE: ");
    LuceneLiveIndex index = indexes.get(ngramSize);
    for (Map.Entry<String, Long> entry : ngramToCount.entrySet()) {
      Term ngram = new Term("ngram", entry.getKey());
      index.reader = DirectoryReader.open(index.indexWriter, true);
      index.searcher = new IndexSearcher(index.reader);
      // not sure why this doesn't work, should be faster:
      /*DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
      if (newReader != null) {
        reader = newReader;
      }*/
      TopDocs topDocs = index.searcher.search(new TermQuery(ngram), 2);
      //System.out.println(ngram + " ==> " + topDocs.totalHits);
      if (topDocs.totalHits == 0) {
        Document doc = getDoc(entry.getKey(), entry.getValue());
        index.indexWriter.addDocument(doc);
      } else if (topDocs.totalHits == 1) {
        int docNumber = topDocs.scoreDocs[0].doc;
        Document document = index.reader.document(docNumber);
        long oldCount = Long.parseLong(document.getField("count").stringValue());
        //System.out.println(ngram + " -> " + oldCount + "+" + entry.getValue());
        index.indexWriter.deleteDocuments(ngram);
        index.indexWriter.addDocument(getDoc(entry.getKey(), oldCount + entry.getValue()));
        // would probably be faster, but we currently rely on the count being a common field:
        //indexWriter.updateNumericDocValue(ngram, "count", oldCount + entry.getValue());
      } else if (topDocs.totalHits > 1) {
        throw new RuntimeException("Got more than one hit for: " + ngram);
      }
      //System.out.println("   " + entry.getKey() + " -> " + entry.getValue());
    }
    // TODO: add/update 'totalTokenCount'
    index.indexWriter.commit();
    ngramToCount.clear();
  }

  @NotNull
  private Document getDoc(String ngram, long count) {
    Document doc = new Document();
    doc.add(new Field("ngram", ngram, StringField.TYPE_STORED));  // TODO: store only for debugging
    doc.add(getCountField(count));
    return doc;
  }

  @NotNull
  private LongField getCountField(long count) {
    FieldType fieldType = new FieldType();
    fieldType.setStored(true);
    fieldType.setNumericType(FieldType.NumericType.LONG);
    fieldType.setDocValuesType(DocValuesType.NUMERIC);
    return new LongField("count", count, fieldType);
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 4) {
      System.out.println("Usage: " + CommonCrawlToNgram.class + " <langCode> <input.xz> <ngramIndexDir> <simpleEvalFile>");
      System.out.println(" <simpleEvalFile> a plain text file with simple error markup");
      System.exit(1);
    }
    Language language = Languages.getLanguageForShortName(args[0]);
    File input = new File(args[1]);
    File outputDir = new File(args[2]);
    File evalFile = new File(args[3]);
    try (CommonCrawlToNgram prg = new CommonCrawlToNgram(language, input, outputDir, evalFile)) {
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
      reader = DirectoryReader.open(indexWriter, true);  // TODO: see if false is faster
      searcher = new IndexSearcher(reader);
    }
    
    void close() throws IOException {
      indexWriter.close();
      directory.close();
    }

  }
}
