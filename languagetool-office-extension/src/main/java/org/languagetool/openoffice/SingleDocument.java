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
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.openoffice.TextLevelCheckQueue.QueueEntry;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.text.TextMarkupType;
import com.sun.star.text.XParagraphCursor;
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
  
  private DocumentCache docCache = null;          //  cache of paragraphs (only readable by parallel thread)
  private DocumentCursorTools docCursor = null;   //  Save document cursor for the single document
  private ViewCursorTools viewCursor = null;      //  Get the view cursor for desktop
  private FlatParagraphTools flatPara = null;     //  Save information for flat paragraphs (including iterator and iterator provider) for the single document
  private Integer numLastVCPara = 0;              //  Save position of ViewCursor for the single documents
  private Integer numLastFlPara = 0;              //  Save position of FlatParagraph for the single documents
  private ResultCache sentencesCache;             //  Cache for matches of sentences rules
  private List<ResultCache> paragraphsCache;      //  Cache for matches of text rules
  private ResultCache singleParaCache;            //  Cache for matches of text rules for single paragraphs
  private CacheIO cacheIO;
  private int resetFrom = 0;                      //  Reset from paragraph
  private int resetTo = 0;                        //  Reset to paragraph
  private int numParasReset = 1;                  //  Number of paragraphs to reset
  private List<Integer> changedParas = null;      //  List of changed paragraphs after editing the document
  private Set<Integer> textIsChanged;             //  false: check number of paragraphs again (ignored by parallel thread)
  private Set<Integer> resetCheck;                //  true: the whole text has to be checked again (use cache)
  private Set<Integer> isDialogRequest;           //  true: check was initiated by right mouse click or proofreading dialog
  private int paraNum;                            //  Number of current checked paragraph
  private List<Integer> minToCheckPara;           //  List of minimal to check paragraphs for different classes of text level rules
  private IgnoredMatches ignoredMatches;          //  Map of matches (number of paragraph, number of character) that should be ignored after ignoreOnce was called
  private boolean useQueue = true;                //  true: use queue to check text level rules (will be overridden by config)
  private boolean disposed = false;               //  true: document with this docId is disposed - SingleDocument shall be removed
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
    this.mDocHandler = mDH;
    this.sentencesCache = new ResultCache();
    this.singleParaCache = new ResultCache();
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
    if (config != null && config.saveLoCache() && xComponent != null) {
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
      SingleProofreadingError[] sErrors = null;
      paraNum = getParaPos(nPara, paraText, locale, paRes.nStartOfSentencePosition);
      this.paraNum = paraNum;
      if (nPara >= 0) {
        isDialogRequest.add(paraNum);
        isIntern = true;
      }
      // Don't use Cache for check in single paragraph mode
      if (numParasToCheck != 0 && paraNum >= 0) {
        //  test real flat paragraph rather then the one given by Proofreader - it could be changed meanwhile
        paraText = docCache.getFlatParagraph(paraNum);
        sErrors = sentencesCache.getMatches(paraNum, paRes.nStartOfSentencePosition);
        // return Cache result if available
        if (sErrors != null) {
          paRes.nStartOfNextSentencePosition = sentencesCache.getNextSentencePosition(paraNum, paRes.nStartOfSentencePosition);
          paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
        }
      }
      if (debugMode > 1) {
        MessageHandler.printToLogFile("... Check Sentence: numCurPara: " + paraNum 
            + "; startPos: " + paRes.nStartOfSentencePosition + "; Paragraph: " + paraText 
            + ", sErrors: " + (sErrors == null ? "null" : sErrors.length) + OfficeTools.LOG_LINE_BREAK);
        if (sErrors != null && sErrors.length > 0) {
          MessageHandler.printToLogFile(".-> sErrors[0]: nStart = " + sErrors[0].nErrorStart + ", nEnd = " + sErrors[0].nErrorStart
              + ", errorID = " + (sErrors[0].aRuleIdentifier == null ? "null" : sErrors[0].aRuleIdentifier));
        }
      }
      String text = null;
      if (sErrors == null) {
        if (!langTool.isRemote()) {
          SentenceFromPara sfp = new SentenceFromPara(paraText, paRes.nStartOfSentencePosition, langTool);
          text = sfp.getSentence();
          paRes.nStartOfSentencePosition = sfp.getPosition();
          paRes.nStartOfNextSentencePosition = sfp.getPosition() + text.length();
        } else {
          text = paraText;
          paRes.nStartOfSentencePosition = 0;
          paRes.nStartOfNextSentencePosition = text.length();
        }
        paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
      }

      List<SingleProofreadingError[]> pErrors = checkTextRules(paraText, paraNum, paRes.nStartOfSentencePosition,
          paRes.nStartOfNextSentencePosition, langTool, isIntern);

      if (sErrors == null) {
        sErrors = checkSentence(text, paRes.nStartOfSentencePosition, paRes.nStartOfNextSentencePosition, 
            paraNum, footnotePositions, langTool, isIntern);
      }
      
      paRes.aErrors = mergeErrors(sErrors, pErrors, nPara >= 0 ? nPara : paraNum);
      if (debugMode > 1) {
        MessageHandler.printToLogFile("paRes.aErrors.length: " + paRes.aErrors.length + "; docID: " + docID + OfficeTools.LOG_LINE_BREAK);
      }
      if (resetCheck.contains(paraNum) && paRes.nStartOfNextSentencePosition >= paraText.length()) {
        if (numParasToCheck != 0 && paraNum >= 0) {
          if (docCursor == null) {
            docCursor = new DocumentCursorTools(xComponent);
          }
          if (useQueue && !isDialogRequest.contains(paraNum) && paragraphsCache.get(1).getEntryByParagraph(paraNum) != null) {
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
    footnotePositions = null;
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
      footnotePositions = new int[]{};  // e.g. for LO/OO < 4.3 and the 'FootnotePositions' property
    }
  }
  
  /**
   * set values set by configuration dialog
   */
  void setConfigValues(Configuration config) {
    this.config = config;
    numParasToCheck = config.getNumParasToCheck();
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
    docCache = null;
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
        sentencesCache = cacheIO.getSentencesCache();
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
      cacheIO.saveCaches(xComponent, docCache, sentencesCache, paragraphsCache, config, mDocHandler);
    }
  }
  
  /** 
   * Reset all caches of the document
   */
  void resetCache() {
    sentencesCache.removeAll();
    singleParaCache.removeAll();
    for (int i = 0; i < OfficeTools.NUMBER_TEXTLEVEL_CACHE; i++) {
      paragraphsCache.get(i).removeAll();
    }
    numParasReset = numParasToCheck;
    if ((numParasToCheck < 0 || useQueue) && mDocHandler != null) {
      minToCheckPara = mDocHandler.getNumMinToCheckParas();
      if (minToCheckPara == null) {
        return;
      }
      if (numParasReset < 0) {
        for (int minPara : minToCheckPara) {
          if (minPara > numParasReset) {
            numParasReset = minPara;
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
      SingleProofreadingError[] sErrors = sentencesCache.getMatches(nPara);
      changedParasMap.put(nPara, mergeErrors(sErrors, pErrors, nPara));
    }
    flatPara.markParagraphs(changedParasMap, docCache, true, cursor);
  }

  /**
   * Fix numbers that are (probably) foot notes.
   * See https://bugs.freedesktop.org/show_bug.cgi?id=69416
   * public for test reasons
   */
  String cleanFootnotes(String paraText) {
    return paraText.replaceAll("([^\\d][.!?])\\d ", "$1¹ ");
  }
  
  /**
   * Search for Position of Paragraph
   * gives Back the Position of flat paragraph / -1 if Paragraph can not be found
   */
  private int getParaPos(int nPara, String chPara, Locale locale, int startPos) {

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
    int nPara = findNextParaPos(numLastFlPara, chPara, locale, startPos);
    if (nPara >= 0) {
      numLastFlPara = nPara;
      if (debugMode > 0) {
        MessageHandler.printToLogFile("From last FlatPragraph Position: Number of Paragraph: " + nPara + OfficeTools.LOG_LINE_BREAK);
      }
      return nPara;
    }
    
    // number of paragraphs has changed? --> Update the internal information
    nPara = changesInNumberOfParagraph(true);
    if (nPara < 0) {
      //  problem with automatic iteration - try to get ViewCursor position
      return getParaFromViewCursorOrDialog(chPara, locale);
    }

    String curFlatParaText = flatPara.getCurrentParaText();
    if (curFlatParaText != null && !curFlatParaText.equals(chPara) && curFlatParaText.equals(docCache.getFlatParagraph(nPara))) {
      //  wrong flat paragraph - try to get ViewCursor position
      return getParaFromViewCursorOrDialog(chPara, locale);
    }

    //  test real flat paragraph rather then the one given by Proofreader - it could be changed meanwhile
    if (curFlatParaText != null) {
      chPara = curFlatParaText;
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
    sentencesCache.remove(nPara);
    for (ResultCache cache : paragraphsCache) {
      cache.remove(nPara);
    }
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
    int nParas = viewCursor.getViewCursorParagraph();
    if (nParas >= 0 && nParas < docCache.textSize() && docCache.isEqual(docCache.getFlatParagraphNumber(nParas), chPara, locale)) {
      numLastVCPara = nParas;
      if (debugMode > 0) {
        MessageHandler.printToLogFile("From View Cursor: Number of Paragraph: " + nParas + OfficeTools.LOG_LINE_BREAK);
      }
      nParas = docCache.getFlatParagraphNumber(nParas);
//      isDialogRequest.add(nParas);
      return nParas;
    }
    // try to get next position from last ViewCursor position (proof per dialog box)
    if (numLastVCPara >= docCache.textSize()) {
      numLastVCPara = 0;
    }
    for (int i = numLastVCPara; i < docCache.textSize(); i++) {
      if (docCache.isEqual(docCache.getFlatParagraphNumber(i), chPara, locale)) {
        numLastVCPara = i;
        if (debugMode > 0) {
          MessageHandler.printToLogFile("From Dialog: Number of Paragraph: " + i + OfficeTools.LOG_LINE_BREAK);
        }
        nParas = docCache.getFlatParagraphNumber(numLastVCPara);
//        isDialogRequest.add(nParas);
        return nParas;
      }
    }
    for (int i = 0; i < numLastVCPara; i++) {
      if (docCache.isEqual(docCache.getFlatParagraphNumber(i), chPara, locale)) {
        numLastVCPara = i;
        if (debugMode > 0) {
          MessageHandler.printToLogFile("From Dialog: Number of Paragraph: " + i + OfficeTools.LOG_LINE_BREAK);
        }
        nParas = docCache.getFlatParagraphNumber(numLastVCPara);
//        isDialogRequest.add(nParas);
        return nParas;
      }
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
      docCache = null;
      return -1;
    }
    int from = 0;
    while (from < docCache.size() && from < oldDocCache.size()
        && docCache.getFlatParagraph(from).equals(oldDocCache.getFlatParagraph(from))) {
      from++;
    }
    resetFrom = from - numParasReset;
    int to = 1;
    while (to <= docCache.size() && to <= oldDocCache.size()
        && docCache.getFlatParagraph(docCache.size() - to).equals(
            oldDocCache.getFlatParagraph(oldDocCache.size() - to))) {
      to++;
    }
    to = docCache.size() - to;
    resetTo = to + numParasReset;
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
    for (ResultCache cache : paragraphsCache) {
      cache.removeAndShift(resetFrom, resetTo, docCache.size() - oldDocCache.size());
    }
    resetTo++;
    sentencesCache.removeAndShift(from, to, docCache.size() - oldDocCache.size());
    if (useQueue) {
      for (int i = 0; i < minToCheckPara.size(); i++) {
        if (minToCheckPara.get(i) != 0) {
          for (int n = from; n <= to; n++) {
            addQueueEntry(n, i, minToCheckPara.get(i), docID, true);
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
    
    numLastFlPara = nPara;  //  Note: This is the number of flat paragraph
    
    if (!docCache.isEqual(nPara, chPara, locale)) {
      if (debugMode > 0) {
        MessageHandler.printToLogFile("!!! flat praragraph changed: nPara: " + nPara
                + "; docID: " + docID
                + OfficeTools.LOG_LINE_BREAK + "old: " + docCache.getFlatParagraph(nPara) + OfficeTools.LOG_LINE_BREAK 
                + "new: " + chPara + OfficeTools.LOG_LINE_BREAK);
      }
      docCache.setFlatParagraph(nPara, chPara, locale);
      resetCheck.add(nPara);
      sentencesCache.remove(nPara);
      if (useQueue) {
        for (int i = 0; i < minToCheckPara.size(); i++) {
          if (minToCheckPara.get(i) == 0) {
            paragraphsCache.get(i).remove(nPara);
          } else {
            addQueueEntry(nPara, i, minToCheckPara.get(i), docID, true);
          }
        }
      } else {
        for (ResultCache cache : paragraphsCache) {
          cache.remove(nPara);
        }
      }
      if (!textIsChanged.contains(nPara)) {
        resetFrom = nPara - numParasReset;
        resetTo = nPara + numParasReset + 1;
        ignoredMatches.removeIgnoredMatches(nPara);
        textIsChanged.add(nPara);
      }
      return nPara;
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("From FlatParagraph: Number of Paragraph: " + nPara + OfficeTools.LOG_LINE_BREAK);
    }
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
    if (startPos > 0) {
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
   * Annotate text
   * Handling of footnotes etc.
   */
  AnnotatedText getAnnotatedText(String text, int[] footnotePos, int startPosition) {
    AnnotatedTextBuilder annotations = new AnnotatedTextBuilder();
    if (footnotePos.length == 0) {
      annotations.addText(text);
    } else {
      boolean hasFootnote = false;
      int lastPos = startPosition;
      for (int i = 0; i < footnotePos.length && footnotePos[i] - startPosition < text.length(); i++) {
        if (footnotePos[i] >= startPosition) {
          if (footnotePos[i] > lastPos) {
            annotations.addText(text.substring(lastPos - startPosition, footnotePos[i] - startPosition));
          }
          annotations.addMarkup(OfficeTools.ZERO_WIDTH_SPACE);
          lastPos = footnotePos[i] + 1;
          hasFootnote = true;
        }
      }
      if (hasFootnote && lastPos < text.length()) {
        annotations.addText(text.substring(lastPos - startPosition));
      } else if (!hasFootnote) {
        annotations.addText(text);
      }
    }
    return annotations.build();
  }

  /**
   * Merge errors from different checks (paragraphs and sentences)
   */
  private SingleProofreadingError[] mergeErrors(SingleProofreadingError[] sErrors, List<SingleProofreadingError[]> pErrors, int nPara) {
    int errorCount = 0;
    if (sErrors != null) {
      errorCount += sErrors.length;
    }
    if (pErrors != null) {
      for (SingleProofreadingError[] pError : pErrors) {
        if (pError != null) {
          errorCount += pError.length;
        }
      }
    }
    if (errorCount == 0) {
      return new SingleProofreadingError[0];
    }
    SingleProofreadingError[] errorArray = new SingleProofreadingError[errorCount];
    int sErrorCount = 0;
    if (sErrors != null) {
      sErrorCount = sErrors.length;
      arraycopy(sErrors, 0, errorArray, 0, sErrorCount);
    }
    if (pErrors != null) {
      for (SingleProofreadingError[] pError : pErrors) {
        if (pError != null) {
          arraycopy(pError, 0, errorArray, sErrorCount, pError.length);
          sErrorCount += pError.length;
        }
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
  private List<SingleProofreadingError[]> checkTextRules( String paraText, int paraNum, 
      int startSentencePos, int endSentencePos, SwJLanguageTool langTool, boolean isIntern) {
    List<SingleProofreadingError[]> pErrors = new ArrayList<>();

    int nTParas = paraNum < 0 ? -1 : docCache.getNumberOfTextParagraph(paraNum);
    if (nTParas < 0 || (numParasToCheck >= 0 && !useQueue)) {
      int parasToCheck = nTParas < 0 ? 0 : numParasToCheck;
      pErrors.add(checkParaRules(paraText, paraNum, startSentencePos, endSentencePos, langTool, 0, parasToCheck, isIntern));
      if (resetCheck.contains(paraNum)) {
        addChangedParas();
      }
    } else {
      //  Real full text check / numParas < 0
      ResultCache oldCache = null;
      List<Integer> tmpChangedParas;
      if (resetCheck.contains(paraNum)) {
        changedParas = new ArrayList<>();
      }
      for (int i = 0; i < minToCheckPara.size(); i++) {
        int parasToCheck = minToCheckPara.get(i);
        if (numParasToCheck >= 0 && (parasToCheck < 0 || numParasToCheck < parasToCheck)) {
          parasToCheck = numParasToCheck;
        }
        defaultParaCheck = PARA_CHECK_DEFAULT;
        mDocHandler.activateTextRulesByIndex(i, langTool);
        if (debugMode > 1) {
          MessageHandler.printToLogFile("ParaCeck: Index: " + i + "/" + minToCheckPara.size() 
            + "; numParasToCheck: " + numParasToCheck + OfficeTools.LOG_LINE_BREAK);
        }
        if (resetCheck.contains(paraNum) && parasToCheck < 0 && !useQueue) {
          oldCache = paragraphsCache.get(i);
          if (parasToCheck < -1) {
            paragraphsCache.set(i, new ResultCache());
          } else {
            paragraphsCache.set(i, new ResultCache(oldCache));
          }
        }
        pErrors.add(checkParaRules(paraText, paraNum, startSentencePos, endSentencePos, langTool, i, parasToCheck, isIntern));
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
          } else {
            addChangedParas();
          }
        }
      }
      oldCache = null;
      mDocHandler.reactivateTextRules(langTool);
    }
    return pErrors;
  }
  
  /**
   * add the numbers of changed paragraphs to list
   */
  private void addChangedParas() {
    int firstPara = resetFrom;
    if (firstPara < 0) {
      firstPara = 0;
    }
    int lastPara = resetTo;
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
        int nStart = docCache.getStartOfParaCheck(nTPara, nCheck, textIsChanged.contains(nTPara));
        int nEnd = docCache.getEndOfParaCheck(nTPara, nCheck, textIsChanged.contains(nTPara));
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
    int nStart = docCache.getStartOfParaCheck(nPara, nCheck, textIsChanged.contains(nPara));
    int nEnd = docCache.getEndOfParaCheck(nPara, nCheck, textIsChanged.contains(nPara));
    return mDocHandler.getTextLevelCheckQueue().createQueueEntry(nStart, nEnd, nCache, nCheck, docID);
  }

  /**
   * get the next queue entry which is the next empty cache entry
   */
  public QueueEntry getNextQueueEntry(int nPara, int nCache) {
    if (docCache != null) {
      for (int i = nPara + 1; i < docCache.textSize(); i++) {
        if (docCache.isFinished() && paragraphsCache.get(nCache).getEntryByParagraph(docCache.getFlatParagraphNumber(i)) == null) {
          return createQueueEntry(i, nCache);
        }
      }
      for (int i = 0; i < nPara; i++) {
        if (docCache.isFinished() && paragraphsCache.get(nCache).getEntryByParagraph(docCache.getFlatParagraphNumber(i)) == null) {
          return createQueueEntry(i, nCache);
        }
      }
      for (int n = 0; n < minToCheckPara.size(); n++) {
        if (n != nCache && minToCheckPara.get(n) != 0) {
          for (int i = 0; i < docCache.textSize(); i++) {
            if (docCache.isFinished() && paragraphsCache.get(n).getEntryByParagraph(docCache.getFlatParagraphNumber(i)) == null) {
              return createQueueEntry(i, n);
            }
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
  private SingleProofreadingError[] checkParaRules( String paraText, int nFPara, int startSentencePos, 
          int endSentencePos, SwJLanguageTool langTool, int cacheNum, int parasToCheck, boolean isIntern) {

    List<RuleMatch> paragraphMatches;
    SingleProofreadingError[] pErrors = null;
    try {
      // use Cache for check in single paragraph mode only after the first call of paragraph
      if (nFPara >= 0) {
        pErrors = paragraphsCache.get(cacheNum).getFromPara(nFPara, startSentencePos, endSentencePos);
        if (debugMode > 1 && pErrors != null) {
          MessageHandler.printToLogFile("Check Para Rules: pErrors from cache: " + pErrors.length);
        }
      } else {
        if (startSentencePos > 0 && lastSinglePara != null && lastSinglePara.equals(paraText)) {
          pErrors = singleParaCache.getFromPara(0, startSentencePos, endSentencePos);
          return pErrors;
        } else if (startSentencePos == 0) {
          lastSinglePara = paraText;
        }
      }
      // return Cache result if available / for right mouse click or Dialog only use cache
      int nPara = nFPara < 0 ? -1 : docCache.getNumberOfTextParagraph(nFPara);
      if (nFPara >= 0 && (pErrors != null || (useQueue && !isDialogRequest.contains(nFPara) && parasToCheck != 0))) {
        if (useQueue && pErrors == null && parasToCheck != 0 && nPara >= 0) {
          addQueueEntry(nFPara, cacheNum, parasToCheck, docID, false);
        }
        return pErrors;
      }
      
      String textToCheck;
      //  One paragraph check (set by options or proof of footnote, etc.)
      if (nPara < 0 || parasToCheck == 0) {
        textToCheck = DocumentCache.fixLinebreak(paraText);
        if (mDocHandler.isSortedRuleForIndex(cacheNum)) {
          paragraphMatches = langTool.check(textToCheck, true, JLanguageTool.ParagraphHandling.ONLYPARA);
        } else {
          paragraphMatches = null;
        }
        if (paragraphMatches == null || paragraphMatches.isEmpty()) {
          if (nFPara < 0) {
            singleParaCache.put(0, new SingleProofreadingError[0]);
          } else {
            paragraphsCache.get(cacheNum).put(nFPara, new SingleProofreadingError[0]);
            if (debugMode > 1) {
              MessageHandler.printToLogFile("--> checkParaRules: Enter to para cache(" + cacheNum + "): Paragraph(" + nFPara + "): " + paraText 
                + "; Error number: " + 0 + OfficeTools.LOG_LINE_BREAK);
            }
          }
        } else {
          List<SingleProofreadingError> errorList = new ArrayList<>();
          for (RuleMatch myRuleMatch : paragraphMatches) {
            int toPos = myRuleMatch.getToPos();
            if (toPos > paraText.length()) {
              toPos = paraText.length();
            }
            errorList.add(createOOoError(myRuleMatch, 0, toPos, isIntern ? ' ' : paraText.charAt(toPos-1)));
          }
          if (!errorList.isEmpty()) {
            if (nFPara < 0) {
              singleParaCache.put(0, errorList.toArray(new SingleProofreadingError[0]));
            } else {
              if (debugMode > 1) {
                MessageHandler.printToLogFile("--> checkParaRules: Enter to para cache(" + cacheNum + "): Paragraph: " + paraText 
                  + "; Error number: " + errorList.size() + OfficeTools.LOG_LINE_BREAK);
              }
              paragraphsCache.get(cacheNum).put(nFPara, errorList.toArray(new SingleProofreadingError[0]));
            }
          } else {
            if (nFPara < 0) {
              singleParaCache.put(0, new SingleProofreadingError[0]);
            } else {
              if (debugMode > 1) {
                MessageHandler.printToLogFile("--> checkParaRules: Enter to para cache(" + cacheNum + "): Paragraph(" + nFPara + "): " + paraText 
                  + "; Error number: " + 0 + OfficeTools.LOG_LINE_BREAK);
              }
              paragraphsCache.get(cacheNum).put(nFPara, new SingleProofreadingError[0]);
            }
          }
        }  
        if (nFPara < 0) {
          return singleParaCache.getFromPara(0, startSentencePos, endSentencePos);
        } else {
          return paragraphsCache.get(cacheNum).getFromPara(nFPara, startSentencePos, endSentencePos);
        }
      }

      //  check of numParasToCheck or full text 
      addParaErrorsToCache(nFPara, langTool, cacheNum, parasToCheck, false, isIntern);
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
    if (docCache == null) {
      return;
    }
    List<ResultCache> paragraphsCache = this.paragraphsCache;
    ResultCache sentencesCache = this.sentencesCache;
    boolean textIsChanged = this.textIsChanged.contains(nFPara);
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
      String textToCheck = docCache.getDocAsString(nTPara, parasToCheck, textIsChanged);
      List<RuleMatch> paragraphMatches = null;
      if (mDocHandler.isSortedRuleForIndex(cacheNum)) {
        paragraphMatches = langTool.check(textToCheck, true, JLanguageTool.ParagraphHandling.ONLYPARA);
      }
      
      int startPara = docCache.getStartOfParaCheck(nTPara, parasToCheck, textIsChanged);
      int endPara = docCache.getEndOfParaCheck(nTPara, parasToCheck, textIsChanged);
      int startPos = docCache.getStartOfParagraph(startPara, nTPara, parasToCheck, textIsChanged);
      int endPos;
      for (int i = startPara; i < endPara; i++) {
        if (useQueue && !isDialogRequest.contains(nFPara) && mDH.getTextLevelCheckQueue().isInterrupted()) {
          return;
        }
        if (i < endPara - 1) {
          endPos = docCache.getStartOfParagraph(i + 1, nTPara, parasToCheck, textIsChanged);
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
                errorList.add(createOOoError(myRuleMatch, -textPos, toPos, isIntern ? ' ' : docCache.getTextParagraph(i).charAt(toPos-1)));
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
            if (sentencesCache.getEntryByParagraph(n) != null) {
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
            SingleProofreadingError[] errors = paragraphsCache.get(cacheNum).getMatches(nFlat, 0);
            if (errors != null && errors.length != 0) {
              SingleProofreadingError[] filteredErrors = filterIgnoredMatches(errors, nFlat);
              if (sentencesCache.getEntryByParagraph(nFlat) != null && filteredErrors != null && filteredErrors.length != 0) {
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
   * check a single sentence
   */
  private SingleProofreadingError[] checkSentence(String sentence, int startPos, int nextPos, 
      int numCurPara, int[] footnotePositions, SwJLanguageTool langTool, boolean isIntern) {
    try {
      SingleProofreadingError[] errorArray;
      if (StringTools.isEmpty(sentence)) {
        errorArray = new SingleProofreadingError[0];
      } else {
        List<RuleMatch> ruleMatches;
        if (!langTool.isRemote()) {
          AnnotatedText annotatedText = getAnnotatedText(sentence, footnotePositions, startPos);
          ruleMatches = langTool.check(annotatedText, false, JLanguageTool.ParagraphHandling.ONLYNONPARA);
        } else {
          ruleMatches = langTool.check(sentence, true, JLanguageTool.ParagraphHandling.ONLYNONPARA);
        }
        if (!ruleMatches.isEmpty()) {
          errorArray = new SingleProofreadingError[ruleMatches.size()];
          int i = 0;
          for (RuleMatch myRuleMatch : ruleMatches) {
            errorArray[i] = createOOoError(myRuleMatch, startPos,
                                          sentence.length(), isIntern ? ' ' : sentence.charAt(sentence.length()-1));
            i++;
          }
        } else {
          errorArray = new SingleProofreadingError[0];
        }
      }
      if (numParasToCheck != 0 && numCurPara >= 0) {
        if (debugMode > 1) {
          MessageHandler.printToLogFile("--> Enter to sentences cache: numCurPara: " + numCurPara 
              + "; startPos: " + startPos + "; Sentence: " + sentence 
              + "; Error number: " + errorArray.length + OfficeTools.LOG_LINE_BREAK);
        }
        sentencesCache.put(numCurPara, startPos, nextPos, errorArray);
      }
      return errorArray;
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return null;
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
   * class to get a sentence out of a paragraph by using LanguageTool tokenization
   */
  private class SentenceFromPara {
    private int position;
    private String str;

    SentenceFromPara(String paraText, int startPos, SwJLanguageTool langTool) {
      List<String> tokenizedSentences = langTool.sentenceTokenize(cleanFootnotes(paraText));
      if (!tokenizedSentences.isEmpty()) {
        int i = 0;
        int index = -1;
        while (index < startPos && i < tokenizedSentences.size()) {
          index += tokenizedSentences.get(i).length();
          if (index < startPos) {
            i++;
          }
        }
        position = index + 1;
        if (i < tokenizedSentences.size()) {
          position -= tokenizedSentences.get(i).length();
          str = tokenizedSentences.get(i);
        } else {
          str = "";
        }
      } else {
        position = 0;
        str = "";
      }
    }
    int getPosition() {
      return position;
    }
    String getSentence() {
      return str;
    }
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
    SingleProofreadingError sError = sentencesCache.getErrorAtPosition(nPara, nChar);
    if (sError != null) {
      tmpErrors.add(sError);
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

}
