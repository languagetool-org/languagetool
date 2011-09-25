/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.dev.index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.tokenizers.SentenceTokenizer;

/**
 * A class with a main() method that takes a text file and indexes its sentences, including POS tags
 * 
 * @author Tao Lin
 */
public class Indexer {

  private final IndexWriter writer;
  private final SentenceTokenizer sentenceTokenizer;

  public Indexer(Directory dir, Language language) {
    try {
      final Analyzer analyzer = new LanguageToolAnalyzer(Version.LUCENE_31, new JLanguageTool(language));
      final IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_31, analyzer);
      iwc.setOpenMode(OpenMode.CREATE);
      writer = new IndexWriter(dir, iwc);
      sentenceTokenizer = language.getSentenceTokenizer();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) throws IOException {
    ensureCorrectUsageOrExit(args);
    run(args[0], args[1]);
  }

  private static void ensureCorrectUsageOrExit(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: Indexer <textFile> <indexDir>");
      System.err.println("\ttextFile path to a text file to be indexed");
      System.err.println("\tindexDir path to a directory storing the index");
      System.exit(1);
    }
  }

  private static void run(String textFile, String indexDir) throws IOException {
    final File file = new File(textFile);
    if (!file.exists() || !file.canRead()) {
      System.out.println("Text file '" + file.getAbsolutePath()
          + "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    final BufferedReader reader = new BufferedReader(new FileReader(file));
    System.out.println("Indexing to directory '" + indexDir + "'...");
    run(reader, new Indexer(FSDirectory.open(new File(indexDir)), Language.ENGLISH), false);
    System.out.println("Index complete!");
  }

  public static void run(String content, Directory dir, Language language, boolean isSentence)
      throws IOException {
    final BufferedReader br = new BufferedReader(new StringReader(content));
    run(br, new Indexer(dir, language), isSentence);
  }

  public static void run(BufferedReader reader, Indexer indexer, boolean isSentence)
      throws IOException {
    indexer.index(reader, isSentence);
    indexer.close();
  }

  public void index(String content, boolean isSentence) throws IOException {
    final BufferedReader br = new BufferedReader(new StringReader(content));
    index(br, isSentence);
  }

  public void index(BufferedReader reader, boolean isSentence) throws IOException {
    String line = "";
    int lineNo = 1;
    while ((line = reader.readLine()) != null) {
      if (isSentence) {
        add(lineNo, line);
      } else {
        final List<String> sentences = sentenceTokenizer.tokenize(line);
        for (String sentence : sentences) {
          add(lineNo, sentence);
          // System.out.println(sentence);
        }
      }
      lineNo++;
    }
  }

  private void add(int lineNo, String sentence) throws IOException {
    final Document doc = new Document();
    doc.add(new Field(PatternRuleQueryBuilder.FIELD_NAME, sentence, Store.YES, Index.ANALYZED));
    // doc.add(new Field(FIELD_LINE, lineNo + "", Store.YES, Index.NO));
    writer.addDocument(doc);

  }

  public void close() throws IOException {
    writer.optimize();
    writer.close();
  }
}
