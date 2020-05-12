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
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.languagetool.dev.index.Lucene;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Index sentences with Lucene from a plain text file, one sentence per line.
 * Can be used for {@link AutomaticConfusionRuleEvaluator} to find example
 * sentences.
 * @since 3.2
 */
class TextIndexCreator {

  private void index(File outputDir, String[] inputFiles) throws IOException {
    Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);
    IndexWriterConfig config = new IndexWriterConfig(analyzer);
    try (FSDirectory directory = FSDirectory.open(outputDir.toPath());
         IndexWriter indexWriter = new IndexWriter(directory, config)) {
      for (String inputFile : inputFiles) {
        indexFile(indexWriter, inputFile);
      }
    }
  }

  private void indexFile(IndexWriter indexWriter, String inputFile) throws IOException {
    System.out.println("Indexing " + inputFile);
    int lineCount = 0;
    try (BufferedReader br = Files.newBufferedReader(Paths.get(inputFile))) {
      String line;
      while ((line = br.readLine()) != null) {
        Document doc = new Document();
        doc.add(new TextField(Lucene.FIELD_NAME, line, Field.Store.YES));
        doc.add(new TextField(Lucene.FIELD_NAME_LOWERCASE, line.toLowerCase(), Field.Store.YES));
        indexWriter.addDocument(doc);
        if (++lineCount % 10_000 == 0) {
          System.out.println(lineCount + "...");
        }
      }
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.out.println("Usage: " + TextIndexCreator.class.getSimpleName() + " <outputDir> <inputFile...>");
      System.exit(0);
    }
    TextIndexCreator creator = new TextIndexCreator();
    File outputDir = new File(args[0]);
    if (outputDir.exists()) {
      throw new RuntimeException("Output directory already exists: " + outputDir);
    }
    creator.index(outputDir, Arrays.copyOfRange(args, 1, args.length));
  }
  
}
