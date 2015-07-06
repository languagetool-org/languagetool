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

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * Create very simple ngram indexes for testing.
 */
final class LuceneSimpleIndexCreator {

  private LuceneSimpleIndexCreator() {}

  public static void main(String[] args) throws IOException {
    IndexWriterConfig conf = new IndexWriterConfig(new KeywordAnalyzer());
    try (IndexWriter iw1 = new IndexWriter(FSDirectory.open(new File("/tmp/1grams").toPath()), conf)) {
      addDoc(iw1, "the", 55);
      addDoc(iw1, "nice", 10);
      addDoc(iw1, "building", 1);
      Document document = new Document();
      document.add(new TextField("totalTokenCount", String.valueOf(3), Field.Store.YES));
      iw1.addDocument(document);
    }
    IndexWriterConfig conf2 = new IndexWriterConfig(new KeywordAnalyzer());
    try (IndexWriter iw2 = new IndexWriter(FSDirectory.open(new File("/tmp/2grams").toPath()), conf2)) {
      addDoc(iw2, "the nice", 3);
      addDoc(iw2, "nice building", 2);
    }
    IndexWriterConfig conf3 = new IndexWriterConfig(new KeywordAnalyzer());
    try (IndexWriter iw3 = new IndexWriter(FSDirectory.open(new File("/tmp/3grams").toPath()), conf3)) {
      addDoc(iw3, "the nice building", 1);
    }
  }

  private static void addDoc(IndexWriter iw, String ngram, int count) throws IOException {
    Document document = new Document();
    document.add(new TextField("ngram", ngram, Field.Store.YES));
    document.add(new TextField("count", String.valueOf(count), Field.Store.YES));
    iw.addDocument(document);
  }

}
