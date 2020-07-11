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
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
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
import com.sun.star.xml.dom.XText;

/**
 * Class defines the spell and grammar check dialog
 * @since 5.1
 * @author Fred Kruse
 */
public class SpellAndGrammarCheckDialog extends Thread {
  
  private static final String spellingError = "Spelling Error";
  private static final String spellRuleId = "SpellingError";
  private final static boolean test = true;
  private XComponentContext xContext;
  private MultiDocumentsHandler documents;
  private SwJLanguageTool langTool;
  private ExtensionSpellChecker spellChecker;
  private Locale locale;
  
  SpellAndGrammarCheckDialog(XComponentContext xContext, MultiDocumentsHandler documents) {
    this.xContext = xContext;
    this.documents = documents;
    spellChecker = new ExtensionSpellChecker();
    langTool = documents.getLanguageTool();
  }
  
  @Override
  public void run() {
    if (!documents.javaVersionOkay()) {
      return;
    }
    LtCheckDialog checkDialog = new LtCheckDialog(xContext);
    checkDialog.show();
  }
  
  public void nextError() {
    SingleDocument document = documents.getCurrentDocument();
    if (document == null) {
      return;
    }
    XComponent xComponent = document.getXComponent();
    DocumentCursorTools docCursor = new DocumentCursorTools(xComponent);
    FlatParagraphTools flatPara = document.getFlatParagraphTools();
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
    MessageHandler.printToLogFile("nChars = " + nChars + "; x = " + x + "; y = " + y + "; xCur = " + xCur + "; yCur = " + yCur);
    setViewCursor(nChars, viewCursor);
  }
  
