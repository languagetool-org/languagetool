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
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.gui.Configuration;
import org.languagetool.openoffice.DocumentCache.TextParagraph;
import org.languagetool.openoffice.OfficeTools.DocumentType;
import org.languagetool.openoffice.ResultCache.CacheEntry;
import org.languagetool.openoffice.SingleDocument.IgnoredMatches;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.text.TextMarkupType;

import static java.lang.System.arraycopy;

/**
 * Class for processing one LO/OO check request
 * Note: There can be some parallel requests from background iteration, dialog, right mouse click or LT text level iteration
 * Gives back the matches found by LT
 * Adds matches to result cache
 * @since 5.3
 * @author Fred Kruse, (including some methods developed by Marcin Miłkowski)
 */
class SingleCheck {
  
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
  
  private static int debugMode;                     //  should be 0 except for testing; 1 = low level; 2 = advanced level

  private final SingleDocument singleDocument;      //  handles one document
  private final MultiDocumentsHandler mDocHandler;  //  handles the different documents loaded in LO/OO
  private final XComponent xComponent;              //  XComponent of the open document
  private final Configuration config;
  private final DocumentCache docCache;             //  cache of paragraphs (only readable by parallel thread)
  private final List<Integer> minToCheckPara;       //  List of minimal to check paragraphs for different classes of text level rules
  private final List<ResultCache> paragraphsCache;  //  Cache for matches of text rules
  private final int numParasToCheck;                //  current number of Paragraphs to be checked
  private final DocumentType docType;               //  save the type of document
  private final boolean isDialogRequest;            //  true: check was initiated by proofreading dialog
  private final boolean isMouseRequest;             //  true: check was initiated by right mouse click
  private final boolean isIntern;                   //  true: check was initiated by intern check dialog
  private final boolean useQueue;                   //  true: use queue to check text level rules (will be overridden by config)
  private final Language docLanguage;               //  Language used for check
  private final IgnoredMatches ignoredMatches;      //  Map of matches (number of paragraph, number of character) that should be ignored after ignoreOnce was called
  private DocumentCursorTools docCursor;            //  Save document cursor for the single document
  private FlatParagraphTools flatPara;              //  Save information for flat paragraphs (including iterator and iterator provider) for the single document

  private int changeFrom = 0;                       //  Change result cache from paragraph
  private int changeTo = 0;                         //  Change result cache to paragraph
  private String lastSinglePara = null;             //  stores the last paragraph which is checked as single paragraph

  private List<Integer> changedParas;               //  List of changed paragraphs after editing the document
  
  SingleCheck(SingleDocument singleDocument, List<ResultCache> paragraphsCache, DocumentCursorTools docCursor,
      FlatParagraphTools flatPara, Language docLanguage, IgnoredMatches ignoredMatches, 
      int numParasToCheck, boolean isDialogRequest, boolean isMouseRequest, boolean isIntern) {
    debugMode = OfficeTools.DEBUG_MODE_SC;
    this.singleDocument = singleDocument;
    this.paragraphsCache = paragraphsCache;
    this.docCursor = docCursor;
    this.flatPara = flatPara;
    this.numParasToCheck = numParasToCheck;
    this.isDialogRequest = isDialogRequest;
    this.isMouseRequest = isMouseRequest;
    this.isIntern = isIntern;
    this.docLanguage = docLanguage;
    this.ignoredMatches = ignoredMatches;
    mDocHandler = singleDocument.getMultiDocumentsHandler();
    xComponent = singleDocument.getXComponent();
    docCache = singleDocument.getDocumentCache();
    docType = singleDocument.getDocumentType();
    config = mDocHandler.getConfiguration();
    useQueue = numParasToCheck != 0 && !isDialogRequest && !mDocHandler.isTestMode() && config.useTextLevelQueue();
    minToCheckPara = mDocHandler.getNumMinToCheckParas();
    changedParas = new ArrayList<>();
  }
  
