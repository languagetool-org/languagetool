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
package org.languagetool.dev.wikipedia;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.languagetool.Language;
import org.languagetool.dev.dumpcheck.DocumentLimitReachedException;
import org.languagetool.dev.index.Indexer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Wikipedia handler for indexing. See {@link org.languagetool.dev.index.Searcher} for a
 * class that lets you use this index.
 * 
 * @author Tao Lin
 * @deprecated use {@link org.languagetool.dev.dumpcheck.SentenceSourceIndexer} instead (deprecated since 2.4)
 */
@Deprecated
public class WikipediaIndexHandler extends DefaultHandler implements AutoCloseable {

  public static final String MAX_DOC_COUNT_VALUE = "maxDocCountValue";
  public static final String MAX_DOC_COUNT_FIELD = "maxDocCount";
  public static final String MAX_DOC_COUNT_FIELD_VAL = "1";

  private final Indexer indexer;
  private int articleCount = 0;
  
  // the number of the wiki page to start indexing
  private int start = 0;
  // the number of the wiki page to end indexing
  private int end = 0;

  private boolean inText = false;
  private boolean inTitle = false;
  private StringBuilder text = new StringBuilder();
  private StringBuilder title = new StringBuilder();
  private TextMapFilter textFilter = new SwebleWikipediaTextFilter();

  // ===========================================================
  // SAX DocumentHandler methods
  // ===========================================================

  public WikipediaIndexHandler(Directory dir, Language language, int start, int end) {
    this.indexer = new Indexer(dir, language);
    this.start = start;
    this.end = end;
    if (start > end && end != 0) {
      throw new RuntimeException("\"start\" should be smaller than \"end\": " + start + ", " + end);
    }
    textFilter = TextFilterTools.getTextFilter(language);
  }

  @Override
  @SuppressWarnings("unused")
  public void startElement(String namespaceURI, String lName, String qName, Attributes attrs)
      throws SAXException {
    if (qName.equals("title")) {
      inTitle = true;
    } else if (qName.equals("text")) {
      inText = true;
    }
  }

  @Override
  @SuppressWarnings("unused")
  public void endElement(String namespaceURI, String sName, String qName) {
    if (qName.equals("title")) {
      inTitle = false;
    } else if (qName.equals("text")) {
      System.out.println(++articleCount + ": " + title);
      final String tempTitle = title.toString();
      title = new StringBuilder();
      if (articleCount < start) {
        return;
      } else if (articleCount >= end && end != 0) {
        throw new DocumentLimitReachedException(end);
      }
      try {
        final String textToCheck;
        try {
          textToCheck = textFilter.filter(text.toString()).getPlainText();
          if (!textToCheck.contains("#REDIRECT") && !textToCheck.trim().equals("")) {
            indexer.index(textToCheck, false, articleCount);
          }
        } catch (Exception e) {
          System.err.println("Exception when filtering/indexing '" + tempTitle + "' (" + articleCount + ") - skipping file. Stacktrace follows:");
          e.printStackTrace();
        }
      } catch (Exception e) {
        throw new RuntimeException("Failed checking article '" + tempTitle + "' (" + articleCount + ")", e);
      }
    }
    text = new StringBuilder();
    inText = false;
  }

  @Override
  public void characters(char[] buf, int offset, int len) {
    final String s = new String(buf, offset, len);
    if (inText) {
      text.append(s);
    } else if (inTitle) {
      title.append(s);
    }
  }

  @Override
  public void close() throws Exception {
    indexer.close();
  }

  private void writeMetaDocuments() throws IOException {
    final Document doc = new Document();
    doc.add(new StringField(MAX_DOC_COUNT_FIELD, MAX_DOC_COUNT_FIELD_VAL, Field.Store.YES));
    doc.add(new StringField(MAX_DOC_COUNT_VALUE, articleCount + "", Field.Store.YES));
    indexer.add(doc);
  }

  public static void main(String... args) throws Exception {
    if (args.length != 4) {
      System.out.println("Usage: " + WikipediaIndexHandler.class.getSimpleName() + " <wikipediaDump> <indexDir> <languageCode> <maxDocs>");
      System.out.println("\t<wikipediaDump> a Wikipedia XML dump");
      System.out.println("\t<indexDir> directory where Lucene index will be written to, existing index content will be removed");
      System.out.println("\t<languageCode> short code like en for English, de for German etc");
      System.out.println("\t<maxDocs> maximum number of documents to be indexed, use 0 for no limit");
      System.exit(1);
    }
    final File dumpFile = new File(args[0]);
    final File indexDir = new File(args[1]);
    final String languageCode = args[2];
    final int maxDocs = Integer.parseInt(args[3]);

    final Language language = Language.getLanguageForShortName(languageCode);
    if (maxDocs == 0) {
      System.out.println("Going to index all documents from " + dumpFile);
    } else {
      System.out.println("Going to index up to " + maxDocs + " documents from " + dumpFile);
    }
    System.out.println("Output index dir: " + indexDir);
    final long start = System.currentTimeMillis();
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    final SAXParser saxParser = factory.newSAXParser();
    try (FSDirectory fsDirectory = FSDirectory.open(indexDir)) {
      final WikipediaIndexHandler handler = new WikipediaIndexHandler(fsDirectory, language, 1, maxDocs);
      try {
        saxParser.parse(new FileInputStream(dumpFile), handler);
      } catch (DocumentLimitReachedException e) {
        System.out.println("Document limit (" + e.getLimit() + ") reached, stopping indexing");
      } finally {
        handler.writeMetaDocuments();
        handler.close();
      }
    }
    final long end = System.currentTimeMillis();
    final float minutes = (end - start) / (float)(1000 * 60);
    System.out.printf("Indexing took %.2f minutes\n", minutes);
  }

}
