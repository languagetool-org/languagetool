/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.openoffice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.languagetool.openoffice.DocumentCursorTools.DocumentText;
import org.languagetool.openoffice.FlatParagraphTools.FlatParagraphContainer;
import org.languagetool.openoffice.OfficeDrawTools.ParagraphContainer;
import org.languagetool.openoffice.OfficeTools.DocumentType;

import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;

/**
 * Class to store the Text of a LO document (document cache)
 * 
 * @since 5.0
 * @author Fred Kruse
 */
public class DocumentCache implements Serializable {

  private static final long serialVersionUID = 3L;

  public final static int CURSOR_TYPE_UNKNOWN = -1;
  public final static int CURSOR_TYPE_ENDNOTE = 0;
  public final static int CURSOR_TYPE_FOOTNOTE = 1;
  public final static int CURSOR_TYPE_HEADER_FOOTER = 2;
  public final static int CURSOR_TYPE_TEXT = 3;
  public final static int CURSOR_TYPE_TABLE = 4;

  public static final int NUMBER_CURSOR_TYPES = 5;

  private static boolean debugMode; // should be false except for testing

  private final List<String> paragraphs = new ArrayList<String>(); // stores the flat paratoTextMappinggraphs of
                                                                   // document

  private final List<List<Integer>> chapterBegins = new ArrayList<List<Integer>>(); // stores the paragraphs formated as
                                                                                    // headings; is used to subdivide
                                                                                    // the document in chapters
  private final List<SerialLocale> locales = new ArrayList<SerialLocale>(); // stores the language of the paragraphs;
  private final List<int[]> footnotes = new ArrayList<int[]>(); // stores the footnotes of the paragraphs;
  private final List<TextParagraph> toTextMapping = new ArrayList<>(); // Mapping from FlatParagraph to DocumentCursor
  private final List<List<Integer>> toParaMapping = new ArrayList<>(); // Mapping from DocumentCursor to FlatParagraph
  private final DocumentType docType;
  private boolean isReset = false;

  DocumentCache(DocumentType docType) {
    debugMode = OfficeTools.DEBUG_MODE_DC;
    this.docType = docType;
  }

  DocumentCache(DocumentCursorTools docCursor, FlatParagraphTools flatPara, Locale docLocale,
      XComponent xComponent, DocumentType docType) {
    debugMode = OfficeTools.DEBUG_MODE_DC;
    this.docType = docType;
    refresh(docCursor, flatPara, docLocale, xComponent, 0);
  }

  DocumentCache(DocumentCache in) {
    debugMode = OfficeTools.DEBUG_MODE_DC;
    add(in);
    docType = in.docType;
  }

  /**
   * set the cache only for test use
   */
  public void setForTest(List<String> paragraphs, List<List<String>> textParagraphs, List<int[]> footnotes,
      List<List<Integer>> chapterBegins, Locale locale) {
    debugMode = OfficeTools.DEBUG_MODE_DC;
    this.paragraphs.addAll(paragraphs);
    this.footnotes.addAll(footnotes);
    this.chapterBegins.addAll(chapterBegins);
    for (int i = 0; i < paragraphs.size(); i++) {
      locales.add(new SerialLocale(locale));
    }
    for (int i = 0; i < NUMBER_CURSOR_TYPES; i++) {
      toParaMapping.add(new ArrayList<Integer>());
    }
    mapParagraphs(this.paragraphs, toTextMapping, toParaMapping, this.chapterBegins, locales, textParagraphs);
  }
  
  /**
   * Refresh the cache
   */
  public void refresh(DocumentCursorTools docCursor, FlatParagraphTools flatPara
      , Locale docLocale, XComponent xComponent, int fromWhere) {
    if (debugMode) {
      MessageHandler.printToLogFile("DocumentCache: refresh: Called from: " + fromWhere);
    }
    if (docType != DocumentType.WRITER) {
      refreshImpressCalcCache(xComponent);
    } else {
      refreshWriterCache(docCursor, flatPara, docLocale, fromWhere);
    }
  }

