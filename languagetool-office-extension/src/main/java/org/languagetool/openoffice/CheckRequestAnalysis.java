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

import java.util.List;
import java.util.Map;

import org.languagetool.Language;
import org.languagetool.gui.Configuration;
import org.languagetool.openoffice.DocumentCache.ChangedRange;
import org.languagetool.openoffice.DocumentCache.TextParagraph;
import org.languagetool.openoffice.OfficeTools.DocumentType;

import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XFlatParagraph;

/**
 * Class to analyze a LO/OO request for grammar check
 * Get the number of paragraph that has to be checked (-1 if paragraph could not be found)
 * Changes document cache, if text has be changed
 * Gives hint that the checked paragraph has changed
 * Shift result cache if lines have be inserted or deleted
 * Gives the range of paragraphs, which should be checked again after text has changed
 * @since 5.3
 * @author Fred Kruse
 */
class CheckRequestAnalysis {
  
  private static int debugMode;                     //  should be 0 except for testing; 1 = low level; 2 = advanced level
  private static boolean debugModeTm;               //  time measurement should be false except for testing
  
  private final int numParasToCheck;                //  current number of Paragraphs to be checked

  private final XComponent xComponent;              //  XComponent of the open document
  private final String docID;                       //  docID of the document
  private final MultiDocumentsHandler mDocHandler;  //  handles the different documents loaded in LO/OO
  private final SingleDocument singleDocument;      //  handles one document
  private final List<Integer> minToCheckPara;       //  List of minimal to check paragraphs for different classes of text level rules
  private final Language docLanguage;               //  docLanguage (usually the Language of the first paragraph)
  private final Language fixedLanguage;             //  fixed language (by configuration); if null: use language of document (given by LO/OO)
  private final boolean useQueue;                   //  true: use queue to check text level rules (given by configuration)
  private final DocumentType docType;               //  save the type of document
  private final int proofInfo;                      //  Information about proof request (supported by LO > 6.4 otherwise: 0 == UNKNOWN)
  private final DocumentCache docCache;             //  cache of paragraphs (only readable by parallel thread)
  private final Map<Integer, String> changedParas;  //  Map of last changed paragraphs;
  private final List<ResultCache> paragraphsCache;  //  Cache for matches of text rules

//  private FlatParagraphTools flatPara;              //  Save information for flat paragraphs (including iterator and iterator provider) for the single document
//  private ViewCursorTools viewCursor;               //  Get the view cursor for desktop
  private int numLastVCPara;                        //  Save position of ViewCursor for the single documents
  private List<Integer> numLastFlPara;              //  Save position of FlatParagraph for the single documents
//  private DocumentCursorTools docCursor = null;     //  Save document cursor for the single document
  private int changeFrom = 0;                       //  Change result cache from paragraph
  private int changeTo = 0;                         //  Change result cache to paragraph
  private boolean textIsChanged = false;            //  true: check number of paragraphs again
  private int numParasToChange = -1;                //  Number of paragraphs to change for n-paragraph cache
  private int paraNum;                              //  Number of current checked paragraph

//  CheckRequestAnalysis(int numLastVCPara, int numLastFlPara, int proofInfo, int numParasToCheck, Language fixedLanguage, Language docLanguage,
//      SingleDocument singleDocument, List<ResultCache> paragraphsCache, ViewCursorTools viewCursor, Map<Integer, String> changedParas) {
  CheckRequestAnalysis(int numLastVCPara, List<Integer> numLastFlPara, int proofInfo, int numParasToCheck, Language fixedLanguage, Language docLanguage,
      SingleDocument singleDocument, List<ResultCache> paragraphsCache, Map<Integer, String> changedParas) {
    debugMode = OfficeTools.DEBUG_MODE_CR;
    debugModeTm = OfficeTools.DEBUG_MODE_TM;
    this.singleDocument = singleDocument;
//    this.viewCursor = viewCursor;
    this.numLastVCPara = numLastVCPara;
    this.numLastFlPara = numLastFlPara;
    this.proofInfo = proofInfo;
    this.paragraphsCache = paragraphsCache;
    this.changedParas = changedParas;
    this.fixedLanguage = fixedLanguage;
    this.docLanguage = docLanguage;
    mDocHandler = singleDocument.getMultiDocumentsHandler();
    xComponent = singleDocument.getXComponent();
    docID = singleDocument.getDocID();
    docType = singleDocument.getDocumentType();
    minToCheckPara = mDocHandler.getNumMinToCheckParas();
    docCache = singleDocument.getDocumentCache();
//    flatPara = singleDocument.getFlatParagraphTools();
    Configuration config = mDocHandler.getConfiguration();
    this.numParasToCheck = mDocHandler.isTestMode() ? 0 : numParasToCheck;
    useQueue = (numParasToCheck != 0 && proofInfo != OfficeTools.PROOFINFO_GET_PROOFRESULT && config.useTextLevelQueue());
    for (int minPara : minToCheckPara) {
      if (minPara > numParasToChange) {
        numParasToChange = minPara;
      }
    }
  }
  
