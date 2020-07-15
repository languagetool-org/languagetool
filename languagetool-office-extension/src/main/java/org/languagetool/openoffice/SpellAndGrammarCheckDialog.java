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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.gui.Tools;

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.text.TextMarkupType;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XWordCursor;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Class defines the spell and grammar check dialog
 * @since 5.1
 * @author Fred Kruse
 */
public class SpellAndGrammarCheckDialog extends Thread {
  
  private static boolean debugMode = false;         //  should be false except for testing

  private static final String spellingError = "Spelling Error";
  private static final String spellRuleId = "SpellingError";
  private XComponentContext xContext;
  private MultiDocumentsHandler documents;
  private SwJLanguageTool langTool;
  private ExtensionSpellChecker spellChecker;
  private Locale locale;
  private int checkType = 0;
  
  SpellAndGrammarCheckDialog(XComponentContext xContext, MultiDocumentsHandler documents) {
    debugMode = OfficeTools.DEBUG_MODE_CD;
    this.xContext = xContext;
    this.documents = documents;
    spellChecker = new ExtensionSpellChecker();
    langTool = documents.getLanguageTool();
  }
  
  @Override
  public void run() {
    if (!documents.javaVersionOkay()) {
      MessageHandler.printToLogFile("Wrong Java Version Check Dialog not started");
      return;
    }
    try {
      LtCheckDialog checkDialog = new LtCheckDialog(xContext);
      checkDialog.show();
    } catch (Throwable e) {
      MessageHandler.showError(e);
    }
  }
  
