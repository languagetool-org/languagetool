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

package org.languagetool.openoffice.stylestatistic;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.openoffice.ResultCache;
import org.languagetool.gui.Configuration;
import org.languagetool.openoffice.DocumentCache;
import org.languagetool.openoffice.DocumentCursorTools;
import org.languagetool.openoffice.ErrorPositionComparator;
import org.languagetool.openoffice.FlatParagraphTools;
import org.languagetool.openoffice.DocumentCache.TextParagraph;
import org.languagetool.openoffice.MultiDocumentsHandler.WaitDialogThread;
import org.languagetool.openoffice.OfficeTools.LoErrorType;
import org.languagetool.openoffice.ResultCache.CacheEntry;
import org.languagetool.openoffice.SingleCheck;
import org.languagetool.openoffice.SingleCheck.SentenceErrors;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.text.TextMarkupType;

import org.languagetool.openoffice.MessageHandler;
import org.languagetool.openoffice.OfficeTools;
import org.languagetool.openoffice.SingleDocument;
import org.languagetool.openoffice.SwJLanguageTool;

/**
 * Statistical Analyzes Document Cache 
 * @since 6.2
 * @author Fred Kruse
 */
public class StatAnCache {
  
  private final static int MAX_NAME_LENGTH = 80;
  
  private List<List<AnalyzedSentence>> analyzedParagraphs = new ArrayList<>();
  private List<Heading> headings = new ArrayList<>();
  private List<Paragraph> paragraphs = new ArrayList<>();
  private SingleDocument document;
  private DocumentCache docCache;
  private ResultCache statAnCache = null;
  private String actRuleId = null;
  private int lastPara = -1;
  private SwJLanguageTool lt;
  
