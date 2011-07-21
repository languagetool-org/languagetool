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
package de.danielnaber.languagetool.dev.wikipedia;

import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TextFilter;
import de.danielnaber.languagetool.dev.index.Indexer;
import de.danielnaber.languagetool.dev.tools.RomanianDiacriticsModifier;

/**
 * 
 * Wikipedia handler for indexing.
 * 
 * @author Tao Lin
 * 
 */
public class WikipediaIndexHandler extends DefaultHandler {

  protected static final int CONTEXT_SIZE = 50;

  protected static final String MARKER_START = "<err>";

  protected static final String MARKER_END = "</err>";

  protected static final String LANG_MARKER = "XX";

  protected static final String URL_PREFIX = "http://" + LANG_MARKER + ".wikipedia.org/wiki/";

  private int articleCount = 0;

  // the number of the wiki page to start indexing
  private int start = 0;

  // the number of the wiki page to end indexing
  private int end = 0;

  private boolean inText = false;

  private StringBuilder text = new StringBuilder();

  private TextFilter textFilter = new WikipediaTextFilter();

  private final Language language;

  private final Indexer indexer;

  // ===========================================================
  // SAX DocumentHandler methods
  // ===========================================================

  public WikipediaIndexHandler(Directory dir, Language language, int start, int end) {
    this.language = language;
    this.indexer = new Indexer(dir, language);
    this.start = start;
    this.end = end;
    if (start > end) {
      throw new RuntimeException("\"Start\" should be smaller than \"End\"");
    }
    initTextFilter();
  }

  /**
   * initialize textFilter field
   */
  private void initTextFilter() {
    if (Language.ROMANIAN == language) {
      textFilter = new WikipediaTextFilter() {
        @Override
        public String filter(String arg0) {
          final String tmp = super.filter(arg0);
          // diacritics correction (comma-bellow instead of sedilla for ș and ț)
          return RomanianDiacriticsModifier.correctDiacritrics(tmp);
        }
      };
    } else {
      textFilter = new WikipediaTextFilter();
    }
  }

  @SuppressWarnings("unused")
  public void startElement(String namespaceURI, String lName, String qName, Attributes attrs)
      throws SAXException {
    if (qName.equals("title")) {
      inText = true;
    } else if (qName.equals("text")) {
      inText = true;
    }
  }

  @SuppressWarnings("unused")
  public void endElement(String namespaceURI, String sName, String qName) {
    if (qName.equals("title")) {
      text = new StringBuilder();
    } else if (qName.equals("text")) {
      try {
        System.out.println(articleCount++);
        if (articleCount < start) {
          return;
        } else if (articleCount >= end) {
          throw new RuntimeException();
        }

        // System.err.println(text.length() + " " + text.substring(0, Math.min(50, text.length())));
        final String textToCheck = textFilter.filter(text.toString());
        // System.out.println(textToCheck);
        if (!textToCheck.contains("#REDIRECT") && !textToCheck.trim().equals("")) {
          // System.err.println("#########################");
          // System.err.println(textToCheck);

          indexer.index(textToCheck, false);
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    text = new StringBuilder();

    inText = false;
  }

  public void characters(char buf[], int offset, int len) {
    final String s = new String(buf, offset, len);
    if (inText) {
      text.append(s);
    }
  }

  public void close() throws Exception {
    indexer.close();
  }

  public static void main(String... strings) throws Exception {
    long start = System.currentTimeMillis();

    final SAXParserFactory factory = SAXParserFactory.newInstance();
    final SAXParser saxParser = factory.newSAXParser();
    WikipediaIndexHandler handler = new WikipediaIndexHandler(FSDirectory.open(new File(
        "E:\\project\\data\\index_en")), Language.ENGLISH, 1, 10000);
    try {
      saxParser.parse(new FileInputStream(new File(
          "E:\\project\\data\\enwiki-20110405-pages-articles1.xml")), handler);
    } catch (RuntimeException e) {
    }
    handler.close();
    long end = System.currentTimeMillis();
    System.out.println("It takes " + (start - end) / (1000 * 60) + " minutes");
  }
}