  /**
   *   get the result for a check of a single document 
   */
  public SingleProofreadingError[] getCheckResults(String paraText, int[] footnotePositions, Locale locale, SwJLanguageTool lt, 
      int paraNum, int startOfSentence, boolean textIsChanged, int changeFrom, int changeTo, String lastSinglePara, 
      int lastChangedPara) {
    if (isDisposed()) {
      return new SingleProofreadingError[0];
    }
    if (docType == DocumentType.WRITER && !isIntern && lastChangedPara >= 0) {
      if (docCursor == null) {
        docCursor = new DocumentCursorTools(xComponent);
      }
      List<Integer> changedParas = singleDocument.getLastChangedParas();
      if (changedParas == null) {
        changedParas = new ArrayList<Integer>();
      } else {
        singleDocument.setLastChangedParas(null);
      }
      if (changedParas.contains(lastChangedPara) )
      changedParas.add(lastChangedPara);
      remarkChangedParagraphs(changedParas, docCursor, flatPara, lt, true);
    }
    this.lastSinglePara = lastSinglePara;
    if (numParasToCheck != 0 && paraNum >= 0) {
      //  test real flat paragraph rather then the one given by Proofreader - it could be changed meanwhile
      //  Don't use Cache for check in single paragraph mode
      paraText = docCache.getFlatParagraph(paraNum);
    }
    List<SingleProofreadingError[]> pErrors = checkTextRules(paraText, locale, footnotePositions, paraNum, startOfSentence, lt, textIsChanged, isIntern);
    startOfSentence = paragraphsCache.get(0).getStartSentencePosition(paraNum, startOfSentence);
    SingleProofreadingError[] errors = mergeErrors(pErrors, paraNum);
    if (debugMode > 1) {
      MessageHandler.printToLogFile("SingleCheck: getCheckResults: paRes.aErrors.length: " + errors.length 
          + "; docID: " + singleDocument.getDocID());
    }
    if (!isDisposed() && docType == DocumentType.WRITER && numParasToCheck != 0 && paraNum >= 0 && (textIsChanged || isDialogRequest)) {
      if (docCursor == null && !isDisposed()) {
        docCursor = new DocumentCursorTools(xComponent);
      }
      if (!isIntern && ((isDialogRequest && !textIsChanged) || (useQueue && !isDialogRequest))) {
        List<Integer> changedParas = new ArrayList<Integer>();
        changedParas.add(paraNum);
        remarkChangedParagraphs(changedParas, docCursor, flatPara, lt, true);
      } else if (textIsChanged && (!useQueue || isDialogRequest)) {
        remarkChangedParagraphs(changedParas, docCursor, flatPara, lt, true);
      }
    }
    return errors;
  }
  
