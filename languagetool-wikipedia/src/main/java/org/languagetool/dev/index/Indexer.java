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
package org.languagetool.dev.index;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.tokenizers.SentenceTokenizer;

/**
 * A class with a main() method that takes a text file and indexes its sentences, including POS tags
 * 
 * @author Tao Lin, Miaojuan Dai
 */
public class Indexer {

  private static final Version LUCENE_VERSION = Version.LUCENE_42;
  private static final String FIELD_NAME = "field";
  private static final String FIELD_NAME_LOWERCASE = "fieldLowercase";

  private final IndexWriter writer;
  private final SentenceTokenizer sentenceTokenizer;

  public Indexer(Directory dir, Language language) {
    try {
      final Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
      analyzerMap.put(FIELD_NAME, new LanguageToolAnalyzer(LUCENE_VERSION, new JLanguageTool(language), false));
      analyzerMap.put(FIELD_NAME_LOWERCASE, new LanguageToolAnalyzer(LUCENE_VERSION, new JLanguageTool(language), true));
      final Analyzer analyzer = new PerFieldAnalyzerWrapper(new DoNotUseAnalyzer(), analyzerMap);
      final IndexWriterConfig writerConfig = new IndexWriterConfig(LUCENE_VERSION, analyzer);
      writerConfig.setOpenMode(OpenMode.CREATE);
      writer = new IndexWriter(dir, writerConfig);
      sentenceTokenizer = language.getSentenceTokenizer();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void main(String[] args) throws IOException {
    ensureCorrectUsageOrExit(args);
    run(args[0], args[1], args[2]);
  }

  private static void ensureCorrectUsageOrExit(String[] args) {
    if (args.length != 3) {
      System.err.println("Usage: Indexer <textFile> <indexDir> <languageCode>");
      System.err.println("\ttextFile path to a text file to be indexed");
      System.err.println("\tindexDir path to a directory storing the index");
      System.err.println("\tlanguageCode short language code, e.g. en for English");
      System.exit(1);
    }
  }

  private static void run(String textFile, String indexDir, String languageCode) throws IOException {
    final File file = new File(textFile);
    if (!file.exists() || !file.canRead()) {
      System.out.println("Text file '" + file.getAbsolutePath()
          + "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    final BufferedReader reader = new BufferedReader(new FileReader(file));
    System.out.println("Indexing to directory '" + indexDir + "'...");
    final FSDirectory directory = FSDirectory.open(new File(indexDir));
    try {
      final Language language = Language.getLanguageForShortName(languageCode);
      final Indexer indexer = new Indexer(directory, language);
      try {
        run(reader, indexer, false);
      } finally {
        indexer.close();
      }
    } finally {
      directory.close();
    }
    System.out.println("Index complete!");
  }

  public static void run(String content, Directory dir, Language language, boolean isSentence) throws IOException {
    final BufferedReader br = new BufferedReader(new StringReader(content));
    final Indexer indexer = new Indexer(dir, language);
    try {
      run(br, indexer, isSentence);
    } finally {
      indexer.close();
    }
  }

  public static void run(BufferedReader reader, Indexer indexer, boolean isSentence) throws IOException {
    indexer.index(reader, isSentence, -1);
  }

  public void index(String content, boolean isSentence, int docCount) throws IOException {
    final BufferedReader br = new BufferedReader(new StringReader(content));
    index(br, isSentence, docCount);
  }

  public void index(BufferedReader reader, boolean isSentence, int docCount) throws IOException {
    String line = "";
    while ((line = reader.readLine()) != null) {
      if (isSentence) {
        add(-1, line);
      } else {
        final List<String> sentences = sentenceTokenizer.tokenize(line);
        for (String sentence : sentences) {
          add(docCount, sentence);
        }
      }
    }
  }

  public void add(Document doc) throws IOException {
    writer.addDocument(doc);
  }

  private void add(int docCount, String sentence) throws IOException {
    final Document doc = new Document();
    final FieldType type = new FieldType();
    type.setStored(true);
    type.setIndexed(true);
    type.setTokenized(true);
    doc.add(new Field(FIELD_NAME, sentence, type));
    doc.add(new Field(FIELD_NAME_LOWERCASE, sentence.toLowerCase(), type));
    if (docCount != -1) {
      final FieldType countType = new FieldType();
      countType.setStored(true);
      countType.setIndexed(false);
      doc.add(new Field("docCount", docCount + "", countType));
    }
    writer.addDocument(doc);
  }

  public void close() throws IOException {
    writer.close();
  }

  class DoNotUseAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String s, Reader reader) {
      throw new RuntimeException("This analyzer is not supposed to be called");
    }
  }
}
