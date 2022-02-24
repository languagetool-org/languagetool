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
import org.languagetool.openoffice.DocumentCache.TextParagraph;
import org.languagetool.openoffice.OfficeTools.DocumentType;

import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XFlatParagraph;
import com.sun.star.uno.XComponentContext;

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
  
  private final int numParasToCheck;                //  current number of Paragraphs to be checked

  private final XComponentContext xContext;         //  The context of the document
  private final XComponent xComponent;              //  XComponent of the open document
  private final String docID;                       //  docID of the document
  private final MultiDocumentsHandler mDocHandler;  //  handles the different documents loaded in LO/OO
  private final SingleDocument singleDocument;      //  handles one document
  private final List<Integer> minToCheckPara;       //  List of minimal to check paragraphs for different classes of text level rules
  private final Language docLanguage;               //  fixed language (by configuration); if null: use language of document (given by LO/OO)
  private final boolean useQueue;                   //  true: use queue to check text level rules (given by configuration)
  private final DocumentType docType;               //  save the type of document
  private final int proofInfo;                      //  Information about proof request (supported by LO > 6.4 otherwise: 0 == UNKNOWN)
  private final DocumentCache docCache;             //  cache of paragraphs (only readable by parallel thread)
  private final Map<Integer, String> changedParas;  //  Map of last changed paragraphs;
  private final List<ResultCache> paragraphsCache;  //  Cache for matches of text rules

  private FlatParagraphTools flatPara;              //  Save information for flat paragraphs (including iterator and iterator provider) for the single document
  private ViewCursorTools viewCursor;               //  Get the view cursor for desktop
  private int numLastVCPara;                        //  Save position of ViewCursor for the single documents
  private int numLastFlPara;                        //  Save position of FlatParagraph for the single documents
  private DocumentCursorTools docCursor = null;     //  Save document cursor for the single document
  private int changeFrom = 0;                       //  Change result cache from paragraph
  private int changeTo = 0;                         //  Change result cache to paragraph
  private boolean textIsChanged = false;            //  true: check number of paragraphs again
  private int numParasToChange = -1;                //  Number of paragraphs to change for n-paragraph cache
  private int paraNum;                              //  Number of current checked paragraph

  CheckRequestAnalysis(int numLastVCPara, int numLastFlPara, int proofInfo, int numParasToCheck,
      SingleDocument singleDocument, List<ResultCache> paragraphsCache, ViewCursorTools viewCursor, Map<Integer, String> changedParas) {
    debugMode = OfficeTools.DEBUG_MODE_CR;
    this.singleDocument = singleDocument;
    this.viewCursor = viewCursor;
    this.numLastVCPara = numLastVCPara;
    this.numLastFlPara = numLastFlPara;
    this.proofInfo = proofInfo;
    this.paragraphsCache = paragraphsCache;
    this.changedParas = changedParas;
    mDocHandler = singleDocument.getMultiDocumentsHandler();
    xContext = mDocHandler.getContext();
    xComponent = singleDocument.getXComponent();
    docID = singleDocument.getDocID();
    docType = singleDocument.getDocumentType();
    minToCheckPara = mDocHandler.getNumMinToCheckParas();
    docCache = singleDocument.getDocumentCache();
    flatPara = singleDocument.getFlatParagraphTools();
    Configuration config = mDocHandler.getConfiguration();
    docLanguage = config.getUseDocLanguage() ? null : singleDocument.getLanguage();
    this.numParasToCheck = mDocHandler.isTestMode() ? 0 : numParasToCheck;
    useQueue = (numParasToCheck != 0 && proofInfo != OfficeTools.PROOFINFO_GET_PROOFRESULT && config.useTextLevelQueue());
    for (int minPara : minToCheckPara) {
      if (minPara > numParasToChange) {
        numParasToChange = minPara;
      }
    }
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
    setFlatParagraphTools(xComponent);
    if (docCache.isEmpty()) {
      docCursor = new DocumentCursorTools(xComponent);
      docCache.refresh(docCursor, flatPara, docLanguage != null ? LinguisticServices.getLocale(docLanguage) : null, xComponent, 1);
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
    XFlatParagraph xFlatPara = flatPara.getFlatParagraphAt(nPara);
    if (isDisposed() || xFlatPara == null) {
      return;
    }
    String chPara = xFlatPara.getText();
    Locale docLocale = docLanguage == null ? null : LinguisticServices.getLocale(docLanguage);
    Locale lastLocale = nPara <= 0 ? null : docCache.getFlatParagraphLocale(nPara - 1);
    try {
     Locale locale = FlatParagraphTools.getPrimaryParagraphLanguage(xFlatPara, 0, chPara.length(), docLocale, lastLocale, false);
      if (!docCache.isEqual(nPara, chPara, locale)) {
        if (debugMode > 1) {
          MessageHandler.printToLogFile("ICheckRequestAnalysis: actualizeDocumentCache: Paragraph has changed:\nold:" 
              + chPara + "\nnew:" + docCache.getFlatParagraph(nPara));
        }
        docCache.setFlatParagraph(nPara, chPara, locale);
        removeResultCache(nPara);
        singleDocument.removeIgnoredMatch(nPara, isIntern);
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
    }
  }
  
  /**
   * correct the changes of document cache
   * add incorrect paragraphs to check queue
   * returns the first incorrectFlat paragraph number
   * returns -1 if there is no change in document cache
   *//*
   *  TODO: Remove after Tests
  int changesInDocumentCache () {
    //  Return -1, if there is no initialized docCache
    if (docCache.isEmpty() || isDisposed()) {
      return -1;
    }
    DocumentCache oldDocCache = new DocumentCache(docCache);
    //  Return -1, if there is no initialized docCache
    if (isDisposed()) {
      return -1;
    }
    setFlatParagraphTools(xComponent);
    int nFirstPara = -1;
    if (docCursor == null) {
      docCursor = new DocumentCursorTools(xComponent);
    }
    docCache.refresh(docCursor, flatPara, docLanguage != null ? LinguisticServices.getLocale(docLanguage) : null, xComponent, 2);
    if (docCache.isEmpty()) {
      return -1;
    }
    if (!isDisposed() && docCache.size() != oldDocCache.size()) {
      int from = 0;
      int to = 1;
      // to prevent spontaneous recheck of nearly the whole text
      // the change of text contents has to be checked first
      // ignore headers and footers and the change of function inside of them
      while (from < docCache.size() && from < oldDocCache.size() 
          && (docCache.getNumberOfTextParagraph(from).type == DocumentCache.CURSOR_TYPE_HEADER_FOOTER
          || docCache.getFlatParagraph(from).equals(oldDocCache.getFlatParagraph(from)))) {
        from++;
      }
      boolean isTextChange = from < docCache.size() && from < oldDocCache.size();
      if (isTextChange) {
        // if change in text is found check the number of text paragraphs which have changed
        while (to <= docCache.size() && to <= oldDocCache.size()
            && (docCache.getNumberOfTextParagraph(docCache.size() - to).type == DocumentCache.CURSOR_TYPE_HEADER_FOOTER
            || docCache.getFlatParagraph(docCache.size() - to).equals(
                    oldDocCache.getFlatParagraph(oldDocCache.size() - to)))) {
          to++;
        }
        to = docCache.size() - to;
        if (to < 0) {
          to = 0;
        }
      } else {
        // if no change in text is found check the number of flat paragraphs which have changed
        from = 0;
        while (from < docCache.size() && from < oldDocCache.size()
            && (docCache.getNumberOfTextParagraph(from).type != DocumentCache.CURSOR_TYPE_HEADER_FOOTER
            || docCache.getFlatParagraph(from).equals(oldDocCache.getFlatParagraph(from)))) {
          from++;
        }
        while (to <= docCache.size() && to <= oldDocCache.size()
            && (docCache.getNumberOfTextParagraph(docCache.size() - to).type != DocumentCache.CURSOR_TYPE_HEADER_FOOTER
            || docCache.getFlatParagraph(docCache.size() - to).equals(
                    oldDocCache.getFlatParagraph(oldDocCache.size() - to)))) {
          to++;
        }
        to = docCache.size() - to;
      }
      changeFrom = from - numParasToChange;
      changeTo = to + numParasToChange + 1;
      singleDocument.removeAndShiftIgnoredMatch(from, to, oldDocCache.size(), docCache.size());
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: changesInDocumentCache: Changed paragraphs: from:" + from + ", to: " + to);
      }
      for (ResultCache cache : paragraphsCache) {
        cache.removeAndShift(from, to, docCache.size() - oldDocCache.size());
      }
      if (!isDisposed() && useQueue && isTextChange) {
        if (debugMode > 0) {
          MessageHandler.printToLogFile("CheckRequestAnalysis: changesInDocumentCache: Number of Paragraphs has changed: new: " + docCache.size() 
          + ",  old: " + oldDocCache.size()+ ", docID: " + docID);
          if (to - from > 1) {
            MessageHandler.printToLogFile("CheckRequestAnalysis: changesInDocumentCache: Number of Paragraphs has changed: Difference from " + from + " to " + to);
            MessageHandler.printToLogFile("CheckRequestAnalysis: changesInDocumentCache: Old Cache size: " + oldDocCache.size());
            MessageHandler.printToLogFile("CheckRequestAnalysis: changesInDocumentCache: new docCache(from): '" + docCache.getFlatParagraph(from) + "'");
            if (from < oldDocCache.size()) {
              MessageHandler.printToLogFile("CheckRequestAnalysis: changesInDocumentCache: old docCache(from): '" + oldDocCache.getFlatParagraph(from) + "'");
            }
            MessageHandler.printToLogFile("CheckRequestAnalysis: changesInDocumentCache: new docCache(to): '" + docCache.getFlatParagraph(to) + "'");
            if (to < oldDocCache.size()) {
              MessageHandler.printToLogFile("CheckRequestAnalysis: changesInDocumentCache: old docCache(to): '" + oldDocCache.getFlatParagraph(to) + "'");
            }
          }
        }
        for (int i = 0; i < minToCheckPara.size(); i++) {
          if (minToCheckPara.get(i) != 0 || nFirstPara >= 0) {
            for (int n = from; n <= to; n++) {
              singleDocument.addQueueEntry(n, i, minToCheckPara.get(i), docID, false, true);
            }
          } else {
            nFirstPara = from;
          }
        }
      }
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: changesInDocumentCache: Cache size changed: from = " + from + "; to = " + to + "; docID: " + docID);
      }
    } else if (isDisposed() && useQueue){
      for (int n = 0; n < docCache.size(); n++) {
        int type = docCache.getNumberOfTextParagraph(n).type;
        if (type != DocumentCache.CURSOR_TYPE_UNKNOWN && !docCache.getFlatParagraph(n).equals(oldDocCache.getFlatParagraph(n))) {
          for (int i = 0; i < minToCheckPara.size(); i++) {
            if (minToCheckPara.get(i) != 0 || nFirstPara >= 0) {
              singleDocument.addQueueEntry(n, i, minToCheckPara.get(i), docID, false, true);
            } else {
              nFirstPara = n;
            }
          }
          if (debugMode > 0) {
            MessageHandler.printToLogFile("CheckRequestAnalysis: changesInDocumentCache: FlatParagraph(" + n + ") has changed; docID: " + docID);
          }
        }
      }
    }
    return nFirstPara;
  }
*/  
  /** 
   * Get new initialized flat paragraph tools
   */
  FlatParagraphTools getFlatParagraphTools() {
    if (flatPara == null) {
      flatPara = new FlatParagraphTools(xComponent);
    }
    return flatPara;
  }
  
  /** 
   * Get new initialized view cursor tools
   */
  ViewCursorTools getViewCursorTools() {
    return viewCursor;
  }
  
  /** 
   * Get new initialized document cursor tools
   */
  DocumentCursorTools getDocumentCursorTools() {
    return docCursor;
  }

  /** 
   * Get last number of paragraph from view cursor
   */
  int getLastParaNumFromViewCursor() {
    return numLastVCPara;
  }
  
  /** 
   * Get last number of paragraph from flat paragraph
   */
  int getLastParaNumFromFlatParagraph() {
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
   */
  private void setFlatParagraphTools(XComponent xComponent) {
    if (flatPara == null) {
      flatPara = new FlatParagraphTools(xComponent);
    } else {
      flatPara.init();
    }
  }
  
  /**
   * Search for Position of Paragraph
   * gives Back the Position of flat paragraph / -1 if Paragraph can not be found
   */
  private int getParaPos(int nPara, String chPara, Locale locale, int startPos, int[] footnotePositions) {
    
    if (isDisposed()) {
      return -1;
    }
    if (docType != DocumentType.WRITER && docCache.isEmpty()) {
      docCache.refresh(docCursor, flatPara, docLanguage != null ? LinguisticServices.getLocale(docLanguage) : null, xComponent, 3);
    }

    if (nPara >= 0) {
      return nPara;
    }

    if (numParasToCheck == 0 || xComponent == null) {
      return -1;  //  check only the processed paragraph
    }

    // Initialization 
    docCursor = null;
    setFlatParagraphTools(xComponent);

    if (docCache.isEmpty()) {
      docCursor = new DocumentCursorTools(xComponent);
      docCache.refresh(docCursor, flatPara, docLanguage != null ? LinguisticServices.getLocale(docLanguage) : null, xComponent, 4);
      if (debugMode > 0) {
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
    
    if (proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT) {
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
    // try to get next position from last FlatParagraph position (for performance reasons)
    if (startPos != 0 && proofInfo == OfficeTools.PROOFINFO_MARK_PARAGRAPH) {
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromFlatparagraph: Number of Paragraph: " + numLastFlPara 
            + " (proofInfo == " + OfficeTools.PROOFINFO_MARK_PARAGRAPH + ")" + OfficeTools.LOG_LINE_BREAK);
      }
      return numLastFlPara;
    }
    int nPara = findNextParaPos(numLastFlPara, chPara, locale, startPos);
    if (nPara >= 0) {
      numLastFlPara = nPara;
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromFlatparagraph: Number of Paragraph: " + nPara 
            + ", start: " + startPos + OfficeTools.LOG_LINE_BREAK);
      }
      return nPara;
    }
    
    // number of paragraphs has changed? --> Update the internal information
    nPara = changesInNumberOfParagraph(true);
    if (nPara < 0) {
      if (proofInfo == OfficeTools.PROOFINFO_UNKNOWN) {
        //  problem with automatic iteration - try to get ViewCursor position
        return getParaFromViewCursorOrDialog(chPara, locale, footnotePositions);
      } else {
        return nPara;
      }
    }
    if (isDisposed()) {
      return -1;
    }
    TextParagraph nTPara = docCache.getNumberOfTextParagraph(nPara); 
    if (proofInfo == OfficeTools.PROOFINFO_MARK_PARAGRAPH) {
      if (nTPara.type == DocumentCache.CURSOR_TYPE_UNKNOWN) {
        return getPosFromChangedPara(chPara, locale, nPara, footnotePositions);
      }
    }
    if (!isDisposed()) {
      String curFlatParaText = flatPara.getCurrentParaText();
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromFlatparagraph: curFlatParaText: " + curFlatParaText + OfficeTools.LOG_LINE_BREAK
            + "chPara: " + chPara + OfficeTools.LOG_LINE_BREAK + "getFlatParagraph: " + docCache.getFlatParagraph(nPara) + OfficeTools.LOG_LINE_BREAK);
      }
      if (proofInfo == OfficeTools.PROOFINFO_UNKNOWN) {
        if (curFlatParaText != null && !curFlatParaText.equals(chPara) && curFlatParaText.equals(docCache.getFlatParagraph(nPara))) {
          //  wrong flat paragraph - try to get ViewCursor position
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
            numLastFlPara = n;
            if (debugMode > 0) {
              MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromFlatparagraph: From document cache: Number of Paragraph: " + n 
                  + ", start: " + startPos + OfficeTools.LOG_LINE_BREAK);
            }
            textIsChanged = true;
            return n;
          }
        }
      }
    }
    // find position from changed paragraph
    return getPosFromChangedPara(chPara, locale, nPara, footnotePositions);
  }

  /**
   * remove all cached matches for one paragraph
   */
  public void removeResultCache(int nPara) {
    if (!isDisposed()) {
      for (ResultCache cache : paragraphsCache) {
        cache.remove(nPara);
      }
    }
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
   * Get the Position of Paragraph if result is ordered by right mouse click or spelling dialog
   * returns -1 if it fails
   */
  private int getParaFromViewCursorOrDialog(String chParaWithFootnotes, Locale locale, int[] footnotePositions) {
    // try to get ViewCursor position (proof initiated by mouse click)
    if (docCache.isEmpty() || isDisposed()) {
      return -1;
    }
    if (viewCursor == null) {
      viewCursor = new ViewCursorTools(xContext);
    }
    int nPara;
    String vcText = SingleCheck.removeFootnotes(viewCursor.getViewCursorParagraphText(), footnotePositions);
    String chPara = SingleCheck.removeFootnotes(chParaWithFootnotes, footnotePositions);
    if (chPara.equals(vcText)) {
      TextParagraph tPara = viewCursor.getViewCursorParagraph();
      if (tPara != null && tPara.type != DocumentCache.CURSOR_TYPE_UNKNOWN) {
        nPara = docCache.getFlatParagraphNumber(tPara);
        numLastVCPara = nPara;
        if(!docCache.isEqual(nPara, chParaWithFootnotes, locale)) {
          actualizeDocumentCache(nPara, false);
          String dcText = SingleCheck.removeFootnotes(docCache.getFlatParagraph(nPara), footnotePositions);
          if (!dcText.equals(chPara)) {
            if (debugMode > 0) {
              MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromViewCursorOrDialog: cText != chPara: Number of Paragraph: " + nPara);
            }
            return -1;
          }
          textIsChanged = true;
        }
        if (debugMode > 0) {
          MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromViewCursorOrDialog: Number of Paragraph: " + nPara + OfficeTools.LOG_LINE_BREAK);
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
    return nPara;
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: getParaFromViewCursorOrDialog: Paragraph not found: return -1" + OfficeTools.LOG_LINE_BREAK);
    }
    return -1;
  }
  
  /**
   * correct the changes in number of paragraph (added or removed paragraphs)
   * returns Flat paragraph number
   * returns -1 if the tested paragraph should be tested for view cursor position
   */
  private int changesInNumberOfParagraph(boolean getCurNum) {
    // Test if Size of allParas is correct; Reset if not
    if (docCache.isEmpty() || isDisposed()) {
      return -1;
    }
    setFlatParagraphTools(xComponent);
    int nPara = 0;
    if (getCurNum) {
      nPara = flatPara.getCurNumFlatParagraph();
      if (nPara < 0) {
        return -2;
      }
    }
    int nFParas = flatPara.getNumberOfAllFlatPara();
    if (nFParas == docCache.size()) {
      return nPara;
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: changesInNumberOfParagraph: resetAllParas: docCache.size: " + docCache.size() + ", nPara: " + nPara
              + ", docID: " + docID + OfficeTools.LOG_LINE_BREAK);
    }
    if (isDisposed()) {
      return -1;
    }
    DocumentCache oldDocCache = new DocumentCache(docCache);
    if (useQueue) {
      mDocHandler.getTextLevelCheckQueue().interruptCheck(docID, true);
    }
    if (docCursor == null) {
      docCursor = new DocumentCursorTools(xComponent);
    }
    docCache.refresh(docCursor, flatPara, docLanguage != null ? LinguisticServices.getLocale(docLanguage) : null, xComponent, 5);
    if (docCache.isEmpty() || isDisposed()) {
      return -1;
    }
    int from = 0;
    int to = 1;
    // to prevent spontaneous recheck of nearly the whole text
    // the change of text contents has to be checked first
    // ignore headers and footers and the change of function inside of them
    while (from < docCache.size() && from < oldDocCache.size()
        && (docCache.getNumberOfTextParagraph(from).type == DocumentCache.CURSOR_TYPE_HEADER_FOOTER
        || docCache.getFlatParagraph(from).equals(oldDocCache.getFlatParagraph(from)))) {
      from++;
    }
    boolean isTextChange = from < docCache.size() && from < oldDocCache.size();
    if (isTextChange) {
      // if change in text is found check the number of text paragraphs which have changed
      while (to <= docCache.size() && to <= oldDocCache.size()
          && (docCache.getNumberOfTextParagraph(docCache.size() - to).type == DocumentCache.CURSOR_TYPE_HEADER_FOOTER
          || docCache.getFlatParagraph(docCache.size() - to).equals(
                  oldDocCache.getFlatParagraph(oldDocCache.size() - to)))) {
        to++;
      }
      to = docCache.size() - to + 1;
      if (to < 0) {
        to = 0;
      }
    } else {
      // if no change in text is found check the number of header and footer paragraphs which have changed
      while (from < docCache.size() && from < oldDocCache.size()
          && (docCache.getNumberOfTextParagraph(from).type != DocumentCache.CURSOR_TYPE_HEADER_FOOTER
          || docCache.getFlatParagraph(from).equals(oldDocCache.getFlatParagraph(from)))) {
        from++;
      }
      while (to <= docCache.size() && to <= oldDocCache.size()
          && (docCache.getNumberOfTextParagraph(docCache.size() - to).type != DocumentCache.CURSOR_TYPE_HEADER_FOOTER
          || docCache.getFlatParagraph(docCache.size() - to).equals(
                  oldDocCache.getFlatParagraph(oldDocCache.size() - to)))) {
        to++;
      }
      to = docCache.size() - to + 1;
    }
    changeFrom = from - numParasToChange;
    changeTo = to + numParasToChange;
    singleDocument.removeAndShiftIgnoredMatch(from, to, oldDocCache.size(), docCache.size());
    if (debugMode > 0) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: changesInNumberOfParagraph: Changed paragraphs: from:" + from + ", to: " + to);
    }
    if(!isDisposed()) {
      for (ResultCache cache : paragraphsCache) {
        cache.removeAndShift(from, to, docCache.size() - oldDocCache.size());
      }
      if (useQueue && isTextChange) {
        if (debugMode > 0) {
          MessageHandler.printToLogFile("CheckRequestAnalysis: changesInNumberOfParagraph: Number of Paragraphs has changed: new: " + docCache.size() 
          + ",  old: " + oldDocCache.size()+ ", docID: " + docID);
          if (to - from > 1) {
            MessageHandler.printToLogFile("CheckRequestAnalysis: changesInNumberOfParagraph: Number of Paragraphs has changed: Difference from " + from + " to " + to);
            MessageHandler.printToLogFile("CheckRequestAnalysis: changesInNumberOfParagraph: Old Cache size: " + oldDocCache.size());
            MessageHandler.printToLogFile("CheckRequestAnalysis: changesInNumberOfParagraph: new docCache(from): '" + docCache.getFlatParagraph(from) + "'");
            if (from < oldDocCache.size()) {
              MessageHandler.printToLogFile("CheckRequestAnalysis: changesInNumberOfParagraph: old docCache(from): '" + oldDocCache.getFlatParagraph(from) + "'");
            }
            MessageHandler.printToLogFile("CheckRequestAnalysis: changesInNumberOfParagraph: new docCache(to): '" + docCache.getFlatParagraph(to) + "'");
            if (to < oldDocCache.size()) {
              MessageHandler.printToLogFile("CheckRequestAnalysis: changesInNumberOfParagraph: old docCache(to): '" + oldDocCache.getFlatParagraph(to) + "'");
            }
          }
        }
        for (int i = 0; i < minToCheckPara.size(); i++) {
          if (minToCheckPara.get(i) != 0) {
            for (int n = from; n < to; n++) {
              singleDocument.addQueueEntry(n, i, minToCheckPara.get(i), docID, false, true);
            }
          }
        }
      }
      //  set divNum (difference between doc cursor text and flat paragraphs (is number of footnotes etc.)
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: changesInNumberOfParagraph: Number FlatParagraphs: " + nFParas + "; docID: " + docID);
      }
    }
    if (isDisposed() || nFParas < docCache.size()) {
      return -1;   // try to get ViewCursor position for proof info unknown
    }
    if (nPara >= docCache.size()) {
      nPara = flatPara.getCurNumFlatParagraph();
      if (nPara < 0 || nPara >= docCache.size()) {
        return -1;
      }
    }
    if (getCurNum) {
      textIsChanged = true;
    }
    return nPara;
  }
  
  /**
   * find position from changed paragraph
   */
  private int getPosFromChangedPara(String chPara, Locale locale, int nPara, int[] footnotePos) {
    if (docCache.isEmpty() || nPara < 0 || isDisposed()) {
      return -1;
    }
    if (!docCache.isEqual(nPara, chPara, locale)) {
      if (debugMode > 0) {
        MessageHandler.printToLogFile("CheckRequestAnalysis: getPosFromChangedPara: flat praragraph changed: nPara: " + nPara + "; docID: " + docID
                + "; locale: isMultilingual: " + docCache.isMultilingualFlatParagraph(nPara) 
                + "; old: " + OfficeTools.localeToString(docCache.getFlatParagraphLocale(nPara))
                + "; new: " + OfficeTools.localeToString(locale) + OfficeTools.LOG_LINE_BREAK
                + "old: " + docCache.getFlatParagraph(nPara) + OfficeTools.LOG_LINE_BREAK 
                + "new: " + chPara + OfficeTools.LOG_LINE_BREAK);
      }
      docCache.setFlatParagraph(nPara, chPara, locale);
      docCache.setFlatParagraphFootnotes(nPara, footnotePos);
      if (useQueue) {
        changedParas.put(nPara, chPara);
        for (int i = 0; i < minToCheckPara.size(); i++) {
          paragraphsCache.get(i).remove(nPara);
          if (minToCheckPara.get(i) > 0) {
            singleDocument.addQueueEntry(nPara, i, minToCheckPara.get(i), docID, true, numLastFlPara < 0 ? false : true);
          }
        }
      } else {
        for (ResultCache cache : paragraphsCache) {
          cache.remove(nPara);
        }
      }
      textIsChanged = true;
      changeFrom = nPara - numParasToChange;
      changeTo = nPara + numParasToChange + 1;
      singleDocument.removeIgnoredMatch(nPara, false);
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("CheckRequestAnalysis: getPosFromChangedPara: Number of Paragraph: " + nPara + OfficeTools.LOG_LINE_BREAK);
    }
    numLastFlPara = nPara;  //  Note: This is the number of flat paragraph
    return nPara;
  }
  
  /**
   * Heuristic try to find next position (automatic iteration)
   * Is paragraph same, next not empty after or before   
   */
  private int findNextParaPos(int startPara, String paraStr, Locale locale, int startPos) {
    if (docCache.size() < 1 || isDisposed()) {
      return -1;
    }
    if (startPos > 0 && numLastFlPara >= 0) {
      if (startPara >= 0 && startPara < docCache.size() && docCache.isEqual(startPara, paraStr, locale)) {
        return startPara;
      }
    } else if (startPos == 0) {
      startPara = startPara >= docCache.size() ? 0 : startPara + 1;
      if (startPara >= 0 && startPara < docCache.size() && docCache.isEqual(startPara, paraStr, locale)) {
        return startPara;
      }
    }
    return -1; 
  }

}
