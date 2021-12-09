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
import com.sun.star.text.XParagraphCursor;

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
  private final int numParasToCheck;                // current number of Paragraphs to be checked
  private final boolean isImpress;                  //  true: is an Impress document
  private final boolean isDialogRequest;            //  true: check was initiated by right mouse click or proofreading dialog
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
      int numParasToCheck, boolean isDialogRequest) {
    debugMode = OfficeTools.DEBUG_MODE_SD;
    this.singleDocument = singleDocument;
    this.paragraphsCache = paragraphsCache;
    this.docCursor = docCursor;
    this.flatPara = flatPara;
    this.numParasToCheck = numParasToCheck;
    this.isDialogRequest = isDialogRequest;
    this.docLanguage = docLanguage;
    this.ignoredMatches = ignoredMatches;
    mDocHandler = singleDocument.getMultiDocumentsHandler();
    xComponent = singleDocument.getXComponent();
    docCache = singleDocument.getDocumentCache();
    isImpress = singleDocument.isImpress();
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
      int lastChangedPara, boolean isIntern) {
    if (isDisposed()) {
      return new SingleProofreadingError[0];
    }
    if (!isImpress && !isIntern && lastChangedPara >= 0) {
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
      remarkChangedParagraphs(changedParas, docCursor.getParagraphCursor(), flatPara, lt, true);
    }
    this.lastSinglePara = lastSinglePara;
    if (numParasToCheck != 0 && paraNum >= 0) {
      //  test real flat paragraph rather then the one given by Proofreader - it could be changed meanwhile
      //  Don't use Cache for check in single paragraph mode
      paraText = docCache.getFlatParagraph(paraNum);
    }
    List<SingleProofreadingError[]> pErrors = checkTextRules(paraText, footnotePositions, paraNum, startOfSentence, lt, textIsChanged, isIntern);
    startOfSentence = paragraphsCache.get(0).getStartSentencePosition(paraNum, startOfSentence);
    int nextSentence = paragraphsCache.get(0).getNextSentencePosition(paraNum, startOfSentence);
    SingleProofreadingError[] errors = mergeErrors(pErrors, paraNum);
    if (debugMode > 1) {
      MessageHandler.printToLogFile("paRes.aErrors.length: " + errors.length + "; docID: " + singleDocument.getDocID() + OfficeTools.LOG_LINE_BREAK);
    }
    if (!isImpress && numParasToCheck != 0 && paraNum >= 0 && ((textIsChanged && nextSentence >= paraText.length()) || isDialogRequest)) {
      if (docCursor == null && !isDisposed()) {
        docCursor = new DocumentCursorTools(xComponent);
      }
      if (!isIntern && ((isDialogRequest && !textIsChanged) || (useQueue && !isDialogRequest))) {
        List<Integer> changedParas = new ArrayList<Integer>();
        changedParas.add(paraNum);
        remarkChangedParagraphs(changedParas, docCursor.getParagraphCursor(), flatPara, lt, true);
      } else if (textIsChanged && (!useQueue || isDialogRequest)) {
        remarkChangedParagraphs(changedParas, docCursor.getParagraphCursor(), flatPara, lt, true);
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
    DocumentCache docCache = this.docCache;
    if (isDisposed()) {
      return;
    }
    if (docCache == null || nFPara < 0 || nFPara >= docCache.size()) {
      return;
    }
    List<ResultCache> paragraphsCache = this.paragraphsCache;
    try {

      ResultCache oldCache = null;
      if (useQueue && !isDialogRequest) {
/*
        oldCache = paragraphsCache.get(cacheNum);
        if (parasToCheck < -1) {
          paragraphsCache.set(cacheNum, new ResultCache());
        } else {
          paragraphsCache.set(cacheNum, new ResultCache(oldCache));
        }
*/
        oldCache = new ResultCache(paragraphsCache.get(cacheNum));
      }
      
      int nTPara = docCache.getNumberOfTextParagraph(nFPara);
      String textToCheck = docCache.getDocAsString(nTPara, parasToCheck, checkOnlyParagraph, useQueue, hasFootnotes);
      List<RuleMatch> paragraphMatches = null;
      if (lt != null && mDocHandler.isSortedRuleForIndex(cacheNum)) {
        paragraphMatches = lt.check(textToCheck, true, JLanguageTool.ParagraphHandling.ONLYPARA);
      }
      
      int startPara = docCache.getStartOfParaCheck(nTPara, parasToCheck, checkOnlyParagraph, useQueue, false);
      int endPara = docCache.getEndOfParaCheck(nTPara, parasToCheck, checkOnlyParagraph, useQueue, false);
      int startPos = docCache.getStartOfParagraph(startPara, nTPara, parasToCheck, checkOnlyParagraph, useQueue);
      int endPos;
      int footnotesBefore = 0;
      for (int i = startPara; i < endPara; i++) {
        if (useQueue && !isDialogRequest && (mDH.getTextLevelCheckQueue() == null || mDH.getTextLevelCheckQueue().isInterrupted())) {
          return;
        }
        int[] footnotePos = docCache.getTextParagraphFootnotes(i);
        if (i < endPara - 1) {
          endPos = docCache.getStartOfParagraph(i + 1, nTPara, parasToCheck, checkOnlyParagraph, useQueue);
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
                errorList.add(correctRuleMatchWithFootnotes(
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
      if (!isImpress && useQueue && !isDialogRequest) {
        if (mDH.getTextLevelCheckQueue() == null || mDH.getTextLevelCheckQueue().isInterrupted()) {
          return;
        }
        if (isDisposed()) {
          return;
        }
        if (docCursor == null && !isDisposed()) {
          docCursor = new DocumentCursorTools(xComponent);
        }
        flatPara = singleDocument.setFlatParagraphTools();
        
        List<Integer> changedParas = new ArrayList<>();
        if (oldCache != null) {
          for (int nText = startPara; nText < endPara; nText++) {
            int nFlat = docCache.getFlatParagraphNumber(nText);
            // TODO: remove after tests
//            if (override || paragraphsCache.get(0).getCacheEntry(nFlat) != null) {
            if (paragraphsCache.get(0).getCacheEntry(nFlat) != null) {
              if (ResultCache.areDifferentEntries(paragraphsCache.get(cacheNum).getCacheEntry(nFlat), oldCache.getCacheEntry(nFlat))) {
                changedParas.add(nFlat);
              }
            }
          }
          if (!changedParas.isEmpty()) {
            if (debugMode > 1) {
              MessageHandler.printToLogFile("Mark paragraphs from " + startPara + " to " + endPara + ": " + changedParas.size() 
                  + " changes, nTPara: " + nTPara + ", nFPara: " + nFPara);
              String tmpText = "Changed Paras: ";
              for (int n : changedParas) {
                tmpText += n + " ";
              }
              MessageHandler.printToLogFile(tmpText);
            }
            singleDocument.setLastChangedParas(changedParas);
            remarkChangedParagraphs(changedParas, docCursor.getParagraphCursor(), flatPara, lt, override);
          } else if (debugMode > 1) {
            MessageHandler.printToLogFile("Mark paragraphs from " + startPara + " to " + endPara + ": No Paras to Mark, nTPara: " + nTPara + ", nFPara: " + nFPara);
          }
        }
/*        
//        if (override) {     //  TODO: remove after tests
          List<Integer> tmpChangedParas;
          tmpChangedParas = paragraphsCache.get(cacheNum).differenceInCaches(oldCache);
          List<Integer> changedParas = new ArrayList<>();
          for (int n : tmpChangedParas) {
            if (textIsChanged || paragraphsCache.get(0).getCacheEntry(n) != null) {
              changedParas.add(n);
            }
          }
          if (debugMode > 1) {
            MessageHandler.printToLogFile("Mark paragraphs (override) numChanged: " + changedParas.size());
          }
          if (!changedParas.isEmpty()) {
            remarkChangedParagraphs(changedParas, docCursor.getParagraphCursor(), flatPara);
          }
/*        } else {
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
*/
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }
  
  /**
   * remark changed paragraphs
   * override existing marks
   */
  public void remarkChangedParagraphs(List<Integer> changedParas, XParagraphCursor cursor, 
      FlatParagraphTools flatPara, SwJLanguageTool lt, boolean override) {
    if (!isDisposed() && !mDocHandler.isSwitchedOff()) {
      Map <Integer, List<SentenceErrors>> changedParasMap = new HashMap<>();
      for (int i = 0; i < changedParas.size(); i++) {
        List<SentenceErrors> sentenceErrors = getSentenceErrosAsList(changedParas.get(i), lt);
        changedParasMap.put(changedParas.get(i), sentenceErrors);
        if (debugMode > 1) {
          MessageHandler.printToLogFile("Mark errors: Paragraph: " + changedParas.get(i) + "; Number of sentence: " + sentenceErrors.size()
            + "; Number of errors: " + sentenceErrors.get(0).sentenceErrors.length);
        }
      }
      flatPara.markParagraphs(changedParasMap, docCache, override, cursor);
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
   * Return true if one of the text level caches is not null
   */
  private boolean textLevelCacheNotEmpty(int paraNum) {
    for (int i = 0; i < minToCheckPara.size(); i++) {
      if(paragraphsCache.get(i).getCacheEntry(paraNum) != null) {
        return true;
      }
    }
    return false;
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
      int startSentencePos, SwJLanguageTool lt, boolean textIsChanged, boolean isIntern) {
    List<SingleProofreadingError[]> pErrors = new ArrayList<>();

    int nTParas = paraNum < 0 ? -1 : docCache.getNumberOfTextParagraph(paraNum);
    if (nTParas < 0) {
      pErrors.add(checkParaRules(paraText, footnotePos, paraNum, startSentencePos, lt, 0, 0, textIsChanged, isIntern));
    } else {
      //  Real full text check / numParas < 0
      ResultCache oldCache = null;
      List<Integer> tmpChangedParas;
      for (int i = 0; i < minToCheckPara.size(); i++) {
        int parasToCheck = minToCheckPara.get(i);
        if (i == 0 || mDocHandler.isSortedRuleForIndex(i)) {
          mDocHandler.activateTextRulesByIndex(i, lt);
          if (debugMode > 1) {
            MessageHandler.printToLogFile("ParaCeck: Index: " + i + "/" + minToCheckPara.size() 
              + "; numParasToCheck: " + numParasToCheck + OfficeTools.LOG_LINE_BREAK);
          }
          if (textIsChanged && !useQueue && parasToCheck != 0 ) {
            oldCache = new ResultCache(paragraphsCache.get(i));
/*
            oldCache = paragraphsCache.get(i);
            if (parasToCheck < -1) {
              paragraphsCache.set(i, new ResultCache());
            } else {
              paragraphsCache.set(i, new ResultCache(oldCache));
            }
*/
          }
          pErrors.add(checkParaRules(paraText, footnotePos, paraNum, startSentencePos, lt, i, parasToCheck, textIsChanged, isIntern));
          if (textIsChanged && !useQueue) {
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
  private SingleProofreadingError[] checkParaRules( String paraText, int[] footnotePos, int nFPara, int sentencePos, 
          SwJLanguageTool lt, int cacheNum, int parasToCheck, boolean textIsChanged, boolean isIntern) {

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
      if (nFPara >= 0 && (pErrors != null || (useQueue && !isDialogRequest && parasToCheck != 0))) {
        if (useQueue && pErrors == null && parasToCheck != 0 && nPara >= 0 && !textIsChanged) {
          singleDocument.addQueueEntry(nFPara, cacheNum, parasToCheck, singleDocument.getDocID(), textIsChanged, textIsChanged);
        }
        return pErrors;
      }
      
      //  One paragraph check (set by options or proof of footnote, etc.)
      if (nPara < 0 || parasToCheck == 0) {
        List<Integer> nextSentencePositions = getNextSentencePositions(paraText, lt);
        paragraphMatches = lt.check(removeFootnotes(paraText, footnotePos), true, JLanguageTool.ParagraphHandling.NORMAL);
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
    if (numParasToCheck != 0 || lt.isRemote()) {
      nextSentencePositions.add(paraText.length());
    } else {
      List<String> tokenizedSentences = lt.sentenceTokenize(cleanFootnotes(paraText));
      int position = 0;
      for (String sentence : tokenizedSentences) {
        position += sentence.length();
        nextSentencePositions.add(position);
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
  private static SingleProofreadingError correctRuleMatchWithFootnotes(SingleProofreadingError pError, int footnotesBefore, int[] footnotes) {
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
   * get all errors of a Paragraph as list
   */
  private List<SentenceErrors> getSentenceErrosAsList(int numberOfParagraph, SwJLanguageTool lt) {
    List<SentenceErrors> sentenceErrors = new ArrayList<SentenceErrors>();
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
      nextSentencePositions =  getNextSentencePositions (docCache.getFlatParagraph(numberOfParagraph), lt);
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
