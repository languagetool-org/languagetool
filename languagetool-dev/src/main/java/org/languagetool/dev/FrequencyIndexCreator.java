/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Index *.gz files from Google's ngram corpus into a Lucene index.
 */
public class FrequencyIndexCreator {

  private static final int MIN_YEAR = 1910;
  private static final String NAME_REGEX = "googlebooks-eng-all-2gram-20120701-(.*?).gz";

  private void run(File inputDir, File indexBaseDir) throws IOException {
    List<File> files = Arrays.asList(inputDir.listFiles());
    for (File file : files) {
      String name = file.getName();
      if (name.contains("_")) {
        System.out.println("Skipping " + name + " contains underscore");
        continue;
      }
      if (!name.startsWith("googlebooks-") && name.endsWith(".gz")) {
        System.out.println("Skipping " + name + " - unexpected file name");
        continue;
      }
      if (!name.matches(NAME_REGEX)) {
        System.out.println("Skipping " + name + " doesn't match regex " + NAME_REGEX);
        continue;
      }
      Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_48);
      File indexDir = new File(indexBaseDir, name.replaceAll(NAME_REGEX, "$1"));
      System.out.println("Index dir: " + indexDir);
      Directory directory = FSDirectory.open(indexDir);
      IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_48, analyzer);
      try (IndexWriter writer = new IndexWriter(directory, config)) {
        Collections.sort(files);
        indexLinesFromGoogleFile(writer, file);
      }
    }
  }

  private void indexLinesFromGoogleFile(IndexWriter writer, File inputFile) throws IOException {
    System.out.println("==== Working on " + inputFile + " ====");
    InputStream fileStream = new FileInputStream(inputFile);
    InputStream gzipStream = new GZIPInputStream(fileStream);
    Reader decoder = new InputStreamReader(gzipStream, "utf-8");
    BufferedReader buffered = new BufferedReader(decoder);
    Scanner scanner = new Scanner(buffered);
    int i = 0;
    long docCount = 0;
    long lineCount = 0;
    String prevText = null;
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine();
      lineCount++;
      String[] parts = line.split("\t");
      String text = parts[0];
      if (text.contains("_")) { // ignore POS tags
        continue;
      }
      int year = Integer.parseInt(parts[1]);
      if (year < MIN_YEAR) {
        continue;
      }
      if (prevText == null || prevText.equals(text)) {
        // aggregate years
        docCount += Long.parseLong(parts[2]);
      } else {
        //System.out.println(">"+ prevText + ": " + count);
        addDoc(writer, prevText, docCount + "");
        if (i % 1_000 == 0) {
          System.out.printf("doc:%s line:%s ngram:%s occ:%s\n",
                  NumberFormat.getNumberInstance(Locale.US).format(i),
                  NumberFormat.getNumberInstance(Locale.US).format(lineCount),
                  prevText,
                  NumberFormat.getNumberInstance(Locale.US).format(docCount));
        }
        docCount = Long.parseLong(parts[2]);
        i++;
      }
      prevText = text;
    }
  }

  private void addDoc(IndexWriter writer, String text, String count) throws IOException {
    Document doc = new Document();
    doc.add(new Field("ngram", text, StringField.TYPE_STORED));
    doc.add(new LongField("count", Long.parseLong(count), Field.Store.YES));
    writer.addDocument(doc);
  }

  public static void main(String[] args) throws IOException {
    FrequencyIndexCreator creator = new FrequencyIndexCreator();
    creator.run(new File("/media/Data/google-ngram/2gram/"),
                new File("/media/Data/google-ngram/2gram/lucene-index"));
  }
}
