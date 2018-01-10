/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import com.sun.star.lang.*;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.linguistic2.LinguServiceEvent;
import com.sun.star.linguistic2.LinguServiceEventFlags;
import com.sun.star.text.TextMarkupType;

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.gui.AboutDialog;
import org.languagetool.gui.Configuration;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.linguistic2.XLinguServiceEventBroadcaster;
import com.sun.star.linguistic2.XLinguServiceEventListener;
import com.sun.star.linguistic2.XProofreader;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.task.XJobExecutor;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * LibreOffice/OpenOffice integration.
 *
 * @author Marcin Miłkowski, Fred Kruse
 */
public class Main extends WeakBase implements XJobExecutor,
    XServiceDisplayName, XServiceInfo, XProofreader,
    XLinguServiceEventBroadcaster {

  // Service name required by the OOo API && our own name.
  private static final String[] SERVICE_NAMES = {
          "com.sun.star.linguistic2.Proofreader",
          "org.languagetool.openoffice.Main" };

  // use a different name than the stand-alone version to avoid conflicts:
  private static final String CONFIG_FILE = ".languagetool-ooo.cfg";

  // use a log-file for output of messages and debug information:
  private static final String LOG_FILE = ".LanguageTool.log";

  private static final ResourceBundle MESSAGES = JLanguageTool.getMessageBundle();

  // LibreOffice (since 4.2.0) special tag for locale with variant 
  // e.g. language ="qlt" country="ES" variant="ca-ES-valencia":
  private static final String LIBREOFFICE_SPECIAL_LANGUAGE_TAG = "qlt";

  private static final int MAX_SUGGESTIONS = 15;
  
  private static boolean testMode = false;

  /**
   * Full text Check
   *
   * numParasToCheck: Paragraphs to be checked for full text rules
   * < 0 check full text (time intensive)
   * == 0 check only one paragraph (works like LT Version <= 3.9)
   * > 0 checks numParasToCheck before and after the processed paragraph
   */
  private static final int MAX_DOC = 5;  // Maximal number of parallel document analyzed
  private static final String END_OF_PARAGRAPH = "\n";  //  Paragraph Separator from gciterator.cxx: 0x2029
  private static final boolean debugMode = false;   //  should be false except for testing
  private static final String logLineBreak = System.getProperty("line.separator");  //  LineBreak in Log-File (MS-Windows compatible)

  private final List<XLinguServiceEventListener> xEventListeners;

  private Configuration config;
  private JLanguageTool langTool;
  private Language docLanguage;

  // Rules disabled using the config dialog box rather than Spelling dialog box
  // or the context menu.
  private Set<String> disabledRules;
  private Set<String> disabledRulesUI;

  // Make another instance of JLanguageTool and assign it to langTool if true.
  private boolean recheck;

  private XComponentContext xContext;
  
  private int numParasToCheck = 5;    // will be overwritten by config
  private List<List<String>> allParas = null;     //  List of paragraphs (only readable by parallel thread)
  private List<List<RuleMatch>> fullTextMatches;  //  List of paragraph matches (only readable by parallel thread)
  private List<RuleMatch> paragraphMatchesFirst;  //  List of paragraph matches (main thread)
  private List<RuleMatch> paragraphMatchesSecond; //  List of paragraph matches (parallel thread)
  private List<String> docIDs = null;             //  List of all docIDs of open documents
  private List<LOCursor> loCursor;                //  Save Cursor for the single documents
  private List<Integer> numLastVCPara;            //  Save position of ViewCursor for the single documents
  private List<Integer> numLastFlPara;            //  Save position of FlatParagraph for the single documents
  private String lastDocID = null;                //  docID of last checked document
  private boolean textIsChecked = false;   //  false: check full text again (ignored by parallel thread)
  private boolean doFullTextCheck = true;  //  true: run full text check (not single paragraphs) (unchanged by parallel thread)
  private int divNum;    //  difference between number of paragraphs from cursor and from flatParagraph (unchanged by parallel thread)
  private int numLastVCPara1;  //  last paragraph for parallel thread (View Cursor)
  private int numLastFlPara1;  //  last paragraph for parallel thread (FlatParagraph)
  private int lastParaPos[] =  {0, 0};
  private int lastDocNum = 0;

  private boolean proofIsRunning = false;

  public Main(XComponentContext xCompContext) {
    changeContext(xCompContext);
    xEventListeners = new ArrayList<>();
    initLogFile();
  }

  private void prepareConfig(Language lang) {
    try {
      config = new Configuration(getHomeDir(), CONFIG_FILE, lang);
      numParasToCheck = config.getNumParasToCheck();
      disabledRules = config.getDisabledRuleIds();
      if (disabledRules == null) {
        disabledRules = new HashSet<>();
      }
      disabledRulesUI = new HashSet<>(disabledRules);
    } catch (Throwable t) {
      showError(t);
    }
  }

  public final void changeContext(XComponentContext xCompContext) {
    xContext = xCompContext;
  }

  @Nullable
  private XComponent getXComponent() {
    try {
      XMultiComponentFactory xMCF = xContext.getServiceManager();
      Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
      XDesktop xDesktop = UnoRuntime.queryInterface(XDesktop.class, desktop);
      return xDesktop.getCurrentComponent();
    } catch (Throwable t) {
      showError(t);
      return null;
    }
  }

  /**
   * Checks the language under the cursor. Used for opening the configuration dialog.
   * @return the language under the visible cursor
   */
  @Nullable
  private Language getLanguage() {
    XComponent xComponent = getXComponent();
    Locale charLocale;
    XPropertySet xCursorProps;
    try {
      XModel model = UnoRuntime.queryInterface(XModel.class, xComponent);
      XTextViewCursorSupplier xViewCursorSupplier =
          UnoRuntime.queryInterface(XTextViewCursorSupplier.class, model.getCurrentController());
      XTextViewCursor xCursor = xViewCursorSupplier.getViewCursor();
      if (xCursor.isCollapsed()) { // no text selection
        xCursorProps = UnoRuntime.queryInterface(XPropertySet.class, xCursor);
      } else { // text is selected, need to create another cursor
        // as multiple languages can occur here - we care only
        // about character under the cursor, which might be wrong
        // but it applies only to the checking dialog to be removed
        xCursorProps = UnoRuntime.queryInterface(
            XPropertySet.class,
            xCursor.getText().createTextCursorByRange(xCursor.getStart()));
      }

      // The CharLocale and CharLocaleComplex properties may both be set, so we still cannot know
      // whether the text is e.g. Khmer or Tamil (the only "complex text layout (CTL)" languages we support so far).
      // Thus we check the text itself:
      if (new KhmerDetector().isThisLanguage(xCursor.getText().getString())) {
        return Languages.getLanguageForShortCode("km");
      }
      if (new TamilDetector().isThisLanguage(xCursor.getText().getString())) {
        return Languages.getLanguageForShortCode("ta");
      }

      Object obj = xCursorProps.getPropertyValue("CharLocale");
      if (obj == null) {
        return Languages.getLanguageForShortCode("en-US");
      }
      charLocale = (Locale) obj;
      boolean langIsSupported = false;
      for (Language element : Languages.get()) {
        if (charLocale.Language.equalsIgnoreCase(LIBREOFFICE_SPECIAL_LANGUAGE_TAG)
            && element.getShortCodeWithCountryAndVariant().equalsIgnoreCase(charLocale.Variant)) {
          langIsSupported = true;
          break;
        }
        if (element.getShortCode().equals(charLocale.Language)) {
          langIsSupported = true;
          break;
        }
      }
      if (!langIsSupported) {
        String message = Tools.i18n(MESSAGES, "language_not_supported", charLocale.Language);
        JOptionPane.showMessageDialog(null, message);
        return null;
      }
    } catch (Throwable t) {
      showError(t);
      return null;
    }
    return getLanguage(charLocale);
  }

  private Language getLanguage(Locale locale) {
    try {
      if (locale.Language.equalsIgnoreCase(LIBREOFFICE_SPECIAL_LANGUAGE_TAG)) {
        return Languages.getLanguageForShortCode(locale.Variant);
      } else {
        return Languages.getLanguageForShortCode(locale.Language + "-" + locale.Country);
      }
    } catch (java.lang.IllegalArgumentException e) {
      return Languages.getLanguageForShortCode(locale.Language);
    }
  }

  /**
   * Runs the grammar checker on paragraph text.
   * 
   * @param docID document ID
   * @param paraText paragraph text
   * @param locale Locale the text Locale
   * @param startOfSentencePos start of sentence position
   * @param nSuggestedBehindEndOfSentencePosition end of sentence position
   * @return ProofreadingResult containing the results of the check.
   */
  @Override
  public final ProofreadingResult doProofreading(String docID,
      String paraText, Locale locale, int startOfSentencePos,
      int nSuggestedBehindEndOfSentencePosition,
      PropertyValue[] propertyValues) {
    ProofreadingResult paRes = new ProofreadingResult();
    paRes.nStartOfSentencePosition = startOfSentencePos;
    paRes.nStartOfNextSentencePosition = nSuggestedBehindEndOfSentencePosition;
    paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
    paRes.xProofreader = this;
    paRes.aLocale = locale;
    paRes.aDocumentIdentifier = docID;
    paRes.aText = paraText;
    paRes.aProperties = propertyValues;
    try {
      int[] footnotePositions = getPropertyValues("FootnotePositions", propertyValues);  // since LO 4.3
      paRes = doGrammarCheckingInternal(paraText, locale, paRes, footnotePositions);
      return paRes;
    } catch (Throwable t) {
      showError(t);
      return paRes;
    }
  }

  private int[] getPropertyValues(String propName, PropertyValue[] propertyValues) {
    for (PropertyValue propertyValue : propertyValues) {
      if (propName.equals(propertyValue.Name)) {
        if (propertyValue.Value instanceof int[]) {
          return (int[]) propertyValue.Value;
        } else {
          printToLogFile("Not of expected type int[]: " + propertyValue.Name + ": " + propertyValue.Value.getClass());
        }
      }
    }
    return new int[]{};  // e.g. for LO/OO < 4.3 and the 'FootnotePositions' property
  }

  private ProofreadingResult doGrammarCheckingInternal(
      String paraText, Locale locale, ProofreadingResult paRes, int[] footnotePositions) {

    if (!StringTools.isEmpty(paraText) && hasLocale(locale)) {
      Language langForShortName = getLanguage(locale);
      if (!langForShortName.equals(docLanguage) || langTool == null || recheck) {
        docLanguage = langForShortName;
        initLanguageTool();
      }

      Set<String> disabledRuleIds = config.getDisabledRuleIds();
      if (disabledRuleIds != null) {
        // copy as the config thread may access this as well
        List<String> list = new ArrayList<>(disabledRuleIds);
        for (String id : list) {
          langTool.disableRule(id);
        }
      }
      Set<String> disabledCategories = config.getDisabledCategoryNames();
      if (disabledCategories != null) {
        // copy as the config thread may access this as well
        List<String> list = new ArrayList<>(disabledCategories);
        for (String categoryName : list) {
          langTool.disableCategory(new CategoryId(categoryName));
        }
      }
      Set<String> enabledRuleIds = config.getEnabledRuleIds();
      if (enabledRuleIds != null) {
        // copy as the config thread may access this as well
        List<String> list = new ArrayList<>(enabledRuleIds);
        for (String ruleName : list) {
          langTool.enableRule(ruleName);
        }
      }
      try {
        SentenceFromPara sfp = new SentenceFromPara(paraText, paRes.nStartOfSentencePosition);
        String sentence = sfp.getSentence();
        paRes.nStartOfSentencePosition = sfp.getPosition();
        paRes.nStartOfNextSentencePosition = sfp.getPosition() + sentence.length();
        paRes.nBehindEndOfSentencePosition = paRes.nStartOfNextSentencePosition;
        if (!StringTools.isEmpty(sentence)) {
          AnnotatedText annotatedText = getAnnotatedText(sentence, footnotePositions, paRes);
          List<RuleMatch> ruleMatches = langTool.check(annotatedText, false,
              JLanguageTool.ParagraphHandling.ONLYNONPARA);
          SingleProofreadingError[] pErrors = checkParaRules(paraText, paRes.nStartOfSentencePosition,
              paRes.nStartOfNextSentencePosition, paRes.aDocumentIdentifier);
          int pErrorCount = 0;
          if (pErrors != null) {
            pErrorCount = pErrors.length;
          }
          if (!ruleMatches.isEmpty()) {
            SingleProofreadingError[] errorArray = 
                    new SingleProofreadingError[ruleMatches.size() + pErrorCount];
            int i = 0;
            for (RuleMatch myRuleMatch : ruleMatches) {
              errorArray[i] = createOOoError(myRuleMatch, paRes.nStartOfSentencePosition, 
            		                            sentence.length(), sentence.charAt(sentence.length()-1));
              i++;
            }
            // add para matches
            if (pErrors != null) {
              for (SingleProofreadingError paraError : pErrors) {
                if (paraError != null) {
                  errorArray[i] = paraError;
                  i++;
                }
              }
            }
            Arrays.sort(errorArray, new ErrorPositionComparator());
            paRes.aErrors = errorArray;

          } else {
            if (pErrors != null) {
              paRes.aErrors = pErrors;
            }
          }
        }
      } catch (Throwable t) {
        showError(t);
      }
    }
    return paRes;
  }

  private AnnotatedText getAnnotatedText(String sentence, int[] footnotePos, ProofreadingResult paRes) {
    Set<Integer> correctedPos = new HashSet<>();
    for (int pos : footnotePos) {
      correctedPos.add(pos - paRes.nStartOfSentencePosition);
    }
    AnnotatedTextBuilder annotations = new AnnotatedTextBuilder();
    // not very efficient but simple implementation:
    for (int i = 0; i < sentence.length(); i++) {
      if (correctedPos.contains(i)) {
        annotations.addMarkup("\u200B");
      } else {
        annotations.addText(String.valueOf(sentence.charAt(i)));
      }
    }
    return annotations.build();
  }

  private void initLanguageTool() {
    try {
      prepareConfig(docLanguage);
      // not using MultiThreadedJLanguageTool here fixes "osl::Thread::Create failed", see https://bugs.documentfoundation.org/show_bug.cgi?id=90740:
      langTool = new JLanguageTool(docLanguage, config.getMotherTongue());
      File ngramDirectory = config.getNgramDirectory();
      if (ngramDirectory != null) {
        File ngramLangDir = new File(config.getNgramDirectory(), docLanguage.getShortCode());
        if (ngramLangDir.exists()) {  // user might have ngram data only for some languages and that's okay
          langTool.activateLanguageModelRules(ngramDirectory);
        }
      }
      for (Rule rule : langTool.getAllActiveOfficeRules()) {
        if (rule.isDictionaryBasedSpellingRule()) {
          langTool.disableRule(rule.getId());
        }
        if (rule.useInOffice()) {
          langTool.enableRule(rule.getId());
        }
      }
      recheck = false;
    } catch (Throwable t) {
      showError(t);
    }
  }

  private class SentenceFromPara {
    private int position;
    private String str;
  
    SentenceFromPara(String paraText, int startPos) {
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

  // Fix numbers that are (probably) foot notes. 
  // See https://bugs.freedesktop.org/show_bug.cgi?id=69416
  // non-private for test case
  String cleanFootnotes(String paraText) {
    return paraText.replaceAll("([^\\d][.!?])\\d ", "$1¹ ");
  }

  @Nullable
  private SingleProofreadingError[] checkParaRules(
      String paraText, int startPos,
      int endPos, String docID) {
    
    List<RuleMatch> paragraphMatches = null;
    boolean sameDocID = true;
    int numThread = 0;
    int numCurPara;
    int numCurDoc;
    int paraPos;
    if (proofIsRunning) {
      numThread = 1;          //  parallel Thread (right-click or dialog-box while background iteration is running)
    } else {
      proofIsRunning = true;  // main thread is running
    }
    try {
      if (lastDocID == null || !lastDocID.equals(docID)) {
        numCurDoc = getNumDocID(docID);
        lastDocID = docID;
        sameDocID = false;
      } else {
        numCurDoc = lastDocNum;
      }
      numCurPara = getParaPos(paraText, numThread, numCurDoc);
      paraPos = getStartOfParagraph(numCurPara, numCurDoc);
      if (numThread > 0 || (numThread == 0 
          && (startPos == 0 || paraPos != lastParaPos[numThread] || !sameDocID))) {
        if (paraPos < 0) {                                          //  Position not found; check only Paragraph context
          paragraphMatches = langTool.check(paraText, true,
              JLanguageTool.ParagraphHandling.ONLYPARA);
          if (numThread == 0) {
            paragraphMatchesFirst = paragraphMatches;
          } else {
            paragraphMatchesSecond = paragraphMatches;
          }            
        } else if (numParasToCheck > 0 && !doFullTextCheck) {          //  Check numParasToCheck paragraphs while text is changed
          paragraphMatches = langTool.check(getDocAsString(numCurPara, numCurDoc), true,
              JLanguageTool.ParagraphHandling.ONLYPARA);
          if (numThread == 0) {
            paragraphMatchesFirst = paragraphMatches;
          } else {
            paragraphMatchesSecond = paragraphMatches;
          }            
        }
        else if (!textIsChecked) {                                  //  Check Full Text only if not already checked
          if (debugMode) {
            printToLogFile("check Text again");    
          }
          fullTextMatches.set(numCurDoc, langTool.check(getDocAsString(numCurPara, numCurDoc), true,
              JLanguageTool.ParagraphHandling.ONLYPARA));
          textIsChecked = true;   // mark Text as checked till next change; use saved fullTextMatches
        }
        lastParaPos[numThread] = paraPos;
        lastDocNum = numCurDoc;
        if (paraPos < 0 || (numParasToCheck > 0 && !doFullTextCheck)) {
          if (numThread == 0) {
            paragraphMatches = paragraphMatchesFirst;
          } else {
            paragraphMatches = paragraphMatchesSecond;
          }            
        }
      } else {
        if (paraPos < 0 || (numParasToCheck > 0 && !doFullTextCheck)) {
          if (numThread == 0) {
            paragraphMatches = paragraphMatchesFirst;
          } else {
            paragraphMatches = paragraphMatchesSecond;
          }            
        }
      }
      if ((paraPos < 0 || (numParasToCheck > 0 && !doFullTextCheck)) 
            && paragraphMatches != null && !paragraphMatches.isEmpty()) {
        List<SingleProofreadingError> errorList = new ArrayList<>();
        int textPos = paraPos;
        if (textPos < 0) textPos = 0;
        for (RuleMatch myRuleMatch : paragraphMatches) {
          int startErrPos = myRuleMatch.getFromPos() - textPos;
          int endErrPos = myRuleMatch.getToPos() - textPos;
          if (startErrPos >= startPos && startErrPos <= endPos
              && endErrPos >= startPos && endErrPos <= endPos) {
            errorList.add(createOOoError(myRuleMatch, -textPos, myRuleMatch.getToPos() - textPos, 
                                          paraText.charAt(myRuleMatch.getToPos()-textPos-1)));
          }
        }
        if (!errorList.isEmpty()) {
          SingleProofreadingError[] errorArray = errorList.toArray(new SingleProofreadingError[errorList.size()]);
          Arrays.sort(errorArray, new ErrorPositionComparator());
          if (numThread == 0) {
            proofIsRunning = false;
          }
          return errorArray;
        }
      } else if (doFullTextCheck && fullTextMatches.get(numCurDoc) != null 
          && !fullTextMatches.get(numCurDoc).isEmpty()) {
        List<SingleProofreadingError> errorList = new ArrayList<>();
        int textPos = paraPos;
        if (textPos < 0) textPos = 0;
        for (RuleMatch myRuleMatch : fullTextMatches.get(numCurDoc)) {
          int startErrPos = myRuleMatch.getFromPos() - textPos;
          int endErrPos = myRuleMatch.getToPos() - textPos;
          if (startErrPos >= startPos && startErrPos <= endPos
              && endErrPos >= startPos && endErrPos <= endPos) {
            errorList.add(createOOoError(myRuleMatch, -textPos, myRuleMatch.getToPos() - textPos, 
                                          paraText.charAt(myRuleMatch.getToPos()-textPos-1)));
          }
        }
        if (!errorList.isEmpty()) {
          SingleProofreadingError[] errorArray = errorList.toArray(new SingleProofreadingError[errorList.size()]);
          Arrays.sort(errorArray, new ErrorPositionComparator());
          if (numThread == 0) {
            proofIsRunning = false;
          }
          return errorArray;
        }
      }
    } catch (Throwable t) {
      showError(t);
    }
    if (numThread == 0) {
      proofIsRunning = false;
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
    int numSuggestions = ruleMatch.getSuggestedReplacements().size();
    String[] allSuggestions = ruleMatch.getSuggestedReplacements().toArray(new String[numSuggestions]);
    //  Filter: remove suggestions for override dot at the end of sentences
    //  needed because of error in dialog
    if (lastChar == '.' && (ruleMatch.getToPos() + startIndex) == sentencesLength) {
      int i;
      for (i = 0; i < numSuggestions && i < MAX_SUGGESTIONS 
    		  && allSuggestions[i].charAt(allSuggestions[i].length()-1) == '.'; i++);
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
    // LibreOffice since version 3.5 supports an URL that provides more
    // information about the error,
    // older version will simply ignore the property:
    if (ruleMatch.getRule().getUrl() != null) {
      aError.aProperties = new PropertyValue[] { new PropertyValue(
          "FullCommentURL", -1, ruleMatch.getRule().getUrl().toString(),
          PropertyState.DIRECT_VALUE) };
    } else {
      aError.aProperties = new PropertyValue[0];
    }
    return aError;
  }

  /**
   * We leave spell checking to OpenOffice/LibreOffice.
   * @return false
   */
  @Override
  public final boolean isSpellChecker() {
    return false;
  }

  /**
   * Runs LT options dialog box.
   */
  public final void runOptionsDialog() {
    Language lang = getLanguage();
    if (lang == null) {
      return;
    }
    prepareConfig(lang);
    ConfigThread configThread = new ConfigThread(lang, config, this);
    configThread.start();
    numParasToCheck = config.getNumParasToCheck();
  }

  /**
   * @return An array of Locales supported by LT
   */
  @Override
  public final Locale[] getLocales() {
    try {
      List<Locale> locales = new ArrayList<>();
      for (Language lang : Languages.get()) {
        if (lang.getCountries().length == 0) {
          // e.g. Esperanto
          if (lang.getVariant() != null) {
            locales.add(new Locale(LIBREOFFICE_SPECIAL_LANGUAGE_TAG, "", lang.getShortCodeWithCountryAndVariant()));
          } else {
            locales.add(new Locale(lang.getShortCode(), "", ""));
          }
        } else {
          for (String country : lang.getCountries()) {
            if (lang.getVariant() != null) {
              locales.add(new Locale(LIBREOFFICE_SPECIAL_LANGUAGE_TAG, country, lang.getShortCodeWithCountryAndVariant()));
            } else {
              locales.add(new Locale(lang.getShortCode(), country, ""));
            }
          }
        }
      }
      return locales.toArray(new Locale[locales.size()]);
    } catch (Throwable t) {
      showError(t);
      return new Locale[0];
    }
  }

  /**
   * @return true if LT supports the language of a given locale
   * @param locale The Locale to check
   */
  @Override
  public final boolean hasLocale(Locale locale) {
    try {
      for (Language element : Languages.get()) {
        if (locale.Language.equalsIgnoreCase(LIBREOFFICE_SPECIAL_LANGUAGE_TAG)
            && element.getShortCodeWithCountryAndVariant().equals(locale.Variant)) {
          return true;
        }
        if (element.getShortCode().equals(locale.Language)) {
          return true;
        }
      }
    } catch (Throwable t) {
      showError(t);
    }
    return false;
  }

  /**
   * Add a listener that allow re-checking the document after changing the
   * options in the configuration dialog box.
   * 
   * @param eventListener the listener to be added
   * @return true if listener is non-null and has been added, false otherwise
   */
  @Override
  public final boolean addLinguServiceEventListener(XLinguServiceEventListener eventListener) {
    if (eventListener == null) {
      return false;
    }
    xEventListeners.add(eventListener);
    return true;
  }

  /**
   * Remove a listener from the event listeners list.
   * 
   * @param eventListener the listener to be removed
   * @return true if listener is non-null and has been removed, false otherwise
   */
  @Override
  public final boolean removeLinguServiceEventListener(XLinguServiceEventListener eventListener) {
    if (eventListener == null) {
      return false;
    }
    if (xEventListeners.contains(eventListener)) {
      xEventListeners.remove(eventListener);
      return true;
    }
    return false;
  }

  /**
   * Inform listener (grammar checking iterator) that options have changed and
   * the doc should be rechecked.
   */
  public final void resetDocument() {
    if (!xEventListeners.isEmpty()) {
      for (XLinguServiceEventListener xEvLis : xEventListeners) {
        if (xEvLis != null) {
          LinguServiceEvent xEvent = new LinguServiceEvent();
          xEvent.nEvent = LinguServiceEventFlags.PROOFREAD_AGAIN;
          xEvLis.processLinguServiceEvent(xEvent);
        }
      }
      recheck = true;
      textIsChecked = false;
      disabledRules = config.getDisabledRuleIds();
      if (disabledRules == null) {
        disabledRules = new HashSet<>();
      }
    }
  }

  @Override
  public String[] getSupportedServiceNames() {
    return getServiceNames();
  }

  public static String[] getServiceNames() {
    return SERVICE_NAMES;
  }

  @Override
  public boolean supportsService(String sServiceName) {
    for (String sName : SERVICE_NAMES) {
      if (sServiceName.equals(sName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String getImplementationName() {
    return Main.class.getName();
  }

  public static XSingleComponentFactory __getComponentFactory(String sImplName) {
    SingletonFactory xFactory = null;
    if (sImplName.equals(Main.class.getName())) {
      xFactory = new SingletonFactory();
    }
    return xFactory;
  }

  public static boolean __writeRegistryServiceInfo(XRegistryKey regKey) {
    return Factory.writeRegistryServiceInfo(Main.class.getName(), Main.getServiceNames(), regKey);
  }

  @Override
  public void trigger(String sEvent) {
    if (Thread.currentThread().getContextClassLoader() == null) {
      Thread.currentThread().setContextClassLoader(Main.class.getClassLoader());
    }
    if (!javaVersionOkay()) {
      return;
    }
    try {
      if ("configure".equals(sEvent)) {
        runOptionsDialog();
      } else if ("about".equals(sEvent)) {
        AboutDialogThread aboutThread = new AboutDialogThread(MESSAGES);
        aboutThread.start();
      } else {
        printToLogFile("Sorry, don't know what to do, sEvent = " + sEvent);
      }
    } catch (Throwable e) {
      showError(e);
    }
  }

  private boolean javaVersionOkay() {
    String version = System.getProperty("java.version");
    if (version != null
        && (version.startsWith("1.0") || version.startsWith("1.1")
            || version.startsWith("1.2") || version.startsWith("1.3")
            || version.startsWith("1.4") || version.startsWith("1.5")
            || version.startsWith("1.6"))) {
      DialogThread dt = new DialogThread(
          "Error: LanguageTool requires Java 7.0 or later. Current version: " + version);
      dt.start();
      return false;
    }
    try {
      // do not set look and feel for on Mac OS X as it causes the following error:
      // soffice[2149:2703] Apple AWT Java VM was loaded on first thread -- can't start AWT.
      if (!System.getProperty("os.name").contains("OS X")) {
         // Cross-Platform Look And Feel @since 3.7
         UIManager.setLookAndFeel(
            UIManager.getSystemLookAndFeelClassName());
      }
    } catch (Exception ignored) {
      // Well, what can we do...
    }
    return true;
  }

  static void showError(Throwable e) {
    if (testMode) {
      throw new RuntimeException(e);
    }
    String msg = "An error has occurred in LanguageTool "
        + JLanguageTool.VERSION + ":\n" + e + "\nStacktrace:\n";
    msg += Tools.getFullStackTrace(e);
    String metaInfo = "OS: " + System.getProperty("os.name") + " on "
        + System.getProperty("os.arch") + ", Java version "
        + System.getProperty("java.version") + " from "
        + System.getProperty("java.vm.vendor");
    msg += metaInfo;
    DialogThread dt = new DialogThread(msg);
    e.printStackTrace();
    dt.start();
  }

  private static File getHomeDir() {
    String homeDir = System.getProperty("user.home");
    if (homeDir == null) {
      showError(new RuntimeException("Could not get home directory"));
    }
    return new File(homeDir);
  }

  /**
   * Will throw exception instead of showing errors as dialogs - use only for test cases.
   * @since 2.9
   */
  static void setTestMode(boolean mode) {
    testMode = mode;
  }

  private static class AboutDialogThread extends Thread {

    private final ResourceBundle messages;

    AboutDialogThread(ResourceBundle messages) {
      this.messages = messages;
    }

    @Override
    public void run() {
      // Note: null can cause the dialog to appear on the wrong screen in a
      // multi-monitor setup, but we just don't have a proper java.awt.Component
      // here which we could use instead:
      AboutDialog about = new AboutDialog(messages, null);
      about.show();
    }
  }

  /**
   * Called when "Ignore" is selected e.g. in the context menu for an error.
   */
  @Override
  public void ignoreRule(String ruleId, Locale locale)
      throws IllegalArgumentException {
    // TODO: config should be locale-dependent
    disabledRulesUI.add(ruleId);
    config.setDisabledRuleIds(disabledRulesUI);
    try {
      config.saveConfiguration(langTool.getLanguage());
    } catch (Throwable t) {
      showError(t);
    }
    recheck = true;
    textIsChecked = false;
  }

  /**
   * Called on rechecking the document - resets the ignore status for rules that
   * was set in the spelling dialog box or in the context menu.
   * 
   * The rules disabled in the config dialog box are left as intact.
   */
  @Override
  public void resetIgnoreRules() {
    config.setDisabledRuleIds(disabledRules);
    try {
      config.saveConfiguration(langTool.getLanguage());
    } catch (Throwable t) {
      showError(t);
    }
    recheck = true;
    textIsChecked = false;
  }

  @Override
  public String getServiceDisplayName(Locale locale) {
    return "LanguageTool";
  }

  static class DialogThread extends Thread {
    private final String text;

    DialogThread(String text) {
      this.text = text;
    }

    @Override
    public void run() {
      JOptionPane.showMessageDialog(null, text);
    }
  }

  /*
   * Full Text Check  (Workaround for XProofread interface)
   */
  
  /**
   * Different possibilities of return
   */
  private int returnOneParaCheck() {
    doFullTextCheck = false;   // paragraph position can not be found
    return -1;
  }

  private int returnContinueCheck(boolean isReset, int numCurPara, int numThread) {
    if (numParasToCheck > 0) {
      doFullTextCheck = false;
    } else {
      doFullTextCheck = true;
    }
    if (isReset && numThread == 0) {
      textIsChecked = false;
    }
    return numCurPara;
  }

  private int returnNParaCheck(int numCurPara, int numThread) {
    if (numParasToCheck > 0) {
      doFullTextCheck = false;
    } else {
      doFullTextCheck = true;
    }
    if (numThread == 0) {
      textIsChecked = false;
    }
    return numCurPara;
  }

  /**
   * Reset allParas
   */
  private boolean resetAllParas(LOCursor loCursor, int docNum) {
    allParas.set(docNum, loCursor.getAllTextParagraphs());
    if (allParas.get(docNum) == null || allParas.get(docNum).size() < 1) {
      return false;
    }
    textIsChecked = false;
    return true;
  }

  /**
   * Gives Back the full Text as String
   */
  private String getDocAsString(int numCurPara, int docNum) {
    if (numCurPara < 0 || allParas.get(docNum) == null || allParas.get(docNum).size() < numCurPara - 1) {
      return "";
    }
    int startPos;
    int endPos;
    if (numParasToCheck < 1 || doFullTextCheck) {
      startPos = 0;
      endPos = allParas.get(docNum).size();
    } else {
      startPos = numCurPara - numParasToCheck;
      if (startPos < 0) startPos = 0;
      endPos = numCurPara + numParasToCheck;
      if (endPos > allParas.get(docNum).size()) endPos = allParas.get(docNum).size();
    }
    String docText = allParas.get(docNum).get(startPos);
    for (int i = startPos + 1; i < endPos; i++) {
      docText += END_OF_PARAGRAPH + allParas.get(docNum).get(i);
    }
    return docText;
  }

  /**
   * Gives Back the StartPosition of Paragraph
   */
  private int getStartOfParagraph(int nPara, int docNum) {
    if (allParas.get(docNum) != null && nPara >= 0 && nPara < allParas.get(docNum).size()) {
      int startPos;
      if (numParasToCheck < 1 || doFullTextCheck) {
        startPos = 0;
      } else {
        startPos = nPara - numParasToCheck;
        if (startPos < 0) startPos = 0;
      }
      int pos = 0;
      for (int i = startPos; i < nPara; i++) pos += allParas.get(docNum).get(i).length() + 1;
      return pos;
    }
    return -1;
  }
  
  /**
   * Heuristic try to find next position (dialog box or automatic iteration)
   * Is paragraph same, next not empty after or before   
   */
  private int findNextParaPos(int startPara, String paraStr, int docNum) {
    if (allParas.get(docNum) == null || allParas.get(docNum).size() < 1) {
      return -1;
    }
    if (startPara >= allParas.get(docNum).size() || startPara < 0) {
      startPara = 0;
    }
    int i;
    for (i = startPara + 1; i < allParas.get(docNum).size() && allParas.get(docNum).get(i).length() < 1; i++) ;
    if (i < allParas.get(docNum).size() && paraStr.equals(allParas.get(docNum).get(i))) {
      return i;
    }
    if (paraStr.equals(allParas.get(docNum).get(startPara))) {
      return startPara;
    }
    for (i = startPara - 1; i >= 0 && allParas.get(docNum).get(i).length() < 1; i--) ;
    if (i >= 0 && paraStr.equals(allParas.get(docNum).get(i))) {
      return i;
    }
    return -1;
  }
  
  /**
   * Search for Position of Paragraph
   * gives Back the Position in full text / -1 if Paragraph can not be found
   */
  private int getParaPos(String chPara, int numThread, int docNum) {

    if (numParasToCheck == 0) {
      return returnOneParaCheck();  //  check only the processed paragraph
    }

    try {
      LOFlatParagraph loFlaPa = null;
      if (loCursor.get(docNum) == null) {
        loCursor.set(docNum, new LOCursor(xContext));
      }
      int nParas;
      boolean isReset = false;

      if (allParas.get(docNum) == null || allParas.get(docNum).size() < 1) {
        if (numThread > 0) {              //  if numThread > 0: Thread may only read allParas
          return returnOneParaCheck();
        }
        if (!resetAllParas(loCursor.get(docNum), docNum)) {
          return returnOneParaCheck();
        }
        if (debugMode) {
          printToLogFile("+++ resetAllParas (allParas == null): allParas.size: " + allParas.get(docNum).size()
                  + "; lastDocNum: " + lastDocNum + ", docNum: " + docNum + logLineBreak);
        }
        isReset = true;
      }

      // Test if Size of allParas is correct; Reset if not
      nParas = loCursor.get(docNum).getNumberOfAllTextParagraphs();
      if (nParas < 2) {
        return returnOneParaCheck();
      } else if (allParas.get(docNum).size() != nParas) {
        if (numThread > 0) {
          return returnOneParaCheck();
        }
        if (debugMode) {
          printToLogFile("*** resetAllParas: allParas.size: " + allParas.get(docNum).size() + ", nParas: " + nParas
                  + "; lastDocNum: " + lastDocNum + ", docNum: " + docNum + logLineBreak);
        }
        if (!resetAllParas(loCursor.get(docNum), docNum)) {
          return returnOneParaCheck();
        }
        isReset = true;
      }

      // try to get next position from last ViewCursorPosition (proof per dialog box)
      int numLastPara;
      if (numThread > 0) {
        numLastPara = numLastVCPara1;
      } else {
        numLastPara = numLastVCPara.get(docNum);
      }
      nParas = findNextParaPos(numLastPara, chPara, docNum);
      if (nParas >= 0) {
        if (numThread > 0) {
          numLastVCPara1 = nParas;
        } else {
          numLastVCPara.set(docNum, nParas);
        }
        return returnContinueCheck(isReset, nParas, numThread);
      }

      // try to get next position from last Position of FlatParagraph (automatic Iteration without Text change)
      if (numThread > 0) {
        numLastPara = numLastFlPara1;
      } else {
        numLastPara = numLastFlPara.get(docNum);
      }
      nParas = findNextParaPos(numLastPara, chPara, docNum);
      if (nParas >= 0) {
        if (numThread > 0) {
          numLastFlPara1 = nParas;
        } else {
          numLastFlPara.set(docNum, nParas);
        }
        return returnContinueCheck(isReset, nParas, numThread);
      }

      // try to get ViewCursor position (proof initiated by mouse click)
      nParas = loCursor.get(docNum).getViewCursorParagraph();
      if (nParas >= 0 && nParas < allParas.get(docNum).size() && chPara.equals(allParas.get(docNum).get(nParas))) {
        if (numThread > 0) {
          numLastVCPara1 = nParas;
        } else {
          numLastVCPara.set(docNum, nParas);
        }
        return returnContinueCheck(isReset, nParas, numThread);
      }

      //  try to get paragraph position from automatic iteration
      if (loFlaPa == null) {
        loFlaPa = new LOFlatParagraph(xContext);
      }
      nParas = loFlaPa.getNumberOfAllFlatPara();

      if (debugMode) {
        printToLogFile("numLastFlPara[" + numThread + "]: " + numLastFlPara.get(docNum) + "; nParas: " + nParas
                + "; lastDocNum: " + lastDocNum + ", docNum: " + docNum + logLineBreak);
      }

      if (nParas < allParas.get(docNum).size()) {
        return returnOneParaCheck();   //  no automatic iteration
      }
      divNum = nParas - allParas.get(docNum).size();

      nParas = loFlaPa.getCurNumFlatParagraphs();

      if (nParas < divNum) {
        return returnOneParaCheck(); //  Proof footnote etc.
      }

      nParas -= divNum;
      if (numThread > 0) {
        numLastFlPara1 = nParas;
      } else {
        numLastFlPara.set(docNum, nParas);
      }
      if (!chPara.equals(allParas.get(docNum).get(nParas))) {
        if (isReset || numThread > 0) {
          return returnOneParaCheck();
        } else {
          if (debugMode) {
            printToLogFile("!!! allParas set: NParas: " + nParas + "; divNum: " + divNum
                    + "lastDocNum: " + lastDocNum + ", docNum: " + docNum
                    + logLineBreak + "old: " + allParas.get(docNum).get(nParas) + logLineBreak + "new: " + chPara + logLineBreak);
          }
          allParas.get(docNum).set(nParas, chPara);
          return returnNParaCheck(nParas, numThread);
        }
      }
      if (debugMode) {
        printToLogFile("nParas from FlatParagraph: " + nParas);
      }
      return returnContinueCheck(isReset, nParas, numThread);
    } catch (Throwable t) {
      showError(t);
      return returnOneParaCheck();
    }
  }
  
  /*
   * Get or Create a Number from docID
   */
  private int getNumDocID(String docID) {
    if (docIDs == null) {
      docIDs = new ArrayList<>();
      allParas = new ArrayList<>();
      fullTextMatches = new ArrayList<>();
      loCursor = new ArrayList<>();
      numLastVCPara = new ArrayList<>();
      numLastFlPara = new ArrayList<>();
    }
    for (int i = 0; i < docIDs.size(); i++) {
      if (docIDs.get(i).equals(docID)) {
        return i;                           //  document exist
      }
    }
    if (docIDs.size() >= MAX_DOC) {
      docIDs.remove(0);
      allParas.remove(0);
      fullTextMatches.remove(0);
      loCursor.remove(0);
      numLastVCPara.remove(0);
      numLastFlPara.remove(0);
    }
    docIDs.add(docID);
    allParas.add(null);
    fullTextMatches.add(null);
    loCursor.add(null);
    numLastVCPara.add(0);
    numLastFlPara.add(0);
    return docIDs.size() - 1;
  }

  /**
   * Initialize log-file
   */
  private void initLogFile() {
    String path = getHomeDir() + "/" + LOG_FILE;
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
      Date date = new Date();
      bw.write("LT office integration log from " + date.toString() + logLineBreak);
    } catch (Throwable t) {
      showError(t);
    }
  }

  /**
   * write to log-file
   */
  static void printToLogFile(String str) {
    String path = getHomeDir() + "/" + LOG_FILE;
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
      bw.write(str + logLineBreak);
    } catch (Throwable t) {
      showError(t);
    }
  }

}
