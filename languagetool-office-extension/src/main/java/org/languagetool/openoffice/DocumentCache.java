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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.openoffice.OfficeTools.DocumentType;

import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;

/**
 * Class enhances DocumentTextCache by AnalyzedSentences
 * @since 6.4
 * @author Fred Kruse
 */
public class DocumentCache extends DocumentTextCache{

  private static final long serialVersionUID = 315782693333390101L;

  private final Map<Integer, List<AnalyzedSentence>> analyzedParagraphs = new HashMap<>();  //  stores analyzed paragraphs
  
  private ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
  

  public DocumentCache(DocumentType docType) {
    super(docType);
  }

  public DocumentCache(SingleDocument document, Locale fixedLocale, Locale docLocale, XComponent xComponent,
      DocumentType docType) {
    super(document, fixedLocale, docLocale, xComponent, docType);
  }

  public DocumentCache(DocumentTextCache in) {
    super(in);
  }
  
  /**
   * Refresh the cache
   */
  public void refresh(SingleDocument document, Locale fixedLocale, Locale docLocale, XComponent xComponent, int fromWhere) {
    clearAnalyzedParagraphs();
    super.refresh(document, fixedLocale, docLocale, xComponent, fromWhere);
  }

  /**
   * set Locale of Flat Paragraph by Index
   */
  public void setFlatParagraphLocale(int n, Locale locale) {
    removeAnalyzedParagraph(n);
    super.setFlatParagraphLocale(n, locale);
  }

  /**
   * set Flat Paragraph at Index
   */
  public void setFlatParagraph(int n, String sPara) {
    removeAnalyzedParagraph(n);
    super.setFlatParagraph(n, sPara);
  }

  /**
   * set Flat Paragraph and Locale at Index
   */
  @Override
  public void setFlatParagraph(int n, String sPara, Locale locale) {
    removeAnalyzedParagraph(n);
    super.setFlatParagraph(n, sPara, locale);
  }

  /**
   * set footnotes of Flat Paragraph by Index
   */
  public void setFlatParagraphFootnotes(int n, int[] footnotePos) {
    removeAnalyzedParagraph(n);
    super.setFlatParagraphFootnotes(n, footnotePos);
  }
  
  /**
   * set deleted characters (report changes) of Flat Paragraph by Index
   */
  public void setFlatParagraphDeletedCharacters(int n, List<Integer> deletedChars) {
    removeAnalyzedParagraph(n);
    super.setFlatParagraphDeletedCharacters(n, deletedChars);
  }

  /**
   * Remove all analyzed paragraphs
   */
  public void clearAnalyzedParagraphs() {
    analyzedParagraphs.clear();
  }
  
  /**
   * Get all analyzed paragraphs
   */
  public Map<Integer, List<AnalyzedSentence>> getAllAnalyzedParagraphs() {
    return analyzedParagraphs;
  }
  
  /**
   * Get an analyzed paragraphs
   */
  public List<AnalyzedSentence> getAnalyzedParagraph(int nFPara) {
    rwLock.readLock().lock();
    try {
      return analyzedParagraphs.get(nFPara);
    } finally {
      rwLock.readLock().unlock();
    }
  }
  
  /**
   * Remove an analyzed paragraphs
   */
  public void removeAnalyzedParagraph(int nFPara) {
    rwLock.writeLock().lock();
    try {
      analyzedParagraphs.remove(nFPara);
    } finally {
      rwLock.writeLock().unlock();
    }
  }
  
  /**
   * Put an analyzed paragraphs
   */
  public void putAnalyzedParagraph(int nFPara, List<AnalyzedSentence> analyzedParagraph) {
    rwLock.writeLock().lock();
    try {
      analyzedParagraphs.put(nFPara, analyzedParagraph);
    } finally {
      rwLock.writeLock().unlock();
    }
  }
  
  /**
   * Remove and shift analyzed paragraphs by a range
   */
  public void removeAndShiftAnalyzedParagraph(int fromParagraph, int toParagraph, int oldSize, int newSize) {
    if (analyzedParagraphs == null || analyzedParagraphs.isEmpty()) {
      return;
    }
    int shift = newSize - oldSize;
    if (fromParagraph < 0 && toParagraph >= newSize) {
      return;
    }
    rwLock.writeLock().lock();
    try {
      Map<Integer, List<AnalyzedSentence>> tmpParagraphs = new HashMap<>(analyzedParagraphs);
      analyzedParagraphs.clear();
      if (shift < 0) {   // new size < old size
        for (int i : tmpParagraphs.keySet()) {
          if (i < fromParagraph) {
            analyzedParagraphs.put(i, tmpParagraphs.get(i));
          } else if (i >= toParagraph - shift) {
            analyzedParagraphs.put(i + shift, tmpParagraphs.get(i));
          }
        }
      } else {
        for (int i : tmpParagraphs.keySet()) {
          if (i < fromParagraph) {
            analyzedParagraphs.put(i, tmpParagraphs.get(i));
          } else if (i >= toParagraph) {
            analyzedParagraphs.put(i + shift, tmpParagraphs.get(i));
          }
        }
      }
    } finally {
      rwLock.writeLock().unlock();
    }
  }