  /**
   * reset the document cache load the actual state of the document into the cache
   * is only used for writer documents
   */
  private void refreshWriterCache(DocumentCursorTools docCursor, FlatParagraphTools flatPara, Locale docLocale, int fromWhere) {
    try {
      long startTime = System.currentTimeMillis();
      List<String> paragraphs = new ArrayList<String>();
      List<List<Integer>> chapterBegins = new ArrayList<List<Integer>>();
      List<SerialLocale> locales = new ArrayList<SerialLocale>();
      List<int[]> footnotes = new ArrayList<int[]>();
      List<TextParagraph> toTextMapping = new ArrayList<>();
      List<List<Integer>> toParaMapping = new ArrayList<>();
      isReset = true;
      clear();
      for (int i = 0; i < NUMBER_CURSOR_TYPES; i++) {
        toParaMapping.add(new ArrayList<Integer>());
      }
      List<DocumentText> documentTexts = new ArrayList<>();
      for (int i = 0; i < NUMBER_CURSOR_TYPES; i++) {
        documentTexts.add(null);
      }
      documentTexts.set(CURSOR_TYPE_TEXT, docCursor.getAllTextParagraphs());
      documentTexts.set(CURSOR_TYPE_TABLE, docCursor.getTextOfAllTables());
      documentTexts.set(CURSOR_TYPE_FOOTNOTE, docCursor.getTextOfAllFootnotes());
      documentTexts.set(CURSOR_TYPE_ENDNOTE, docCursor.getTextOfAllEndnotes());
      documentTexts.set(CURSOR_TYPE_HEADER_FOOTER, docCursor.getTextOfAllHeadersAndFooters());
      if (debugMode) {
        for (int i = 0; i < NUMBER_CURSOR_TYPES; i++) {
          if (documentTexts.get(i) == null) {
            MessageHandler.printToLogFile("DocumentCache: refresh: CursorType: " + i + "; Document Text is Null!");
          } else {
            MessageHandler.printToLogFile("DocumentCache: refresh: CursorType: " + i + "; Number of paragraphs: "
                + documentTexts.get(i).paragraphs.size());
          }
        }
      }
      FlatParagraphContainer paragraphContainer = null;
      List<List<String>> textParas = new ArrayList<>();
      if (documentTexts.get(CURSOR_TYPE_TEXT) != null) {
        for (DocumentText documentText : documentTexts) {
          textParas.add(documentText.paragraphs);
          chapterBegins.add(documentText.headingNumbers);
        }
        paragraphContainer = flatPara.getAllFlatParagraphs(docLocale);
        if (paragraphContainer == null) {
          MessageHandler.printToLogFile(
              "WARNING: DocumentCache: refresh: paragraphContainer == null - ParagraphCache not initialised");
          isReset = false;
          return;
        }
        if (paragraphContainer.paragraphs == null) {
          MessageHandler
              .printToLogFile("WARNING: DocumentCache: refresh: paragraphs in paragraphContainer == null - ParagraphCache not initialised");
          isReset = false;
          return;
        }
        paragraphs.addAll(paragraphContainer.paragraphs);
        for (Locale locale : paragraphContainer.locales) {
          locales.add(new SerialLocale(locale));
        }
        footnotes.addAll(paragraphContainer.footnotePositions);
      }
      mapParagraphs(paragraphs, toTextMapping, toParaMapping, chapterBegins, locales, textParas);
      actualizeCache (paragraphs, chapterBegins, locales, footnotes, toTextMapping, toParaMapping);
      if (fromWhere != 2) { //  do not write time to log for text level queue
        long endTime = System.currentTimeMillis();
        MessageHandler.printToLogFile("Time to generate cache(" + fromWhere + "): " + (endTime - startTime));
      }
    } finally {
      isReset = false;
    }
  }
  
  /**
   * Actualize cache
   */
  private synchronized void actualizeCache (List<String> paragraphs, List<List<Integer>> chapterBegins, List<SerialLocale> locales, 
      List<int[]> footnotes, List<TextParagraph> toTextMapping, List<List<Integer>> toParaMapping) {
    this.paragraphs.clear();
    this.paragraphs.addAll(paragraphs);
    this.chapterBegins.clear();
    this.chapterBegins.addAll(chapterBegins);
    this.locales.clear();
    this.locales.addAll(locales);
    this.footnotes.clear();
    this.footnotes.addAll(footnotes);
    this.toTextMapping.clear();
    this.toTextMapping.addAll(toTextMapping);
    this.toParaMapping.addAll(toParaMapping);
  }

