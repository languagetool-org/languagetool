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
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.gui.Configuration;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.text.TextMarkupType;
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
  private static final String END_OF_PARAGRAPH = "\n";  //  Paragraph Separator like in standalone GUI
  private static final String MANUAL_LINEBREAK = "\r";  //  to distinguish from paragraph separator
  private static final String ZERO_WIDTH_SPACE = "\u200B";  // Used to mark footnotes
  private static final String logLineBreak = System.getProperty("line.separator");  //  LineBreak in Log-File (MS-Windows compatible)
  private static final int PARA_CHECK_FACTOR = 10;  //  Factor for parameter checked at once at iteration (no text change)
  private static final int MAX_SUGGESTIONS = 15;


  private static int debugMode = 0;               //  should be 0 except for testing; 1 = low level; 2 = advanced level
  private static boolean specialOptimization = true;   //  special optimization switched on; TODO: test this version and delete switch, if it is OK
  
  private Configuration config;

  private int numParasToCheck = 5;                // will be overwritten by config
  private int defaultParaCheck = 10;              // will be overwritten by config
  private boolean doResetCheck = true;            // will be overwritten by config

  private XComponentContext xContext;             //  The context of the document
  private String docID;                           //  docID of the document
  private XComponent xComponent;                  //  XComponent of the open document

  private List<String> allParas = null;           //  List of paragraphs (only readable by parallel thread)
  private DocumentCursorTools docCursor = null;   //  Save Cursor for the single documents
