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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.languagetool.Language;
import org.languagetool.gui.Configuration;
import org.languagetool.openoffice.DocumentCache.TextParagraph;
import org.languagetool.openoffice.OfficeTools.DocumentType;
import org.languagetool.openoffice.TextLevelCheckQueue.QueueEntry;

import com.sun.star.awt.MouseButton;
import com.sun.star.awt.MouseEvent;
import com.sun.star.awt.XMouseClickHandler;
import com.sun.star.awt.XUserInputInterception;
import com.sun.star.beans.PropertyValue;
import com.sun.star.document.DocumentEvent;
import com.sun.star.document.XDocumentEventBroadcaster;
import com.sun.star.document.XDocumentEventListener;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Class for checking text of one LO document 
 * @since 4.3
 * @author Fred Kruse, Marcin Miłkowski
 */
class SingleDocument {
  
  /**
   * Full text Check:
   * numParasToCheck: Paragraphs to be checked for full text rules
   * < 0 check full text (time intensive)
   * == 0 check only one paragraph (works like LT Version <= 3.9)
   * > 0 checks numParasToCheck before and after the processed paragraph
   * 
   * Cache:
   * sentencesCache: only used for doResetCheck == true (LO checks again only changed paragraphs by default)
   * paragraphsCache: used to store LT matches for a fast return to LO (numParasToCheck != 0)
   * singleParaCache: used for one paragraph check by default or for special paragraphs like headers, footers, footnotes, etc.
   *  
   */
  
  private static int debugMode;                   //  should be 0 except for testing; 1 = low level; 2 = advanced level
  
  private Configuration config;

  private int numParasToCheck = 0;                // current number of Paragraphs to be checked

  private XComponentContext xContext;             //  The context of the document
  private String docID;                           //  docID of the document
  private XComponent xComponent;                  //  XComponent of the open document
  private final MultiDocumentsHandler mDocHandler;      //  handles the different documents loaded in LO/OO
  private LTDokumentEventListener eventListener = null; //  listens for save of document 
  
  private final DocumentCache docCache;           //  cache of paragraphs (only readable by parallel thread)
  private final List<ResultCache> paragraphsCache;//  Cache for matches of text rules
  private final Map<Integer, String> changedParas;//  Map of last changed paragraphs;
  private DocumentCursorTools docCursor = null;   //  Save document cursor for the single document
  private ViewCursorTools viewCursor = null;      //  Get the view cursor for desktop
  private FlatParagraphTools flatPara = null;     //  Save information for flat paragraphs (including iterator and iterator provider) for the single document
  private Integer numLastVCPara = 0;              //  Save position of ViewCursor for the single documents
  private Integer numLastFlPara = -1;             //  Save position of FlatParagraph for the single documents
  private CacheIO cacheIO;
  private int changeFrom = 0;                     //  Change result cache from paragraph
  private int changeTo = 0;                       //  Change result cache to paragraph
  private int paraNum;                            //  Number of current checked paragraph
  private int lastChangedPara;                    //  lastPara which was detected as changed
  private List<Integer> lastChangedParas;         //  lastPara which was detected as changed
  private IgnoredMatches ignoredMatches;          //  Map of matches (number of paragraph, number of character) that should be ignored after ignoreOnce was called
  private final DocumentType docType;             //  save the type of document
  private boolean disposed = false;               //  true: document with this docId is disposed - SingleDocument shall be removed
  private boolean resetDocCache = false;          //  true: the cache of the document should be reseted before the next check
  private boolean hasFootnotes = true;            //  true: Footnotes are supported by LO/OO
  private boolean isLastIntern = false;           //  true: last check was intern
  private boolean isRightButtonPressed = false;   //  true: right mouse Button was pressed
  private String lastSinglePara = null;           //  stores the last paragraph which is checked as single paragraph
  private Language docLanguage = null;            //  Language used for check
  private LanguageToolMenus ltMenus = null;       //  LT menus (tools menu and context menu)

  SingleDocument(XComponentContext xContext, Configuration config, String docID, 
      XComponent xComp, MultiDocumentsHandler mDH) {
    debugMode = OfficeTools.DEBUG_MODE_SD;
    this.xContext = xContext;
    this.config = config;
    this.docID = docID;
    if (docID.charAt(0) == 'I') {
      docType = DocumentType.IMPRESS;
    } else if (docID.charAt(0) == 'C') {
      docType = DocumentType.CALC;
    } else {
      docType = DocumentType.WRITER;
    }
    xComponent = xComp;
    mDocHandler = mDH;
    changedParas = new HashMap<Integer, String>();
    setDokumentListener(xComponent);
    List<ResultCache> paraCache = new ArrayList<>();
    for (int i = 0; i < OfficeTools.NUMBER_TEXTLEVEL_CACHE; i++) {
      paraCache.add(new ResultCache());
    }
    paragraphsCache = Collections.unmodifiableList(paraCache);
    if (config != null) {
      setConfigValues(config);
    }
    resetCache();
    ignoredMatches = new IgnoredMatches();
    if (docCursor == null) {
      docCursor = new DocumentCursorTools(xComponent);
    }
    docCache = new DocumentCache(docType);
    if (config != null && config.saveLoCache() && xComponent != null && !mDocHandler.isTestMode()) {
      readCaches();
    }
    if (xComponent != null) {
      setFlatParagraphTools();
    }
  }
  
