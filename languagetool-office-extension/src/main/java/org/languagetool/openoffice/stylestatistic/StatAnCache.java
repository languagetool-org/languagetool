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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.languagetool.AnalyzedSentence;
import org.languagetool.openoffice.DocumentCache;
import org.languagetool.openoffice.DocumentCache.TextParagraph;
import org.languagetool.openoffice.MessageHandler;
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
  private DocumentCache cache;
  
  public StatAnCache(DocumentCache cache, SwJLanguageTool lt) {
    this.cache = cache;
    for (int i = 0; i < cache.textSize(DocumentCache.CURSOR_TYPE_TEXT); i++) {
      String tPara = cache.getTextParagraph(new TextParagraph(DocumentCache.CURSOR_TYPE_TEXT, i));
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
    }
    setHeadings();
    setParagraphs();
  }
  
  private void setHeadings() {
    Map<Integer, Integer> headingMap = cache.getHeadingMap();
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
    for (int i = 0; i < cache.textSize(DocumentCache.CURSOR_TYPE_TEXT); i++) {
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
   * get name of paragraph (maximal MAX_NAME_LENGTH characters)
   */
  public String getNameOfParagraph(int nPara) {
    String tPara = cache.getTextParagraph(new TextParagraph(DocumentCache.CURSOR_TYPE_TEXT, nPara));
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
   * class paragraph (stores all information needed)
   */
  public class Paragraph {
//    public List<List<Word>> sentences;
    public String name;
    public int hierarchy;
    public int paraNum;
    
    Paragraph (String name, int hierarchy, int paraNum) {
 //   Paragraph (List<List<Word>> sentences, String name, int hierarchy, int paraNum) {
//      this.sentences = sentences;
      this.name = new String(name);
      this.hierarchy = hierarchy;
      this.paraNum = paraNum;
    }
 /*   
    void printParagraphToLogFile() {
      String txt = "";
      for (List<Word> sentence : sentences) {
        for (Word word : sentence) {
          txt += word.name + " "; 
        }
      }
      MessageHandler.printToLogFile(txt);
    }
*/
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
