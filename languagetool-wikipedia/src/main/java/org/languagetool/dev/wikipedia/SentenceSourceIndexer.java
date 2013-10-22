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
package org.languagetool.dev.wikipedia;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.languagetool.Language;
import org.languagetool.dev.index.Indexer;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Creates a Lucene index of a {@link SentenceSource}.
 * @since 2.4
 */
class SentenceSourceIndexer extends DefaultHandler implements AutoCloseable {

  private static final String MAX_DOC_COUNT_VALUE = "maxDocCountValue";
  private static final String MAX_DOC_COUNT_FIELD = "maxDocCount";
  private static final String MAX_DOC_COUNT_FIELD_VAL = "1";

  private final Indexer indexer;
  private final int maxDocs;
  
  private int sentenceCount = 0;
  
  SentenceSourceIndexer(Directory dir, Language language, int maxDocs) {
    this.indexer = new Indexer(dir, language);
    this.maxDocs = maxDocs;
  }

  @Override
  public void close() throws Exception {
    indexer.close();
  }

  private void run(List<String> dumpFilesNames, Language language) throws IOException, XMLStreamException {
    MixingSentenceSource mixingSource = getMixingSource(dumpFilesNames, language);
    while (mixingSource.hasNext()) {
      Sentence sentence = mixingSource.next();
      if (sentenceCount % 100 == 0) {
        System.out.println("Indexing sentence #" + sentenceCount + ":");
        System.out.println("  " + sentence);
      }
      indexer.index(sentence.getSentence(), sentence.getSource(), true, sentenceCount);
      sentenceCount++;
      if (sentenceCount > maxDocs) {
        throw new DocumentLimitReachedException(maxDocs);
      }
    }
  }

  private MixingSentenceSource getMixingSource(List<String> dumpFileNames, Language language) throws XMLStreamException, FileNotFoundException {
    List<SentenceSource> sources = new ArrayList<>();
    for (String dumpFileName : dumpFileNames) {
      File file = new File(dumpFileName);
      if (file.getName().endsWith(".xml")) {
        sources.add(new WikipediaSentenceSource(new FileInputStream(dumpFileName), language));
      } else if (file.getName().startsWith("tatoeba-")) {
        sources.add(new TatoebaSentenceSource(new FileInputStream(dumpFileName)));
      } else {
        throw new RuntimeException("Could not find a source handler for " + dumpFileName +
                " - Wikipedia files must be named '*.xml', Tatoeba files must be named 'tatoeba-*'");
      }
    }
    return new MixingSentenceSource(sources);
  }

  private void writeMetaDocuments() throws IOException {
    final Document doc = new Document();
    doc.add(new StringField(MAX_DOC_COUNT_FIELD, MAX_DOC_COUNT_FIELD_VAL, Field.Store.YES));
    doc.add(new StringField(MAX_DOC_COUNT_VALUE, sentenceCount + "", Field.Store.YES));
    indexer.add(doc);
  }

  public static void main(String... args) throws Exception {
    if (args.length != 4) {
      System.out.println("Usage: " + SentenceSourceIndexer.class.getSimpleName() + " <dataFile...> <indexDir> <languageCode> <maxDocs>");
      System.out.println("\t<dataFiles> comma-separated list of a Wikipedia XML dumps (*.xml) and/or Tatoeba files (tatoeba-*)");
      System.out.println("\t<indexDir> directory where Lucene index will be written to, existing index content will be removed");
      System.out.println("\t<languageCode> short code like en for English, de for German etc");
      System.out.println("\t<maxDocs> maximum number of documents to be indexed, use 0 for no limit");
      System.exit(1);
    }
    final List<String> dumpFilesNames = Arrays.asList(args[0].split(","));
    final File indexDir = new File(args[1]);
    final String languageCode = args[2];
    final int maxDocs = Integer.parseInt(args[3]);

    final Language language = Language.getLanguageForShortName(languageCode);
    if (maxDocs == 0) {
      System.out.println("Going to index contents from " + dumpFilesNames);
    } else {
      System.out.println("Going to index up to " + maxDocs + " documents from " + dumpFilesNames);
    }
    System.out.println("Output index dir: " + indexDir);
    
    final long start = System.currentTimeMillis();
    try (FSDirectory fsDirectory = FSDirectory.open(indexDir)) {
      final SentenceSourceIndexer indexer = new SentenceSourceIndexer(fsDirectory, language, maxDocs);
      try {
        indexer.run(dumpFilesNames, language);
      } catch (DocumentLimitReachedException e) {
        System.out.println("Document limit (" + e.getLimit() + ") reached, stopping indexing");
      } finally {
        indexer.writeMetaDocuments();
        indexer.close();
      }
    }
    final long end = System.currentTimeMillis();
    final float minutes = (end - start) / (float)(1000 * 60);
    System.out.printf("Indexing took %.2f minutes\n", minutes);
  }

}
