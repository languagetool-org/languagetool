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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.languagetool.Language;
import org.languagetool.gui.Configuration;
import org.languagetool.openoffice.TextLevelCheckQueue.QueueEntry;

import com.sun.star.beans.PropertyValue;
import com.sun.star.document.DocumentEvent;
import com.sun.star.document.XDocumentEventBroadcaster;
import com.sun.star.document.XDocumentEventListener;
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
  
  private static final int PARA_CHECK_DEFAULT = 50;  //  Factor for parameter checked at once at iteration (no text change)

  private static int debugMode;                   //  should be 0 except for testing; 1 = low level; 2 = advanced level
  
  private Configuration config;

  private int defaultParaCheck = 10;              // will be overwritten by config
  private int numParasToCheck = 0;                // current number of Paragraphs to be checked

  private XComponentContext xContext;             //  The context of the document
  private String docID;                           //  docID of the document
  private XComponent xComponent;                  //  XComponent of the open document
  private final MultiDocumentsHandler mDocHandler;      //  handles the different documents loaded in LO/OO
  private LTDokumentEventListener eventListener = null; //  listens for save of document 
  
  private DocumentCache docCache = null;          //  cache of paragraphs (only readable by parallel thread)
  private DocumentCursorTools docCursor = null;   //  Save document cursor for the single document
  private ViewCursorTools viewCursor = null;      //  Get the view cursor for desktop
  private FlatParagraphTools flatPara = null;     //  Save information for flat paragraphs (including iterator and iterator provider) for the single document
  private Integer numLastVCPara = 0;              //  Save position of ViewCursor for the single documents
  private Integer numLastFlPara = -1;             //  Save position of FlatParagraph for the single documents
  private List<ResultCache> paragraphsCache;      //  Cache for matches of text rules
  private CacheIO cacheIO;
  private int changeFrom = 0;                     //  Change result cache from paragraph
  private int changeTo = 0;                       //  Change result cache to paragraph
  private int paraNum;                            //  Number of current checked paragraph
  private int lastChangedPara;                    //  lastPara which was detected as changed
  private List<Integer> lastChangedParas;         //  lastPara which was detected as changed
  private IgnoredMatches ignoredMatches;          //  Map of matches (number of paragraph, number of character) that should be ignored after ignoreOnce was called
  private boolean isImpress = false;              //  true: is an Impress document 
  private boolean disposed = false;               //  true: document with this docId is disposed - SingleDocument shall be removed
  private boolean resetDocCache = false;          //  true: the cache of the document should be reseted before the next check
  private boolean hasFootnotes = true;            //  true: Footnotes are supported by LO/OO
  private String lastSinglePara = null;           //  stores the last paragraph which is checked as single paragraph
  private Language docLanguage = null;            //  Language used for check
  private LanguageToolMenus ltMenus = null;       //  LT menus (tools menu and context menu)

  SingleDocument(XComponentContext xContext, Configuration config, String docID, 
      XComponent xComponent, MultiDocumentsHandler mDH) {
    debugMode = OfficeTools.DEBUG_MODE_SD;
    this.xContext = xContext;
    this.config = config;
    this.docID = docID;
    if (docID.charAt(0) == 'I') {
      isImpress = true;
    }
    this.xComponent = xComponent;
    setDokumentListener(xComponent);
    this.mDocHandler = mDH;
    this.paragraphsCache = new ArrayList<>();
    for (int i = 0; i < OfficeTools.NUMBER_TEXTLEVEL_CACHE; i++) {
      paragraphsCache.add(new ResultCache());
    }
    if (config != null) {
      setConfigValues(config);
    }
    resetCache();
    ignoredMatches = new IgnoredMatches();
    if (config != null && config.saveLoCache() && xComponent != null && !mDocHandler.isTestMode()) {
      readCaches();
    }
    if (xComponent != null) {
      setFlatParagraphTools(xComponent);
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
    
    int [] footnotePositions = null;  // e.g. for LO/OO < 4.3 and the 'FootnotePositions' property
    int proofInfo = OfficeTools.PROOFINFO_UNKNOWN;  //  OO and LO < 6.5 do not support ProofInfo
    for (PropertyValue propertyValue : propertyValues) {
      if ("FootnotePositions".equals(propertyValue.Name)) {
        if (propertyValue.Value instanceof int[]) {
          footnotePositions = (int[]) propertyValue.Value;
        } else {
          MessageHandler.printToLogFile("Not of expected type int[]: " + propertyValue.Name + ": " + propertyValue.Value.getClass());
        }
      }
      if ("ProofInfo".equals(propertyValue.Name)) {
        if (propertyValue.Value instanceof Integer) {
          proofInfo = (int) propertyValue.Value;
        } else {
          MessageHandler.printToLogFile("Not of expected type int: " + propertyValue.Name + ": " + propertyValue.Value.getClass());
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

    if (resetDocCache && nPara >= 0) {
      docCache = null;
      resetDocCache = false;
    }
    if (docLanguage == null) {
      docLanguage = lt.getLanguage();
    }
    if (!isImpress && ltMenus == null) {
      ltMenus = new LanguageToolMenus(xContext, this, config);
    }
    
    try {
      if (docReset) {
        numLastVCPara = 0;
        ignoredMatches = new IgnoredMatches();
      }
      boolean isIntern = nPara < 0 ? false : true;
      boolean isDialogRequest = (nPara >= 0 || proofInfo == OfficeTools.PROOFINFO_GET_PROOFRESULT);
      
      CheckRequestAnalysis requestAnalysis = new CheckRequestAnalysis(numLastVCPara, numLastFlPara, defaultParaCheck, 
          proofInfo, numParasToCheck, this, paragraphsCache, viewCursor);
      int paraNum = requestAnalysis.getNumberOfParagraph(nPara, paraText, locale, paRes.nStartOfSentencePosition, footnotePositions);
      this.paraNum = paraNum;
      flatPara = requestAnalysis.getFlatParagraphTools();
      docCursor = requestAnalysis.getDocumentCursorTools();
      viewCursor = requestAnalysis.getViewCursorTools();
      changeFrom = requestAnalysis.getFirstParagraphToChange();
      changeTo = requestAnalysis.getLastParagraphToChange();
      numLastFlPara = requestAnalysis.getLastParaNumFromFlatParagraph();
      numLastVCPara = requestAnalysis.getLastParaNumFromViewCursor();
      boolean textIsChanged = requestAnalysis.textIsChanged();
      
      SingleCheck singleCheck = new SingleCheck(this, paragraphsCache, docCursor, flatPara, 
          docLanguage, ignoredMatches, numParasToCheck, isDialogRequest);
      paRes.aErrors = singleCheck.getCheckResults(paraText, footnotePositions, locale, lt, paraNum, 
          paRes.nStartOfSentencePosition, textIsChanged, changeFrom, changeTo, lastSinglePara, lastChangedPara, isIntern);
      lastSinglePara = singleCheck.getLastSingleParagraph();
      paRes.nStartOfSentencePosition = paragraphsCache.get(0).getStartSentencePosition(paraNum, paRes.nStartOfSentencePosition);
      paRes.nStartOfNextSentencePosition = paragraphsCache.get(0).getNextSentencePosition(paraNum, paRes.nStartOfSentencePosition);
      paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
      lastChangedPara = (textIsChanged && numParasToCheck != 0) ? paraNum : -1;
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return paRes;
  }
  
  /**
   * set values set by configuration dialog
   */
  void setConfigValues(Configuration config) {
    this.config = config;
    numParasToCheck = (mDocHandler.isTestMode() || mDocHandler.heapLimitIsReached()) ? 0 : config.getNumParasToCheck();
    defaultParaCheck = PARA_CHECK_DEFAULT;
    if (ltMenus != null) {
      ltMenus.setConfigValues(config);
    }
    if (config.noBackgroundCheck() || numParasToCheck == 0) {
      setFlatParagraphTools(xComponent);
    }
  }

  /**
   * set the document cache - use only for tests
   * @since 5.3
   */
  void setDocumentCacheForTests(List<String> paragraphs, List<String> textParagraphs, List<int[]> footnotes, Locale locale) {
    docCache = new DocumentCache(paragraphs, textParagraphs, footnotes, locale);
    numParasToCheck = -1;
    mDocHandler.resetSortedTextRules();
  }
  
  /** Set LanguageTool menu
   */
  void setLtMenus(LanguageToolMenus ltMenus) {
    this.ltMenus = ltMenus;
  }
  
  /** Get LanguageTool menu
   */
  LanguageToolMenus getLtMenu() {
    return ltMenus;
  }
  
  /**
   * set menu ID to MultiDocumentsHandler
   */
  void dispose() {
    disposed = true;
  }
  
  /**
   * is an Impress document
   */
  boolean isImpress() {
    return isImpress;
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
  
  /** Set XComponentContext and XComponent of the document
   */
  void setXComponent(XComponentContext xContext, XComponent xComponent) {
    this.xContext = xContext;
    this.xComponent = xComponent;
    setDokumentListener(xComponent);
  }
  
  /** Get xComponent of the document
   */
  XComponent getXComponent() {
    return xComponent;
  }
  
  /** Get MultiDocumentsHandler
   */
  MultiDocumentsHandler getMultiDocumentsHandler() {
    return mDocHandler;
  }
  
  /** Get ID of the document
   */
  String getDocID() {
    return docID;
  }
  
  /** Get ID of the document
   */
  void setDocID(String docId) {
    docID = docId;
  }
  
  /** Get flat paragraph tools of the document
   */
  FlatParagraphTools getFlatParagraphTools () {
    return flatPara;
  }
  
  /** Get document cache of the document
   */
  DocumentCache getDocumentCache() {
    return docCache;
  }
  
  /** Set document cache of the document
   */
  void setDocumentCache(DocumentCache docCache) {
    this.docCache = docCache;
  }
  
  /** reset document cache of the document
   */
  void resetDocumentCache() {
    resetDocCache = true;
  }
  
  /** set last changed paragraphs
   */
  void setLastChangedParas(List<Integer> lastChangedParas) {
    this.lastChangedParas = lastChangedParas;
  }
  
  /** get last changed paragraphs
   */
  List<Integer> getLastChangedParas() {
    return lastChangedParas;
  }
  
  /** Update document cache and get it
   */
  DocumentCache getUpdatedDocumentCache(int nPara) {
    CheckRequestAnalysis requestAnalysis = new CheckRequestAnalysis(numLastVCPara, numLastFlPara, defaultParaCheck, 
        OfficeTools.PROOFINFO_GET_PROOFRESULT, numParasToCheck, this, paragraphsCache, viewCursor);
    docCache = requestAnalysis.actualizeDocumentCache(nPara);
    return docCache;
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
        docCache = cacheIO.getDocumentCache();
        paragraphsCache = cacheIO.getParagraphsCache();
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
      cacheIO.saveCaches(xComponent, docCache, paragraphsCache, ignoredMatches, config, mDocHandler);
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
  public FlatParagraphTools setFlatParagraphTools(XComponent xComponent) {
    if (flatPara == null) {
      flatPara = new FlatParagraphTools(xComponent);
    } else {
      flatPara.init();
    }
    return flatPara;
  }
  
  /** 
   * Set flat paragraph tools from other class
   *//*
  public void setFlatParagraphTools(FlatParagraphTools flatPara) {
    if (flatPara != null) {
      this.flatPara = flatPara;
    }
  }
  
  /**
   * Add an new entry to text level queue
   * nFPara is number of flat paragraph
   */
  public void addQueueEntry(int nFPara, int nCache, int nCheck, String docId, boolean checkOnlyParagraph, boolean overrideRunning) {
    if (mDocHandler.isSortedRuleForIndex(nCache) && docCache != null) {
      int nTPara = docCache.getNumberOfTextParagraph(nFPara);
      if (nTPara >= 0) {
        int nStart;
        int nEnd;
        if (checkOnlyParagraph && nCheck > 0) {
          nStart = nTPara;
          nEnd = nTPara + 1;
        } else {
          nStart = docCache.getStartOfParaCheck(nTPara, nCheck, overrideRunning, true, false);
          nEnd = docCache.getEndOfParaCheck(nTPara, nCheck, overrideRunning, true, false);
        }
        mDocHandler.getTextLevelCheckQueue().addQueueEntry(nStart, nEnd, nCache, nCheck, docId, overrideRunning);
      }
    }
  }
  
  /**
   * create a queue entry 
   * used by getNextQueueEntry
   */
  private QueueEntry createQueueEntry(int nPara, int nCache) {
    int nCheck = mDocHandler.getNumMinToCheckParas().get(nCache);
    int nStart = docCache.getStartOfParaCheck(nPara, nCheck, false, true, false);
    int nEnd = docCache.getEndOfParaCheck(nPara, nCheck, false, true, false);
    return mDocHandler.getTextLevelCheckQueue().createQueueEntry(nStart, nEnd, nCache, nCheck, docID, false);
  }

  /**
   * get the next queue entry which is the next empty cache entry
   */
  public QueueEntry getNextQueueEntry(int nPara) {
    if (docCache != null) {
      if (nPara >= 0 && nPara < docCache.textSize()) {
        for (int nCache = 1; nCache < paragraphsCache.size(); nCache++) {
          if (mDocHandler.isSortedRuleForIndex(nCache) && docCache.isFinished() && paragraphsCache.get(nCache).getCacheEntry(docCache.getFlatParagraphNumber(nPara)) == null) {
            return createQueueEntry(nPara, nCache);
          }
        }
      }
      for (int i = nPara + 1; i < docCache.textSize(); i++) {
        for (int nCache = 1; nCache < paragraphsCache.size(); nCache++) {
          if (mDocHandler.isSortedRuleForIndex(nCache) && docCache.isFinished() && paragraphsCache.get(nCache).getCacheEntry(docCache.getFlatParagraphNumber(i)) == null) {
            return createQueueEntry(i, nCache);
          }
        }
      }
      for (int i = 0; i < nPara && i < docCache.textSize(); i++) {
        for (int nCache = 1; nCache < paragraphsCache.size(); nCache++) {
          if (mDocHandler.isSortedRuleForIndex(nCache) && docCache.isFinished() && paragraphsCache.get(nCache).getCacheEntry(docCache.getFlatParagraphNumber(i)) == null) {
            return createQueueEntry(i, nCache);
          }
        }
      }
    }
    return null;
  }

  /**
   * run a text level check from a queue entry (initiated by the queue)
   */
  public void runQueueEntry(int nStart, int nEnd, int cacheNum, int nCheck, boolean override, SwJLanguageTool lt) {
    if (flatPara != null && docCache.isFinished()) {
      SingleCheck singleCheck = new SingleCheck(this, paragraphsCache, docCursor, flatPara, docLanguage, ignoredMatches, numParasToCheck, false);
      singleCheck.addParaErrorsToCache(docCache.getFlatParagraphNumber(nStart), lt, cacheNum, nCheck, nEnd == nStart + 1, override, false, hasFootnotes);
    }
  }
  
  private void remarkChangedParagraphs(List<Integer> changedParas) {
    SingleCheck singleCheck = new SingleCheck(this, paragraphsCache, docCursor, flatPara, docLanguage, ignoredMatches, numParasToCheck, false);
    if (docCursor == null) {
      docCursor = new DocumentCursorTools(xComponent);
    }
    singleCheck.remarkChangedParagraphs(changedParas, docCursor.getParagraphCursor(), flatPara, mDocHandler.getLanguageTool(), true);
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
    ViewCursorTools viewCursor = new ViewCursorTools(xContext);
    int y = docCache.getFlatParagraphNumber(viewCursor.getViewCursorParagraph());
    int x = viewCursor.getViewCursorCharacter();
    String ruleId = getRuleIdFromCache(y, x);
    setIgnoredMatch (x, y, ruleId);
    return docID;
  }
  
  /**
   * add a ignore once entry for point x, y to queue and remove the mark
   */
  public void setIgnoredMatch(int x, int y, String ruleId) {
    ignoredMatches.setIgnoredMatch(x, y, ruleId);
    if (!isImpress && numParasToCheck != 0) {
      List<Integer> changedParas = new ArrayList<>();
      changedParas.add(y);
      remarkChangedParagraphs(changedParas);
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("Ignore Match added at: paragraph: " + y + "; character: " + x + "; ruleId: " + ruleId);
    }
  }
  
  /**
   * remove all ignore once entries for paragraph y from queue and set the mark
   */
  public void removeAndShiftIgnoredMatch(int from, int to, int oldSize, int newSize) {
    if (!ignoredMatches.isEmpty()) {
      IgnoredMatches tmpIgnoredMatches = new IgnoredMatches();
      for (int i = 0; i < from; i++) {
        if (ignoredMatches.containsKey(i)) {
          tmpIgnoredMatches.put(i, ignoredMatches.get(i));
        }
      }
      for (int i = to + 1; i < oldSize; i++) {
        int n = i + newSize - oldSize;
        if (ignoredMatches.containsKey(i)) {
          tmpIgnoredMatches.put(n, ignoredMatches.get(i));
        }
      }
      ignoredMatches = tmpIgnoredMatches;
    }
  }
  
  /**
   * remove all ignore once entries for paragraph y from queue and set the mark
   */
  public void removeIgnoredMatch(int y) {
    ignoredMatches.removeIgnoredMatches(y);
    if (numParasToCheck != 0 && flatPara != null) {
      List<Integer> changedParas = new ArrayList<>();
      changedParas.add(y);
      remarkChangedParagraphs(changedParas);
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("All Ignored Matches removed at: paragraph: " + y);
    }
  }
  
  /**
   * remove a ignore once entry for point x, y from queue and set the mark
   * if x < 0 remove all ignore once entries for paragraph y
   */
  public void removeIgnoredMatch(int x, int y, String ruleId) {
    ignoredMatches.removeIgnoredMatch(x, y, ruleId);
    if (numParasToCheck != 0) {
      List<Integer> changedParas = new ArrayList<>();
      changedParas.add(y);
      remarkChangedParagraphs(changedParas);
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("Ignore Match removed at: paragraph: " + y + "; character: " + x);
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
          MessageHandler.printToLogFile("Error[" + i + "]: ruleID: " + errors[i].aRuleIdentifier + ", Start = " + errors[i].nErrorStart + ", Length = " + errors[i].nErrorLength);
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
    MessageHandler.printToLogFile("getRuleIdFromCache: No ruleId found");
    return null;
  }
  
  /**
   * get back the rule ID to deactivate a rule
   */
  public String deactivateRule() {
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
    public boolean containsKey(int y) {
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
        MessageHandler.printToLogFile("Could not add document event listener!");
      }
    }
  }
  
  class LTDokumentEventListener implements XDocumentEventListener {

    @Override
    public void disposing(EventObject event) {
    }

    @Override
    public void documentEventOccured(DocumentEvent event) {
//      MessageHandler.printToLogFile("Document Event: " + event.EventName);
      if (event.EventName.equals("OnSave") && config.saveLoCache()) {
        writeCaches();
      }
    }
  }

}
