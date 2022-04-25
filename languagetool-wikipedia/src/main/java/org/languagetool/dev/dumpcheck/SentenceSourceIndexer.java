/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.dumpcheck;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.dev.index.Indexer;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Creates a Lucene index of a {@link SentenceSource}.
 * Performance examples (Dell XPS 13 9360):
 * German Wikipedia and Tatoeba With POS tags: 22,000 sentences per minute 
 * German Wikipedia and Tatoeba Without POS tags: 2.4 million sentences per minute 
 * @since 2.4
 */
public class SentenceSourceIndexer extends DefaultHandler implements AutoCloseable {

  public static final String MAX_DOC_COUNT_VALUE = "maxDocCountValue";
  public static final String MAX_DOC_COUNT_FIELD = "maxDocCount";
  public static final String MAX_DOC_COUNT_FIELD_VAL = "1";
  
  private static final boolean LC_ONLY = true;

  private final Indexer indexer;
  private final int maxSentences;
  
  private boolean stopped = false;
  
  private int sentenceCount = 0;
  
  SentenceSourceIndexer(Directory dir, Language language, int maxSentences, Analyzer analyzer) {
    if (analyzer == null) {
      this.indexer = new Indexer(dir, language);
    } else {
      this.indexer = new Indexer(dir, language, analyzer);
    }
    this.indexer.setLowercaseOnly(LC_ONLY);
    this.maxSentences = maxSentences;
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        super.run();
        stopped = true;
        System.out.println("----- Got SIGHUP, will commit and exit ----");
        try {
          indexer.commit();
          System.out.println("----- commit done, will exit now ----");
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  SentenceSourceIndexer(Directory dir, Language language, int maxSentences) {
    this(dir, language, maxSentences, null);
  }

  @Override
  public void close() throws Exception {
    indexer.close();
  }

  private void run(List<String> dumpFileNames, Language language) throws IOException {
    MixingSentenceSource mixingSource = MixingSentenceSource.create(dumpFileNames, language);
    while (mixingSource.hasNext()) {
      if (stopped) {
        return;
      }
      Sentence sentence = mixingSource.next();
      if (sentenceCount % 10_000 == 0) {
        //System.out.println("Indexing sentence #" + sentenceCount + " (" + mixingSource.getSourceDistribution() + "):");  // doesn't work well with URLs as source
        System.out.println("Indexing sentence #" + sentenceCount + ":");
        System.out.println("  [" +  sentence.getSource() + "] " + sentence);
      }
      indexer.indexSentence(sentence, sentenceCount);
      sentenceCount++;
      if (maxSentences > 0 && sentenceCount >= maxSentences) {
        throw new DocumentLimitReachedException(maxSentences);
      }
    }
  }

  private void writeMetaDocuments() throws IOException {
    Document doc = new Document();
    doc.add(new StringField(MAX_DOC_COUNT_FIELD, MAX_DOC_COUNT_FIELD_VAL, Field.Store.YES));
    doc.add(new StringField(MAX_DOC_COUNT_VALUE, sentenceCount + "", Field.Store.YES));
    indexer.add(doc);
  }

  public static void main(String... args) throws Exception {
    if (args.length != 5) {
      System.out.println("Usage: " + SentenceSourceIndexer.class.getSimpleName() + " <dataFile...> <indexDir> <languageCode> <maxSentences> <indexPosTags>");
      System.out.println("\t<dataFiles> comma-separated list of a Wikipedia XML dump (*.xml) and/or Tatoeba files (tatoeba-*)");
      System.out.println("\t<indexDir> directory where Lucene index will be written to, existing index content will be removed");
      System.out.println("\t<languageCode> short code like en for English, de for German etc");
      System.out.println("\t<maxSentences> maximum number of sentences to be indexed, use 0 for no limit");
      System.out.println("\t<indexPosTags> 1 to also index POS tags (i.e. analyze text by LT), 0 to index only the plain text");
      System.exit(1);
    }
    List<String> dumpFilesNames = Arrays.asList(args[0].split(","));
    File indexDir = new File(args[1]);
    String languageCode = args[2];
    int maxSentences = Integer.parseInt(args[3]);

    Language language = Languages.getLanguageForShortCode(languageCode);
    if (maxSentences == 0) {
      System.out.println("Going to index contents from " + dumpFilesNames);
    } else {
      System.out.println("Going to index up to " + maxSentences + " sentences from " + dumpFilesNames);
    }
    System.out.println("Output index dir: " + indexDir);
    long start = System.currentTimeMillis();
    Analyzer analyzer;
    String indexPos = args[4];
    if (indexPos.equals("1")) {
      analyzer = null;  // this will use LanguageToolAnalyzer
    } else if (indexPos.equals("0")) {
      analyzer = new StandardAnalyzer(new CharArraySet(Collections.emptyList(), false));
    } else {
      throw new IllegalArgumentException("Unknown value '" + indexPos + "' for indexPosTags parameter, use 0 or 1");
    }
    try (FSDirectory fsDirectory = FSDirectory.open(indexDir.toPath());
         SentenceSourceIndexer indexer = new SentenceSourceIndexer(fsDirectory, language, maxSentences, analyzer)) {
      try {
        indexer.run(dumpFilesNames, language);
      } catch (DocumentLimitReachedException e) {
        System.out.println("Sentence limit (" + e.getLimit() + ") reached, stopping indexing");
      } finally {
        indexer.writeMetaDocuments();
      }
      if (analyzer != null) {
        analyzer.close();
      }
    }
    long end = System.currentTimeMillis();
    float minutes = (end - start) / (float)(1000 * 60);
    System.out.printf("Indexing took %.2f minutes\n", minutes);
  }

}
