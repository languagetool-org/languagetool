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
  private final Directory directory;
  private final IndexWriter indexWriter;
  
  private DirectoryReader reader;
  private IndexSearcher searcher;
  private int limit = 1000;

  CommonCrawlToNgram(Language language, File input, File outputDir) throws IOException {
    this.language = language;
    this.input = input;
    Analyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    directory = FSDirectory.open(outputDir.toPath());
    indexWriter = new IndexWriter(directory, config);
    reader = DirectoryReader.open(indexWriter, true);  // TODO: see if false is faster
    searcher = new IndexSearcher(reader);
  }
  
  @Override
  public void close() throws IOException {
    indexWriter.close();
    reader.close();
    directory.close();
  }

  void setLimit(int limit) {
    this.limit = limit;
  }
  
  void indexInputFile() throws IOException {
    FileInputStream fin = new FileInputStream(input);
    BufferedInputStream in = new BufferedInputStream(fin);
    Tokenizer wordTokenizer = language.getWordTokenizer();  // TODO: use a more Google-like tokenizer
    Map<String, Long> ngramToCount = new HashMap<>();
    try (XZInputStream xzIn = new XZInputStream(in)) {
      final byte[] buffer = new byte[8192];
      int n;
      while ((n = xzIn.read(buffer)) != -1) {
        String buf = new String(buffer, 0, n);  // TODO: not always correct, we need to wait for line end first?
        String[] lines = buf.split("\n");
        indexLine(wordTokenizer, lines, ngramToCount);
      }
    }
  }

  private void indexLine(Tokenizer wordTokenizer, String[] lines, Map<String, Long> ngramToCount) throws IOException {
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
        if (prevPrev != null && prev != null) {
          String ngram = prevPrev + " " + prev + " " + token;
          Long count = ngramToCount.get(ngram);
          if (count == null) {
            ngramToCount.put(ngram, 1L);
          } else {
            ngramToCount.put(ngram, count + 1);
          }
          if (ngramToCount.size() > limit) {
            writeToLucene(ngramToCount);
            ngramToCount.clear();
          }
        }
        prevPrev = prev;
        prev = token;
      }
    }
    writeToLucene(ngramToCount);
  }

  private void writeToLucene(Map<String, Long> ngramToCount) throws IOException {
    //System.out.println("WRITE: ");
    for (Map.Entry<String, Long> entry : ngramToCount.entrySet()) {
      Term ngram = new Term("ngram", entry.getKey());
      reader = DirectoryReader.open(indexWriter, true);
      searcher = new IndexSearcher(reader);
      // not sure why this doesn't work, should be faster:
      /*DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
      if (newReader != null) {
        reader = newReader;
      }*/
      TopDocs topDocs = searcher.search(new TermQuery(ngram), 2);
      //System.out.println(ngram + " ==> " + topDocs.totalHits);
      if (topDocs.totalHits == 0) {
        Document doc = getDoc(entry.getKey(), entry.getValue());
        indexWriter.addDocument(doc);
      } else if (topDocs.totalHits == 1) {
        int docNumber = topDocs.scoreDocs[0].doc;
        Document document = reader.document(docNumber);
        long oldCount = Long.parseLong(document.getField("count").stringValue());
        //System.out.println(ngram + " -> " + oldCount + "+" + entry.getValue());
        indexWriter.deleteDocuments(ngram);
        indexWriter.addDocument(getDoc(entry.getKey(), oldCount + entry.getValue()));
        // would probably be faster, but we currently rely on the count being a common field:
        //indexWriter.updateNumericDocValue(ngram, "count", oldCount + entry.getValue());
      } else if (topDocs.totalHits > 1) {
        throw new RuntimeException("Got more than one hit for: " + ngram);
      }
      //System.out.println("   " + entry.getKey() + " -> " + entry.getValue());
    }
    indexWriter.commit();
  }

  @NotNull
  private Document getDoc(String ngram, long count) {
    Document doc = new Document();
    doc.add(new Field("ngram", ngram, StringField.TYPE_STORED));
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
    if (args.length != 2) {
      System.out.println("Usage: " + CommonCrawlToNgram.class + " <langCode> <input.xz>");
      System.exit(1);
    }
    Language language = Languages.getLanguageForShortName(args[0]);
    File input = new File(args[1]);
    File outputDir = new File(args[2]);
    try (CommonCrawlToNgram prg = new CommonCrawlToNgram(language, input, outputDir)) {
      prg.indexInputFile();
    }
  }
}
