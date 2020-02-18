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
import java.util.ResourceBundle;

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.gui.Configuration;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.openoffice.TextLevelCheckQueue.QueueEntry;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexContainer;
import com.sun.star.frame.XController;
import com.sun.star.lang.Locale;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.text.TextMarkupType;
import com.sun.star.text.XTextDocument;
import com.sun.star.ui.ActionTriggerSeparatorType;
import com.sun.star.ui.ContextMenuExecuteEvent;
import com.sun.star.ui.ContextMenuInterceptorAction;
import com.sun.star.ui.XContextMenuInterception;
import com.sun.star.ui.XContextMenuInterceptor;
import com.sun.star.uno.Any;
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
  public static final String END_OF_PARAGRAPH = "\n\n";  //  Paragraph Separator like in standalone GUI
  public static final int NUMBER_PARAGRAPH_CHARS = END_OF_PARAGRAPH.length();  //  number of end of paragraph characters

  private static final ResourceBundle MESSAGES = JLanguageTool.getMessageBundle();
  private static final String SINGLE_END_OF_PARAGRAPH = "\n";
  private static final String MANUAL_LINEBREAK = "\r";  //  to distinguish from paragraph separator
  private static final String ZERO_WIDTH_SPACE = "\u200B";  // Used to mark footnotes
  private static final String logLineBreak = System.getProperty("line.separator");  //  LineBreak in Log-File (MS-Windows compatible)
  private static final int PARA_CHECK_DEFAULT = 50;  //  Factor for parameter checked at once at iteration (no text change)
  private static final int MAX_SUGGESTIONS = 15;

  private static int debugMode = 0;               //  should be 0 except for testing; 1 = low level; 2 = advanced level
  
  private Configuration config;

  private int defaultParaCheck = 10;              // will be overwritten by config
  private boolean doFullCheckAtFirst = true;      // will be overwritten by config
  
  private int numParasToCheck = 0;                // current number of Paragraphs to be checked
  private boolean firstCheckIsDone = false;       // Is first check done?

  private XComponentContext xContext;             //  The context of the document
  private String docID;                           //  docID of the document
  private XComponent xComponent;                  //  XComponent of the open document
  private MultiDocumentsHandler mDocHandler;
  
  private List<String> allParas = null;           //  List of paragraphs (only readable by parallel thread)
  private DocumentCursorTools docCursor = null;   //  Save document cursor for the single document
  private FlatParagraphTools flatPara = null;     //  Save information for flat paragraphs (including iterator and iterator provider) for the single document
  private Integer numLastVCPara = 0;              //  Save position of ViewCursor for the single documents
  private Integer numLastFlPara = 0;              //  Save position of FlatParagraph for the single documents
  private boolean isMouseOrDialog = false;        //  true: check was initiated by right mouse click or proofreading dialog
  private boolean textIsChanged = false;          //  false: check number of paragraphs again (ignored by parallel thread)
  private boolean resetCheck = false;             //  true: the whole text has to be checked again (use cache)
  private int divNum;                             //  difference between number of paragraphs from cursor and from flatParagraph (unchanged by parallel thread)
  private ResultCache sentencesCache;             //  Cache for matches of sentences rules
  private List<ResultCache> paragraphsCache;      //  Cache for matches of text rules
  private ResultCache singleParaCache;            //  Cache for matches of text rules for single paragraphs
  private int resetFrom = 0;                      //  Reset from paragraph
  private int resetTo = 0;                        //  Reset to paragraph
  private int numParasReset = 1;                  //  Number of paragraphs to reset
  private List<Integer> changedParas = null;      //  List of changed paragraphs after editing the document
  private int paraNum;                            //  Number of current checked paragraph
  private List<Integer> minToCheckPara;                   //  List of minimal to check paragraphs for different classes of text level rules
  private Map<Integer, List<Integer>> ignoredMatches;     //  Map of matches (number of paragraph, number of character) that should be ignored after ignoreOnce was called
  private boolean isRemote;                               //  true: Check is done by remote server
  private boolean useQueue = true;                        //  true: use queue to check text level rules (will be overridden by config
  private List<Integer> headings;                         //  stores the paragraphs formated as headings; is used to subdivide the document in chapters
  private String lastSinglePara = null;                   //  stores the last paragraph which is checked as single paragraph

  @SuppressWarnings("unused") 
  private ContextMenuInterceptor contextMenuInterceptor = null;
  
  SingleDocument(XComponentContext xContext, Configuration config, String docID, 
      XComponent xComponent, MultiDocumentsHandler mDH) {
    this.xContext = xContext;
    this.config = config;
    this.docID = docID;
    this.xComponent = xComponent;
    this.mDocHandler = mDH;
    this.sentencesCache = new ResultCache();
    this.singleParaCache = new ResultCache();
    if (config != null) {
      setConfigValues(config);
    }
    resetCache();
    ignoredMatches = new HashMap<>();
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
      int[] footnotePositions, boolean docReset, SwJLanguageTool langTool) {
    isRemote = langTool.isRemote();
    try {
      if(docReset) {
        numLastVCPara = 0;
        ignoredMatches = new HashMap<>();
      }
      SingleProofreadingError[] sErrors = null;
      paraNum = getParaPos(paraText, paRes.nStartOfSentencePosition);
      // Don't use Cache for check in single paragraph mode
      if(numParasToCheck != 0 && paraNum >= 0) {
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
      String text = null;
      if(sErrors == null) {
        if(!langTool.isRemote()) {
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
          paRes.nStartOfNextSentencePosition, langTool);

      if(sErrors == null) {
        sErrors = checkSentence(text, paRes.nStartOfSentencePosition, paRes.nStartOfNextSentencePosition, 
            paraNum, footnotePositions, langTool);
      }
      
      paRes.aErrors = mergeErrors(sErrors, pErrors);
      textIsChanged = false;
      if (debugMode > 1) {
        MessageHandler.printToLogFile("paRes.aErrors.length: " + paRes.aErrors.length + "; docID: " + docID + logLineBreak);
      }
      if(resetCheck) {
        if(!useQueue) {
          remarkChangedParagraphs(changedParas);
        }
        resetCheck = false;
      }
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
    defaultParaCheck = PARA_CHECK_DEFAULT;
    useQueue = config.useTextLevelQueue();
    if(useQueue || numParasToCheck == 0) {
      doFullCheckAtFirst = false;
    } else {
      doFullCheckAtFirst = config.doFullCheckAtFirst();
    }
    changedParas = null;
    firstCheckIsDone = false;
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
    singleParaCache.removeAll();
    paragraphsCache = new ArrayList<>();
    numParasReset = numParasToCheck;
    if((doFullCheckAtFirst || numParasToCheck < 0 || useQueue) && mDocHandler != null) {
      minToCheckPara = mDocHandler.getNumMinToCheckParas();
      for(int i = 0; i < minToCheckPara.size(); i++) {
        paragraphsCache.add(new ResultCache());
      }
      if(numParasReset < 0) {
        for(int minPara : minToCheckPara) {
          if(minPara > numParasReset) {
            numParasReset = minPara;
          }
        }
      }
    } else {
      paragraphsCache.add(new ResultCache());
    }
  }
  
  /**
   * remark changed paragraphs
   * override existing marks
   */
  private void remarkChangedParagraphs(List<Integer> changedParas) {
    Map <Integer, SingleProofreadingError[]> changedParasMap = new HashMap<>();
    for (int nPara : changedParas) {
      List<SingleProofreadingError[]> pErrors = new ArrayList<SingleProofreadingError[]>();
      for(int i = 0; i < minToCheckPara.size(); i++) {
        pErrors.add(paragraphsCache.get(i).getMatches(nPara));
      }
      SingleProofreadingError[] sErrors = sentencesCache.getMatches(nPara);
      changedParasMap.put(nPara, mergeErrors(sErrors, pErrors));
    }
    flatPara.markParagraphs(changedParasMap, divNum, true);
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
  private int getParaPos(String chPara, int startPos) {

    if (numParasToCheck == 0 || xComponent == null) {
      return -1;  //  check only the processed paragraph
    }

    if (contextMenuInterceptor == null) {
      contextMenuInterceptor = new ContextMenuInterceptor(xContext);
    }

    int nParas;
    boolean isReset = false;
    textIsChanged = false;
    isMouseOrDialog = false;

    if (allParas == null || allParas.isEmpty()) {
      docCursor = new DocumentCursorTools(xComponent);
      flatPara = new FlatParagraphTools(xComponent);
      if (!resetAllParas(docCursor, flatPara)) {
        return -1;
      }
      if (debugMode > 0) {
        MessageHandler.printToLogFile("+++ resetAllParas (allParas == null): allParas.size: " + allParas.size()
                + ", docID: " + docID + logLineBreak);
      }
      isReset = true;
    }
    
    // try to get next position from last FlatParagraph position (for performance reasons)
    nParas = findNextParaPos(numLastFlPara, chPara, startPos);
    if (nParas >= 0) {
      numLastFlPara = nParas;
      if (debugMode > 0) {
        MessageHandler.printToLogFile("From last FlatPragraph Position: Number of Paragraph: " + nParas + logLineBreak);
      }
      return nParas;
    }
    // Test if Size of allParas is correct; Reset if not
    if (docCursor == null) {
      docCursor = new DocumentCursorTools(xComponent);
    }
    nParas = docCursor.getNumberOfAllTextParagraphs();
    if (nParas < 2) {
      return -1;
    } else if (allParas.size() != nParas) {
      if (debugMode > 0) {
        MessageHandler.printToLogFile("*** resetAllParas: allParas.size: " + allParas.size() + ", nParas: " + nParas
                + ", docID: " + docID + logLineBreak);
      }
      List<String> oldParas = allParas;
      if (flatPara == null) {
        flatPara = new FlatParagraphTools(xComponent);
      }
      if (!resetAllParas(docCursor, flatPara)) {
        return -1;
      }
      int from = 0;
      while (from < allParas.size() && from < oldParas.size()
          && allParas.get(from).equals(oldParas.get(from))) {
        from++;
      }
      resetFrom = from - numParasReset;
      int to = 1;
      while (to <= allParas.size() && to <= oldParas.size()
          && allParas.get(allParas.size() - to).equals(
              oldParas.get(oldParas.size() - to))) {
        to++;
      }
      to = allParas.size() - to;
      resetTo = to + numParasReset;
      if(!ignoredMatches.isEmpty()) {
        Map<Integer, List<Integer>> tmpIgnoredMatches = new HashMap<>();
        for (int i = 0; i < from; i++) {
          if(ignoredMatches.containsKey(i)) {
            tmpIgnoredMatches.put(i, ignoredMatches.get(i));
          }
        }
        for (int i = to + 1; i < oldParas.size(); i++) {
          int n = i + allParas.size() - oldParas.size();
          if(ignoredMatches.containsKey(i)) {
            tmpIgnoredMatches.put(n, ignoredMatches.get(i));
          }
        }
        ignoredMatches = tmpIgnoredMatches;
      }
      for(ResultCache cache : paragraphsCache) {
        cache.removeAndShift(resetFrom, resetTo, allParas.size() - oldParas.size());
      }
      resetTo++;
      isReset = true;
      sentencesCache.removeAndShift(from, to, allParas.size() - oldParas.size());
      resetCheck = true;
      textIsChanged = true;
      if(useQueue) {
        for (int i = 0; i < minToCheckPara.size(); i++) {
          if(minToCheckPara.get(i) != 0) {
            for (int n = from; n <= to; n++) {
              addQueueEntry(n, i, minToCheckPara.get(i), docID);
            }
          }
        }
      }
    }
    //  try to get paragraph position from automatic iteration
    if (flatPara == null) {
      flatPara = new FlatParagraphTools(xComponent);
    }
    nParas = flatPara.getNumberOfAllFlatPara();

    if (debugMode > 0) {
      MessageHandler.printToLogFile("Number FlatParagraphs: " + nParas + "; docID: " + docID);
    }

    if (nParas < allParas.size()) {   //  no automatic iteration
      return getParaFromViewCursorOrDialog(chPara);   // try to get ViewCursor position
    }
    divNum = nParas - allParas.size();

    nParas = flatPara.getCurNumFlatParagraph();

    if (nParas < divNum || nParas >= divNum + allParas.size()) {
      return -1; //  nParas < divNum: Proof footnote etc.  /  nParas >= allParas.size():  document was changed while checking
    }

    nParas -= divNum;
    numLastFlPara = nParas;
    
    if (!chPara.equals(allParas.get(nParas))) {
      int nVParas = getParaFromViewCursorOrDialog(chPara);   // try to get ViewCursor position
      if (nVParas >= 0) {
        return nVParas;
      }
      if (isReset) {
        return -1;
      } else {
        if (debugMode > 0) {
          MessageHandler.printToLogFile("!!! allParas set: NParas: " + nParas + "; divNum: " + divNum
                  + "; docID: " + docID
                  + logLineBreak + "old: " + allParas.get(nParas) + logLineBreak 
                  + "new: " + chPara + logLineBreak);
        }
        allParas.set(nParas, chPara);
        for(ResultCache cache : paragraphsCache) {
          cache.remove(nParas);
        }
        sentencesCache.remove(nParas);
        resetCheck = true;
        if(useQueue) {
          for (int i = 0; i < minToCheckPara.size(); i++) {
            if(minToCheckPara.get(i) != 0) {
              addQueueEntry(nParas, i, minToCheckPara.get(i), docID);
            }
          }
        }
        if(!textIsChanged) {
          resetFrom = nParas - numParasReset;
          resetTo = nParas + numParasReset + 1;
          ignoredMatches.remove(nParas);
          textIsChanged = true;
        }
        return nParas;
      }
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("From FlatParagraph: Number of Paragraph: " + nParas + logLineBreak);
    }
    return nParas;
  }

  /** Get the number of paragraph from position of ViewCursor or from the last position (dialog)
   * @return number of paragraph or -1 if it fails
   */
  private int getParaFromViewCursorOrDialog(String chPara) {
    // try to get ViewCursor position (proof initiated by mouse click)
    if(xComponent != OfficeTools.getCurrentComponent(xContext) ) {
      return -1;
    }
    ViewCursorTools viewCursor = new ViewCursorTools(xContext);
    isMouseOrDialog = true;
    int nParas = viewCursor.getViewCursorParagraph();
    if (nParas >= 0 && nParas < allParas.size() && chPara.equals(allParas.get(nParas))) {
      numLastVCPara = nParas;
      if (debugMode > 0) {
        MessageHandler.printToLogFile("From View Cursor: Number of Paragraph: " + nParas + logLineBreak);
      }
      return nParas;
    }
    // try to get next position from last ViewCursor position (proof per dialog box)
    for(int i = numLastVCPara; i < allParas.size(); i++) {
      if (chPara.equals(allParas.get(i))) {
        numLastVCPara = i;
        if (debugMode > 0) {
          MessageHandler.printToLogFile("From Dialog: Number of Paragraph: " + i + logLineBreak);
        }
        return numLastVCPara;
      }
    }
    for(int i = 0; i < numLastVCPara; i++) {
      if (chPara.equals(allParas.get(i))) {
        numLastVCPara = i;
        if (debugMode > 0) {
          MessageHandler.printToLogFile("From Dialog: Number of Paragraph: " + i + logLineBreak);
        }
        return numLastVCPara;
      }
    }
    isMouseOrDialog = false;
    return -1;
  }
  
  /**
   * Reset allParas
   */
  private boolean resetAllParas(DocumentCursorTools docCursor, FlatParagraphTools flatPara) {
    allParas = docCursor.getAllTextParagraphs();
    if (allParas == null || allParas.size() < 1) {
      return false;
    }
    headings = docCursor.getParagraphHeadings();
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
   * Heuristic try to find next position (automatic iteration)
   * Is paragraph same, next not empty after or before   
   */
  private int findNextParaPos(int startPara, String paraStr, int startPos) {
    if (allParas == null || allParas.size() < 1) {
      return -1;
    }
    if (startPos > 0) {
      if (startPara >= 0 && startPara < allParas.size() && paraStr.equals(allParas.get(startPara))) {
        return startPara;
      }
    } else if (startPos == 0) {
      startPara = startPara >= allParas.size() ? 0 : startPara + 1;
      if (startPara >= 0 && startPara < allParas.size() && paraStr.equals(allParas.get(startPara))) {
        return startPara;
      }
    }
    return -1;
  }

  /**
   * Change manual linebreak to distinguish from end of paragraph
   */
  private static String fixLinebreak (String text) {
    return text.replaceAll(SINGLE_END_OF_PARAGRAPH, MANUAL_LINEBREAK);
  }

  /**
   * Gives back the start paragraph for text level check
   */
  private int getStartOfParaCheck(int numCurPara, int parasToCheck, 
      List<String> allParas, List<Integer> headings, boolean textIsChanged) {
    if (numCurPara < 0 || allParas == null || allParas.size() <= numCurPara) {
      return -1;
    }
    if(parasToCheck < -1) {
      return 0;
    }
    if(parasToCheck == 0) {
      return numCurPara;
    }
    int headingBefore = -1;
    for(int heading : headings) {
      if(heading > numCurPara) {
        break;
      } 
      headingBefore = heading;
    }
    if(headingBefore == numCurPara) {
      return headingBefore;
    }
    headingBefore++;
    if(parasToCheck < 0) {
      return headingBefore;
    }
    int startPos = numCurPara - parasToCheck;
    if(textIsChanged) {
      startPos -= parasToCheck;
    }
    if (startPos < headingBefore) {
      startPos = headingBefore;
    }
    return startPos;
  }
  
  /**
   * Gives back the end paragraph for text level check
   */
  private int getEndOfParaCheck(int numCurPara, int parasToCheck,
      List<String> allParas, List<Integer> headings, boolean textIsChanged) {
    if (numCurPara < 0 || allParas == null || allParas.size() <= numCurPara) {
      return -1;
    }
    int headingAfter = -1;
    if(parasToCheck < -1) {
      return allParas.size();
    }
    if(parasToCheck == 0) {
      return numCurPara + 1;
    }
    for(int heading : headings) {
      headingAfter = heading;
      if(heading >= numCurPara) {
        break;
      }
    }
    if(headingAfter == numCurPara) {
      return headingAfter + 1;
    }
    if(headingAfter < numCurPara) {
      headingAfter = allParas.size();
    }
    if(parasToCheck < 0) {
      return headingAfter;
    }
    int endPos = numCurPara + 1 + parasToCheck;
    if(!textIsChanged) {
      endPos += defaultParaCheck;
    } else {
      endPos += parasToCheck;
    }
    if (endPos > headingAfter) {
      endPos = headingAfter;
    }
    return endPos;
  }
  
  /**
   * Gives Back the full Text as String
   */
  private String getDocAsString(int numCurPara, int parasToCheck, 
      List<String> allParas, List<Integer> headings, boolean textIsChanged) {
    int startPos = getStartOfParaCheck(numCurPara, parasToCheck, allParas, headings, textIsChanged);
    int endPos = getEndOfParaCheck(numCurPara, parasToCheck, allParas, headings, textIsChanged);
    if(startPos < 0 || endPos < 0) {
      return "";
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
  private int getStartOfParagraph(int nPara, int checkedPara, int parasToCheck, 
      List<String> allParas, List<Integer> headings, boolean textIsChanged) {
    if (allParas == null || nPara < 0 || nPara >= allParas.size()) {
      return -1;
    }
    int startPos = getStartOfParaCheck(checkedPara, parasToCheck, allParas, headings, textIsChanged);
    if(startPos < 0) {
      return -1;
    }
    int pos = 0;
    for (int i = startPos; i < nPara; i++) {
      pos += allParas.get(i).length() + NUMBER_PARAGRAPH_CHARS;
    }
    return pos;
  }

  /**
   * Annotate text
   * Handling of footnotes etc.
   */
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

  /**
   * Merge errors from different checks (paragraphs and sentences)
   */
  private SingleProofreadingError[] mergeErrors(SingleProofreadingError[] sErrors, List<SingleProofreadingError[]> pErrors) {
    int errorCount = 0;
    if (sErrors != null) {
      errorCount += sErrors.length;
    }
    if (pErrors != null) {
      for(SingleProofreadingError[] pError : pErrors) {
        if(pError != null) {
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
      for(SingleProofreadingError[] pError : pErrors) {
        if(pError != null) {
          arraycopy(pError, 0, errorArray, sErrorCount, pError.length);
          sErrorCount += pError.length;
        }
      }
    }
    Arrays.sort(errorArray, new ErrorPositionComparator());
    return filterIgnoredMatches(errorArray, paraNum);
  }
  
  /**
   * Filter ignored errors (from ignore once)
   */
  private SingleProofreadingError[] filterIgnoredMatches (SingleProofreadingError[] unFilteredErrors, int nPara) {
    if(!ignoredMatches.isEmpty() && ignoredMatches.containsKey(nPara)) {
      List<Integer> xIgnoredMatches = ignoredMatches.get(nPara);
      List<SingleProofreadingError> filteredErrors = new ArrayList<>();
      for (SingleProofreadingError error : unFilteredErrors) {
        boolean noFilter = true;
        for (int nIgnore : xIgnoredMatches) {
          if(error.nErrorStart <= nIgnore && error.nErrorStart + error.nErrorLength > nIgnore) {
            noFilter = false;
            break;
          }
        }
        if (noFilter) {
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
      int startSentencePos, int endSentencePos, SwJLanguageTool langTool) {
    List<SingleProofreadingError[]> pErrors = new ArrayList<>();

    if(paraNum < 0 || (numParasToCheck >= 0 && !doFullCheckAtFirst && !useQueue)) {
      pErrors.add(checkParaRules(paraText, paraNum, startSentencePos, endSentencePos, langTool, 0, numParasToCheck));
      if(resetCheck) {
        addChangedParas();
      }
    } else {
      //  Real full text check / numParas < 0
      ResultCache oldCache = null;
      List<Integer> tmpChangedParas;
      if(resetCheck) {
        changedParas = new ArrayList<>();
      }
      for(int i = 0; i < minToCheckPara.size(); i++) {
        int parasToCheck = minToCheckPara.get(i);
        if(!firstCheckIsDone && numParasToCheck >= 0 && parasToCheck < 0) {
          parasToCheck = -2;
        }
        if(firstCheckIsDone && numParasToCheck >= 0 && (parasToCheck < 0 || numParasToCheck < parasToCheck)) {
          parasToCheck = numParasToCheck;
        }
        defaultParaCheck = PARA_CHECK_DEFAULT;
        mDocHandler.activateTextRulesByIndex(i);
        if (debugMode > 1) {
          MessageHandler.printToLogFile("ParaCeck: Index: " + i + "/" + minToCheckPara.size() 
            + "; numParasToCheck: " + numParasToCheck + logLineBreak);
        }
        if(resetCheck && parasToCheck < 0 && !useQueue) {
          oldCache = paragraphsCache.get(i);
          if(parasToCheck < -1) {
            paragraphsCache.set(i, new ResultCache());
          } else {
            paragraphsCache.set(i, new ResultCache(oldCache));
          }
        }
        pErrors.add(checkParaRules(paraText, paraNum, startSentencePos, endSentencePos, langTool, i, parasToCheck));
        if(resetCheck && !useQueue) {
          if(parasToCheck < 0) {
            tmpChangedParas = paragraphsCache.get(i).differenceInCaches(oldCache);
            if(changedParas == null) {
              changedParas = new ArrayList<>();
            }
            for(int chPara : tmpChangedParas) {
              if(!changedParas.contains(chPara)) {
                changedParas.add(chPara);
              }
            }
          } else {
            addChangedParas();
          }
        }
      }
      if(resetCheck && useQueue) {
        int cacheNum = 1;
        tmpChangedParas = paragraphsCache.get(cacheNum).differenceInCaches(oldCache);
        changedParas = new ArrayList<>();
        for(int n : tmpChangedParas) {
          if(paragraphsCache.get(cacheNum).getEntryByParagraph(n) != null) {
            changedParas.add(n);
          }
        }
      }
      if(!firstCheckIsDone) {
        firstCheckIsDone = true;
      }
      oldCache = null;
      mDocHandler.reactivateTextRules();
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
    if (lastPara > allParas.size()) {
      lastPara = allParas.size();
    }
    if(changedParas == null) {
      changedParas = new ArrayList<>();
    }
    for (int n = firstPara; n < lastPara; n++) {
      if(!changedParas.contains(n)) {
        changedParas.add(n);
      }
    }
  }
  
  /**
   * Add an new entry to text level queue
   */
  public void addQueueEntry(int nPara, int nCache, int nCheck, String docId) {
    int nStart = getStartOfParaCheck(nPara, nCheck, allParas, headings, textIsChanged);
    int nEnd = getEndOfParaCheck(nPara, nCheck, allParas, headings, textIsChanged);
    mDocHandler.getTextLevelCheckQueue().addQueueEntry(nStart, nEnd, nCache, nCheck, docId, resetCheck);
  }
  
  /**
   * create a queue entry 
   * used by getNextQueueEntry
   */
  private QueueEntry createQueueEntry(int nPara, int nCache) {
    int nCheck = minToCheckPara.get(nCache);
    int nStart = getStartOfParaCheck(nPara, nCheck, allParas, headings, textIsChanged);
    int nEnd = getEndOfParaCheck(nPara, nCheck, allParas, headings, textIsChanged);
    return mDocHandler.getTextLevelCheckQueue().createQueueEntry(nStart, nEnd, nCache, nCheck, docID);
  }

  /**
   * get the next queue entry which is the next empty cache entry
   */
  public QueueEntry getNextQueueEntry(int nPara, int nCache) {
    for(int i = nPara + 1; i < allParas.size(); i++) {
      if(paragraphsCache.get(nCache).getEntryByParagraph(i) == null) {
        return createQueueEntry(i, nCache);
      }
    }
    for(int i = 0; i < nPara; i++) {
      if(paragraphsCache.get(nCache).getEntryByParagraph(i) == null) {
        return createQueueEntry(i, nCache);
      }
    }
    for(int n = 0; n < minToCheckPara.size(); n++) {
      if(n != nCache && minToCheckPara.get(n) != 0) {
        for(int i = 0; i < allParas.size(); i++) {
          if(paragraphsCache.get(n).getEntryByParagraph(i) == null) {
            return createQueueEntry(i, n);
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
    addParaErrorsToCache(nStart, langTool, cacheNum, nCheck, doReset);
  }

  /**
   * check the text level rules associated with a given cache (cacheNum)
   */
  @Nullable
  private SingleProofreadingError[] checkParaRules( String paraText, int paraNum, 
      int startSentencePos, int endSentencePos, SwJLanguageTool langTool, int cacheNum, int parasToCheck) {

    List<RuleMatch> paragraphMatches;
    SingleProofreadingError[] pErrors = null;
    try {
      // use Cache for check in single paragraph mode only after the first call of paragraph
      if(paraNum >= 0) {
        pErrors = paragraphsCache.get(cacheNum).getFromPara(paraNum, startSentencePos, endSentencePos);
        if (debugMode > 1 && pErrors != null) {
          MessageHandler.printToLogFile("Check Para Rules: pErrors from cache: " + pErrors.length);
        }
      } else {
        if (startSentencePos > 0 && lastSinglePara != null && lastSinglePara.equals(paraText)) {
          pErrors = singleParaCache.getFromPara(0, startSentencePos, endSentencePos);
          return pErrors;
        } else if(startSentencePos == 0) {
          lastSinglePara = new String(paraText);
        }
      }
      // return Cache result if available / for right mouse click or Dialog only use cache
      if(paraNum >= 0 && (pErrors != null || isMouseOrDialog || (useQueue && parasToCheck != 0))) {
        if(useQueue && pErrors == null && parasToCheck != 0) {
          addQueueEntry(paraNum, cacheNum, parasToCheck, docID);
        }
        return pErrors;
      }
      
      String textToCheck;
      //  One paragraph check (set by options or proof of footnote, etc.)
      if(paraNum < 0 || parasToCheck == 0) {
        textToCheck = fixLinebreak(paraText);
        paragraphMatches = langTool.check(textToCheck, true, JLanguageTool.ParagraphHandling.ONLYPARA);
        if(paragraphMatches == null || paragraphMatches.isEmpty()) {
          if (paraNum < 0) {
            singleParaCache.put(0, new SingleProofreadingError[0]);
          } else {
            paragraphsCache.get(cacheNum).put(paraNum, new SingleProofreadingError[0]);
            if (debugMode > 1) {
              MessageHandler.printToLogFile("--> Enter to para cache(" + cacheNum + "): Paragraph: " + paraText 
                + "; Error number: " + 0 + logLineBreak);
            }
          }
        } else {
          List<SingleProofreadingError> errorList = new ArrayList<>();
          for (RuleMatch myRuleMatch : paragraphMatches) {
            int toPos = myRuleMatch.getToPos();
            if(toPos > paraText.length()) {
              toPos = paraText.length();
            }
            errorList.add(createOOoError(myRuleMatch, 0, toPos, paraText.charAt(toPos-1)));
          }
          if (!errorList.isEmpty()) {
            if (paraNum < 0) {
              singleParaCache.put(0, errorList.toArray(new SingleProofreadingError[0]));
            } else {
              if (debugMode > 1) {
                MessageHandler.printToLogFile("--> Enter to para cache(" + cacheNum + "): Paragraph: " + paraText 
                  + "; Error number: " + errorList.size() + logLineBreak);
              }
              paragraphsCache.get(cacheNum).put(paraNum, errorList.toArray(new SingleProofreadingError[0]));
            }
          } else {
            if (paraNum < 0) {
              singleParaCache.put(0, new SingleProofreadingError[0]);
            } else {
              if (debugMode > 1) {
                MessageHandler.printToLogFile("--> Enter to para cache(" + cacheNum + "): Paragraph: " + paraText 
                  + "; Error number: " + 0 + logLineBreak);
              }
              paragraphsCache.get(cacheNum).put(paraNum, new SingleProofreadingError[0]);
            }
          }
        }  
        if (paraNum < 0) {
          return singleParaCache.getFromPara(0, startSentencePos, endSentencePos);
        } else {
          return paragraphsCache.get(cacheNum).getFromPara(paraNum, startSentencePos, endSentencePos);
        }
      }

      //  check of numParasToCheck or full text 
      addParaErrorsToCache(paraNum, langTool, cacheNum, parasToCheck, false);
      return paragraphsCache.get(cacheNum).getFromPara(paraNum, startSentencePos, endSentencePos);

    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return null;
  }

  /**
   *   check for number of Paragraphs > 0, chapter wide or full text
   *   is also called by text level queue
   */
  private void addParaErrorsToCache(int paraNum, SwJLanguageTool langTool, int cacheNum, int parasToCheck, boolean override) {
    //  make the method thread save
    MultiDocumentsHandler mDH = mDocHandler;
    FlatParagraphTools flatPara = this.flatPara;
    List<String> allParas = this.allParas;
    List<Integer> headings = this.headings;
    boolean textIsChanged = this.textIsChanged;
    int divNum = this.divNum;
    try {

      ResultCache oldCache = null;
      if(useQueue) {
        oldCache = paragraphsCache.get(cacheNum);
        if(parasToCheck < -1) {
          paragraphsCache.set(cacheNum, new ResultCache());
        } else {
          paragraphsCache.set(cacheNum, new ResultCache(oldCache));
        }
      }

      String textToCheck = getDocAsString(paraNum, parasToCheck, allParas, headings, textIsChanged);
      List<RuleMatch> paragraphMatches = langTool.check(textToCheck, true, JLanguageTool.ParagraphHandling.ONLYPARA);
      
      int startPara = getStartOfParaCheck(paraNum, parasToCheck, allParas, headings, textIsChanged);
      int endPara = getEndOfParaCheck(paraNum, parasToCheck, allParas, headings, textIsChanged);
      int startPos = getStartOfParagraph(startPara, paraNum, parasToCheck, allParas, headings, textIsChanged);
      int endPos;
      for (int i = startPara; i < endPara; i++) {
        if(useQueue && mDH.getTextLevelCheckQueue().isInterrupted()) {
          return;
        }
        if(i < endPara - 1) {
          endPos = getStartOfParagraph(i + 1, paraNum, parasToCheck, allParas, headings, textIsChanged);
        } else {
          endPos = textToCheck.length();
        }
        if(paragraphMatches == null || paragraphMatches.isEmpty()) {
          paragraphsCache.get(cacheNum).put(i, new SingleProofreadingError[0]);
          if (debugMode > 1) {
            MessageHandler.printToLogFile("--> Enter to para cache(" + cacheNum + "): Paragraph: " + allParas.get(i) 
               + "; Error number: 0" + logLineBreak);
          }
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
            paragraphsCache.get(cacheNum).put(i, errorList.toArray(new SingleProofreadingError[0]));
            if (debugMode > 1) {
              MessageHandler.printToLogFile("--> Enter to para cache(" + cacheNum + "): Paragraph: " + allParas.get(i) 
                + "; Error number: " + errorList.size() + logLineBreak);
            }
          } else {
            paragraphsCache.get(cacheNum).put(i, new SingleProofreadingError[0]);
            if (debugMode > 1) {
              MessageHandler.printToLogFile("--> Enter to para cache(" + cacheNum + "): Paragraph: " + allParas.get(i) 
                 + "; Error number: 0" + logLineBreak);
            }
          }
        }
        startPos = endPos;
      }
      if(useQueue) {
        if(mDH.getTextLevelCheckQueue().isInterrupted()) {
          return;
        }
        if(override) {
          if (debugMode > 0) {
            MessageHandler.printToLogFile("Do Reset (useQueue == true)");
          }
          List<Integer> tmpChangedParas;
          tmpChangedParas = paragraphsCache.get(cacheNum).differenceInCaches(oldCache);
          List<Integer> changedParas = new ArrayList<>();
          for(int n : tmpChangedParas) {
            if(sentencesCache.getEntryByParagraph(n) != null) {
              changedParas.add(n);
            }
          }
          if(!changedParas.isEmpty()) {
            remarkChangedParagraphs(changedParas);
          }
        } else {
          Map<Integer, SingleProofreadingError[]> changedParasMap;
          if (debugMode > 0) {
            MessageHandler.printToLogFile("Mark paragraphs from " + startPara + " to " + endPara);
          }
          changedParasMap = new HashMap<>();
          for(int n = startPara; n < endPara; n++) {
            SingleProofreadingError[] errors = paragraphsCache.get(cacheNum).getMatches(n, 0);
            if(errors != null && errors.length != 0) {
              SingleProofreadingError[] filteredErrors = filterIgnoredMatches(errors, n);
              if(filteredErrors != null && filteredErrors.length != 0) {
                changedParasMap.put(n, filteredErrors);
              }
            }
          }
          flatPara.markParagraphs(changedParasMap, divNum, false);
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
      int numCurPara, int[] footnotePositions, SwJLanguageTool langTool) {
    try {
      SingleProofreadingError[] errorArray;
      if (StringTools.isEmpty(sentence)) {
        errorArray = new SingleProofreadingError[0];
      } else {
        List<RuleMatch> ruleMatches;
        if(!langTool.isRemote()) {
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
                                          sentence.length(), sentence.charAt(sentence.length()-1));
            i++;
          }
        } else {
          errorArray = new SingleProofreadingError[0];
        }
      }
      if(numParasToCheck != 0 && numCurPara >= 0) {
        if (debugMode > 1) {
          MessageHandler.printToLogFile("--> Enter to sentences cache: numCurPara: " + numCurPara 
              + "; startPos: " + startPos + "; Sentence: " + sentence 
              + "; Error number: " + errorArray.length + logLineBreak);
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
    ignoredMatches = new HashMap<>();
  }
  
  /**
   * add a ignore once entry to queue an remove the mark
   */
  public String ignoreOnce() {
    ViewCursorTools viewCursor = new ViewCursorTools(xContext);
    int x = viewCursor.getViewCursorCharacter();
    int y = viewCursor.getViewCursorParagraph();
    if (ignoredMatches.containsKey(y)) {
      List<Integer> charNums = ignoredMatches.get(y);
      charNums.add(x);
      ignoredMatches.put(y, charNums);
    } else {
      List<Integer> charNums = new ArrayList<>();
      charNums.add(x);
      ignoredMatches.put(y, charNums);
    }
    if(numParasToCheck != 0) {
      List<Integer> changedParas = new ArrayList<>();
      changedParas.add(y);
      remarkChangedParagraphs(changedParas);
    }
    if (debugMode > 0) {
      MessageHandler.printToLogFile("Ignore Match added at: paragraph: " + y + "; character: " + x);
    }
    return docID;
  }
  
  /**
   * get a rule ID of an error out of the cache 
   * by the position of the error (paragraph number and number of character)
   */
  private String getRuleIdFromCache(int nPara, int nChar) {
    SingleProofreadingError error = sentencesCache.getErrorAtPosition(nPara, nChar);
    for(ResultCache paraCache : paragraphsCache) {
      SingleProofreadingError err = paraCache.getErrorAtPosition(nPara, nChar);
      if(err != null) {
        if(error == null || error.nErrorStart < err.nErrorStart
            || (error.nErrorStart == err.nErrorStart && error.nErrorLength > err.nErrorLength)) {
          error = err;
        } 
      }
    }
    if(error != null) {
      return error.aRuleIdentifier;
    } else {
      return null;
    }
  }
  
  /**
   * get back the rule ID to deactivate a rule
   */
  public String deactivateRule() {
    ViewCursorTools viewCursor = new ViewCursorTools(xContext);
    int x = viewCursor.getViewCursorCharacter();
    int y = viewCursor.getViewCursorParagraph();
    return getRuleIdFromCache(y, x);
  }

  /** 
   * Class to add a LanguageTool Options item to the context menu
   * since 4.6
   */
  class ContextMenuInterceptor implements XContextMenuInterceptor{
    
    private final static String IGNORE_ONCE_URL = "slot:201";
    private final static String ADD_TO_DICTIONARY_2 = "slot:2";
    private final static String ADD_TO_DICTIONARY_3 = "slot:3";
    private final static String LT_OPTIONS_URL = "service:org.languagetool.openoffice.Main?configure";
    private final static String LT_IGNORE_ONCE = "service:org.languagetool.openoffice.Main?ignoreOnce";
    private final static String LT_DEACTIVATE_RULE = "service:org.languagetool.openoffice.Main?deactivateRule";
    private final static String LT_REMOTE_HINT = "service:org.languagetool.openoffice.Main?remoteHint";   

    public ContextMenuInterceptor() {}
    
    public ContextMenuInterceptor(XComponentContext xContext) {
      try {
        XTextDocument xTextDocument = OfficeTools.getCurrentDocument(xContext);
        if (xTextDocument == null) {
          MessageHandler.printToLogFile("ContextMenuInterceptor: xTextDocument == null");
          return;
        }
        xTextDocument.getCurrentController();
        XController xController = xTextDocument.getCurrentController();
        if (xController == null) {
          MessageHandler.printToLogFile("ContextMenuInterceptor: xController == null");
          return;
        }
        XContextMenuInterception xContextMenuInterception = UnoRuntime.queryInterface(XContextMenuInterception.class, xController);
        if (xContextMenuInterception == null) {
          MessageHandler.printToLogFile("ContextMenuInterceptor: xContextMenuInterception == null");
          return;
        }
        ContextMenuInterceptor aContextMenuInterceptor = new ContextMenuInterceptor();
        XContextMenuInterceptor xContextMenuInterceptor = 
            UnoRuntime.queryInterface(XContextMenuInterceptor.class, aContextMenuInterceptor);
        if (xContextMenuInterceptor == null) {
          MessageHandler.printToLogFile("ContextMenuInterceptor: xContextMenuInterceptor == null");
          return;
        }
        xContextMenuInterception.registerContextMenuInterceptor(xContextMenuInterceptor);
      } catch (Throwable t) {
        MessageHandler.printException(t);
      }
    }
  
    @Override
    public ContextMenuInterceptorAction notifyContextMenuExecute(ContextMenuExecuteEvent aEvent) {
      try {
        XIndexContainer xContextMenu = aEvent.ActionTriggerContainer;
        int count = xContextMenu.getCount();
        
        //  Add LT Options Item if a Grammar or Spell error was detected
        for (int i = 0; i < count; i++) {
          Any a = (Any) xContextMenu.getByIndex(i);
          XPropertySet props = (XPropertySet) a.getObject();
          if (debugMode > 0) {
            printProperties(props);
          }
          String str = null;
          if(props.getPropertySetInfo().hasPropertyByName("CommandURL")) {
            str = props.getPropertyValue("CommandURL").toString();
          }
          if(str != null && IGNORE_ONCE_URL.equals(str)) {
            int n;  
            for(n = i + 1; n < count; n++) {
              a = (Any) xContextMenu.getByIndex(n);
              XPropertySet tmpProps = (XPropertySet) a.getObject();
              if(tmpProps.getPropertySetInfo().hasPropertyByName("CommandURL")) {
                str = tmpProps.getPropertyValue("CommandURL").toString();
              }
              if(ADD_TO_DICTIONARY_2.equals(str) || ADD_TO_DICTIONARY_3.equals(str)) {
                break;
              }
            }
            if(n >= count) {
              mDocHandler.setMenuDocId(getDocID());
              if(paraNum >= 0) {
                props.setPropertyValue("CommandURL", LT_IGNORE_ONCE);
              }
              XMultiServiceFactory xMenuElementFactory = UnoRuntime.queryInterface(XMultiServiceFactory.class, xContextMenu);

              XPropertySet xNewMenuEntry1 = UnoRuntime.queryInterface(XPropertySet.class,
                  xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
              xNewMenuEntry1.setPropertyValue("Text", MESSAGES.getString("loContextMenuDeactivateRule"));
              xNewMenuEntry1.setPropertyValue("CommandURL", LT_DEACTIVATE_RULE);
              xContextMenu.insertByIndex(i + 2, xNewMenuEntry1);
              
              int nId = i + 4;
              if(isRemote) {
                XPropertySet xNewMenuEntry2 = UnoRuntime.queryInterface(XPropertySet.class,
                    xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
                xNewMenuEntry2.setPropertyValue("Text", MESSAGES.getString("loMenuRemoteInfo"));
                xNewMenuEntry2.setPropertyValue("CommandURL", LT_REMOTE_HINT);
                xContextMenu.insertByIndex(nId, xNewMenuEntry2);
                nId++;
              }
              
              XPropertySet xNewMenuEntry = UnoRuntime.queryInterface(XPropertySet.class,
                  xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
              xNewMenuEntry.setPropertyValue("Text", MESSAGES.getString("loContextMenuOptions"));
              xNewMenuEntry.setPropertyValue("CommandURL", LT_OPTIONS_URL);
              xContextMenu.insertByIndex(nId, xNewMenuEntry);
  
              return ContextMenuInterceptorAction.EXECUTE_MODIFIED;
            }
          }
        }

        //  Add LT Options Item for context menu without grammar error
        XMultiServiceFactory xMenuElementFactory = UnoRuntime.queryInterface(XMultiServiceFactory.class, xContextMenu);
        XPropertySet xSeparator = UnoRuntime.queryInterface(XPropertySet.class,
            xMenuElementFactory.createInstance("com.sun.star.ui.ActionTriggerSeparator"));
        xSeparator.setPropertyValue("SeparatorType", ActionTriggerSeparatorType.LINE);
        xContextMenu.insertByIndex(count, xSeparator);
        
        int nId = count + 1;
        if(isRemote) {
          XPropertySet xNewMenuEntry2 = UnoRuntime.queryInterface(XPropertySet.class,
              xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
          xNewMenuEntry2.setPropertyValue("Text", MESSAGES.getString("loMenuRemoteInfo"));
          xNewMenuEntry2.setPropertyValue("CommandURL", LT_REMOTE_HINT);
          xContextMenu.insertByIndex(nId, xNewMenuEntry2);
          nId++;
        }

        XPropertySet xNewMenuEntry = UnoRuntime.queryInterface(XPropertySet.class,
            xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
        xNewMenuEntry.setPropertyValue("Text", MESSAGES.getString("loContextMenuOptions"));
        xNewMenuEntry.setPropertyValue("CommandURL", LT_OPTIONS_URL);
        xContextMenu.insertByIndex(nId, xNewMenuEntry);

        return ContextMenuInterceptorAction.EXECUTE_MODIFIED;

      } catch (Throwable t) {
        MessageHandler.printException(t);
      }
      
      MessageHandler.printToLogFile("no change in Menu");
      return ContextMenuInterceptorAction.IGNORED;
    }
    
    private void printProperties(XPropertySet props) throws UnknownPropertyException, WrappedTargetException {
      Property[] propInfo = props.getPropertySetInfo().getProperties();
      for (Property property : propInfo) {
        MessageHandler.printToLogFile("Property: Name: " + property.Name + ", Type: " + property.Type);
      }
      if(props.getPropertySetInfo().hasPropertyByName("Text")) {
        MessageHandler.printToLogFile("Property: Name: " + props.getPropertyValue("Text").toString());
      }
      if(props.getPropertySetInfo().hasPropertyByName("CommandURL")) {
        MessageHandler.printToLogFile("Property: CommandURL: " + props.getPropertyValue("CommandURL").toString());
      }
    }

  }
  
}
