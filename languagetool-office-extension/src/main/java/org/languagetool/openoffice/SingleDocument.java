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
  private static boolean debugModeTm;             // time measurement should be false except for testing
  
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
//  private ViewCursorTools viewCursor = null;      //  Get the view cursor for desktop
  private FlatParagraphTools flatPara = null;     //  Save information for flat paragraphs (including iterator and iterator provider) for the single document
  private Integer numLastVCPara = 0;              //  Save position of ViewCursor for the single documents
  private final List<Integer> numLastFlPara;      //  Save position of FlatParagraph for the single documents
  private CacheIO cacheIO;
  private int changeFrom = 0;                     //  Change result cache from paragraph
  private int changeTo = 0;                       //  Change result cache to paragraph
  private int paraNum;                            //  Number of current checked paragraph
  private int lastChangedPara;                    //  lastPara which was detected as changed
  private List<Integer> lastChangedParas;         //  lastPara which was detected as changed
  private IgnoredMatches ignoredMatches;          //  Map of matches (number of paragraph, number of character) that should be ignored after ignoreOnce was called
  private IgnoredMatches permanentIgnoredMatches; //  Map of matches (number of paragraph, number of character) that should be ignored permanent
  private final DocumentType docType;             //  save the type of document
  private boolean disposed = false;               //  true: document with this docId is disposed - SingleDocument shall be removed
  private boolean resetDocCache = false;          //  true: the cache of the document should be reseted before the next check
  private boolean hasFootnotes = true;            //  true: Footnotes are supported by LO/OO
  private boolean hasSortedTextId = true;         //  true: Node Index is supported by LO
  private boolean isLastIntern = false;           //  true: last check was intern
  private boolean isRightButtonPressed = false;   //  true: right mouse Button was pressed
  private boolean isOnUnload = false;             //  Document will be closed
  private String lastSinglePara = null;           //  stores the last paragraph which is checked as single paragraph
  private Language docLanguage;                   //  docLanguage (usually the Language of the first paragraph)
  private final Language fixedLanguage;           //  fixed language (by configuration); if null: use language of document (given by LO/OO)
  private LanguageToolMenus ltMenus = null;       //  LT menus (tools menu and context menu)

  SingleDocument(XComponentContext xContext, Configuration config, String docID, 
      XComponent xComp, MultiDocumentsHandler mDH) {
    numLastFlPara = new ArrayList<>();
    for (int i = 0; i < DocumentCache.NUMBER_CURSOR_TYPES + 1; i++) {
      numLastFlPara.add(-1);
    }
    debugMode = OfficeTools.DEBUG_MODE_SD;
    debugModeTm = OfficeTools.DEBUG_MODE_TM;
    if (!OfficeTools.DEVELOP_MODE_ST) {
      hasSortedTextId = false;
    }
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
    fixedLanguage = config.getDefaultLanguage();
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
    resetResultCache();
    ignoredMatches = new IgnoredMatches();
    permanentIgnoredMatches = new IgnoredMatches();
    docCache = new DocumentCache(docType);
    if (config != null && config.saveLoCache() && !config.noBackgroundCheck() && xComponent != null && !mDocHandler.isTestMode()) {
      readCaches();
    }
    if (xComponent != null) {
      setFlatParagraphTools();
    }
    if (docType == DocumentType.IMPRESS && ltMenus == null) {
      ltMenus = new LanguageToolMenus(xContext, xComponent, this, config);
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
    int sortedTextId = -1;
    int documentElementsCount = -1;
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
      if (hasSortedTextId) {
        if ("SortedTextId".equals(propertyValue.Name)) {
          if (propertyValue.Value instanceof Integer) {
            sortedTextId = (int) propertyValue.Value;
          } else {
            MessageHandler.printToLogFile("SingleDocument: getCheckResults: Not of expected type int: " + propertyValue.Name + ": " + propertyValue.Value.getClass());
          }
        }
        if ("DocumentElementsCount".equals(propertyValue.Name)) {
          if (propertyValue.Value instanceof Integer) {
            documentElementsCount = (int) propertyValue.Value;
          } else {
            MessageHandler.printToLogFile("SingleDocument: getCheckResults: Not of expected type int: " + propertyValue.Name + ": " + propertyValue.Value.getClass());
          }
        }
      }
    }
    if (hasSortedTextId && sortedTextId < 0) {
      hasSortedTextId = false;
      MessageHandler.printToLogFile("SingleDocument: getCheckResults: SortedTextId and DocumentElementsCount are not supported by LO!");
    }
    if (debugMode > 0 && hasSortedTextId) {
      MessageHandler.printToLogFile("SingleDocument: getCheckResults: sortedTextId: " + sortedTextId);
      MessageHandler.printToLogFile("SingleDocument: getCheckResults: documentElementsCount: " + documentElementsCount);
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

    if (proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT 
        && (DocumentCursorTools.isBusy() || ViewCursorTools.isBusy() || FlatParagraphTools.isBusy() || docCache.isResetRunning())) {
      //  NOTE: LO blocks the read of information by document or view cursor tools till a PROOFINFO_GET_PROOFRESULT request is done
      //        This causes a hanging of LO when the request isn't answered immediately by a 0 matches result
      SingleCheck singleCheck = new SingleCheck(this, paragraphsCache, fixedLanguage,
          docLanguage, ignoredMatches, permanentIgnoredMatches, numParasToCheck, true, isMouseRequest, false);
      paRes.aErrors = singleCheck.checkParaRules(paraText, locale, footnotePositions, -1, paRes.nStartOfSentencePosition, lt, 0, 0, false, false);
//      docCursor = null;
//      viewCursor = null;
      return paRes;
    }
    if (debugMode > 0 && proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT) {
      MessageHandler.printToLogFile("SingleDocument: getCheckResults: start PROOFRESULT");
    }
    if (resetDocCache) {
      if (debugMode > 0 && proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT) {
        MessageHandler.printToLogFile("SingleDocument: getCheckResults: is resetDocCache");
      }
      if (docCursor == null) {
        if (debugMode > 0 && proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT) {
          MessageHandler.printToLogFile("SingleDocument: getCheckResults: get docCursor");
        }
        docCursor = getDocumentCursorTools();
      }
      if (debugMode > 0 && proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT) {
        MessageHandler.printToLogFile("SingleDocument: getCheckResults: refresh docCache");
      }
      docCache.refresh(this, LinguisticServices.getLocale(fixedLanguage), 
          LinguisticServices.getLocale(docLanguage),xComponent, 6);
      resetDocCache = false;
    }
    if (docLanguage == null) {
      docLanguage = lt.getLanguage();
    }
    if (disposed) {
      docCursor = null;
//      viewCursor = null;
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
          proofInfo, numParasToCheck, fixedLanguage, docLanguage, this, paragraphsCache, changedParas);
      long startTime = 0;
      if (debugModeTm) {
        startTime = System.currentTimeMillis();
      }
      int paraNum;
      if (hasSortedTextId) {
        paraNum = requestAnalysis.getNumberOfParagraphFromSortedTextId(sortedTextId, documentElementsCount, paraText, locale, footnotePositions);
      } else {
        paraNum = requestAnalysis.getNumberOfParagraph(nPara, paraText, locale, paRes.nStartOfSentencePosition, footnotePositions);
      }
      if (debugModeTm) {
        long runTime = System.currentTimeMillis() - startTime;
        if (runTime > OfficeTools.TIME_TOLERANCE) {
          MessageHandler.printToLogFile("Single document: Time to run request analyses: " + runTime);
        }
      }
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
//      flatPara = requestAnalysis.getFlatParagraphTools();
//      docCursor = requestAnalysis.getDocumentCursorTools();
//      viewCursor = requestAnalysis.getViewCursorTools();
      changeFrom = requestAnalysis.getFirstParagraphToChange();
      changeTo = requestAnalysis.getLastParagraphToChange();
//      numLastFlPara = requestAnalysis.getLastParaNumFromFlatParagraph();
      numLastVCPara = requestAnalysis.getLastParaNumFromViewCursor();
      boolean textIsChanged = requestAnalysis.textIsChanged();
      
      if (disposed) {
        docCursor = null;
//        viewCursor = null;
        return paRes;
      }
      if (debugModeTm) {
        startTime = System.currentTimeMillis();
      }
      SingleCheck singleCheck = new SingleCheck(this, paragraphsCache, fixedLanguage,
          docLanguage, ignoredMatches, permanentIgnoredMatches, numParasToCheck, isDialogRequest, isMouseRequest, isIntern);
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
      if (debugModeTm) {
        long runTime = System.currentTimeMillis() - startTime;
        if (runTime > OfficeTools.TIME_TOLERANCE) {
          MessageHandler.printToLogFile("Single document: Time to run single check: " + runTime);
        }
      }
      if (proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT || isIntern) {
        if (debugModeTm) {
          startTime = System.currentTimeMillis();
        }
        addSynonyms(paRes, paraText, locale, lt);
        if (debugModeTm) {
          long runTime = System.currentTimeMillis() - startTime;
          if (runTime > OfficeTools.TIME_TOLERANCE) {
            MessageHandler.printToLogFile("Single document: Time to addSynonyms: " + runTime);
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    if (ltMenus == null && docType == DocumentType.WRITER && paraText.length() > 0) {
      ltMenus = new LanguageToolMenus(xContext, xComponent, this, config);
    }
 //   docCursor = null;
 //   viewCursor = null;
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
    mDocHandler.resetSortedTextRules(mDocHandler.getLanguageTool());
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
//      if (viewCursor != null) {
//        viewCursor.setDisposed();
//      }
      if (flatPara != null) {
        flatPara.setDisposed();
      }
      ltMenus.removeListener();
      ltMenus = null;
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
//      viewCursor = null;
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
  FlatParagraphTools getFlatParagraphTools() {
    if (flatPara == null) {
      setFlatParagraphTools();
    }
    return flatPara;
  }
  
  /**
   *  Get document cursor tools
   */
  DocumentCursorTools getDocumentCursorTools() {
    OfficeTools.waitForLO();
    if (docCursor == null) {
      docCursor = new DocumentCursorTools(xComponent);
    }
    return docCursor;
  }

  /**
   *  Get document cache of the document
   */
  List<ResultCache> getParagraphsCache() {
    return paragraphsCache;
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
    if (numParasToCheck != 0 && docType == DocumentType.WRITER) {
      cacheIO = new CacheIO(xComponent);
      boolean cacheExist = cacheIO.readAllCaches(config, mDocHandler);
      if (cacheExist) {
        docCache.put(cacheIO.getDocumentCache());
        for (int i = 0; i < cacheIO.getParagraphsCache().size(); i++) {
          paragraphsCache.get(i).replace(cacheIO.getParagraphsCache().get(i));
        }
        permanentIgnoredMatches = new IgnoredMatches(cacheIO.getIgnoredMatches());
        if (docType == DocumentType.WRITER && mDocHandler != null) {
          mDocHandler.runShapeCheck(docCache.hasUnsupportedText(), 9);
        }
      }
      cacheIO.resetAllCache();
    }
  }
  
  /**
   * write caches to file
   */
  void writeCaches() {
    if (numParasToCheck != 0 && !config.noBackgroundCheck() && docType == DocumentType.WRITER) {
      DocumentCache docCache = new DocumentCache(this.docCache);
      List<ResultCache> paragraphsCache = new ArrayList<ResultCache>();
      for (int i = 0; i < this.paragraphsCache.size(); i++) {
        paragraphsCache.add(new ResultCache(this.paragraphsCache.get(i)));
      }
      cacheIO.saveCaches(docCache, paragraphsCache, permanentIgnoredMatches, config, mDocHandler);
    }
  }
  
  /** 
   * Reset all caches of the document
   */
  void resetResultCache() {
    for (int i = 0; i < OfficeTools.NUMBER_TEXTLEVEL_CACHE; i++) {
      paragraphsCache.get(i).removeAll();
    }
  }
  
  /**
   * remove all cached matches for one paragraph
   */
  public void removeResultCache(int nPara, boolean alsoParaLevel) {
    if (!isDisposed()) {
      if (alsoParaLevel) {
        paragraphsCache.get(0).remove(nPara);
      }
      if (!docCache.setSingleParagraphsCacheToNull(nPara, paragraphsCache)) {
        //  NOTE: Don't remove paragraph cache 0. It is needed to set correct markups
        for (int i = 1; i < paragraphsCache.size(); i++) {
          paragraphsCache.get(i).remove(nPara);
        }
      }
    }
  }
  
  /**
   * Remove a special Proofreading error from all caches of document
   */
  public void removeRuleError(String ruleId) {
    List<Integer> allChanged = new ArrayList<>();
    for (ResultCache cache : paragraphsCache) {
      List<Integer> changed = cache.removeRuleError(ruleId);
      if (changed.size() > 0) {
        for (int n : changed) {
          if (!allChanged.contains(n)) {
            allChanged.add(n);
          }
        }
      }
    }
    if (allChanged.size() > 0) {
      allChanged.sort(null);
      remarkChangedParagraphs(allChanged, true);
    }
  }
  
  /** 
   * Open new flat paragraph tools or initialize them again
   */
  public FlatParagraphTools setFlatParagraphTools() {
	  if (disposed) {
      flatPara = null;
      return flatPara;
	  }
    OfficeTools.waitForLO();
	  if (flatPara == null) {
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
    if (!disposed && mDocHandler.getTextLevelCheckQueue() != null && mDocHandler.isSortedRuleForIndex(nCache) && 
        docCache != null && !docCache.isSingleParagraph(nFPara)) {
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
      if ((nStart == nPara.number || (nPara.number == 0
              || paragraphsCache.get(nCache).getCacheEntry(docCache.getFlatParagraphNumber(new TextParagraph(nPara.type, nPara.number - 1))) != null)) 
          && (nEnd == nPara.number || nPara.number == docCache.textSize(nPara) - 1
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
              && (paragraphsCache.get(nCache).getCacheEntry(docCache.getFlatParagraphNumber(nPara)) == null && 
                  !docCache.isSingleParagraph(docCache.getFlatParagraphNumber(nPara)))) {
            return createQueueEntry(nPara, nCache);
          }
        }
      }
      int nStart = (nPara == null || nPara.type == DocumentCache.CURSOR_TYPE_UNKNOWN || nPara.number < docCache.textSize(nPara)) ? 
          0 : docCache.getFlatParagraphNumber(nPara);
      for (int i = nStart; i < docCache.size(); i++) {
        if (docCache.getNumberOfTextParagraph(i).type != DocumentCache.CURSOR_TYPE_UNKNOWN) {
          for (int nCache = 1; nCache < paragraphsCache.size(); nCache++) {
            if (mDocHandler.isSortedRuleForIndex(nCache) && docCache.isFinished() && 
                (paragraphsCache.get(nCache).getCacheEntry(i) == null  && !docCache.isSingleParagraph(i))) {
              return createQueueEntry(docCache.getNumberOfTextParagraph(i), nCache);
            }
          }
        }
      }
      for (int i = 0; i < nStart && i < docCache.size(); i++) {
        if (docCache.getNumberOfTextParagraph(i).type != DocumentCache.CURSOR_TYPE_UNKNOWN) {
          for (int nCache = 1; nCache < paragraphsCache.size(); nCache++) {
            if (mDocHandler.isSortedRuleForIndex(nCache) && docCache.isFinished() && 
                (paragraphsCache.get(nCache).getCacheEntry(i) == null  && !docCache.isSingleParagraph(i))) {
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
      Set<Integer> nParas = new HashSet<Integer>(changedParas.keySet());
      for (int nPara : nParas) {
        OfficeTools.waitForLO();
        String sPara = flatPara.getFlatParagraphAt(nPara).getText();
        if (sPara != null) {
          String sChangedPara = changedParas.get(nPara);
          changedParas.remove(nPara);
          if (sChangedPara != null && !sChangedPara.equals(sPara)) {
            docCache.setFlatParagraph(nPara, sPara);
//            if (!disposed) {
//              mDocHandler.handleLtDictionary(sPara, docCache.getFlatParagraphLocale(nPara));
//            }
            removeResultCache(nPara, false);
            return createQueueEntry(docCache.getNumberOfTextParagraph(nPara), 0);
          }
        }
      }
    }
    return null;
  }
  
  public void addShapeQueueEntries() {
    int shapeTextSize = docCache.textSize(DocumentCache.CURSOR_TYPE_SHAPE) + docCache.textSize(DocumentCache.CURSOR_TYPE_TABLE);
    if (shapeTextSize > 0) {
      if (docCursor == null) {
        docCursor = getDocumentCursorTools();
      }
      List<Integer> changedParas = docCache.getChangedUnsupportedParagraphs(docCursor, paragraphsCache.get(0));
      if (changedParas != null) { 
        for (int i = 0; i < changedParas.size(); i++) {
          for (int nCache = 0; nCache < paragraphsCache.size(); nCache++) {
            int nCheck = mDocHandler.getNumMinToCheckParas().get(nCache);
            addQueueEntry(changedParas.get(i), nCache, nCheck, docID, false, true);
          }
        }
      }
    }
  }

  /**
   * run a text level check from a queue entry (initiated by the queue)
   */
  public void runQueueEntry(TextParagraph nStart, TextParagraph nEnd, int cacheNum, int nCheck, boolean override, SwJLanguageTool lt) {
    if (!disposed && flatPara != null && docCache.isFinished() && nStart.number < docCache.textSize(nStart)) {
      SingleCheck singleCheck = new SingleCheck(this, paragraphsCache,
          fixedLanguage, docLanguage, ignoredMatches, permanentIgnoredMatches, numParasToCheck, false, false, false);
      singleCheck.addParaErrorsToCache(docCache.getFlatParagraphNumber(nStart), lt, cacheNum, nCheck, 
          nEnd.number == nStart.number + 1, override, false, hasFootnotes);
//      docCursor = null;
    }
  }
  
  private void remarkChangedParagraphs(List<Integer> changedParas, boolean isIntern) {
    if (!disposed) {
      SingleCheck singleCheck = new SingleCheck(this, paragraphsCache, fixedLanguage, docLanguage, 
          ignoredMatches, permanentIgnoredMatches, numParasToCheck, false, false, isIntern);
      singleCheck.remarkChangedParagraphs(changedParas, mDocHandler.getLanguageTool(), true);
//      docCursor = null;
    }
  }

/**
 * Renew text markups for paragraphs under view cursor
 */
  public void renewMarkups() {
    if (disposed) {
      return;
    }
    ViewCursorTools viewCursor = new ViewCursorTools(xComponent);
    int y = docCache.getFlatParagraphNumber(viewCursor.getViewCursorParagraph());
    if (debugMode > 0) {
      MessageHandler.printToLogFile("SingleDocument: renewMarkups: Number of Flat Paragraph = " + y);
    }
    List<Integer> changedParas = new ArrayList<Integer>();
    changedParas.add(y);
    remarkChangedParagraphs(changedParas, false);
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
    ViewCursorTools viewCursor = new ViewCursorTools(xComponent);
    int y = docCache.getFlatParagraphNumber(viewCursor.getViewCursorParagraph());
    int x = viewCursor.getViewCursorCharacter();
    String ruleId = getRuleIdFromCache(y, x).ruleID;
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
   * reset the permanent ignore cache
   */
  public void resetIgnorePermanent() {
    List<Integer> changedParas = permanentIgnoredMatches.getAllParagraphs();
    permanentIgnoredMatches = new IgnoredMatches();
    remarkChangedParagraphs(changedParas, false);
  }
  
  /**
   * add a ignore once entry to queue and remove the mark
   */
  public String ignorePermanent() {
    if (disposed) {
      return null;
    }
    ViewCursorTools viewCursor = new ViewCursorTools(xComponent);
    int y = docCache.getFlatParagraphNumber(viewCursor.getViewCursorParagraph());
    int x = viewCursor.getViewCursorCharacter();
    String ruleId = getRuleIdFromCache(y, x).ruleID;
    setPermanentIgnoredMatch (x, y, ruleId, false);
    return docID;
  }
  
  /**
   * add a ignore once entry for point x, y to queue and remove the mark
   */
  public void setPermanentIgnoredMatch(int x, int y, String ruleId, boolean isIntern) {
    permanentIgnoredMatches.setIgnoredMatch(x, y, ruleId);
    if (debugMode > 1) {
      MessageHandler.printToLogFile("SingleDocument: setPermanentIgnoredMatch: DocumentType = " + docType + "; numParasToCheck = " + numParasToCheck);
    }
    if (docType == DocumentType.WRITER && numParasToCheck != 0) {
      List<Integer> changedParas = new ArrayList<>();
      changedParas.add(y);
      remarkChangedParagraphs(changedParas, isIntern);
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("SingleDocument: setPermanentIgnoredMatch: Ignore Match added at: paragraph: " + y + "; character: " + x + "; ruleId: " + ruleId);
    }
  }
  
  public void setPermanentIgnoredMatches(IgnoredMatches ignoredMatches) {
    List<Integer> changedParas = permanentIgnoredMatches.getAllParagraphs();
    permanentIgnoredMatches = ignoredMatches;
    remarkChangedParagraphs(changedParas, false);
    changedParas = permanentIgnoredMatches.getAllParagraphs();
    remarkChangedParagraphs(changedParas, false);
  }
  
  public IgnoredMatches getPermanentIgnoredMatches() {
    return permanentIgnoredMatches;
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
   * remove a ignore Permanent entry for point x, y from queue and set the mark
   * if x < 0 remove all ignore once entries for paragraph y
   */
  public void removePermanentIgnoredMatch(int x, int y, String ruleId, boolean isIntern) {
    permanentIgnoredMatches.removeIgnoredMatch(x, y, ruleId);
    if (numParasToCheck != 0) {
      List<Integer> changedParas = new ArrayList<>();
      changedParas.add(y);
      remarkChangedParagraphs(changedParas, isIntern);
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("SingleDocument: removePermanentIgnoredMatch: Ignore Match removed at: paragraph: " + y + "; character: " + x);
    }
  }
  
  /**
   * get a rule ID of an error out of the cache 
   * by the position of the error (flat paragraph number and number of character)
   */
  private RuleDesc getRuleIdFromCache(int nPara, int nChar) {
    List<SingleProofreadingError> tmpErrors = new ArrayList<SingleProofreadingError>();
    if (nPara < 0 || nPara >= docCache.size()) {
      MessageHandler.printToLogFile("SingleDocument: getRuleIdFromCache(nPara = " + nPara + ", docCache.size() = " + docCache.size() + "): nPara out of range!");
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
      return new RuleDesc(docCache.getFlatParagraphLocale(nPara), errors[0].aRuleIdentifier);
    } else {
      MessageHandler.printToLogFile("SingleDocument: getRuleIdFromCache(nPara = " + nPara + ", nChar = " + nChar + "): No ruleId found!");
      return null;
    }
  }
  
  /**
   * get a rule ID of an error from a check 
   * by the position of the error (number of character)
   */
  private RuleDesc getRuleIdFromCheck(int nChar, ViewCursorTools viewCursor) {
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
            return new RuleDesc(paRes.aLocale, error.aRuleIdentifier);
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
  public RuleDesc deactivateRule() {
    if (disposed) {
      return null;
    }
    ViewCursorTools viewCursor = new ViewCursorTools(xComponent);
    int x = viewCursor.getViewCursorCharacter();
    if (numParasToCheck == 0) {
      return getRuleIdFromCheck(x, viewCursor);
    }
    int y = docCache.getFlatParagraphNumber(viewCursor.getViewCursorParagraph());
    return getRuleIdFromCache(y, x);
  }
  
  /**
   * class for store and handle ignored matches
   */
  public static class IgnoredMatches {
    
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

    /**
     * get all paragraphs containing ignored matches
     */
    public List<Integer> getAllParagraphs() {
      return new ArrayList<Integer>(ignoredMatches.keySet());
    }
  }
  
  private void addSynonyms(ProofreadingResult paRes, String para, Locale locale, SwJLanguageTool lt) throws IOException {
    LinguisticServices linguServices = mDocHandler.getLinguisticServices();
    if (linguServices != null) {
      for (SingleProofreadingError error : paRes.aErrors) {
        if ((error.aSuggestions == null || error.aSuggestions.length == 0) 
            && linguServices.isThesaurusRelevantRule(error.aRuleIdentifier)) {
          String word = para.substring(error.nErrorStart, error.nErrorStart + error.nErrorLength);
          List<String> suggestions = new ArrayList<>();
          List<String> lemmas = lt.getLemmasOfWord(word);
          int num = 0;
          for (String lemma : lemmas) {
            if (debugMode > 0) {
              MessageHandler.printToLogFile("SingleDocument: addSynonyms: Find Synonyms for lemma:" + lemma);
            }
            List<String> synonyms = linguServices.getSynonyms(lemma, locale);
            for (String synonym : synonyms) {
              synonym = synonym.replaceAll("\\(.*\\)", "").trim();
              if (!synonym.isEmpty() && !suggestions.contains(synonym)) {
                suggestions.add(synonym);
                num++;
              }
              if (num >= OfficeTools.MAX_SUGGESTIONS) {
                break;
              }
            }
            if (num >= OfficeTools.MAX_SUGGESTIONS) {
              break;
            }
          }
          if (!suggestions.isEmpty()) {
            error.aSuggestions = suggestions.toArray(new String[suggestions.size()]);
          }
        }
      }
    }
  }
  
  private void setDokumentListener(XComponent xComponent) {
    try {
      if (!disposed && xComponent != null && eventListener == null) {
        eventListener = new LTDokumentEventListener();
        XDocumentEventBroadcaster broadcaster = UnoRuntime.queryInterface(XDocumentEventBroadcaster.class, xComponent);
        if (!disposed && broadcaster != null) {
          broadcaster.addDocumentEventListener(eventListener);
        } else {
          MessageHandler.printToLogFile("SingleDocument: setDokumentListener: Could not add document event listener!");
        }
        XModel xModel = UnoRuntime.queryInterface(XModel.class, xComponent);
        if (disposed || xModel == null) {
          MessageHandler.printToLogFile("SingleDocument: setDokumentListener: XModel not found!");
          return;
        }
        XController xController = xModel.getCurrentController();
        if (disposed || xController == null) {
          MessageHandler.printToLogFile("SingleDocument: setDokumentListener: XController not found!");
          return;
        }
        XUserInputInterception xUserInputInterception = UnoRuntime.queryInterface(XUserInputInterception.class, xController);
        if (disposed || xUserInputInterception == null) {
          MessageHandler.printToLogFile("SingleDocument: setDokumentListener: XUserInputInterception not found!");
          return;
        }
        xUserInputInterception.addMouseClickHandler(eventListener);
//        xUserInputInterception.addKeyHandler(eventListener);
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);
    }
  }
  
  public void removeDokumentListener(XComponent xComponent) {
    if (eventListener != null) {
      XDocumentEventBroadcaster broadcaster = UnoRuntime.queryInterface(XDocumentEventBroadcaster.class, xComponent);
      if (broadcaster != null) {
        broadcaster.removeDocumentEventListener(eventListener);
      }
    }
  }
  
  public static class RuleDesc {
    String langCode;
    String ruleID;
    
    RuleDesc(Locale locale, String ruleID) {
      langCode = OfficeTools.localeToString(locale);
      this.ruleID = ruleID;
    }
  }
  
//  private class LTDokumentEventListener implements XDocumentEventListener, XMouseClickHandler, XKeyHandler {
  private class LTDokumentEventListener implements XDocumentEventListener, XMouseClickHandler {

    @Override
    public void disposing(EventObject event) {
    }

    @Override
    public void documentEventOccured(DocumentEvent event) {
      if(event.EventName.equals("OnUnload")) {
        isOnUnload = true;
      } else if(event.EventName.equals("OnUnfocus") && !isOnUnload) {
        mDocHandler.getCurrentDocument();
      } else if(event.EventName.equals("OnSave") && config.saveLoCache()) {
        writeCaches();
      } else if(event.EventName.equals("OnSaveAsDone") && config.saveLoCache()) {
//        writeCaches();
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
/*
    @Override
    public boolean keyPressed(KeyEvent arg0) {
      return false;
    }

    @Override
    public boolean keyReleased(KeyEvent arg0) {
      MessageHandler.printToLogFile("SingleDocument: setDokumentListener: Set Timestamp");
      OfficeTools.setKeyReleaseTime(System.currentTimeMillis());
      return false;
    }
*/
  }

}