  /**
   * Map text paragraphs to flat paragraphs is only used for writer documents
   */
  private void mapParagraphs(List<String> paragraphs, List<TextParagraph> toTextMapping, List<List<Integer>> toParaMapping,
        List<List<Integer>> chapterBegins, List<SerialLocale> locales, List<List<String>> textParas) {
    if (textParas != null && !textParas.isEmpty()) {
      List<Integer> nText = new ArrayList<>();
      for (int i = 0; i < textParas.size(); i++) {
        nText.add(0);
      }
      // TODO: Develop a more advanced method of mapping
      int firstText = paragraphs.size() - textParas.get(CURSOR_TYPE_TABLE).size()
          - textParas.get(CURSOR_TYPE_TEXT).size();
      for (int i = 0; i < paragraphs.size(); i++) {
        boolean hasFootnote = footnotes != null && i < footnotes.size() && footnotes.get(i).length > 0;
        boolean isMapped = false;
        for (int n = 0; n < textParas.size(); n++) {
          if (i < firstText && (n > 2 || 
              (toParaMapping.get(CURSOR_TYPE_ENDNOTE).size() == textParas.get(CURSOR_TYPE_ENDNOTE).size()
              && toParaMapping.get(CURSOR_TYPE_FOOTNOTE).size() == textParas.get(CURSOR_TYPE_FOOTNOTE).size()
              && toParaMapping.get(CURSOR_TYPE_HEADER_FOOTER).size() == textParas.get(CURSOR_TYPE_HEADER_FOOTER).size()))) {
            break;
          }
          int j = nText.get(n);
          if (j < textParas.get(n).size()) {
            if (i >= firstText && (nText.get(CURSOR_TYPE_TABLE) == textParas.get(CURSOR_TYPE_TABLE).size() 
                || nText.get(CURSOR_TYPE_TEXT) == textParas.get(CURSOR_TYPE_TEXT).size())) {
              isMapped = true;
            } else {
              String flatPara = hasFootnote ? SingleCheck.removeFootnotes(paragraphs.get(i), footnotes.get(i))
                  : paragraphs.get(i);
              String textPara = hasFootnote ? SingleCheck.removeFootnotes(textParas.get(n).get(j), footnotes.get(i))
                  : textParas.get(n).get(j);
              if (flatPara.equals(textPara) || removeZeroWidthSpace(flatPara).equals(textPara)) {
                isMapped = true;
              }  
            }
            if (isMapped) {
              toTextMapping.add(new TextParagraph(n, j));
              toParaMapping.get(n).add(i);
              nText.set(n, j + 1);
              break;
            }
          }
        }
        if (!isMapped) {
          if (i >= firstText) {
            String flatPara = hasFootnote ? SingleCheck.removeFootnotes(paragraphs.get(i), footnotes.get(i))
                : paragraphs.get(i);
            int j = nText.get(CURSOR_TYPE_TEXT) + 1;
            if (debugMode) {
              MessageHandler.printToLogFile("DocumentCache: mapParagraphs: Not mapped Paragraph(" + i + "): " + flatPara);
              MessageHandler.printToLogFile("DocumentCache: mapParagraphs: firstText: " + firstText + "; j = " + j);
            }
            if (j < textParas.get(CURSOR_TYPE_TEXT).size()) {
              String textPara = hasFootnote ? SingleCheck.removeFootnotes(textParas.get(CURSOR_TYPE_TEXT).get(j), footnotes.get(i))
                  : textParas.get(CURSOR_TYPE_TEXT).get(j);
              if (flatPara.equals(textPara) || removeZeroWidthSpace(flatPara).equals(textPara)) {
                isMapped = true;
              }  
            }
            if (!isMapped) {
              j = nText.get(CURSOR_TYPE_TABLE);
              String textPara = hasFootnote ? SingleCheck.removeFootnotes(textParas.get(CURSOR_TYPE_TABLE).get(j), footnotes.get(i))
                  : textParas.get(CURSOR_TYPE_TABLE).get(j);
              if (flatPara.equals(textPara) || removeZeroWidthSpace(flatPara).equals(textPara)) {
                isMapped = true;
              }  
            }
            if (isMapped) {
              toTextMapping.add(new TextParagraph(CURSOR_TYPE_TEXT, nText.get(CURSOR_TYPE_TEXT)));
              toParaMapping.get(CURSOR_TYPE_TEXT).add(i);
              nText.set(CURSOR_TYPE_TEXT, nText.get(CURSOR_TYPE_TEXT) + 1);
            } else {
              toTextMapping.add(new TextParagraph(CURSOR_TYPE_TABLE, nText.get(CURSOR_TYPE_TABLE)));
              toParaMapping.get(CURSOR_TYPE_TABLE).add(i);
              nText.set(CURSOR_TYPE_TABLE, nText.get(CURSOR_TYPE_TABLE) + 1);
            }
            continue;
          }
          toTextMapping.add(new TextParagraph(CURSOR_TYPE_UNKNOWN, -1));
          if (debugMode || i >= firstText || !paragraphs.get(i).isEmpty()) {
            MessageHandler.printToLogFile(
                "WARNING: DocumentCache: Could not map Paragraph(" + i + "): '" + paragraphs.get(i) + "'");
          }
          if (debugMode) {
            MessageHandler.printToLogFile("DocumentCache: mapParagraphs:");
            for (int k = 0; k < NUMBER_CURSOR_TYPES; k++) {
              MessageHandler.printToLogFile("Actual Cursor Paragraph (Type " + k + "): "
                  + (nText.get(k) < textParas.get(k).size() ? "'" + textParas.get(k).get(nText.get(k)) + "'"
                      : "no paragraph left"));
            }
          }
        }
      }
      prepareChapterBeginsForText(chapterBegins, toTextMapping, locales);
      isReset = false;
      if (debugMode) {
        MessageHandler.printToLogFile("\nDocumentCache: mapParagraphs: toParaMapping:");
        for (int n = 0; n < NUMBER_CURSOR_TYPES; n++) {
          MessageHandler.printToLogFile("Cursor Type: " + n);
          for (int i = 0; i < toParaMapping.get(n).size(); i++) {
            MessageHandler
                .printToLogFile("DocumentCache: mapParagraphs: Doc: " + i + " Flat: " + toParaMapping.get(n).get(i));
          }
        }
        MessageHandler.printToLogFile("\nDocumentCache: mapParagraphs: toTextMapping:");
        for (int i = 0; i < toTextMapping.size(); i++) {
          MessageHandler.printToLogFile("DocumentCache: mapParagraphs: Flat: " + i + " Doc: "
              + toTextMapping.get(i).number + " Type: " + toTextMapping.get(i).type + "; locale: "
              + locales.get(i).Language + "-" + locales.get(i).Country + "; '" + getFlatParagraph(i) + "'");
        }
        MessageHandler.printToLogFile("\nDocumentCache: mapParagraphs: headings:");
        for (int n = 0; n < NUMBER_CURSOR_TYPES; n++) {
          MessageHandler.printToLogFile("\nDocumentCache: mapParagraphs: Cursor Type: " + n);
          for (int i = 0; i < chapterBegins.get(n).size(); i++) {
            MessageHandler
                .printToLogFile("DocumentCache: mapParagraphs: Num: " + i + " Heading: " + chapterBegins.get(n).get(i));
          }
        }
        MessageHandler.printToLogFile("DocumentCache: mapParagraphs: Number of Flat Paragraphs: " + paragraphs.size());
        MessageHandler.printToLogFile(
            "DocumentCache: mapParagraphs: Number of Text Paragraphs: " + toParaMapping.get(CURSOR_TYPE_TEXT).size());
        MessageHandler.printToLogFile("DocumentCache: mapParagraphs: Number of footnotes: " + footnotes.size());
        MessageHandler.printToLogFile("DocumentCache: mapParagraphs: Number of locales: " + locales.size());
      }
    }
  }

