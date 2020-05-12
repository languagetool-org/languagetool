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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Indexing the result of {@link CommonCrawlNGramJob} with Lucene.
 * @since 3.2
 */
class AggregatedNgramToLucene implements AutoCloseable {

  private final Map<Integer, LuceneIndex> indexes = new HashMap<>();
  
  private long totalTokenCount = 0;
  private long lineCount = 0;

  AggregatedNgramToLucene(File indexTopDir) throws IOException {
    indexes.put(1, new LuceneIndex(new File(indexTopDir, "1grams")));
    indexes.put(2, new LuceneIndex(new File(indexTopDir, "2grams")));
    indexes.put(3, new LuceneIndex(new File(indexTopDir, "3grams")));
  }
  
  @Override
  public void close() throws IOException {
    for (LuceneIndex index : indexes.values()) {
      index.close();
    }
  }

  void indexInputFile(File file) throws IOException {
    System.out.println("=== Indexing " + file + " ===");
    try (Scanner scanner = new Scanner(file)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        indexLine(line);
      }
    }
  }

  private void indexLine(String line) throws IOException {
    if (lineCount++ % 250_000 == 0) {
      System.out.printf(Locale.ENGLISH, "Indexing line %d\n", lineCount);
    }
    String[] lineParts = line.split("\t");
    if (lineParts.length != 2) {
      System.err.println("Not 2 parts but " + lineParts.length + ", ignoring: '" + line + "'");
      return;
    }
    String ngram = lineParts[0];
    String[] ngramParts = ngram.split(" ");
    LuceneIndex index = indexes.get(ngramParts.length);
    if (index == null) {
      throw new RuntimeException("No ngram data found for: " + Arrays.toString(lineParts));
    }
    long count = Long.parseLong(lineParts[1]);
    if (ngramParts.length == 1) {
      totalTokenCount += count;
    }
    index.indexWriter.addDocument(getDoc(ngram, count));
  }

  @NotNull
  private Document getDoc(String ngram, long count) {
    Document doc = new Document();
    doc.add(new Field("ngram", ngram, StringField.TYPE_NOT_STORED));  // use StringField.TYPE_STORED for easier debugging with e.g. Luke
    doc.add(getCountField(count));
    return doc;
  }

  @NotNull
  private LongField getCountField(long count) {
    FieldType fieldType = new FieldType();
    fieldType.setStored(true);
    fieldType.setOmitNorms(true);
    fieldType.setNumericType(FieldType.NumericType.LONG);
    fieldType.setDocValuesType(DocValuesType.NUMERIC);
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
    if (args.length != 1) {
      System.out.println("Usage: " + AggregatedNgramToLucene.class + " <inputDir>");
      System.out.println(" <inputDir> is a directory with aggregated ngram files from Hadoop, e.g. produced by CommonCrawlNGramJob");
      System.exit(1);
    }
    File inputDir = new File(args[0]);
    File outputDir = new File(inputDir, "index");
    System.out.println("Indexing to " + outputDir);
    try (AggregatedNgramToLucene prg = new AggregatedNgramToLucene(outputDir)) {
      for (File file : inputDir.listFiles()) {
        if (file.isFile()) {
          prg.indexInputFile(file);
        }
      }
      prg.addTotalTokenCountDoc(prg.totalTokenCount, prg.indexes.get(1).indexWriter);
    }
  }
  
  static class LuceneIndex {

    private final Directory directory;
    private final IndexWriter indexWriter;

    LuceneIndex(File dir) throws IOException {
      Analyzer analyzer = new StandardAnalyzer();
      IndexWriterConfig config = new IndexWriterConfig(analyzer);
      directory = FSDirectory.open(dir.toPath());
      indexWriter = new IndexWriter(directory, config);
    }
    
    void close() throws IOException {
      indexWriter.close();
      directory.close();
    }

  }
}