  /**
   * create an analyzed paragraph and store it in analyzed Cache
   */
  public List<AnalyzedSentence> createAnalyzedParagraph(int nFPara, SwJLanguageTool lt) throws IOException {
    String paraText = getFlatParagraph(nFPara);
    if (paraText == null) {
      return null;
    }
    paraText = SingleCheck.removeFootnotes(paraText, 
        getFlatParagraphFootnotes(nFPara), getFlatParagraphDeletedCharacters(nFPara));
//          + OfficeTools.END_OF_PARAGRAPH;
    AnnotatedText text = new AnnotatedTextBuilder().addText(paraText).build();
    List<AnalyzedSentence> analyzedParagraph = lt.analyzeText(text.getPlainText());
    putAnalyzedParagraph(nFPara, analyzedParagraph);
    return analyzedParagraph;
  }

  /**
   * Get an analyzed paragraph from analyzed Cache
   * if the requested paragraph doesn't exist create it
   */
  public List<AnalyzedSentence> getOrCreateAnalyzedParagraph(int nFPara, SwJLanguageTool lt) throws IOException {
    List<AnalyzedSentence> analyzedParagraph = getAnalyzedParagraph(nFPara);
    if (analyzedParagraph == null) {
      analyzedParagraph = createAnalyzedParagraph(nFPara, lt);
    }
    return analyzedParagraph;
  }

  /**
   * Get a range of analyzed paragraphs from analyzed Cache
   * if the requested paragraphs don't exist create it
   */
  public List<AnalyzedSentence> getAnalyzedParagraphs(TextParagraph from, TextParagraph to, SwJLanguageTool lt) throws IOException {
    List<AnalyzedSentence> analyzedParagraphs = new ArrayList<>();
    for (int i = from.number; i < to.number; i++) {
      int n = getFlatParagraphNumber(new TextParagraph(from.type, i));
      List<AnalyzedSentence> analyzedParagraph = getOrCreateAnalyzedParagraph(n, lt);
      if (analyzedParagraph == null) {
        return null;
      }
      if (analyzedParagraph.size() == 0) {
        int last = analyzedParagraphs.size() - 1;
        AnalyzedSentence sentence = analyzedParagraphs.get(last);
        AnalyzedTokenReadings[] tokens = sentence.getTokens();
        int len = tokens.length;
        AnalyzedTokenReadings[] newTokens = new AnalyzedTokenReadings[len + 2];
        for (int k = 0; k < len; k++) {
          newTokens[k] = tokens[k];
        }
        int startPos = tokens[len - 1].getEndPos();
        newTokens[len] = new AnalyzedTokenReadings(new AnalyzedToken("\n", null, null), startPos);
        newTokens[len + 1] = new AnalyzedTokenReadings(new AnalyzedToken("\n", null, null), startPos + 1);
        sentence = new AnalyzedSentence(newTokens);
        analyzedParagraphs.set(last, sentence);
      } else {
        for (int j = 0; j < analyzedParagraph.size(); j++) {
          AnalyzedSentence sentence = analyzedParagraph.get(j);
          if (j == analyzedParagraph.size() - 1 && i < to.number - 1) {
            AnalyzedTokenReadings[] tokens = sentence.getTokens();
            int len = tokens.length;
            AnalyzedTokenReadings[] newTokens = new AnalyzedTokenReadings[len + 2];
            for (int k = 0; k < len; k++) {
              newTokens[k] = tokens[k];
            }
            int startPos = tokens[len - 1].getEndPos();
            newTokens[len] = new AnalyzedTokenReadings(new AnalyzedToken("\n", null, null), startPos);
            newTokens[len + 1] = new AnalyzedTokenReadings(new AnalyzedToken("\n", null, null), startPos + 1);
            sentence = new AnalyzedSentence(newTokens);
          }
          analyzedParagraphs.add(sentence);
        }
      }
//      analyzedParagraphs.addAll(analyzedParagraph);
    }
    return analyzedParagraphs;
  }
  
  public static class TextParagraph implements Serializable {
    private static final long serialVersionUID = 1L;
    int type;
    int number;

    public TextParagraph(int type, int number) {
      this.type = type;
      this.number = number;
    }
  }
  
  public static void printTokenizedSentences(List<AnalyzedSentence> sentences) {
    for (AnalyzedSentence sentence : sentences) {
      String str = "";
      for (AnalyzedTokenReadings token : sentence.getTokens()) {
        str += "'" + token.getToken(); 
        if (token.isSentenceStart()) {
          str += "{sent start}";
        }
        if (token.isSentenceEnd()) {
          str += "{sent end}";
        }
        if (token.isParagraphEnd()) {
          str += "{para end}";
        }
        str += "' ";
      }
      MessageHandler.printToLogFile("Sentence: " + str);
    }
  }

  public static class ChangedRange {
    final public int from;
    final public int to;
    final public int oldSize;
    final public int newSize;
    
    ChangedRange(int from, int to, int oldSize, int newSize) {
      this.from = from;
      this.to = to;
      this.oldSize = oldSize;
      this.newSize = newSize;
    }
  }



}