  /**
   * reset the document cache for impress documents
   */
  private synchronized void refreshImpressCalcCache(XComponent xComponent) {
    ParagraphContainer container;
    if (docType == DocumentType.IMPRESS) {
      container = OfficeDrawTools.getAllParagraphs(xComponent);
    } else if (docType == DocumentType.CALC) {
      container = OfficeSpreadsheetTools.getAllParagraphs(xComponent);
    } else {
      return;
    }
    clear();
    paragraphs.addAll(container.paragraphs);
    for (int i = 0; i < NUMBER_CURSOR_TYPES - 1; i++) {
      chapterBegins.add(new ArrayList<Integer>());
    }
    chapterBegins.get(CURSOR_TYPE_TEXT).addAll(container.pageBegins);
    for (Locale locale : container.locales) {
      locales.add(new SerialLocale(locale));
    }
    for (int i = 0; i < NUMBER_CURSOR_TYPES; i++) {
      toParaMapping.add(new ArrayList<Integer>());
    }
    for (int i = 0; i < paragraphs.size(); i++) {
      toTextMapping.add(new TextParagraph(CURSOR_TYPE_TEXT, i));
      toParaMapping.get(CURSOR_TYPE_TEXT).add(i);
      footnotes.add(new int[0]);
    }
    if (debugMode) {
      MessageHandler.printToLogFile("DocumentCache: reset: isImpress: Number of paragraphse: " + paragraphs.size());
      for (int i = 0; i < NUMBER_CURSOR_TYPES; i++) {
        MessageHandler.printToLogFile("DocumentCache: reset: CursorType: " + i + "; Number of paragraphs: " + toParaMapping.get(i).size());
      }
    }
  }