  private SingleProofreadingError getNextErrorInParagraph (int x, int y, DocumentCache docCache, SingleDocument document, 
      DocumentCursorTools docTools, Map<Integer, Set<Integer>> ignoredSpellMatches) {
    String text = docCache.getTextParagraph(y);
    locale = docCache.getTextParagraphLocale(y);
    int[] footnotePosition = docCache.getTextParagraphFootnotes(y);

    SingleProofreadingError sError = getNextSpellErrorInParagraph (x, y, locale, docTools, ignoredSpellMatches);
    SingleProofreadingError gError = getNextGrammatikErrorInParagraph(x, y, text, footnotePosition, locale, document);
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
            if (!isIgnoredMatch (wordBegin, wordBegin + wordLength, numPara, ignoredSpellMatches)) {
              SingleProofreadingError aError = new SingleProofreadingError();
              aError.nErrorType = TextMarkupType.SPELLCHECK;
              aError.aFullComment = spellingError;
              aError.aShortComment = aError.aFullComment;
              aError.nErrorStart = wordBegin;
              aError.nErrorLength = wordLength;
              aError.aRuleIdentifier = spellRuleId;
              errorArray.add(aError);
  //            MessageHandler.printToLogFile("Error: Word: " + token.getToken() + ", Start: " + aError.nErrorStart);
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

    public Map<Integer, String> replaceAllWordsInText(String word, String replace, DocumentCursorTools cursorTools) {
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
      
      public String getNextWord () {
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
        if (tmpWord.endsWith(".")) {
          wCursor.goLeft((short)1, false);
          wCursor.gotoStartOfWord(true);
          tmpWord = wCursor.getString();
        }
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
      
      public Map<Integer, String> replaceWordInText (String oWord, String replace) {
        Map<Integer, String> paraMap = null;
        pCursor.gotoStart(false);
        boolean pRes = true;
        int nPara = 0;
        while (pRes) {
          pCursor.gotoStartOfParagraph(false);
          pCursor.gotoEndOfParagraph(true);
          String paraText = pCursor.getString();
          paraLength = paraText.length();
          MessageHandler.printToLogFile("Paragraph (" + paraLength + "):" + pCursor.getString());
          pCursor.gotoStartOfParagraph(false);
          wordStart = 0;
          wordLength = 0;
          boolean wRes = wCursor.gotoEndOfWord(false);
          while (wRes) {
            wCursor.gotoStartOfWord(false);
            wCursor.gotoEndOfWord(true);
            String tmpWord = wCursor.getString();
            if (tmpWord.endsWith(".")) {
              wCursor.goLeft((short)1, false);
              wCursor.gotoStartOfWord(true);
              tmpWord = wCursor.getString();
            }
            if (oWord.equals(tmpWord)) {
              paraMap = addChangeToUndoMap(nPara, paraText, paraMap);
              wCursor.setString(replace);
              tmpWord = replace;
            }
            wCursor.gotoStartOfWord(false);
            XTextRange startOfWord = wCursor.getStart();
            pCursor.gotoStartOfParagraph(true);
            int nStart = pCursor.getString().length();
            pCursor.gotoRange(startOfWord, false);
            if (tmpWord.isEmpty() || nStart < wordStart + wordLength 
                || wordStart + wordLength + tmpWord.length() > paraLength) {
              break;
            }
            MessageHandler.printToLogFile(tmpWord);
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
      
      private Map<Integer, String> addChangeToUndoMap(int y, String orgPara, Map<Integer, String> paraMap) {
        if (paraMap == null) {
          paraMap = new HashMap<Integer, String>();
        }
        if (!paraMap.containsKey(y)) {
          paraMap.put(y, orgPara);
        }
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
    public Map<Integer, String> orgParas;
    
    UndoContainer(int x, int y, String action, String ruleId, String word, Map<Integer, String> orgParas) {
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
    private final ResourceBundle messages = JLanguageTool.getMessageBundle();
    private final static int maxUndos = 20;
    private final static String dialogName = "Spell and Grammar Check Dialog"; 
    private final static String labelLanguage = "Language of Text:"; 
    private final static String labelSuggestions = "Suggestions:"; 
    private final static String moreButtonName = "More Information"; 
    private final static String ignoreButtonName = "Ignore"; 
    private final static String ignoreAllButtonName = "Ignore All"; 
    private final static String deactivateRuleButtonName = "Deactivate Rule"; 
    private final static String addToDictionaryName = "Add to Dictionary";
    private final static String changeButtonName = "Change"; 
    private final static String changeAllButtonName = "Change All"; 
    private final static String helpButtonName = "Help"; 
    private final static String optionsButtonName = "Options"; 
    private final static String undoButtonName = "Undo"; 
    private final static String closeButtonName = "Close"; 
    private JDialog dialog;
    private JLabel languageLabel;
    private JComboBox<String> language;
    private JTextArea errorDescription;
    private JTextPane sentenceIncludeError;
    private JLabel suggestionsLabel;
    private JList<String> suggestions;
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
    private int x;
    private int y;
    private boolean isSpellError = false;
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
      MessageHandler.printToLogFile("LtCheckDialog called");
      currentDocument = documents.getCurrentDocument();
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
      dialog.setSize(640, 440);

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
      
      yFirstCol += 2 * disFirstCol + 100;
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
    }

    public void show() {
      gotoNextError(false);
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = dialog.getSize();
      dialog.setLocation(screenSize.width / 2 - frameSize.width / 2,
          screenSize.height / 2 - frameSize.height / 2);
      documents.setLtDialog(this);
      dialog.setAutoRequestFocus(true);
      dialog.setVisible(true);
    }
    
    private void gotoNextError(boolean startAtBegin) {
      error = getNextError(startAtBegin);
      if (sentenceIncludeError == null || errorDescription == null || suggestions == null) {
        MessageHandler.printToLogFile("SentenceIncludeError == null || errorDescription == null || suggestions == null");
      } else if (error != null) {
        
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
        }
        informationUrl = getUrl(error);
        more.setVisible(informationUrl != null);
        undo.setEnabled(undoList != null && !undoList.isEmpty());
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
      DocumentCache docCache = new DocumentCache(docCursor, flatPara, -1);
      if (docCache.size() <= 0) {
        return null;
      }
      viewCursor = new ViewCursorTools(xContext);
      long nChars = 0;
      y = viewCursor.getViewCursorParagraph();
      x = viewCursor.getViewCursorCharacter();
      if (startAtBegin) {
        nChars = -x;
        x = 0;
      }
      SingleProofreadingError nextError = getNextErrorInParagraph (x, y, docCache, currentDocument, docCursor, ignoredSpellMatches);
      while (y < docCache.textSize() - 1 && nextError == null) {
        nChars += docCache.getTextParagraph(y).length() + 1;
        y++;
        nextError = getNextErrorInParagraph (0, y, docCache, currentDocument, docCursor,ignoredSpellMatches);
      }
      if (nextError != null) {
        if (nextError.aRuleIdentifier.equals(spellRuleId)) {
          wrongWord = docCache.getTextParagraph(y).substring(nextError.nErrorStart, nextError.nErrorStart + nextError.nErrorLength);
        }
        nChars += nextError.nErrorStart - x + 1;
        setViewCursor(nChars, viewCursor);
      } else {
        MessageHandler.showMessage("End of document is reached");  // TODO: Add language specific message
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
      undoList = null;
      documents.setLtDialog(null);
      dialog.setVisible(false);
      MessageHandler.printToLogFile("Close Spell And Grammar Check Dialog");
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
        MessageHandler.printToLogFile("Ignored word: " + wrongWord);
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

    private void changeText() {
      XParagraphCursor pCursor = viewCursor.getParagraphCursorFromViewCursor();
      pCursor.gotoStartOfParagraph(false);
      pCursor.gotoEndOfParagraph(true);
      String orgText = pCursor.getString();
      addSingleChangeUndo(y, orgText);
      String dialogText = sentenceIncludeError.getText();
      if (!orgText.equals(dialogText)) {
        pCursor.setString(dialogText);
        MessageHandler.printToLogFile("Org: " + orgText + "\nDia: " + dialogText);
      } else if (suggestions.getComponentCount() > 0) {
        String newText = orgText.substring(0, error.nErrorStart) + suggestions.getSelectedValue() 
          + orgText.substring(error.nErrorStart + error.nErrorLength);
        pCursor.setString(newText);
        MessageHandler.printToLogFile("Org: " + orgText + "\nNew: " + newText);
      } else {
        MessageHandler.printToLogFile("No text selected to change");
        return;
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
        Map<Integer, String> orgParas = spellChecker.replaceAllWordsInText(word, replace, docCursor);
        if (orgParas != null) {
          addChangeUndo(y, orgParas);
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
    
    private void addUndo(int x, int y, String action, String ruleId, Map<Integer, String> orgParas) {
      addUndo(x, y, action, ruleId, null, orgParas);
    }
    
    private void addUndo(int x, int y, String action, String ruleId, String word, Map<Integer, String> orgParas) {
      if (undoList.size() >= maxUndos) {
        undoList.remove(0);
      }
      undoList.add(new UndoContainer(x, y, action, ruleId, word, orgParas));
    }
    
    private void addChangeUndo(int y, Map<Integer, String> orgParas) {
      addUndo(0, y, "change", null, orgParas);
    }
    
    private void addSingleChangeUndo(int y, String orgPara) {
      Map<Integer, String> paraMap = new HashMap<Integer, String>();
      paraMap.put(y, orgPara);
      addChangeUndo(y, paraMap);
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
          MessageHandler.printToLogFile("Ignored word removed: " + wrongWord);
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
        Map<Integer, String> paras = lastUndo.orgParas;
        for(int n : paras.keySet()) {
          String sPara = paras.get(n);
          pCursor.gotoStart(false);
          for(int i = 0; i < n; i++) {
            pCursor.gotoNextParagraph(false);
          }
          pCursor.gotoStartOfParagraph(false);
          pCursor.gotoEndOfParagraph(true);
          pCursor.setString(sPara);
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
