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

  private static final long serialVersionUID = 7L;

  public final static int CURSOR_TYPE_UNKNOWN = -1;
  public final static int CURSOR_TYPE_ENDNOTE = 0;
  public final static int CURSOR_TYPE_FOOTNOTE = 1;
  public final static int CURSOR_TYPE_HEADER_FOOTER = 2;
  public final static int CURSOR_TYPE_FRAME = 3;
  public final static int CURSOR_TYPE_TEXT = 4;
  public final static int CURSOR_TYPE_TABLE = 5;

  public static final int NUMBER_CURSOR_TYPES = 6;

  private static boolean debugMode;     // should be false except for testing
  private static boolean debugModeTm;   // time measurement should be false except for testing

  private final List<String> paragraphs = new ArrayList<String>(); // stores the flat paratoTextMappinggraphs of
                                                                   // document

  private final List<List<Integer>> chapterBegins = new ArrayList<List<Integer>>(); // stores the paragraphs formated as
                                                                                    // headings; is used to subdivide
                                                                                    // the document in chapters
  private final List<Integer> automaticParagraphs = new ArrayList<Integer>(); // stores the paragraphs automatic generated (will not be checked)
  private final List<SerialLocale> locales = new ArrayList<SerialLocale>(); // stores the language of the paragraphs;
  private final List<int[]> footnotes = new ArrayList<int[]>();             // stores the footnotes of the paragraphs;
  private final List<List<Integer>> deletedCharacters = new ArrayList<List<Integer>>(); // stores the deleted characters (report changes) of the paragraphs;
  private final List<TextParagraph> toTextMapping = new ArrayList<>(); // Mapping from FlatParagraph to DocumentCursor
  private final List<List<Integer>> toParaMapping = new ArrayList<>(); // Mapping from DocumentCursor to FlatParagraph
  private final DocumentType docType;
  private boolean isReset = false;
  private int nEndnote = 0;
  private int nFootnote = 0;
  private int nHeaderFooter = 0;
  private int nFrame = 0;
  private int nText = 0;
  private int nTable = 0;
  
  private int nWriteAccess = 0;

  DocumentCache(DocumentType docType) {
    debugMode = OfficeTools.DEBUG_MODE_DC;
    debugModeTm = OfficeTools.DEBUG_MODE_TM;
    this.docType = docType;
  }

  DocumentCache(DocumentCursorTools docCursor, FlatParagraphTools flatPara, Locale fixedLocale, Locale docLocale,
      XComponent xComponent, DocumentType docType) {
    debugMode = OfficeTools.DEBUG_MODE_DC;
    debugModeTm = OfficeTools.DEBUG_MODE_TM;
    this.docType = docType;
    refresh(docCursor, flatPara, fixedLocale, docLocale, xComponent, 0);
  }

  DocumentCache(DocumentCache in) {
    nWriteAccess++;
    waitForFinish(nWriteAccess - 1);
    isReset = true;
    debugMode = OfficeTools.DEBUG_MODE_DC;
    debugModeTm = OfficeTools.DEBUG_MODE_TM;
    if (in.size() > 0) {
      add(in);
    }
    docType = in.docType;
    isReset = false;
    nWriteAccess--;
  }

  /**
   * set the cache only for test use
   */
  public void setForTest(List<String> paragraphs, List<List<String>> textParagraphs, List<int[]> footnotes,
      List<List<Integer>> chapterBegins, Locale locale) {
    isReset = true;
    debugMode = OfficeTools.DEBUG_MODE_DC;
    debugModeTm = OfficeTools.DEBUG_MODE_TM;
    this.paragraphs.addAll(paragraphs);
    this.footnotes.addAll(footnotes);
    this.chapterBegins.addAll(chapterBegins);
    for (int i = 0; i < paragraphs.size(); i++) {
      locales.add(new SerialLocale(locale));
    }
    for (int i = 0; i < NUMBER_CURSOR_TYPES; i++) {
      toParaMapping.add(new ArrayList<Integer>());
    }
    nText = textParagraphs.get(CURSOR_TYPE_TEXT).size();
    nTable = textParagraphs.get(CURSOR_TYPE_TABLE).size();
    nFrame = textParagraphs.get(CURSOR_TYPE_FRAME).size();
    nFootnote = textParagraphs.get(CURSOR_TYPE_FOOTNOTE).size();
    nEndnote = textParagraphs.get(CURSOR_TYPE_ENDNOTE).size();
    nHeaderFooter = textParagraphs.get(CURSOR_TYPE_HEADER_FOOTER).size();
    mapParagraphs(this.paragraphs, toTextMapping, toParaMapping, this.chapterBegins, locales, footnotes, textParagraphs, deletedCharacters, null);
    isReset = false;
  }
  
  /**
   * Refresh the cache
   */
  public void refresh(DocumentCursorTools docCursor, FlatParagraphTools flatPara,
      Locale fixedLocale, Locale docLocale, XComponent xComponent, int fromWhere) {
    nWriteAccess++;
    waitForFinish(nWriteAccess - 1);
    if (isReset) {
      return;
    }
    isReset = true;
    if (debugMode) {
      MessageHandler.printToLogFile("DocumentCache: refresh: Called from: " + fromWhere);
    }
    if (docType != DocumentType.WRITER) {
      refreshImpressCalcCache(xComponent);
    } else {
      refreshWriterCache(docCursor, flatPara, fixedLocale, docLocale, fromWhere);
    }
    isReset = false;
    nWriteAccess--;
  }

  /**
   * reset the document cache load the actual state of the document into the cache
   * is only used for writer documents
   */
  private void refreshWriterCache(DocumentCursorTools docCursor, FlatParagraphTools flatPara, 
      Locale fixedLocale, Locale docLocale, int fromWhere) {
    try {
      long startTime = System.currentTimeMillis();
      List<String> paragraphs = new ArrayList<String>();
      List<List<Integer>> chapterBegins = new ArrayList<List<Integer>>();
      List<List<Integer>> deletedCharacters = new ArrayList<List<Integer>>();
      List<SerialLocale> locales = new ArrayList<SerialLocale>();
      List<int[]> footnotes = new ArrayList<int[]>();
      List<TextParagraph> toTextMapping = new ArrayList<>();
      List<List<Integer>> toParaMapping = new ArrayList<>();
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
      documentTexts.set(CURSOR_TYPE_FRAME, docCursor.getTextOfAllFrames());
      documentTexts.set(CURSOR_TYPE_FOOTNOTE, docCursor.getTextOfAllFootnotes());
      documentTexts.set(CURSOR_TYPE_ENDNOTE, docCursor.getTextOfAllEndnotes());
      documentTexts.set(CURSOR_TYPE_HEADER_FOOTER, docCursor.getTextOfAllHeadersAndFooters());
      for (int i = 0; i < NUMBER_CURSOR_TYPES; i++) {
        if(documentTexts.get(i) == null) {
          documentTexts.set(i, docCursor.new DocumentText());
        }
      }
      nText = documentTexts.get(CURSOR_TYPE_TEXT).paragraphs.size();
      nTable = documentTexts.get(CURSOR_TYPE_TABLE).paragraphs.size();
      nFrame = documentTexts.get(CURSOR_TYPE_FRAME).paragraphs.size();
      nFootnote = documentTexts.get(CURSOR_TYPE_FOOTNOTE).paragraphs.size();
      nEndnote = documentTexts.get(CURSOR_TYPE_ENDNOTE).paragraphs.size();
      nHeaderFooter = documentTexts.get(CURSOR_TYPE_HEADER_FOOTER).paragraphs.size();
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
      List<List<List<Integer>>> deletedChars = new ArrayList<>();
      if (documentTexts.get(CURSOR_TYPE_TEXT) != null) {
        for (DocumentText documentText : documentTexts) {
          textParas.add(documentText.paragraphs);
          chapterBegins.add(documentText.headingNumbers);
          deletedChars.add(documentText.deletedCharacters);
        }
        paragraphContainer = flatPara.getAllFlatParagraphs(fixedLocale);
        if (paragraphContainer == null) {
          MessageHandler.printToLogFile(
              "WARNING: DocumentCache: refresh: paragraphContainer == null - ParagraphCache not initialised");
          return;
        }
        if (paragraphContainer.paragraphs == null) {
          MessageHandler
              .printToLogFile("WARNING: DocumentCache: refresh: paragraphs in paragraphContainer == null - ParagraphCache not initialised");
          return;
        }
        paragraphs.addAll(paragraphContainer.paragraphs);
        for (Locale locale : paragraphContainer.locales) {
          locales.add(new SerialLocale(locale));
        }
        footnotes.addAll(paragraphContainer.footnotePositions);
      }
      mapParagraphs(paragraphs, toTextMapping, toParaMapping, chapterBegins, locales, footnotes, textParas, deletedCharacters, deletedChars);
      actualizeCache (paragraphs, chapterBegins, locales, footnotes, toTextMapping, toParaMapping, 
          deletedCharacters, documentTexts.get(CURSOR_TYPE_TEXT).automaticTextParagraphs);
      if (fromWhere != 2 || debugModeTm) { //  do not write time to log for text level queue
        long endTime = System.currentTimeMillis();
        MessageHandler.printToLogFile("Time to generate cache(" + fromWhere + "): " + (endTime - startTime));
      }
    } finally {
    }
  }
  
  /**
   * Actualize cache
   */
  private void actualizeCache (List<String> paragraphs, List<List<Integer>> chapterBegins, List<SerialLocale> locales, 
      List<int[]> footnotes, List<TextParagraph> toTextMapping, List<List<Integer>> toParaMapping, 
      List<List<Integer>> deletedCharacters, List<Integer> automaticParagraphs) {
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
    this.deletedCharacters.clear();
    this.deletedCharacters.addAll(deletedCharacters);
    this.automaticParagraphs.addAll(automaticParagraphs);
  }
  
  public static boolean isEqualText(String flatPara, String textPara) {
    flatPara = removeZeroWidthSpace(flatPara);
    textPara = removeZeroWidthSpace(textPara);
    if (flatPara.isEmpty() && textPara.isEmpty()) {
      return true;
    }
    return flatPara.equals(textPara);
  }

  /**
   * Map text paragraphs to flat paragraphs is only used for writer documents
   */
  private void mapParagraphs(List<String> paragraphs, List<TextParagraph> toTextMapping, List<List<Integer>> toParaMapping,
        List<List<Integer>> chapterBegins, List<SerialLocale> locales, List<int[]> footnotes, List<List<String>> textParas, 
        List<List<Integer>> deletedCharacters, List<List<List<Integer>>> deletedChars) {
    if (textParas != null && !textParas.isEmpty()) {
      List<Integer> nTables = new ArrayList<>();
      List<Integer> nText = new ArrayList<>();
      for (int i = 0; i < textParas.size(); i++) {
        nText.add(0);
      }
      int nUnknown = 0;
      for (int i = 0; i < NUMBER_CURSOR_TYPES; i++) {
        nUnknown += textParas.get(i).size();
      }
      nUnknown = paragraphs.size() - nUnknown;  // nUnknown: number of headings of graphic elements
      int nFrameTable = 0;  // Number of table paragraphs in Frames
      if (debugMode) {
        MessageHandler.printToLogFile("DocumentCache: mapParagraphs: Unknown paragraphs: " + nUnknown);
      }
      for (int i = 0; i < textParas.get(CURSOR_TYPE_TABLE).size(); i++) {
        toParaMapping.get(CURSOR_TYPE_TABLE).add(-1);
      }
      int numUnknown = 0;
      boolean thirdTextDone = false;
      for (int i = 0; i < paragraphs.size(); i++) {
        boolean hasFootnote = footnotes != null && i < footnotes.size() && footnotes.get(i).length > 0;
        boolean isMapped = false;
        boolean firstTextDone = toParaMapping.get(CURSOR_TYPE_ENDNOTE).size() == textParas.get(CURSOR_TYPE_ENDNOTE).size()
            && toParaMapping.get(CURSOR_TYPE_FOOTNOTE).size() == textParas.get(CURSOR_TYPE_FOOTNOTE).size();
        boolean secondTextDone = firstTextDone && numUnknown >= nUnknown 
            && toParaMapping.get(CURSOR_TYPE_FRAME).size() == textParas.get(CURSOR_TYPE_FRAME).size()
            && toParaMapping.get(CURSOR_TYPE_HEADER_FOOTER).size() == textParas.get(CURSOR_TYPE_HEADER_FOOTER).size();
        //  test for footnote, endnote, frame or header/footer
        //  listed before text or embedded tables
        if (!secondTextDone) {
          for (int n = 0; n < NUMBER_CURSOR_TYPES - 2; n++) {
            if (n > 2 && !firstTextDone) {
              break;
            }
            int j = nText.get(n);
            if (j < textParas.get(n).size()) {
              if (isEqualText(paragraphs.get(i), textParas.get(n).get(j))) {
                isMapped = true;
                toTextMapping.add(new TextParagraph(n, j));
                toParaMapping.get(n).add(i);
                nText.set(n, j + 1);
                break;
              }
            }
          }
          if (isMapped) {
            continue;
          }
        }
        //  test for movable tables
        //  listed before text or embedded tables
        if ((!secondTextDone || !thirdTextDone) && nTables.size() < textParas.get(CURSOR_TYPE_TABLE).size()) {
          String flatPara = hasFootnote ? SingleCheck.removeFootnotes(paragraphs.get(i), footnotes.get(i), null)
              : paragraphs.get(i);
          for (int k = nFrameTable; k < textParas.get(CURSOR_TYPE_TABLE).size(); k++) {
            if (!nTables.contains(k)) {
              String textPara = hasFootnote ? SingleCheck.removeFootnotes(textParas.get(CURSOR_TYPE_TABLE).get(k), footnotes.get(i), null)
                  : textParas.get(CURSOR_TYPE_TABLE).get(k);
              if (isEqualText(flatPara, textPara)) {
                toTextMapping.add(new TextParagraph(CURSOR_TYPE_TABLE, k));
                toParaMapping.get(CURSOR_TYPE_TABLE).set(k, i);
                nTables.add(k);
                isMapped = true;
                nFrameTable = k + 1;
                break;
              }  
            }
          }
          if (isMapped) {
            continue;
          } else if (secondTextDone) {
            nFrameTable = 0;
            thirdTextDone = true;
          }
        }
        //  there are unknown paragraphs before text paragraphs
        if (!secondTextDone) {
          toTextMapping.add(new TextParagraph(CURSOR_TYPE_UNKNOWN, -1));
          numUnknown++;
          if (debugMode || firstTextDone || !paragraphs.get(i).isEmpty()) {
            MessageHandler.printToLogFile(
                "WARNING: DocumentCache: Could not map Paragraph(" + i + "): '" + paragraphs.get(i) + "'; secondTextDone: " + secondTextDone);
          }
          if (debugMode) {
            MessageHandler.printToLogFile("DocumentCache: mapParagraphs:");
            for (int k = 0; k < NUMBER_CURSOR_TYPES; k++) {
              MessageHandler.printToLogFile("Actual Cursor Paragraph (Type " + k + "): "
                  + (nText.get(k) < textParas.get(k).size() ? "'" 
                  + (k == CURSOR_TYPE_TABLE ? textParas.get(k).get(nFrameTable) : textParas.get(k).get(nText.get(k))) + "'"
                      : "no paragraph left"));
            }
            MessageHandler.printToLogFile("Unknown Paragraphs: " + (numUnknown - 1) + " from " + nUnknown);
          }
          continue;
        }
        int j = nText.get(CURSOR_TYPE_TEXT);
        if (nTables.size() == textParas.get(CURSOR_TYPE_TABLE).size()) {
          //  no tables left / text assumed
          toTextMapping.add(new TextParagraph(CURSOR_TYPE_TEXT, j));
          toParaMapping.get(CURSOR_TYPE_TEXT).add(i);
          nText.set(CURSOR_TYPE_TEXT, j + 1);
          continue;
        } else if (j == textParas.get(CURSOR_TYPE_TEXT).size()) {
          //  no text left / table assumed
          int k = nFrameTable;
          String textPara = null;
          for (; k < textParas.get(CURSOR_TYPE_TABLE).size(); k++) {
            if (!nTables.contains(k)) {
              textPara = hasFootnote ? SingleCheck.removeFootnotes(textParas.get(CURSOR_TYPE_TABLE).get(k), footnotes.get(i), null)
              : textParas.get(CURSOR_TYPE_TABLE).get(k);
              break;
            }
          }
          nTables.add(k);
          nFrameTable = k + 1;
          toTextMapping.add(new TextParagraph(CURSOR_TYPE_TABLE, k));
          toParaMapping.get(CURSOR_TYPE_TABLE).set(k, i);
          continue;
        } else {
          //  test for table
          String flatPara = hasFootnote ? SingleCheck.removeFootnotes(paragraphs.get(i), footnotes.get(i), null)
                      : paragraphs.get(i);
          String textPara = null;
          int k = nFrameTable;
          for (; k < textParas.get(CURSOR_TYPE_TABLE).size(); k++) {
            if (!nTables.contains(k)) {
              textPara = hasFootnote ? SingleCheck.removeFootnotes(textParas.get(CURSOR_TYPE_TABLE).get(k), footnotes.get(i), null)
              : textParas.get(CURSOR_TYPE_TABLE).get(k);
              break;
            }
          }
          if (isEqualText(flatPara, textPara)) {
            textPara = hasFootnote ? SingleCheck.removeFootnotes(textParas.get(CURSOR_TYPE_TEXT).get(j), footnotes.get(i), null)
                : textParas.get(CURSOR_TYPE_TEXT).get(j);
            boolean equalTable = true;
            boolean equalText = isEqualText(flatPara, textPara);
            //  test if table and text are equal
            int mk = k;
            int mj = j;
            int mi = i;
            while (equalTable && equalText && mi < paragraphs.size() - 1) {
              mi++;
              boolean hasFn = footnotes != null && i < footnotes.size() && footnotes.get(mi).length > 0;
              String flatP = hasFn ? SingleCheck.removeFootnotes(paragraphs.get(mi), footnotes.get(mi), null)
                  : paragraphs.get(mi);

              mk++;
              if (mk < textParas.get(CURSOR_TYPE_TABLE).size() && !nTables.contains(mk)) {
                textPara = hasFn ? SingleCheck.removeFootnotes(textParas.get(CURSOR_TYPE_TABLE).get(mk), footnotes.get(mi), null)
                    : textParas.get(CURSOR_TYPE_TABLE).get(mk);
                equalTable = isEqualText(flatP, textPara);
              } else {
                equalTable = false;
              }
              mj++;
              if (mj < textParas.get(CURSOR_TYPE_TEXT).size()) {
                textPara = hasFootnote ? SingleCheck.removeFootnotes(textParas.get(CURSOR_TYPE_TEXT).get(mj), footnotes.get(mi), null)
                    : textParas.get(CURSOR_TYPE_TEXT).get(mj);
                equalText = isEqualText(flatP, textPara);
              } else {
                equalText = false;
              }
            }
            if (!equalText) {
              nTables.add(k);
              nFrameTable = k + 1;
              toTextMapping.add(new TextParagraph(CURSOR_TYPE_TABLE, k));
              toParaMapping.get(CURSOR_TYPE_TABLE).set(k, i);
              continue;
            }
            isMapped = true;
          }
          //  test for text
          if (!isMapped) {
            textPara = hasFootnote ? SingleCheck.removeFootnotes(textParas.get(CURSOR_TYPE_TEXT).get(j), footnotes.get(i), null)
                : textParas.get(CURSOR_TYPE_TEXT).get(j);
            if (isEqualText(flatPara, textPara)) {
              isMapped = true;
            }
          }
          if (isMapped) {
            toTextMapping.add(new TextParagraph(CURSOR_TYPE_TEXT, j));
            toParaMapping.get(CURSOR_TYPE_TEXT).add(i);
            nText.set(CURSOR_TYPE_TEXT, j + 1);
            continue;
          }
        }
        //  unknown paragraph
        toTextMapping.add(new TextParagraph(CURSOR_TYPE_UNKNOWN, -1));
        numUnknown++;
        if (debugMode || firstTextDone || !paragraphs.get(i).isEmpty()) {
          MessageHandler.printToLogFile(
              "WARNING: DocumentCache: Could not map Paragraph(" + i + "): '" + paragraphs.get(i) + "'; secondTextDone: " + secondTextDone);
        }
        if (debugMode) {
          MessageHandler.printToLogFile("DocumentCache: mapParagraphs:");
          for (int k = 0; k < NUMBER_CURSOR_TYPES; k++) {
            MessageHandler.printToLogFile("Actual Cursor Paragraph (Type " + k + "): "
                + (nText.get(k) < textParas.get(k).size() ? "'" 
                + (k == CURSOR_TYPE_TABLE ? textParas.get(k).get(nFrameTable) : textParas.get(k).get(nText.get(k))) + "'"
                    : "no paragraph left"));
          }
          MessageHandler.printToLogFile("Unknown Paragraphs: " + (numUnknown - 1) + " from " + nUnknown);
        }
      }
      if (deletedChars == null) {
        for (int i = 0; i < toTextMapping.size(); i++) {
          deletedCharacters.add(null);
        }
      } else {
        for (int i = 0; i < toTextMapping.size(); i++) {
          if (toTextMapping.get(i).type == CURSOR_TYPE_UNKNOWN) {
            deletedCharacters.add(null);
            MessageHandler.printToLogFile("Warning: CURSOR_TYPE_UNKNOWN at Paragraph " + i + ": deleted Characters set to null");
          } else {
            deletedCharacters.add(deletedChars.get(toTextMapping.get(i).type).get(toTextMapping.get(i).number));
          }
        }
      }
      prepareChapterBeginsForText(chapterBegins, toTextMapping, locales);
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
              + locales.get(i).Language + "-" + locales.get(i).Country 
              + "; Deleted Chars size: " + (deletedCharacters.get(i) == null ? "null" : deletedCharacters.get(i).size())
              + "; '" + paragraphs.get(i) + "'");
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
        MessageHandler.printToLogFile("DocumentCache: mapParagraphs: Number of Deleted Chars: " + deletedCharacters.size());
      }