  /**
   * wait till reset is finished
   */
  public boolean isFinished() {
    while (isReset) {
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        MessageHandler.showError(e);
        return false;
      }
    }
    return paragraphs == null ? false : true;
  }

  /**
   * get Flat Paragraph by Index
   */
  public synchronized String getFlatParagraph(int n) {
    return paragraphs.get(n);
  }

  /**
   * set Flat Paragraph at Index
   */
  public synchronized void setFlatParagraph(int n, String sPara) {
    paragraphs.set(n, sPara);
  }

  /**
   * set Flat Paragraph and Locale at Index
   */
  public synchronized void setFlatParagraph(int n, String sPara, Locale locale) {
    paragraphs.set(n, sPara);
    locales.set(n, new SerialLocale(locale));
  }

  /**
   * is multilingual Flat Paragraph
   */
  public synchronized boolean isMultilingualFlatParagraph(int n) {
    return locales.get(n).Variant.startsWith(OfficeTools.MULTILINGUAL_LABEL);
  }

  /**
   * set multilingual flag to Flat Paragraph
   */
  public synchronized void setMultilingualFlatParagraph(int n) {
    SerialLocale locale = locales.get(n);
    if (!locale.Variant.startsWith(OfficeTools.MULTILINGUAL_LABEL)) {
      locale.Variant = OfficeTools.MULTILINGUAL_LABEL + locale.Variant;
      locales.set(n, locale);
    }
  }

  /**
   * get Locale of Flat Paragraph by Index
   */
  public synchronized Locale getFlatParagraphLocale(int n) {
    return locales.get(n).toLocaleWithoutLabel();
  }

  /**
   * set Locale of Flat Paragraph by Index
   */
  public synchronized void setFlatParagraphLocale(int n, Locale locale) {
    locales.set(n, new SerialLocale(locale));
  }

  /**
   * get footnotes of Flat Paragraph by Index
   */
  public synchronized int[] getFlatParagraphFootnotes(int n) {
    return footnotes.get(n);
  }

  /**
   * set footnotes of Flat Paragraph by Index
   */
  public synchronized void setFlatParagraphFootnotes(int n, int[] footnotePos) {
    footnotes.set(n, footnotePos);
  }
  
  /**
   * clear document cache
   */
  public synchronized void clear() {
    paragraphs.clear();
    chapterBegins.clear();
    locales.clear();
    footnotes.clear();
    toTextMapping.clear();
    toParaMapping.clear();
  }
  
  /**
   * Add a document Cache
   */
  private synchronized void add(DocumentCache in) {
    paragraphs.addAll(in.paragraphs);
    chapterBegins.addAll(in.chapterBegins);
    locales.addAll(in.locales);
    footnotes.addAll(in.footnotes);
    toTextMapping.addAll(in.toTextMapping);
    toParaMapping.addAll(in.toParaMapping);
  }
  
  /**
   * Replace a document Cache
   */
  public synchronized void put(DocumentCache in) {
    clear();
    add(in);
  }

  /**
   * is DocumentCache empty
   */
  public synchronized boolean isEmpty() {
    return paragraphs == null || paragraphs.isEmpty();
  }

  /**
   * has no content
   */
  public synchronized boolean hasNoContent() {
    return paragraphs == null || paragraphs.isEmpty() || (paragraphs.size() == 1 && paragraphs.get(0).isEmpty());
  }

  /**
   * size of document cache (number of all flat paragraphs)
   */
  public synchronized int size() {
    return paragraphs == null ? 0 : paragraphs.size();
  }

  /**
   * get the Content of a Text Paragraph
   */
  public synchronized String getTextParagraph(TextParagraph textParagraph) {
    if (textParagraph.type == CURSOR_TYPE_UNKNOWN) {
      return null;
    }
    return paragraphs.get(toParaMapping.get(textParagraph.type).get(textParagraph.number));
  }

  /**
   * get Number of Flat Paragraph from Number of Text Paragraph
   */
  public synchronized int getFlatParagraphNumber(TextParagraph textParagraph) {
    if (textParagraph.type == CURSOR_TYPE_UNKNOWN) {
      return -1;
    }
    return toParaMapping.get(textParagraph.type).get(textParagraph.number);
  }

  /**
   * get Locale of Text Paragraph by Index
   */
  public synchronized Locale getTextParagraphLocale(TextParagraph textParagraph) {
    if (textParagraph.type == CURSOR_TYPE_UNKNOWN) {
      return null;
    }
    return locales.get(toParaMapping.get(textParagraph.type).get(textParagraph.number)).toLocaleWithoutLabel();
  }

  /**
   * get footnotes of Text Paragraph by Index
   */
  public synchronized int[] getTextParagraphFootnotes(TextParagraph textParagraph) {
    if (textParagraph.type == CURSOR_TYPE_UNKNOWN) {
      return new int[0];
    }
    return footnotes.get(toParaMapping.get(textParagraph.type).get(textParagraph.number));
  }

  /**
   * set footnotes of Text Paragraph
   */
  public synchronized void setTextParagraphFootnotes(TextParagraph textParagraph, int[] footnotePos) {
    if (textParagraph.type != CURSOR_TYPE_UNKNOWN) {
      footnotes.set(toParaMapping.get(textParagraph.type).get(textParagraph.number), footnotePos);
    }
  }

  /**
   * get Number of Text Paragraph from Number of Flat Paragraph
   */
  public synchronized TextParagraph getNumberOfTextParagraph(int numberOfFlatParagraph) {
    return toTextMapping.get(numberOfFlatParagraph);
  }

  /**
   * size of text cache (without headers, footnotes, etc.)
   */
  public synchronized int textSize(TextParagraph textParagraph) {
    if (textParagraph.type == CURSOR_TYPE_UNKNOWN) {
      return 0;
    }
    return toParaMapping.get(textParagraph.type).size();
  }

  /**
   * size of text cache (without headers, footnotes, etc.)
   */
  public synchronized boolean isEqual(int n, String text, Locale locale) {
    return ((n < 0 || n >= locales.size() || locales.get(n) == null) ? false
        : ((isMultilingualFlatParagraph(n) || locales.get(n).equalsLocale(locale)) && text.equals(paragraphs.get(n))));
  }

  /**
   * Gives back the start paragraph for text level check
   */
  public synchronized int getStartOfParaCheck(TextParagraph textParagraph, int parasToCheck, boolean checkOnlyParagraph,
      boolean useQueue, boolean addParas) {
    if (textParagraph.number < 0 || toParaMapping.get(textParagraph.type).size() <= textParagraph.number) {
      return -1;
    }
    if (parasToCheck < -1) { // check all flat paragraphs
      return 0;
    }
    if (parasToCheck == 0) {
      return textParagraph.number;
    }
    int headingBefore = 0;
    for (int heading : chapterBegins.get(textParagraph.type)) {
      if (heading > textParagraph.number) {
        break;
      }
      headingBefore = heading;
    }
    if (headingBefore == textParagraph.number || parasToCheck < 0 || (useQueue && !checkOnlyParagraph)) {
      return headingBefore;
    }
    int startPos = textParagraph.number - parasToCheck;
    if (addParas) {
      startPos -= parasToCheck;
    }
    if (startPos < headingBefore) {
      startPos = headingBefore;
    }
    return startPos;
  }

  /**
   * Gives back the end paragraph for text level check
   */
  public synchronized int getEndOfParaCheck(TextParagraph textParagraph, int parasToCheck, boolean checkOnlyParagraph,
      boolean useQueue, boolean addParas) {
    if (textParagraph.number < 0 || toParaMapping.get(textParagraph.type).size() <= textParagraph.number) {
      return -1;
    }
    if (parasToCheck < -1) { // check all flat paragraphs
      return size();
    }
    int headingAfter = -1;
    if (parasToCheck == 0) {
      return textParagraph.number + 1;
    }
    for (int heading : chapterBegins.get(textParagraph.type)) {
      headingAfter = heading;
      if (heading > textParagraph.number) {
        break;
      }
    }
    if (headingAfter <= textParagraph.number || headingAfter > toParaMapping.get(textParagraph.type).size()) {
      headingAfter = toParaMapping.get(textParagraph.type).size();
    }
    if (parasToCheck < 0 || (useQueue && !checkOnlyParagraph)) {
      return headingAfter;
    }
    int endPos = textParagraph.number + 1 + parasToCheck;
    if (!checkOnlyParagraph) {
      endPos += parasToCheck * OfficeTools.CHECK_MULTIPLIKATOR;
    }
    if (addParas) {
      endPos += parasToCheck;
    }
    if (endPos > headingAfter) {
      endPos = headingAfter;
    }
    return endPos;
  }

  /**
   * Gives Back the full Text as String
   */
  public synchronized String getDocAsString(TextParagraph textParagraph, int parasToCheck, boolean checkOnlyParagraph,
      boolean useQueue, boolean hasFootnotes) {
    int startPos = getStartOfParaCheck(textParagraph, parasToCheck, checkOnlyParagraph, useQueue, true);
    int endPos = getEndOfParaCheck(textParagraph, parasToCheck, checkOnlyParagraph, useQueue, true);
    StringBuilder docText;
    if (parasToCheck < -1) { // check all flat paragraphs
      if (startPos < 0 || endPos < 0
          || (hasFootnotes && getFlatParagraph(startPos).isEmpty() && getFlatParagraphFootnotes(startPos).length > 0)) {
        return "";
      }
      docText = new StringBuilder(fixLinebreak(SingleCheck.removeFootnotes(getFlatParagraph(startPos),
          (hasFootnotes ? getFlatParagraphFootnotes(startPos) : null))));
      for (int i = startPos + 1; i < endPos; i++) {
        docText.append(OfficeTools.END_OF_PARAGRAPH).append(fixLinebreak(
            SingleCheck.removeFootnotes(getFlatParagraph(i), (hasFootnotes ? getFlatParagraphFootnotes(i) : null))));
      }
    } else {
      TextParagraph startPara = new TextParagraph(textParagraph.type, startPos);
      if (startPos < 0 || endPos < 0 || (hasFootnotes && getTextParagraph(startPara).isEmpty()
          && getTextParagraphFootnotes(startPara).length > 0)) {
        return "";
      }
      docText = new StringBuilder(fixLinebreak(SingleCheck.removeFootnotes(getTextParagraph(startPara),
          (hasFootnotes ? getTextParagraphFootnotes(startPara) : null))));
      for (int i = startPos + 1; i < endPos; i++) {
        TextParagraph tPara = new TextParagraph(textParagraph.type, i);
        docText.append(OfficeTools.END_OF_PARAGRAPH).append(fixLinebreak(SingleCheck
            .removeFootnotes(getTextParagraph(tPara), (hasFootnotes ? getTextParagraphFootnotes(tPara) : null))));
      }
    }
    return docText.toString();
  }

  /**
   * Change manual linebreak to distinguish from end of paragraph
   */
  public static String fixLinebreak(String text) {
    return text.replaceAll(OfficeTools.SINGLE_END_OF_PARAGRAPH, OfficeTools.MANUAL_LINEBREAK);
  }

  /**
   * Change manual linebreak to distinguish from end of paragraph
   */
  public static String removeZeroWidthSpace(String text) {
    return text.replaceAll(OfficeTools.ZERO_WIDTH_SPACE, "");
  }

  /**
   * Gives Back the StartPosition of Paragraph
   */
  public synchronized int getStartOfParagraph(int nPara, TextParagraph textParagraph, int parasToCheck, boolean checkOnlyParagraph,
      boolean useQueue, boolean hasFootnotes) {
    if (nPara < 0 || (parasToCheck > -2 && toParaMapping.get(textParagraph.type).size() <= nPara)
        || (parasToCheck < -1 && paragraphs.size() <= nPara)) {
      return -1;
    }
    int startPos = getStartOfParaCheck(textParagraph, parasToCheck, checkOnlyParagraph, useQueue, true);
    if (startPos < 0) {
      return -1;
    }
    int pos = 0;
    if (parasToCheck < -1) {
      for (int i = startPos; i < nPara; i++) {
        pos += SingleCheck.removeFootnotes(getFlatParagraph(i), 
                (hasFootnotes ? getFlatParagraphFootnotes(i) : null)).length() + OfficeTools.NUMBER_PARAGRAPH_CHARS;
      }
    } else {
      for (int i = startPos; i < nPara; i++) {
        TextParagraph tPara = new TextParagraph(textParagraph.type, i);
        pos += SingleCheck.removeFootnotes(getTextParagraph(tPara), 
                (hasFootnotes ? getTextParagraphFootnotes(tPara) : null)).length() + OfficeTools.NUMBER_PARAGRAPH_CHARS;
      }
    }
    return pos;
  }

  /**
   * For cursor type text: Add the next chapter begin after Heading and changes of
   * language to the chapter begins
   */
  private void prepareChapterBeginsForText(List<List<Integer>> chapterBegins, List<TextParagraph> toTextMapping, List<SerialLocale> locales) {
    List<Integer> prepChBegins = new ArrayList<Integer>(chapterBegins.get(CURSOR_TYPE_TEXT));
    for (int begin : chapterBegins.get(CURSOR_TYPE_TEXT)) {
      if (!prepChBegins.contains(begin + 1)) {
        prepChBegins.add(begin + 1);
      }
    }
    if (locales.size() > 0) {
      SerialLocale lastLocale = locales.get(0);
      for (int i = 1; i < locales.size(); i++) {
        if (locales != null && !locales.get(i).equalsLocale(lastLocale)) {
          TextParagraph nText = toTextMapping.get(i);
          if (nText.type == CURSOR_TYPE_TEXT && nText.number >= 0) {
            if (!prepChBegins.contains(nText.number)) {
              prepChBegins.add(nText.number);
            }
            lastLocale = locales.get(i);
            if (debugMode) {
              MessageHandler.printToLogFile(
                  "DocumentCache: prepareChapterBeginsForText: Paragraph(" + i + "): Locale changed to: "
                      + lastLocale.Language + (lastLocale.Country == null ? "" : ("-" + lastLocale.Country)));
            }
          }
        }
      }
    }
    prepChBegins.sort(null);
    chapterBegins.set(CURSOR_TYPE_TEXT, prepChBegins);
  }

  public TextParagraph createTextParagraph(int type, int paragraph) {
    return new TextParagraph(type, paragraph);
  }

  static class TextParagraph implements Serializable {
    private static final long serialVersionUID = 1L;
    int type;
    int number;

    TextParagraph(int type, int number) {
      this.type = type;
      this.number = number;
    }
  }

  /**
   * Class of serializable locale needed to save cache
   */
  class SerialLocale implements Serializable {

    private static final long serialVersionUID = 1L;
    String Country;
    String Language;
    String Variant;

    SerialLocale(Locale locale) {
      this.Country = locale.Country;
      this.Language = locale.Language;
      this.Variant = locale.Variant;
    }

    /**
     * return the language as Locale
     */
    Locale toLocale() {
      return new Locale(Language, Country, Variant);
    }

    /**
     * return the language as Locale without multilingual label
     */
    Locale toLocaleWithoutLabel() {
      if (Variant.startsWith(OfficeTools.MULTILINGUAL_LABEL)) {
        return new Locale(Language, Country, Variant.substring(OfficeTools.MULTILINGUAL_LABEL.length()));
      }
      return new Locale(Language, Country, Variant);
    }

    /**
     * True if the Language is the same as Locale
     */
    boolean equalsLocale(Locale locale) {
      return ((locale == null || Language == null || Country == null || Variant == null) ? false
          : Language.equals(locale.Language) && Country.equals(locale.Country) && Variant.equals(locale.Variant));
    }

    boolean equalsLocale(SerialLocale locale) {
      return ((locale == null || Language == null || Country == null || Variant == null) ? false
          : Language.equals(locale.Language) && Country.equals(locale.Country) && Variant.equals(locale.Variant));
    }

  }

}