  public StatAnCache(SingleDocument document, WaitDialogThread waitdialog) {
    this.document = document;
    lt = document.getMultiDocumentsHandler().getLanguageTool();
    docCache = document.getDocumentCache();
    
    while (docCache.getHeadingMap() == null) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        MessageHandler.showError(e);
      }
    }
    if (waitdialog != null) {
      waitdialog.initializeProgressBar(0, 100);
    }
    int textSize = docCache.textSize(DocumentCache.CURSOR_TYPE_TEXT);
    for (int i = 0; i < textSize; i++) {
      String tPara = docCache.getTextParagraph(new TextParagraph(DocumentCache.CURSOR_TYPE_TEXT, i));
      List<AnalyzedSentence> sentences = null;
      try {
        sentences = lt.analyzeText(tPara);
      } catch (IOException e) {
        MessageHandler.showError(e);
      }
      if (sentences == null) {
        sentences = new ArrayList<>();
      }
      analyzedParagraphs.add(sentences);
      if (waitdialog != null) {
        waitdialog.setValueForProgressBar(90 * i / textSize);;
      }
    }
    setHeadings();
    setParagraphs();
  }
  
  private void setHeadings() {
    Map<Integer, Integer> headingMap = docCache.getHeadingMap();
    List<Integer> headParas = new ArrayList<>();
    for (int nPara : headingMap.keySet()) {
      headParas.add(nPara);
    }
    headParas.sort(null);
    for (int nPara : headParas) {
      headings.add(new Heading(getNameOfParagraph(nPara), headingMap.get(nPara), nPara));
    }
  }
  
  private void setParagraphs() {
    for (int i = 0; i < docCache.textSize(DocumentCache.CURSOR_TYPE_TEXT); i++) {
      paragraphs.add(new Paragraph(getNameOfParagraph(i), getHeadingHierarchy(i), i));
    }
  }
  
  public int size() {
    return analyzedParagraphs.size();
  }

  public List<AnalyzedSentence> getAnalysedParagraph(int n) {
    return analyzedParagraphs.get(n);
  }

  public List<List<AnalyzedSentence>> getAnalysedParagraphsfrom(int from, int to) {
    List<List<AnalyzedSentence>> tmpParagraphs = new ArrayList<>();
    for (int i = from; i < to; i++) {
      tmpParagraphs.add(analyzedParagraphs.get(i));
    }
    return tmpParagraphs;
  }

  public List<Paragraph> getParagraphsfrom(int from, int to) {
    List<Paragraph> tmpParagraphs = new ArrayList<>();
    for (int i = from; i < to; i++) {
      tmpParagraphs.add(paragraphs.get(i));
    }
    return tmpParagraphs;
  }

  /**
   * get number of flatparagraph from Number of text paragraph
   */
  public int getNumFlatParagraph(int textPara) {
    return docCache.getFlatParagraphNumber(new TextParagraph(DocumentCache.CURSOR_TYPE_TEXT, textPara));
  }
  
  /**
   * get name of paragraph (maximal MAX_NAME_LENGTH characters)
   */
  public String getNameOfParagraph(int nPara) {
    String tPara = docCache.getTextParagraph(new TextParagraph(DocumentCache.CURSOR_TYPE_TEXT, nPara));
    return getNameOfParagraph(tPara);
  }
  
  /**
   * get name of paragraph (maximal MAX_NAME_LENGTH characters)
   */
  public String getNameOfParagraph(String text) {
    if (text.length() > MAX_NAME_LENGTH) {
      text = text.substring(0, MAX_NAME_LENGTH - 3) + "...";
    }
    return text;
  }
  
  private int getHeadingHierarchy(int nPara) {
    for (int i = 0; i < headings.size(); i++) {
      if(headings.get(i).paraNum == nPara) {
        return (headings.get(i).hierarchy);
      }
    }
    return -1;
  }
  
  public List<Heading> getAllHeadings() {
    return headings;
  }
  
  /**
   * Set the marks for rule matches in in document text
   *//*
  public void setMarkUps(String ruleId, ResultCache rCache, List<Integer> chngedParas) {
    if (ruleId == null || rCache == null || chngedParas == null || chngedParas.isEmpty()) {
      document.setStatAnRuleId(null);
      document.setStatAnCache(null);
      if (changedParas != null && !changedParas.isEmpty()) {
        document.remarkChangedParagraphs(changedParas, changedParas, true);
        changedParas = null;
      }
      return;
    }
    document.setStatAnRuleId(ruleId);
    document.setStatAnCache(rCache);
    changedParas = chngedParas;
    document.remarkChangedParagraphs(changedParas, changedParas, true);
  }
*/  
  /**
   * Add statistical analysis errors
   */
  private SingleProofreadingError[] addStatAnalysisErrors (SingleProofreadingError[] errors, 
          SingleProofreadingError[] statAnErrors, String statAnRuleId) {
    
    List<SingleProofreadingError> errorList = new  ArrayList<>();
    for (SingleProofreadingError error : errors) {
      if (!error.aRuleIdentifier.equals(statAnRuleId)) {
        errorList.add(error);
      }
    }
    for (SingleProofreadingError error : statAnErrors) {
      errorList.add(error);
    }
    return errorList.toArray(new SingleProofreadingError[errorList.size()]);
  }
  
  /**
   * Merge errors from different checks (paragraphs and sentences)
   */
  private SingleProofreadingError[] mergeErrors(List<SingleProofreadingError[]> pErrors, 
      SingleProofreadingError[] statAnErrors, String statAnRuleId, int nPara) {
    SingleProofreadingError[] errorArray = document.mergeErrors(pErrors, nPara);
    MessageHandler.printToLogFile("SingleDocument: mergeErrors: nPara: " + nPara + ", statAnRuleId: " 
        + (statAnRuleId == null ? "null" : statAnRuleId));
    MessageHandler.printToLogFile("SingleDocument: mergeErrors: statAnErrors: " 
        + (statAnErrors == null ? "null" : statAnErrors.length));
    if (statAnRuleId != null && statAnErrors != null && statAnErrors.length > 0) {
      errorArray = addStatAnalysisErrors (errorArray, statAnErrors, statAnRuleId);
    }
    MessageHandler.printToLogFile("SingleDocument: mergeErrors: number Errors: " + errorArray.length);
    Arrays.sort(errorArray, new ErrorPositionComparator());
    return errorArray;
  }

  /**
   * get all errors of a Paragraph as list
   */
  private List<SentenceErrors> getSentencesErrosAsList(int numberOfParagraph, String sRuleId, ResultCache sCache) {
    List<SentenceErrors> sentenceErrors = new ArrayList<SentenceErrors>();
    List<ResultCache> paragraphsCache = document.getParagraphsCache();
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
      nextSentencePositions = SingleCheck.getNextSentencePositions(docCache.getFlatParagraph(numberOfParagraph), lt);
    }
    SingleProofreadingError[] sErrors = null;
    int startPosition = 0;
    if (nextSentencePositions.size() == 1) {
      List<SingleProofreadingError[]> errorList = new ArrayList<SingleProofreadingError[]>();
      for (ResultCache cache : paragraphsCache) {
        errorList.add(cache.getMatches(numberOfParagraph, LoErrorType.GRAMMAR));
      }
      if (sRuleId != null && sCache != null) {
        sErrors = sCache.getMatches(numberOfParagraph, LoErrorType.GRAMMAR);
      }
      sentenceErrors.add(new SentenceErrors(startPosition, nextSentencePositions.get(0), 
          mergeErrors(errorList, sErrors, sRuleId, numberOfParagraph)));
    } else {
      for (int nextPosition : nextSentencePositions) {
        List<SingleProofreadingError[]> errorList = new ArrayList<SingleProofreadingError[]>();
        for (ResultCache cache : paragraphsCache) {
          errorList.add(cache.getFromPara(numberOfParagraph, startPosition, nextPosition, LoErrorType.GRAMMAR));
        }
        if (sRuleId != null && sCache != null) {
          sErrors = sCache.getFromPara(numberOfParagraph, startPosition, nextPosition, LoErrorType.GRAMMAR);
        }
        sentenceErrors.add(new SentenceErrors(startPosition, nextPosition, 
            mergeErrors(errorList, sErrors, sRuleId, numberOfParagraph)));
        startPosition = nextPosition;
      }
    }
    return sentenceErrors;
  }

  /**
   * Set a new result cache
   * reset a paragraph if necessary
   */
  public void setNewResultcache(String ruleId, ResultCache sCache) {
    if (actRuleId != null && lastPara >= 0) {
      remarkChangedParagraph(lastPara, null, null);
      lastPara = -1;
    }
    actRuleId = ruleId;
    statAnCache = sCache;
  }

  /**
   * remark a paragraph
   * reset a paragraph if necessary
   */
  public void markParagraph (int nPara) {
    if (actRuleId != null) {
      if (lastPara >= 0) {
        remarkChangedParagraph(lastPara, null, null);
      }
      lastPara = nPara;
      remarkChangedParagraph(lastPara, actRuleId, statAnCache);
    }
  }

  /**
   * remark changed paragraph
   * override existing marks
   */
  private void remarkChangedParagraph(int nFPara, String sRuleId, ResultCache sCache) {
    Map <Integer, List<SentenceErrors>> changedParasMap = new HashMap<>();
    List <TextParagraph> toRemarkTextParas = new ArrayList<>();
    List<SentenceErrors> sentencesErrors = getSentencesErrosAsList(nFPara, sRuleId, sCache);
    changedParasMap.put(nFPara, sentencesErrors);
    toRemarkTextParas.add(docCache.getNumberOfTextParagraph(nFPara));
    DocumentCursorTools docCursor = document.getDocumentCursorTools();
    if (docCursor != null) {
      docCursor.removeMarks(toRemarkTextParas);
    }
    FlatParagraphTools flatPara = document.getFlatParagraphTools();
    if (flatPara != null) {
      flatPara.markParagraphs(changedParasMap);
    }
  }
  
  /**
   * create an array of SingleProofreadingErrors from an array of rule matches
   */
  public SingleProofreadingError[] createLoErrors(RuleMatch[] ruleMatches) {
    if (ruleMatches == null || ruleMatches.length == 0) {
      return new SingleProofreadingError[0];
    }
    SingleProofreadingError[] errors = new SingleProofreadingError[ruleMatches.length];
    for (int i = 0; i < ruleMatches.length; i++) {
      errors[i] = createLoError(ruleMatches[i]);
    }
    return errors;
  }
  
  /**
   * create a SingleProofreadingError from a rule match
   */
  private SingleProofreadingError createLoError(RuleMatch ruleMatch) {
    SingleProofreadingError aError = new SingleProofreadingError();
    aError.nErrorType = TextMarkupType.PROOFREADING;
    // the API currently has no support for formatting text in comments
    String msg = ruleMatch.getMessage();
    Language docLanguage = lt.getLanguage();
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
    //  Filter: provide user to delete footnotes by suggestion
    int numSuggestions;
    String[] allSuggestions;
    numSuggestions = ruleMatch.getSuggestedReplacements().size();
    allSuggestions = ruleMatch.getSuggestedReplacements().toArray(new String[numSuggestions]);
    if (numSuggestions > OfficeTools.MAX_SUGGESTIONS) {
      aError.aSuggestions = Arrays.copyOfRange(allSuggestions, 0, OfficeTools.MAX_SUGGESTIONS);
    } else {
      aError.aSuggestions = allSuggestions;
    }
    aError.nErrorStart = ruleMatch.getFromPos();
    aError.nErrorLength = ruleMatch.getToPos() - ruleMatch.getFromPos();
    aError.aRuleIdentifier = ruleMatch.getRule().getId();
    Color underlineColor = new Color(0, 180, 180);
    int ucolor = underlineColor.getRGB() & 0xFFFFFF;
    PropertyValue[] propertyValues = new PropertyValue[2];
    propertyValues[0] = new PropertyValue("LineColor", -1, ucolor, PropertyState.DIRECT_VALUE);
    propertyValues[1] = new PropertyValue("LineType", -1, Configuration.UNDERLINE_BOLDWAVE, PropertyState.DIRECT_VALUE);
    aError.aProperties = propertyValues;
    return aError;
  }

  
  /**
   * class paragraph (stores all information needed)
   */
  public class Paragraph {
    public String name;
    public int hierarchy;
    public int paraNum;
    
    Paragraph (String name, int hierarchy, int paraNum) {
      this.name = new String(name);
      this.hierarchy = hierarchy;
      this.paraNum = paraNum;
    }
  }

  public class Heading {
    String name;
    int hierarchy;
    int paraNum;
    
    Heading (String name, int hierarchy, int paraNum) {
      this.name = new String(name);
      this.hierarchy = hierarchy;
      this.paraNum = paraNum;
    }
    
  }

}
