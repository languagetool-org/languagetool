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
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.LineBorder;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;

import com.sun.star.awt.Selection;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XComboBox;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogEventHandler;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XWindow;
import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.lang.Locale;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.text.TextMarkupType;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XWordCursor;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XURLTransformer;

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
  private Locale lang;
  
  SpellAndGrammarCheckDialog(XComponentContext xContext, MultiDocumentsHandler documents) {
    this.xContext = xContext;
    this.documents = documents;
    spellChecker = new ExtensionSpellChecker();
    langTool = documents.getLanguageTool();
    lang = LinguisticServices.getLocale(langTool.getLanguage());
  }
  
  @Override
  public void run() {
    if(!documents.javaVersionOkay()) {
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
    int x = viewCursor.getViewCursorCharacter();
    int y = viewCursor.getViewCursorParagraph();
    long nChars = 0;
//    for (int i = 0; i < y; i++) {
//      nChars += docCache.getTextParagraph(i).length() + 1;
//    }
    SingleProofreadingError nextError = getNextErrorInParagraph (x, y, docCache, document, docCursor);
    while (y < docCache.textSize() - 1 && nextError == null) {
      nChars += docCache.getTextParagraph(y).length() + 1;
      y++;
      nextError = getNextErrorInParagraph (0, y, docCache, document, docCursor);
    }
    if (nextError != null) {
//      String suggestions = "";
//      for (String suggestion : nextError.aSuggestions) {
//        suggestions += (suggestion + " ");
//      }
      nChars += nextError.nErrorStart + 1 - x;
/*      
      MessageHandler.printToLogFile("Error in Para: " + docCache.getTextParagraph(y)
      + "\nStart: " + nextError.nErrorStart + ", Length: " + nextError.nErrorLength
      + ", Comment: " + nextError.aFullComment + "\nSuggestions: " + suggestions 
      + "\nnChars: " + nChars);
*/
      setViewCursor(nChars, viewCursor);
    } else {
      MessageHandler.showMessage("End of document is reached");  // TODO: Add language specific message
    }
  }
  
  private void setViewCursor(long nChars, ViewCursorTools viewCursor)  {
    XTextViewCursor vCursor = viewCursor.getViewCursor();
//    vCursor.gotoStart(false);
    vCursor.collapseToStart();
    while (nChars > Short.MAX_VALUE) {
      vCursor.goRight(Short.MAX_VALUE, false);
//      MessageHandler.showMessage("nChars old: " + nChars);
      nChars -= Short.MAX_VALUE;
//      MessageHandler.showMessage("nChars new: " + nChars);
    }
    vCursor.goRight((short)nChars, false);
  }
  
  private SingleProofreadingError getNextErrorInParagraph (int x, int y, DocumentCache docCache, SingleDocument document, DocumentCursorTools docTools) {
    String text = docCache.getTextParagraph(y);
    Locale locale = docCache.getTextParagraphLocale(y);
    int[] footnotePosition = docCache.getTextParagraphFootnotes(y);

    SingleProofreadingError sError = getNextSpellErrorInParagraph (x, y, locale, docTools);
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
  
  private SingleProofreadingError getNextSpellErrorInParagraph (int x, int y, Locale locale, DocumentCursorTools cursorTools) {
    SingleProofreadingError[] errors = spellChecker.getSpellErrors(y, locale, cursorTools);
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
  
/*  
  private SingleProofreadingError getNextSpellErrorInParagraph (int x, String text, Locale locale) {
    List<String> sentences = langTool.sentenceTokenize(text);
    int start = 0;
    for (String sentence : sentences) {
      SingleProofreadingError[] errors = spellChecker.getSpellErrors(sentence, locale);
      if (errors != null) {
        for (SingleProofreadingError error : errors) {
          if (error.nErrorStart + start >= x) {
            error.nErrorStart += start;
            return error;
          }
        }
      }
      start += sentence.length();
    }
    return null;
  }
*/
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
/*    
    public SingleProofreadingError[] getSpellErrors(String sentence, Locale lang) {
      try {
        List<SingleProofreadingError> errorArray = new ArrayList<SingleProofreadingError>();
        AnalyzedSentence analyzedSentence = langTool.getAnalyzedSentence(sentence);
        AnalyzedTokenReadings[] tokens = analyzedSentence.getTokensWithoutWhitespace();
        for (AnalyzedTokenReadings token : tokens) {
          if(!token.isNonWord() && !token.isImmunized() && !token.isIgnoredBySpeller() && !linguServices.isCorrectSpell(token.getToken(), lang)) {
            SingleProofreadingError aError = new SingleProofreadingError();
            aError.nErrorType = TextMarkupType.SPELLCHECK;
            aError.aFullComment = spellingError;
            aError.aShortComment = aError.aFullComment;
            aError.nErrorStart = token.getStartPos();
            aError.nErrorLength = token.getEndPos() - token.getStartPos();
            aError.aRuleIdentifier = spellRuleId;
            errorArray.add(aError);
//            MessageHandler.printToLogFile("Error: Word: " + token.getToken() + ", Start: " + aError.nErrorStart);
            String[] alternatives = linguServices.getSpellAlternatives(token.getToken(), lang);
            if (alternatives != null) {
              aError.aSuggestions = alternatives;
            } else {
              aError.aSuggestions = new String[0];
            }
          }
        }
        return errorArray.toArray(new SingleProofreadingError[0]);
      } catch (Throwable t) {
        MessageHandler.showError(t);
      }
      return null;
    }
*/
    public SingleProofreadingError[] getSpellErrors(int numPara, Locale lang, DocumentCursorTools cursorTools) {
      try {
        List<SingleProofreadingError> errorArray = new ArrayList<SingleProofreadingError>();
        WordsFromParagraph wParas = new WordsFromParagraph(numPara, cursorTools);
        String word = wParas.getNextWord();
        while (word != null) {
          if(!linguServices.isCorrectSpell(word, lang)) {
            SingleProofreadingError aError = new SingleProofreadingError();
            aError.nErrorType = TextMarkupType.SPELLCHECK;
            aError.aFullComment = spellingError;
            aError.aShortComment = aError.aFullComment;
            aError.nErrorStart = wParas.getBeginOfWord();
            aError.nErrorLength = wParas.getLengthOfWord();
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
          word = wParas.getNextWord();
        }
        return errorArray.toArray(new SingleProofreadingError[0]);
      } catch (Throwable t) {
        MessageHandler.showError(t);
      }
      return null;
    }
 /*   
    public void getWordList(int n, DocumentCursorTools cursorTools) {
      WordsFromParagraph wParas = new WordsFromParagraph(n, cursorTools);
      int j = 0;
      MessageHandler.printToLogFile("Words from Paragraph " + n + "(length = " + wParas.paraLength + "):");
      while (j < 100) {
        String word = wParas.getNextWord();
        if (word == null) {
          return;
        }
        MessageHandler.printToLogFile(word);
        j++;
      }
    }
*/    
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
      
    }
    
  } 
  
  /**
   * Class for dialog to check text for spell and grammar errors
   */
  public class LtCheckDialog implements ActionListener {
    private final ResourceBundle messages = JLanguageTool.getMessageBundle();
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
    SingleProofreadingError error;
    private int x;
    private int y;
    private boolean isSpellError = false;
    
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
      errorDescription.setBounds(begFirstCol, yFirstCol, widFirstCol, 40);
      errorDescription.setEditable(false);
      errorDescription.setLineWrap(true);
      errorDescription.setWrapStyleWord(true);
      errorDescription.setBackground(dialog.getContentPane().getBackground());
      Font descriptionFont = dialogFont.deriveFont(Font.BOLD);
      errorDescription.setFont(descriptionFont);
      dialog.add(errorDescription);

      yFirstCol += disFirstCol + 40;
      sentenceIncludeError = new JTextPane();
      sentenceIncludeError.setFont(dialogFont);
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
      suggestions.setFixedCellHeight((int) (suggestions.getFont().getSize() * 1.2 + 0.5));
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
      
      addToDictionary = new JComboBox<String> ();
      addToDictionary.setFont(dialogFont);
      addToDictionary.setBounds(begSecondCol, ySecondCol, buttonWidthCol, buttonHigh);
      addToDictionary.setPrototypeDisplayValue(addToDictionaryName);
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
      dialog.add(changeAll);
    }
  
    public void show() {
      gotoNextError(false);
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = dialog.getSize();
      dialog.setLocation(screenSize.width / 2 - frameSize.width / 2,
          screenSize.height / 2 - frameSize.height / 2);
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
      }
    }
    
    private void closeDialog() {
      dialog.setVisible(false);
    }
    
    private void ignoreOnce() {
/*      if (isSpellError) {
        XTextViewCursor vCursor = viewCursor.getViewCursor();
        vCursor.goRight((short)error.nErrorLength, true);
        OfficeTools2.dispatchCmd("SpellCheckIgnore", xContext);
      } else {
        currentDocument.ignoreOnce();
      }
*/      gotoNextError(false);

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
          if("LineColor".equals(property.Name)) {
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
      if (startAtBegin) {
        x = 0;   //  TODO: Change if ignoreOnce is completed
        x++;
      } else {
        x = viewCursor.getViewCursorCharacter();
      }
      y = viewCursor.getViewCursorParagraph();
      long nChars = 0;
      SingleProofreadingError nextError = getNextErrorInParagraph (x, y, docCache, currentDocument, docCursor);
      while (y < docCache.textSize() - 1 && nextError == null) {
        nChars += docCache.getTextParagraph(y).length() + 1;
        y++;
        nextError = getNextErrorInParagraph (0, y, docCache, currentDocument, docCursor);
      }
      if (nextError != null) {
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
      } else {
        MessageHandler.showMessage("Action '" + action.getActionCommand() + "' not supported");
      }
    }
    


  }

}
