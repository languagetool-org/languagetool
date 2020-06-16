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
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.dev.dumpcheck.Sentence;
import org.languagetool.tokenizers.SentenceTokenizer;

import static org.languagetool.dev.index.Lucene.FIELD_NAME;
import static org.languagetool.dev.index.Lucene.FIELD_NAME_LOWERCASE;
import static org.languagetool.dev.index.Lucene.SOURCE_FIELD_NAME;

/**
 * A class with a main() method that takes a text file and indexes its sentences, including POS tags
 * 
 * @author Tao Lin, Miaojuan Dai
 */
public class Indexer implements AutoCloseable {

  static final String TITLE_FIELD_NAME = "title";

  private final IndexWriter writer;
  private final SentenceTokenizer sentenceTokenizer;

  private boolean lowercaseOnly;

  public Indexer(Directory dir, Language language) {
    this(dir, language, getAnalyzer(language));
  }

  public Indexer(Directory dir, Language language, Analyzer analyzer) {
    try {
      IndexWriterConfig writerConfig = getIndexWriterConfig(analyzer);
      writerConfig.setOpenMode(OpenMode.CREATE);
      writer = new IndexWriter(dir, writerConfig);
      sentenceTokenizer = language.getSentenceTokenizer();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Set to true to index only a lowercase field (makes index smaller).
   */
  public void setLowercaseOnly(boolean lowercaseOnly) {
    this.lowercaseOnly = lowercaseOnly;
  }
  
  public static void main(String[] args) throws IOException {
    ensureCorrectUsageOrExit(args);
    run(args[0], args[1], args[2]);
  }

  static Analyzer getAnalyzer(Language language) {
    Map<String, Analyzer> analyzerMap = new HashMap<>();
    analyzerMap.put(FIELD_NAME, new LanguageToolAnalyzer(new JLanguageTool(language), false));
    analyzerMap.put(FIELD_NAME_LOWERCASE, new LanguageToolAnalyzer(new JLanguageTool(language), true));
    return new PerFieldAnalyzerWrapper(new DoNotUseAnalyzer(), analyzerMap);
  }

  static IndexWriterConfig getIndexWriterConfig(Analyzer analyzer) {
    return new IndexWriterConfig(analyzer);
  }

  private static void ensureCorrectUsageOrExit(String[] args) {
    if (args.length != 3) {
      System.err.println("Usage: Indexer <textFile> <indexDir> <languageCode>");
      System.err.println("\ttextFile path to a text file to be indexed (line end implies sentence end)");
      System.err.println("\tindexDir path to a directory storing the index");
      System.err.println("\tlanguageCode short language code, e.g. en for English");
      System.exit(1);
    }
  }

  private static void run(String textFile, String indexDir, String languageCode) throws IOException {
    File file = new File(textFile);
    if (!file.exists() || !file.canRead()) {
      System.out.println("Text file '" + file.getAbsolutePath()
          + "' does not exist or is not readable, please check the path");
      System.exit(1);
    }
    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      System.out.println("Indexing to directory '" + indexDir + "'...");
      try (FSDirectory directory = FSDirectory.open(new File(indexDir).toPath())) {
        Language language = Languages.getLanguageForShortCode(languageCode);
        try (Indexer indexer = new Indexer(directory, language)) {
          indexer.indexText(reader);
        }
      }
    }
    System.out.println("Index complete!");
  }

  public static void run(String content, Directory dir, Language language) throws IOException {
    BufferedReader br = new BufferedReader(new StringReader(content));
    try (Indexer indexer = new Indexer(dir, language)) {
      indexer.indexText(br);
    }
  }

  public void indexSentence(Sentence sentence, int docCount) throws IOException {
    BufferedReader reader = new BufferedReader(new StringReader(sentence.getText()));
    String line;
    while ((line = reader.readLine()) != null) {
      add(line, sentence.getSource(), sentence.getTitle(), docCount);
    }
  }

  public void indexText(BufferedReader reader) throws IOException {
    String line;
    StringBuilder paragraph = new StringBuilder();
    int i = 0;
    int addCount = 0;
    while ((line = reader.readLine()) != null) {
      if (!(line.equals("")) && paragraph.length() + line.length() < Integer.MAX_VALUE) {
        paragraph.append(line).append('\n');
      } else {
        List<String> sentences = sentenceTokenizer.tokenize(paragraph.toString());
        for (String sentence : sentences) {
          if (sentence.trim().length() > 0) {
            if (++addCount % 1000 == 0) {
              System.out.println("(1) Adding item " + addCount);
            }
            add(sentence.replaceAll(" \n"," "), null, null, -1);
          }
        }
        if (paragraph.length() + line.length() >= Integer.MAX_VALUE) {
          List<String> lastSentences = sentenceTokenizer.tokenize(line);
          for (String sentence : lastSentences) {
            if (++addCount % 1000 == 0) {
              System.out.println("(2) Adding item " + addCount);
            }
            add(sentence, null, null, -1);
          }
        }
        paragraph.setLength(0);
      }
      if (++i % 10_000 == 0) {
        System.out.println("Loading line " + i);
      }
    }
    if (paragraph.length() > 0) {
      List<String> sentences = sentenceTokenizer.tokenize(paragraph.toString());
      for (String sentence : sentences) {
        if (++addCount % 1000 == 0) {
          System.out.println("(3) Adding item " + addCount);
        }
        add(sentence, null, null, -1);
      }
    }
  }

  public void add(Document doc) throws IOException {
    writer.addDocument(doc);
  }

  private void add(String sentence, String source, String title, int docCount) throws IOException {
    Document doc = new Document();
    FieldType type = new FieldType();
    type.setStored(true);
    type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
    type.setTokenized(true);
    if (!lowercaseOnly) {
      doc.add(new Field(FIELD_NAME, sentence, type));
    }
    doc.add(new Field(FIELD_NAME_LOWERCASE, sentence, type));
    if (docCount != -1) {
      FieldType countType = new FieldType();
      countType.setStored(true);
      countType.setIndexOptions(IndexOptions.NONE);
      doc.add(new Field("docCount", String.valueOf(docCount), countType));
    }
    if (title != null) {
      FieldType titleType = new FieldType();
      titleType.setStored(true);
      titleType.setIndexOptions(IndexOptions.NONE);
      titleType.setTokenized(false);
      doc.add(new Field(TITLE_FIELD_NAME, title, titleType));
    }
    if (source != null) {
      FieldType sourceType = new FieldType();
      sourceType.setStored(true);
      sourceType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
      sourceType.setTokenized(false);
      doc.add(new Field(SOURCE_FIELD_NAME, source, sourceType));
    }
    writer.addDocument(doc);
  }

  @Override
  public void close() throws IOException {
    writer.close();
  }

  public void commit() throws IOException {
    if (writer.isOpen()) {
      writer.commit();
    }
  }
}