  /**
   * get number of paragraph from node index
   */
  int getNumberOfParagraphFromSortedTextId(int sortedTextId, int documentElementsCount, String paraText, Locale locale, int[] footnotePosition) {
    //  test if doc cache has changed --> actualize
    if (!docCache.isActual(documentElementsCount)) {
      handleCacheChanges();
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalyzes: getNumberOfParagraphFromSortedTextId: cache actualized, documentElementsCount: " + documentElementsCount);
      }
    }
    int paraNum = docCache.getFlatparagraphFromSortedTextId(sortedTextId);
    //  if number of paragraph < 0 --> actualize doc cache and try again
    if (paraNum < 0) {
      handleCacheChanges();
      paraNum = docCache.getFlatparagraphFromSortedTextId(sortedTextId);
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalyzes: getNumberOfParagraphFromSortedTextId: paraNum < 0 " + 
              " --> cache actualized, new paraNum: " + paraNum);
      }
    }
    if (paraNum >= 0) {
      //  test if paragraph has changed --> actualize all caches for single paragraph
      TextParagraph tPara = docCache.getNumberOfTextParagraph(paraNum);
      DocumentCursorTools docCursor = singleDocument.getDocumentCursorTools();
      List<Integer> deletedChars = docCursor.getDeletedCharactersOfTextParagraph(tPara);
      
      if (!docCache.isEqual(paraNum, paraText, locale, deletedChars)) {
        handleChangedPara(paraNum, paraText, locale, footnotePosition, deletedChars);
      }
    }
    return paraNum;
  }
  
  /**
   * get number of paragraph
   */
  int getNumberOfParagraph(int nPara, String chPara, Locale locale, int startPos, int[] footnotePositions) {
    paraNum = getParaPos(nPara, chPara, locale, startPos, footnotePositions);
    if (isDisposed() || paraNum >= docCache.size()) {
      paraNum = -1;
    }
    return paraNum;
  }
  
  /**
   * Actualize document cache and result cache for given paragraph number
   */
  void actualizeDocumentCache (int nPara, boolean isIntern) {
    if (isDisposed()) {
      return;
    }
    long startTime = 0;
    if (debugModeTm) {
      startTime = System.currentTimeMillis();
    }
    if (docCache.isEmpty()) {
      docCache.refresh(singleDocument, LinguisticServices.getLocale(fixedLanguage), LinguisticServices.getLocale(docLanguage), xComponent, 1);
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: actualizeDocumentCache: resetAllParas (docCache is empty): new docCache.size: " + docCache.size()
                + ", docID: " + docID + OfficeTools.LOG_LINE_BREAK);
      }
      if (docCache.isEmpty()) {
        return;
      }
    } else {
      int nOldParas = docCache.size();
      changesInNumberOfParagraph(false);
      int numParas = docCache.size();
      if (numParas <= 0) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: actualizeDocumentCache: docCache error!");
        return;
      }
      textIsChanged = true;
      if (nOldParas != numParas) {
        if (debugMode > 1) {
          MessageHandler.printToLogFile("CheckRequestAnalysis: actualizeDocumentCache: Number of Paragraphs has changed: old:" +
              nOldParas + ", new:" + numParas);
        }
        return;
      }
    }
    FlatParagraphTools flatPara = singleDocument.getFlatParagraphTools();
    XFlatParagraph xFlatPara = flatPara.getFlatParagraphAt(nPara);
    if (isDisposed() || xFlatPara == null) {
      return;
    }
    String chPara = xFlatPara.getText();
    Locale docLocale = docLanguage == null ? null : LinguisticServices.getLocale(docLanguage);
    Locale lastLocale = nPara <= 0 ? null : docCache.getFlatParagraphLocale(nPara - 1);
    try {
      Locale locale = flatPara.getPrimaryParagraphLanguage(xFlatPara, 0, chPara.length(), docLocale, lastLocale, false);
      DocumentCursorTools docCursor = singleDocument.getDocumentCursorTools();
      List<Integer> deletedChars = docCursor.getDeletedCharactersOfTextParagraph(docCache.getNumberOfTextParagraph(nPara));
      if (!docCache.isEqual(nPara, chPara, locale, deletedChars)) {
        if (debugMode > 1) {
          MessageHandler.printToLogFile("ICheckRequestAnalysis: actualizeDocumentCache: Paragraph has changed:\nold:" 
              + chPara + "\nnew:" + docCache.getFlatParagraph(nPara));
        }
        docCache.setFlatParagraph(nPara, chPara, locale);
        singleDocument.removeResultCache(nPara,true);
        singleDocument.removeIgnoredMatch(nPara, isIntern);
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
    }
    if (debugModeTm) {
      long runTime = System.currentTimeMillis() - startTime;
      if (runTime > OfficeTools.TIME_TOLERANCE) {
        MessageHandler.printToLogFile("Time to actualize document cache: " + runTime);
      }
    }
  }
  
  /** 
   * Get new initialized flat paragraph tools
   *//*
  FlatParagraphTools getFlatParagraphTools() {
    if (flatPara == null) {
      flatPara = new FlatParagraphTools(xComponent);
    }
    return flatPara;
  }
*/  
  /** 
   * Get new initialized view cursor tools
   *//*
  ViewCursorTools getViewCursorTools() {
    return viewCursor;
  }
*/  
  /** 
   * Get new initialized document cursor tools
   *//*
  DocumentCursorTools getDocumentCursorTools() {
    return docCursor;
  }
*/
  /** 
   * Get last number of paragraph from view cursor
   */
  int getLastParaNumFromViewCursor() {
    return numLastVCPara;
  }
  
  /** 
   * Get last number of paragraph from flat paragraph
   */
  List<Integer> getLastParaNumFromFlatParagraph() {
    return numLastFlPara;
  }
  
  /** Get first paragraph to change
   */
  int getFirstParagraphToChange() {
    return changeFrom;
  }
  
  /** Get last paragraph to change
   */
  int getLastParagraphToChange() {
    return changeTo;
  }

  /** Text is changed
   */
  boolean textIsChanged() {
    return textIsChanged;
  }

  /** 
   * Open new flat paragraph tools or initialize them again
   *//*
  private void setFlatParagraphTools(XComponent xComponent) {
    long startTime = 0;
    if (debugModeTm) {
      startTime = System.currentTimeMillis();
    }
    if (flatPara == null) {
      flatPara = new FlatParagraphTools(xComponent);
    } else {
      flatPara.init();
    }
    if (debugModeTm) {
      long runTime = System.currentTimeMillis() - startTime;
      if (runTime > OfficeTools.TIME_TOLERANCE) {
        MessageHandler.printToLogFile("Time to run setFlatParagraphTools: " + runTime);
      }
    }
  }
  */
  /**
   * Search for Position of Paragraph
   * gives Back the Position of flat paragraph / -1 if Paragraph can not be found
   */
  private int getParaPos(int nPara, String chPara, Locale locale, int startPos, int[] footnotePositions) {
    
    if (isDisposed()) {
      return -1;
    }
    long startTime = 0;
    if (debugModeTm) {
      startTime = System.currentTimeMillis();
    }
    if (debugMode > 0 && proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: getParaPos: PROOFINFO_GET_PROOFRESULT: analysis started");
    }
    if (docType != DocumentType.WRITER && docCache.isEmpty()) {
      if (debugMode > 0 && proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: getParaPos: start docCache.refresh");
      }
      docCache.refresh(singleDocument, LinguisticServices.getLocale(fixedLanguage), LinguisticServices.getLocale(docLanguage), xComponent, 3);
    }

    if (nPara >= 0) {
      return nPara;
    }

    if (numParasToCheck == 0 || xComponent == null) {
      return -1;  //  check only the processed paragraph
    }

    // Initialization 
    if (debugMode > 0 && proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: getParaPos: start setFlatParagraphTools");
    }

    if (debugMode > 0 && proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: getParaPos: test docCache.isEmpty");
    }
    if (docCache.isEmpty()) {
      if (debugMode > 0 && proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: getParaPos: get DocumentCursorTools");
      }
      docCache.refresh(singleDocument, LinguisticServices.getLocale(fixedLanguage), LinguisticServices.getLocale(docLanguage), xComponent, 4);
      if (debugMode > 0 && proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: getParaPos: start docCache.refresh");
      }
      if (debugMode > 0 || proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: getParaPos: resetAllParas (docCache is empty): new docCache.size: " + docCache.size()
                + ", docID: " + docID + OfficeTools.LOG_LINE_BREAK);
      }
      if (docCache.isEmpty()) {
        return -1;
      }
    }
    
    if (debugMode > 1) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: getParaPos: proofInfo = " + proofInfo);
    }
    if (debugModeTm) {
      long runTime = System.currentTimeMillis() - startTime;
      if (runTime > OfficeTools.TIME_TOLERANCE) {
        MessageHandler.printToLogFile("Time to run getParaPos initialization: " + runTime);
      }
    }
    
    if (proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT) {
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: getParaPos: start getParaFromViewCursorOrDialog");
      }
      return getParaFromViewCursorOrDialog(chPara, locale, footnotePositions);
    }
    else {
      return getParaFromFlatparagraph(chPara, locale, startPos, footnotePositions);
    }
    
  }
  
  /**
   * Search for Position of Paragraph if reason for proof is mark paragraph or no proof info
   * returns -1 if Paragraph can not be found
   */
  private int getParaFromFlatparagraph(String chPara, Locale locale, int startPos, int[] footnotePositions) {
    if (isDisposed() || docCache.isEmpty()) {
      return -1;
    }
    long startTime = 0;
    if (debugModeTm) {
      startTime = System.currentTimeMillis();
    }
    // try to get next position from last FlatParagraph position (for performance reasons)
    if (startPos != 0 && proofInfo == OfficeTools.PROOFINFO_MARK_PARAGRAPH) {
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromFlatparagraph: Number of Paragraph: " + numLastFlPara 
            + "; startPos = " + startPos
            + " (proofInfo == " + OfficeTools.PROOFINFO_MARK_PARAGRAPH + ")" + OfficeTools.LOG_LINE_BREAK);
      }
      if (debugModeTm) {
        long runTime = System.currentTimeMillis() - startTime;
        if (runTime > OfficeTools.TIME_TOLERANCE) {
          MessageHandler.printToLogFile("Time to run getParaFromFlatparagraph(numLastFlPara, startPos > 0): " + runTime);
        }
      }
      return numLastFlPara.get(numLastFlPara.size() - 1);
    }
    int nPara = findNextParaPos(chPara, locale, startPos);
    if (nPara >= 0) {
//      numLastFlPara = nPara;
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromFlatparagraph: Number of Paragraph: " + nPara 
            + ", start: " + startPos + OfficeTools.LOG_LINE_BREAK);
      }
      if (debugModeTm) {
        long runTime = System.currentTimeMillis() - startTime;
        if (runTime > OfficeTools.TIME_TOLERANCE) {
          MessageHandler.printToLogFile("Time to run getParaFromFlatparagraph(numLastFlPara, startPos == 0): " + runTime);
        }
      }
      return nPara;
    } else if (debugMode > 0) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromFlatparagraph: last Number of Paragraph: " + numLastFlPara +
          ", Type: " + docCache.getNumberOfTextParagraph(numLastFlPara.get(numLastFlPara.size() - 1)).type);
      MessageHandler.printToLogFile("Old Para: " + docCache.getFlatParagraph(numLastFlPara.get(numLastFlPara.size() - 1)));
      MessageHandler.printToLogFile("New Para: " + chPara);
    }
    
    // number of paragraphs has changed? --> Update the internal information
    nPara = changesInNumberOfParagraph(true);
    if (debugMode > 0) {
      MessageHandler.printToLogFile("New Para Number: " + nPara + ", Type: " + docCache.getNumberOfTextParagraph(nPara).type);
    }
    if (nPara < 0) {
      //  problem with automatic iteration - try to get ViewCursor position
      if (debugModeTm) {
        long runTime = System.currentTimeMillis() - startTime;
        if (runTime > OfficeTools.TIME_TOLERANCE) {
          MessageHandler.printToLogFile("Time to run getParaFromFlatparagraph(getParaFromViewCursorOrDialog 1): " + runTime);
        }
      }
      return getParaFromViewCursorOrDialog(chPara, locale, footnotePositions);
    }
    if (isDisposed()) {
      return -1;
    }
    TextParagraph nTPara = docCache.getNumberOfTextParagraph(nPara); 
    if (proofInfo == OfficeTools.PROOFINFO_MARK_PARAGRAPH) {
      if (nTPara.type == DocumentCache.CURSOR_TYPE_UNKNOWN) {
        if (debugModeTm) {
          long runTime = System.currentTimeMillis() - startTime;
          if (runTime > OfficeTools.TIME_TOLERANCE) {
            MessageHandler.printToLogFile("Time to run getParaFromFlatparagraph(getPosFromChangedPara 1): " + runTime);
          }
        }
        return getPosFromChangedPara(chPara, locale, nPara, footnotePositions);
      }
    }
    if (!isDisposed()) {
      FlatParagraphTools flatPara = singleDocument.getFlatParagraphTools();
      String curFlatParaText = flatPara.getCurrentParaText();
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromFlatparagraph: curFlatParaText: " + curFlatParaText + OfficeTools.LOG_LINE_BREAK
            + "chPara: " + chPara + OfficeTools.LOG_LINE_BREAK + "getFlatParagraph: " + docCache.getFlatParagraph(nPara) + OfficeTools.LOG_LINE_BREAK);
      }
      if (proofInfo == OfficeTools.PROOFINFO_UNKNOWN) {
        if (curFlatParaText != null && !curFlatParaText.equals(chPara) && curFlatParaText.equals(docCache.getFlatParagraph(nPara))) {
          //  wrong flat paragraph - try to get ViewCursor position
          if (debugModeTm) {
            long runTime = System.currentTimeMillis() - startTime;
            if (runTime > OfficeTools.TIME_TOLERANCE) {
              MessageHandler.printToLogFile("Time to run getParaFromFlatparagraph(getParaFromViewCursorOrDialog 2): " + runTime);
            }
          }
          return getParaFromViewCursorOrDialog(chPara, locale, footnotePositions);
        }
        //  test real flat paragraph rather then the one given by Proofreader - it could be changed meanwhile
        if (curFlatParaText != null) {
          chPara = curFlatParaText;
        }
      } else {
        if (curFlatParaText != null && !curFlatParaText.equals(docCache.getFlatParagraph(nPara))) {
          //  wrong flat paragraph - try to get paragraph from cache
          int n = getParaFromDocCache(chPara, locale, nPara);
          if (n >= 0) {
            TextParagraph tPara = docCache.getNumberOfTextParagraph(n);
            if (tPara != null && tPara.type != DocumentCache.CURSOR_TYPE_UNKNOWN && tPara.number >= 0) {
              numLastFlPara.set(tPara.type, tPara.number);
            }
            numLastFlPara.set(numLastFlPara.size() - 1, n);
            if (debugMode > 0) {
              MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromFlatparagraph: From document cache: Number of Paragraph: " + n 
                  + ", start: " + startPos + OfficeTools.LOG_LINE_BREAK);
            }
            textIsChanged = true;
            if (debugModeTm) {
              long runTime = System.currentTimeMillis() - startTime;
              if (runTime > OfficeTools.TIME_TOLERANCE) {
                MessageHandler.printToLogFile("Time to run getParaFromFlatparagraph: " + runTime);
              }
            }
            return n;
          }
        }
      }
    }
    // find position from changed paragraph
    if (debugModeTm) {
      long runTime = System.currentTimeMillis() - startTime;
      if (runTime > OfficeTools.TIME_TOLERANCE) {
        MessageHandler.printToLogFile("Time to run getParaFromFlatparagraph(getPosFromChangedPara 2): " + runTime);
      }
    }
    return getPosFromChangedPara(chPara, locale, nPara, footnotePositions);
  }

  /**
   *  Is document disposed?
   */
  private boolean isDisposed() {
    return singleDocument.isDisposed();
  }

  /**
   * Get number of flat paragraph from document cache
   * start with a known paragraph
   * return -1 if fails
   */
  private int getParaFromDocCache(String chPara, Locale locale, int nStart) {
    if (!isDisposed()) {
      for (int i = nStart; i < docCache.size(); i++) {
        if (docCache.isEqual(i, chPara, locale)) {
          return i;
        }
      }
      for (int i = nStart - 1; i >= 0; i--) {
        if (docCache.isEqual(i, chPara, locale)) {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   *  Get view cursor tools
   */
  ViewCursorTools getViewCursorTools() {
    OfficeTools.waitForLO();
    return new ViewCursorTools(xComponent);
  }
  
  /** 
   * Get the Position of Paragraph if result is ordered by right mouse click or spelling dialog
   * returns -1 if it fails
   */
  private int getParaFromViewCursorOrDialog(String chParaWithFootnotes, Locale locale, int[] footnotePositions) {
    // try to get ViewCursor position (proof initiated by mouse click)
    if (isDisposed() || !docCache.isFinished() || docCache.isEmpty()) {
      return -1;
    }
    long startTime = 0;
    if (debugModeTm) {
      startTime = System.currentTimeMillis();
    }
    ViewCursorTools viewCursor = new ViewCursorTools(xComponent);
    int nPara;
    String vcText = SingleCheck.removeFootnotes(viewCursor.getViewCursorParagraphText(), footnotePositions, null);
    String chPara = SingleCheck.removeFootnotes(chParaWithFootnotes, footnotePositions, null);
    if (debugMode > 0) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromViewCursorOrDialog: vcText: " + vcText);
      MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromViewCursorOrDialog: chPara: " + chPara);
    }
    if (chPara.equals(vcText)) {
      TextParagraph tPara = viewCursor.getViewCursorParagraph();
      if (tPara != null && tPara.type != DocumentCache.CURSOR_TYPE_UNKNOWN) {
        nPara = docCache.getFlatParagraphNumber(tPara);
        numLastVCPara = nPara;
        if (proofInfo == OfficeTools.PROOFINFO_MARK_PARAGRAPH) {
          numLastFlPara.set(tPara.type, tPara.number);
          numLastFlPara.set(numLastFlPara.size() - 1, nPara);
        }
        if(!docCache.isEqual(nPara, chParaWithFootnotes, locale)) {
          actualizeDocumentCache(nPara, false);
          if (docCache.getFlatParagraph(nPara) == null || !DocumentCache.isEqualText(docCache.getFlatParagraph(nPara), chPara, footnotePositions)) {
            if (debugMode > 0) {
              MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromViewCursorOrDialog: cText != chPara: Number of Paragraph: " + nPara);
            }
            if (debugModeTm) {
              long runTime = System.currentTimeMillis() - startTime;
              if (runTime > OfficeTools.TIME_TOLERANCE) {
                MessageHandler.printToLogFile("Time to run getParaFromViewCursorOrDialog: " + runTime);
              }
            }
            return -1;
          }
          textIsChanged = true;
        }
        if (debugMode > 0) {
          MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromViewCursorOrDialog: Number of Paragraph: " + nPara + OfficeTools.LOG_LINE_BREAK);
        }
        if (debugModeTm) {
          long runTime = System.currentTimeMillis() - startTime;
          if (runTime > OfficeTools.TIME_TOLERANCE) {
            MessageHandler.printToLogFile("Time to run getParaFromViewCursorOrDialog: " + runTime);
          }
        }
        return nPara;
      }
    }
    // try to get next position from last ViewCursor position (proof per dialog box)
    if (numLastVCPara >= docCache.size()) {
      numLastVCPara = 0;
    }
    nPara = getParaFromDocCache(chPara, locale, numLastVCPara);
    if (nPara >= 0) {
      numLastVCPara = nPara;
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromViewCursorOrDialog: From DocCache: Number of Paragraph: " + nPara + OfficeTools.LOG_LINE_BREAK);
      }
      if (debugModeTm) {
        long runTime = System.currentTimeMillis() - startTime;
        if (runTime > OfficeTools.TIME_TOLERANCE) {
          MessageHandler.printToLogFile("Time to run getParaFromViewCursorOrDialog: " + runTime);
        }
      }
    return nPara;
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromViewCursorOrDialog: Paragraph not found: return -1" + OfficeTools.LOG_LINE_BREAK);
    }
    if (debugModeTm) {
      long runTime = System.currentTimeMillis() - startTime;
      if (runTime > OfficeTools.TIME_TOLERANCE) {
        MessageHandler.printToLogFile("Time to run getParaFromViewCursorOrDialog: " + runTime);
      }
    }
    return -1;
  }

  /**
   * Handle the changed document cache
   * adjust the result cache to changes
   */
  private boolean handleCacheChanges() {
    if (useQueue) {
      mDocHandler.getTextLevelCheckQueue().interruptCheck(docID, true);
    }
    ChangedRange changed = docCache.refreshAndCompare(singleDocument, LinguisticServices.getLocale(fixedLanguage), 
        LinguisticServices.getLocale(docLanguage), xComponent, 5);
    if (changed == null || isDisposed()) {
      return false;
    }
    changeFrom = changed.from - numParasToChange;
    changeTo = changed.to + numParasToChange;
    singleDocument.removeAndShiftIgnoredMatch(changed.from, changed.to, changed.oldSize, changed.newSize);
    if (debugMode > 0) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: handleCacheChanges: Changed paragraphs: from:" + changed.from + ", to: " + changed.to);
    }
    if(!isDisposed()) {
      for (ResultCache cache : paragraphsCache) {
        cache.removeAndShift(changed.from, changed.to, changed.oldSize, changed.newSize);
      }
      if (useQueue) {
        if (debugMode > 0) {
          MessageHandler.printToLogFile("CheckRequestAnalysis: handleCacheChanges: Number of Paragraphs has changed: new: " + changed.newSize 
          + ",  old: " + changed.oldSize + ", docID: " + docID);
          if (changed.to - changed.from > 1) {
            MessageHandler.printToLogFile("CheckRequestAnalysis: handleCacheChanges: Number of Paragraphs has changed: Difference from " 
                + changed.from + " to " + changed.to);
            MessageHandler.printToLogFile("CheckRequestAnalysis: handleCacheChanges: Old Cache size: " + changed.oldSize);
            MessageHandler.printToLogFile("CheckRequestAnalysis: handleCacheChanges: new docCache(from): '" 
                + docCache.getFlatParagraph(changed.from) + "'");
          }
        }
        for (int i = 0; i < minToCheckPara.size(); i++) {
          if (minToCheckPara.get(i) != 0) {
            if (changed.newSize - changed.oldSize > 0) {
              for (int n = changed.from; n < changed.to; n++) {
                docCache.setSingleParagraphsCacheToNull(n, paragraphsCache);
                singleDocument.addQueueEntry(n, i, minToCheckPara.get(i), docID, true);
              }
            } else if (changed.newSize - changed.oldSize < 0) {
              for (int n = changed.from; n < changed.from + 1; n++) {
                singleDocument.addQueueEntry(n, i, minToCheckPara.get(i), docID, true);
              }
            }
          }
        }
        mDocHandler.getTextLevelCheckQueue().wakeupQueue(docID);;
      }
    }
    return true;
  }
  
  /**
   * correct the changes in number of paragraph (added or removed paragraphs)
   * returns Flat paragraph number
   * returns -1 if the tested paragraph should be tested for view cursor position
   */
  private int changesInNumberOfParagraph(boolean getCurNum) {
    // Test if Size of allParas is correct; Reset if not
    long startTime = 0;
    if (debugModeTm) {
      startTime = System.currentTimeMillis();
    }
    if (docCache.isEmpty() || isDisposed()) {
      return -1;
    }
    int nPara = 0;
    long startTime1 = 0;
    FlatParagraphTools flatPara = singleDocument.getFlatParagraphTools();
    if (getCurNum) {
      if (debugModeTm) {
        startTime1 = System.currentTimeMillis();
      }
      nPara = flatPara.getCurNumFlatParagraph();
      if (debugModeTm) {
        long runTime = System.currentTimeMillis() - startTime1;
        if (runTime > OfficeTools.TIME_TOLERANCE) {
          MessageHandler.printToLogFile("Time to run changesInNumberOfParagraph (getCurNumFlatParagraph): " + runTime);
        }
      }
      if (nPara < 0) {
        if (debugModeTm) {
          long runTime = System.currentTimeMillis() - startTime;
          if (runTime > OfficeTools.TIME_TOLERANCE) {
            MessageHandler.printToLogFile("Time to run changesInNumberOfParagraph (return -2): " + runTime);
          }
        }
        return -2;
      }
    }
    if (debugModeTm) {
      startTime1 = System.currentTimeMillis();
    }
    int nFParas = flatPara.getNumberOfAllFlatPara();
//    boolean isEqualCache = docCache.isEqualCacheSize(singleDocument.getDocumentCursorTools());
    if (debugModeTm) {
      long runTime = System.currentTimeMillis() - startTime1;
      if (runTime > OfficeTools.TIME_TOLERANCE) {
        MessageHandler.printToLogFile("Time to run changesInNumberOfParagraph (docCache.isEqualCacheSize): " + runTime);
      }
    }
    if (nFParas == docCache.size()) {
//    if (isEqualCache) {
      if (debugModeTm) {
        long runTime = System.currentTimeMillis() - startTime;
        if (runTime > OfficeTools.TIME_TOLERANCE) {
          MessageHandler.printToLogFile("Time to run changesInNumberOfParagraph (no change): " + runTime);
        }
      }
      return nPara;
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: changesInNumberOfParagraph: resetAllParas: docCache.size: " + docCache.size() + ", nPara: " + nPara
              + ", docID: " + docID + OfficeTools.LOG_LINE_BREAK);
    }
    if (isDisposed()) {
      return -1;
    }
    if (!handleCacheChanges() || isDisposed()) {
      return -1;
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: changesInNumberOfParagraph: Number FlatParagraphs: " + docCache.size() + "; docID: " + docID);
    }
    if (nPara >= docCache.size()) {
      nPara = flatPara.getCurNumFlatParagraph();
      if (nPara < 0 || nPara >= docCache.size()) {
        if (debugModeTm) {
          long runTime = System.currentTimeMillis() - startTime;
          if (runTime > OfficeTools.TIME_TOLERANCE) {
            MessageHandler.printToLogFile("Time to run changesInNumberOfParagraph (return -1): " + runTime);
          }
        }
        return -1;
      }
    }
    if (getCurNum) {
      textIsChanged = true;
    }
    if (debugModeTm) {
      long runTime = System.currentTimeMillis() - startTime;
      if (runTime > OfficeTools.TIME_TOLERANCE) {
        MessageHandler.printToLogFile("Time to run changesInNumberOfParagraph (cache has changed): " + runTime);
      }
    }
    return nPara;
  }
  
  /**
   * Handle caches for changed paragraph
   */
  private void handleChangedPara(int nPara, String chPara, Locale locale, int[] footnotePos, List<Integer> deletedChars) {
    if (debugMode > 0) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: handleChangedPara: flat praragraph changed: nPara: " + nPara + "; docID: " + docID
              + "; locale: isMultilingual: " + docCache.isMultilingualFlatParagraph(nPara) 
              + "; old: " + OfficeTools.localeToString(docCache.getFlatParagraphLocale(nPara))
              + "; new: " + OfficeTools.localeToString(locale) + OfficeTools.LOG_LINE_BREAK
              + "old: " + docCache.getFlatParagraph(nPara) + OfficeTools.LOG_LINE_BREAK 
              + "new: " + chPara + OfficeTools.LOG_LINE_BREAK);
    }
