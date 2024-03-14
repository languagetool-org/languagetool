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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
// import org.languagetool.gui.Configuration;
import org.languagetool.gui.Tools;
import org.languagetool.openoffice.DocumentCache.TextParagraph;
import org.languagetool.openoffice.MultiDocumentsHandler.WaitDialogThread;
import org.languagetool.openoffice.OfficeDrawTools.UndoMarkupContainer;
import org.languagetool.openoffice.OfficeTools.DocumentType;
import org.languagetool.openoffice.OfficeTools.LoErrorType;
// import org.languagetool.rules.Rule;
import org.languagetool.rules.Rule;

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.text.TextMarkupType;
import com.sun.star.text.XFlatParagraph;
import com.sun.star.text.XMarkingAccess;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XTextCursor;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Class defines the spell and grammar check dialog
 * @since 5.1
 * @author Fred Kruse
 */
public class SpellAndGrammarCheckDialog extends Thread {
  
  private static boolean debugMode = OfficeTools.DEBUG_MODE_CD;         //  should be false except for testing
//  private static boolean debugModeTm = OfficeTools.DEBUG_MODE_TM;       //  should be false except for testing
  private static boolean debugModeTm = OfficeTools.DEBUG_MODE_TM;       //  should be false except for testing

  private static final ResourceBundle messages = JLanguageTool.getMessageBundle();
  private static final String spellingError = messages.getString("desc_spelling");
  private static final String spellRuleId = "LO_SPELLING_ERROR";
  
  private final static int DIALOG_LOOPS = 20;
  private final static int LOOP_WAIT_TIME = 50;
  private final static int TEST_LOOPS = 10;
  
  private final static String dialogName = messages.getString("guiOOoCheckDialogName");
  private final static String labelLanguage = messages.getString("textLanguage");
  private final static String labelSuggestions = messages.getString("guiOOosuggestions"); 
  private final static String moreButtonName = messages.getString("guiMore"); 
  private final static String ignoreButtonName = messages.getString("guiOOoIgnoreButton"); 
  private final static String ignoreAllButtonName = messages.getString("guiOOoIgnoreAllButton"); 
  private final static String ignorePermanentButtonName = messages.getString("loContextMenuIgnorePermanent"); 
  private final static String resetIgnorePermanentButtonName = messages.getString("loMenuResetIgnorePermanent"); 
  private final static String ignoreRuleButtonName = messages.getString("guiOOoIgnoreRuleButton"); 
  private final static String deactivateRuleButtonName = messages.getString("loContextMenuDeactivateRule"); 
  private final static String addToDictionaryName = messages.getString("guiOOoaddToDictionary");
  private final static String changeButtonName = messages.getString("guiOOoChangeButton"); 
  private final static String changeAllButtonName = messages.getString("guiOOoChangeAllButton"); 
  private final static String autoCorrectButtonName = messages.getString("guiOOoAutoCorrectButton"); 
  private final static String helpButtonName = messages.getString("guiOOoHelpButton"); 
  private final static String optionsButtonName = messages.getString("guiOOoOptionsButton"); 
  private final static String undoButtonName = messages.getString("guiUndo");
  private final static String closeButtonName = messages.getString("guiOOoCloseButton");
  private final static String changeLanguageList[] = { messages.getString("guiOOoChangeLanguageRequest"),
                                                messages.getString("guiOOoChangeLanguageMatch"),
                                                messages.getString("guiOOoChangeLanguageParagraph") };
  private final static String languageHelp = messages.getString("loDialogLanguageHelp");
  private final static String changeLanguageHelp = messages.getString("loDialogChangeLanguageHelp");
  private final static String matchDescriptionHelp = messages.getString("loDialogMatchDescriptionHelp");
  private final static String matchParagraphHelp = messages.getString("loDialogMatchParagraphHelp");
  private final static String suggestionsHelp = messages.getString("loDialogSuggestionsHelp");
  private final static String checkTypeHelp = messages.getString("loDialogCheckTypeHelp");
  private final static String helpButtonHelp = messages.getString("loDialogHelpButtonHelp"); 
  private final static String optionsButtonHelp = messages.getString("loDialogOptionsButtonHelp"); 
  private final static String undoButtonHelp = messages.getString("loDialogUndoButtonHelp");
  private final static String closeButtonHelp = messages.getString("loDialogCloseButtonHelp");
  private final static String moreButtonHelp = messages.getString("loDialogMoreButtonHelp"); 
  private final static String ignoreButtonHelp = messages.getString("loDialogIgnoreButtonHelp"); 
  private final static String ignoreAllButtonHelp = messages.getString("loDialogIgnoreAllButtonHelp"); 
  private final static String ignorePermanentButtonHelp = messages.getString("loDialogIgnorePermanentButtonHelp"); 
  private final static String resetIgnorePermanentButtonHelp = messages.getString("loDialogResetIgnorePermanentButtonHelp"); 
  private final static String deactivateRuleButtonHelp = messages.getString("loDialogDeactivateRuleButtonHelp"); 
  private final static String activateRuleButtonHelp = messages.getString("loDialogActivateRuleButtonHelp"); 
  private final static String addToDictionaryHelp = messages.getString("loDialogAddToDictionaryButtonHelp");
  private final static String changeButtonHelp = messages.getString("loDialogChangeButtonHelp"); 
  private final static String changeAllButtonHelp = messages.getString("loDialogChangeAllButtonHelp"); 
  private final static String autoCorrectButtonHelp = messages.getString("loDialogAutoCorrectButtonHelp"); 
  private final static String checkStatusInitialization = messages.getString("loCheckStatusInitialization"); 
  private final static String checkStatusCheck = messages.getString("loCheckStatusCheck"); 
  private final static String labelCheckProgress = messages.getString("loLabelCheckProgress");
  private final static String loBusyMessage = messages.getString("loBusyMessage");
//  private final static String loWaitMessage = messages.getString("loWaitMessage");
  
  private static int nLastFlat = 0;
  
  private final XComponentContext xContext;
  private final MultiDocumentsHandler documents;
//  private ExtensionSpellChecker spellChecker;
  private LinguisticServices linguServices;
  
  private WaitDialogThread inf;
  private SwJLanguageTool lt = null;
  private Language lastLanguage;
  private Locale locale;
  private int checkType = 0;
  private String checkRuleId = null;
  private DocumentCache docCache;
  private DocumentType docType = DocumentType.WRITER;
  private boolean doInit = true;
  private int dialogX = -1;
  private int dialogY = -1;
  private boolean hasUncheckedParas = false;
  
  
  SpellAndGrammarCheckDialog(XComponentContext xContext, MultiDocumentsHandler documents, Language language, WaitDialogThread inf) {
    debugMode = OfficeTools.DEBUG_MODE_CD;
    this.xContext = xContext;
    this.documents = documents;
    this.inf = inf;
    lastLanguage = language;
    locale = LinguisticServices.getLocale(language);
    if(!documents.javaVersionOkay()) {
      return;
    }
    if (documents.noLtSpeller()) {
      linguServices = new LinguisticServices(xContext);
    }
  }

  /**
   * Initialize LanguageTool to run in LT check dialog and next error function
   */
  private void setLangTool(SingleDocument document, Language language) {
    if (document.getDocumentType() == DocumentType.IMPRESS) {
      documents.setCheckImpressDocument(true);
    }
    lt = documents.initLanguageTool(language, false);
    if (lt != null) {
      documents.initCheck(lt);
      documents.resetSortedTextRules(lt);
    }
    if (debugMode) {
      for (String id : lt.getDisabledRules()) {
        MessageHandler.printToLogFile("CheckDialog: setLangTool: After init disabled rule: " + id);
      }
    }
    doInit = false;
  }

  /**
   * opens the LT check dialog for spell and grammar check
   */
  @Override
  public void run() {
    try {
      long startTime = 0;
      if (debugModeTm) {
        startTime = System.currentTimeMillis();
      }
//      InformationThread inf = new InformationThread(loWaitMessage);
//      inf.start();
      SingleDocument currentDocument = getCurrentDocument(false);
      if (inf.canceled()) {
        return;
      }
      if (currentDocument == null || docCache == null || docCache.size() <= 0) {
        inf.close();
        MessageHandler.showMessage(loBusyMessage);
        documents.setLtDialogIsRunning(false);
        return;
      }
      LtCheckDialog checkDialog = new LtCheckDialog(xContext, currentDocument, inf);
      if (inf.canceled()) {
        return;
      }
      documents.setLtDialog(checkDialog);
      if (debugModeTm) {
        long runTime = System.currentTimeMillis() - startTime;
        if (runTime > OfficeTools.TIME_TOLERANCE) {
          MessageHandler.printToLogFile("CheckDialog: Time to initialise dialog: " + runTime);
        }
      }
      inf.close();
      checkDialog.show();
    } catch (Throwable e) {
      MessageHandler.showError(e);
      documents.setLtDialogIsRunning(false);
    }
  }

  /**
   * Actualize impress/calc document cache
   */
  private void actualizeNonWriterDocumentCache(SingleDocument document) {
    if (docType != DocumentType.WRITER || documents.isBackgroundCheckOff()) {
      DocumentCache oldCache = new DocumentCache(docCache);
      docCache.refresh(document, null, null, document.getXComponent(), 7);
      if (!oldCache.isEmpty()) {
        boolean isSame = true;
        if (oldCache.size() != docCache.size()) {
          isSame = false;
        } else {
          for (int i = 0; i < docCache.size(); i++) {
            if (!docCache.getFlatParagraph(i).equals(oldCache.getFlatParagraph(i))) {
              isSame = false;
              break;
            }
          }
        }
        if (!isSame) {
          document.resetResultCache(true);
        }
      }
    }
  }
  
