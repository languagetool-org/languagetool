/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Sohaib Afifi, Taha Zerrouki
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

import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Create ngram indexes for Arabic from Gumar Ngrams (https://github.com/CAMeL-Lab/Gumar-Ngrams).
 *
 * @author Sohaib Afifi
 * @since 4.9
 */
final class LuceneIndexCreator {


  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + LuceneIndexCreator.class + " <gumarNgramsDir> <ngramIndexDir>");
      System.exit(1);
    }
    File gumarNgramsDir = new File(args[0]);
    File ngramIndexDir = new File(args[1]);
    if (!ngramIndexDir.exists()) {
      ngramIndexDir.mkdir();
    }

    for (int i = 1; i <= 3; i++) {
      IndexWriterConfig conf = new IndexWriterConfig(new KeywordAnalyzer());
      String folderName = ngramIndexDir.getPath() + "/" + i + "grams";
      String srcFile = gumarNgramsDir.getPath() + "/ALL/" + i + "-grams_ALL.tsv";
      System.out.println("Generating: " + folderName + " from " + srcFile);
      try (IndexWriter iw = new IndexWriter(FSDirectory.open(new File(folderName).toPath()), conf)) {
        Scanner scan = new Scanner(new File(srcFile));
        int totalTokenCount = 0;
        while (scan.hasNext()) {
          String curLine = scan.nextLine();
          String[] splitted = curLine.split("\t");
          String word = splitted[0].trim();
          String freq = splitted[1].trim();
          addDoc(iw, word, Integer.parseInt(freq));
          totalTokenCount++;
        }
        Document document = new Document();
        document.add(new TextField("totalTokenCount", String.valueOf(totalTokenCount), Field.Store.YES));
        iw.addDocument(document);
      }

    }

  }

  private static void addDoc(IndexWriter iw, String ngram, int count) throws IOException {
    Document document = new Document();
    document.add(new TextField("ngram", ngram, Field.Store.YES));
    document.add(new TextField("count", String.valueOf(count), Field.Store.YES));
    iw.addDocument(document);
  }

}