//    boolean checkOnlyPara = (docCache.getFlatParagraph(nPara).isEmpty() ? false : true);
    docCache.setFlatParagraph(nPara, chPara, locale);
    docCache.setFlatParagraphFootnotes(nPara, footnotePos);
    docCache.setFlatParagraphDeletedCharacters(nPara, deletedChars);
//    mDocHandler.handleLtDictionary(chPara, locale);
    if (useQueue) {
      changedParas.put(nPara, chPara);
      singleDocument.removeResultCache(nPara, true);
      for (int i = 1; i < minToCheckPara.size(); i++) {
//        singleDocument.addQueueEntry(nPara, i, minToCheckPara.get(i), docID, checkOnlyPara, numLastFlPara.get(numLastFlPara.size() - 1) < 0 ? false : true);
        singleDocument.addQueueEntry(nPara, i, minToCheckPara.get(i), docID, numLastFlPara.get(numLastFlPara.size() - 1) < 0 ? false : true);
      }
    } else {
      singleDocument.removeResultCache(nPara, true);
    }
    textIsChanged = true;
    changeFrom = nPara - numParasToChange;
    changeTo = nPara + numParasToChange + 1;
    singleDocument.removeIgnoredMatch(nPara, false);
  }
  
  
  /**
   * find position from changed paragraph
   */
  private int getPosFromChangedPara(String chPara, Locale locale, int nPara, int[] footnotePos) {
    if (docCache.isEmpty() || nPara < 0 || isDisposed()) {
      return -1;
    }
    long startTime = 0;
    if (debugModeTm) {
      startTime = System.currentTimeMillis();
    }
    TextParagraph tPara = docCache.getNumberOfTextParagraph(nPara);
    DocumentCursorTools docCursor = singleDocument.getDocumentCursorTools();
    List<Integer> deletedChars = docCursor.getDeletedCharactersOfTextParagraph(tPara);
    
    if (!docCache.isEqual(nPara, chPara, locale, deletedChars)) {
      handleChangedPara(nPara, chPara, locale, footnotePos, deletedChars);
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: getPosFromChangedPara: Number of Paragraph: " + nPara + OfficeTools.LOG_LINE_BREAK);
    }
    if (tPara != null && tPara.type != DocumentCache.CURSOR_TYPE_UNKNOWN && tPara.number >= 0) {
      numLastFlPara.set(tPara.type, tPara.number);
    }
    numLastFlPara.set(numLastFlPara.size() - 1, nPara);  //  Note: This is the number of flat paragraph
    if (debugModeTm) {
      long runTime = System.currentTimeMillis() - startTime;
      if (runTime > OfficeTools.TIME_TOLERANCE) {
        MessageHandler.printToLogFile("Time to run getPosFromChangedPara: " + runTime);
      }
    }
    return nPara;
  }
  
  /**
   * Heuristic try to find next position (automatic iteration)
   * Is paragraph same, next not empty after or before   
   */
  private int findNextParaPos(String paraStr, Locale locale, int startPos) {
    if (docCache.size() < 1 || isDisposed()) {
      return -1;
    }
    int startPara = numLastFlPara.get(numLastFlPara.size() - 1);
    if (startPos > 0 && startPara >= 0) {
      if (startPara >= 0 && startPara < docCache.size() && docCache.isEqual(startPara, paraStr, locale)) {
        return startPara;
      }
    } else if (startPos == 0) {
      startPara = startPara >= docCache.size() ? 0 : startPara + 1;
      if (startPara >= 0 && startPara < docCache.size() && docCache.isEqual(startPara, paraStr, locale)) {
        TextParagraph tPara = docCache.getNumberOfTextParagraph(startPara);
        if (tPara != null && tPara.type != DocumentCache.CURSOR_TYPE_UNKNOWN && tPara.number >= 0) {
          numLastFlPara.set(tPara.type, tPara.number);
          numLastFlPara.set(numLastFlPara.size() - 1, startPara);
        }
        return startPara;
      }
      //  check if paragraph is next shape, text or table
      for (int type = 3; type < numLastFlPara.size() - 1; type++) {
        int docPara = numLastFlPara.get(type);
        docPara = docPara >= docCache.textSize(type) ? 0 : docPara + 1;
        startPara = docCache.getFlatParagraphNumber(new TextParagraph(type, docPara));
        if (startPara >= 0 && startPara < docCache.size() && docCache.isEqual(startPara, paraStr, locale)) {
          numLastFlPara.set(type, docPara);
          numLastFlPara.set(numLastFlPara.size() - 1, startPara);
          return startPara;
        }
      }
      //  check if paragraph is a end- or foornote or a header or footer
      for (int type = 0; type < 3; type++) {
        for(int docPara = numLastFlPara.get(type) + 1; docPara < docCache.textSize(type); docPara++) {
          startPara = docCache.getFlatParagraphNumber(new TextParagraph(type, docPara));
          if (startPara >= 0 && startPara < docCache.size() && docCache.isEqual(startPara, paraStr, locale)) {
            numLastFlPara.set(type, docPara);
//            numLastFlPara.set(numLastFlPara.size() - 1, startPara);
            return startPara;
          }
        }
        for(int docPara = 0; docPara < numLastFlPara.get(type); docPara++) {
          startPara = docCache.getFlatParagraphNumber(new TextParagraph(type, docPara));
          if (startPara >= 0 && startPara < docCache.size() && docCache.isEqual(startPara, paraStr, locale)) {
            numLastFlPara.set(type, docPara);
//            numLastFlPara.set(numLastFlPara.size() - 1, startPara);
            return startPara;
          }
        }
      }
    }
    return -1; 
  }

}