  /**
   * Get the current document
   * Wait until it is initialized (by LO/OO)
   */
  private SingleDocument getCurrentDocument(boolean actualize) {
    SingleDocument currentDocument = documents.getCurrentDocument();
    int nWait = 0;
    while (currentDocument == null) {
      if (documents.isNotTextDocument()) {
        return null;
      }
      if (nWait > 400) {
        return null;
      }
      MessageHandler.printToLogFile("CheckDialog: getCurrentDocument: Wait: " + ((nWait + 1) * 20));
      nWait++;
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
        MessageHandler.printException(e);
      }
      currentDocument = documents.getCurrentDocument();
    }
    if (currentDocument != null) {
      docType = currentDocument.getDocumentType();
      docCache = currentDocument.getDocumentCache();
      if (docType != DocumentType.WRITER || documents.isBackgroundCheckOff()) {
        actualizeNonWriterDocumentCache(currentDocument);
      }
    }
    return currentDocument;
  }

   /**
   * Find the next error relative to the position of cursor and set the view cursor to the position
   */
  public void nextError() {
    try {
      SingleDocument document = getCurrentDocument(false);
      if (document == null || docType != DocumentType.WRITER || !documents.isEnoughHeapSpace()) {
        return;
      }
      if (docCache == null || docCache.size() <= 0) {
        return;
      }
/*
      if (spellChecker == null) {
        spellChecker = new ExtensionSpellChecker();
      }
*/
      if (lt == null || !documents.isCheckImpressDocument()) {
        setLangTool(document, lastLanguage);
      }
      XComponent xComponent = document.getXComponent();
      DocumentCursorTools docCursor = new DocumentCursorTools(xComponent);
      ViewCursorTools viewCursor = new ViewCursorTools(xComponent);
      int yFlat = getCurrentFlatParagraphNumber(viewCursor, docCache);
      if (yFlat < 0) {
        MessageHandler.showClosingInformationDialog(messages.getString("loNextErrorUnsupported"));
        return;
      }
      int x = viewCursor.getViewCursorCharacter();
      while (yFlat < docCache.size()) {
        CheckError nextError = getNextErrorInParagraph (x, yFlat, document, docCursor, false);
        if (nextError != null && setFlatViewCursor(nextError.error.nErrorStart + 1, yFlat, viewCursor, docCache)) {
          return;
        }
        x = 0;
        yFlat++;
      }
      MessageHandler.showClosingInformationDialog(messages.getString("guiCheckComplete"));
  } catch (Throwable t) {
    MessageHandler.showError(t);
  }
  }

  /**
   * get the current number of the flat paragraph related to the position of view cursor
   * the function considers footnotes, headlines, tables, etc. included in the document 
   */
  private int getCurrentFlatParagraphNumber(ViewCursorTools viewCursor, DocumentCache docCache) {
    TextParagraph textPara = viewCursor.getViewCursorParagraph();
    if (textPara.type == DocumentCache.CURSOR_TYPE_UNKNOWN) {
      return -1;
    }
    nLastFlat = docCache.getFlatParagraphNumber(textPara);
    return nLastFlat; 
  }

   /**
   * Set the view cursor to text position x, y 
   * y = Paragraph of pure text (no footnotes, tables, etc.)
   * x = number of character in paragraph
   */
  public static void setTextViewCursor(int x, TextParagraph y, ViewCursorTools viewCursor)  {
    viewCursor.setTextViewCursor(x, y);
  }

  /**
   * Set the view cursor to position of flat paragraph xFlat, yFlat 
   * y = Flat paragraph of pure text (includes footnotes, tables, etc.)
   * x = number of character in flat paragraph
   */
  private boolean setFlatViewCursor(int xFlat, int yFlat, ViewCursorTools viewCursor, DocumentCache docCache)  {
    if (yFlat < 0) {
      return false;
    }
    TextParagraph para = docCache.getNumberOfTextParagraph(yFlat);
    viewCursor.setTextViewCursor(xFlat, para);
    return true;
  }
  
  /**
   * change the text of a paragraph independent of the type of document
   */
  private void changeTextOfParagraph(int nFPara, int nStart, int nLength, String replace, 
      SingleDocument document, ViewCursorTools viewCursor) {
    String sPara = docCache.getFlatParagraph(nFPara);
    String sEnd = (nStart + nLength < sPara.length() ? sPara.substring(nStart + nLength) : "");
    sPara = sPara.substring(0, nStart) + replace + sEnd;
    if (debugMode) {
      MessageHandler.printToLogFile("CheckDialog: changeTextOfParagraph: set setFlatParagraph: " + sPara);
    }
    if (docType == DocumentType.IMPRESS) {
      OfficeDrawTools.changeTextOfParagraph(nFPara, nStart, nLength, replace, document.getXComponent());
    } else if (docType == DocumentType.CALC) {
      OfficeSpreadsheetTools.setTextofCell(nFPara, sPara, document.getXComponent());
    } else {
      document.getFlatParagraphTools().changeTextOfParagraph(nFPara, nStart, nLength, replace);
    }
    docCache.setFlatParagraph(nFPara, sPara);
    document.removeResultCache(nFPara, true);
    document.removeIgnoredMatch(nFPara, true);
    if (documents.getConfiguration().useTextLevelQueue() && !documents.getConfiguration().noBackgroundCheck()) {
      for (int i = 1; i < documents.getNumMinToCheckParas().size(); i++) {
        document.addQueueEntry(nFPara, i, documents.getNumMinToCheckParas().get(i), document.getDocID(), true);
      }
    }
  }

  /**
   * change the text of a paragraph independent of the type of document
   */
  private Map<Integer, List<Integer>> changeTextInAllParagraph(String word, String ruleID, String replace, 
      SingleDocument document, ViewCursorTools viewCursor) {
    if (word == null || replace == null || word.isEmpty() || replace.isEmpty() || word.equals(replace)) {
      return null;
    }
    Map<Integer, List<Integer>> replacePoints = new HashMap<Integer, List<Integer>>();
    int nLength = word.length();
    for (int n = 0; n < docCache.size(); n++) {
      List<SingleProofreadingError[]> pErrors = new ArrayList<>();
      for (ResultCache resultCache : document.getParagraphsCache()) {
        pErrors.add(resultCache.getSafeMatches(n));
      }
      SingleProofreadingError[] errors = document.mergeErrors(pErrors, n);
      if (errors != null) {
        List<Integer> startPoints = null;
        for (int i = errors.length - 1; i >= 0; i--) {
          SingleProofreadingError error = errors[i];
          if (nLength == error.nErrorLength && ruleID.equals(error.aRuleIdentifier)) {
            String sPara = docCache.getFlatParagraph(n);
            String errWord = sPara.substring(error.nErrorStart, error.nErrorStart + error.nErrorLength);
            if (word.equals(errWord)) {
              if (startPoints == null) {
                startPoints = new ArrayList<>();
              }
              startPoints.add(error.nErrorStart);
              changeTextOfParagraph(n, error.nErrorStart, error.nErrorLength, replace, document, viewCursor);
            }
          }
        }
        if (startPoints != null) {
          replacePoints.put(n, startPoints);
        }
      }
    }
    return replacePoints;
  }

  /**
   * Get the first error in the flat paragraph nFPara at or after character position x
   * @throws Throwable 
   */
  private CheckError getNextErrorInParagraph (int x, int nFPara, SingleDocument document, 
      DocumentCursorTools docTools, boolean checkFrames) throws Throwable {
    if (docCache.isAutomaticGenerated(nFPara, true)) {
      return null;
    }
    String text = docCache.getFlatParagraph(nFPara);
    locale = docCache.getFlatParagraphLocale(nFPara);
    if (locale.Language.equals("zxx")) { // unknown Language 
      locale = documents.getLocale();
    }
    int[] footnotePosition = docCache.getFlatParagraphFootnotes(nFPara);
    
    LoErrorType errType;
    if (checkType == 1 || !(checkFrames || docCache.getParagraphType(nFPara) != DocumentCache.CURSOR_TYPE_SHAPE)) {
      errType = LoErrorType.SPELL;
    }
    else if (checkType == 2) {
      errType = LoErrorType.GRAMMAR;
    }
    else {
      errType = LoErrorType.BOTH;
    }

    SingleProofreadingError  error = getNextGrammatikOrSpellErrorInParagraph(x, nFPara, text, footnotePosition, locale, document, errType);
    if (error != null) {
      return new CheckError(locale, error);
    } else {
      return null;
    }
  }

  /**
   * Get the proofreading result from cache
   */
  SingleProofreadingError[] getErrorsFromCache(int nFPara) {
    int nWait = 0;
    boolean noNull = true;
    SingleDocument document = null;
    List<SingleProofreadingError[]> errors = new ArrayList<>();
    while (nWait < TEST_LOOPS) {
      document = documents.getCurrentDocument();
      for (int cacheNum = 0; cacheNum < documents.getNumMinToCheckParas().size(); cacheNum++) {
        if (!docCache.isAutomaticGenerated(nFPara, true) && (cacheNum == 0 || (documents.isSortedRuleForIndex(cacheNum) 
                                && !document.getDocumentCache().isSingleParagraph(nFPara)))) {
          SingleProofreadingError[] pErrors = document.getParagraphsCache().get(cacheNum).getSafeMatches(nFPara);
          //  Note: unsafe matches are needed to prevent the thread to get into a read lock
          if (pErrors == null) {
            noNull = false;
            hasUncheckedParas = true;
            if (debugMode) {
              MessageHandler.printToLogFile("CheckDialog: getErrorsFromCache: Cache(" + cacheNum + ") is null for Paragraph: " + nFPara);
            }
            errors.add(null);
          } else {
            errors.add(pErrors);
          }
        } else {
          errors.add(new SingleProofreadingError[0]);
        }
      }
      if (noNull) {
        return document.mergeErrors(errors, nFPara);
      }
      try {
        Thread.sleep(LOOP_WAIT_TIME);
      } catch (InterruptedException e) {
        MessageHandler.showError(e);
      }
      nWait++;
    }
    for (int cacheNum = 0; cacheNum < documents.getNumMinToCheckParas().size(); cacheNum++) {
      if (documents.isSortedRuleForIndex(cacheNum)) {
        document.addQueueEntry(nFPara, cacheNum, documents.getNumMinToCheckParas().get(cacheNum), document.getDocID(), false);
      }
    }
    return document.mergeErrors(errors, nFPara);
  }
  
  /**
   * get a list of all spelling errors of the flat paragraph nPara
   */
  public SingleProofreadingError[] getSpellErrors(int nPara, String text, Locale lang, SingleDocument document) throws Throwable {
    try {
      List<SingleProofreadingError> errorArray = new ArrayList<>();
      if (document == null) {
        return null;
      }
      XFlatParagraph xFlatPara = null;
      if (docType == DocumentType.WRITER) {
        xFlatPara = document.getFlatParagraphTools().getFlatParagraphAt(nPara);
        if (xFlatPara == null) {
          return null;
        }
      }
      Locale locale = null;
      List<AnalyzedSentence> analyzedSentences = docCache.getAnalyzedParagraph(nPara);
      int pos = 0;
      for (AnalyzedSentence analyzedSentence : analyzedSentences) {
        AnalyzedTokenReadings[] tokens = analyzedSentence.getTokensWithoutWhitespace();
        for (int i = 0; i < tokens.length; i++) {
          AnalyzedTokenReadings token = tokens[i];
          String sToken = token.getToken();
          if (!token.isNonWord()) {
            int nStart = token.getStartPos() + pos;
            int nEnd = token.getEndPos() + pos;
            if (i < tokens.length - 1) {
              if (tokens[i + 1].getToken().equals(".")) {
                sToken += ".";
              } else { 
                String nextToken = tokens[i + 1].getToken();
                boolean shouldComposed = nextToken.length() > 1 
                    && (nextToken.charAt(0) == '’' || nextToken.charAt(0) == '\''
                    || nextToken.startsWith("n’") || nextToken.startsWith("n'"));
                if (shouldComposed) {
                  sToken += nextToken;
                  nEnd = tokens[i + 1].getEndPos();
                  i++;
                }
              }
            }
            if (sToken.length() > 1) {
              if (xFlatPara != null) {
                locale = xFlatPara.getLanguageOfText(nStart, nEnd - nStart);
              }
              if (locale == null) {
                locale = lang;
              }
              if (linguServices == null) {
                linguServices = new LinguisticServices(xContext);
              }
              if (!linguServices.isCorrectSpell(sToken, locale)) {
                SingleProofreadingError aError = new SingleProofreadingError();
                if (debugMode) {
                  MessageHandler.printToLogFile("CheckDialog: getSpellErrors: Spell Error: Word: " + sToken 
                      + ", Start: " + nStart + ", End: " + nEnd + ", Token(" + i + "): " + tokens[i].getToken()
                      + (i < tokens.length - 1 ? (", Token(" + (i + 1) + "): " + tokens[i + 1].getToken()) : ""));
                }
                if (!document.isIgnoreOnce(nStart, nEnd, nPara, spellRuleId)) {
                  aError.nErrorType = TextMarkupType.SPELLCHECK;
                  aError.aFullComment = spellingError;
                  aError.aShortComment = aError.aFullComment;
                  aError.nErrorStart = nStart;
                  aError.nErrorLength = nEnd - nStart;
                  aError.aRuleIdentifier = spellRuleId;
                  String[] alternatives = linguServices.getSpellAlternatives(token.getToken(), locale);
                  if (alternatives != null) {
                    aError.aSuggestions = alternatives;
                  } else {
                    aError.aSuggestions = new String[0];
                  }
                  aError = SingleCheck.correctRuleMatchWithFootnotes(aError, 
                      docCache.getFlatParagraphFootnotes(nPara), docCache.getFlatParagraphDeletedCharacters(nPara));
                  errorArray.add(aError);
                }
              }
            }
          }
        }
        pos = analyzedSentence.getCorrectedTextLength();
      }
      return errorArray.toArray(new SingleProofreadingError[errorArray.size()]);
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return null;
  }

  /**
   * Get the first grammatical or spell error in the flat paragraph y at or after character position x
   */
  SingleProofreadingError getNextGrammatikOrSpellErrorInParagraph(int x, int nFPara, String text, int[] footnotePosition, 
      Locale locale, SingleDocument document, LoErrorType errType) throws Throwable {
    if (text == null || text.isEmpty() || x >= text.length() || !MultiDocumentsHandler.hasLocale(locale)) {
      return null;
    }
    if (document.getDocumentType() == DocumentType.WRITER && documents.getTextLevelCheckQueue() != null && !documents.noLtSpeller()) {
      SingleProofreadingError[] errors = getErrorsFromCache(nFPara);
      if (errors == null) {
        return null;
      }
      for (SingleProofreadingError error : errors) {
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: getNextGrammatikErrorInParagraph: Start: " + error.nErrorStart + ", ID: " + error.aRuleIdentifier);
          if (error.nErrorType == TextMarkupType.SPELLCHECK) {
            MessageHandler.printToLogFile("CheckDialog: getNextGrammatikErrorInParagraph: Test for correct spell: " 
                    + text.substring(error.nErrorStart, error.nErrorStart + error.nErrorLength));
          }
        }
        if (checkType != 3 && ((errType != LoErrorType.SPELL && error.nErrorType != TextMarkupType.SPELLCHECK)
             || (errType != LoErrorType.GRAMMAR && error.nErrorType == TextMarkupType.SPELLCHECK
             && !documents.getLinguisticServices().isCorrectSpell(text.substring(error.nErrorStart, error.nErrorStart + error.nErrorLength), locale)))
            || (checkType == 3 && error.aRuleIdentifier.equals(checkRuleId)) ) {
          if (error.nErrorStart >= x) {
            if ((error.aSuggestions == null || error.aSuggestions.length == 0) 
                && documents.getLinguisticServices().isThesaurusRelevantRule(error.aRuleIdentifier)) {
              error.aSuggestions = document.getSynonymArray(error, text, locale, lt, false);
            } else if (error.nErrorType == TextMarkupType.SPELLCHECK) {
              List<String> suggestionList = new ArrayList<>();
              for (String suggestion : error.aSuggestions) {
                suggestionList.add(suggestion);
              }
              String[] suggestions = documents.getLinguisticServices().getSpellAlternatives(
                        text.substring(error.nErrorStart, error.nErrorStart + error.nErrorLength), locale);
              if (suggestions != null) {
                for (String suggestion : suggestions) {
                  if (!suggestionList.contains(suggestion)) {
                    suggestionList.add(suggestion);
                  }
                }
              }
              error.aSuggestions = suggestionList.toArray(new String[0]);
            }
            return error;
          }
        }
      }
    } else {
      PropertyValue[] propertyValues = { new PropertyValue("FootnotePositions", -1, footnotePosition, PropertyState.DIRECT_VALUE) };
      ProofreadingResult paRes = new ProofreadingResult();
      paRes.nStartOfSentencePosition = 0;
      paRes.nStartOfNextSentencePosition = 0;
      paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
      paRes.xProofreader = null;
      paRes.aLocale = locale;
      paRes.aDocumentIdentifier = document.getDocID();
      paRes.aText = text;
      paRes.aProperties = propertyValues;
      paRes.aErrors = null;
      Language langForShortName = MultiDocumentsHandler.getLanguage(locale);
      if (langForShortName != null) {
        if (doInit || !langForShortName.equals(lastLanguage) || !documents.isCheckImpressDocument()) {
          lastLanguage = langForShortName;
          setLangTool(document, lastLanguage);
          document.removeResultCache(nFPara, true);
        }
        int lastSentenceStart = -1;
        while (paRes.nStartOfNextSentencePosition < text.length() && paRes.nStartOfNextSentencePosition != lastSentenceStart) {
          lastSentenceStart = paRes.nStartOfNextSentencePosition;
          paRes.nStartOfSentencePosition = paRes.nStartOfNextSentencePosition;
          paRes.nStartOfNextSentencePosition = text.length();
          paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
          if (debugMode) {
            for (String id : lt.getDisabledRules()) {
              MessageHandler.printToLogFile("CheckDialog: getNextGrammatikErrorInParagraph: Dialog disabled rule: " + id);
            }
          }
          paRes = document.getCheckResults(text, locale, paRes, propertyValues, false, lt, nFPara, errType);
          SingleProofreadingError[] spellErrors = null;
          SingleProofreadingError[] allErrors = null;
          if (checkType != 3 && errType != LoErrorType.GRAMMAR) {
            spellErrors = getSpellErrors(nFPara, text, locale, document);
          }
          if (paRes == null || paRes.aErrors == null || paRes.aErrors.length == 0) {
            allErrors = spellErrors;
          } else if (spellErrors == null || spellErrors.length == 0) {
            allErrors = paRes.aErrors;
          } else {
            List<SingleProofreadingError[]> errorList = new ArrayList<>();
            errorList.add(spellErrors);
            errorList.add(paRes.aErrors);
            allErrors = document.mergeErrors(errorList, nFPara);
          }
          if (allErrors != null) {
            if (debugMode) {
              MessageHandler.printToLogFile("CheckDialog: getNextGrammatikErrorInParagraph: Number of Errors = " 
                  + paRes.aErrors.length + ", Paragraph: " + nFPara + ", Next Position: " + paRes.nStartOfNextSentencePosition
                  + ", Text.length: " + text.length());
            }
            for (SingleProofreadingError error : allErrors) {
              if (debugMode) {
                MessageHandler.printToLogFile("CheckDialog: getNextGrammatikErrorInParagraph: Start: " + error.nErrorStart + ", ID: " + error.aRuleIdentifier);
                if (error.nErrorType != TextMarkupType.SPELLCHECK) {
                  MessageHandler.printToLogFile("CheckDialog: getNextGrammatikErrorInParagraph: Test for correct spell: " 
                          + text.substring(error.nErrorStart, error.nErrorStart + error.nErrorLength));
                }
              }
              if (checkType != 3 && ((errType != LoErrorType.SPELL && error.nErrorType != TextMarkupType.SPELLCHECK)
                  || (errType != LoErrorType.GRAMMAR && error.nErrorType == TextMarkupType.SPELLCHECK
                  && !documents.getLinguisticServices().isCorrectSpell(text.substring(error.nErrorStart, error.nErrorStart + error.nErrorLength), locale)))
                 || (checkType == 3 && error.aRuleIdentifier.equals(checkRuleId)) ) {
                if (error.nErrorStart >= x) {
                  if ((error.aSuggestions == null || error.aSuggestions.length == 0) 
                      && documents.getLinguisticServices().isThesaurusRelevantRule(error.aRuleIdentifier)) {
                    error.aSuggestions = document.getSynonymArray(error, text, locale, lt, false);
                  } else if (error.nErrorType == TextMarkupType.SPELLCHECK) {
                    List<String> suggestionList = new ArrayList<>();
                    for (String suggestion : error.aSuggestions) {
                      suggestionList.add(suggestion);
                    }
                    String[] suggestions = documents.getLinguisticServices().getSpellAlternatives(text, locale);
                    if (suggestions != null) {
                      for (String suggestion : suggestions) {
                        if (!suggestionList.contains(suggestion)) {
                          suggestionList.add(suggestion);
                        }
                      }
                    }
                    error.aSuggestions = suggestionList.toArray(new String[0]);
                  }
                  return error;
                }
              }
            }
          }
        }
      }
    }
    return null;
  }
  
  /** 
   * Class for spell checking in LT check dialog
   * The LO/OO spell checker is used
   *//*
  private class ExtensionSpellChecker {

    private LinguisticServices linguServices;
     
    ExtensionSpellChecker() {
      linguServices = new LinguisticServices(xContext);
    }

    /**
     * replaces all words that matches 'word' with the string 'replace'
     * gives back a map of positions where a replace was done (for undo function)
     *//*
    public Map<Integer, List<Integer>> replaceAllWordsInText(String word, String replace, 
        DocumentCursorTools cursorTools, SingleDocument document, ViewCursorTools viewCursor) {
      if (word == null || replace == null || word.isEmpty() || replace.isEmpty() || word.equals(replace)) {
        return null;
      }
      Map<Integer, List<Integer>> replacePoints = new HashMap<Integer, List<Integer>>();
      try {
        int xVC = 0;
        TextParagraph yVC = null;
        if (docType == DocumentType.WRITER) {
          yVC = viewCursor.getViewCursorParagraph();
          xVC = viewCursor.getViewCursorCharacter();
        }
        for (int n = 0; n < docCache.size(); n++) {
          if (lt.isRemote()) {
            String text = docCache.getFlatParagraph(n);
            List<RuleMatch> matches = lt.check(text, true, ParagraphHandling.ONLYNONPARA, RemoteCheck.ONLY_SPELL);
            for (RuleMatch match : matches) {
              List<Integer> x;
              String matchWord = text.substring(match.getFromPos(), match.getToPos());
              if (matchWord.equals(word)) {
                changeTextOfParagraph(n, match.getFromPos(), word.length(), replace, document, viewCursor);
                if (replacePoints.containsKey(n)) {
                  x = replacePoints.get(n);
                } else {
                  x = new ArrayList<Integer>();
                }
                x.add(0, match.getFromPos());
                replacePoints.put(n, x);
                if (debugMode) {
                  MessageHandler.printToLogFile("CheckDialog: replaceAllWordsInText: add change undo: y = " + n + ", NumX = " + replacePoints.get(n).size());
                }
              }
            }
          } else {
            AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(docCache.getFlatParagraph(n));
            AnalyzedTokenReadings[] tokens = analyzedSentence.getTokensWithoutWhitespace();
            for (int i = tokens.length - 1; i >= 0 ; i--) {
              List<Integer> x ;
              if (tokens[i].getToken().equals(word)) {
                if (debugMode) {
                  MessageHandler.printToLogFile("CheckDialog: replaceAllWordsInText: change paragraph: y = " + n + ", word = " + tokens[i].getToken()  + ", replace = " + word);
                }
                changeTextOfParagraph(n, tokens[i].getStartPos(), word.length(), replace, document, viewCursor);
                if (replacePoints.containsKey(n)) {
                  x = replacePoints.get(n);
                } else {
                  x = new ArrayList<Integer>();
                }
                x.add(0, tokens[i].getStartPos());
                replacePoints.put(n, x);
                if (debugMode) {
                  MessageHandler.printToLogFile("CheckDialog: replaceAllWordsInText: add change undo: y = " + n + ", NumX = " + replacePoints.get(n).size());
                }
              }
            }
          }
        }
        if (docType == DocumentType.WRITER) {
          setTextViewCursor(xVC, yVC, viewCursor);
        }
      } catch (Throwable t) {
        MessageHandler.showError(t);
      }
      return replacePoints;
    }
    
    public LinguisticServices getLinguServices() {
      return linguServices;
    }

  }
*/
  /**
   * class to store the information for undo
   */
  private class UndoContainer {
    public int x;
    public int y;
    public boolean isSpellError;
    public String action;
    public String ruleId;
    public String word;
    public Map<Integer, List<Integer>> orgParas;
    public IgnoredMatches ignoredMatches;
    
    UndoContainer(int x, int y, boolean isSpellError, String action, String ruleId, String word, Map<Integer, 
        List<Integer>> orgParas, IgnoredMatches ignoredMatches) {
      this.x = x;
      this.y = y;
      this.isSpellError = isSpellError;
      this.action = action;
      this.ruleId = ruleId;
      this.orgParas = orgParas;
      this.word = word;
      this.ignoredMatches = ignoredMatches;
    }
  }

  /**
   * class contains the SingleProofreadingError and the locale of the match
   */
  private class CheckError {
    public Locale locale;
    public SingleProofreadingError error;
    
    CheckError(Locale locale, SingleProofreadingError error) {
      this.locale = locale;
      this.error = error;
    }
  }
  
  /**
   * class contains the SingleProofreadingError and the locale of the match
   */
  private class RuleIdentification {
    public String ruleId;
    public String ruleName;
    
    RuleIdentification (String ruleId, String ruleName) {
      this.ruleId = ruleId;
      this.ruleName = ruleName;
    }
  }
  
  /**
   * Class for dialog to check text for spell and grammar errors
   */
  public class LtCheckDialog implements ActionListener {
    private final static String ACORR_PREFIX = "acor_";
    private final static String ACORR_SUFFIX = ".dat";
    private final static int CHECK_TYPE_NUM = 4;
    private final static int maxUndos = 50;
    private final static int toolTipWidth = 300;
    
    private final static int dialogWidth = 640;
    private final static int dialogHeight = 600;
    private final static int MAX_ITEM_LENGTH = 28;
    private UndoMarkupContainer undoMarkup;

    private Color defaultForeground;

    private final JDialog dialog;
    private final Container contentPane;
    private final JLabel languageLabel;
    private final JComboBox<String> language;
    private final JComboBox<String> changeLanguage; 
    private final JTextArea errorDescription;
    private final JTextPane sentenceIncludeError;
    private final JLabel suggestionsLabel;
    private final JList<String> suggestions;
    private final JLabel checkTypeLabel;
    private final JLabel checkProgressLabel;
    private final JLabel cacheStatusLabel;
    private final ButtonGroup checkTypeGroup;
    private final JRadioButton[] checkTypeButtons;
    private final JButton more; 
    private final JButton ignoreOnce; 
    private final JButton ignorePermanent; 
    private final JButton resetIgnorePermanent; 
    private final JButton ignoreAll; 
    private final JButton deactivateRule;
    private final JComboBox<String> addToDictionary; 
    private final JComboBox<String> activateRule; 
    private final JComboBox<String> checkRuleBox; 
    private final JButton change; 
    private final JButton changeAll; 
    private final JButton autoCorrect; 
    private final JButton help; 
    private final JButton options; 
    private final JButton undo; 
    private final JButton close;
    private JProgressBar checkProgress;
    private final Image ltImage;
    private final List<UndoContainer> undoList;
    
    private SingleDocument currentDocument;
    private ViewCursorTools viewCursor;
    private SingleProofreadingError error;
    private List<RuleIdentification> allDifferentErrors;
    String docId;
    private String[] userDictionaries;
    private String informationUrl;
    private String lastLang = new String();
    private String endOfDokumentMessage;
    private int x = 0;
    private int y = 0;  //  current flat Paragraph
    private int startOfRange = -1;
    private int endOfRange = -1;
    private int lastX = 0;
    private int lastY = -1;
    private int lastPara = -1;
    private boolean isSpellError = false;
    private boolean focusLost = false;
    private boolean blockSentenceError = true;
    private boolean isDisposed = false;
    private boolean atWork = false;
    private boolean uncheckedParasLeft = false;

    private String wrongWord;
    private Locale locale;
    /**
     * the constructor of the class creates all elements of the dialog
     */
    public LtCheckDialog(XComponentContext xContext, SingleDocument document, WaitDialogThread inf) {
      long startTime = 0;
      if (debugModeTm) {
        startTime = System.currentTimeMillis();
      }
      ltImage = OfficeTools.getLtImage();
      if (!documents.isJavaLookAndFeelSet()) {
        documents.setJavaLookAndFeel();
      }
      
      currentDocument = document;
      
      undoList = new ArrayList<UndoContainer>();

      dialog = new JDialog();
      contentPane = dialog.getContentPane();
      languageLabel = new JLabel(labelLanguage);
      changeLanguage = new JComboBox<String> (changeLanguageList);
      language = new JComboBox<String>(getPossibleLanguages());
      errorDescription = new JTextArea();
      sentenceIncludeError = new JTextPane();
      suggestionsLabel = new JLabel(labelSuggestions);
      suggestions = new JList<String>();
      checkTypeLabel = new JLabel(Tools.getLabel(messages.getString("guiOOoCheckTypeLabel")));
      checkTypeButtons = new JRadioButton[CHECK_TYPE_NUM];
      checkTypeGroup = new ButtonGroup();
      help = new JButton (helpButtonName);
      options = new JButton (optionsButtonName);
      undo = new JButton (undoButtonName);
      close = new JButton (closeButtonName);
      more = new JButton (moreButtonName);
      ignoreOnce = new JButton (ignoreButtonName);
      ignorePermanent = new JButton (ignorePermanentButtonName);
      resetIgnorePermanent = new JButton (resetIgnorePermanentButtonName);
      ignoreAll = new JButton (ignoreAllButtonName);
      deactivateRule = new JButton (deactivateRuleButtonName);
      addToDictionary = new JComboBox<String> ();
      change = new JButton (changeButtonName);
      changeAll = new JButton (changeAllButtonName);
      autoCorrect = new JButton (autoCorrectButtonName);
      activateRule = new JComboBox<String> ();
      checkRuleBox = new JComboBox<String> ();
      checkProgressLabel = new JLabel(labelCheckProgress);
      cacheStatusLabel = new JLabel(" █ ");
      cacheStatusLabel.setToolTipText(messages.getString("loDialogCacheLabel"));
      checkProgress = new JProgressBar(0, 100);

      try {
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: LtCheckDialog: LtCheckDialog called");
        }
  
        
        if (dialog == null) {
          MessageHandler.printToLogFile("CheckDialog: LtCheckDialog: LtCheckDialog == null");
        }
        dialog.setName(dialogName);
        dialog.setTitle(dialogName + " (LanguageTool " + OfficeTools.getLtInformation() + ")");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        ((Frame) dialog.getOwner()).setIconImage(ltImage);
        defaultForeground = dialog.getForeground() == null ? Color.BLACK : dialog.getForeground();
  
        Font dialogFont = languageLabel.getFont();
        languageLabel.setFont(dialogFont);
  
        language.setFont(dialogFont);
        language.setToolTipText(formatToolTipText(languageHelp));
        language.addItemListener(e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            String selectedLang = (String) language.getSelectedItem();
            if (!lastLang.equals(selectedLang)) {
              changeLanguage.setEnabled(true);
            }
          }
        });
  
        changeLanguage.setFont(dialogFont);
        changeLanguage.setToolTipText(formatToolTipText(changeLanguageHelp));
        changeLanguage.addItemListener(e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (changeLanguage.getSelectedIndex() > 0) {
              Thread t = new Thread(new Runnable() {
                public void run() {
                  try {
                    Locale locale = null;
                    FlatParagraphTools flatPara= null;
                    setAtWorkButtonState();
                    String selectedLang = (String) language.getSelectedItem();
                    locale = getLocaleFromLanguageName(selectedLang);
                    flatPara = currentDocument.getFlatParagraphTools();
                    currentDocument.removeResultCache(y, true);
                    if (changeLanguage.getSelectedIndex() == 1) {
                      if (docType == DocumentType.IMPRESS) {
                        OfficeDrawTools.setLanguageOfParagraph(y, error.nErrorStart, error.nErrorLength, locale, currentDocument.getXComponent());
                      } else if (docType == DocumentType.CALC) {
                        OfficeSpreadsheetTools.setLanguageOfSpreadsheet(locale, currentDocument.getXComponent());
                      } else {
                        flatPara.setLanguageOfParagraph(y, error.nErrorStart, error.nErrorLength, locale);
                      }
                      addLanguageChangeUndo(y, error.nErrorStart, error.nErrorLength, lastLang);
                      docCache.setMultilingualFlatParagraph(y);
                    } else if (changeLanguage.getSelectedIndex() == 2) {
                      if (docType == DocumentType.IMPRESS) {
                        OfficeDrawTools.setLanguageOfParagraph(y, 0, docCache.getFlatParagraph(y).length(), locale, currentDocument.getXComponent());
                      } else if (docType == DocumentType.CALC) {
                        OfficeSpreadsheetTools.setLanguageOfSpreadsheet(locale, currentDocument.getXComponent());
                      } else {
                        flatPara.setLanguageOfParagraph(y, 0, docCache.getFlatParagraph(y).length(), locale);
                      }
                      docCache.setFlatParagraphLocale(y, locale);
                      addLanguageChangeUndo(y, 0, docCache.getFlatParagraph(y).length(), lastLang);
                    }
                    lastLang = selectedLang;
                    changeLanguage.setSelectedIndex(0);
                    gotoNextError();
                  } catch (Throwable t) {
                    MessageHandler.showError(t);
                    closeDialog();
                  }
                }
              });
              t.start();
            }
          }
        });
        changeLanguage.setSelectedIndex(0);
        changeLanguage.setEnabled(false);

        if (debugModeTm) {
          long runTime = System.currentTimeMillis() - startTime;
//          if (runTime > OfficeTools.TIME_TOLERANCE) {
            MessageHandler.printToLogFile("CheckDialog: Time to initialise Languages: " + runTime);
//          }
            startTime = System.currentTimeMillis();
        }
        if (inf.canceled()) {
          return;
        }

        errorDescription.setEditable(false);
        errorDescription.setLineWrap(true);
        errorDescription.setWrapStyleWord(true);
        errorDescription.setBackground(dialog.getContentPane().getBackground());
        errorDescription.setText(checkStatusInitialization);
        errorDescription.setForeground(Color.RED);
        Font descriptionFont = dialogFont.deriveFont(Font.BOLD);
        errorDescription.setFont(descriptionFont);
        errorDescription.setToolTipText(formatToolTipText(matchDescriptionHelp));
        JScrollPane descriptionPane = new JScrollPane(errorDescription);
        descriptionPane.setMinimumSize(new Dimension(0, 20));
  
        sentenceIncludeError.setFont(dialogFont);
        sentenceIncludeError.setToolTipText(formatToolTipText(matchParagraphHelp));
        sentenceIncludeError.getDocument().addDocumentListener(new DocumentListener() {
          @Override
          public void changedUpdate(DocumentEvent e) {
            if (!blockSentenceError) {
              if (!change.isEnabled()) {
                change.setEnabled(true);
              }
              if (changeAll.isEnabled()) {
                changeAll.setEnabled(false);
              }
              if (autoCorrect.isEnabled()) {
                autoCorrect.setEnabled(false);
              }
            }
          }
          @Override
          public void insertUpdate(DocumentEvent e) {
            changedUpdate(e);
          }
          @Override
          public void removeUpdate(DocumentEvent e) {
            changedUpdate(e);
          }
        });
        JScrollPane sentencePane = new JScrollPane(sentenceIncludeError);
        sentencePane.setMinimumSize(new Dimension(0, 30));
        
        suggestionsLabel.setFont(dialogFont);
  
        suggestions.setFont(dialogFont);
        suggestions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestions.setFixedCellHeight((int)(suggestions.getFont().getSize() * 1.2 + 0.5));
        suggestions.setToolTipText(formatToolTipText(suggestionsHelp));
        JScrollPane suggestionsPane = new JScrollPane(suggestions);
        suggestionsPane.setMinimumSize(new Dimension(0, 30));
  
        checkTypeLabel.setFont(dialogFont);
        checkTypeLabel.setToolTipText(formatToolTipText(checkTypeHelp));

        if (debugModeTm) {
          long runTime = System.currentTimeMillis() - startTime;
//          if (runTime > OfficeTools.TIME_TOLERANCE) {
            MessageHandler.printToLogFile("CheckDialog: Time to initialise suggestions, etc.: " + runTime);
//          }
            startTime = System.currentTimeMillis();
        }
        if (inf.canceled()) {
          return;
        }
        
        checkTypeButtons[0] = new JRadioButton(Tools.getLabel(messages.getString("guiOOoCheckAllButton")));
        checkTypeButtons[0].setSelected(true);
        checkTypeButtons[0].addActionListener(e -> {
          setAtWorkButtonState();
          checkType = 0;
          checkRuleBox.setEnabled(false);
          Thread t = new Thread(new Runnable() {
            public void run() {
              try {
                setAtWorkButtonState();
                gotoNextError();
              } catch (Throwable t) {
                MessageHandler.showError(t);
                closeDialog();
              }
            }
          });
          t.start();
        });
        checkTypeButtons[1] = new JRadioButton(Tools.getLabel(messages.getString("guiOOoCheckSpellingButton")));
        checkTypeButtons[1].addActionListener(e -> {
          setAtWorkButtonState();
          checkType = 1;
          checkRuleBox.setEnabled(false);
          Thread t = new Thread(new Runnable() {
            public void run() {
              try {
                setAtWorkButtonState();
                gotoNextError();
              } catch (Throwable t) {
                MessageHandler.showError(t);
                closeDialog();
              }
            }
          });
          t.start();
        });
        checkTypeButtons[2] = new JRadioButton(Tools.getLabel(messages.getString("guiOOoCheckGrammarButton")));
        checkTypeButtons[2].addActionListener(e -> {
          setAtWorkButtonState();
          checkType = 2;
          checkRuleBox.setEnabled(false);
          Thread t = new Thread(new Runnable() {
            public void run() {
              try {
                setAtWorkButtonState();
                gotoNextError();
              } catch (Throwable t) {
                MessageHandler.showError(t);
                closeDialog();
              }
            }
          });
          t.start();
        });
        checkTypeButtons[3] = new JRadioButton(Tools.getLabel(messages.getString("guiOOoCheckOnlyRuleButton")));
        checkTypeButtons[3].addActionListener(e -> {
          if (checkType != 3) {
            checkType = 3;
//            checkRuleId = null;
          }
          checkRuleBox.setEnabled(true);
        });
        for (int i = 0; i < CHECK_TYPE_NUM; i++) {
          checkTypeGroup.add(checkTypeButtons[i]);
          checkTypeButtons[i].setFont(dialogFont);
          checkTypeButtons[i].setToolTipText(formatToolTipText(checkTypeHelp));
        }
  
        help.setFont(dialogFont);
        help.addActionListener(this);
        help.setActionCommand("help");
        help.setToolTipText(formatToolTipText(helpButtonHelp));
        
        options.setFont(dialogFont);
        options.addActionListener(this);
        options.setActionCommand("options");
        options.setToolTipText(formatToolTipText(optionsButtonHelp));
        
        undo.setFont(dialogFont);
        undo.addActionListener(this);
        undo.setActionCommand("undo");
        undo.setToolTipText(formatToolTipText(undoButtonHelp));
        
        close.setFont(dialogFont);
        close.addActionListener(this);
        close.setActionCommand("close");
        close.setToolTipText(formatToolTipText(closeButtonHelp));
        
        more.setFont(dialogFont);
        more.addActionListener(this);
        more.setActionCommand("more");
        more.setToolTipText(formatToolTipText(moreButtonHelp));
        
        ignoreOnce.setFont(dialogFont);
        ignoreOnce.addActionListener(this);
        ignoreOnce.setActionCommand("ignoreOnce");
        ignoreOnce.setToolTipText(formatToolTipText(ignoreButtonHelp));
        
        ignorePermanent.setFont(dialogFont);
        ignorePermanent.addActionListener(this);
        ignorePermanent.setActionCommand("ignorePermanent");
        ignorePermanent.setToolTipText(formatToolTipText(ignorePermanentButtonHelp));
        
        resetIgnorePermanent.setFont(dialogFont);
        resetIgnorePermanent.addActionListener(this);
        resetIgnorePermanent.setActionCommand("resetIgnorePermanent");
        resetIgnorePermanent.setToolTipText(formatToolTipText(resetIgnorePermanentButtonHelp));
        
        ignoreAll.setFont(dialogFont);
        ignoreAll.addActionListener(this);
        ignoreAll.setActionCommand("ignoreAll");
        ignoreAll.setToolTipText(formatToolTipText(ignoreAllButtonHelp));
        
        deactivateRule.setFont(dialogFont);
        deactivateRule.setVisible(false);
        deactivateRule.addActionListener(this);
        deactivateRule.setActionCommand("deactivateRule");
        deactivateRule.setToolTipText(formatToolTipText(deactivateRuleButtonHelp));
        
        addToDictionary.setFont(dialogFont);
        addToDictionary.setToolTipText(formatToolTipText(addToDictionaryHelp));
        addToDictionary.addItemListener(e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            if (addToDictionary.getSelectedIndex() > 0) {
              Thread t = new Thread(new Runnable() {
                public void run() {
                  try {
                    setAtWorkButtonState();
                    String dictionary = (String) addToDictionary.getSelectedItem();
                    LtDictionary.addWordToDictionary(dictionary, wrongWord, xContext);
                    addUndo(y, "addToDictionary", dictionary, wrongWord);
                    addToDictionary.setSelectedIndex(0);
                    gotoNextError();
                  } catch (Throwable t) {
                    MessageHandler.showError(t);
                    closeDialog();
                  }
                }
              });
              t.start();
            }
          }
        });
        
        change.setFont(dialogFont);
        change.addActionListener(this);
        change.setActionCommand("change");
        change.setToolTipText(formatToolTipText(changeButtonHelp));
        
        changeAll.setFont(dialogFont);
        changeAll.addActionListener(this);
        changeAll.setActionCommand("changeAll");
        changeAll.setEnabled(false);
        changeAll.setToolTipText(formatToolTipText(changeAllButtonHelp));
  
        autoCorrect.setFont(dialogFont);
        autoCorrect.addActionListener(this);
        autoCorrect.setActionCommand("autoCorrect");
        autoCorrect.setEnabled(false);
        autoCorrect.setToolTipText(formatToolTipText(autoCorrectButtonHelp));
        
        activateRule.setFont(dialogFont);
        activateRule.setToolTipText(formatToolTipText(activateRuleButtonHelp));
        activateRule.setVisible(false);
        activateRule.addItemListener(e -> {
          if (e.getStateChange() == ItemEvent.SELECTED) {
            Thread t = new Thread(new Runnable() {
              public void run() {
                try {
                  int selectedIndex = activateRule.getSelectedIndex();
                  if (selectedIndex > 0) {
                    Map<String, String> deactivatedRulesMap = documents.getDisabledRulesMap(OfficeTools.localeToString(locale));
                    int j = 1;
                    for(String ruleId : deactivatedRulesMap.keySet()) {
                      if (j == selectedIndex) {
                        setAtWorkButtonState();
                        documents.activateRule(ruleId);
                        addUndo(y, "activateRule", ruleId, null);
                        activateRule.setSelectedIndex(0);
                        gotoNextError();
                      }
                      j++;
                    }
                  }
                } catch (Throwable t) {
                  MessageHandler.showError(t);
                  closeDialog();
                }
              }
            });
            t.start();
          }
        });
        
        checkRuleBox.setFont(dialogFont);
        checkRuleBox.setVisible(true);
        checkRuleBox.setEnabled(false);
        checkRuleBox.addItemListener(e -> {
          if (e.getStateChange() == ItemEvent.SELECTED && checkType == 3 && checkRuleBox.getItemCount() > 0) {
            String newRuleId = getRuleId((String) checkRuleBox.getSelectedItem(), allDifferentErrors);
            if (checkRuleId == null || !checkRuleId.equals(newRuleId)) {
              Thread t = new Thread(new Runnable() {
                public void run() {
                  try {
                    MessageHandler.printToLogFile("checkDialog: Set checkRuleId old: " + checkRuleId + ", new: " + newRuleId);
                    checkRuleId = newRuleId;
                    startOfRange = -1;
                    endOfRange = -1;
                    lastPara = -1;
                    gotoNextError();
                  } catch (Throwable t) {
                    MessageHandler.showError(t);
                    closeDialog();
                  }
                }
              });
              t.start();
            }
          }
        });
        
        dialog.addWindowFocusListener(new WindowFocusListener() {
          @Override
          public void windowGainedFocus(WindowEvent e) {
            if (focusLost && !atWork) {
              Thread t = new Thread(new Runnable() {
                public void run() {
                  try {
                    Point p = dialog.getLocation();
                    dialogX = p.x;
                    dialogY = p.y;
                    if (debugMode) {
                      MessageHandler.printToLogFile("CheckDialog: LtCheckDialog: Window Focus gained: Event = " + e.paramString());
                    }
                    setAtWorkButtonState();
                    currentDocument = getCurrentDocument(true);
                    if (currentDocument == null) {
                      closeDialog();
                      return;
                    }
                    String newDocId = currentDocument.getDocID();
                    if (debugMode) {
                      MessageHandler.printToLogFile("CheckDialog: LtCheckDialog: Window Focus gained: new docID = " + newDocId + ", old = " + docId + ", docType: " + docType);
                    }
                    if (!docId.equals(newDocId)) {
                      docId = newDocId;
                      undoList.clear();
                    }
                    if (!initCursor(false)) {
                      closeDialog();
                      return;
                    }
                    if (debugMode) {
                      MessageHandler.printToLogFile("CheckDialog: LtCheckDialog: cache refreshed - size: " + docCache.size());
                    }
                    gotoNextError();
                    focusLost = false;
                  } catch (Throwable t) {
                    MessageHandler.showError(t);
                    closeDialog();
                  }
                }
              });
              t.start();
            }
          }
          @Override
          public void windowLostFocus(WindowEvent e) {
            Thread t = new Thread(new Runnable() {
              public void run() {
                try {
                  if (debugMode) {
                    MessageHandler.printToLogFile("CheckDialog: LtCheckDialog: Window Focus lost: Event = " + e.paramString());
                  }
                  removeMarkups();
                  setAtWorkButtonState(atWork);
                  dialog.setEnabled(true);
                  focusLost = true;
                } catch (Throwable t) {
                  MessageHandler.showError(t);
                  closeDialog();
                }
              }
            });
            t.start();
          }
        });
        
        dialog.addWindowListener(new WindowListener() {
          @Override
          public void windowOpened(WindowEvent e) {
          }
          @Override
          public void windowClosing(WindowEvent e) {
            closeDialog();
          }
          @Override
          public void windowClosed(WindowEvent e) {
          }
          @Override
          public void windowIconified(WindowEvent e) {
          }
          @Override
          public void windowDeiconified(WindowEvent e) {
          }
          @Override
          public void windowActivated(WindowEvent e) {
          }
          @Override
          public void windowDeactivated(WindowEvent e) {
          }
        });
        
        checkProgress.setStringPainted(true);
        checkProgress.setIndeterminate(true);
  
        //  set selection background color to get compatible layout to LO
        Color selectionColor = UIManager.getLookAndFeelDefaults().getColor("ProgressBar.selectionBackground");
        suggestions.setSelectionBackground(selectionColor);
        setJComboSelectionBackground(language, selectionColor);
        setJComboSelectionBackground(changeLanguage, selectionColor);
        setJComboSelectionBackground(addToDictionary, selectionColor);
        setJComboSelectionBackground(activateRule, selectionColor);
  
        if (debugModeTm) {
          long runTime = System.currentTimeMillis() - startTime;
//          if (runTime > OfficeTools.TIME_TOLERANCE) {
            MessageHandler.printToLogFile("CheckDialog: Time to initialise Buttons: " + runTime);
//          }
            startTime = System.currentTimeMillis();
        }
        if (inf.canceled()) {
          return;
        }
        
        //  Define panels
  
        //  Define language panel
        JPanel languagePanel = new JPanel();
        languagePanel.setLayout(new GridBagLayout());
        GridBagConstraints cons11 = new GridBagConstraints();
        cons11.insets = new Insets(2, 2, 2, 2);
        cons11.gridx = 0;
        cons11.gridy = 0;
        cons11.anchor = GridBagConstraints.NORTHWEST;
        cons11.fill = GridBagConstraints.HORIZONTAL;
        cons11.weightx = 0.0f;
        cons11.weighty = 0.0f;
        languagePanel.add(languageLabel, cons11);
        cons11.gridx++;
        cons11.weightx = 1.0f;
        languagePanel.add(language, cons11);
  
        //  Define 1. right panel
        JPanel rightPanel1 = new JPanel();
        rightPanel1.setLayout(new GridBagLayout());
        GridBagConstraints cons21 = new GridBagConstraints();
        cons21.insets = new Insets(2, 0, 2, 0);
        cons21.gridx = 0;
        cons21.gridy = 0;
        cons21.anchor = GridBagConstraints.NORTHWEST;
        cons21.fill = GridBagConstraints.BOTH;
        cons21.weightx = 1.0f;
        cons21.weighty = 0.0f;
        cons21.gridy++;
        rightPanel1.add(ignoreOnce, cons21);
        cons21.gridy++;
        rightPanel1.add(ignorePermanent, cons21);
        cons21.gridy++;
        rightPanel1.add(ignoreAll, cons21);
        cons21.gridy++;
        rightPanel1.add(deactivateRule, cons21);
        rightPanel1.add(addToDictionary, cons21);
  
        //  Define 2. right panel
        JPanel rightPanel2 = new JPanel();
        rightPanel2.setLayout(new GridBagLayout());
        GridBagConstraints cons22 = new GridBagConstraints();
        cons22.insets = new Insets(2, 0, 2, 0);
        cons22.gridx = 0;
        cons22.gridy = 0;
        cons22.anchor = GridBagConstraints.NORTHWEST;
        cons22.fill = GridBagConstraints.BOTH;
        cons22.weightx = 1.0f;
        cons22.weighty = 0.0f;
        cons22.gridy++;
        cons22.gridy++;
        rightPanel2.add(change, cons22);
        cons22.gridy++;
        rightPanel2.add(changeAll, cons22);
        cons22.gridy++;
        rightPanel2.add(autoCorrect, cons22);
        rightPanel2.add(activateRule, cons22);
        cons22.gridy++;
        rightPanel2.add(resetIgnorePermanent, cons22);
        
        //  Define check type panel
        JPanel checkTypePanel = new JPanel();
        checkTypePanel.setLayout(new GridBagLayout());
        GridBagConstraints cons12 = new GridBagConstraints();
        cons12.insets = new Insets(2, 2, 2, 2);
        cons12.gridx = 0;
        cons12.gridy = 0;
        cons12.anchor = GridBagConstraints.NORTHWEST;
        cons12.fill = GridBagConstraints.HORIZONTAL;
        cons12.weightx = 0.0f;
        cons12.weighty = 0.0f;
        checkTypePanel.add(checkTypeLabel, cons12);
        JPanel checkTypePanel1 = new JPanel();
        checkTypePanel1.setLayout(new GridBagLayout());
        GridBagConstraints cons121 = new GridBagConstraints();
        cons121.insets = new Insets(2, 2, 2, 2);
        cons121.gridx = 0;
        cons121.gridy = 0;
        cons121.anchor = GridBagConstraints.NORTHWEST;
        cons121.fill = GridBagConstraints.HORIZONTAL;
        cons121.weightx = 1.0f;
        cons121.weighty = 0.0f;
        for (int i = 0; i < CHECK_TYPE_NUM - 1; i++) {
          checkTypePanel1.add(checkTypeButtons[i], cons121);
          cons121.gridx++;
        }
        cons12.weightx = 1.0f;
        cons12.gridx++;
        checkTypePanel.add(checkTypePanel1, cons12);
        JPanel checkTypePanel2 = new JPanel();
        checkTypePanel2.setLayout(new GridBagLayout());
        GridBagConstraints cons122 = new GridBagConstraints();
        cons122.insets = new Insets(2, 2, 2, 2);
        cons122.gridx = 0;
        cons122.gridy = 0;
        cons122.anchor = GridBagConstraints.NORTHWEST;
        cons122.fill = GridBagConstraints.HORIZONTAL;
        cons122.weightx = 0.0f;
        cons122.weighty = 0.0f;
        checkTypePanel2.add(checkTypeButtons[3], cons122);
        cons122.gridx++;
        cons122.weightx = 1.0f;
        checkTypePanel2.add(checkRuleBox, cons122);
        cons12.gridy++;
        checkTypePanel.add(checkTypePanel2, cons12);
        
        //  Define main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints cons1 = new GridBagConstraints();
        cons1.insets = new Insets(4, 4, 4, 4);
        cons1.gridx = 0;
        cons1.gridy = 0;
        cons1.anchor = GridBagConstraints.NORTHWEST;
        cons1.fill = GridBagConstraints.BOTH;
        cons1.weightx = 1.0f;
        cons1.weighty = 0.0f;
        mainPanel.add(languagePanel, cons1);
        cons1.weightx = 0.0f;
        cons1.gridx++;
        mainPanel.add(changeLanguage, cons1);
        cons1.gridx = 0;
        cons1.gridy++;
        cons1.weightx = 1.0f;
        cons1.weighty = 1.0f;
        mainPanel.add(descriptionPane, cons1);
        cons1.gridx++;
        cons1.weightx = 0.0f;
        cons1.weighty = 0.0f;
        mainPanel.add(more, cons1);
        cons1.gridx = 0;
        cons1.gridy++;
        cons1.weightx = 1.0f;
        cons1.weighty = 2.0f;
        mainPanel.add(sentencePane, cons1);
        cons1.gridx++;
        cons1.weightx = 0.0f;
        cons1.weighty = 0.0f;
        mainPanel.add(rightPanel1, cons1);
        cons1.gridx = 0;
        cons1.gridy++;
        cons1.weightx = 1.0f;
        mainPanel.add(suggestionsLabel, cons1);
        cons1.gridy++;
        cons1.weighty = 2.0f;
        mainPanel.add(suggestionsPane, cons1);
        cons1.gridx++;
        cons1.weightx = 0.0f;
        cons1.weighty = 0.0f;
        mainPanel.add(rightPanel2, cons1);
        cons1.gridx = 0;
        cons1.gridy++;
        cons1.weightx = 1.0f;
        cons1.weighty = 0.0f;
        mainPanel.add(checkTypePanel, cons1);
  
        //  Define general button panel
        JPanel generalButtonPanel = new JPanel();
        generalButtonPanel.setLayout(new GridBagLayout());
        GridBagConstraints cons3 = new GridBagConstraints();
        cons3.insets = new Insets(4, 4, 4, 4);
        cons3.gridx = 0;
        cons3.gridy = 0;
        cons3.anchor = GridBagConstraints.NORTHWEST;
        cons3.fill = GridBagConstraints.HORIZONTAL;
        cons3.weightx = 1.0f;
        cons3.weighty = 0.0f;
        generalButtonPanel.add(help, cons3);
        cons3.gridx++;
        generalButtonPanel.add(options, cons3);
        cons3.gridx++;
        generalButtonPanel.add(undo, cons3);
        cons3.gridx++;
        generalButtonPanel.add(close, cons3);
        
        //  Define check progress panel
        JPanel checkProgressPanel = new JPanel();
        checkProgressPanel.setLayout(new GridBagLayout());
        GridBagConstraints cons4 = new GridBagConstraints();
        cons4.insets = new Insets(4, 4, 4, 4);
        cons4.gridx = 0;
        cons4.gridy = 0;
        cons4.anchor = GridBagConstraints.NORTHWEST;
        cons4.fill = GridBagConstraints.HORIZONTAL;
        cons4.weightx = 0.0f;
        cons4.weighty = 0.0f;
        checkProgressPanel.add(cacheStatusLabel, cons4);
        cons4.gridx++;
        cons4.weightx = 0.0f;
        cons4.weighty = 0.0f;
        checkProgressPanel.add(checkProgressLabel, cons4);
        cons4.gridx++;
        cons4.weightx = 4.0f;
        checkProgressPanel.add(checkProgress, cons4);
  
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints cons = new GridBagConstraints();
        cons.insets = new Insets(8, 8, 8, 8);
        cons.gridx = 0;
        cons.gridy = 0;
        cons.anchor = GridBagConstraints.NORTHWEST;
        cons.fill = GridBagConstraints.BOTH;
        cons.weightx = 1.0f;
        cons.weighty = 1.0f;
        contentPane.add(mainPanel, cons);
        cons.gridy++;
        cons.weighty = 0.0f;
        contentPane.add(generalButtonPanel, cons);
        cons.gridy++;
        contentPane.add(checkProgressPanel, cons);
  
        if (debugModeTm) {
          long runTime = System.currentTimeMillis() - startTime;
//          if (runTime > OfficeTools.TIME_TOLERANCE) {
            MessageHandler.printToLogFile("CheckDialog: Time to initialise panels: " + runTime);
//          }
            startTime = System.currentTimeMillis();
        }
        if (inf.canceled()) {
          return;
        }

        dialog.pack();
        // center on screen:
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = new Dimension(dialogWidth, dialogHeight);
        dialog.setSize(frameSize);
        dialog.setLocation(screenSize.width / 2 - frameSize.width / 2,
            screenSize.height / 2 - frameSize.height / 2);
        dialog.setLocationByPlatform(true);
        
        ToolTipManager.sharedInstance().setDismissDelay(30000);
        if (debugModeTm) {
          long runTime = System.currentTimeMillis() - startTime;
//          if (runTime > OfficeTools.TIME_TOLERANCE) {
            MessageHandler.printToLogFile("CheckDialog: Time to initialise dialog size: " + runTime);
//          }
        }
      } catch (Throwable t) {
        MessageHandler.showError(t);
        closeDialog();
      }
    }
    
    /**
     * Set the selection color to a combo box
     */
    private void setJComboSelectionBackground(JComboBox<String> comboBox, Color color) {
      Object context = comboBox.getAccessibleContext().getAccessibleChild(0);
      BasicComboPopup popup = (BasicComboPopup)context;
      JList<Object> list = popup.getList();
      list.setSelectionBackground(color);
    }

    /**
     * Set the Progress value for the progress bar
     * The checked number of paragraphs and the percent of checked of paragraphs are printed
     */
    void setProgressValue(int value, boolean setText) {
      int max = checkProgress.getMaximum();
      int val = value < 0 ? 1 : value + 1;
      if (val > max) {
        val = max;
      }
      checkProgress.setValue(val);
      if (setText) {
        int p = (int) (((val * 100) / max) + 0.5);
        checkProgress.setString(p + " %  ( " + val + " / " + max + " )");
        checkProgress.setStringPainted(true);
      }
    }
    
    /**
     * Set Color of cache status label
     * red if cache not filled green for full cache
     */
    void setCacheStatusColor() {
      int fullSize = docCache.size();
      int nSingle = 0;
      int nAuto = 0;
      for (int i = 0; i < docCache.size(); i++) {
        if (docCache.isAutomaticGenerated(i, true)) {
          nAuto++;
        } else if (docCache.isSingleParagraph(i)) {
          nSingle++;
        }
      }
      int pSize = 0;
      int numCache = 0;
      for (int i = 0; i < currentDocument.getParagraphsCache().size(); i++) {
        if (documents.isSortedRuleForIndex(i)) {
          pSize += (currentDocument.getParagraphsCache().get(i).size() + nAuto);
          if (i > 0) {
            pSize += nSingle;
          }
          numCache++;
        }
      }
      int size;
      if (docType == DocumentType.WRITER) {
        size = (fullSize == 0 || numCache == 0) ? 0 : (pSize * 100) / (fullSize * numCache);
        if (size < 0) {
          size = 0;
        } else if (size > 100) {
          size = 100;
        }
      } else {
        size = 100;
      }
      cacheStatusLabel.setToolTipText(messages.getString("loDialogCacheLabel") + ": " + size + "%");
      if (size < 25) {
        cacheStatusLabel.setForeground(new Color(fitToColorboundaries(145 + 4 * size), 0, 0));
      } else if (size < 50) {
        cacheStatusLabel.setForeground(new Color(255, fitToColorboundaries(5 + 4 * size), 0));
      } else if (size < 75) {
        cacheStatusLabel.setForeground(new Color(fitToColorboundaries(255 - 4 * (size - 25)), 255, 0));
      } else {
        cacheStatusLabel.setForeground(new Color(0, fitToColorboundaries(255 - 4 * (size - 70)), 0));
      }
    }

    private int fitToColorboundaries(int col) {
      if (col < 0 || col > 255) {
        return 255;
      }
      return col;
    }
    
    void errorReturn() {
      MessageHandler.showMessage(loBusyMessage);
      closeDialog();
    }

    /**
     * show the dialog
     * @throws Throwable 
     */
    public void show() throws Throwable {
      if (debugMode) {
        MessageHandler.printToLogFile("CheckDialog: show: Goto next Error");
      }
      if (dialogX < 0 || dialogY < 0) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = dialog.getSize();
        dialogX = screenSize.width / 2 - frameSize.width / 2;
        dialogY = screenSize.height / 2 - frameSize.height / 2;
      }
      dialog.setLocation(dialogX, dialogY);
      dialog.setAutoRequestFocus(true);
      dialog.setVisible(true);
      Thread t = new Thread(new Runnable() {
        public void run() {
          try {
            long startTime = 0;
            if (debugModeTm) {
              startTime = System.currentTimeMillis();
            }
            setAtWorkButtonState();
            dialog.toFront();
/*            
            currentDocument = getCurrentDocument(false);
            if (currentDocument == null || docCache == null || docCache.size() <= 0) {
              errorReturn();
              return;
            }
*/
            docId = currentDocument.getDocID();
/*            
            if (spellChecker == null) {
              spellChecker = new ExtensionSpellChecker();
            }
*/
            if (lt == null || !documents.isCheckImpressDocument()) {
              setLangTool(currentDocument, lastLanguage);
            }
            setUserDictionaries();
            for (String dic : userDictionaries) {
              addToDictionary.addItem(dic);
            }
            if (!initCursor(true)) {
              errorReturn();
              return;
            }
            if (debugModeTm) {
              long runTime = System.currentTimeMillis() - startTime;
//              if (runTime > OfficeTools.TIME_TOLERANCE) {
                MessageHandler.printToLogFile("CheckDialog: Time to initialise run: " + runTime);
//              }
            }
            setCacheStatusColor();
            gotoNextError(false);
          } catch (Throwable t) {
            MessageHandler.showError(t);
            closeDialog();
          }
        }
      });
      t.start();
    }

    /**
     * Initialize the cursor / define the range for check
     * @throws Throwable 
     */
    private boolean initCursor(boolean isStart) throws Throwable {
      if (docType == DocumentType.WRITER) {
        viewCursor = new ViewCursorTools(currentDocument.getXComponent());
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: initCursor: viewCursor initialized: docId: " + docId);
        }
        XTextCursor tCursor = viewCursor.getTextCursorBeginn();
        if (tCursor != null) {
          tCursor.gotoStart(true);
          int nBegin = tCursor.getString().length();
          tCursor = viewCursor.getTextCursorEnd();
          tCursor.gotoStart(true);
          int nEnd = tCursor.getString().length();
          if (nBegin < nEnd) {
            startOfRange = viewCursor.getViewCursorCharacter();
            endOfRange = nEnd - nBegin + startOfRange;
            lastX = 0;
            lastY = -1;
          } else {
            startOfRange = -1;
            endOfRange = -1;
          }
        } else {
          closeDialog();
          MessageHandler.showClosingInformationDialog(messages.getString("loDialogUnsupported"));
          return false;
        }
      } else {
        startOfRange = -1;
        endOfRange = -1;
      }
      if (isStart || endOfRange > 0) {
        lastPara = -1;
      }
      return true;
    }

    /**
     * get all different kinds of errors
     */
    List<RuleIdentification> getAllDifferentErrors() {
      List<RuleIdentification> errors = new ArrayList<>();
      List<Rule> allRules = lt.getAllRules();
      for (int nFPara = 0; nFPara < docCache.size(); nFPara++) {
        for (int cacheNum = 0; cacheNum < documents.getNumMinToCheckParas().size(); cacheNum++) {
          if (!docCache.isAutomaticGenerated(nFPara, true) && (cacheNum == 0 || (documents.isSortedRuleForIndex(cacheNum) 
                                  && !currentDocument.getDocumentCache().isSingleParagraph(nFPara)))) {
            SingleProofreadingError[] pErrors = currentDocument.getParagraphsCache().get(cacheNum).getSafeMatches(nFPara);
            if (pErrors != null) {
              for (SingleProofreadingError pError : pErrors) {
                if (pError.nErrorType != TextMarkupType.SPELLCHECK) {
                  boolean toAdd = true;
                  for (RuleIdentification error : errors) {
                    if (pError.aRuleIdentifier.equals(error.ruleId)) {
                      toAdd = false;
                      break;
                    }
                  }
                  if (toAdd) {
                    for (Rule rule : allRules) {
                      if (rule.getId().equals(pError.aRuleIdentifier)) {
                        int n = 0;
                        for (RuleIdentification error : errors) {
                          if (rule.getDescription().compareToIgnoreCase(error.ruleName) < 0) {
                            break;
                          }
                          n++;
                        }
                        errors.add(n, new RuleIdentification(pError.aRuleIdentifier, rule.getDescription()));
                        break;
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
      return errors;
    }
    
    /**
     * get the name of a rule by Id out of a list of RuleIdentification
     */
    String getRuleName (String ruleId, List<RuleIdentification> rules) {
      for (RuleIdentification rule : rules) {
        if (ruleId.equals(rule.ruleId)) {
          return rule.ruleName;
        }
      }
      return null;
    }
    
    /**
     * get the ID of a rule by name out of a list of RuleIdentification
     */
    String getRuleId (String ruleName, List<RuleIdentification> rules) {
      for (RuleIdentification rule : rules) {
        if (ruleName.equals(rule.ruleName)) {
          return rule.ruleId;
        }
      }
      return null;
    }
    
    /**
     * Formats the tooltip text
     * The text is given by a text string which is formatted into html:
     * \n are formatted to html paragraph breaks
     * \n- is formatted to an unordered List
     * \n1. is formatted to an ordered List (every digit 1 - 9 is allowed 
     */
    private String formatToolTipText(String Text) {
      String toolTipText = Text;
      int breakIndex = 0;
      int isNum = 0;
      while (breakIndex >= 0) {
        breakIndex = toolTipText.indexOf("\n", breakIndex);
        if (breakIndex >= 0) {
          int nextNonBlank = breakIndex + 1;
          while (' ' == toolTipText.charAt(nextNonBlank)) {
            nextNonBlank++;
          }
          if (isNum == 0) {
            if (toolTipText.charAt(nextNonBlank) == '-') {
              toolTipText = toolTipText.substring(0, breakIndex) + "</p><ul width=\"" 
                  + toolTipWidth + "\"><li>" + toolTipText.substring(nextNonBlank + 1);
              isNum = 1;
            } else if (toolTipText.charAt(nextNonBlank) >= '1' && toolTipText.charAt(nextNonBlank) <= '9' 
                            && toolTipText.charAt(nextNonBlank + 1) == '.') {
              toolTipText = toolTipText.substring(0, breakIndex) + "</p><ol width=\"" 
                  + toolTipWidth + "\"><li>" + toolTipText.substring(nextNonBlank + 2);
              isNum = 2;
            } else {
              toolTipText = toolTipText.substring(0, breakIndex) + "</p><p width=\"" 
                              + toolTipWidth + "\">" + toolTipText.substring(breakIndex + 1);
            }
          } else if (isNum == 1) {
            if (toolTipText.charAt(nextNonBlank) == '-') {
              toolTipText = toolTipText.substring(0, breakIndex) + "</li><li>" + toolTipText.substring(nextNonBlank + 1);
            } else {
              toolTipText = toolTipText.substring(0, breakIndex) + "</li></ul><p width=\"" 
                  + toolTipWidth + "\">" + toolTipText.substring(breakIndex + 1);
              isNum = 0;
            }
          } else {
            if (toolTipText.charAt(nextNonBlank) >= '1' && toolTipText.charAt(nextNonBlank) <= '9' 
                && toolTipText.charAt(nextNonBlank + 1) == '.') {
              toolTipText = toolTipText.substring(0, breakIndex) + "</li><li>" + toolTipText.substring(nextNonBlank + 2);
            } else {
              toolTipText = toolTipText.substring(0, breakIndex) + "</li></ol><p width=\"" 
                  + toolTipWidth + "\">" + toolTipText.substring(breakIndex + 1);
              isNum = 0;
            }
          }
        }
      }
      if (isNum == 0) {
        toolTipText = "<html><div style='color:black;'><p width=\"" + toolTipWidth + "\">" + toolTipText + "</p></html>";
      } else if (isNum == 1) {
        toolTipText = "<html><div style='color:black;'><p width=\"" + toolTipWidth + "\">" + toolTipText + "</ul></html>";
      } else {
        toolTipText = "<html><div style='color:black;'><p width=\"" + toolTipWidth + "\">" + toolTipText + "</ol></html>";
      }
      return toolTipText;
    }
    
    /**
     * Initial button state
     */
    private void setAtWorkButtonState() {
      setAtWorkButtonState(true);
    }
    
    private void setAtWorkButtonState(boolean work) {
      checkProgress.setIndeterminate(true);
      ignoreOnce.setEnabled(false);
      ignorePermanent.setEnabled(false);
      resetIgnorePermanent.setEnabled(false);
      ignoreAll.setEnabled(false);
      deactivateRule.setEnabled(false);
      change.setEnabled(false);
      changeAll.setEnabled(false);
      autoCorrect.setEnabled(false);
      addToDictionary.setEnabled(false);
      more.setEnabled(false);
      help.setEnabled(false);
      options.setEnabled(false);
      undo.setEnabled(false);
      close.setEnabled(false);
      language.setEnabled(false);
      changeLanguage.setEnabled(false);
      activateRule.setEnabled(false);
      checkRuleBox.setEnabled(false);
      endOfDokumentMessage = null;
      sentenceIncludeError.setBackground(Color.LIGHT_GRAY);
      sentenceIncludeError.setEnabled(false);
      suggestions.setBackground(Color.LIGHT_GRAY);
      suggestions.setEnabled(false);
      errorDescription.setText(checkStatusCheck);
      errorDescription.setForeground(Color.RED);
      errorDescription.setBackground(Color.LIGHT_GRAY);
      contentPane.revalidate();
      contentPane.repaint();
      dialog.setEnabled(false);
      atWork = work;
    }
    
    /**
     * find the next match
     * set the view cursor to the position of match
     * fill the elements of the dialog with the information of the match
     */
    private void gotoNextError() {
      gotoNextError(true);
    }

    private void gotoNextError(boolean startAtBegin) {
      try {
        if (!documents.isEnoughHeapSpace()) {
          closeDialog();
          return;
        }
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: findNextError: start getNextError");
        }
        removeMarkups();
        if (checkType == 3) {
          if (checkRuleId == null) {
            if (checkRuleBox.getItemCount() > 0) {
              checkRuleId = getRuleId((String) checkRuleBox.getSelectedItem(), allDifferentErrors);
            } else {
              checkTypeButtons[0].setSelected(true);
              checkType = 0;
              checkRuleBox.setEnabled(false);
            }
          }
        }
        CheckError checkError = getNextError(startAtBegin);
        if (isDisposed) {
          return;
        }
        OfficeTools.waitForLO();  //  wait to end all LO related process to prevent LO hang up
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: findNextError: Error is " + (checkError == null ? "Null" : "NOT Null"));
        }
        error = checkError == null ? null : checkError.error;
        locale = checkError == null ? null : checkError.locale;
        
        dialog.setEnabled(true);
        checkProgress.setIndeterminate(false);
        help.setEnabled(true);
        options.setEnabled(true);
        close.setEnabled(true);
        resetIgnorePermanent.setEnabled(true);
        if (sentenceIncludeError == null || errorDescription == null || suggestions == null) {
          MessageHandler.printToLogFile("CheckDialog: findNextError: SentenceIncludeError == null || errorDescription == null || suggestions == null");
          error = null;
        }
        
        setCacheStatusColor();
        if (error != null) {
//          isSpellError = error.aRuleIdentifier.equals(spellRuleId);
          isSpellError = error.nErrorType == TextMarkupType.SPELLCHECK;
          blockSentenceError = true;
          sentenceIncludeError.setEnabled(true);
          sentenceIncludeError.setBackground(Color.white);
          sentenceIncludeError.setText(docCache.getFlatParagraph(y));
          setAttributesForErrorText(error);
          blockSentenceError = false;
          errorDescription.setEnabled(true);
          errorDescription.setText(error.aFullComment);
          errorDescription.setForeground(defaultForeground);
          errorDescription.setBackground(Color.white);
          ignoreOnce.setEnabled(true);
          ignoreAll.setEnabled(true);
          if (debugMode) {
            MessageHandler.printToLogFile("CheckDialog: findNextError: Error Text set");
          }
          if (error.aSuggestions != null && error.aSuggestions.length > 0) {
            suggestions.setEnabled(true);
            suggestions.setListData(error.aSuggestions);
            suggestions.setSelectedIndex(0);
            suggestions.setBackground(Color.white);
            change.setEnabled(true);
            changeAll.setEnabled(true);
            autoCorrect.setEnabled(true);
          } else {
            suggestions.setEnabled(true);
            suggestions.setListData(new String[0]);
            change.setEnabled(false);
            changeAll.setEnabled(true);
            autoCorrect.setEnabled(false);
          }
          if (debugMode) {
            MessageHandler.printToLogFile("CheckDialog: findNextError: Suggestions set");
          }
          Language lang = locale == null ? lt.getLanguage() : MultiDocumentsHandler.getLanguage(locale);
          if (lang == null) {
            lang = lt.getLanguage();
          }
          if (debugMode && lt.getLanguage() == null) {
            MessageHandler.printToLogFile("CheckDialog: findNextError: LT language == null");
          }
          lastLang = lang.getTranslatedName(messages);
          language.setEnabled(true);
          language.setSelectedItem(lang.getTranslatedName(messages));
          if (debugMode) {
            MessageHandler.printToLogFile("CheckDialog: findNextError: Language set");
          }
          
          allDifferentErrors = getAllDifferentErrors();
          if (allDifferentErrors.size() != checkRuleBox.getItemCount()) {
            checkRuleBox.removeAllItems();
            for (RuleIdentification error : allDifferentErrors) {
              checkRuleBox.addItem(error.ruleName);
            }
          }
          if (checkType == 3) {
            if (checkRuleId != null) {
              checkRuleBox.setSelectedItem(getRuleName(checkRuleId, allDifferentErrors));
            }
            checkRuleBox.setEnabled(true);
          }
          
          Map<String, String> deactivatedRulesMap = documents.getDisabledRulesMap(OfficeTools.localeToString(locale));
          if (!isSpellError && !deactivatedRulesMap.isEmpty()) {
            activateRule.removeAllItems();
            activateRule.addItem(messages.getString("loContextMenuActivateRule"));
            for (String ruleId : deactivatedRulesMap.keySet()) {
              String tmpStr = deactivatedRulesMap.get(ruleId);
              if (tmpStr.length() > MAX_ITEM_LENGTH) {
                tmpStr = tmpStr.substring(0, MAX_ITEM_LENGTH - 3) + "...";
              }
              activateRule.addItem(tmpStr);
            }
            activateRule.setVisible(true);
            activateRule.setEnabled(true);
            autoCorrect.setVisible(false);
          } else {
            activateRule.setVisible(false);
            autoCorrect.setVisible(true);
            autoCorrect.setEnabled(false);
          }
          
          if (isSpellError) {
            ignoreAll.setText(ignoreAllButtonName);
            addToDictionary.setVisible(true);
            deactivateRule.setVisible(false);
            addToDictionary.setEnabled(true);
            changeAll.setEnabled(true);
            autoCorrect.setEnabled(true);
            ignorePermanent.setEnabled(true);
          } else {
            ignoreAll.setText(ignoreRuleButtonName);
            addToDictionary.setVisible(false);
            changeAll.setEnabled(true);
            deactivateRule.setVisible(true);
            deactivateRule.setEnabled(true);
            autoCorrect.setEnabled(false);
            ignorePermanent.setEnabled(true);
          }
          informationUrl = getUrl(error);
          more.setEnabled(informationUrl != null);
          undo.setEnabled(undoList != null && !undoList.isEmpty());
          if (debugMode) {
            MessageHandler.printToLogFile("CheckDialog: findNextError: All set");
          }
        } else {
          language.setEnabled(true);
          Language lang = locale == null || !MultiDocumentsHandler.hasLocale(locale)? lt.getLanguage() : MultiDocumentsHandler.getLanguage(locale);
          language.setSelectedItem(lang.getTranslatedName(messages));
          language.setEnabled(false);
          more.setEnabled(false);
          ignoreOnce.setEnabled(false);
          ignoreAll.setEnabled(false);
          addToDictionary.setVisible(true);
          addToDictionary.setEnabled(false);
          deactivateRule.setVisible(false);
          changeAll.setEnabled(false);
          activateRule.setVisible(false);
          autoCorrect.setVisible(true);
          autoCorrect.setEnabled(false);
          focusLost = false;
          suggestions.setEnabled(true);;
          suggestions.setListData(new String[0]);
          undo.setEnabled(undoList != null && !undoList.isEmpty());
          errorDescription.setEnabled(true);
          errorDescription.setForeground(Color.RED);
          errorDescription.setText(endOfDokumentMessage == null ? " " : endOfDokumentMessage);
          errorDescription.setBackground(Color.white);
          sentenceIncludeError.setEnabled(false);
//          sentenceIncludeError.setText(" ");
          errorDescription.setEnabled(true);
          change.setEnabled(false);
          if (docCache.size() > 0) {
            locale = docCache.getFlatParagraphLocale(docCache.size() - 1);
          }
          sentenceIncludeError.setEnabled(false);
        }
      } catch (Throwable e) {
        MessageHandler.showError(e);
        closeDialog();
      }
      atWork = false;
    }

    /**
     * stores the list of local dictionaries into the dialog element
     */
    private void setUserDictionaries () {
      String[] tmpDictionaries = LtDictionary.getUserDictionaries(xContext);
      userDictionaries = new String[tmpDictionaries.length + 1];
      userDictionaries[0] = addToDictionaryName;
      for (int i = 0; i < tmpDictionaries.length; i++) {
        String tmpStr = tmpDictionaries[i];
        if (tmpStr.length() > MAX_ITEM_LENGTH) {
          tmpStr = tmpStr.substring(0, MAX_ITEM_LENGTH - 3) + "...";
        }
        userDictionaries[i + 1] = tmpStr;
      }
    }

    /**
     * returns an array of the translated names of the languages supported by LT
     */
    private String[] getPossibleLanguages() {
      List<String> languages = new ArrayList<>();
      for (Language lang : Languages.get()) {
        languages.add(lang.getTranslatedName(messages));
        languages.sort(null);
      }
      languages.add(0, messages.getString("loDialogDoNotCheck"));
      return languages.toArray(new String[languages.size()]);
    }

    /**
     * returns the locale from a translated language name 
     */
    private Locale getLocaleFromLanguageName(String translatedName) {
      if (translatedName.equals(messages.getString("loDialogDoNotCheck"))) {
        return new Locale(OfficeTools.IGNORE_LANGUAGE, "", "");
      }
      for (Language lang : Languages.get()) {
        if (translatedName.equals(lang.getTranslatedName(messages))) {
          return (LinguisticServices.getLocale(lang));
        }
      }
      return null;
    }

    /**
     * set the attributes for the text inside the editor element
     */
    private void setAttributesForErrorText(SingleProofreadingError error) {
      //  Get Attributes
      sentenceIncludeError.setEnabled(true);
      MutableAttributeSet attrs = sentenceIncludeError.getInputAttributes();
      StyledDocument doc = sentenceIncludeError.getStyledDocument();
      //  Set back to default values
      StyleConstants.setBold(attrs, false);
      StyleConstants.setUnderline(attrs, false);
      StyleConstants.setForeground(attrs, defaultForeground);
      doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, true);
      //  Set values for error
      StyleConstants.setBold(attrs, true);
      StyleConstants.setUnderline(attrs, true);
      Color color = null;
      if (isSpellError) {
        color = Color.RED;
      } else {
        PropertyValue[] properties = error.aProperties;
        for (PropertyValue property : properties) {
          if ("LineColor".equals(property.Name)) {
            color = new Color((int) property.Value);
            break;
          }
        }
        if (color == null) {
          color = Color.BLUE;
        }
      }
      StyleConstants.setForeground(attrs, color);
      doc.setCharacterAttributes(error.nErrorStart, error.nErrorLength, attrs, true);
    }

    /**
     * returns the URL to more information of match
     * returns null, if such an URL does not exist
     */
    private String getUrl(SingleProofreadingError error) {
      if (!isSpellError) {
        PropertyValue[] properties = error.aProperties;
        for (PropertyValue property : properties) {
          if ("FullCommentURL".equals(property.Name)) {
            String url = new String((String) property.Value);
            return url;
          }
        }
      }
      return null;
    }

   /**
     * returns the next match
     * starting at the current cursor position
     * @throws Throwable 
     */
    private CheckError getNextError(boolean startAtBegin) throws Throwable {
      if (docType != DocumentType.WRITER) {
        currentDocument = getCurrentDocument(false);
      }
      if (currentDocument == null) {
        MessageHandler.printToLogFile("CheckDialog: getNextError: currentDocument == null: close dialog");
        MessageHandler.showMessage(messages.getString("loDialogErrorCloseMessage"));
        closeDialog();
        return null;
      }
      XComponent xComponent = currentDocument.getXComponent();
      DocumentCursorTools docCursor = new DocumentCursorTools(xComponent);
      if (docCache.size() <= 0) {
        MessageHandler.printToLogFile("CheckDialog: getNextError: docCache size == 0: close dialog");
        MessageHandler.showMessage(messages.getString("loDialogErrorCloseMessage"));
        closeDialog();
        return null;
      }
      int nWhile = 0;
      while (nWhile < DIALOG_LOOPS) {
        if (debugMode) {
          MessageHandler.printToLogFile("\nCheckDialog: getNextError: Loop: " + nWhile + "\n");
        }
        if (uncheckedParasLeft) {
          uncheckedParasLeft = false;
        } else {
          hasUncheckedParas = false;
        }
        if (docType == DocumentType.WRITER) {
          if (nWhile > 0 && hasUncheckedParas && endOfRange < 0) {
            y = lastPara;
          } else {
            TextParagraph tPara = viewCursor.getViewCursorParagraph();
            y = docCache.getFlatParagraphNumber(tPara);
          }
        } else if (docType == DocumentType.IMPRESS) {
          y = OfficeDrawTools.getParagraphFromCurrentPage(xComponent);
        } else {
          y = OfficeSpreadsheetTools.getParagraphFromCurrentSheet(xComponent);
        }
        if (y < 0 || y >= docCache.size()) {
          MessageHandler.printToLogFile("CheckDialog: getNextError: y (= " + y + ") >= text size (= " + docCache.size() + "): close dialog");
          MessageHandler.showMessage(messages.getString("loDialogErrorCloseMessage"));
          closeDialog();
          return null;
        }
        if (lastPara < 0) {
          lastPara = y;
        }
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: getNextError: (x/y): (" + x + "/" + y + ") < text size (= " + docCache.size() + ")");
        }
        if (endOfRange >= 0 && y == lastPara) {
          x = startOfRange;
        } else {
          x = 0;
        }
        int nStart = 0;
        for (int i = lastPara; i < y && i < docCache.size(); i++) {
          nStart += docCache.getFlatParagraph(i).length() + 1;
        }
        checkProgress.setMaximum(endOfRange < 0 ? docCache.size() : endOfRange);
        CheckError nextError = null;
        while (y < docCache.size() && y >= lastPara && nextError == null && (endOfRange < 0 || nStart < endOfRange)) {
          setProgressValue(endOfRange < 0 ? y - lastPara : nStart + (lastY == y ? lastX : 0), endOfRange < 0);
          nextError = getNextErrorInParagraph (x, y, currentDocument, docCursor, true);
          if(isDisposed) {
            return null;
          }
          if (debugMode) {
            MessageHandler.printToLogFile("CheckDialog: getNextError: endOfRange = " + endOfRange + ", startOfRange = " 
                  + startOfRange + ", nStart = " + nStart + ", lastX = " + lastX + ", lastPara = " + lastPara);
          }
          int pLength = docCache.getFlatParagraph(y).length() + 1;
          nStart += pLength;
          if (nextError != null && (endOfRange < 0 || nStart - pLength + nextError.error.nErrorStart < endOfRange)) {
  //          if (nextError.error.aRuleIdentifier.equals(spellRuleId)) {
            if (nextError.error.nErrorType == TextMarkupType.SPELLCHECK) {
              wrongWord = docCache.getFlatParagraph(y).substring(nextError.error.nErrorStart, 
                  nextError.error.nErrorStart + nextError.error.nErrorLength);
            }
            if (debugMode) {
              MessageHandler.printToLogFile("CheckDialog: getNextError: endOfRange: " + endOfRange + "; ErrorStart(" + nStart 
                  + "/" + pLength + "/" 
                  + nextError.error.nErrorStart + "): " + (nStart - pLength + nextError.error.nErrorStart));
              MessageHandler.printToLogFile("CheckDialog: getNextError: x: " + x + "; y: " + y);
            }
            setFlatViewCursor(nextError.error.nErrorStart, y, nextError.error, viewCursor);
            lastX = nextError.error.nErrorStart;
            lastY = y;
            if (debugMode) {
              MessageHandler.printToLogFile("CheckDialog: getNextError: FlatViewCursor set");
            }
            uncheckedParasLeft = hasUncheckedParas;
            return nextError;
          } else if (debugMode) {
            MessageHandler.printToLogFile("CheckDialog: getNextError: Next Error = " + (nextError == null ? "null" : nextError.error.nErrorStart) 
                + ", endOfRange: " + endOfRange + "; x: " + x + "; y: " + y + "; lastPara:" + lastPara);
          }
          y++;
          x = 0;
        }
        if (endOfRange < 0) {
          if (y == docCache.size()) {
            y = 0;
          }
          while (y < lastPara) {
            setProgressValue(docCache.size() + y - lastPara, true);
            nextError = getNextErrorInParagraph (0, y, currentDocument, docCursor, true);
            if(isDisposed) {
              return null;
            }
            if (nextError != null) {
  //            if (nextError.error.aRuleIdentifier.equals(spellRuleId)) {
              if (nextError.error.nErrorType == TextMarkupType.SPELLCHECK) {
                wrongWord = docCache.getFlatParagraph(y).substring(nextError.error.nErrorStart, 
                    nextError.error.nErrorStart + nextError.error.nErrorLength);
              }
              setFlatViewCursor(nextError.error.nErrorStart, y, nextError.error, viewCursor);
              if (debugMode) {
                MessageHandler.printToLogFile("CheckDialog: getNextError: y: " + y + "lastPara: " + lastPara 
                    + ", ErrorStart: " + nextError.error.nErrorStart + ", ErrorLength: " + nextError.error.nErrorLength);
              }
              uncheckedParasLeft = hasUncheckedParas;
              return nextError;
            }
            y++;
          }
          endOfDokumentMessage = messages.getString("guiCheckComplete");
          setProgressValue(docCache.size() - 1, true);
          if (!hasUncheckedParas) {
            break;
          }
        } else {
          endOfDokumentMessage = messages.getString("guiSelectionCheckComplete");
          setProgressValue(endOfRange - 1, false);
          break;
        }
        nWhile++;
      }
      uncheckedParasLeft = false;
      lastPara = -1;
      if (debugMode) {
        MessageHandler.printToLogFile("CheckDialog: getNextError: Error == null, y: " + y + "; lastPara: " + lastPara);
      }
      return null;
    }

    /**
     * Actions of buttons
     */
    @Override
    public void actionPerformed(ActionEvent action) {
      if (!atWork) {
        try {
          if (debugMode) {
            MessageHandler.printToLogFile("CheckDialog: actionPerformed: Action: " + action.getActionCommand());
          }
          if (action.getActionCommand().equals("close")) {
            closeDialog();
          } else if (action.getActionCommand().equals("more")) {
            Tools.openURL(informationUrl);
          } else if (action.getActionCommand().equals("options")) {
            documents.runOptionsDialog();
          } else if (action.getActionCommand().equals("help")) {
            MessageHandler.showMessage(messages.getString("loDialogHelpText"));
          } else {
            Thread t = new Thread(new Runnable() {
              public void run() {
                try {
                  if (action.getActionCommand().equals("ignoreOnce")) {
                    setAtWorkButtonState();
                    ignoreOnce();
                  } else if (action.getActionCommand().equals("ignoreAll")) {
                    setAtWorkButtonState();
                    ignoreAll();
                  } else if (action.getActionCommand().equals("ignorePermanent")) {
                    setAtWorkButtonState();
                    ignorePermanent();
                  } else if (action.getActionCommand().equals("resetIgnorePermanent")) {
                    setAtWorkButtonState();
                    resetIgnorePermanent();
                  } else if (action.getActionCommand().equals("deactivateRule")) {
                    setAtWorkButtonState();
                    deactivateRule();
                  } else if (action.getActionCommand().equals("change")) {
                    setAtWorkButtonState();
                    changeText();
                  } else if (action.getActionCommand().equals("changeAll")) {
                    setAtWorkButtonState();
                    changeAll();
                  } else if (action.getActionCommand().equals("autoCorrect")) {
                    setAtWorkButtonState();
                    autoCorrect();
                  } else if (action.getActionCommand().equals("undo")) {
                    setAtWorkButtonState();
                    undo();
                  } else {
                    MessageHandler.showMessage("Action '" + action.getActionCommand() + "' not supported");
                  }
                } catch (Throwable e) {
                  MessageHandler.showError(e);
                  closeDialog();
                }
              }
            });
            t.start();
          }
        } catch (Throwable e) {
          MessageHandler.showError(e);
          closeDialog();
        }
      }
    }

    /**
     * closes the dialog
     */
    public void closeDialog() {
      isDisposed = true;
      removeMarkups();
      dialog.setVisible(false);
      if (debugMode) {
        MessageHandler.printToLogFile("CheckDialog: closeDialog: Close Spell And Grammar Check Dialog");
      }
      undoList.clear();
      documents.setLtDialog(null);
      documents.setLtDialogIsRunning(false);
      atWork = false;
    }
    
    /**
     * remove a mark for spelling error in document
     * TODO: The function works very temporarily
     */
    private void removeSpellingMark(int nFlat) throws Throwable {
      XParagraphCursor pCursor = viewCursor.getParagraphCursorFromViewCursor();
      pCursor.gotoStartOfParagraph(false);
      pCursor.goRight((short)error.nErrorStart, false);
      pCursor.goRight((short)error.nErrorLength, true);
      XMarkingAccess xMarkingAccess = UnoRuntime.queryInterface(XMarkingAccess.class, pCursor);
      if (xMarkingAccess == null) {
        MessageHandler.printToLogFile("CheckDialog: removeSpellingMark: xMarkingAccess == null");
      } else {
        xMarkingAccess.invalidateMarkings(TextMarkupType.SPELLCHECK);
      }
    }

    /**
     * set the information to ignore just the match at the given position
     * @throws Throwable 
     */
    private void ignoreOnce() throws Throwable {
      x = error.nErrorStart;
      if (isSpellError && docType == DocumentType.WRITER) {
          removeSpellingMark(y);
      }
      currentDocument.setIgnoredMatch(x, y, error.aRuleIdentifier, true);
      addUndo(x, y, "ignoreOnce", error.aRuleIdentifier);
      gotoNextError();
    }

    /**
     * set the information to ignore the match at the given position permanent
     * @throws Throwable 
     */
    private void ignorePermanent() throws Throwable {
      x = error.nErrorStart;
      Locale locale = error.nErrorType == TextMarkupType.SPELLCHECK ? docCache.getFlatParagraphLocale(y) : null;
      int len = error.nErrorType == TextMarkupType.SPELLCHECK ? error.nErrorLength : 0;
      currentDocument.setPermanentIgnoredMatch(error.nErrorStart, y, len, error.aRuleIdentifier, locale, false);
      addUndo(x, y, "ignorePermanent", error.aRuleIdentifier);
      gotoNextError();
    }

    /**
     * set the information to ignore the match at the given position permanent
     * @throws Throwable 
     */
    private void resetIgnorePermanent() throws Throwable {
      addUndo(y, "resetIgnorePermanent", currentDocument.getPermanentIgnoredMatches());
      currentDocument.resetIgnorePermanent();
      gotoNextError();
    }

    /**
     * ignore all performed:
     * spelling error: add word to temporary dictionary
     * grammatical error: deactivate rule
     * both actions are only for the current session
     * @throws Throwable 
     */
    private void ignoreAll() throws Throwable {
      if (isSpellError) {
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: ignoreAll: Ignored word: " + wrongWord);
        }
        LtDictionary.addIgnoredWord(wrongWord, xContext);
      } else {
        documents.ignoreRule(error.aRuleIdentifier, locale);
        documents.initDocuments(true);
        documents.resetDocument();
        doInit = true;
      }
      addUndo(y, "ignoreAll", error.aRuleIdentifier);
      gotoNextError();
    }

    /**
     * the rule is deactivated permanently (saved in the configuration file)
     * @throws Throwable 
     */
    private void deactivateRule() throws Throwable {
      if (!isSpellError) {
        documents.deactivateRule(error.aRuleIdentifier, OfficeTools.localeToString(locale), false);
        addUndo(y, "deactivateRule", error.aRuleIdentifier);
        doInit = true;
      }
      gotoNextError();
    }

    /**
     * compares two strings from the beginning
     * returns the first different character 
     */
    private int getDifferenceFromBegin(String text1, String text2) {
      for (int i = 0; i < text1.length() && i < text2.length(); i++) {
        if (text1.charAt(i) != text2.charAt(i)) {
          return i;
        }
      }
      return (text1.length() < text2.length() ? text1.length() : text2.length());
    }

    /**
     * compares two strings from the end
     * returns the first different character 
     */
    private int getDifferenceFromEnd(String text1, String text2) {
      for (int i = 1; i <= text1.length() && i <= text2.length(); i++) {
        if (text1.charAt(text1.length() - i) != text2.charAt(text2.length() - i)) {
          return text1.length() - i + 1;
        }
      }
      return (text1.length() < text2.length() ? 0 : text1.length() - text2.length());
    }

    /**
     * change the text of the paragraph inside the document
     * use the difference between the original paragraph and the text inside the editor element
     * or if there is no difference replace the match by the selected suggestion
     * @throws Throwable 
     */
    private void changeText() throws Throwable {
      String word = "";
      String replace = "";
      String orgText;
      removeMarkups();
      if (debugMode) {
        MessageHandler.printToLogFile("CheckDialog: changeText entered - docType: " + docType);
      }
      String dialogText = sentenceIncludeError.getText();
      if (debugMode) {
        MessageHandler.printToLogFile("CheckDialog: changeText: dialog text: " + dialogText);
      }
      orgText = docCache.getFlatParagraph(y);
      if (debugMode) {
        MessageHandler.printToLogFile("CheckDialog: changeText: original text: " + orgText);
      }
      if (!orgText.equals(dialogText)) {
        int firstChange = getDifferenceFromBegin(orgText, dialogText);
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: changeText: firstChange: " + firstChange);
        }
        int lastEqual = getDifferenceFromEnd(orgText, dialogText);
        if (lastEqual < firstChange) {
          lastEqual = firstChange;
        }
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: changeText: lastEqual: " + lastEqual);
        }
        int lastDialogEqual = dialogText.length() - orgText.length() + lastEqual;
        if (lastDialogEqual < firstChange) {
          firstChange += dialogText.length() - orgText.length();
        }
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: changeText: lastDialogEqual: " + lastDialogEqual);
        }
        if (firstChange < lastEqual) {
          word = orgText.substring(firstChange, lastEqual);
        } else {
          word ="";
        }
        if (firstChange < lastDialogEqual) {
          replace = dialogText.substring(firstChange, lastDialogEqual);
        } else {
          replace ="";
        }
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: changeText: word: '" + word + "', replace: '" + replace + "'");
        }
        changeTextOfParagraph(y, firstChange, lastEqual - firstChange, replace, currentDocument, viewCursor);
        addSingleChangeUndo(firstChange, y, word, replace);
      } else if (suggestions.getComponentCount() > 0) {
        if (orgText.length() >= error.nErrorStart + error.nErrorLength) {
          word = orgText.substring(error.nErrorStart, error.nErrorStart + error.nErrorLength);
          replace = suggestions.getSelectedValue();
          changeTextOfParagraph(y, error.nErrorStart, error.nErrorLength, replace, currentDocument, viewCursor);
          addSingleChangeUndo(error.nErrorStart, y, word, replace);
        }
      } else {
        MessageHandler.printToLogFile("CheckDialog: changeText: No text selected to change");
        return;
      }
      if (debugMode) {
        MessageHandler.printToLogFile("CheckDialog: changeText: Org: " + word + "\nDia: " + replace);
      }
      currentDocument = getCurrentDocument(true);
      gotoNextError();
    }