/*
      for (int i = 0; i < locales.size(); i++) {
        MessageHandler.printToLogFile("DocumentCache: mapParagraphs: Num: " + i + " locale: " + locales.get(i).toString());
      }
*/
    }
  }

  /**
   * reset the document cache for impress documents
   */
  private void refreshImpressCalcCache(XComponent xComponent) {
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
    nText = container.paragraphs.size();
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
      deletedCharacters.add(null);
    }
    if (debugMode) {
      MessageHandler.printToLogFile("DocumentCache: reset: isImpress: Number of paragraphse: " + paragraphs.size());
      for (int i = 0; i < NUMBER_CURSOR_TYPES; i++) {
        MessageHandler.printToLogFile("DocumentCache: reset: CursorType: " + i + "; Number of paragraphs: " + toParaMapping.get(i).size());
      }
    }
  }

  /**
   * wait till write < n is finished
   */
  public void waitForFinish(int n) {
    while (nWriteAccess > n + 1) {
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        MessageHandler.showError(e);
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
  public String getFlatParagraph(int n) {
    waitForFinish(-1);
    return n < 0 || n >= locales.size() ? null : paragraphs.get(n);
  }

  /**
   * set Flat Paragraph at Index
   */
  public void setFlatParagraph(int n, String sPara) {
    waitForFinish(-1);
    paragraphs.set(n, sPara);
  }

  /**
   * set Flat Paragraph and Locale at Index
   */
  public void setFlatParagraph(int n, String sPara, Locale locale) {
    waitForFinish(-1);
    paragraphs.set(n, sPara);
    locales.set(n, new SerialLocale(locale));
  }

  /**
   * is multilingual Flat Paragraph
   */
  public boolean isMultilingualFlatParagraph(int n) {
    waitForFinish(-1);
    return n < 0 || n >= locales.size() ? false : locales.get(n).Variant.startsWith(OfficeTools.MULTILINGUAL_LABEL);
  }

  /**
   * set multilingual flag to Flat Paragraph
   */
  public void setMultilingualFlatParagraph(int n) {
    waitForFinish(-1);
    SerialLocale locale = locales.get(n);
    if (!locale.Variant.startsWith(OfficeTools.MULTILINGUAL_LABEL)) {
      locale.Variant = OfficeTools.MULTILINGUAL_LABEL + locale.Variant;
      locales.set(n, locale);
    }
  }

  /**
   * get Locale of Flat Paragraph by Index
   */
  public Locale getFlatParagraphLocale(int n) {
    waitForFinish(-1);
    return locales.get(n).toLocaleWithoutLabel();
  }

  /**
   * set Locale of Flat Paragraph by Index
   */
  public void setFlatParagraphLocale(int n, Locale locale) {
    waitForFinish(-1);
    locales.set(n, new SerialLocale(locale));
  }

  /**
   * get footnotes of Flat Paragraph by Index
   */
  public int[] getFlatParagraphFootnotes(int n) {
    waitForFinish(-1);
    return footnotes.get(n);
  }

  /**
   * set footnotes of Flat Paragraph by Index
   */
  public void setFlatParagraphFootnotes(int n, int[] footnotePos) {
    waitForFinish(-1);
    footnotes.set(n, footnotePos);
  }
  
  /**
   * get deleted characters (report changes) of Flat Paragraph by Index
   */
  public List<Integer> getFlatParagraphDeletedCharacters(int n) {
    waitForFinish(-1);
    return deletedCharacters.get(n);
  }

  /**
   * get deleted characters (report changes) of Flat Paragraph by Index
   */
  public boolean isAutomaticGenerated(int n) {
    waitForFinish(-1);
    if (n >= 0 && n < toTextMapping.size()) {
      if (locales.get(n).Language.equals(OfficeTools.IGNORE_LANGUAGE)) {
        return true;
      }
      TextParagraph tPara = getNumberOfTextParagraph(n);
      if (tPara.type == CURSOR_TYPE_TEXT && automaticParagraphs.contains(tPara.number)) {
        return true;
      }
    }
    return false;
  }

  /**
   * set deleted characters (report changes) of Flat Paragraph by Index
   */
  public void setFlatParagraphDeletedCharacters(int n, List<Integer> deletedChars) {
    waitForFinish(-1);
    deletedCharacters.set(n, deletedChars);
  }

  /**
   * correct a start point to change of flat paragraph by zero space characters
   */
  public int correctStartPoint(int nStart, int nFPara) {
    int cor = 0;
    for (int i = 0; i < nStart; i++) {
      if (paragraphs.get(nFPara).charAt(i) == OfficeTools.ZERO_WIDTH_SPACE_CHAR) {
        boolean isFootnote = false;
        for (int n : footnotes.get(nFPara)) {
          if (n == i) {
            isFootnote = true;
            break;
          }
        }
        if (!isFootnote) {
          cor++;
        }
      }
    }
    return nStart - cor;
  }

  /**
   * clear document cache
   */
  private void clear() {
    paragraphs.clear();
    chapterBegins.clear();
    locales.clear();
    footnotes.clear();
    toTextMapping.clear();
    toParaMapping.clear();
    deletedCharacters.clear();
  }
  
  /**
   * Add a document Cache
   */
  private void add(DocumentCache in) {
    paragraphs.addAll(in.paragraphs);
    chapterBegins.addAll(in.chapterBegins);
    locales.addAll(in.locales);
    footnotes.addAll(in.footnotes);
    toTextMapping.addAll(in.toTextMapping);
    for (int i = 0; i < NUMBER_CURSOR_TYPES; i++) {
      toParaMapping.add(new ArrayList<Integer>(in.toParaMapping.get(i)));
    }
    deletedCharacters.addAll(in.deletedCharacters);
    nText = in.nText;
    nTable = in.nTable;
    nFrame = in.nFrame;
    nFootnote = in.nFootnote;
    nEndnote = in.nEndnote;
    nHeaderFooter = in.nHeaderFooter;
  }
  
  /**
   * Replace a document Cache
   */
  public void put(DocumentCache in) {
    nWriteAccess++;
    waitForFinish(nWriteAccess - 1);
    clear();
    add(in);
    nWriteAccess--;
  }

  /**
   * is DocumentCache empty
   */
  public boolean isEmpty() {
    waitForFinish(-1);
    return paragraphs == null || paragraphs.isEmpty();
  }

  /**
   * has no content
   */
  public boolean hasNoContent() {
    waitForFinish(-1);
    return paragraphs == null || paragraphs.isEmpty() || (paragraphs.size() == 1 && paragraphs.get(0).isEmpty());
  }

  /**
   * size of document cache (number of all flat paragraphs)
   */
  public int size() {
    waitForFinish(-1);
    return paragraphs == null ? 0 : paragraphs.size();
  }

  /**
   * get the Content of a Text Paragraph
   */
  public String getTextParagraph(TextParagraph textParagraph) {
    waitForFinish(-1);
    if (textParagraph.type == CURSOR_TYPE_UNKNOWN) {
      return null;
    }
    return paragraphs.get(toParaMapping.get(textParagraph.type).get(textParagraph.number));
  }

  /**
   * get Number of Flat Paragraph from Number of Text Paragraph
   */
  public int getFlatParagraphNumber(TextParagraph textParagraph) {
    waitForFinish(-1);
//    if (textParagraph.type == CURSOR_TYPE_UNKNOWN || !isFinished()) {
    if (textParagraph.type == CURSOR_TYPE_UNKNOWN || toParaMapping.get(textParagraph.type).size() <= textParagraph.number) {
      return -1;
    }
    return toParaMapping.get(textParagraph.type).get(textParagraph.number);
  }

  /**
   * get Locale of Text Paragraph by Index
   */
  public Locale getTextParagraphLocale(TextParagraph textParagraph) {
    waitForFinish(-1);
    if (textParagraph.type == CURSOR_TYPE_UNKNOWN) {
      return null;
    }
    return locales.get(toParaMapping.get(textParagraph.type).get(textParagraph.number)).toLocaleWithoutLabel();
  }

  /**
   * get deleted Characters of Text Paragraph
   */
  public List<Integer> getTextParagraphDeletedCharacters(TextParagraph textParagraph) {
    waitForFinish(-1);
    if (textParagraph.type == CURSOR_TYPE_UNKNOWN) {
      return null;
    }
    return deletedCharacters.get(toParaMapping.get(textParagraph.type).get(textParagraph.number));
  }

  /**
   * get footnotes of Text Paragraph by Index
   */
  public int[] getTextParagraphFootnotes(TextParagraph textParagraph) {
    waitForFinish(-1);
    if (textParagraph.type == CURSOR_TYPE_UNKNOWN) {
      return new int[0];
    }
    return footnotes.get(toParaMapping.get(textParagraph.type).get(textParagraph.number));
  }

  /**
   * set footnotes of Text Paragraph
   */
  public void setTextParagraphFootnotes(TextParagraph textParagraph, int[] footnotePos) {
    waitForFinish(-1);
    if (textParagraph.type != CURSOR_TYPE_UNKNOWN) {
      footnotes.set(toParaMapping.get(textParagraph.type).get(textParagraph.number), footnotePos);
    }
  }

  /**
   * get Number of Text Paragraph from Number of Flat Paragraph
   */
  public TextParagraph getNumberOfTextParagraph(int numberOfFlatParagraph) {
    waitForFinish(-1);
    return toTextMapping.get(numberOfFlatParagraph);
  }

  /**
   * size of text cache (without headers, footnotes, etc.)
   */
  public int textSize(TextParagraph textParagraph) {
    waitForFinish(-1);
    if (textParagraph.type == CURSOR_TYPE_UNKNOWN || textParagraph.type >= toParaMapping.size()) {
      return 0;
    }
    return toParaMapping.get(textParagraph.type).size();
  }

  /**
   * Text and local are equal to cache
   */
  public boolean isEqual(int n, String text, Locale locale) {
    waitForFinish(-1);
    return ((n < 0 || n >= locales.size() || locales.get(n) == null) ? false
        : ((isMultilingualFlatParagraph(n) || locales.get(n).equalsLocale(locale)) && text.equals(paragraphs.get(n))));
  }

  /**
   * Text, deleted chars and local are equal to cache
   */
  public boolean isEqual(int n, String text, Locale locale, List<Integer> delChars) {
    waitForFinish(-1);
    if (n < 0 || n >= locales.size() || locales.get(n) == null) {
      return false;
    }
    if (!isMultilingualFlatParagraph(n) && !locales.get(n).equalsLocale(locale)) {
      return false;
    }
    if ((delChars != null && deletedCharacters.get(n) == null) || (delChars == null && deletedCharacters.get(n) != null) 
       || (delChars != null && deletedCharacters.get(n) != null && delChars.size() != deletedCharacters.get(n).size())) {
      return false;
    }
    return text.equals(paragraphs.get(n));
  }
  
  /**
   * size of cavhe has changed?
   */
  public boolean isEqualCacheSize(DocumentCursorTools docCursor) {
    waitForFinish(-1);
    if (nText != docCursor.getNumberOfAllTextParagraphs()) {
      return false;
    }
    if (nTable != docCursor.getNumberOfAllTables()) {
      return false;
    }
    if (nFrame != docCursor.getNumberOfAllFrames()) {
      return false;
    }
    if (nFootnote != docCursor.getNumberOfAllFootnotes()) {
      return false;
    }
    if (nEndnote != docCursor.getNumberOfAllEndnotes()) {
      return false;
    }
    if (nHeaderFooter != docCursor.getNumberOfAllHeadersAndFooters()) {
      return false;
    }
    return true;
  }

  /**
   * Gives back the start paragraph for text level check
   */
  public int getStartOfParaCheck(TextParagraph textParagraph, int parasToCheck, boolean checkOnlyParagraph,
      boolean useQueue, boolean addParas) {
    waitForFinish(-1);
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
  public int getEndOfParaCheck(TextParagraph textParagraph, int parasToCheck, boolean checkOnlyParagraph,
      boolean useQueue, boolean addParas) {
    waitForFinish(-1);
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
  public String getDocAsString(TextParagraph textParagraph, int parasToCheck, boolean checkOnlyParagraph,
      boolean useQueue, boolean hasFootnotes) {
    waitForFinish(-1);
    int startPos = getStartOfParaCheck(textParagraph, parasToCheck, checkOnlyParagraph, useQueue, true);
    int endPos = getEndOfParaCheck(textParagraph, parasToCheck, checkOnlyParagraph, useQueue, true);
    StringBuilder docText;
    if (parasToCheck < -1) { // check all flat paragraphs
      if (startPos < 0 || endPos < 0
          || (hasFootnotes && getFlatParagraph(startPos).isEmpty() && getFlatParagraphFootnotes(startPos).length > 0)) {
        return "";
      }
      docText = new StringBuilder(fixLinebreak(SingleCheck.removeFootnotes(getFlatParagraph(startPos),
          (hasFootnotes ? getFlatParagraphFootnotes(startPos) : null), getFlatParagraphDeletedCharacters(startPos))));
      for (int i = startPos + 1; i < endPos; i++) {
        docText.append(OfficeTools.END_OF_PARAGRAPH).append(fixLinebreak(
            SingleCheck.removeFootnotes(getFlatParagraph(i), (hasFootnotes ? getFlatParagraphFootnotes(i) : null), getFlatParagraphDeletedCharacters(i))));
      }
    } else {
      TextParagraph startPara = new TextParagraph(textParagraph.type, startPos);
      if (startPos < 0 || endPos < 0 || (hasFootnotes && getTextParagraph(startPara).isEmpty()
          && getTextParagraphFootnotes(startPara).length > 0)) {
        return "";
      }
      docText = new StringBuilder(fixLinebreak(SingleCheck.removeFootnotes(getTextParagraph(startPara),
          (hasFootnotes ? getTextParagraphFootnotes(startPara) : null), getTextParagraphDeletedCharacters(startPara))));
      for (int i = startPos + 1; i < endPos; i++) {
        TextParagraph tPara = new TextParagraph(textParagraph.type, i);
        docText.append(OfficeTools.END_OF_PARAGRAPH).append(fixLinebreak(SingleCheck
            .removeFootnotes(getTextParagraph(tPara), (hasFootnotes ? getTextParagraphFootnotes(tPara) : null), getTextParagraphDeletedCharacters(tPara))));
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
  public int getStartOfParagraph(int nPara, TextParagraph textParagraph, int parasToCheck, boolean checkOnlyParagraph,
      boolean useQueue, boolean hasFootnotes) {
    waitForFinish(-1);
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
                (hasFootnotes ? getFlatParagraphFootnotes(i) : null), getFlatParagraphDeletedCharacters(i)).length() + OfficeTools.NUMBER_PARAGRAPH_CHARS;
      }
    } else {
      for (int i = startPos; i < nPara; i++) {
        TextParagraph tPara = new TextParagraph(textParagraph.type, i);
        pos += SingleCheck.removeFootnotes(getTextParagraph(tPara), 
                (hasFootnotes ? getTextParagraphFootnotes(tPara) : null), getTextParagraphDeletedCharacters(tPara)).length() + OfficeTools.NUMBER_PARAGRAPH_CHARS;
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
     *  Get a String from SerialLocale
     */
    public String toString() {
      return Language + (Country.isEmpty() ? "" : "-" + Country) + (Variant.isEmpty() ? "" : "-" + Variant);
    }

    /**
     * return the Locale as String
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
