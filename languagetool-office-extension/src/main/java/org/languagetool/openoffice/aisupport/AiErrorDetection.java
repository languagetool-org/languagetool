/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.openoffice.aisupport;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.gui.Configuration;
import org.languagetool.openoffice.DocumentCache;
import org.languagetool.openoffice.MessageHandler;
import org.languagetool.openoffice.OfficeTools;
import org.languagetool.openoffice.ResultCache;
import org.languagetool.openoffice.SingleCheck;
import org.languagetool.openoffice.SingleDocument;
import org.languagetool.openoffice.SwJLanguageTool;
import org.languagetool.rules.RuleMatch;

import com.sun.star.linguistic2.SingleProofreadingError;

/**
 * Class to detect errors by a AI API
 * @since 6.5
 * @author Fred Kruse
 */
public class AiErrorDetection {
  private static final ResourceBundle messages = JLanguageTool.getMessageBundle();
  private final static String CORRECT_COMMAND = "Write the corrected text";
  private final SingleDocument document;
  private final DocumentCache docCache;
  private final Configuration config;
  
  public AiErrorDetection(SingleDocument document, Configuration config) {
    this.document = document;
    this.config = config;
    docCache = document.getDocumentCache();
  }
  
  public void addAiRuleMatchesForParagraph(int nFPara) {
    if (docCache == null) {
      return;
    }
    try {
      List<AnalyzedSentence> analyzedSentences = docCache.getAnalyzedParagraph(nFPara);
      String paraText = docCache.getFlatParagraph(nFPara);
      if (paraText == null || analyzedSentences == null || paraText.trim().isEmpty()) {
        return;
      }
      int[] footnotePos = docCache.getFlatParagraphFootnotes(nFPara);
      List<Integer> deletedChars = docCache.getFlatParagraphDeletedCharacters(nFPara);
      paraText = DocumentCache.fixLinebreak(SingleCheck.removeFootnotes(paraText, 
          footnotePos, deletedChars));
        addMatchesByAiRule(nFPara, paraText, analyzedSentences, footnotePos, deletedChars);
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }
    
  public void addAiRuleMatchesForParagraph(String paraText, int[] footnotePos, List<Integer> deletedChars) {
    if (paraText == null || paraText.trim().isEmpty()) {
      return;
    }
    try {
      SwJLanguageTool lt = document.getMultiDocumentsHandler().getLanguageTool();
      paraText = DocumentCache.fixLinebreak(SingleCheck.removeFootnotes(paraText, 
          footnotePos, deletedChars));
      List<AnalyzedSentence> analyzedSentences =  lt.analyzeText(paraText.replace("\u00AD", ""));
      if (analyzedSentences == null) {
        return;
      }
      addMatchesByAiRule(-1, paraText, analyzedSentences, footnotePos, deletedChars);
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }
    
  private void addMatchesByAiRule(int nFPara, String paraText, List<AnalyzedSentence> analyzedSentences,
      int[] footnotePos, List<Integer> deletedChars) throws Throwable {
    ResultCache aiCache = document.getParagraphsCache().get(OfficeTools.CACHE_AI);
    String result = getAiResult(paraText);
    if (result == null || result.trim().isEmpty()) {
      aiCache.put(nFPara, null, new SingleProofreadingError[0]);
      return;
    }
    SwJLanguageTool lt = document.getMultiDocumentsHandler().getLanguageTool();
    List<AnalyzedSentence> analyzedAiResult =  lt.analyzeText(result.replace("\u00AD", ""));
    AiDetectionRule aiRule = new AiDetectionRule(result, analyzedAiResult, messages);
    RuleMatch[] ruleMatches = aiRule.match(analyzedSentences);
    if (ruleMatches == null || ruleMatches.length == 0) {
      aiCache.put(nFPara, null, new SingleProofreadingError[0]);
      return;
    }
    List<SingleProofreadingError> errorList = new ArrayList<>();
    for (RuleMatch myRuleMatch : ruleMatches) {
      errorList.add(SingleCheck.correctRuleMatchWithFootnotes(
          SingleCheck.createOOoError(myRuleMatch, 0, footnotePos, null, config), footnotePos, deletedChars));
    }
    aiCache.put(nFPara, null, errorList.toArray(new SingleProofreadingError[0]));
  }
    
  private String getAiResult(String para) throws Throwable {
    if (para == null || para.isEmpty()) {
      return "";
    }
    String text = CORRECT_COMMAND + ": " + para;
//    MessageHandler.showMessage("Input is: " + text);
    AiRemote aiRemote = new AiRemote(config);
    String output = aiRemote.answerQuestion(text);
    return output;
  }

}
