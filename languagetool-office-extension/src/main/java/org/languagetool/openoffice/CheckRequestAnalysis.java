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

import org.languagetool.Language;
import org.languagetool.gui.Configuration;

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
  
  private final int defaultParaCheck;               //  will be overwritten by config
  private final int numParasToCheck;                //  current number of Paragraphs to be checked

  private final XComponentContext xContext;         //  The context of the document
  private final XComponent xComponent;              //  XComponent of the open document
  private final String docID;                       //  docID of the document
  private final MultiDocumentsHandler mDocHandler;  //  handles the different documents loaded in LO/OO
  private final SingleDocument singleDocument;      //  handles one document
  private final List<Integer> minToCheckPara;       //  List of minimal to check paragraphs for different classes of text level rules
  private final Language docLanguage;               //  fixed language (by configuration); if null: use language of document (given by LO/OO)
  private final boolean useQueue;                   //  true: use queue to check text level rules (given by configuration)
  private final int proofInfo;                      //  Information about proof request (supported by LO > 6.4 otherwise: 0 == UNKNOWN)

  private DocumentCache docCache;                   //  cache of paragraphs (only readable by parallel thread)
  private List<ResultCache> paragraphsCache;        //  Cache for matches of text rules
  private FlatParagraphTools flatPara;              //  Save information for flat paragraphs (including iterator and iterator provider) for the single document
  private ViewCursorTools viewCursor;               //  Get the view cursor for desktop
  private int numLastVCPara;                        //  Save position of ViewCursor for the single documents
  private int numLastFlPara;                        //  Save position of FlatParagraph for the single documents

  private DocumentCursorTools docCursor = null;     //  Save document cursor for the single document
  private int changeFrom = 0;                       //  Change result cache from paragraph
  private int changeTo = 0;                         //  Change result cache to paragraph
  private boolean textIsChanged = false;            //  false: check number of paragraphs again (ignored by parallel thread)
  private boolean resetCheck = false;               //  true: the whole text has to be checked again (use cache)
  private int numParasToChange = -1;                //  Number of paragraphs to change for n-paragraph cache
  private int paraNum;                              //  Number of current checked paragraph

  CheckRequestAnalysis(int numLastVCPara, int numLastFlPara, int defaultParaCheck, int proofInfo,
      SingleDocument singleDocument, List<ResultCache> paragraphsCache, ViewCursorTools viewCursor) {
    debugMode = OfficeTools.DEBUG_MODE_SD;
    this.singleDocument = singleDocument;
    this.viewCursor = viewCursor;
    this.numLastVCPara = numLastVCPara;
    this.numLastFlPara = numLastFlPara;
    this.defaultParaCheck = defaultParaCheck;
    this.proofInfo = proofInfo;
    this.paragraphsCache = paragraphsCache;
    mDocHandler = singleDocument.getMultiDocumentsHandler();
    xContext = mDocHandler.getContext();
    xComponent = singleDocument.getXComponent();
    docID = singleDocument.getDocID();
    minToCheckPara = mDocHandler.getNumMinToCheckParas();
    docCache = singleDocument.getDocumentCache();
    flatPara = singleDocument.getFlatParagraphTools();
    Configuration config = mDocHandler.getConfiguration();
    docLanguage = config.getUseDocLanguage() ? null : singleDocument.getLanguage();
    numParasToCheck = mDocHandler.isTestMode() ? 0 : config.getNumParasToCheck();
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
  int getNumberOfParagraph(int nPara, String chPara, Locale locale, int startPos) {
    paraNum = getParaPos(nPara, chPara, locale, startPos);
    if (docCache == null || paraNum >= docCache.size()) {
      paraNum = -1;
    }
    return paraNum;
  }
  
  /**
   * Actualize document cache and result cache for given paragraph number
   */
  DocumentCache actualizeDocumentCache (int nPara) {
    setFlatParagraphTools(xComponent);
    if (docCache == null) {
      docCursor = new DocumentCursorTools(xComponent);
      docCache = new DocumentCache(docCursor, flatPara, defaultParaCheck,
          docLanguage != null ? LinguisticServices.getLocale(docLanguage) : null);
      if (debugMode > 0) {
        MessageHandler.printToLogFile("+++ resetAllParas (docCache == null): docCache.size: " + docCache.size()
                + ", docID: " + docID + OfficeTools.LOG_LINE_BREAK);
      }
      if (docCache.isEmpty()) {
        return null;
      }
      singleDocument.setDocumentCache(docCache);
    } else {
      int nOldParas = docCache.size();
      changesInNumberOfParagraph(false);
      int numParas = docCache.size();
      if (numParas <= 0) {
        MessageHandler.printToLogFile("Internal request: docCache error!");
        return null;
      }
      resetCheck = true;
      textIsChanged = true;
      if (nOldParas != numParas) {
        if (debugMode > 1) {
          MessageHandler.printToLogFile("Internal request: Number of Paragraphs has changed: o:" +
              nOldParas + ", n:" + numParas);
        }
        return docCache;
      }
    }
    XFlatParagraph xFlatPara = flatPara.getFlatParagraphAt(nPara);
    String chPara = xFlatPara.getText();
    Locale docLocale = docLanguage == null ? null : LinguisticServices.getLocale(docLanguage);
    Locale lastLocale = nPara <= 0 ? null : docCache.getFlatParagraphLocale(nPara - 1);
    try {
     Locale locale = FlatParagraphTools.getPrimaryParagraphLanguage(xFlatPara, chPara.length(), docLocale, lastLocale);
      if (!docCache.isEqual(nPara, chPara, locale)) {
        if (debugMode > 1) {
          MessageHandler.printToLogFile("Internal request: Paragraph has changed:\no:" 
              + chPara + "\nn:" + docCache.getTextParagraph(nPara));
        }
        docCache.setFlatParagraph(nPara, chPara, locale);
        removeResultCache(nPara);
        singleDocument.removeIgnoredMatch(nPara);;
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
    }
    return docCache;
  }
  
  /** Get new initialized flat paragraph tools
   */
  FlatParagraphTools getFlatParagraphTools() {
    return flatPara;
  }
  
  /** Get new initialized view cursor tools
   */
  ViewCursorTools getViewCursorTools() {
    return viewCursor;
  }
  
  /** Get new initialized document cursor tools
   */
  DocumentCursorTools getDocumentCursorTools() {
    return docCursor;
  }

  /** Get last number of paragraph from view cursor
   */
  int getLastParaNumFromViewCursor() {
    return numLastVCPara;
  }
  
  /** Get last number of paragraph from flat paragraph
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

  /** check has to be reseted
   */
  boolean resetCheck() {
    return resetCheck;
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
  private int getParaPos(int nPara, String chPara, Locale locale, int startPos) {

    if (nPara >= 0) {
      return nPara;
    }

    if (numParasToCheck == 0 || xComponent == null) {
      return -1;  //  check only the processed paragraph
    }

    // Initialization 
    
    docCursor = null;
    setFlatParagraphTools(xComponent);

    if (docCache == null) {
      docCursor = new DocumentCursorTools(xComponent);
      docCache = new DocumentCache(docCursor, flatPara, defaultParaCheck,
          docLanguage != null ? LinguisticServices.getLocale(docLanguage) : null);
      if (debugMode > 0) {
        MessageHandler.printToLogFile("+++ resetAllParas (docCache == null): docCache.size: " + docCache.size()
                + ", docID: " + docID + OfficeTools.LOG_LINE_BREAK);
      }
      if (docCache.isEmpty()) {
        docCache = null;
        return -1;
      }
      singleDocument.setDocumentCache(docCache);
    }
    
    if (debugMode > 1) {
      MessageHandler.printToLogFile("proofInfo = " + proofInfo);
    }
    
    if (proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT) {
      return getParaFromViewCursorOrDialog(chPara, locale);
    }
    else {
      return getParaFromFlatparagraph(chPara, locale, startPos);
    }
    
  }
  
  /**
   * Search for Position of Paragraph if reason for proof is mark paragraph or no proof info
   * returns -1 if Paragraph can not be found
   */
  private int getParaFromFlatparagraph(String chPara, Locale locale, int startPos) {
    if (docCache == null) {
      return -1;
    }
    // try to get next position from last FlatParagraph position (for performance reasons)
    if (startPos != 0 && proofInfo == OfficeTools.PROOFINFO_MARK_PARAGRAPH) {
      if (debugMode > 0) {
        MessageHandler.printToLogFile("From FlatParagraph: Number of Paragraph: " + numLastFlPara 
            + " (proofInfo == " + OfficeTools.PROOFINFO_MARK_PARAGRAPH + ")" + OfficeTools.LOG_LINE_BREAK);
      }
      return numLastFlPara;
    }
    int nPara = findNextParaPos(numLastFlPara, chPara, locale, startPos);
    if (nPara >= 0) {
      numLastFlPara = nPara;
      if (debugMode > 0) {
        MessageHandler.printToLogFile("From last FlatPragraph Position: Number of Paragraph: " + nPara 
            + ", start: " + startPos + OfficeTools.LOG_LINE_BREAK);
      }
      return nPara;
    }
    
    // number of paragraphs has changed? --> Update the internal information
    nPara = changesInNumberOfParagraph(true);
    if (nPara < 0) {
      if (proofInfo == OfficeTools.PROOFINFO_UNKNOWN) {
        //  problem with automatic iteration - try to get ViewCursor position
        return getParaFromViewCursorOrDialog(chPara, locale);
      } else {
        return -1;
      }
    }
    int nTPara = docCache.getNumberOfTextParagraph(nPara); 
    if (proofInfo == OfficeTools.PROOFINFO_MARK_PARAGRAPH) {
      if (nTPara < 0) {
        return getPosFromChangedPara(chPara, locale, nPara);
      }
    }
    String curFlatParaText = flatPara.getCurrentParaText();
    if (debugMode > 0) {
      MessageHandler.printToLogFile("curFlatParaText: " + curFlatParaText + OfficeTools.LOG_LINE_BREAK
          + "chPara: " + chPara + OfficeTools.LOG_LINE_BREAK + "getFlatParagraph: " + docCache.getFlatParagraph(nPara) + OfficeTools.LOG_LINE_BREAK);
    }
    if (proofInfo == OfficeTools.PROOFINFO_UNKNOWN) {
      if (curFlatParaText != null && !curFlatParaText.equals(chPara) && curFlatParaText.equals(docCache.getFlatParagraph(nPara))) {
        //  wrong flat paragraph - try to get ViewCursor position
        return getParaFromViewCursorOrDialog(chPara, locale);
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
            MessageHandler.printToLogFile("From document cache: Number of Paragraph: " + n 
                + ", start: " + startPos + OfficeTools.LOG_LINE_BREAK);
          }
          textIsChanged = true;
          resetCheck = true;
          return n;
        }
      }
    }

    // find position from changed paragraph
    return getPosFromChangedPara(chPara, locale, nPara);
  }

  /**
   * remove all cached matches for one paragraph
   */
  public void removeResultCache(int nPara) {
    for (ResultCache cache : paragraphsCache) {
      cache.remove(nPara);
    }
  }
  
  /**
   * Get number of flat paragraph from document cache
   * start with a known paragraph
   * return -1 if fails
   */
  private int getParaFromDocCache(String chPara, Locale locale, int nStart) {
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
    return -1;
  }

  /** 
   * Get the Position of Paragraph if result is ordered by right mouse click or spelling dialog
   * returns -1 if it fails
   */
  private int getParaFromViewCursorOrDialog(String chPara, Locale locale) {
    // try to get ViewCursor position (proof initiated by mouse click)
    if (docCache == null) {
      return -1;
    }
    if (viewCursor == null) {
      viewCursor = new ViewCursorTools(xContext);
    }
    int nPara = viewCursor.getViewCursorParagraph();
    if (nPara >= 0 && nPara < docCache.textSize() && docCache.isEqual(docCache.getFlatParagraphNumber(nPara), chPara, locale)) {
      nPara = docCache.getFlatParagraphNumber(nPara);
      numLastVCPara = nPara;
      if (debugMode > 0) {
        MessageHandler.printToLogFile("From View Cursor: Number of Paragraph: " + nPara + OfficeTools.LOG_LINE_BREAK);
      }
//      isDialogRequest.add(nParas);
      return nPara;
    }
    // try to get next position from last ViewCursor position (proof per dialog box)
    if (numLastVCPara >= docCache.size()) {
      numLastVCPara = 0;
    }
    nPara = getParaFromDocCache(chPara, locale, numLastVCPara);
    if (nPara >= 0) {
      numLastVCPara = nPara;
      if (debugMode > 0) {
        MessageHandler.printToLogFile("From Dialog: Number of Paragraph: " + nPara + OfficeTools.LOG_LINE_BREAK);
      }
//    isDialogRequest.add(nParas);
    return nPara;
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("From Dialog: Paragraph not found: return -1" + OfficeTools.LOG_LINE_BREAK);
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
    DocumentCache docCache = this.docCache;
    if (docCache == null) {
      return -1;
    }
    setFlatParagraphTools(xComponent);
    int nPara = 0;
    if (getCurNum) {
      nPara = flatPara.getCurNumFlatParagraph();
      if (nPara < 0) {
        return -1;
      }
    }
    int nFParas = flatPara.getNumberOfAllFlatPara();
    if (nFParas == docCache.size()) {
      return nPara;
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("*** resetAllParas: docCache.size: " + docCache.size() + ", nPara: " + nPara
              + ", docID: " + docID + OfficeTools.LOG_LINE_BREAK);
    }
    DocumentCache oldDocCache = docCache;
    if (useQueue) {
      mDocHandler.getTextLevelCheckQueue().interruptCheck(docID);
    }
    if (docCursor == null) {
      docCursor = new DocumentCursorTools(xComponent);
    }
    docCache = new DocumentCache(docCursor, flatPara, defaultParaCheck,
        docLanguage != null ? LinguisticServices.getLocale(docLanguage) : null);
    if (docCache.isEmpty()) {
      this.docCache = null;
      return -1;
    }
    int from = 0;
    while (from < docCache.size() && from < oldDocCache.size()
        && docCache.getFlatParagraph(from).equals(oldDocCache.getFlatParagraph(from))) {
      from++;
    }
    changeFrom = from - numParasToChange;
    int to = 1;
    while (to <= docCache.size() && to <= oldDocCache.size()
        && docCache.getFlatParagraph(docCache.size() - to).equals(
            oldDocCache.getFlatParagraph(oldDocCache.size() - to))) {
      to++;
    }
    to = docCache.size() - to;
    changeTo = to + numParasToChange + 1;
    singleDocument.removeAndShiftIgnoredMatch(from, to, oldDocCache.size(), docCache.size());
    if (debugMode > 0) {
      MessageHandler.printToLogFile("!!!Changed paragraphs: from:" + from + ", to: " + to);
    }
    for (ResultCache cache : paragraphsCache) {
      cache.removeAndShift(from, to, docCache.size() - oldDocCache.size());
    }
    this.docCache = docCache;
    singleDocument.setDocumentCache(docCache);
    if (useQueue) {
      if (debugMode > 0) {
        MessageHandler.printToLogFile("Number of Paragraphs has changed: new: " + docCache.size() 
        +",  old: " + oldDocCache.size()+ ", docID: " + docID);
      }
      for (int i = 0; i < minToCheckPara.size(); i++) {
        if (minToCheckPara.get(i) != 0) {
          for (int n = from; n <= to; n++) {
            singleDocument.addQueueEntry(n, i, minToCheckPara.get(i), docID, false);
          }
        }
      }
    }
    //  set divNum (difference between doc cursor text and flat paragraphs (is number of footnotes etc.)
    if (debugMode > 0) {
      MessageHandler.printToLogFile("Number FlatParagraphs: " + nFParas + "; docID: " + docID);
    }
    if (nFParas < docCache.textSize()) {
      return -1;   // try to get ViewCursor position for proof info unknown
    }
    if (nPara >= docCache.size()) {
      nPara = flatPara.getCurNumFlatParagraph();
      if (nPara < 0 || nPara >= docCache.size()) {
        return -1;
      }
    }
    if (getCurNum) {
      resetCheck = true;
      textIsChanged = true;
    }
    return nPara;
  }
  
  /**
   * find position from changed paragraph
   */
  private int getPosFromChangedPara(String chPara, Locale locale, int nPara) {
    if (docCache == null || nPara < 0) {
      return -1;
    }
    if (!docCache.isEqual(nPara, chPara, locale)) {
      if (debugMode > 0) {
        MessageHandler.printToLogFile("!!! flat praragraph changed: nPara: " + nPara
                + "; docID: " + docID
                + OfficeTools.LOG_LINE_BREAK + "old: " + docCache.getFlatParagraph(nPara) + OfficeTools.LOG_LINE_BREAK 
                + "new: " + chPara + OfficeTools.LOG_LINE_BREAK);
      }
      docCache.setFlatParagraph(nPara, chPara, locale);
      resetCheck = true;
      if (useQueue) {
        for (int i = 0; i < minToCheckPara.size(); i++) {
          if (minToCheckPara.get(i) == 0) {
            paragraphsCache.get(i).remove(nPara);
          } else {
            singleDocument.addQueueEntry(nPara, i, minToCheckPara.get(i), docID, numLastFlPara < 0 ? false : true);
          }
        }
      } else {
        for (ResultCache cache : paragraphsCache) {
          cache.remove(nPara);
        }
      }
      if (textIsChanged) {
        changeFrom = nPara - numParasToChange;
        changeTo = nPara + numParasToChange + 1;
        singleDocument.removeIgnoredMatch(nPara);
      }
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("From FlatParagraph: Number of Paragraph: " + nPara + OfficeTools.LOG_LINE_BREAK);
    }
    numLastFlPara = nPara;  //  Note: This is the number of flat paragraph
    return nPara;
  }
  
  /**
   * Heuristic try to find next position (automatic iteration)
   * Is paragraph same, next not empty after or before   
   */
  private int findNextParaPos(int startPara, String paraStr, Locale locale, int startPos) {
    if (docCache == null || docCache.size() < 1) {
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
