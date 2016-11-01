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
package org.languagetool.dev;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * Create an empty Lucene index that only contains a meta document.
 * Useful to fake ngram data if one doesn't have enough disk space.
 */
final class EmptyLuceneIndexCreator {

  private EmptyLuceneIndexCreator() {
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + EmptyLuceneIndexCreator.class.getSimpleName() + " <indexPath>");
      System.exit(1);
    }
    Analyzer analyzer = new StandardAnalyzer();
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    Directory directory = FSDirectory.open(new File(args[0]).toPath());
    try (IndexWriter writer = new IndexWriter(directory, config)) {
      FieldType fieldType = new FieldType();
      fieldType.setIndexOptions(IndexOptions.DOCS);
      fieldType.setStored(true);
      Field countField = new Field("totalTokenCount", String.valueOf(0), fieldType);
      Document doc = new Document();
      doc.add(countField);
      writer.addDocument(doc);
    }
  }

}