/*
    private void changeText() throws Throwable {
      String word = "";
      String replace = "";
      String orgText;
      removeMarkups();
      if (debugMode) {
        MessageHandler.printToLogFile("CheckDialog: changeText entered - docType: " + docType);
      }
      String dialogText = sentenceIncludeError.getText();
      if (debugMode) {
        MessageHandler.printToLogFile("CheckDialog: changeText: dialog text: " + dialogText);
      }
      if (docType != DocumentType.WRITER) {
        orgText = docCache.getFlatParagraph(y);
        if (!orgText.equals(dialogText)) {
          int firstChange = getDifferenceFromBegin(orgText, dialogText);
          int lastEqual = getDifferenceFromEnd(orgText, dialogText);
          if (lastEqual < firstChange) {
            lastEqual = firstChange;
          }
          int lastDialogEqual = dialogText.length() - orgText.length() + lastEqual;
          if (lastDialogEqual < firstChange) {
            firstChange += dialogText.length() - orgText.length();
          }
          word = orgText.substring(firstChange, lastEqual);
          replace = dialogText.substring(firstChange, lastDialogEqual);
          changeTextOfParagraph(y, firstChange, lastEqual - firstChange, replace, currentDocument, viewCursor);
          addSingleChangeUndo(firstChange, y, word, replace);
        } else if (suggestions.getComponentCount() > 0) {
          word = orgText.substring(error.nErrorStart, error.nErrorStart + error.nErrorLength);
          replace = suggestions.getSelectedValue();
          changeTextOfParagraph(y, error.nErrorStart, error.nErrorLength, replace, currentDocument, viewCursor);
          addSingleChangeUndo(error.nErrorStart, y, word, replace);
        } else {
          MessageHandler.printToLogFile("CheckDialog: changeText: No text selected to change");
          return;
        }
      } else {
        orgText = docCache.getFlatParagraph(y);
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: changeText: original text: " + orgText);
        }
        if (!orgText.equals(dialogText)) {
          int firstChange = getDifferenceFromBegin(orgText, dialogText);
          if (debugMode) {
            MessageHandler.printToLogFile("CheckDialog: changeText: firstChange: " + firstChange);
          }
          int lastEqual = getDifferenceFromEnd(orgText, dialogText);
          if (lastEqual < firstChange) {
            lastEqual = firstChange;
          }
          if (debugMode) {
            MessageHandler.printToLogFile("CheckDialog: changeText: lastEqual: " + lastEqual);
          }
          int lastDialogEqual = dialogText.length() - orgText.length() + lastEqual;
          if (lastDialogEqual < firstChange) {
            firstChange += dialogText.length() - orgText.length();
          }
          if (debugMode) {
            MessageHandler.printToLogFile("CheckDialog: changeText: lastDialogEqual: " + lastDialogEqual);
          }
          if (firstChange < lastEqual) {
            word = orgText.substring(firstChange, lastEqual);
          } else {
            word ="";
          }
          if (firstChange < lastDialogEqual) {
            replace = dialogText.substring(firstChange, lastDialogEqual);
          } else {
            replace ="";
          }
          if (debugMode) {
            MessageHandler.printToLogFile("CheckDialog: changeText: word: '" + word + "', replace: '" + replace + "'");
          }
          changeTextOfParagraph(y, firstChange, lastEqual - firstChange, replace, currentDocument, viewCursor);
          addSingleChangeUndo(firstChange, y, word, replace);
        } else if (suggestions.getComponentCount() > 0) {
          if (orgText.length() >= error.nErrorStart + error.nErrorLength) {
            word = orgText.substring(error.nErrorStart, error.nErrorStart + error.nErrorLength);
            replace = suggestions.getSelectedValue();
            changeTextOfParagraph(y, error.nErrorStart, error.nErrorLength, replace, currentDocument, viewCursor);
            addSingleChangeUndo(error.nErrorStart, y, word, replace);
          }
        } else {
          MessageHandler.printToLogFile("CheckDialog: changeText: No text selected to change");
          return;
        }
      }
      if (debugMode) {
        MessageHandler.printToLogFile("CheckDialog: changeText: Org: " + word + "\nDia: " + replace);
      }
      currentDocument = getCurrentDocument(true);
      gotoNextError();
    }
*/
    /**
     * Change all matched words of the document by the selected suggestion
     * @throws Throwable 
     */
    private void changeAll() throws Throwable {
      if (suggestions.getComponentCount() > 0) {
        removeMarkups();
        String orgText = sentenceIncludeError.getText();
        String word = orgText.substring(error.nErrorStart, error.nErrorStart + error.nErrorLength);
        String replace = suggestions.getSelectedValue();
//        XComponent xComponent = currentDocument.getXComponent();
//        DocumentCursorTools docCursor = new DocumentCursorTools(xComponent);
        Map<Integer, List<Integer>> orgParas = changeTextInAllParagraph(word, error.aRuleIdentifier, replace, currentDocument, viewCursor);
//        Map<Integer, List<Integer>> orgParas = spellChecker.replaceAllWordsInText(word, replace, docCursor, currentDocument, viewCursor);
        if (orgParas != null) {
          addChangeUndo(error.nErrorStart, y, word, replace, orgParas);
        }
        gotoNextError();
      }
    }

    /**
     * Change all matched words of the document by the selected suggestion
     * Add word-suggestion-pair to AutoCorrect
     * @throws Throwable 
     */
    private void autoCorrect() throws Throwable {
      if (suggestions.getComponentCount() > 0) {
        String orgText = sentenceIncludeError.getText();
        String word = orgText.substring(error.nErrorStart, error.nErrorStart + error.nErrorLength);
        String replace = suggestions.getSelectedValue();
//        XComponent xComponent = currentDocument.getXComponent();
//        DocumentCursorTools docCursor = new DocumentCursorTools(xComponent);
        Map<Integer, List<Integer>> orgParas = changeTextInAllParagraph(word, error.aRuleIdentifier, replace, currentDocument, viewCursor);
//        Map<Integer, List<Integer>> orgParas = spellChecker.replaceAllWordsInText(word, replace, docCursor, currentDocument, viewCursor);
        if (orgParas != null) {
          addChangeUndo(error.nErrorStart, y, word, replace, orgParas);
        }
        addToAutoCorrect(word, replace);
        gotoNextError();
      }
    }
    
    /**
     * Add word-suggestion-pair to AutoCorrect
     * @throws Throwable 
     */
    private void addToAutoCorrect(String word, String replace) throws Throwable {
      XMultiComponentFactory xMCF = UnoRuntime.queryInterface(XMultiComponentFactory.class,
          xContext.getServiceManager());
      if (xMCF == null) {
        MessageHandler.printToLogFile("Could not get XMultiComponentFactory");
        return;
      }
      Object obj = xMCF.createInstanceWithContext("com.sun.star.util.PathSettings", xContext);
      XPropertySet xPathSettings = UnoRuntime.queryInterface(XPropertySet.class, obj);
      String aCorrPathUri = (String) xPathSettings.getPropertyValue("AutoCorrect_writable");
      URI aCorrUri = new URI(aCorrPathUri);
      String aCorrFile = aCorrUri.getPath() + "/" + ACORR_PREFIX + locale.Language + "-" + locale.Country + ACORR_SUFFIX;
      String aCorrTmpFile = aCorrUri.getPath() + "/" + ACORR_PREFIX + locale.Language + "-" + locale.Country + "_tmp" + ACORR_SUFFIX;
      MessageHandler.printToLogFile("AutoCorrect path: " + aCorrFile);
      String tmpDir = aCorrUri.getPath() + "/tmp";
      unzipACorr(tmpDir, aCorrFile);
      addWordPairToCorFile(tmpDir, word, replace);
      zipACorr(tmpDir, aCorrTmpFile);
      File tmpDr = new File(tmpDir); 
      deleteFullDirectory(tmpDr);
      File file = new File(aCorrFile);
      File newFile = new File(aCorrTmpFile);
      file.delete();
      newFile.renameTo(file);
    }
    
    boolean deleteFullDirectory(File directory) {
      File[] contents = directory.listFiles();
      if (contents != null) {
          for (File file : contents) {
              deleteFullDirectory(file);
          }
      }
      return directory.delete();
    }
    
    private void unzipACorr(String destDirPath, String zipFilePath) throws IOException {
      File destDir = new File(destDirPath);
      if (destDir.exists() && destDir.isDirectory()) {
        if(!deleteFullDirectory(destDir)) {
          throw new IOException("Failed to remove directory " + destDirPath);
        }
      }
      if (!destDir.mkdir()) {
        throw new IOException("Failed to create directory " + destDirPath);
      }
      byte[] buffer = new byte[1024];
      try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
          File newFile = new File(destDir, zipEntry.getName());
          if (zipEntry.isDirectory()) {
            if (!newFile.isDirectory() && !newFile.mkdirs()) {
              throw new IOException("Failed to create directory " + newFile);
            }
          } else {
            // fix for Windows-created archives
            File parent = newFile.getParentFile();
            if (!parent.isDirectory() && !parent.mkdirs()) {
              throw new IOException("Failed to create directory " + parent);
            }
            // write file content
            FileOutputStream fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
              fos.write(buffer, 0, len);
            }
            fos.close();
          }
          zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
      }
    }
    
    private void addWordPairToCorFile(String dirPath, String word, String replace) throws Throwable {
      String fileName = "DocumentList.xml";
      String tmpFileName = "DocumentList_tmp.xml";
      File file = new File(dirPath, fileName);
      File newFile = new File(dirPath, tmpFileName);
      FileInputStream fis = new FileInputStream(file);
      FileOutputStream fos = new FileOutputStream(newFile);
      byte[] buffer = new byte[1024];
      int len;
      long maxReadLen = file.length() - 24;
      while (maxReadLen > 0 && (len = fis.read(buffer)) > 0) {
        if (len > maxReadLen) {
          len = (int) maxReadLen;
        }
        fos.write(buffer, 0, len);
        maxReadLen -= len;
      }
      fis.close();
      String str = "<block-list:block block-list:abbreviated-name=\"" + word + "\" block-list:name=\"" + replace + "\"/></block-list:block-list>";
      buffer = str.getBytes();
      len = buffer.length;
      fos.write(buffer, 0, len);
      fos.close();
      file.delete();
      newFile.renameTo(file);
    }

    private void zipACorr(String sourceDirPath, String zipFilePath) throws IOException {
      FileOutputStream fos = new FileOutputStream(zipFilePath);
      ZipOutputStream zipOut = new ZipOutputStream(fos);
      File dirToZip = new File(sourceDirPath);
      File[] children = dirToZip.listFiles();
      for (File childFile : children) {
        zipFileRec(childFile, childFile.getName(), zipOut);
      }
      zipOut.close();
      fos.close();
    }
    
    private void zipFileRec(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
      if (fileToZip.isHidden()) {
          return;
      }
      if (fileToZip.isDirectory()) {
        if (fileName.endsWith("/")) {
          zipOut.putNextEntry(new ZipEntry(fileName));
          zipOut.closeEntry();
        } else {
          zipOut.putNextEntry(new ZipEntry(fileName + "/"));
          zipOut.closeEntry();
        }
        File[] children = fileToZip.listFiles();
        for (File childFile : children) {
          zipFileRec(childFile, fileName + "/" + childFile.getName(), zipOut);
        }
        return;
      }
      FileInputStream fis = new FileInputStream(fileToZip);
      ZipEntry zipEntry = new ZipEntry(fileName);
      zipOut.putNextEntry(zipEntry);
      byte[] bytes = new byte[1024];
      int length;
      while ((length = fis.read(bytes)) >= 0) {
          zipOut.write(bytes, 0, length);
      }
      fis.close();
    }
    
    /**
     * Add undo information
     * maxUndos changes are stored in the undo list
     */
    private void addUndo(int y, String action, String ruleId) throws Throwable {
      addUndo(0, y, action, ruleId);
    }
    
    private void addUndo(int x, int y, String action, String ruleId) throws Throwable {
      addUndo(x, y, action, ruleId, null);
    }
    
    private void addUndo(int y, String action, String ruleId, String word) throws Throwable {
      addUndo(0, y, action, ruleId, word, null, null);
    }
    
    private void addUndo(int x, int y, String action, String ruleId, Map<Integer, List<Integer>> orgParas) throws Throwable {
      addUndo(x, y, action, ruleId, null, orgParas, null);
    }
    
    private void addUndo(int y, String action, IgnoredMatches ignoredMatches) throws Throwable {
      addUndo(0, y, action, null, null, null, ignoredMatches);
    }
    
    private void addUndo(int x, int y, String action, String ruleId, String word, Map<Integer, 
        List<Integer>> orgParas, IgnoredMatches ignoredMatches) throws Throwable {
      if (undoList.size() >= maxUndos) {
        undoList.remove(0);
      }
      undoList.add(new UndoContainer(x, y, isSpellError, action, ruleId, word, orgParas, ignoredMatches));
    }

    /**
     * add undo information for change function (general)
     */
    private void addChangeUndo(int x, int y, String word, String replace, Map<Integer, List<Integer>> orgParas) throws Throwable {
      addUndo(x, y, "change", replace, word, orgParas, null);
    }
    
    /**
     * add undo information for a single change
     * @throws Throwable 
     */
    private void addSingleChangeUndo(int x, int y, String word, String replace) throws Throwable {
      Map<Integer, List<Integer>> paraMap = new HashMap<Integer, List<Integer>>();
      List<Integer> xVals = new ArrayList<Integer>();
      xVals.add(x);
      paraMap.put(y, xVals);
      addChangeUndo(x, y, word, replace, paraMap);
    }

    /**
     * add undo information for a language change
     * @throws Throwable 
     */
    private void addLanguageChangeUndo(int nFlat, int nStart, int nLen, String originalLanguage) throws Throwable {
      Map<Integer, List<Integer>> paraMap = new HashMap<Integer, List<Integer>>();
      List<Integer> xVals = new ArrayList<Integer>();
      xVals.add(nStart);
      xVals.add(nLen);
      paraMap.put(nFlat, xVals);
      addUndo(0, nFlat, "changeLanguage", originalLanguage, null, paraMap, null);
    }

    /**
     * undo the last change triggered by the LT check dialog
     */
    private void undo() throws Throwable {
      if (undoList == null || undoList.isEmpty()) {
        return;
      }
      try {
        removeMarkups();
        int nLastUndo = undoList.size() - 1;
        UndoContainer lastUndo = undoList.get(nLastUndo);
        String action = lastUndo.action;
        int xUndo = lastUndo.x;
        int yUndo = lastUndo.y;
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: Undo: Action: " + action);
        }
        if (action.equals("ignoreOnce")) {
          currentDocument.removeIgnoredMatch(xUndo, yUndo, lastUndo.ruleId, true);
        } else if (action.equals("ignorePermanent")) {
          currentDocument.removePermanentIgnoredMatch(xUndo, yUndo, lastUndo.ruleId, true);
        } else if (action.equals("resetIgnorePermanent")) {
          currentDocument.setPermanentIgnoredMatches(lastUndo.ignoredMatches);
        } else if (action.equals("ignoreAll")) {
          if (lastUndo.isSpellError) {
            if (debugMode) {
              MessageHandler.printToLogFile("CheckDialog: Undo: Ignored word removed: " + wrongWord);
            }
            LtDictionary.removeIgnoredWord(wrongWord, xContext);
          } else {
            Locale locale = docCache.getFlatParagraphLocale(yUndo);
            documents.removeDisabledRule(OfficeTools.localeToString(locale), lastUndo.ruleId);
            documents.initDocuments(true);
            documents.resetDocument();
            doInit = true;
          }
        } else if (action.equals("deactivateRule")) {
          currentDocument.removeResultCache(yUndo, true);
          Locale locale = docCache.getFlatParagraphLocale(yUndo);
          documents.deactivateRule(lastUndo.ruleId, OfficeTools.localeToString(locale), true);
          doInit = true;
        } else if (action.equals("activateRule")) {
          currentDocument.removeResultCache(yUndo,true);
          Locale locale = docCache.getFlatParagraphLocale(yUndo);
          documents.deactivateRule(lastUndo.ruleId, OfficeTools.localeToString(locale), false);
          doInit = true;
        } else if (action.equals("addToDictionary")) {
          LtDictionary.removeWordFromDictionary(lastUndo.ruleId, lastUndo.word, xContext);
        } else if (action.equals("changeLanguage")) {
          Locale locale = getLocaleFromLanguageName(lastUndo.ruleId);
          FlatParagraphTools flatPara = currentDocument.getFlatParagraphTools();
          int nFlat = lastUndo.y;
          int nStart = lastUndo.orgParas.get(nFlat).get(0);
          int nLen = lastUndo.orgParas.get(nFlat).get(1);
          if (debugMode) {
            MessageHandler.printToLogFile("CheckDialog: Undo: Change Language: Locale: " + locale.Language + "-" + locale.Country 
              + ", nFlat = " + nFlat + ", nStart = " + nStart + ", nLen = " + nLen);
          }
          if (docType == DocumentType.IMPRESS) {
            OfficeDrawTools.setLanguageOfParagraph(nFlat, nStart, nLen, locale, currentDocument.getXComponent());
          } else if (docType == DocumentType.CALC) {
            OfficeSpreadsheetTools.setLanguageOfSpreadsheet(locale, currentDocument.getXComponent());
          } else {
            flatPara.setLanguageOfParagraph(nFlat, nStart, nLen, locale);
          }
          if (nLen == docCache.getFlatParagraph(nFlat).length()) {
            docCache.setFlatParagraphLocale(nFlat, locale);
            DocumentCache curDocCache = currentDocument.getDocumentCache();
            if (curDocCache != null) {
              curDocCache.setFlatParagraphLocale(nFlat, locale);
            }
          }
          currentDocument.removeResultCache(nFlat,true);
        } else if (action.equals("change")) {
          Map<Integer, List<Integer>> paras = lastUndo.orgParas;
          short length = (short) lastUndo.ruleId.length();
          for (int nFlat : paras.keySet()) {
            List<Integer> xStarts = paras.get(nFlat);
            TextParagraph n = docCache.getNumberOfTextParagraph(nFlat);
            if (debugMode) {
              MessageHandler.printToLogFile("CheckDialog: Undo: Ignore change: nFlat = " + nFlat + ", n = (" + n.type + "," + n.number + "), x = " + xStarts.get(0));
            }
            if (docType != DocumentType.WRITER) {
              for (int i = xStarts.size() - 1; i >= 0; i --) {
                int xStart = xStarts.get(i);
                changeTextOfParagraph(nFlat, xStart, length, lastUndo.word, currentDocument, viewCursor);
              }
            } else {
//              String para = docCache.getFlatParagraph(nFlat);
              for (int i = xStarts.size() - 1; i >= 0; i --) {
                int xStart = xStarts.get(i);
//                para = para.substring(0, xStart) + lastUndo.word + para.substring(xStart + length);
                changeTextOfParagraph(nFlat, xStart, length, lastUndo.word, currentDocument, viewCursor);
              }
            }
            currentDocument.removeResultCache(nFlat, true);
          }
        } else {
          MessageHandler.showMessage("Undo '" + action + "' not supported");
        }
        undoList.remove(nLastUndo);
        setFlatViewCursor(xUndo, yUndo, null, viewCursor);
        if (debugMode) {
          MessageHandler.printToLogFile("CheckDialog: Undo: yUndo = " + yUndo + ", xUndo = " + xUndo 
              + ", lastPara = " + lastPara);
        }
        gotoNextError();
      } catch (Throwable e) {
        MessageHandler.showError(e);
        closeDialog();
      }
    }

    void setFlatViewCursor(int x, int y, SingleProofreadingError error, ViewCursorTools viewCursor) throws Throwable {
      this.x = x;
      this.y = y;
      if (docType == DocumentType.WRITER) {
        TextParagraph para = docCache.getNumberOfTextParagraph(y);
        SpellAndGrammarCheckDialog.setTextViewCursor(x, para, viewCursor);
      } else if (docType == DocumentType.IMPRESS) {
        OfficeDrawTools.setCurrentPage(y, currentDocument.getXComponent());
        if (OfficeDrawTools.isParagraphInNotesPage(y, currentDocument.getXComponent())) {
          OfficeTools.dispatchCmd(".uno:NotesMode", xContext);
          //  Note: a delay interval is needed to put the dialog to front
          try {
            Thread.sleep(500);
          } catch (InterruptedException e) {
            MessageHandler.printException(e);
          }
          dialog.toFront();
        }
        if (error != null) {
          undoMarkup = new UndoMarkupContainer();
          OfficeDrawTools.setMarkup(y, error, undoMarkup, currentDocument.getXComponent());
        }
      } else {
        OfficeSpreadsheetTools.setCurrentSheet(y, currentDocument.getXComponent());
      }
    }

    void removeMarkups() {
      if (docType == DocumentType.IMPRESS && undoMarkup != null) {
        OfficeDrawTools.removeMarkup(undoMarkup, currentDocument.getXComponent());
        undoMarkup = null;
      }
    }
    
  }
  
}