//  private FlatParagraphTools flatPara = null;   //  Save FlatParagraph for the single documents
  private Integer numLastVCPara = 0;              //  Save position of ViewCursor for the single documents
  private Integer numLastFlPara = 0;              //  Save position of FlatParagraph for the single documents
  private boolean textIsChanged = false;          //  false: check number of paragraphs again (ignored by parallel thread)
  private boolean resetCheck = false;             //  true: the whole text has to be checked again (use cache)
  private int divNum;                             //  difference between number of paragraphs from cursor and from flatParagraph (unchanged by parallel thread)
  private ResultCache sentencesCache;             //  Cache for matches of sentences rules
  private ResultCache paragraphsCache;            //  Cache for matches of text rules
  private ResultCache singleParaCache;            //  Cache for matches of text rules for single paragraphs
  private boolean firstCheckDone = false;         //  Is set to true, if first iteration of whole document is done
  private int resetFrom = 0;                      //  Reset from paragraph
  private int resetTo = 0;                        //  Reset to paragraph
  
  SingleDocument(XComponentContext xContext, Configuration config, String docID, XComponent xComponent) {
    this.xContext = xContext;
    this.config = config;
    this.docID = docID;
    this.xComponent = xComponent;
    this.sentencesCache = new ResultCache();
    this.paragraphsCache = new ResultCache();
    this.singleParaCache = new ResultCache();
  }
  
  /**  get the result for a check of a single document 
   * 
   * @param paraText          paragraph text
   * @param paRes             proof reading result
   * @param footnotePositions position of footnotes
   * @param isParallelThread  true: check runs as parallel thread
   * @return                  proof reading result
   */
  ProofreadingResult getCheckResults(String paraText, Locale locale, ProofreadingResult paRes, 
      int[] footnotePositions, boolean isParallelThread, JLanguageTool langTool) {
    try {
      SingleProofreadingError[] sErrors = null;
      int paraNum = getParaPos(paraText, isParallelThread);
      // Don't use Cache for check in single paragraph mode
      if(numParasToCheck != 0 && paraNum >= 0 && doResetCheck) {
        sErrors = sentencesCache.getMatches(paraNum, paRes.nStartOfSentencePosition);
        // return Cache result if available
        if(sErrors != null) {
          paRes.nStartOfNextSentencePosition = sentencesCache.getNextSentencePosition(paraNum, paRes.nStartOfSentencePosition);
          paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
        }
      }
      if (debugMode > 1) {
        MessageHandler.printToLogFile("... Check Sentence: numCurPara: " + paraNum 
            + "; startPos: " + paRes.nStartOfSentencePosition + "; Paragraph: " + paraText 
            + ", sErrors: " + (sErrors == null ? 0 : sErrors.length) + logLineBreak);
      }
      if(sErrors == null) {
        SentenceFromPara sfp = new SentenceFromPara(paraText, paRes.nStartOfSentencePosition, langTool);
        String sentence = sfp.getSentence();
        paRes.nStartOfSentencePosition = sfp.getPosition();
        paRes.nStartOfNextSentencePosition = sfp.getPosition() + sentence.length();
        paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
        sErrors = checkSentence(sentence, paRes.nStartOfSentencePosition, paRes.nStartOfNextSentencePosition, 
            paraNum, footnotePositions, isParallelThread, langTool);
        if(specialOptimization) {
          setFirstCheckDone();
        }
      }
      SingleProofreadingError[] pErrors = checkParaRules(paraText, paraNum, paRes.nStartOfSentencePosition,
          paRes.nStartOfNextSentencePosition, isParallelThread, langTool);
      paRes.aErrors = mergeErrors(sErrors, pErrors);
      if (debugMode > 1) {
        MessageHandler.printToLogFile("paRes.aErrors.length: " + paRes.aErrors.length + "; docID: " + docID + logLineBreak);
      }

      textIsChanged = false;
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
    numParasToCheck = config.getNumParasToCheck();
    defaultParaCheck = numParasToCheck * PARA_CHECK_FACTOR;
    doResetCheck = config.isResetCheck();
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
  
  /** Reset all caches of the document
   */
  void resetCache() {
    sentencesCache.removeAll();
    paragraphsCache.removeAll();
    singleParaCache.removeAll();
    firstCheckDone = false;
  }
  
  /** 
   * Do a reset to check document again
   */
  boolean doresetCheck() {
    if(!doResetCheck) {
      return false;
    }
    return resetCheck;
  }
  
  /** 
   * Reset only changed paragraphs
   */
  void optimizeReset() {
    if(firstCheckDone) {
      FlatParagraphTools flatPara = new FlatParagraphTools(xContext);
      flatPara.markFlatParasAsChecked(resetFrom, resetTo);
    }
  }
  
  // Fix numbers that are (probably) foot notes.
  // See https://bugs.freedesktop.org/show_bug.cgi?id=69416
  // public for test reasons
  String cleanFootnotes(String paraText) {
    return paraText.replaceAll("([^\\d][.!?])\\d ", "$1¹ ");
  }
  
  /**
   * Search for Position of Paragraph
   * gives Back the Position in full text / -1 if Paragraph can not be found
   */
  private int getParaPos(String chPara, boolean isParallelThread) {

    if (numParasToCheck == 0 || xComponent == null) {
      return -1;  //  check only the processed paragraph
    }

    if (docCursor == null) {
      docCursor = new DocumentCursorTools(xContext);
    }
    FlatParagraphTools flatPara = null;

    int nParas;
    boolean isReset = false;
    textIsChanged = false;
    resetCheck = false;

    if (allParas == null || allParas.size() < 1) {
      if (isParallelThread) {              //  if numThread > 0: Thread may only read allParas
        return -1;
      }
      flatPara = new FlatParagraphTools(xContext);
      if (!resetAllParas(docCursor, flatPara)) {
        return -1;
      }
      if (debugMode > 0) {
        MessageHandler.printToLogFile("+++ resetAllParas (allParas == null): allParas.size: " + allParas.size()
                + ", docID: " + docID + logLineBreak);
      }
      isReset = true;
    }

    // Test if Size of allParas is correct; Reset if not
    nParas = docCursor.getNumberOfAllTextParagraphs();
    if (nParas < 2) {
      return -1;
    } else if (allParas.size() != nParas) {
      if (isParallelThread) {
        return -1;
      }
      if (debugMode > 0) {
        MessageHandler.printToLogFile("*** resetAllParas: allParas.size: " + allParas.size() + ", nParas: " + nParas
                + ", docID: " + docID + logLineBreak);
      }
      List<String> oldParas = allParas;
      if (flatPara == null) {
        flatPara = new FlatParagraphTools(xContext);
      }
      if (!resetAllParas(docCursor, flatPara)) {
        return -1;
      }
      int from = 0;
      while (from < allParas.size() && from < oldParas.size()
          && allParas.get(from).equals(oldParas.get(from))) {
        from++;
      }
      from -= 1 + numParasToCheck;
      resetFrom = from;
      int to = 1;
      while (to <= allParas.size() && to <= oldParas.size()
          && allParas.get(allParas.size() - to).equals(
              oldParas.get(oldParas.size() - to))) {
        to++;
      }
      to = allParas.size() + numParasToCheck - to;
      resetTo = to;
      paragraphsCache.removeAndShift(from, to, allParas.size() - oldParas.size());
      isReset = true;
      resetCheck = true;
      if(doResetCheck) {
        from += numParasToCheck;
        to -= numParasToCheck;
        sentencesCache.removeAndShift(from, to, allParas.size() - oldParas.size());
      }
    }

    // try to get next position from last ViewCursorPosition (proof per dialog box)
    int numLastPara;
    numLastPara = numLastVCPara;
    nParas = findNextParaPos(numLastPara, chPara);
    if (nParas >= 0) {
      numLastVCPara = nParas;
      return nParas;
    }

    // try to get next position from last Position of FlatParagraph (automatic Iteration without Text change)
    numLastPara = numLastFlPara;
    nParas = findNextParaPos(numLastPara, chPara);
    if (nParas >= 0) {
      numLastFlPara = nParas;
      return nParas;
    }

    // try to get ViewCursor position (proof initiated by mouse click)
    nParas = docCursor.getViewCursorParagraph();
    if (nParas >= 0 && nParas < allParas.size() && chPara.equals(allParas.get(nParas))) {
      numLastVCPara = nParas;
      return nParas;
    }

    //  try to get paragraph position from automatic iteration
    if (flatPara == null) {
      flatPara = new FlatParagraphTools(xContext);
    }
    nParas = flatPara.getNumberOfAllFlatPara();

    if (debugMode > 0) {
      MessageHandler.printToLogFile("numLastFlPara: " + numLastFlPara + " (isParallelThread: " + isParallelThread 
          + "); nParas: " + nParas + "; docID: " + docID + logLineBreak);
    }

    if (nParas < allParas.size()) {
      return -1;   //  no automatic iteration
    }
    divNum = nParas - allParas.size();

    nParas = flatPara.getCurNumFlatParagraphs();

    if (nParas < divNum) {
      return -1; //  Proof footnote etc.
    }

    nParas -= divNum;
    numLastFlPara = nParas;
    
    if (!chPara.equals(allParas.get(nParas))) {
      if (isReset || isParallelThread) {
        return -1;
      } else {
        if (debugMode > 0) {
          MessageHandler.printToLogFile("!!! allParas set: NParas: " + nParas + "; divNum: " + divNum
                  + "; docID: " + docID
                  + logLineBreak + "old: " + allParas.get(nParas) + logLineBreak 
                  + "new: " + chPara + logLineBreak);
        }
        allParas.set(nParas, chPara);
        paragraphsCache.remove(nParas);
        if(doResetCheck) {
          sentencesCache.remove(nParas);
        }
        resetFrom = nParas - numParasToCheck;
        resetTo = nParas + numParasToCheck + 1;
        textIsChanged = true;
        resetCheck = true;
        return nParas;
      }
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("nParas from FlatParagraph: " + nParas);
    }
    return nParas;
  }

  /**
   * Reset allParas
   */
  private boolean resetAllParas(DocumentCursorTools docCursor, FlatParagraphTools flatPara) {
    allParas = docCursor.getAllTextParagraphs();
    if (allParas == null || allParas.size() < 1) {
      return false;
    }
    //  change all footnotes to \u200B (like in paraText)
    //  List of footnotes
    List<int[]> footnotes = flatPara.getFootnotePositions();
    divNum = footnotes.size() - allParas.size();
    if (divNum >= 0) {
      for (int i = 0; i < allParas.size(); i++) {
        for (int pos : footnotes.get(i + divNum)) {
          if(pos <= allParas.get(i).length()) {
            String paraText = allParas.get(i).substring(0, pos) + ZERO_WIDTH_SPACE;
            if (pos < allParas.get(i).length() - 1) {
              paraText += allParas.get(i).substring(pos + 1);
            }
            allParas.set(i, paraText);
          }
        }
      }
    }
    return true;
  }
  
  /**
   * Heuristic try to find next position (dialog box or automatic iteration)
   * Is paragraph same, next not empty after or before   
   */
  private int findNextParaPos(int startPara, String paraStr) {
    if (allParas == null || allParas.size() < 1) {
      return -1;
    }
    if (startPara >= allParas.size() || startPara < 0) {
      startPara = 0;
    }
    if (startPara + 1 < allParas.size() && paraStr.equals(allParas.get(startPara + 1))) {
      return startPara + 1;
    }
    if (paraStr.equals(allParas.get(startPara))) {
      return startPara;
    }
    if (startPara - 1 >= 0 && paraStr.equals(allParas.get(startPara - 1))) {
      return startPara - 1;
    }
    return -1;
  }

  /**
   * Change manual linebreak to distinguish from end of paragraph
   */
  private static String fixLinebreak (String text) {
    return text.replaceAll(END_OF_PARAGRAPH, MANUAL_LINEBREAK);
  }

  /**
   * Gives Back the full Text as String
   */
  private String getDocAsString(int numCurPara) {
    if (numCurPara < 0 || allParas == null || allParas.size() < numCurPara - 1) {
      return "";
    }
    int startPos;
    int endPos;
    if (numParasToCheck < 1) {
      startPos = 0;
      endPos = allParas.size();
    } else {
      startPos = numCurPara - numParasToCheck;
      if(textIsChanged && doResetCheck) {
        startPos -= numParasToCheck;
      }
      if (startPos < 0) {
        startPos = 0;
      }
      endPos = numCurPara + 1 + numParasToCheck;
      if(!textIsChanged) {
        endPos += defaultParaCheck;
      } else if(doResetCheck) {
        endPos += numParasToCheck;
      }
      if (endPos > allParas.size()) {
        endPos = allParas.size();
      }
    }
    StringBuilder docText = new StringBuilder(fixLinebreak(allParas.get(startPos)));
    for (int i = startPos + 1; i < endPos; i++) {
      docText.append(END_OF_PARAGRAPH).append(fixLinebreak(allParas.get(i)));
    }
    return docText.toString();
  }

  /**
   * Gives Back the StartPosition of Paragraph
   */
  private int getStartOfParagraph(int nPara, int checkedPara) {
    if (allParas != null && nPara >= 0 && nPara < allParas.size()) {
      int startPos;
      if (numParasToCheck < 1) {
        startPos = 0;
      } else {
        startPos = checkedPara - numParasToCheck;
        if(textIsChanged && doResetCheck) {
          startPos -= numParasToCheck;
        }
        if (startPos < 0) startPos = 0;
      }
      int pos = 0;
      for (int i = startPos; i < nPara; i++) {
        pos += allParas.get(i).length() + 1;
      }
      return pos;
    }
    return -1;
  }

  private AnnotatedText getAnnotatedText(String text, int[] footnotePos, int startPosition) {
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
          annotations.addMarkup(ZERO_WIDTH_SPACE);
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

  private SingleProofreadingError[] mergeErrors(SingleProofreadingError[] sErrors, SingleProofreadingError[] pErrors) {
    int errorCount = 0;
    if (sErrors != null) {
      errorCount += sErrors.length;
    }
    if (pErrors != null) {
      errorCount += pErrors.length;
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
      arraycopy(pErrors, 0, errorArray, sErrorCount, pErrors.length);
    }
    Arrays.sort(errorArray, new ErrorPositionComparator());
    return errorArray;
  }

  @Nullable
  private SingleProofreadingError[] checkParaRules( String paraText, int paraNum, 
      int startSentencePos, int endSentencePos, boolean isParallelThread, JLanguageTool langTool) {

    List<RuleMatch> paragraphMatches;
    SingleProofreadingError[] pErrors = null;
    try {
      
      // use Cache for check in single paragraph mode only after the first call of paragraph
      if(numParasToCheck != 0 && paraNum >= 0) {
        pErrors = paragraphsCache.getFromPara(paraNum, startSentencePos, endSentencePos);
      } else if (startSentencePos > 0) {
        pErrors = singleParaCache.getFromPara(0, startSentencePos, endSentencePos);
        
      }
      if(pErrors != null || isParallelThread) {   // return Cache result if available
        return pErrors;                           // for parallel Thread only use cache
      }
      
      String textToCheck;
      if(paraNum < 0 || numParasToCheck == 0) {
        textToCheck = fixLinebreak(paraText);
      } else {
        textToCheck = getDocAsString(paraNum);
      }
      paragraphMatches = langTool.check(textToCheck, true, JLanguageTool.ParagraphHandling.ONLYPARA);
      
      //  One paragraph check (set by options or proof of footnote, etc.)
      if (numParasToCheck == 0 || paraNum < 0) {
        if(paragraphMatches == null || paragraphMatches.isEmpty()) {
          singleParaCache.put(0, new SingleProofreadingError[0]);
        } else {
          List<SingleProofreadingError> errorList = new ArrayList<>();
          for (RuleMatch myRuleMatch : paragraphMatches) {
            int toPos = myRuleMatch.getToPos();
            if(toPos > textToCheck.length()) {
              toPos = textToCheck.length();
            }
            errorList.add(createOOoError(myRuleMatch, 0, toPos, paraText.charAt(toPos-1)));
          }
          if (!errorList.isEmpty()) {
            singleParaCache.put(0, errorList.toArray(new SingleProofreadingError[0]));
          } else {
            singleParaCache.put(0, new SingleProofreadingError[0]);
          }
        }  
        return singleParaCache.getFromPara(0, startSentencePos, endSentencePos);
      }

      //  check of numParasToCheck or full text 
      int startPara;
      int endPara;
      if(numParasToCheck < 0) {
        startPara = 0;
        endPara = allParas.size();
      } else {
        startPara = paraNum;
        if(textIsChanged && doResetCheck) {
          startPara -= numParasToCheck;
        }
        if(startPara < 0) {
          startPara = 0;
        }
        endPara= paraNum + 1;
        if(textIsChanged && doResetCheck) {
          endPara += numParasToCheck;
        } else if(!textIsChanged){
          endPara += defaultParaCheck;
        }
        if(endPara > allParas.size()) {
          endPara = allParas.size();
        }
      }
      int startPos = getStartOfParagraph(startPara, paraNum);
      int endPos;
      for (int i = startPara; i < endPara; i++) {
        if(i < endPara - 1) {
          endPos = getStartOfParagraph(i + 1, paraNum);
        } else {
          endPos = textToCheck.length();
        }
        if(paragraphMatches == null || paragraphMatches.isEmpty()) {
          paragraphsCache.put(i, new SingleProofreadingError[0]);
        } else {
          List<SingleProofreadingError> errorList = new ArrayList<>();
          int textPos = startPos;
          if (textPos < 0) textPos = 0;
          for (RuleMatch myRuleMatch : paragraphMatches) {
            int startErrPos = myRuleMatch.getFromPos();
            if (startErrPos >= startPos && startErrPos < endPos) {
              int toPos = allParas.get(i).length();
              if(toPos > 0) {
                errorList.add(createOOoError(myRuleMatch, -textPos, toPos, allParas.get(i).charAt(toPos-1)));
              }
            }
          }
          if (!errorList.isEmpty()) {
            paragraphsCache.put(i, errorList.toArray(new SingleProofreadingError[0]));
            if (debugMode > 1) {
              MessageHandler.printToLogFile("--> Enter to para cache: Paragraph: " + allParas.get(i) + "; Error number: " + errorList.size() + logLineBreak);
            }
          } else {
            paragraphsCache.put(i, new SingleProofreadingError[0]);
            if (debugMode > 1) {
              MessageHandler.printToLogFile("--> Enter to para cache: Paragraph: " + allParas.get(i) + "; Error number: 0" + logLineBreak);
            }
          }
        }
        startPos = endPos;
      }
      return paragraphsCache.getFromPara(paraNum, startSentencePos, endSentencePos);
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return null;
  }
  
  private SingleProofreadingError[] checkSentence(String sentence, int startPos, int nextPos, 
      int numCurPara, int[] footnotePositions, boolean isParallelThread, JLanguageTool langTool) {
    try {
      SingleProofreadingError[] errorArray;
      if (StringTools.isEmpty(sentence)) {
        errorArray = new SingleProofreadingError[0];
      } else {
        AnnotatedText annotatedText = getAnnotatedText(sentence, footnotePositions, startPos);
        List<RuleMatch> ruleMatches = langTool.check(annotatedText, false, JLanguageTool.ParagraphHandling.ONLYNONPARA);
        if (!ruleMatches.isEmpty()) {
          errorArray = new SingleProofreadingError[ruleMatches.size()];
          int i = 0;
          for (RuleMatch myRuleMatch : ruleMatches) {
            errorArray[i] = createOOoError(myRuleMatch, startPos,
                                          sentence.length(), sentence.charAt(sentence.length()-1));
            i++;
          }
        } else {
          errorArray = new SingleProofreadingError[0];
        }
      }
      if(!isParallelThread && numParasToCheck != 0 && doResetCheck) {
        if (debugMode > 1) {
          MessageHandler.printToLogFile("--> Enter to sentences cache: numCurPara: " + numCurPara 
              + "; startPos: " + startPos + "; Sentence: " + sentence 
              + "; Error number: " + errorArray.length + logLineBreak);
        }
        sentencesCache.remove(numCurPara, startPos);
        sentencesCache.add(numCurPara, startPos, nextPos, errorArray);
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
    aError.aFullComment = ruleMatch.getMessage()
        .replaceAll("<suggestion>", "\"").replaceAll("</suggestion>", "\"")
        .replaceAll("([\r]*\n)", " ");
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
    if (lastChar == '.' && (ruleMatch.getToPos() + startIndex) == sentencesLength) {
      int i = 0;
      while (i < numSuggestions && i < MAX_SUGGESTIONS
          && allSuggestions[i].length() > 0 && allSuggestions[i].charAt(allSuggestions[i].length()-1) == '.') {
        i++;
      }
      if (i < numSuggestions && i < MAX_SUGGESTIONS) {
      numSuggestions = 0;
      allSuggestions = new String[0];
      }
    }
    //  End of Filter
    if (numSuggestions > MAX_SUGGESTIONS) {
      aError.aSuggestions = Arrays.copyOfRange(allSuggestions, 0, MAX_SUGGESTIONS);
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
    URL url = ruleMatch.getUrl();
    if (url == null) {                      // match URL overrides rule URL 
      url = ruleMatch.getRule().getUrl();
    }
    if(underlineColor != Color.blue) {
      int ucolor = underlineColor.getRGB() & 0xFFFFFF;
      if (url != null) {
        aError.aProperties = new PropertyValue[] { new PropertyValue(
            "FullCommentURL", -1, url.toString(), PropertyState.DIRECT_VALUE),
            new PropertyValue("LineColor", -1, ucolor, PropertyState.DIRECT_VALUE) };
      } else {
        aError.aProperties = new PropertyValue[] {
            new PropertyValue("LineColor", -1, ucolor, PropertyState.DIRECT_VALUE) };
      }
    } else {
      if (url != null) {
        aError.aProperties = new PropertyValue[] { new PropertyValue(
            "FullCommentURL", -1, url.toString(), PropertyState.DIRECT_VALUE) };
      } else {
        aError.aProperties = new PropertyValue[0];
      }
    }
    return aError;
  }

  private class SentenceFromPara {
    private int position;
    private String str;

    SentenceFromPara(String paraText, int startPos, JLanguageTool langTool) {
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

  private void setFirstCheckDone() {
    if (!firstCheckDone && allParas != null) {
      int numParas = sentencesCache.getNumberOfParas();
      if (debugMode > 1) {
        MessageHandler.printToLogFile("firstCheckDone --> sentenceCache: " + numParas 
            + "; allParas: " + allParas.size() + "; docID: " + docID + logLineBreak
            + "sentenceCache entries: " + sentencesCache.getNumberOfEntries()
            + "; paragraphsCache entries: " + paragraphsCache.getNumberOfEntries()
            + "; singleParaCache entries: " + singleParaCache.getNumberOfEntries());
      }
      if(numParas == allParas.size()) {
        firstCheckDone = true;
        if (debugMode > 0) {
          MessageHandler.printToLogFile(">>> firstCheckDone: true; docID: " + docID + logLineBreak);
        }
      }
    }
  }

}