  /**
   *   check for number of Paragraphs > 0, chapter wide or full text
   *   is also called by text level queue
   */
  public void addParaErrorsToCache(int nFPara, SwJLanguageTool lt, int cacheNum, int parasToCheck, 
        boolean checkOnlyParagraph, boolean override, boolean isIntern, boolean hasFootnotes) {
    //  make the method thread save
    MultiDocumentsHandler mDH = mDocHandler;
    DocumentCursorTools docCursor = this.docCursor;
    if (isDisposed() || docCache == null || nFPara < 0 || nFPara >= docCache.size()) {
      MessageHandler.printToLogFile("SingleCheck: addParaErrorsToCache: return: isDisposed = " + isDisposed() + ", nFPara = " + nFPara 
          + ", docCache(Size) = " + (docCache == null ? "null" : docCache.size()) );
      return;
    }
    if (lt == null) {
      MessageHandler.printToLogFile("SingleCheck: addParaErrorsToCache: return: lt is null");
    }
    DocumentCache docCache = new DocumentCache(this.docCache);
    try {

      ResultCache oldCache = null;
      if (useQueue && !isDialogRequest) {
        oldCache = new ResultCache(paragraphsCache.get(cacheNum));
      }
      
      TextParagraph tPara = docCache.getNumberOfTextParagraph(nFPara);
      int cursorType = tPara.type;
      
      String textToCheck = docCache.getDocAsString(tPara, parasToCheck, checkOnlyParagraph, useQueue, hasFootnotes);
      List<RuleMatch> paragraphMatches = null;
      //  NOTE: lt == null if language is not supported by LT
      //        but empty proof reading errors have added to cache to satisfy text level queue
      if (lt != null && mDocHandler.isSortedRuleForIndex(cacheNum)) {
        paragraphMatches = lt.check(textToCheck, true, JLanguageTool.ParagraphHandling.ONLYPARA);
      }
      
      int startPara = docCache.getStartOfParaCheck(tPara, parasToCheck, checkOnlyParagraph, useQueue, false);
      int endPara = docCache.getEndOfParaCheck(tPara, parasToCheck, checkOnlyParagraph, useQueue, false);
      int startPos = docCache.getStartOfParagraph(startPara, tPara, parasToCheck, checkOnlyParagraph, useQueue, hasFootnotes);
      int endPos;
      for (int i = startPara; i < endPara; i++) {
        if (isDisposed() || (useQueue && !isDialogRequest && (mDH.getTextLevelCheckQueue() == null || mDH.getTextLevelCheckQueue().isInterrupted()))) {
          MessageHandler.printToLogFile("SingleCheck: addParaErrorsToCache: return: isDisposed = " + isDisposed() + ", useQueue = " + useQueue
              + ", isDialogRequest = " + isDialogRequest + ", TextLevelCheckQueue(isInterrupted) = " 
              + (mDH.getTextLevelCheckQueue() == null ? "null" : mDH.getTextLevelCheckQueue().isInterrupted()));
          return;
        }
        TextParagraph textPara = docCache.createTextParagraph(cursorType, i);
        int[] footnotePos = docCache.getTextParagraphFootnotes(textPara);
        if (i < endPara - 1) {
          endPos = docCache.getStartOfParagraph(i + 1, tPara, parasToCheck, checkOnlyParagraph, useQueue, hasFootnotes);
        } else {
          endPos = textToCheck.length();
        }
        if (paragraphMatches == null || paragraphMatches.isEmpty()) {
          paragraphsCache.get(cacheNum).put(docCache.getFlatParagraphNumber(textPara), new SingleProofreadingError[0]);
          if (debugMode > 1) {
            MessageHandler.printToLogFile("SingleCheck: addParaErrorsToCache: Enter to para cache(" + cacheNum + "): Paragraph(" 
                + docCache.getFlatParagraphNumber(textPara) + "): " + docCache.getTextParagraph(textPara) + "; Error number: 0");
          }
        } else {
          List<SingleProofreadingError> errorList = new ArrayList<>();
          int textPos = startPos;
          if (textPos < 0) textPos = 0;
          for (RuleMatch myRuleMatch : paragraphMatches) {
            int startErrPos = myRuleMatch.getFromPos();
            if (debugMode > 2) {
              MessageHandler.printToLogFile("SingleCheck: addParaErrorsToCache: Cache = " + cacheNum 
                  + ", startPos = " + startPos + ", endPos = " + endPos + ", startErrPos = " + startErrPos);
            }
            if (startErrPos >= startPos && startErrPos < endPos) {
              int toPos = docCache.getTextParagraph(textPara).length();
              if (toPos > 0) {
                errorList.add(correctRuleMatchWithFootnotes(
                    createOOoError(myRuleMatch, -textPos, toPos, isIntern ? ' ' : docCache.getTextParagraph(textPara).charAt(toPos-1)),
                      footnotePos));
              }
            }
          }
          if (!errorList.isEmpty()) {
            paragraphsCache.get(cacheNum).put(docCache.getFlatParagraphNumber(textPara), errorList.toArray(new SingleProofreadingError[0]));
            if (debugMode > 1) {
              MessageHandler.printToLogFile("SingleCheck: addParaErrorsToCache: Enter to para cache(" + cacheNum + "): Paragraph(" 
                  + docCache.getFlatParagraphNumber(textPara) + "): " + docCache.getTextParagraph(textPara) 
                  + "; Error number: " + errorList.size());
            }
          } else {
            paragraphsCache.get(cacheNum).put(docCache.getFlatParagraphNumber(textPara), new SingleProofreadingError[0]);
            if (debugMode > 1) {
              MessageHandler.printToLogFile("SingleCheck: addParaErrorsToCache: Enter to para cache(" + cacheNum + "): Paragraph(" 
                  + docCache.getFlatParagraphNumber(textPara) + "): " + docCache.getTextParagraph(textPara) + "; Error number: 0");
            }
          }
        }
        startPos = endPos;
      }
      if (!isDisposed() && docType == DocumentType.WRITER && useQueue && !isDialogRequest) {
        if (mDH.getTextLevelCheckQueue() == null || mDH.getTextLevelCheckQueue().isInterrupted()) {
          return;
        }
        if (docCursor == null) {
          docCursor = new DocumentCursorTools(xComponent);
        }
        flatPara = singleDocument.setFlatParagraphTools();
        
        List<Integer> changedParas = new ArrayList<>();
        if (oldCache != null) {
          for (int nText = startPara; nText < endPara; nText++) {
            int nFlat = docCache.getFlatParagraphNumber(docCache.createTextParagraph(cursorType, nText));
            if (paragraphsCache.get(0).getCacheEntry(nFlat) != null) {
              if (ResultCache.areDifferentEntries(paragraphsCache.get(cacheNum).getCacheEntry(nFlat), oldCache.getCacheEntry(nFlat))) {
                changedParas.add(nFlat);
              }
            }
          }
          if (!changedParas.isEmpty()) {
            if (debugMode > 1) {
              MessageHandler.printToLogFile("SingleCheck: addParaErrorsToCache: Mark paragraphs from " 
                  + startPara + " to " + endPara + ": " + changedParas.size() 
                  + " changes, tPara.type: " + tPara.type + ", tPara.number: " + tPara.number + ", nFPara: " + nFPara);
              String tmpText = "Changed Paras: ";
              for (int n : changedParas) {
                tmpText += n + " ";
              }
              MessageHandler.printToLogFile(tmpText);
            }
            singleDocument.setLastChangedParas(changedParas);
            remarkChangedParagraphs(changedParas, docCursor, flatPara, lt, override);
          } else if (debugMode > 1) {
            MessageHandler.printToLogFile("SingleCheck: addParaErrorsToCache: Mark paragraphs from " + startPara + " to " + endPara 
                + ": No Paras to Mark, tPara.type: " + tPara.type + ", tPara.number: " + tPara.number + ", nFPara: " + nFPara);
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }
  
  /**
   * remark changed paragraphs
   * override existing marks
   */
  public void remarkChangedParagraphs(List<Integer> changedParas, DocumentCursorTools docCursor, 
      FlatParagraphTools flatPara, SwJLanguageTool lt, boolean override) {
    if (!isDisposed() && !mDocHandler.isSwitchedOff() && (!isDialogRequest || isIntern)) {
      Map <Integer, List<SentenceErrors>> changedParasMap = new HashMap<>();
      for (int i = 0; i < changedParas.size(); i++) {
        List<SentenceErrors> sentenceErrors = getSentenceErrosAsList(changedParas.get(i), lt);
        changedParasMap.put(changedParas.get(i), sentenceErrors);
        if (debugMode > 1) {
          MessageHandler.printToLogFile("SingleCheck: remarkChangedParagraphs: Mark errors: Paragraph: " + changedParas.get(i) 
            + "; Number of sentence: " + sentenceErrors.size()
            + "; Number of errors: " + (sentenceErrors.size() > 0 ? sentenceErrors.get(0).sentenceErrors.length : 0));
        }
      }
      flatPara.markParagraphs(changedParasMap, docCache, override, docCursor);
    }
  }
  
  /**
   *  return last single paragraph (not text paragraph)
   */
  public String getLastSingleParagraph () {
    return lastSinglePara;
  }

  /**
   *  Is document disposed?
   */
  private boolean isDisposed() {
    return singleDocument.isDisposed();
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
    if (!ignoredMatches.isEmpty() && ignoredMatches.containsParagraph(nPara)) {
      List<SingleProofreadingError> filteredErrors = new ArrayList<>();
      for (SingleProofreadingError error : unFilteredErrors) {
        if (!ignoredMatches.isIgnored(error.nErrorStart, error.nErrorStart + error.nErrorLength, nPara, error.aRuleIdentifier)) {
          filteredErrors.add(error);
        }
      }
      if (debugMode > 2) {
        MessageHandler.printToLogFile("SingleCheck: filterIgnoredMatches: unFilteredErrors.length: " + unFilteredErrors.length);
        MessageHandler.printToLogFile("SingleCheck: filterIgnoredMatches: filteredErrors.length: " + filteredErrors.size());
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
  private List<SingleProofreadingError[]> checkTextRules( String paraText, Locale locale, int[] footnotePos, int paraNum, 
      int startSentencePos, SwJLanguageTool lt, boolean textIsChanged, boolean isIntern) {
    List<SingleProofreadingError[]> pErrors = new ArrayList<>();
    if (isDisposed()) {
      return pErrors;
    }
    TextParagraph nTParas = paraNum < 0 ? null : docCache.getNumberOfTextParagraph(paraNum);
    if (nTParas == null || nTParas.type == DocumentCache.CURSOR_TYPE_UNKNOWN) {
      pErrors.add(checkParaRules(paraText, locale, footnotePos, paraNum, startSentencePos, lt, 0, 0, textIsChanged, isIntern));
    } else {
      //  Real full text check / numParas < 0
      ResultCache oldCache = null;
      List<Integer> tmpChangedParas;
      for (int i = 0; i < minToCheckPara.size(); i++) {
        int parasToCheck = minToCheckPara.get(i);
        if (i == 0 || mDocHandler.isSortedRuleForIndex(i)) {
          mDocHandler.activateTextRulesByIndex(i, lt);
          if (debugMode > 1) {
            MessageHandler.printToLogFile("SingleCheck: checkTextRules: Index: " + i + "/" + minToCheckPara.size() 
            + "; paraNum: " + paraNum + "; numParasToCheck: " + parasToCheck);
          }
          if (textIsChanged && !useQueue && parasToCheck != 0 ) {
            oldCache = new ResultCache(paragraphsCache.get(i));
          }
          pErrors.add(checkParaRules(paraText, locale, footnotePos, paraNum, startSentencePos, lt, i, parasToCheck, textIsChanged, isIntern));
          if (!isDisposed() && textIsChanged && !useQueue) {
            if (parasToCheck != 0) {
              tmpChangedParas = paragraphsCache.get(i).differenceInCaches(oldCache);
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
      mDocHandler.reactivateTextRules(lt);
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
    for (int n = firstPara; n < lastPara; n++) {
      if (!changedParas.contains(n)) {
        changedParas.add(n);
      }
    }
  }

  /**
   * check the text level rules associated with a given cache (cacheNum)
   */
  @Nullable
  private SingleProofreadingError[] checkParaRules(String paraText, Locale locale, int[] footnotePos, int nFPara, int sentencePos, 
          SwJLanguageTool lt, int cacheNum, int parasToCheck, boolean textIsChanged, boolean isIntern) {

    List<RuleMatch> paragraphMatches;
    SingleProofreadingError[] pErrors = null;
    int startSentencePos = 0;
    int endSentencePos = 0;
    try {
      if (isDisposed()) {
        return pErrors;
      }
      boolean isMultiLingual = nFPara >= 0 ? docCache.isMultilingualFlatParagraph(nFPara) : false;
      // use Cache for check in single paragraph mode only after the first call of paragraph
      if (nFPara >= 0 || (sentencePos > 0 && lastSinglePara != null && lastSinglePara.equals(paraText))) {
        if (paragraphsCache.get(0).getCacheEntry(nFPara) != null) {
          startSentencePos = paragraphsCache.get(0).getStartSentencePosition(nFPara, sentencePos);
          endSentencePos = paragraphsCache.get(0).getNextSentencePosition(nFPara, sentencePos);
          pErrors = paragraphsCache.get(cacheNum).getFromPara(nFPara, startSentencePos, endSentencePos);
          if (debugMode > 1 && pErrors != null) {
            MessageHandler.printToLogFile("SingleCheck: checkParaRules: Para: " + nFPara + "; pErrors from cache(" + cacheNum + "): " + pErrors.length);
          }
        }
      } else if (sentencePos == 0) {
        lastSinglePara = paraText;
      }
      // return Cache result if available / for right mouse click or Dialog only use cache
      boolean isTextParagraph = nFPara >= 0 && docCache != null && docCache.getNumberOfTextParagraph(nFPara).type != DocumentCache.CURSOR_TYPE_UNKNOWN;
      if (nFPara >= 0 && (pErrors != null || isMouseRequest || (useQueue && !isDialogRequest && parasToCheck != 0))) {
        if (useQueue && pErrors == null && parasToCheck > 0 && isTextParagraph && !textIsChanged && mDocHandler.getTextLevelCheckQueue().isWaiting()) {
          mDocHandler.getTextLevelCheckQueue().wakeupQueue(singleDocument.getDocID());
        }
        return pErrors;
      }
      
      //  One paragraph check (set by options or proof of footnote, etc.)
      if (!isTextParagraph || parasToCheck == 0) {
        Locale primaryLocale = isMultiLingual ? docCache.getFlatParagraphLocale(nFPara) : locale;
        SwJLanguageTool mLt;
        if (OfficeTools.isEqualLocale(primaryLocale, locale) || !mDocHandler.hasLocale(primaryLocale)) {
          mLt = lt;
        } else {
          mLt = mDocHandler.initLanguageTool(mDocHandler.getLanguage(primaryLocale), false);
          mDocHandler.initCheck(mLt);
        }
        List<Integer> nextSentencePositions = getNextSentencePositions(paraText, mLt);
        if (mLt == null) {
          paragraphMatches = null;
        } else {
          paragraphMatches = mLt.check(removeFootnotes(paraText, footnotePos), true, JLanguageTool.ParagraphHandling.NORMAL);
        }
        if (isDisposed()) {
          return null;
        }
        if (paragraphMatches == null || paragraphMatches.isEmpty()) {
          paragraphsCache.get(cacheNum).put(nFPara, nextSentencePositions, new SingleProofreadingError[0]);
          if (debugMode > 1) {
            MessageHandler.printToLogFile("SingleCheck: checkParaRules: Enter " + (isMultiLingual ? "only para " : " ") + "errors to cache(" 
                + cacheNum + "): Paragraph(" + nFPara + "): " + paraText + "; Error number: " + 0);
          }
        } else {
          List<SingleProofreadingError> errorList = new ArrayList<>();
          for (RuleMatch myRuleMatch : paragraphMatches) {
            int toPos = myRuleMatch.getToPos();
            if (toPos > paraText.length()) {
              toPos = paraText.length();
            }
            errorList.add(correctRuleMatchWithFootnotes(
                createOOoError(myRuleMatch, 0, toPos, isIntern ? ' ' : paraText.charAt(toPos-1)), footnotePos));
          }
          if (!errorList.isEmpty()) {
            if (debugMode > 1) {
              MessageHandler.printToLogFile("SingleCheck: checkParaRules: Enter " + (isMultiLingual ? "only para " : " ") + "errors to cache(" 
                  + cacheNum + "): Paragraph(" + nFPara + "): " + paraText + "; Error number: " + errorList.size());
            }
            paragraphsCache.get(cacheNum).put(nFPara, nextSentencePositions, errorList.toArray(new SingleProofreadingError[0]));
          } else {
            if (debugMode > 1) {
              MessageHandler.printToLogFile("SingleCheck: checkParaRules: Enter " + (isMultiLingual ? "only para " : " ") + "errors to cache(" 
                  + cacheNum + "): Paragraph(" + nFPara + "): " + nFPara + "): " + paraText + "; Error number: " + 0);
            }
            paragraphsCache.get(cacheNum).put(nFPara, nextSentencePositions, new SingleProofreadingError[0]);
          }
        }
        startSentencePos = paragraphsCache.get(cacheNum).getStartSentencePosition(nFPara, sentencePos);
        endSentencePos = paragraphsCache.get(cacheNum).getNextSentencePosition(nFPara, sentencePos);
        return paragraphsCache.get(cacheNum).getFromPara(nFPara, startSentencePos, endSentencePos);
      }

      //  check of numParasToCheck or full text 
      if (isDisposed()) {
        return null;
      }
      addParaErrorsToCache(nFPara, lt, cacheNum, parasToCheck, textIsChanged, textIsChanged, isIntern, (footnotePos != null));
      return paragraphsCache.get(cacheNum).getFromPara(nFPara, startSentencePos, endSentencePos);

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
    String msg = ruleMatch.getMessage();
    if (docLanguage != null) {
      msg = docLanguage.toAdvancedTypography(msg);
    }
    msg = msg.replaceAll("<suggestion>", docLanguage == null ? "\"" : docLanguage.getOpeningDoubleQuote())
        .replaceAll("</suggestion>", docLanguage == null ? "\"" : docLanguage.getClosingDoubleQuote())
        .replaceAll("([\r]*\n)", " "); 
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
    Color underlineColor = config.getUnderlineColor(ruleMatch.getRule().getCategory().getName(), ruleMatch.getRule().getId());
    short underlineType = config.getUnderlineType(ruleMatch.getRule().getCategory().getName(), ruleMatch.getRule().getId());
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
  private List<Integer> getNextSentencePositions (String paraText, SwJLanguageTool lt) {
    List<Integer> nextSentencePositions = new ArrayList<Integer>();
    if (paraText == null || paraText.isEmpty()) {
      nextSentencePositions.add(0);
      return nextSentencePositions;
    }
    if (lt == null || lt.isRemote()) {
      nextSentencePositions.add(paraText.length());
    } else {
      List<String> tokenizedSentences = lt.sentenceTokenize(cleanFootnotes(paraText));
      int position = 0;
      for (String sentence : tokenizedSentences) {
        position += sentence.length();
        nextSentencePositions.add(position);
      }
      if (nextSentencePositions.get(nextSentencePositions.size() - 1) != paraText.length()) {
        nextSentencePositions.set(nextSentencePositions.size() - 1, paraText.length());
      }
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
      if (footnotes[i] < paraText.length()) {
        paraText = paraText.substring(0, footnotes[i]) + paraText.substring(footnotes[i] + 1);
      }
    }
    return paraText;
  }
  
  /**
   * Correct SingleProofreadingError by footnote positions
   * footnotes before is the sum of all footnotes before the checked paragraph
   */
  private static SingleProofreadingError correctRuleMatchWithFootnotes(SingleProofreadingError pError, int[] footnotes) {
    if (footnotes == null || footnotes.length == 0) {
      return pError;
    }
    for (int i :footnotes) {
      if (i <= pError.nErrorStart) {
        pError.nErrorStart++;
      } else if (i < pError.nErrorStart + pError.nErrorLength) {
        pError.nErrorLength++;
      }
    }
    return pError;
  }
  
  /**
   * get all errors of a Paragraph as list
   */
  private List<SentenceErrors> getSentenceErrosAsList(int numberOfParagraph, SwJLanguageTool lt) {
    List<SentenceErrors> sentenceErrors = new ArrayList<SentenceErrors>();
    if (!isDisposed()) {
      CacheEntry entry = paragraphsCache.get(0).getCacheEntry(numberOfParagraph);
      List<Integer> nextSentencePositions = null;
      if (entry != null) {
        nextSentencePositions = entry.nextSentencePositions;
      }
      if (nextSentencePositions == null) {
        nextSentencePositions = new ArrayList<Integer>();
      }
      if (nextSentencePositions.size() == 0 && docCache != null 
          && numberOfParagraph >= 0 && numberOfParagraph < docCache.size()) {
        nextSentencePositions = getNextSentencePositions(docCache.getFlatParagraph(numberOfParagraph), lt);
      }
      int startPosition = 0;
      if (nextSentencePositions.size() == 1) {
        List<SingleProofreadingError[]> errorList = new ArrayList<SingleProofreadingError[]>();
        for (ResultCache cache : paragraphsCache) {
          CacheEntry cacheEntry = cache.getCacheEntry(numberOfParagraph);
          errorList.add(cacheEntry == null ? null : cacheEntry.getErrorArray());
        }
        sentenceErrors.add(new SentenceErrors(startPosition, nextSentencePositions.get(0), mergeErrors(errorList, numberOfParagraph)));
      } else {
        for (int nextPosition : nextSentencePositions) {
          List<SingleProofreadingError[]> errorList = new ArrayList<SingleProofreadingError[]>();
          for (ResultCache cache : paragraphsCache) {
            errorList.add(cache.getFromPara(numberOfParagraph, startPosition, nextPosition));
          }
          sentenceErrors.add(new SentenceErrors(startPosition, nextPosition, mergeErrors(errorList, numberOfParagraph)));
          startPosition = nextPosition;
        }
      }
    }
    return sentenceErrors;
  }

  /**
   * Class of proofreading errors of one sentence
   */
  class SentenceErrors {
    final int sentenceStart;
    final int sentenceEnd;
    final SingleProofreadingError[] sentenceErrors;
    
    SentenceErrors(int start, int end, SingleProofreadingError[] errors) {
      sentenceStart = start;
      sentenceEnd = end;
      sentenceErrors = errors;
    }
  }

  
}
