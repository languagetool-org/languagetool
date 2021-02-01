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

import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.gui.Configuration;
import org.languagetool.openoffice.TextLevelCheckQueue.QueueEntry;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.document.DocumentEvent;
import com.sun.star.document.XDocumentEventBroadcaster;
import com.sun.star.document.XDocumentEventListener;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.text.TextMarkupType;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import static java.lang.System.arraycopy;

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

  private static int debugMode;               //  should be 0 except for testing; 1 = low level; 2 = advanced level
  
  private Configuration config;

  private int defaultParaCheck = 10;              // will be overwritten by config
  private int numParasToCheck = 0;                // current number of Paragraphs to be checked

  private XComponentContext xContext;             //  The context of the document
  private String docID;                           //  docID of the document
  private XComponent xComponent;                  //  XComponent of the open document
  private MultiDocumentsHandler mDocHandler;
  private LTDokumentEventListener eventListener = null;
  
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
  private int numParasToChange = 1;               //  Number of paragraphs to change for n-paragraph cache
  private List<Integer> changedParas = null;      //  List of changed paragraphs after editing the document
  private Set<Integer> textIsChanged;             //  false: check number of paragraphs again (ignored by parallel thread)
  private Set<Integer> resetCheck;                //  true: the whole text has to be checked again (use cache)
  private Set<Integer> isDialogRequest;           //  true: check was initiated by right mouse click or proofreading dialog
  private int paraNum;                            //  Number of current checked paragraph
  private List<Integer> minToCheckPara;           //  List of minimal to check paragraphs for different classes of text level rules
  private IgnoredMatches ignoredMatches;          //  Map of matches (number of paragraph, number of character) that should be ignored after ignoreOnce was called
  private boolean useQueue = true;                //  true: use queue to check text level rules (will be overridden by config)
  private boolean disposed = false;               //  true: document with this docId is disposed - SingleDocument shall be removed
  private boolean resetDocCache= false;           //  true: the cache of the document should be reseted before the next check
  private String lastSinglePara = null;           //  stores the last paragraph which is checked as single paragraph
  private boolean isFixedLanguage = false;
  private Language docLanguage = null;
  private LanguageToolMenus ltMenus = null;
  int[] footnotePositions = null;
  
  int proofInfo = 0;

  SingleDocument(XComponentContext xContext, Configuration config, String docID, 
      XComponent xComponent, MultiDocumentsHandler mDH) {
    debugMode = OfficeTools.DEBUG_MODE_SD;
    this.xContext = xContext;
    this.config = config;
    this.docID = docID;
    this.xComponent = xComponent;
    setDokumentListener(xComponent);
    this.mDocHandler = mDH;
    this.paragraphsCache = new ArrayList<>();
    for (int i = 0; i < OfficeTools.NUMBER_TEXTLEVEL_CACHE; i++) {
      paragraphsCache.add(new ResultCache());
    }
    this.textIsChanged = new HashSet<>();
    this.isDialogRequest = new HashSet<>();
    this.resetCheck = new HashSet<>();
    if (config != null) {
      setConfigValues(config);
    }
    resetCache();
    ignoredMatches = new IgnoredMatches();
    if (config != null && config.saveLoCache() && xComponent != null && !mDocHandler.isTestMode()) {
      readCaches();
    }
  }
  
  /**  get the result for a check of a single document 
   * 
   * @param paraText          paragraph text
   * @param paRes             proof reading result
   * @param footnotePositions position of footnotes
   * @param isParallelThread  true: check runs as parallel thread
   * @param nPara             number of flat paragraph (if known; only for LT internal functions)
   * @return                  proof reading result
   */
  ProofreadingResult getCheckResults(String paraText, Locale locale, ProofreadingResult paRes, 
      PropertyValue[] propertyValues, boolean docReset, SwJLanguageTool langTool) {
    return getCheckResults(paraText, locale, paRes, propertyValues, docReset, langTool, -1);
  }
    
  ProofreadingResult getCheckResults(String paraText, Locale locale, ProofreadingResult paRes, 
      PropertyValue[] propertyValues, boolean docReset, SwJLanguageTool langTool, int nPara) {
    
    getPropertyValues(propertyValues);

    if (resetDocCache && nPara >= 0) {
      docCache = null;
      resetDocCache = false;
    }
    if (docLanguage == null) {
      docLanguage = langTool.getLanguage();
    }
    if (ltMenus == null) {
      ltMenus = new LanguageToolMenus(xContext, this, config);
    }
    
    int paraNum = -1;
    boolean isIntern = false;
    try {
      if (docReset) {
        numLastVCPara = 0;
        ignoredMatches = new IgnoredMatches();
      }
      paraNum = getParaPos(nPara, paraText, locale, paRes.nStartOfSentencePosition);
      if (docCache == null || paraNum >= docCache.size()) {
        paraNum = -1;
      }
      this.paraNum = paraNum;
      if (nPara >= 0) {
        isDialogRequest.add(paraNum);
        isIntern = true;
      }
      // Don't use Cache for check in single paragraph mode
      if (numParasToCheck != 0 && paraNum >= 0) {
        //  test real flat paragraph rather then the one given by Proofreader - it could be changed meanwhile
        paraText = docCache.getFlatParagraph(paraNum);
      }
      List<SingleProofreadingError[]> pErrors = checkTextRules(paraText, footnotePositions, paraNum, paRes.nStartOfSentencePosition,
          langTool, isIntern);
      
      paRes.nStartOfSentencePosition = paragraphsCache.get(0).getStartSentencePosition(paraNum, paRes.nStartOfSentencePosition);
      paRes.nStartOfNextSentencePosition = paragraphsCache.get(0).getNextSentencePosition(paraNum, paRes.nStartOfSentencePosition);
      paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
      
      paRes.aErrors = mergeErrors(pErrors, nPara >= 0 ? nPara : paraNum);
      if (debugMode > 1) {
        MessageHandler.printToLogFile("paRes.aErrors.length: " + paRes.aErrors.length + "; docID: " + docID + OfficeTools.LOG_LINE_BREAK);
      }
      if (resetCheck.contains(paraNum) && paRes.nStartOfNextSentencePosition >= paraText.length()) {
        if (numParasToCheck != 0 && paraNum >= 0) {
          if (docCursor == null) {
            docCursor = new DocumentCursorTools(xComponent);
          }
          if (useQueue && !isDialogRequest.contains(paraNum) && paragraphsCache.get(1).getCacheEntry(paraNum) != null) {
            List<Integer> changedParas = new ArrayList<Integer>();
            changedParas.add(paraNum);
            remarkChangedParagraphs(changedParas, docCursor.getParagraphCursor(), flatPara);
          } else if (!useQueue || isDialogRequest.contains(paraNum)) {
            remarkChangedParagraphs(changedParas, docCursor.getParagraphCursor(), flatPara);
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    textIsChanged.remove(paraNum);
    resetCheck.remove(paraNum);
    isDialogRequest.remove(paraNum);
    return paRes;
  }
  
  /**
   * Get property values for footnotes (since LO 4.3)
   * and information for reason of proof (since LO 6.5)
   */
  private void getPropertyValues(PropertyValue[] propertyValues) {
    footnotePositions = null;  // e.g. for LO/OO < 4.3 and the 'FootnotePositions' property
    proofInfo = OfficeTools.PROOFINFO_UNKNOWN;  //  OO and LO < 6.5 do not support ProofInfo
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
    if (footnotePositions == null) {
      //  OO and LO < 4.3 do not support 'FootnotePositions' property and other advanced features
      //  switch back to single paragraph check mode
      if (numParasToCheck != 0 || useQueue) {
        numParasToCheck = 0;
        if (useQueue) {
          mDocHandler.getTextLevelCheckQueue().setStop();
          useQueue = false;
        }
        MessageHandler.printToLogFile("Single paragraph check mode set!");
      }
    }
  }
  
  /**
   * set values set by configuration dialog
   */
  void setConfigValues(Configuration config) {
    this.config = config;
    numParasToCheck = mDocHandler.isTestMode() ? 0 : config.getNumParasToCheck();
    defaultParaCheck = PARA_CHECK_DEFAULT;
    if (numParasToCheck == 0) {
      useQueue = false;
    } else {
      useQueue = mDocHandler.isTestMode() ? false : config.useTextLevelQueue();
    }
    changedParas = new ArrayList<Integer>();
    if (ltMenus != null) {
      ltMenus.setConfigValues(config);
    }
    isFixedLanguage = !config.getUseDocLanguage();
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
    minToCheckPara = mDocHandler.getNumMinToCheckParas();
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
  
  /** reset document cache of the document
   */
  void resetDocumentCache() {
    resetDocCache = true;
  }
  
  /** Update document cache and get it
   */
  DocumentCache getUpdatedDocumentCache() {
    if (docCursor == null) {
      docCursor = new DocumentCursorTools(xComponent);
    }
    setFlatParagraphTools(xComponent);
    DocumentCache newCache = new DocumentCache(docCursor, flatPara, defaultParaCheck, 
        isFixedLanguage ? LinguisticServices.getLocale(docLanguage) : null);
    if (!newCache.isEmpty()) {
      docCache = newCache;
    }
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
      }
      cacheIO.resetAllCache();
    }
  }
  
  /**
   * write caches to file
   */
  void writeCaches() {
    if (numParasToCheck != 0) {
      cacheIO.saveCaches(xComponent, docCache, paragraphsCache, config, mDocHandler);
    }
  }
  
  /** 
   * Reset all caches of the document
   */
  void resetCache() {
    for (int i = 0; i < OfficeTools.NUMBER_TEXTLEVEL_CACHE; i++) {
      paragraphsCache.get(i).removeAll();
    }
    numParasToChange = numParasToCheck;
    if ((numParasToCheck < 0 || useQueue) && mDocHandler != null) {
      minToCheckPara = mDocHandler.getNumMinToCheckParas();
      if (minToCheckPara == null) {
        return;
      }
      if (numParasToChange < 0) {
        for (int minPara : minToCheckPara) {
          if (minPara > numParasToChange) {
            numParasToChange = minPara;
          }
        }
      }
    }
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
   * remark changed paragraphs
   * override existing marks
   */
  private void remarkChangedParagraphs(List<Integer> changedParas, XParagraphCursor cursor, FlatParagraphTools flatPara) {
    Map <Integer, SingleProofreadingError[]> changedParasMap = new HashMap<>();
    for (int nPara : changedParas) {
      List<SingleProofreadingError[]> pErrors = new ArrayList<SingleProofreadingError[]>();
      for (int i = 0; i < minToCheckPara.size(); i++) {
        pErrors.add(paragraphsCache.get(i).getMatches(nPara));
      }
      changedParasMap.put(nPara, mergeErrors(pErrors, nPara));
    }
    flatPara.markParagraphs(changedParasMap, docCache, true, cursor);
  }

  /**
   * Search for Position of Paragraph
   * gives Back the Position of flat paragraph / -1 if Paragraph can not be found
   */
  private int getParaPos(int nPara, String chPara, Locale locale, int startPos) {

    if (mDocHandler.isTestMode() && nPara >= 0 && docCache != null) {
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
          isFixedLanguage ? LinguisticServices.getLocale(docLanguage) : null);
      if (debugMode > 0) {
        MessageHandler.printToLogFile("+++ resetAllParas (docCache == null): docCache.size: " + docCache.size()
                + ", docID: " + docID + OfficeTools.LOG_LINE_BREAK);
      }
      if (docCache.isEmpty()) {
        docCache = null;
        return -1;
      }
    }

    if (nPara >= 0) {
      return setPossibleChanges(chPara, locale, nPara);
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
          if (textIsChanged.add(nPara)) {
            resetCheck.remove(nPara);
            textIsChanged.remove(nPara);
            resetCheck.add(n);
            textIsChanged.add(n);
          }
          return n;
        }
      }
    }

    // find position from changed paragraph
    return getPosFromChangedPara(chPara, locale, nPara);
  }

  /**
   * Actualize document cache and result cache for given paragraph number
   */
  private int setPossibleChanges (String chPara, Locale locale, int nPara) {
    int nOldParas = docCache.size();
    changesInNumberOfParagraph(false);
    int numParas = docCache.size();
    if (numParas <= 0) {
      if (debugMode > 1) {
        MessageHandler.printToLogFile("Internal request: docCache error!");
      }
      return -1;
    }
    resetCheck.add(nPara);
    textIsChanged.add(nPara);
    if (nOldParas != numParas) {
      if (debugMode > 1) {
        MessageHandler.printToLogFile("Internal request: Number of Paragraphs has changed: o:" +
            nOldParas + ", n:" + numParas);
      }
      return nPara;
    }
    if (!chPara.equals(docCache.getFlatParagraph(nPara))) {
      if (debugMode > 1) {
        MessageHandler.printToLogFile("Internal request: Paragraph has changed:\no:" 
            + chPara + "\nn:" + docCache.getTextParagraph(nPara));
      }
      docCache.setFlatParagraph(nPara, chPara, locale);
      removeResultCache(nPara);
      ignoredMatches.removeIgnoredMatches(nPara);
    }
    return nPara;
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
        isFixedLanguage ? LinguisticServices.getLocale(docLanguage) : null);
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
    if (!ignoredMatches.isEmpty()) {
      IgnoredMatches tmpIgnoredMatches = new IgnoredMatches();
      for (int i = 0; i < from; i++) {
        if (ignoredMatches.containsKey(i)) {
          tmpIgnoredMatches.put(i, ignoredMatches.get(i));
        }
      }
      for (int i = to + 1; i < oldDocCache.size(); i++) {
        int n = i + docCache.size() - oldDocCache.size();
        if (ignoredMatches.containsKey(i)) {
          tmpIgnoredMatches.put(n, ignoredMatches.get(i));
        }
      }
      ignoredMatches = tmpIgnoredMatches;
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("!!!Changed paragraphs: from:" + from + ", to: " + to);
    }
    for (ResultCache cache : paragraphsCache) {
      cache.removeAndShift(from, to, docCache.size() - oldDocCache.size());
    }
    this.docCache = docCache;
    if (useQueue) {
      if (debugMode > 0) {
        MessageHandler.printToLogFile("Number of Paragraphs has changed: new: " + docCache.size() 
        +",  old: " + oldDocCache.size()+ ", docID: " + docID);
      }
      for (int i = 0; i < minToCheckPara.size(); i++) {
        if (minToCheckPara.get(i) != 0) {
          for (int n = from; n <= to; n++) {
            addQueueEntry(n, i, minToCheckPara.get(i), docID, false);
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
      resetCheck.add(nPara);
      textIsChanged.add(nPara);
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
      resetCheck.add(nPara);
      if (useQueue) {
        for (int i = 0; i < minToCheckPara.size(); i++) {
          if (minToCheckPara.get(i) == 0) {
            paragraphsCache.get(i).remove(nPara);
          } else {
            addQueueEntry(nPara, i, minToCheckPara.get(i), docID, numLastFlPara < 0 ? false : true);
          }
        }
      } else {
        for (ResultCache cache : paragraphsCache) {
          cache.remove(nPara);
        }
      }
      if (!textIsChanged.contains(nPara)) {
        changeFrom = nPara - numParasToChange;
        changeTo = nPara + numParasToChange + 1;
        ignoredMatches.removeIgnoredMatches(nPara);
        textIsChanged.add(nPara);
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

  /**
   * Merge errors from different checks (paragraphs and sentences)
   */
  private SingleProofreadingError[] mergeErrors(List<SingleProofreadingError[]> pErrors, int nPara) {
    int errorCount = 0;
    if (pErrors != null) {
      for (SingleProofreadingError[] pError : pErrors) {
        if (pError != null) {
          errorCount += pError.length;
        }
      }
    }
    if (errorCount == 0 || pErrors == null) {
      return new SingleProofreadingError[0];
    }
    SingleProofreadingError[] errorArray = new SingleProofreadingError[errorCount];
    errorCount = 0;
    for (SingleProofreadingError[] pError : pErrors) {
      if (pError != null) {
        arraycopy(pError, 0, errorArray, errorCount, pError.length);
        errorCount += pError.length;
      }
    }
    Arrays.sort(errorArray, new ErrorPositionComparator());
    return filterIgnoredMatches(errorArray, nPara);
  }
  
  /**
   * Filter ignored errors (from ignore once)
   */
  private SingleProofreadingError[] filterIgnoredMatches (SingleProofreadingError[] unFilteredErrors, int nPara) {
    if (!ignoredMatches.isEmpty() && ignoredMatches.containsKey(nPara)) {
      List<SingleProofreadingError> filteredErrors = new ArrayList<>();
      for (SingleProofreadingError error : unFilteredErrors) {
        if (!ignoredMatches.isIgnored(error.nErrorStart, error.nErrorStart + error.nErrorLength, nPara, error.aRuleIdentifier)) {
          filteredErrors.add(error);
        }
      }
      return filteredErrors.toArray(new SingleProofreadingError[0]);
    }
    return unFilteredErrors;
  }

  /**
   * check text rules 
   * different caches are supported for check of different number of paragraphs at once 
   * (for different kinds of text level rules)
   */
  private List<SingleProofreadingError[]> checkTextRules( String paraText, int[] footnotePos, int paraNum, 
      int startSentencePos, SwJLanguageTool langTool, boolean isIntern) {
    List<SingleProofreadingError[]> pErrors = new ArrayList<>();

    int nTParas = paraNum < 0 ? -1 : docCache.getNumberOfTextParagraph(paraNum);
    if (nTParas < 0) {
      pErrors.add(checkParaRules(paraText, footnotePos, paraNum, startSentencePos, langTool, 0, 0, isIntern));
    } else {
      //  Real full text check / numParas < 0
      ResultCache oldCache = null;
      List<Integer> tmpChangedParas;
      if (resetCheck.contains(paraNum)) {
        changedParas = new ArrayList<>();
      }
      for (int i = 0; i < minToCheckPara.size(); i++) {
        int parasToCheck = minToCheckPara.get(i);
        defaultParaCheck = PARA_CHECK_DEFAULT;
        if (i == 0 || mDocHandler.isSortedRuleForIndex(i)) {
          mDocHandler.activateTextRulesByIndex(i, langTool);
          if (debugMode > 1) {
            MessageHandler.printToLogFile("ParaCeck: Index: " + i + "/" + minToCheckPara.size() 
              + "; numParasToCheck: " + numParasToCheck + OfficeTools.LOG_LINE_BREAK);
          }
          if (resetCheck.contains(paraNum) && !useQueue && parasToCheck < 0 ) {
            oldCache = paragraphsCache.get(i);
            if (parasToCheck < -1) {
              paragraphsCache.set(i, new ResultCache());
            } else {
              paragraphsCache.set(i, new ResultCache(oldCache));
            }
          }
          pErrors.add(checkParaRules(paraText, footnotePos, paraNum, startSentencePos, langTool, i, parasToCheck, isIntern));
          if (resetCheck.contains(paraNum) && !useQueue) {
            if (parasToCheck < 0) {
              tmpChangedParas = paragraphsCache.get(i).differenceInCaches(oldCache);
              if (changedParas == null) {
                changedParas = new ArrayList<>();
              }
              for (int chPara : tmpChangedParas) {
                if (!changedParas.contains(chPara)) {
                  changedParas.add(chPara);
                }
              }
              if (!changedParas.contains(paraNum)) {
                changedParas.add(paraNum);
              }
              oldCache = null;
            } else {
              addChangedParas();
            }
          } 
        } else {
          pErrors.add(new SingleProofreadingError[0]);
        }
      }
      mDocHandler.reactivateTextRules(langTool);
    }
    return pErrors;
  }
  
  /**
   * add the numbers of changed paragraphs to list
   */
  private void addChangedParas() {
    int firstPara = changeFrom;
    if (firstPara < 0) {
      firstPara = 0;
    }
    int lastPara = changeTo;
    if (lastPara > docCache.size()) {
      lastPara = docCache.size();
    }
    if (changedParas == null) {
      changedParas = new ArrayList<>();
    }
    for (int n = firstPara; n < lastPara; n++) {
      if (!changedParas.contains(n)) {
        changedParas.add(n);
      }
    }
  }

  /**
   * Add an new entry to text level queue
   * nFPara is number of flat paragraph
   */
  public void addQueueEntry(int nFPara, int nCache, int nCheck, String docId, boolean overrideRunning) {
    if (mDocHandler.isSortedRuleForIndex(nCache)) {
      int nTPara = docCache.getNumberOfTextParagraph(nFPara);
      if (nTPara >= 0) {
        int nStart = docCache.getStartOfParaCheck(nTPara, nCheck, overrideRunning, true, false);
        int nEnd = docCache.getEndOfParaCheck(nTPara, nCheck, overrideRunning, true, false);
        mDocHandler.getTextLevelCheckQueue().addQueueEntry(nStart, nEnd, nCache, nCheck, docId, overrideRunning);
      }
    }
  }
  
  /**
   * create a queue entry 
   * used by getNextQueueEntry
   */
  private QueueEntry createQueueEntry(int nPara, int nCache) {
    int nCheck = minToCheckPara.get(nCache);
    int nStart = docCache.getStartOfParaCheck(nPara, nCheck, false, true, false);
    int nEnd = docCache.getEndOfParaCheck(nPara, nCheck, false, true, false);
    return mDocHandler.getTextLevelCheckQueue().createQueueEntry(nStart, nEnd, nCache, nCheck, docID, false);
  }

  /**
   * get the next queue entry which is the next empty cache entry
   */
  public QueueEntry getNextQueueEntry(int nPara) {
    if (docCache != null) {
      if (nPara >= 0) {
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
      for (int i = 0; i < nPara; i++) {
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
  public void runQueueEntry(int nStart, int nEnd, int cacheNum, int nCheck, boolean doReset, SwJLanguageTool langTool) {
    if (docCache.isFinished()) {
      addParaErrorsToCache(docCache.getFlatParagraphNumber(nStart), langTool, cacheNum, nCheck, doReset, false);
    }
  }

  /**
   * check the text level rules associated with a given cache (cacheNum)
   */
  @Nullable
  private SingleProofreadingError[] checkParaRules( String paraText, int[] footnotePos, int nFPara, int sentencePos, 
          SwJLanguageTool langTool, int cacheNum, int parasToCheck, boolean isIntern) {

    List<RuleMatch> paragraphMatches;
    SingleProofreadingError[] pErrors = null;
    int startSentencePos = 0;
    int endSentencePos = 0;
    try {
      // use Cache for check in single paragraph mode only after the first call of paragraph
      if (nFPara >= 0 || (sentencePos > 0 && lastSinglePara != null && lastSinglePara.equals(paraText))) {
        if (paragraphsCache.get(0).getCacheEntry(nFPara) != null) {
          startSentencePos = paragraphsCache.get(0).getStartSentencePosition(nFPara, sentencePos);
          endSentencePos = paragraphsCache.get(0).getNextSentencePosition(nFPara, sentencePos);
          pErrors = paragraphsCache.get(cacheNum).getFromPara(nFPara, startSentencePos, endSentencePos);
          if (debugMode > 1 && pErrors != null) {
            MessageHandler.printToLogFile("Check Para Rules: pErrors from cache: " + pErrors.length);
          }
        }
      } else if (sentencePos == 0) {
        lastSinglePara = paraText;
      }
      // return Cache result if available / for right mouse click or Dialog only use cache
      int nPara = nFPara < 0 || docCache == null ? -1 : docCache.getNumberOfTextParagraph(nFPara);
      if (nFPara >= 0 && (pErrors != null || (useQueue && !isDialogRequest.contains(nFPara) && parasToCheck != 0))) {
        if (useQueue && pErrors == null && parasToCheck != 0 && nPara >= 0 && !textIsChanged.contains(nFPara)) {
          addQueueEntry(nFPara, cacheNum, parasToCheck, docID, false);
        }
        return pErrors;
      }
      
      //  One paragraph check (set by options or proof of footnote, etc.)
      if (nPara < 0 || parasToCheck == 0) {
        List<Integer> nextSentencePositions;
        if (langTool.isRemote()) {
          nextSentencePositions = new ArrayList<Integer>();
          nextSentencePositions.add(paraText.length());
        } else {
          nextSentencePositions = getNextSentencePositions(paraText, langTool);
        }
        paragraphMatches = langTool.check(removeFootnotes(paraText, footnotePos), true, JLanguageTool.ParagraphHandling.NORMAL);
        if (paragraphMatches == null || paragraphMatches.isEmpty()) {
          paragraphsCache.get(cacheNum).put(nFPara, nextSentencePositions, new SingleProofreadingError[0]);
          if (debugMode > 1) {
            MessageHandler.printToLogFile("--> checkParaRules: Enter to para cache(" + cacheNum + "): Paragraph(" + nFPara + "): " + paraText 
              + "; Error number: " + 0 + OfficeTools.LOG_LINE_BREAK);
          }
        } else {
          List<SingleProofreadingError> errorList = new ArrayList<>();
          for (RuleMatch myRuleMatch : paragraphMatches) {
            int toPos = myRuleMatch.getToPos();
            if (toPos > paraText.length()) {
              toPos = paraText.length();
            }
            errorList.add(correctRuleMatchWithFootnotes(
                createOOoError(myRuleMatch, 0, toPos, isIntern ? ' ' : paraText.charAt(toPos-1)), 0, footnotePos));
          }
          if (!errorList.isEmpty()) {
            if (debugMode > 1) {
              MessageHandler.printToLogFile("--> checkParaRules: Enter to para cache(" + cacheNum + "): Paragraph: " + paraText 
                + "; Error number: " + errorList.size() + OfficeTools.LOG_LINE_BREAK);
            }
            paragraphsCache.get(cacheNum).put(nFPara, nextSentencePositions, errorList.toArray(new SingleProofreadingError[0]));
          } else {
            if (debugMode > 1) {
              MessageHandler.printToLogFile("--> checkParaRules: Enter to para cache(" + cacheNum + "): Paragraph(" + nFPara + "): " + paraText 
                + "; Error number: " + 0 + OfficeTools.LOG_LINE_BREAK);
            }
            paragraphsCache.get(cacheNum).put(nFPara, nextSentencePositions, new SingleProofreadingError[0]);
          }
        }
        startSentencePos = paragraphsCache.get(cacheNum).getStartSentencePosition(nFPara, sentencePos);
        endSentencePos = paragraphsCache.get(cacheNum).getNextSentencePosition(nFPara, sentencePos);
        return paragraphsCache.get(cacheNum).getFromPara(nFPara, startSentencePos, endSentencePos);
      }

      //  check of numParasToCheck or full text 
      addParaErrorsToCache(nFPara, langTool, cacheNum, parasToCheck, textIsChanged.contains(nFPara), isIntern);
      return paragraphsCache.get(cacheNum).getFromPara(nFPara, startSentencePos, endSentencePos);

    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return null;
  }

  /**
   *   check for number of Paragraphs > 0, chapter wide or full text
   *   is also called by text level queue
   */
  private void addParaErrorsToCache(int nFPara, SwJLanguageTool langTool, int cacheNum, int parasToCheck, 
          boolean override, boolean isIntern) {
    //  make the method thread save
    MultiDocumentsHandler mDH = mDocHandler;
    DocumentCursorTools docCursor = this.docCursor;
    DocumentCache docCache = this.docCache;
    if (docCache == null || nFPara < 0 || nFPara >= docCache.size()) {
      return;
    }
    List<ResultCache> paragraphsCache = this.paragraphsCache;
//    boolean textIsChanged = this.textIsChanged.contains(nFPara);
    boolean textIsChanged = override;
    try {

      ResultCache oldCache = null;
      if (useQueue && !isDialogRequest.contains(nFPara)) {
        oldCache = paragraphsCache.get(cacheNum);
        if (parasToCheck < -1) {
          paragraphsCache.set(cacheNum, new ResultCache());
        } else {
          paragraphsCache.set(cacheNum, new ResultCache(oldCache));
        }
      }
      
      int nTPara = docCache.getNumberOfTextParagraph(nFPara);
      String textToCheck = docCache.getDocAsString(nTPara, parasToCheck, textIsChanged, useQueue);
      List<RuleMatch> paragraphMatches = null;
      if (mDocHandler.isSortedRuleForIndex(cacheNum)) {
        paragraphMatches = langTool.check(textToCheck, true, JLanguageTool.ParagraphHandling.ONLYPARA);
      }
      
      int startPara = docCache.getStartOfParaCheck(nTPara, parasToCheck, textIsChanged, useQueue, false);
      int endPara = docCache.getEndOfParaCheck(nTPara, parasToCheck, textIsChanged, useQueue, false);
      int startPos = docCache.getStartOfParagraph(startPara, nTPara, parasToCheck, textIsChanged, useQueue);
      int endPos;
      int footnotesBefore = 0;
      for (int i = startPara; i < endPara; i++) {
        if (useQueue && !isDialogRequest.contains(nFPara) && mDH.getTextLevelCheckQueue().isInterrupted()) {
          return;
        }
        int[] footnotePos = docCache.getTextParagraphFootnotes(i);
        if (i < endPara - 1) {
          endPos = docCache.getStartOfParagraph(i + 1, nTPara, parasToCheck, textIsChanged, useQueue);
        } else {
          endPos = textToCheck.length();
        }
        if (paragraphMatches == null || paragraphMatches.isEmpty()) {
          paragraphsCache.get(cacheNum).put(docCache.getFlatParagraphNumber(i), new SingleProofreadingError[0]);
          if (debugMode > 1) {
            MessageHandler.printToLogFile("--> addParaErrorsToCache: Enter to para cache(" + cacheNum + "): Paragraph(" 
                + docCache.getFlatParagraphNumber(i) + "): " + docCache.getTextParagraph(i) + "; Error number: 0" + OfficeTools.LOG_LINE_BREAK);
          }
        } else {
          List<SingleProofreadingError> errorList = new ArrayList<>();
          int textPos = startPos;
          if (textPos < 0) textPos = 0;
          for (RuleMatch myRuleMatch : paragraphMatches) {
            int startErrPos = myRuleMatch.getFromPos();
            if (startErrPos >= startPos && startErrPos < endPos) {
              int toPos = docCache.getTextParagraph(i).length();
              if (toPos > 0) {
                errorList.add(this.correctRuleMatchWithFootnotes(
                    createOOoError(myRuleMatch, -textPos, toPos, isIntern ? ' ' : docCache.getTextParagraph(i).charAt(toPos-1)),
                      footnotesBefore, footnotePos));
              }
            }
          }
          if (!errorList.isEmpty()) {
            paragraphsCache.get(cacheNum).put(docCache.getFlatParagraphNumber(i), errorList.toArray(new SingleProofreadingError[0]));
            if (debugMode > 1) {
              MessageHandler.printToLogFile("--> addParaErrorsToCache: Enter to para cache(" + cacheNum + "): Paragraph(" 
                  + docCache.getFlatParagraphNumber(i) + "): " + docCache.getTextParagraph(i) 
                  + "; Error number: " + errorList.size() + OfficeTools.LOG_LINE_BREAK);
            }
          } else {
            paragraphsCache.get(cacheNum).put(docCache.getFlatParagraphNumber(i), new SingleProofreadingError[0]);
            if (debugMode > 1) {
              MessageHandler.printToLogFile("--> addParaErrorsToCache: Enter to para cache(" + cacheNum + "): Paragraph(" 
                  + docCache.getFlatParagraphNumber(i) + "): " + docCache.getTextParagraph(i) + "; Error number: 0" + OfficeTools.LOG_LINE_BREAK);
            }
          }
        }
        startPos = endPos;
        footnotesBefore += footnotePos.length;
      }
      if (useQueue && !isDialogRequest.contains(nFPara)) {
        if (mDH.getTextLevelCheckQueue().isInterrupted()) {
          return;
        }
        if (docCursor == null) {
          docCursor = new DocumentCursorTools(xComponent);
        }
        setFlatParagraphTools(xComponent);

        if (override) {
          List<Integer> tmpChangedParas;
          tmpChangedParas = paragraphsCache.get(cacheNum).differenceInCaches(oldCache);
          List<Integer> changedParas = new ArrayList<>();
          for (int n : tmpChangedParas) {
            if (paragraphsCache.get(0).getCacheEntry(n) != null) {
              changedParas.add(n);
            }
          }
          if (debugMode > 1) {
            MessageHandler.printToLogFile("Mark paragraphs (override) numChanged: " + changedParas.size());
          }
          if (!changedParas.isEmpty()) {
            remarkChangedParagraphs(changedParas, docCursor.getParagraphCursor(), flatPara);
          }
        } else {
          if (debugMode > 1) {
            MessageHandler.printToLogFile("Mark paragraphs from " + startPara + " to " + endPara);
          }
          List<Integer> changedParas = new ArrayList<>();
          for (int nText = startPara; nText < endPara; nText++) {
            int nFlat = docCache.getFlatParagraphNumber(nText);
            SingleProofreadingError[] errors = paragraphsCache.get(cacheNum).getMatches(nFlat);
            if (errors != null && errors.length != 0) {
              SingleProofreadingError[] filteredErrors = filterIgnoredMatches(errors, nFlat);
              if (paragraphsCache.get(0).getCacheEntry(nFlat) != null && filteredErrors != null && filteredErrors.length != 0) {
                changedParas.add(nFlat);
              }
            }
          }
          if (!changedParas.isEmpty()) {
            remarkChangedParagraphs(changedParas, docCursor.getParagraphCursor(), flatPara);
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }
  
  /**
   * Creates a SingleGrammarError object for use in LO/OO.
   */
  private SingleProofreadingError createOOoError(RuleMatch ruleMatch, int startIndex, int sentencesLength, char lastChar) {
    SingleProofreadingError aError = new SingleProofreadingError();
    aError.nErrorType = TextMarkupType.PROOFREADING;
    // the API currently has no support for formatting text in comments
    String msg = ruleMatch.getMessage()
        .replaceAll("<suggestion>", docLanguage == null ? "\"" : docLanguage.getOpeningDoubleQuote())
        .replaceAll("</suggestion>", docLanguage == null ? "\"" : docLanguage.getClosingDoubleQuote())
        .replaceAll("([\r]*\n)", " ");
    if (docLanguage != null) {
      msg = docLanguage.toAdvancedTypography(msg);
    }
    aError.aFullComment = msg;
    // not all rules have short comments
    if (!StringTools.isEmpty(ruleMatch.getShortMessage())) {
      aError.aShortComment = ruleMatch.getShortMessage();
    } else {
      aError.aShortComment = aError.aFullComment;
    }
    aError.aShortComment = org.languagetool.gui.Tools.shortenComment(aError.aShortComment);
    int numSuggestions;
    String[] allSuggestions;
    numSuggestions = ruleMatch.getSuggestedReplacements().size();
    allSuggestions = ruleMatch.getSuggestedReplacements().toArray(new String[numSuggestions]);
    //  Filter: remove suggestions for override dot at the end of sentences
    //  needed because of error in dialog
    /*  since LT 5.2: Filter is commented out because of default use of LT dialog
    if (lastChar == '.' && (ruleMatch.getToPos() + startIndex) == sentencesLength) {
      int i = 0;
      while (i < numSuggestions && i < OfficeTools.MAX_SUGGESTIONS
          && allSuggestions[i].length() > 0 && allSuggestions[i].charAt(allSuggestions[i].length()-1) == '.') {
        i++;
      }
      if (i < numSuggestions && i < OfficeTools.MAX_SUGGESTIONS) {
      numSuggestions = 0;
      allSuggestions = new String[0];
      }
    }
    */
    //  End of Filter
    if (numSuggestions > OfficeTools.MAX_SUGGESTIONS) {
      aError.aSuggestions = Arrays.copyOfRange(allSuggestions, 0, OfficeTools.MAX_SUGGESTIONS);
    } else {
      aError.aSuggestions = allSuggestions;
    }
    aError.nErrorStart = ruleMatch.getFromPos() + startIndex;
    aError.nErrorLength = ruleMatch.getToPos() - ruleMatch.getFromPos();
    aError.aRuleIdentifier = ruleMatch.getRule().getId();
    // LibreOffice since version 3.5 supports an URL that provides more information about the error,
    // LibreOffice since version 6.2 supports the change of underline color (key: "LineColor", value: int (RGB))
    // LibreOffice since version 6.2 supports the change of underline style (key: "LineType", value: short (DASHED = 5))
    // older version will simply ignore the properties
    Color underlineColor = config.getUnderlineColor(ruleMatch.getRule().getCategory().getName());
    short underlineType = config.getUnderlineType(ruleMatch.getRule().getCategory().getName());
    URL url = ruleMatch.getUrl();
    if (url == null) {                      // match URL overrides rule URL 
      url = ruleMatch.getRule().getUrl();
    }
    int nDim = 0;
    if (url != null) {
      nDim++;
    }
    if (underlineColor != Color.blue) {
      nDim++;
    }
    if (underlineType != Configuration.UNDERLINE_WAVE || (config.markSingleCharBold() && aError.nErrorLength == 1)) {
      nDim++;
    }
    if (nDim > 0) {
      //  HINT: Because of result cache handling:
      //  handle should always be -1
      //  property state should always be PropertyState.DIRECT_VALUE
      //  otherwise result cache handling has to be adapted
      PropertyValue[] propertyValues = new PropertyValue[nDim];
      int n = 0;
      if (url != null) {
        propertyValues[n] = new PropertyValue("FullCommentURL", -1, url.toString(), PropertyState.DIRECT_VALUE);
        n++;
      }
      if (underlineColor != Color.blue) {
        int ucolor = underlineColor.getRGB() & 0xFFFFFF;
        propertyValues[n] = new PropertyValue("LineColor", -1, ucolor, PropertyState.DIRECT_VALUE);
        n++;
      }
      if (underlineType != Configuration.UNDERLINE_WAVE) {
        propertyValues[n] = new PropertyValue("LineType", -1, underlineType, PropertyState.DIRECT_VALUE);
      } else if (config.markSingleCharBold() && aError.nErrorLength == 1) {
        propertyValues[n] = new PropertyValue("LineType", -1, Configuration.UNDERLINE_BOLDWAVE, PropertyState.DIRECT_VALUE);
      }
      aError.aProperties = propertyValues;
    } else {
        aError.aProperties = new PropertyValue[0];
    }
    return aError;
  }
  
  /**
   * get beginning of next sentence using LanguageTool tokenization
   */
  List<Integer> getNextSentencePositions (String paraText, SwJLanguageTool langTool) {
    List<Integer> nextSentencePositions = new ArrayList<Integer>();
    List<String> tokenizedSentences = langTool.sentenceTokenize(cleanFootnotes(paraText));
    int position = 0;
    for (String sentence : tokenizedSentences) {
      position += sentence.length();
      nextSentencePositions.add(position);
    }
    return nextSentencePositions;
  }
  
  /**
   * Fix numbers that are (probably) foot notes.
   * See https://bugs.freedesktop.org/show_bug.cgi?id=69416
   * public for test reasons
   */
  static String cleanFootnotes(String paraText) {
    return paraText.replaceAll("([^\\d][.!?])\\d ", "$1¹ ");
  }
  
  /**
   * Remove footnotes from paraText
   * run cleanFootnotes if information about footnotes are not supported
   */
  static String removeFootnotes(String paraText, int[] footnotes) {
    if (footnotes == null) {
      return cleanFootnotes(paraText);
    }
    for (int i = footnotes.length - 1; i >= 0; i--) {
      paraText = paraText.substring(0, footnotes[i]) + paraText.substring(footnotes[i] + 1);
    }
    return paraText;
  }
  
  /**
   * Correct SingleProofreadingError by footnote positions
   * footnotes before is the sum of all footnotes before the checked paragraph
   */
  SingleProofreadingError correctRuleMatchWithFootnotes(SingleProofreadingError pError, int footnotesBefore, int[] footnotes) {
    if (footnotesBefore == 0 && (footnotes == null || footnotes.length == 0)) {
      return pError;
    }
    for (int i :footnotes) {
      if (i <= pError.nErrorStart) {
        pError.nErrorStart++;
      } else if (i < pError.nErrorStart + pError.nErrorLength) {
        pError.nErrorLength++;
      }
    }
    pError.nErrorStart += footnotesBefore;
    return pError;
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
    if (numParasToCheck != 0) {
      List<Integer> changedParas = new ArrayList<>();
      changedParas.add(y);
      if (docCursor == null) {
        docCursor = new DocumentCursorTools(xComponent);
      }
      remarkChangedParagraphs(changedParas, docCursor.getParagraphCursor(), flatPara);
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("Ignore Match added at: paragraph: " + y + "; character: " + x);
    }
  }
  
  /**
   * remove all ignore once entries for paragraph y from queue and set the mark
   */
  public void removeIgnoredMatch(int y) {
    ignoredMatches.removeIgnoredMatches(y);
    if (numParasToCheck != 0) {
      List<Integer> changedParas = new ArrayList<>();
      changedParas.add(y);
      if (docCursor == null) {
        docCursor = new DocumentCursorTools(xComponent);
      }
      remarkChangedParagraphs(changedParas, docCursor.getParagraphCursor(), flatPara);
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
      if (docCursor == null) {
        docCursor = new DocumentCursorTools(xComponent);
      }
      remarkChangedParagraphs(changedParas, docCursor.getParagraphCursor(), flatPara);
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("Ignore Match removed at: paragraph: " + y + "; character: " + x);
    }
  }
  
  /**
   * get a rule ID of an error out of the cache 
   * by the position of the error (paragraph number and number of character)
   */
  private String getRuleIdFromCache(int nPara, int nChar) {
    List<SingleProofreadingError> tmpErrors = new ArrayList<SingleProofreadingError>();
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
    int y = viewCursor.getViewCursorParagraph();
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
     * Get all ignored matches of a paragraph
     */
    public Map<String, Set<Integer>>  get(int y) {
      return ignoredMatches.get(y);
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