  /**  get the result for a check of a single document 
   * 
   * @param paraText          paragraph text
   * @param paRes             proof reading result
   * @return                  proof reading result
   */
  ProofreadingResult getCheckResults(String paraText, Locale locale, ProofreadingResult paRes, 
      PropertyValue[] propertyValues, boolean docReset, SwJLanguageTool lt) {
    return getCheckResults(paraText, locale, paRes, propertyValues, docReset, lt, -1);
  }
    
  ProofreadingResult getCheckResults(String paraText, Locale locale, ProofreadingResult paRes, 
      PropertyValue[] propertyValues, boolean docReset, SwJLanguageTool lt, int nPara) {
    boolean isMouseRequest = false;
    if (isRightButtonPressed) {
      isMouseRequest = true;
      isRightButtonPressed = false;
    }
    int [] footnotePositions = null;  // e.g. for LO/OO < 4.3 and the 'FootnotePositions' property
    int proofInfo = OfficeTools.PROOFINFO_UNKNOWN;  //  OO and LO < 6.5 do not support ProofInfo
    for (PropertyValue propertyValue : propertyValues) {
      if ("FootnotePositions".equals(propertyValue.Name)) {
        if (propertyValue.Value instanceof int[]) {
          footnotePositions = (int[]) propertyValue.Value;
        } else {
          MessageHandler.printToLogFile("SingleDocument: getCheckResults: Not of expected type int[]: " + propertyValue.Name + ": " + propertyValue.Value.getClass());
        }
      }
      if ("ProofInfo".equals(propertyValue.Name)) {
        if (propertyValue.Value instanceof Integer) {
          proofInfo = (int) propertyValue.Value;
        } else {
          MessageHandler.printToLogFile("SingleDocument: getCheckResults: Not of expected type int: " + propertyValue.Name + ": " + propertyValue.Value.getClass());
        }
      }
    }
    hasFootnotes = footnotePositions != null;
    if (!hasFootnotes) {
      //  OO and LO < 4.3 do not support 'FootnotePositions' property and other advanced features
      //  switch back to single paragraph check mode - save settings in configuration
      if (numParasToCheck != 0) {
        if (config.useTextLevelQueue()) {
          mDocHandler.getTextLevelCheckQueue().setStop();
        }
        numParasToCheck = 0;
        config.setNumParasToCheck(numParasToCheck);
        config.setUseTextLevelQueue(false);
        try {
          config.saveConfiguration(docLanguage);
        } catch (IOException e) {
          MessageHandler.showError(e);
        }
        MessageHandler.printToLogFile("Single paragraph check mode set!");
      }
      mDocHandler.setUseOriginalCheckDialog();
    }

    if (resetDocCache) {
      if (nPara >= 0) {
        docCache.clear();
      } else {
        if (docCursor == null) {
          docCursor = new DocumentCursorTools(xComponent);
        }
        docCache.refresh(docCursor, flatPara, 
            docLanguage != null ? LinguisticServices.getLocale(docLanguage) : null, xComponent, 6);
      }
      resetDocCache = false;
    }
    if (docLanguage == null) {
      docLanguage = lt.getLanguage();
    }
    if (disposed) {
      return paRes;
    }
    try {
      if (docReset) {
        numLastVCPara = 0;
        ignoredMatches = new IgnoredMatches();
      }
      boolean isIntern = nPara < 0 ? false : true;
      boolean isDialogRequest = (nPara >= 0 || (proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT));
      
      CheckRequestAnalysis requestAnalysis = new CheckRequestAnalysis(numLastVCPara, numLastFlPara,
          proofInfo, numParasToCheck, this, paragraphsCache, viewCursor, changedParas);
      int paraNum = requestAnalysis.getNumberOfParagraph(nPara, paraText, locale, paRes.nStartOfSentencePosition, footnotePositions);
      if (debugMode > 1) {
        MessageHandler.printToLogFile("Single document: getCheckResults: paraNum = " + paraNum + ", nPara = " + nPara);
      }
      if (paraNum == -2) {
        paraNum = isLastIntern ? this.paraNum : -1;
        if (debugMode > 1) {
          MessageHandler.printToLogFile("Single document: getCheckResults: paraNum set to: " + paraNum + ", isLastIntern = " + isLastIntern);
        }
      }
      this.paraNum = paraNum;
      isLastIntern = isIntern;
      flatPara = requestAnalysis.getFlatParagraphTools();
      docCursor = requestAnalysis.getDocumentCursorTools();
      viewCursor = requestAnalysis.getViewCursorTools();
      changeFrom = requestAnalysis.getFirstParagraphToChange();
      changeTo = requestAnalysis.getLastParagraphToChange();
      numLastFlPara = requestAnalysis.getLastParaNumFromFlatParagraph();
      numLastVCPara = requestAnalysis.getLastParaNumFromViewCursor();
      boolean textIsChanged = requestAnalysis.textIsChanged();
      
      if (disposed) {
        return paRes;
      }
      SingleCheck singleCheck = new SingleCheck(this, paragraphsCache, docCursor, flatPara, 
          docLanguage, ignoredMatches, numParasToCheck, isDialogRequest, isMouseRequest, isIntern);
      paRes.aErrors = singleCheck.getCheckResults(paraText, footnotePositions, locale, lt, paraNum, 
          paRes.nStartOfSentencePosition, textIsChanged, changeFrom, changeTo, lastSinglePara, lastChangedPara);
      lastSinglePara = singleCheck.getLastSingleParagraph();
      paRes.nStartOfSentencePosition = paragraphsCache.get(0).getStartSentencePosition(paraNum, paRes.nStartOfSentencePosition);
      paRes.nStartOfNextSentencePosition = paragraphsCache.get(0).getNextSentencePosition(paraNum, paRes.nStartOfSentencePosition);
      if (paRes.nStartOfNextSentencePosition == 0) {
        paRes.nStartOfNextSentencePosition = paraText.length();
      }
      paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
      lastChangedPara = (textIsChanged && numParasToCheck != 0) ? paraNum : -1;
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    if (docType == DocumentType.WRITER && ltMenus == null && paraText.length() > 4) {
      ltMenus = new LanguageToolMenus(xContext, this, config);
    }
    return paRes;
  }
  
  /**
   * set values set by configuration dialog
   */
  void setConfigValues(Configuration config) {
    this.config = config;
    numParasToCheck = (mDocHandler.isTestMode() || mDocHandler.heapLimitIsReached()) ? 0 : config.getNumParasToCheck();
    if (ltMenus != null) {
      ltMenus.setConfigValues(config);
    }
    if (config.noBackgroundCheck() || numParasToCheck == 0) {
      setFlatParagraphTools();
    }
  }

  /**
   * set the document cache - use only for tests
   * @since 5.3
   */
  void setDocumentCacheForTests(List<String> paragraphs, List<List<String>> textParagraphs, List<int[]> footnotes, List<List<Integer>> chapterBegins, Locale locale) {
    docCache.setForTest(paragraphs, textParagraphs, footnotes, chapterBegins, locale);
    numParasToCheck = -1;
    mDocHandler.resetSortedTextRules();
  }
  
  /** Get LanguageTool menu
   */
  LanguageToolMenus getLtMenu() {
    return ltMenus;
  }
  
  /**
   * set menu ID to MultiDocumentsHandler
   */
  void dispose(boolean disposed) {
    this.disposed = disposed;
    if (disposed) {
      if (docCursor != null) {
        docCursor.setDisposed();
      }
      if (viewCursor != null) {
        viewCursor.setDisposed();
      }
      if (flatPara != null) {
        flatPara.setDisposed();
      }
    }
  }
  
  /**
   * get type of document
   */
  DocumentType getDocumentType() {
    return docType;
  }
  
  /**
   * get number of current paragraph
   */
  boolean isDisposed() {
    return disposed;
  }
  
  /**
   * set menu ID to MultiDocumentsHandler
   */
  void setMenuDocId() {
    mDocHandler.setMenuDocId(getDocID());
  }
  
  /**
   * get number of current paragraph
   */
  int getCurrentNumberOfParagraph() {
    return paraNum;
  }
  
  /**
   * get language of the document
   */
  Language getLanguage() {
    return docLanguage;
  }
  
  /**
   * set language of the document
   */
  void setLanguage(Language language) {
    docLanguage = language;
  }
  
  /** 
   * Set XComponentContext and XComponent of the document
   */
  void setXComponent(XComponentContext xContext, XComponent xComponent) {
    this.xContext = xContext;
    this.xComponent = xComponent;
    if (xComponent == null) {
      docCursor = null;
      viewCursor = null;
      flatPara = null;
    } else {
      setDokumentListener(xComponent);
    }
  }
  
  /**
   *  Get xComponent of the document
   */
  XComponent getXComponent() {
    return xComponent;
  }
  
  /**
   *  Get MultiDocumentsHandler
   */
  MultiDocumentsHandler getMultiDocumentsHandler() {
    return mDocHandler;
  }
  
  /**
   *  Get ID of the document
   */
  String getDocID() {
    return docID;
  }
  
  /**
   *  Get ID of the document
   */
  void setDocID(String docId) {
    docID = docId;
  }
  
  /**
   *  Get flat paragraph tools of the document
   */
  FlatParagraphTools getFlatParagraphTools () {
    return flatPara;
  }
  
  /**
   *  Get document cache of the document
   */
  DocumentCache getDocumentCache() {
    return docCache;
  }
  
  /**
   *  reset document cache of the document
   */
  void resetDocumentCache() {
    resetDocCache = true;
  }
  
  /**
   *  set last changed paragraphs
   */
  void setLastChangedParas(List<Integer> lastChangedParas) {
    this.lastChangedParas = lastChangedParas;
  }
  
  /**
   *  get last changed paragraphs
   */
  List<Integer> getLastChangedParas() {
    return lastChangedParas;
  }
  
  /**
   * reset the Document
   */
  void resetDocument() {
    mDocHandler.resetDocument();
  }
  
  /**
   * read caches from file
   */
  void readCaches() {
    if (numParasToCheck != 0) {
      cacheIO = new CacheIO(xComponent);
      boolean cacheExist = cacheIO.readAllCaches(config, mDocHandler);
      if (cacheExist) {
        docCache.put(cacheIO.getDocumentCache());
        for (int i = 0; i < cacheIO.getParagraphsCache().size(); i++) {
          paragraphsCache.get(i).replace(cacheIO.getParagraphsCache().get(i));
        }
        ignoredMatches = new IgnoredMatches(cacheIO.getIgnoredMatches());
      }
      cacheIO.resetAllCache();
    }
  }
  
  /**
   * write caches to file
   */
  void writeCaches() {
    if (numParasToCheck != 0) {
      DocumentCache docCache = new DocumentCache(this.docCache);
      List<ResultCache> paragraphsCache = new ArrayList<ResultCache>();
      for (int i = 0; i < this.paragraphsCache.size(); i++) {
        paragraphsCache.add(new ResultCache(this.paragraphsCache.get(i)));
      }
      cacheIO.saveCaches(docCache, paragraphsCache, ignoredMatches, config, mDocHandler);
    }
  }
  
  /** 
   * Reset all caches of the document
   */
  void resetCache() {
    for (int i = 0; i < OfficeTools.NUMBER_TEXTLEVEL_CACHE; i++) {
      paragraphsCache.get(i).removeAll();
    }
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
   * Open new flat paragraph tools or initialize them again
   */
  public FlatParagraphTools setFlatParagraphTools() {
	  if (disposed) {
      flatPara = null;
	  } else if (flatPara == null) {
      flatPara = new FlatParagraphTools(xComponent);
      if (!flatPara.isValid()) {
        flatPara = null;
      }
    } else {
      flatPara.init();
    }
    return flatPara;
  }
  
  /**
   * Add an new entry to text level queue
   * nFPara is number of flat paragraph
   */
  public void addQueueEntry(int nFPara, int nCache, int nCheck, String docId, boolean checkOnlyParagraph, boolean overrideRunning) {
    if (!disposed && mDocHandler.isSortedRuleForIndex(nCache) && docCache != null) {
      TextParagraph nTPara = docCache.getNumberOfTextParagraph(nFPara);
      if (nTPara != null && nTPara.type != DocumentCache.CURSOR_TYPE_UNKNOWN) {
        int nStart;
        int nEnd;
        if (checkOnlyParagraph && nCheck > 0) {
          nStart = nTPara.number;
          nEnd = nTPara.number + 1;
        } else {
          nStart = docCache.getStartOfParaCheck(nTPara, nCheck, checkOnlyParagraph, true, false);
          nEnd = docCache.getEndOfParaCheck(nTPara, nCheck, checkOnlyParagraph, true, false);
        }
        mDocHandler.getTextLevelCheckQueue().addQueueEntry(docCache.createTextParagraph(nTPara.type, nStart), 
            docCache.createTextParagraph(nTPara.type, nEnd), nCache, nCheck, docId, overrideRunning);
      }
    }
  }
  
  /**
   * create a queue entry 
   * used by getNextQueueEntry
   */
  private QueueEntry createQueueEntry(TextParagraph nPara, int nCache) {
    int nCheck = mDocHandler.getNumMinToCheckParas().get(nCache);
    int nStart = docCache.getStartOfParaCheck(nPara, nCheck, false, true, false);
    int nEnd = docCache.getEndOfParaCheck(nPara, nCheck, false, true, false);
    if (nCheck > 0 && nStart + 1 < nEnd) {
      if ((nStart == nPara.number 
              || paragraphsCache.get(nCache).getCacheEntry(docCache.getFlatParagraphNumber(new TextParagraph(nPara.type, nPara.number - 1))) != null) 
          && (nEnd == nPara.number 
              || paragraphsCache.get(nCache).getCacheEntry(docCache.getFlatParagraphNumber(new TextParagraph(nPara.type, nPara.number + 1))) != null)) {
        nStart = nPara.number;
        nEnd = nStart + 1;
      }
    }
    return mDocHandler.getTextLevelCheckQueue().createQueueEntry(docCache.createTextParagraph(nPara.type, nStart), 
        docCache.createTextParagraph(nPara.type, nEnd), nCache, nCheck, docID, false);
  }

  /**
   * get the next queue entry which is the next empty cache entry
   */
  public QueueEntry getNextQueueEntry(TextParagraph nPara) {
    if (!disposed && docCache != null) {
      if (nPara != null && nPara.type != DocumentCache.CURSOR_TYPE_UNKNOWN && nPara.number < docCache.textSize(nPara)) {
        for (int nCache = 1; nCache < paragraphsCache.size(); nCache++) {
          if (mDocHandler.isSortedRuleForIndex(nCache) && docCache.isFinished() 
              && paragraphsCache.get(nCache).getCacheEntry(docCache.getFlatParagraphNumber(nPara)) == null) {
            return createQueueEntry(nPara, nCache);
          }
        }
      }
      int nStart = (nPara == null || nPara.type == DocumentCache.CURSOR_TYPE_UNKNOWN || nPara.number < docCache.textSize(nPara)) ? 
          0 : docCache.getFlatParagraphNumber(nPara);
      for (int i = nStart; i < docCache.size(); i++) {
        if (docCache.getNumberOfTextParagraph(i).type != DocumentCache.CURSOR_TYPE_UNKNOWN) {
          for (int nCache = 1; nCache < paragraphsCache.size(); nCache++) {
            if (mDocHandler.isSortedRuleForIndex(nCache) && docCache.isFinished() && paragraphsCache.get(nCache).getCacheEntry(i) == null) {
              return createQueueEntry(docCache.getNumberOfTextParagraph(i), nCache);
            }
          }
        }
      }
      for (int i = 0; i < nStart && i < docCache.size(); i++) {
        if (docCache.getNumberOfTextParagraph(i).type != DocumentCache.CURSOR_TYPE_UNKNOWN) {
          for (int nCache = 1; nCache < paragraphsCache.size(); nCache++) {
            if (mDocHandler.isSortedRuleForIndex(nCache) && docCache.isFinished() && paragraphsCache.get(nCache).getCacheEntry(i) == null) {
              return createQueueEntry(docCache.getNumberOfTextParagraph(i), nCache);
            }
          }
        }
      }
    }
    return null;
  }

  /**
   * get the queue entry for the first changed paragraph in document cache
   */
  public QueueEntry getQueueEntryForChangedParagraph() {
    if (!disposed && docCache != null && flatPara != null && !changedParas.isEmpty()) {
/*  TODO: Remove after Tests
      CheckRequestAnalysis requestAnalysis = new CheckRequestAnalysis(numLastVCPara, numLastFlPara,
          OfficeTools.PROOFINFO_GET_PROOFRESULT, numParasToCheck, this, paragraphsCache, viewCursor);
      int nPara = requestAnalysis.changesInDocumentCache();
*/
      Set<Integer> nParas = new HashSet<Integer>(changedParas.keySet());
      for (int nPara : nParas) {
        String sPara = flatPara.getFlatParagraphAt(nPara).getText();
        if (sPara != null) {
          String sChangedPara = changedParas.get(nPara);
          changedParas.remove(nPara);
          if (sChangedPara != null && !sChangedPara.equals(sPara)) {
            docCache.setFlatParagraph(nPara, sPara);
            for (int i = 0; i < mDocHandler.getNumMinToCheckParas().size(); i++) {
              paragraphsCache.get(i).remove(nPara);
            }
            return createQueueEntry(docCache.getNumberOfTextParagraph(nPara), 0);
          }
        }
      }
    }
    return null;
  }

  /**
   * run a text level check from a queue entry (initiated by the queue)
   */
  public void runQueueEntry(TextParagraph nStart, TextParagraph nEnd, int cacheNum, int nCheck, boolean override, SwJLanguageTool lt) {
    if (!disposed && flatPara != null && docCache.isFinished() && nStart.number < docCache.textSize(nStart)) {
      SingleCheck singleCheck = new SingleCheck(this, paragraphsCache, docCursor, flatPara, docLanguage, ignoredMatches, numParasToCheck, false, false, false);
      singleCheck.addParaErrorsToCache(docCache.getFlatParagraphNumber(nStart), lt, cacheNum, nCheck, 
          nEnd.number == nStart.number + 1, override, false, hasFootnotes);
    }
  }
  
  private void remarkChangedParagraphs(List<Integer> changedParas, boolean isIntern) {
    if (!disposed) {
      SingleCheck singleCheck = new SingleCheck(this, paragraphsCache, docCursor, flatPara, docLanguage, ignoredMatches, numParasToCheck, true, false, isIntern);
      if (docCursor == null) {
        docCursor = new DocumentCursorTools(xComponent);
      }
      singleCheck.remarkChangedParagraphs(changedParas, docCursor, flatPara, mDocHandler.getLanguageTool(), true);
    }
  }

  /**
   * is a ignore once entry in cache
   */
  public boolean isIgnoreOnce(int xFrom, int xTo, int y, String ruleId) {
    return ignoredMatches.isIgnored(xFrom, xTo, y, ruleId);
  }
  
  /**
   * reset the ignore once cache
   */
  public void resetIgnoreOnce() {
    ignoredMatches = new IgnoredMatches();
  }
  
  /**
   * add a ignore once entry to queue and remove the mark
   */
  public String ignoreOnce() {
    if (disposed) {
      return null;
    }
    ViewCursorTools viewCursor = new ViewCursorTools(xContext);
    int y = docCache.getFlatParagraphNumber(viewCursor.getViewCursorParagraph());
    int x = viewCursor.getViewCursorCharacter();
    String ruleId = getRuleIdFromCache(y, x);
    setIgnoredMatch (x, y, ruleId, false);
    return docID;
  }
  
  /**
   * add a ignore once entry for point x, y to queue and remove the mark
   */
  public void setIgnoredMatch(int x, int y, String ruleId, boolean isIntern) {
    ignoredMatches.setIgnoredMatch(x, y, ruleId);
    if (debugMode > 1) {
      MessageHandler.printToLogFile("SingleDocument: setIgnoredMatch: DocumentType = " + docType + "; numParasToCheck = " + numParasToCheck);
    }
    if (docType == DocumentType.WRITER && numParasToCheck != 0) {
      List<Integer> changedParas = new ArrayList<>();
      changedParas.add(y);
      remarkChangedParagraphs(changedParas, isIntern);
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("SingleDocument: setIgnoredMatch: Ignore Match added at: paragraph: " + y + "; character: " + x + "; ruleId: " + ruleId);
    }
  }
  
  /**
   * remove all ignore once entries for paragraph y from queue and set the mark
   */
  public void removeAndShiftIgnoredMatch(int from, int to, int oldSize, int newSize) {
    if (!ignoredMatches.isEmpty()) {
      IgnoredMatches tmpIgnoredMatches = new IgnoredMatches();
      for (int i = 0; i < from; i++) {
        if (ignoredMatches.containsParagraph(i)) {
          tmpIgnoredMatches.put(i, ignoredMatches.get(i));
        }
      }
      for (int i = to + 1; i < oldSize; i++) {
        int n = i + newSize - oldSize;
        if (ignoredMatches.containsParagraph(i)) {
          tmpIgnoredMatches.put(n, ignoredMatches.get(i));
        }
      }
      ignoredMatches = tmpIgnoredMatches;
    }
  }
  
  /**
   * remove all ignore once entries for paragraph y from queue and set the mark
   */
  public void removeIgnoredMatch(int y, boolean isIntern) {
    ignoredMatches.removeIgnoredMatches(y);
    if (numParasToCheck != 0 && flatPara != null) {
      List<Integer> changedParas = new ArrayList<>();
      changedParas.add(y);
      remarkChangedParagraphs(changedParas, isIntern);
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("SingleDocument: removeIgnoredMatch: All Ignored Matches removed at: paragraph: " + y);
    }
  }
  
  /**
   * remove a ignore once entry for point x, y from queue and set the mark
   * if x < 0 remove all ignore once entries for paragraph y
   */
  public void removeIgnoredMatch(int x, int y, String ruleId, boolean isIntern) {
    ignoredMatches.removeIgnoredMatch(x, y, ruleId);
    if (numParasToCheck != 0) {
      List<Integer> changedParas = new ArrayList<>();
      changedParas.add(y);
      remarkChangedParagraphs(changedParas, isIntern);
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("SingleDocument: removeIgnoredMatch: Ignore Match removed at: paragraph: " + y + "; character: " + x);
    }
  }
  
  /**
   * get a rule ID of an error out of the cache 
   * by the position of the error (flat paragraph number and number of character)
   */
  private String getRuleIdFromCache(int nPara, int nChar) {
    List<SingleProofreadingError> tmpErrors = new ArrayList<SingleProofreadingError>();
    if (nPara < 0 || nPara >= docCache.size()) {
      return null;
    }
    for (ResultCache paraCache : paragraphsCache) {
      SingleProofreadingError tError = paraCache.getErrorAtPosition(nPara, nChar);
      if (tError != null) {
        tmpErrors.add(tError);
      }
    }
    if (tmpErrors.size() > 0) {
      SingleProofreadingError[] errors = new SingleProofreadingError[tmpErrors.size()];
      for (int i = 0; i < tmpErrors.size(); i++) {
        errors[i] = tmpErrors.get(i);
      }
      Arrays.sort(errors, new ErrorPositionComparator());
      if (debugMode > 0) {
        for (int i = 0; i < errors.length; i++) {
          MessageHandler.printToLogFile("SingleDocument: getRuleIdFromCache: Error[" + i + "]: ruleID: " + errors[i].aRuleIdentifier + ", Start = " + errors[i].nErrorStart + ", Length = " + errors[i].nErrorLength);
        }
      }
      return errors[0].aRuleIdentifier;
    } else {
      return null;
    }
  }
  
  /**
   * get a rule ID of an error from a check 
   * by the position of the error (number of character)
   */
  private String getRuleIdFromCache(int nChar, ViewCursorTools viewCursor) {
    String text = viewCursor.getViewCursorParagraphText();
    if (text == null) {
      return null;
    }
    PropertyValue[] propertyValues = new PropertyValue[0];
    ProofreadingResult paRes = new ProofreadingResult();
    paRes.nStartOfSentencePosition = 0;
    paRes.nStartOfNextSentencePosition = 0;
    paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
    paRes.xProofreader = null;
    paRes.aLocale = mDocHandler.getLocale();
    paRes.aDocumentIdentifier = docID;
    paRes.aText = text;
    paRes.aProperties = propertyValues;
    paRes.aErrors = null;
    while (nChar > paRes.nStartOfNextSentencePosition && paRes.nStartOfNextSentencePosition < text.length()) {
      paRes.nStartOfSentencePosition = paRes.nStartOfNextSentencePosition;
      paRes.nStartOfNextSentencePosition = text.length();
      paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
      paRes = getCheckResults(text, paRes.aLocale, paRes, propertyValues, false, mDocHandler.getLanguageTool(), -1);
      if (paRes.nStartOfNextSentencePosition > nChar) {
        if (paRes.aErrors == null) {
          return null;
        }
        for (SingleProofreadingError error : paRes.aErrors) {
          if (error.nErrorStart <= nChar && nChar < error.nErrorStart + error.nErrorLength) {
            return error.aRuleIdentifier;
          }
        }
      }
    }
    MessageHandler.printToLogFile("SingleDocument: getRuleIdFromCache: No ruleId found");
    return null;
  }
  
  /**
   * get back the rule ID to deactivate a rule
   */
  public String deactivateRule() {
    if (disposed) {
      return null;
    }
    ViewCursorTools viewCursor = new ViewCursorTools(xContext);
    int x = viewCursor.getViewCursorCharacter();
    if (numParasToCheck == 0) {
      return getRuleIdFromCache(x, viewCursor);
    }
    int y = docCache.getFlatParagraphNumber(viewCursor.getViewCursorParagraph());
    return getRuleIdFromCache(y, x);
  }
  
  /**
   * class for store and handle ignored matches
   */
  class IgnoredMatches {
    
    private Map<Integer, Map<String, Set<Integer>>> ignoredMatches;
    
    IgnoredMatches () {
      ignoredMatches = new HashMap<>();
    }
    
    IgnoredMatches (Map<Integer, Map<String, Set<Integer>>> ignoredMatches) {
      this.ignoredMatches = ignoredMatches;
    }
    
    /**
     * Set an ignored match
     */
    public void setIgnoredMatch(int x, int y, String ruleId) {
      Map<String, Set<Integer>> ruleAtX;
      Set<Integer> charNums;
      if (ignoredMatches.containsKey(y)) {
        ruleAtX = ignoredMatches.get(y);
        if (ruleAtX.containsKey(ruleId)) {
          charNums = ruleAtX.get(ruleId);
        } else {
          charNums = new HashSet<>();
        }
      } else {
        ruleAtX = new HashMap<String, Set<Integer>>();
        charNums = new HashSet<>();
      }
      charNums.add(x);
      ruleAtX.put(ruleId, charNums);
      ignoredMatches.put(y, ruleAtX);
    }
   
    /**
     * Remove an ignored matches in a paragraph
     */
    public void removeIgnoredMatches(int y) {
      if (ignoredMatches.containsKey(y)) {
        ignoredMatches.remove(y);
      }
    }
      
    /**
     * Remove an ignored matches of a special ruleID in a paragraph
     */
    public void removeIgnoredMatches(int y, String ruleId) {
      if (ignoredMatches.containsKey(y)) {
        Map<String, Set<Integer>> ruleAtX = ignoredMatches.get(y);
        if (ruleAtX.containsKey(ruleId)) {
          ruleAtX.remove(ruleId);
        }
        if (ruleAtX.isEmpty()) {
          ignoredMatches.remove(y);
        } else {
          ignoredMatches.put(y, ruleAtX);
        }
      }
    }
      
    /**
     * Remove one ignored match
     */
    public void removeIgnoredMatch(int x, int y, String ruleId) {
      if (ignoredMatches.containsKey(y)) {
        Map<String, Set<Integer>> ruleAtX = ignoredMatches.get(y);
        if (ruleAtX.containsKey(ruleId)) {
          Set<Integer> charNums = ruleAtX.get(ruleId);
          if (charNums.contains(x)) {
            charNums.remove(x);
            if (charNums.isEmpty()) {
              ruleAtX.remove(ruleId);
            } else {
              ruleAtX.put(ruleId, charNums);
            }
            if (ruleAtX.isEmpty()) {
              ignoredMatches.remove(y);
            } else {
              ignoredMatches.put(y, ruleAtX);
            }
          }
        }
      }
    }

    /**
     * Is the match of a ruleID at a position ignored
     */
    public boolean isIgnored(int xFrom, int xTo, int y, String ruleId) {
      if (ignoredMatches.containsKey(y) && ignoredMatches.get(y).containsKey(ruleId)) {
        for (int x : ignoredMatches.get(y).get(ruleId)) {
          if (x >= xFrom && x < xTo) {
            return true;
          }
        }
      }
      return false;
    }
    
    /**
     * Contains a paragraph ignored matches
     */
    public boolean containsParagraph(int y) {
      return ignoredMatches.containsKey(y);
    }

    /**
     * Is the list of ignored matches empty - no ignored matches
     */
    public boolean isEmpty() {
      return ignoredMatches.isEmpty();
    }

    /**
     * size: number of paragraphs containing ignored matches
     */
    public int size() {
      return ignoredMatches.size();
    }

    /**
     * Get all ignored matches of a paragraph
     */
    public Map<String, Set<Integer>>  get(int y) {
      return ignoredMatches.get(y);
    }

    /**
     * Get a copy of map
     */
    public Map<Integer, Map<String, Set<Integer>>>  getFullMap() {
      return ignoredMatches;
    }

    /**
     * add or replace a map of ignored matches to a paragraph
     */
    public void put(int y, Map<String, Set<Integer>> ruleAtX) {
      ignoredMatches.put(y, ruleAtX);
    }
  }
  
  private void setDokumentListener(XComponent xComponent) {
    if (xComponent != null && eventListener == null) {
      eventListener = new LTDokumentEventListener();
      XDocumentEventBroadcaster broadcaster = UnoRuntime.queryInterface(XDocumentEventBroadcaster.class, xComponent);
      if (broadcaster != null) {
        broadcaster.addDocumentEventListener(eventListener);
      } else {
        MessageHandler.printToLogFile("SingleDocument: setDokumentListener: Could not add document event listener!");
      }
      XTextDocument curDoc = UnoRuntime.queryInterface(XTextDocument.class, xComponent);
      if (curDoc == null) {
        MessageHandler.printToLogFile("SingleDocument: setDokumentListener: XTextDocument not found!");
        return;
      }
      XModel xModel = UnoRuntime.queryInterface(XModel.class, xComponent);
      if (xModel == null) {
        MessageHandler.printToLogFile("SingleDocument: setDokumentListener: XModel not found!");
        return;
      }
      XController xController = xModel.getCurrentController();
      if (xController == null) {
        MessageHandler.printToLogFile("SingleDocument: setDokumentListener: XController not found!");
        return;
      }
      XUserInputInterception xUserInputInterception = UnoRuntime.queryInterface(XUserInputInterception.class, xController);
      if (xUserInputInterception == null) {
        MessageHandler.printToLogFile("SingleDocument: setDokumentListener: XUserInputInterception not found!");
        return;
      }
      xUserInputInterception.addMouseClickHandler(eventListener);
    }
  }
  
  class LTDokumentEventListener implements XDocumentEventListener, XMouseClickHandler {

    @Override
    public void disposing(EventObject event) {
    }

    @Override
    public void documentEventOccured(DocumentEvent event) {
      if (event.EventName.equals("OnSave") && config.saveLoCache()) {
        writeCaches();
      } else if(event.EventName.equals("OnSaveAsDone") && config.saveLoCache()) {
        writeCaches();
        cacheIO.setDocumentPath(xComponent);
        writeCaches();
      }
    }

    @Override
    public boolean mousePressed(MouseEvent event) {
      if (event.Buttons == MouseButton.RIGHT) {
        isRightButtonPressed = true;
      }
      return false;
    }

    @Override
    public boolean mouseReleased(MouseEvent event) {
      return false;
    }
  }

}