  public void nextError() {
    SingleDocument document = documents.getCurrentDocument();
    while (document == null) {
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        MessageHandler.printException(e);
      }
      document = documents.getCurrentDocument();
    }
    XComponent xComponent = document.getXComponent();
    DocumentCursorTools docCursor = new DocumentCursorTools(xComponent);
    FlatParagraphTools flatPara = document.getFlatParagraphTools();
    if (flatPara == null) {
      flatPara = new FlatParagraphTools(xComponent);
    } else {
      flatPara.init();
    }
    DocumentCache docCache = new DocumentCache(docCursor, flatPara, -1);
    if (docCache.size() <= 0) {
      return;
    }
    ViewCursorTools viewCursor = new ViewCursorTools(xContext);
    int y = viewCursor.getViewCursorParagraph();
    int x = viewCursor.getViewCursorCharacter();
    long nChars = 0;
    SingleProofreadingError nextError = getNextErrorInParagraph (x, y, docCache, document, docCursor, null);
    while (y < docCache.textSize() - 1 && nextError == null) {
      nChars += docCache.getTextParagraph(y).length() + 1;
      y++;
      nextError = getNextErrorInParagraph (0, y, docCache, document, docCursor, null);
    }
    if (nextError != null) {
      nChars += nextError.nErrorStart + 1 - x;
      setViewCursor(nChars, viewCursor);
    } else {
      MessageHandler.showMessage("End of document is reached");  // TODO: Add language specific message
    }
  }
  
  private void setViewCursor(long nChars, ViewCursorTools viewCursor)  {
    if (nChars == 0) {
      return;
    }
    XTextViewCursor vCursor = viewCursor.getViewCursor();
    vCursor.collapseToStart();
    boolean toRight = true;
    if (nChars < 0) {
      toRight = false;
      nChars = -nChars;
    }
    while (nChars > Short.MAX_VALUE) {
      if (toRight) {
        vCursor.goRight(Short.MAX_VALUE, false);
      } else {
        vCursor.goLeft(Short.MAX_VALUE, false);
      }
      nChars -= Short.MAX_VALUE;
    }
    if (toRight) {
      vCursor.goRight((short)nChars, false);
    } else {
      vCursor.goLeft((short)nChars, false);
    }
  }
  
  private void setViewCursor(int x, int y, ViewCursorTools viewCursor)  {
    SingleDocument document = documents.getCurrentDocument();
    XComponent xComponent = document.getXComponent();
    DocumentCursorTools docCursor = new DocumentCursorTools(xComponent);
    FlatParagraphTools flatPara = document.getFlatParagraphTools();
    flatPara.init();
    DocumentCache docCache = new DocumentCache(docCursor, flatPara, -1);
    if (docCache.size() <= 0) {
      return;
    }
    int yCur = viewCursor.getViewCursorParagraph();
    int xCur = viewCursor.getViewCursorCharacter();
    if (y == yCur && x == xCur) {
      return;
    }
    long nChars = 0;
    if (yCur < y || (yCur == y && xCur < x)) {
      for (int i = yCur; i < y; i++) {
        nChars += docCache.getTextParagraph(i).length() + 1;
      }
      nChars += x - xCur;
    } else {
      for (int i = yCur - 1; i >= y; i--) {
        nChars -= docCache.getTextParagraph(i).length() + 1;
      }
      nChars -= xCur - x;
    }
    if (debugMode) {
      MessageHandler.printToLogFile("nChars = " + nChars + "; x = " + x + "; y = " + y + "; xCur = " + xCur + "; yCur = " + yCur);
    }
    setViewCursor(nChars, viewCursor);
  }
  
  private SingleProofreadingError getNextErrorInParagraph (int x, int y, DocumentCache docCache, SingleDocument document, 
      DocumentCursorTools docTools, Map<Integer, Set<Integer>> ignoredSpellMatches) {
    String text = docCache.getTextParagraph(y);
    locale = docCache.getTextParagraphLocale(y);
    int[] footnotePosition = docCache.getTextParagraphFootnotes(y);

    SingleProofreadingError sError = null;
    SingleProofreadingError gError = null;
    if (checkType != 2) {
      sError = getNextSpellErrorInParagraph (x, y, locale, docTools, ignoredSpellMatches);
    }
    if (checkType != 1) {
      gError = getNextGrammatikErrorInParagraph(x, y, text, footnotePosition, locale, document);
    }
    if (sError != null) {
      if (gError != null && gError.nErrorStart < sError.nErrorStart) {
        return gError;
      }
      return sError; 
    } else {
      return gError;
    }
  }
  
  private SingleProofreadingError getNextSpellErrorInParagraph (int x, int y, Locale locale,
      DocumentCursorTools cursorTools, Map<Integer, Set<Integer>> ignoredSpellMatches) {
    SingleProofreadingError[] errors = spellChecker.getSpellErrors(y, locale, cursorTools, ignoredSpellMatches);
    if (errors != null) {
      for (SingleProofreadingError error : errors) {
        if (error.nErrorStart >= x) {
//            MessageHandler.printToLogFile("Next Error: ErrorStart == " + error.nErrorStart + ", start: " + start);
          return error;
        }
      }
    }
    return null;
  }
  
  SingleProofreadingError getNextGrammatikErrorInParagraph(int x, int y, String text, int[] footnotePosition, Locale locale, SingleDocument document) {
    if (text == null || text.isEmpty() || x >= text.length()) {
      return null;
    }
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
    langTool = documents.getLanguageTool();
    while (paRes.nStartOfNextSentencePosition < text.length()) {
      paRes.nStartOfSentencePosition = paRes.nStartOfNextSentencePosition;
      paRes.nStartOfNextSentencePosition = text.length();
      paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
      paRes = document.getCheckResults(text, locale, paRes, propertyValues, false, langTool, y);
      if (paRes.aErrors != null) {
        for (SingleProofreadingError error : paRes.aErrors) {
          if (error.nErrorStart >= x) {
            return error;
          }        
        }
      }
    }
    return null;
  }
  
  /** 
   * Class for spell checking in spell and grammar dialog
   */
  public class ExtensionSpellChecker {

    private LinguisticServices linguServices;
     
    ExtensionSpellChecker() {
      linguServices = new LinguisticServices(xContext);
    }

    public SingleProofreadingError[] getSpellErrors(int numPara, Locale lang, 
        DocumentCursorTools cursorTools, Map<Integer, Set<Integer>> ignoredSpellMatches) {
      try {
        List<SingleProofreadingError> errorArray = new ArrayList<SingleProofreadingError>();
        WordsFromParagraph wParas = new WordsFromParagraph(numPara, cursorTools);
        String word = wParas.getNextWord();
        while (word != null) {
          if (!linguServices.isCorrectSpell(word, lang)) {
            int wordBegin = wParas.getBeginOfWord();
            int wordLength = wParas.getLengthOfWord();
            if (word.charAt(wordLength - 1) == '.') {
              word = word.substring(0, wordLength - 1);
              wordLength--;
            }
            if (!isIgnoredMatch (wordBegin, wordBegin + wordLength, numPara, ignoredSpellMatches)) {
              SingleProofreadingError aError = new SingleProofreadingError();
              aError.nErrorType = TextMarkupType.SPELLCHECK;
              aError.aFullComment = spellingError;
              aError.aShortComment = aError.aFullComment;
              aError.nErrorStart = wordBegin;
              aError.nErrorLength = wordLength;
              aError.aRuleIdentifier = spellRuleId;
              errorArray.add(aError);
              String[] alternatives = linguServices.getSpellAlternatives(word, lang);
              if (alternatives != null) {
                aError.aSuggestions = alternatives;
              } else {
                aError.aSuggestions = new String[0];
              }
            }
          }
          word = wParas.getNextWord();
        }
        return errorArray.toArray(new SingleProofreadingError[0]);
      } catch (Throwable t) {
        MessageHandler.showError(t);
      }
      return null;
    }
    
    boolean isIgnoredMatch (int wBegin, int wEnd, int nPara, Map<Integer, Set<Integer>> ignoredSpellMatches) {
      if (ignoredSpellMatches != null && ignoredSpellMatches.containsKey(nPara)) {
        for (int nChar : ignoredSpellMatches.get(nPara)) {
          if (wBegin <= nChar && wEnd > nChar) {
            return true;
          }
        }
      }
      return false;
    }

    public Map<Integer, List<Integer>> replaceAllWordsInText(String word, String replace, DocumentCursorTools cursorTools) {
      if (word == null || replace == null || word.isEmpty() || replace.isEmpty() || word.equals(replace)) {
        return null;
      }
      WordsFromParagraph wParas = new WordsFromParagraph(0, cursorTools);
      return wParas.replaceWordInText(word, replace);
    }

    class WordsFromParagraph {
      int paraLength;
      int wordStart = -1;
      int wordLength;
      String word;
      XParagraphCursor pCursor;
      XWordCursor wCursor;
      
      public WordsFromParagraph(int n, DocumentCursorTools cursorTools) {
        pCursor = cursorTools.getParagraphCursor();
        wCursor = UnoRuntime.queryInterface(XWordCursor.class, pCursor);
        pCursor.gotoStart(false);
        for (int i = 0; i < n && pCursor != null; i++) {
          pCursor.gotoNextParagraph(false);
        }
        pCursor.gotoStartOfParagraph(false);
        pCursor.gotoEndOfParagraph(true);
        paraLength = pCursor.getString().length();
        pCursor.gotoStartOfParagraph(false);
        wCursor.gotoStartOfWord(false);
      }
      
      public int getLengthOfParagraph() {
        return paraLength;
      }
      
      public String getNextWord() {
        if (wordStart >= 0) {
          boolean res = wCursor.gotoNextWord(false);
          if (!res) {
            return null;
          }
        } else {
          wCursor.gotoEndOfWord(false);
        }
        wCursor.gotoStartOfWord(false);
        wCursor.gotoEndOfWord(true);
        String tmpWord = wCursor.getString();
        wCursor.gotoStartOfWord(false);
        XTextRange startOfWord = wCursor.getStart();
        pCursor.gotoStartOfParagraph(true);
        int nStart = pCursor.getString().length();
        pCursor.gotoRange(startOfWord, false);
        if (tmpWord.isEmpty() || nStart < wordStart + wordLength 
            || wordStart + wordLength + tmpWord.length() > paraLength) {
          return null;
        }
        word = tmpWord;
        wordStart = nStart;
        wordLength = word.length();
        return word;
      }
      
      public int getBeginOfWord () {
        return wordStart;
      }
      
      public int getLengthOfWord () {
        return wordLength;
      }
      
      public Map<Integer, List<Integer>> replaceWordInText (String oWord, String replace) {
        Map<Integer, List<Integer>> paraMap = null;
        pCursor.gotoStart(false);
        boolean pRes = true;
        int nPara = 0;
        while (pRes) {
          pCursor.gotoStartOfParagraph(false);
          pCursor.gotoEndOfParagraph(true);
          String paraText = pCursor.getString();
          paraLength = paraText.length();
          if (debugMode) {
            MessageHandler.printToLogFile("Paragraph (" + paraLength + "):" + pCursor.getString());
          }
          pCursor.gotoStartOfParagraph(false);
          wordStart = 0;
          wordLength = 0;
          boolean wRes = wCursor.gotoEndOfWord(false);
          while (wRes) {
            wCursor.gotoStartOfWord(false);
            wCursor.gotoEndOfWord(true);
            String tmpWord = wCursor.getString();
            boolean addToUndo = false;
            if (tmpWord.endsWith(".")) {
              wCursor.goLeft((short)1, false);
              wCursor.gotoStartOfWord(true);
              tmpWord = wCursor.getString();
            }
            if (oWord.equals(tmpWord)) {
              wCursor.setString(replace);
              tmpWord = replace;
              addToUndo = true;
            }
            wCursor.gotoStartOfWord(false);
            XTextRange startOfWord = wCursor.getStart();
            pCursor.gotoStartOfParagraph(true);
            int nStart = pCursor.getString().length();
            if (addToUndo) {
              paraMap = addChangeToUndoMap(nStart, nPara, paraMap);
            }
            pCursor.gotoRange(startOfWord, false);
            if (tmpWord.isEmpty() || nStart < wordStart + wordLength 
                || wordStart + wordLength + tmpWord.length() > paraLength) {
              break;
            }
            if (debugMode) {
              MessageHandler.printToLogFile(tmpWord);
            }
            word = tmpWord;
            wordStart = nStart;
            wordLength = word.length();
            wRes = wCursor.gotoNextWord(false);
          }
          nPara++;
          pRes = pCursor.gotoNextParagraph(false);
        }
        return paraMap;
      }
      
      private Map<Integer, List<Integer>> addChangeToUndoMap(int x, int y, Map<Integer, List<Integer>> paraMap) {
        if (paraMap == null) {
          paraMap = new HashMap<Integer, List<Integer>>();
        }
        List<Integer> xVals;
        if (paraMap.containsKey(y)) {
          xVals = paraMap.get(y);
        } else {
          xVals = new ArrayList<Integer>();
        }
        xVals.add(x);
        paraMap.put(y, xVals);
        return paraMap;
      }

    }
    
  }
  
  public class UndoContainer {
    public int x;
    public int y;
    public String action;
    public String ruleId;
    public String word;
    public Map<Integer, List<Integer>> orgParas;
    
    UndoContainer(int x, int y, String action, String ruleId, String word, Map<Integer, List<Integer>> orgParas) {
      this.x = x;
      this.y = y;
      this.action = action;
      this.ruleId = ruleId;
      this.orgParas = orgParas;
      this.word = word;
    }
  }
  
  /**
   * Class for dialog to check text for spell and grammar errors
   */
  public class LtCheckDialog implements ActionListener {
    private final static int maxUndos = 20;
    private final ResourceBundle messages = JLanguageTool.getMessageBundle();
    private final String dialogName = messages.getString("guiOOoCheckDialogName");
    private final String labelLanguage = messages.getString("textLanguage");
    private final String labelSuggestions = messages.getString("guiOOosuggestions"); 
    private final String moreButtonName = messages.getString("guiMore"); 
    private final String ignoreButtonName = messages.getString("guiOOoIgnoreButton"); 
    private final String ignoreAllButtonName = messages.getString("guiOOoIgnoreAllButton"); 
    private final String deactivateRuleButtonName = messages.getString("loContextMenuDeactivateRule"); 
    private final String addToDictionaryName = messages.getString("guiOOoaddToDictionary");
    private final String changeButtonName = messages.getString("guiOOoChangeButton"); 
    private final String changeAllButtonName = messages.getString("guiOOoChangeAllButton"); 
    private final String helpButtonName = messages.getString("guiMenuHelp"); 
    private final String optionsButtonName = messages.getString("guiOOoOptionsButton"); 
    private final String undoButtonName = messages.getString("guiUndo"); 
    private final String closeButtonName = messages.getString("guiCloseButton"); 
    private JDialog dialog;
    private JLabel languageLabel;
    private JComboBox<String> language;
    private JTextArea errorDescription;
    private JTextPane sentenceIncludeError;
    private JLabel suggestionsLabel;
    private JList<String> suggestions;
    private JLabel checkTypeLabel;
    private ButtonGroup checkTypeGroup;
    private JRadioButton[] checkTypeButtons;
    private JButton more; 
    private JButton ignoreOnce; 
    private JButton ignoreAll; 
    private JButton deactivateRule;
    private JComboBox<String> addToDictionary; 
    private JButton change; 
    private JButton changeAll; 
    private JButton help; 
    private JButton options; 
    private JButton undo; 
    private JButton close; 
    
    private SingleDocument currentDocument;
    private ViewCursorTools viewCursor;
    private SingleProofreadingError error;
    private Map<Integer, Set<Integer>> ignoredSpellMatches;
    private String[] userDictionaries;
    private String informationUrl;
    private String docId;
    private int x;
    private int y;
    private int endOfRange = -1;
    private boolean isSpellError = false;
    private boolean focusLost = false;
    private String wrongWord;
    private List<UndoContainer> undoList;
    
    public LtCheckDialog(XComponentContext xContext) {
      int begFirstCol = 10;
      int widFirstCol = 450;
      int disFirstCol = 10;
      int buttonHigh = 30;
      int begSecondCol = 470;
      int buttonWidthCol = 160;
      int buttonDistCol = 10;
      int buttonWidthRow = 120;
      int buttonDistRow = (begSecondCol + buttonWidthCol - begFirstCol - 4 * buttonWidthRow) / 3;
      if (debugMode) {
        MessageHandler.printToLogFile("LtCheckDialog called");
      }
      currentDocument = documents.getCurrentDocument();
      while (currentDocument == null) {
        try {
          Thread.sleep(5);
        } catch (InterruptedException e) {
          MessageHandler.printException(e);
        }
        currentDocument = documents.getCurrentDocument();
      }
      docId = currentDocument.getDocID();
      ignoredSpellMatches = new HashMap<>();
      undoList = new ArrayList<UndoContainer>();
      setUserDictionaries();

      dialog = new JDialog();
      if (dialog == null) {
        MessageHandler.printToLogFile("LtCheckDialog == null");
      }
      dialog.setName(dialogName);
      dialog.setTitle(dialogName);
      dialog.setLayout(null);
      dialog.setSize(640, 480);

      languageLabel = new JLabel(labelLanguage);
      Font dialogFont = languageLabel.getFont().deriveFont((float) 12);
      languageLabel.setBounds(begFirstCol, disFirstCol, 180, 30);
      languageLabel.setFont(dialogFont);
      dialog.add(languageLabel);

      language = new JComboBox<String>(getPossibleLanguages());
      language.setFont(dialogFont);
      language.setBounds(190, disFirstCol, widFirstCol + begFirstCol - 190, 30);
      dialog.add(language);

      int yFirstCol = 2 * disFirstCol + 30;
      errorDescription = new JTextArea();
//      errorDescription.setBorder(new LineBorder(Color.black));
      errorDescription.setEditable(false);
      errorDescription.setLineWrap(true);
      errorDescription.setWrapStyleWord(true);
      errorDescription.setBackground(dialog.getContentPane().getBackground());
      Font descriptionFont = dialogFont.deriveFont(Font.BOLD);
      errorDescription.setFont(descriptionFont);
      JScrollPane descriptionPane = new JScrollPane(errorDescription);
      descriptionPane.setBounds(begFirstCol, yFirstCol, widFirstCol, 40);
      dialog.add(descriptionPane);

      yFirstCol += disFirstCol + 40;
      sentenceIncludeError = new JTextPane();
      sentenceIncludeError.setFont(dialogFont);
      sentenceIncludeError.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void changedUpdate(DocumentEvent e) {
          if (!change.isEnabled()) {
            change.setEnabled(true);
          }
          if (changeAll.isEnabled()) {
            changeAll.setEnabled(false);
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
      sentencePane.setBounds(begFirstCol, yFirstCol, widFirstCol, 110);
//      sentencePane.setBorder(new LineBorder(Color.black));
      dialog.add(sentencePane);
      
      yFirstCol += disFirstCol + 110;
      suggestionsLabel = new JLabel(labelSuggestions);
      suggestionsLabel.setFont(dialogFont);
      suggestionsLabel.setBounds(begFirstCol, yFirstCol, widFirstCol, 15);
      dialog.add(suggestionsLabel);

      yFirstCol += disFirstCol + 15;
      suggestions = new JList<String>();
      suggestions.setFont(dialogFont);
      suggestions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      suggestions.setFixedCellHeight((int)(suggestions.getFont().getSize() * 1.2 + 0.5));
      JScrollPane suggestionsPane = new JScrollPane(suggestions);
      suggestionsPane.setBounds(begFirstCol, yFirstCol, widFirstCol, 100);
//      suggestionsPane.setBorder(new LineBorder(Color.black));
      dialog.add(suggestionsPane);
      
      yFirstCol += disFirstCol + 100;
      checkTypeLabel = new JLabel(Tools.getLabel(messages.getString("guiOOoCheckTypeLabel")));
      checkTypeLabel.setFont(dialogFont);
      checkTypeLabel.setBounds(begFirstCol, yFirstCol, 3*widFirstCol/16 - 1, 30);
      dialog.add(checkTypeLabel);

      checkTypeButtons = new JRadioButton[3];
      checkTypeGroup = new ButtonGroup();
      checkTypeButtons[0] = new JRadioButton(Tools.getLabel(messages.getString("guiOOoCheckAllButton")));
      checkTypeButtons[0].setBounds(begFirstCol + 3*widFirstCol/16, yFirstCol, 3*widFirstCol/16 - 1, 30);
      checkTypeButtons[0].setSelected(true);
      checkTypeButtons[0].addActionListener(e -> {
        checkType = 0;
        gotoNextError(true);
      });
      checkTypeButtons[1] = new JRadioButton(Tools.getLabel(messages.getString("guiOOoCheckSpellingButton")));
      checkTypeButtons[1].setBounds(begFirstCol + 6*widFirstCol/16, yFirstCol, 5*widFirstCol/16 - 1, 30);
      checkTypeButtons[1].addActionListener(e -> {
        checkType = 1;
        gotoNextError(true);
      });
      checkTypeButtons[2] = new JRadioButton(Tools.getLabel(messages.getString("guiOOoCheckGrammarButton")));
      checkTypeButtons[2].setBounds(begFirstCol + 11*widFirstCol/16, yFirstCol, 5*widFirstCol/16 - 1, 30);
      checkTypeButtons[2].addActionListener(e -> {
        checkType = 2;
        gotoNextError(true);
      });
      for (int i = 0; i < 3; i++) {
        checkTypeGroup.add(checkTypeButtons[i]);
        checkTypeButtons[i].setFont(dialogFont);
        dialog.add(checkTypeButtons[i]);
      }

      yFirstCol += 2 * disFirstCol + 30;
      help = new JButton (helpButtonName);
      help.setFont(dialogFont);
      help.setBounds(begFirstCol, yFirstCol, buttonWidthRow, buttonHigh);
      help.addActionListener(this);
      help.setActionCommand("help");
      dialog.add(help);
      
      int xButtonRow = begFirstCol + buttonWidthRow + buttonDistRow;
      options = new JButton (optionsButtonName);
      options.setFont(dialogFont);
      options.setBounds(xButtonRow, yFirstCol, buttonWidthRow, buttonHigh);
      options.addActionListener(this);
      options.setActionCommand("options");
      dialog.add(options);
      
      xButtonRow += buttonWidthRow + buttonDistRow;
      undo = new JButton (undoButtonName);
      undo.setFont(dialogFont);
      undo.setBounds(xButtonRow, yFirstCol, buttonWidthRow, buttonHigh);
      undo.addActionListener(this);
      undo.setActionCommand("undo");
      dialog.add(undo);
      
      xButtonRow += buttonWidthRow + buttonDistRow;
      close = new JButton (closeButtonName);
      close.setFont(dialogFont);
      close.setBounds(xButtonRow, yFirstCol, buttonWidthRow, buttonHigh);
      close.addActionListener(this);
      close.setActionCommand("close");
      dialog.add(close);
      
      int ySecondCol = 2 * disFirstCol + 30;
      more = new JButton (moreButtonName);
      more.setBounds(begSecondCol, ySecondCol, buttonWidthCol, buttonHigh);
      more.setFont(dialogFont);
      more.addActionListener(this);
      more.setActionCommand("more");
      dialog.add(more);
      
      ySecondCol += disFirstCol + 40;
      ignoreOnce = new JButton (ignoreButtonName);
      ignoreOnce.setFont(dialogFont);
      ignoreOnce.setBounds(begSecondCol, ySecondCol, buttonWidthCol, buttonHigh);
      ignoreOnce.addActionListener(this);
      ignoreOnce.setActionCommand("ignoreOnce");
      dialog.add(ignoreOnce);
      
      ySecondCol += buttonDistCol + buttonHigh;
      ignoreAll = new JButton (ignoreAllButtonName);
      ignoreAll.setFont(dialogFont);
      ignoreAll.setBounds(begSecondCol, ySecondCol, buttonWidthCol, buttonHigh);
      ignoreAll.addActionListener(this);
      ignoreAll.setActionCommand("ignoreAll");
      dialog.add(ignoreAll);
      
      ySecondCol += buttonDistCol + buttonHigh;
      deactivateRule = new JButton (deactivateRuleButtonName);
      deactivateRule.setFont(dialogFont);
      deactivateRule.setBounds(begSecondCol, ySecondCol, buttonWidthCol, buttonHigh);
      deactivateRule.setVisible(false);
      deactivateRule.addActionListener(this);
      deactivateRule.setActionCommand("deactivateRule");
      dialog.add(deactivateRule);
      
      addToDictionary = new JComboBox<String> (userDictionaries);
      addToDictionary.setFont(dialogFont);
      addToDictionary.setBounds(begSecondCol, ySecondCol, buttonWidthCol, buttonHigh);
      addToDictionary.addItemListener(e -> {
        if (e.getStateChange() == ItemEvent.SELECTED) {
          if (addToDictionary.getSelectedIndex() > 0) {
            String dictionary = (String) addToDictionary.getSelectedItem();
            documents.getLtDictionary().addWordToDictionary(dictionary, wrongWord, xContext);
            addUndo(y, "addToDictionary", dictionary, wrongWord);
            addToDictionary.setSelectedIndex(0);
            gotoNextError(true);
          }
        }
      });

      dialog.add(addToDictionary);
      
      ySecondCol += 4*buttonDistCol + buttonHigh;
      change = new JButton (changeButtonName);
      change.setFont(dialogFont);
      change.setBounds(begSecondCol, ySecondCol, buttonWidthCol, buttonHigh);
      change.addActionListener(this);
      change.setActionCommand("change");
      dialog.add(change);
      
      ySecondCol += buttonDistCol + buttonHigh;
      changeAll = new JButton (changeAllButtonName);
      changeAll.setFont(dialogFont);
      changeAll.setBounds(begSecondCol, ySecondCol, buttonWidthCol, buttonHigh);
      changeAll.addActionListener(this);
      changeAll.setActionCommand("changeAll");
      changeAll.setEnabled(false);
      dialog.add(changeAll);

      dialog.addWindowFocusListener(new WindowFocusListener() {
        @Override
        public void windowGainedFocus(WindowEvent e) {
          if (focusLost) {
//            MessageHandler.printToLogFile("Check Dialog: Window Focus gained: Event = " + e.paramString());
            currentDocument = documents.getCurrentDocument();
            String newDocId = currentDocument.getDocID();
            if (!docId.equals(newDocId)) {
              docId = newDocId;
              undoList = new ArrayList<UndoContainer>();
            }
            dialog.setEnabled(false);
            initCursor();
            gotoNextError(false);
            dialog.setEnabled(true);
            focusLost = false;
          }
        }
        @Override
        public void windowLostFocus(WindowEvent e) {
//          MessageHandler.printToLogFile("Check Dialog: Window Focus lost: Event = " + e.paramString());
          focusLost = true;
        }
      });
    }

    public void show() {
      if (debugMode) {
        MessageHandler.printToLogFile("Check Dialog: Goto next Error");
      }
      dialog.setEnabled(false);
      initCursor();
      gotoNextError(false);
      dialog.setEnabled(true);
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = dialog.getSize();
      dialog.setLocation(screenSize.width / 2 - frameSize.width / 2,
          screenSize.height / 2 - frameSize.height / 2);
      documents.setLtDialog(this);
      dialog.setAutoRequestFocus(true);
      dialog.setVisible(true);
    }
    
    private void initCursor() {
      viewCursor = new ViewCursorTools(xContext);
      XTextCursor tCursor = viewCursor.getTextCursorBeginn();
      tCursor.gotoStart(true);
      int nBegin = tCursor.getString().length();
      tCursor = viewCursor.getTextCursorEnd();
      tCursor.gotoStart(true);
      int nEnd = tCursor.getString().length();
      if (nBegin < nEnd) {
        endOfRange = nEnd;
      }
    }
    
    private void gotoNextError(boolean startAtBegin) {
      error = getNextError(startAtBegin);
      if (sentenceIncludeError == null || errorDescription == null || suggestions == null) {
        MessageHandler.printToLogFile("SentenceIncludeError == null || errorDescription == null || suggestions == null");
      } else if (error != null) {
        
        ignoreOnce.setEnabled(true);
        ignoreAll.setEnabled(true);

        isSpellError = error.aRuleIdentifier.equals(spellRuleId);

        sentenceIncludeError.setText(currentDocument.getDocumentCache().getTextParagraph(y));
        setAttributesForErrorText(error);

        errorDescription.setText(error.aFullComment);
        
        if (error.aSuggestions != null && error.aSuggestions.length > 0) {
          suggestions.setListData(error.aSuggestions);
          suggestions.setSelectedIndex(0);
          change.setEnabled(true);
          changeAll.setEnabled(true);
        } else {
          suggestions.setListData(new String[0]);
          change.setEnabled(false);
          changeAll.setEnabled(false);
        }
        
        language.setSelectedItem(langTool.getLanguage().getTranslatedName(messages));
        
        if (isSpellError) {
          addToDictionary.setVisible(true);
          changeAll.setVisible(true);
          deactivateRule.setVisible(false);
        } else {
          addToDictionary.setVisible(false);
          changeAll.setVisible(false);
          deactivateRule.setVisible(true);
          deactivateRule.setEnabled(true);
        }
        informationUrl = getUrl(error);
        more.setVisible(informationUrl != null);
        undo.setEnabled(undoList != null && !undoList.isEmpty());
      } else {
        ignoreOnce.setEnabled(false);
        ignoreAll.setEnabled(false);
        deactivateRule.setEnabled(false);
        change.setEnabled(false);
        changeAll.setVisible(false);
        addToDictionary.setVisible(false);
        deactivateRule.setVisible(true);
        more.setVisible(false);
        focusLost = false;
      }
    }
    
    private void setUserDictionaries () {
      String[] tmpDictionaries = documents.getLtDictionary().getUserDictionaries(xContext);
      userDictionaries = new String[tmpDictionaries.length + 1];
      userDictionaries[0] = addToDictionaryName;
      for (int i = 0; i < tmpDictionaries.length; i++) {
        userDictionaries[i + 1] = tmpDictionaries[i];
      }
    }
    
    private String[] getPossibleLanguages() {
      List<String> languages = new ArrayList<>();
      for (Language lang : Languages.get()) {
        languages.add(lang.getTranslatedName(messages));
        languages.sort(null);
      }
      return languages.toArray(new String[languages.size()]);
    }
    
    private void setAttributesForErrorText(SingleProofreadingError error) {
      //  Get Attributes
      MutableAttributeSet attrs = sentenceIncludeError.getInputAttributes();
      StyledDocument doc = sentenceIncludeError.getStyledDocument();
      //  Set back to default values
      StyleConstants.setBold(attrs, false);
      StyleConstants.setUnderline(attrs, false);
      StyleConstants.setForeground(attrs, Color.BLACK);
      doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, true);
      //  Set values for error
      StyleConstants.setBold(attrs, true);
      StyleConstants.setUnderline(attrs, true);
      Color color = null;
      if (isSpellError) {
        color = Color.RED;
      } else {
        PropertyValue[] properties = error.aProperties;
        for(PropertyValue property : properties) {
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

    private String getUrl(SingleProofreadingError error) {
      if (!isSpellError) {
        PropertyValue[] properties = error.aProperties;
        for(PropertyValue property : properties) {
          if ("FullCommentURL".equals(property.Name)) {
            String url = new String((String) property.Value);
            return url;
          }
        }
      }
      return null;
    }

    private SingleProofreadingError getNextError(boolean startAtBegin) {
      currentDocument = documents.getCurrentDocument();
      if (currentDocument == null) {
        return null;
      }
      XComponent xComponent = currentDocument.getXComponent();
      DocumentCursorTools docCursor = new DocumentCursorTools(xComponent);
      FlatParagraphTools flatPara = currentDocument.getFlatParagraphTools();
      if (flatPara == null) {
        flatPara = new FlatParagraphTools(xComponent);
      } else {
        flatPara.init();
      }
      DocumentCache docCache = new DocumentCache(docCursor, flatPara, -1);
      if (docCache.size() <= 0) {
        return null;
      }
      long nChars = 0;
      y = viewCursor.getViewCursorParagraph();
      x = viewCursor.getViewCursorCharacter();
      if (startAtBegin) {
        nChars = -x;
        x = 0;
      }
      int nStart = 0;
      for (int i = 0; i <= y; i++) {
        nStart += docCache.getTextParagraph(i).length() + 1;
      }
      SingleProofreadingError nextError = getNextErrorInParagraph (x, y, docCache, currentDocument, docCursor, ignoredSpellMatches);
      int pLength = docCache.getTextParagraph(y).length() + 1;
      while (y < docCache.textSize() - 1 && nextError == null && (endOfRange < 0 || nStart < endOfRange)) {
        nChars += pLength;
        y++;
        nextError = getNextErrorInParagraph (0, y, docCache, currentDocument, docCursor,ignoredSpellMatches);
        pLength = docCache.getTextParagraph(y).length() + 1;
        nStart += pLength;
      }
      if (nextError != null && (endOfRange < 0 || nStart - pLength + nextError.nErrorStart < endOfRange)) {
        if (nextError.aRuleIdentifier.equals(spellRuleId)) {
          wrongWord = docCache.getTextParagraph(y).substring(nextError.nErrorStart, nextError.nErrorStart + nextError.nErrorLength);
        }
//        MessageHandler.printToLogFile("endOfRange:" + endOfRange + "; ErrorStart(" + nStart + "/" + pLength + "/" 
//              + nextError.nErrorStart + "): " + (nStart - pLength + nextError.nErrorStart));
        nChars += nextError.nErrorStart - x;
        setViewCursor(nChars, viewCursor);
      } else {
//        MessageHandler.printToLogFile("endOfRange:" + endOfRange + "; ErrorStart(" + nStart + "/" + pLength + "/" 
//              + (nextError != null ? nextError.nErrorStart : 0) +"): " + (nStart - pLength + (nextError != null ? nextError.nErrorStart : 0)));
        if (endOfRange < 0) {
          MessageHandler.showMessage(messages.getString("guiCheckComplete"));
        } else {
          MessageHandler.showMessage(messages.getString("guiSelectionCheckComplete"));
        }
        return null;
      }
      return nextError;
    }

    @Override
    public void actionPerformed(ActionEvent action) {
      if (action.getActionCommand().equals("close")) {
        closeDialog();
      } else if (action.getActionCommand().equals("ignoreOnce")) {
        ignoreOnce();
      } else if (action.getActionCommand().equals("ignoreAll")) {
        ignoreAll();
      } else if (action.getActionCommand().equals("deactivateRule")) {
        deactivateRule();
      } else if (action.getActionCommand().equals("change")) {
        changeText();
      } else if (action.getActionCommand().equals("changeAll")) {
        changeAll();
      } else if (action.getActionCommand().equals("undo")) {
        undo();
      } else if (action.getActionCommand().equals("more")) {
        Tools.openURL(informationUrl);
      } else if (action.getActionCommand().equals("options")) {
        documents.runOptionsDialog();
      } else {
        MessageHandler.showMessage("Action '" + action.getActionCommand() + "' not supported");
      }
    }
    
    public void closeDialog() {
      if (debugMode) {
        MessageHandler.printToLogFile("Close Spell And Grammar Check Dialog");
      }
      undoList = null;
      documents.setLtDialog(null);
      dialog.setVisible(false);
    }
    
    private void ignoreOnce() {
      y = viewCursor.getViewCursorParagraph();
      x = viewCursor.getViewCursorCharacter();
      if (isSpellError) {
        if (ignoredSpellMatches.containsKey(y)) {
          Set<Integer> charNums = ignoredSpellMatches.get(y);
          charNums.add(x);
          ignoredSpellMatches.put(y, charNums);
        } else {
          Set<Integer> charNums = new HashSet<>();
          charNums.add(x);
          ignoredSpellMatches.put(y, charNums);
        }
        //  TODO: Delete marks inside document
      } else {
        currentDocument.setIgnoredMatch(x, y);
      }
      addUndo(x, y, "ignoreOnce", error.aRuleIdentifier);
      gotoNextError(true);
    }
    
    private void ignoreAll() {
      if (isSpellError) {
        if (debugMode) {
          MessageHandler.printToLogFile("Ignored word: " + wrongWord);
        }
        documents.getLtDictionary().addIgnoredWord(wrongWord);
      } else {
        documents.ignoreRule(error.aRuleIdentifier, locale);
        documents.resetDocument();
      }
      addUndo(0, "ignoreAll", error.aRuleIdentifier);
      gotoNextError(true);
    }

    private void deactivateRule() {
      if (!isSpellError) {
        documents.deactivateRule(error.aRuleIdentifier, false);
        documents.resetDocument();
        addUndo(0, "deactivateRule", error.aRuleIdentifier);
      }
      gotoNextError(true);
    }
    
    private int getDifferenceFromBegin(String text1, String text2) {
      for (int i = 0; i < text1.length() && i < text2.length(); i++) {
        if (text1.charAt(i) != text2.charAt(i)) {
          return i;
        }
      }
      return (text1.length() < text2.length() ? text1.length() : text2.length());
    }

    private int getDifferenceFromEnd(String text1, String text2) {
      for (int i = 1; i <= text1.length() && i <= text2.length(); i++) {
        if (text1.charAt(text1.length() - i) != text2.charAt(text2.length() - i)) {
          return text1.length() - i + 1;
        }
      }
      return (text1.length() < text2.length() ? 0 : text1.length() - text2.length());
    }

    private void changeText() {
      XParagraphCursor pCursor = viewCursor.getParagraphCursorFromViewCursor();
      pCursor.gotoStartOfParagraph(false);
      pCursor.gotoEndOfParagraph(true);
      String orgText = pCursor.getString();
      pCursor.gotoStartOfParagraph(false);
      String dialogText = sentenceIncludeError.getText();
      String word;
      String replace;
      if (!orgText.equals(dialogText)) {
        int firstChange = getDifferenceFromBegin(orgText, dialogText);
        int lastEqual = getDifferenceFromEnd(orgText, dialogText);
        pCursor.goRight((short)firstChange, false);
        pCursor.goRight((short)(lastEqual - firstChange), true);
        int lastDialogEqual = dialogText.length() - orgText.length() + lastEqual;
        replace = dialogText.substring(firstChange, lastDialogEqual);
      } else if (suggestions.getComponentCount() > 0) {
        pCursor.goRight((short)error.nErrorStart, false);
        pCursor.goRight((short)error.nErrorLength, true);
        replace = suggestions.getSelectedValue();
      } else {
        MessageHandler.printToLogFile("No text selected to change");
        return;
      }
      word = pCursor.getString();
      pCursor.setString(replace);
      addSingleChangeUndo(error.nErrorStart, y, word, replace);
      if (debugMode) {
        MessageHandler.printToLogFile("Org: " + word + "\nDia: " + replace);
      }
      gotoNextError(true);
    }
    
    private void changeAll() {
      if (suggestions.getComponentCount() > 0) {
        String orgText = sentenceIncludeError.getText();
        String word = orgText.substring(error.nErrorStart, error.nErrorStart + error.nErrorLength);
        String replace = suggestions.getSelectedValue();
        XComponent xComponent = currentDocument.getXComponent();
        DocumentCursorTools docCursor = new DocumentCursorTools(xComponent);
        Map<Integer, List<Integer>> orgParas = spellChecker.replaceAllWordsInText(word, replace, docCursor);
        if (orgParas != null) {
          addChangeUndo(y, word, replace, orgParas);
        }
        gotoNextError(true);
      }
    }
    
    private void addUndo(int y, String action, String ruleId) {
      addUndo(0, y, action, ruleId);
    }
    
    private void addUndo(int x, int y, String action, String ruleId) {
      addUndo(x, y, action, ruleId, null);
    }
    
    private void addUndo(int y, String action, String ruleId, String word) {
      addUndo(0, y, action, ruleId, word, null);
    }
    
    private void addUndo(int x, int y, String action, String ruleId, Map<Integer, List<Integer>> orgParas) {
      addUndo(x, y, action, ruleId, null, orgParas);
    }
    
    private void addUndo(int x, int y, String action, String ruleId, String word, Map<Integer, List<Integer>> orgParas) {
      if (undoList.size() >= maxUndos) {
        undoList.remove(0);
      }
      undoList.add(new UndoContainer(x, y, action, ruleId, word, orgParas));
    }
    
    private void addChangeUndo(int y, String word, String replace, Map<Integer, List<Integer>> orgParas) {
      addUndo(0, y, "change", replace, word, orgParas);
    }
    
    private void addSingleChangeUndo(int x, int y, String word, String replace) {
      Map<Integer, List<Integer>> paraMap = new HashMap<Integer, List<Integer>>();
      List<Integer> xVals = new ArrayList<Integer>();
      xVals.add(x);
      paraMap.put(y, xVals);
      addChangeUndo(y, word, replace, paraMap);
    }

    private void undo() {
      if (undoList == null || undoList.isEmpty()) {
        return;
      }
      int nLastUndo = undoList.size() - 1;
      UndoContainer lastUndo = undoList.get(nLastUndo);
      String action = lastUndo.action;
      int xUndo = lastUndo.x;
      int yUndo = lastUndo.y;
      if (action == "ignoreOnce") {
        if (isSpellError) {
          if (ignoredSpellMatches.containsKey(yUndo)) {
            Set<Integer> charNums = ignoredSpellMatches.get(yUndo);
            if (charNums.contains(xUndo)) {
              if (charNums.size() < 2) {
                ignoredSpellMatches.remove(yUndo);
              } else {
                charNums.remove(xUndo);
                ignoredSpellMatches.put(yUndo, charNums);
              }
            }
          }
          //  TODO: Delete marks inside document
        } else {
          currentDocument.removeIgnoredMatch(xUndo, yUndo);
        }
      } else if (action == "ignoreAll") {
        if (lastUndo.ruleId.equals(spellRuleId)) {
          if (debugMode) {
            MessageHandler.printToLogFile("Ignored word removed: " + wrongWord);
          }
          documents.getLtDictionary().removeIgnoredWord(wrongWord);
        } else {
          documents.removeDisabledRule(lastUndo.ruleId);
          documents.setRecheck();
          documents.resetDocument();
        }
      } else if (action == "deactivateRule") {
        documents.deactivateRule(lastUndo.ruleId, true);
        documents.resetDocument();
      } else if (action == "addToDictionary") {
        documents.getLtDictionary().removeWordFromDictionary(lastUndo.ruleId, lastUndo.word, xContext);
      } else if (action == "change") {
        XComponent xComponent = currentDocument.getXComponent();
        DocumentCursorTools docCursor = new DocumentCursorTools(xComponent);
        XParagraphCursor pCursor = docCursor.getParagraphCursor();
        Map<Integer, List<Integer>> paras = lastUndo.orgParas;
        short length = (short) lastUndo.ruleId.length();
        for(int n : paras.keySet()) {
          List<Integer> xStarts = paras.get(n);
          pCursor.gotoStart(false);
          for(int i = 0; i < n; i++) {
            pCursor.gotoNextParagraph(false);
          }
          for(int xStart : xStarts) {
            pCursor.gotoStartOfParagraph(false);
            pCursor.goRight((short)xStart, false);
            pCursor.goRight(length, true);
            pCursor.setString(lastUndo.word);
          }
        }
      } else {
        MessageHandler.showMessage("Undo '" + action + "' not supported");
      }
      undoList.remove(nLastUndo);
      setViewCursor(xUndo, yUndo, viewCursor);
      gotoNextError(true);
    }

  }

}
