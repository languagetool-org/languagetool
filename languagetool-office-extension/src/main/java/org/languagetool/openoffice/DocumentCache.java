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

import java.util.ArrayList;
import java.util.List;

import org.languagetool.openoffice.FlatParagraphTools.ParagraphContainer;

import com.sun.star.lang.Locale;

/**
 * Class to store the Text of a LO document 
 * @since 5.0
 * @author Fred Kruse
 */
public class DocumentCache {
  
  private static boolean debugMode;         //  should be false except for testing

  private List<String> paragraphs = null;            //  stores the flat paragraphs of document
  private List<Integer> headings = null;             //  stores the paragraphs formated as headings; is used to subdivide the document in chapters
  private List<Locale> locales = null;               //  stores the language of the paragraphs;
  private List<int[]> footnotes = null;              //  stores the footnotes of the paragraphs;
  private List<Integer> toTextMapping = new ArrayList<>();  //Mapping from FlatParagraph to DocumentCursor
  private List<Integer> toParaMapping = new ArrayList<>();  //Mapping from DocumentCursor to FlatParagraph
  private int defaultParaCheck;

  DocumentCache(DocumentCursorTools docCursor, FlatParagraphTools flatPara, int defaultParaCheck) {
    debugMode = OfficeTools.DEBUG_MODE_DC;
    this.defaultParaCheck = defaultParaCheck;
    reset(docCursor, flatPara);
  }
  
  public void reset(DocumentCursorTools docCursor, FlatParagraphTools flatPara) {
    List<String> textParas = docCursor.getAllTextParagraphs();
    ParagraphContainer paragraphContainer = null;
    if(textParas != null) {
      headings = docCursor.getParagraphHeadings();
      paragraphContainer = flatPara.getAllFlatParagraphs();
      if(paragraphContainer == null) {
        MessageHandler.printToLogFile("paragraphContainer == null - ParagraphCache not initialised");
        return;
      }
      paragraphs = paragraphContainer.paragraphs;
    }
    if(paragraphs == null) {
      MessageHandler.printToLogFile("paragraphs == null - ParagraphCache not initialised");
      return;
    }
    locales = paragraphContainer.locales;
    footnotes = paragraphContainer.footnotePositions;
    
    if(debugMode) {
      MessageHandler.printToLogFile("\n\nNot mapped paragraphs:");
    }
    if(textParas != null && !textParas.isEmpty()) {
      int n = 0; 
      for(int i = 0; i < paragraphs.size(); i++) {
        if((footnotes != null && i < footnotes.size() && footnotes.get(i).length > 0)
            || (n < textParas.size() && (paragraphs.get(i).equals(textParas.get(n)) 
                || removeZeroWidthSpace(paragraphs.get(i)).equals(textParas.get(n))))) {
          toTextMapping.add(n);
          toParaMapping.add(i);
          n++;
        } else {
          toTextMapping.add(-1);
          if(debugMode) {
            MessageHandler.printToLogFile("\nFlat("  + i + "): '" + paragraphs.get(i));
            if(n < textParas.size()) {
              MessageHandler.printToLogFile("Doc("  + n + "): '" + textParas.get(n));
            }
          }  
        }
      }
      if(debugMode) {
        MessageHandler.printToLogFile("\n\ntoParaMapping:");
        for(int i = 0; i < toParaMapping.size(); i++) {
          MessageHandler.printToLogFile("Doc: " + i + " Flat: " + toParaMapping.get(i)
          + OfficeTools.LOG_LINE_BREAK + getTextParagraph(i));
        }
        MessageHandler.printToLogFile("\n\ntoTextMapping:");
        for(int i = 0; i < toTextMapping.size(); i++) {
          MessageHandler.printToLogFile("Flat: " + i + " Doc: " + toTextMapping.get(i));
//        if(toTextMapping.get(i) == -1) {
//          MessageHandler.printToLogFile("'" + paragraphs.get(i) + "'");
//        }
        }
        MessageHandler.printToLogFile("\n\nheadings:");
        for(int i = 0; i < headings.size(); i++) {
          MessageHandler.printToLogFile("Num: " + i + " Heading: " + headings.get(i));
        }
        MessageHandler.printToLogFile("\nNumber of Flat Paragraphs: " + paragraphs.size());
        MessageHandler.printToLogFile("Number of Text Paragraphs: " + toParaMapping.size());
        MessageHandler.printToLogFile("Number of footnotes: " + footnotes.size());
        MessageHandler.printToLogFile("Number of locales: " + locales.size());
      }
    }
  }
  
  /**
   * get Flat Paragraph by Index
   */
  public String getFlatParagraph(int n) {
    return paragraphs.get(n);
  }
  
  /**
   * set Flat Paragraph at Index
   */
  public void setFlatParagraph(int n, String sPara) {
    paragraphs.set(n, sPara);
  }
    
  /**
   * get Text Paragraph by Index
   */
  public String getTextParagraph(int n) {
    return paragraphs.get(toParaMapping.get(n));
  }
  
  /**
   * get Number of Flat Paragraph from Number of Text Paragraph
   */
  public int getFlatParagraphNumber(int n) {
    return toParaMapping.get(n);
  }
  
  /**
   * get Locale of Flat Paragraph by Index
   */
  public Locale getFlatParagraphLocale(int n) {
    return locales.get(n);
  }
  
  /**
   * get Locale of Text Paragraph by Index
   */
  public Locale getTextParagraphLocale(int n) {
    return locales.get(toParaMapping.get(n));
  }
  
  /**
   * get footnotes of Flat Paragraph by Index
   */
  public int[] getFlatParagraphFootnotes(int n) {
    return footnotes.get(n);
  }
  
  /**
   * get footnotes of Text Paragraph by Index
   */
  public int[] getTextParagraphFootnotes(int n) {
    return footnotes.get(toParaMapping.get(n));
  }
  
  /**
   * is DocumentCache empty
   */
  public boolean isEmpty() {
    return paragraphs == null || paragraphs.isEmpty();
  }
  
  /**
   * size of document cache (number of all flat paragraphs)
   */
  public int size() {
    return paragraphs == null ? 0 : paragraphs.size();
  }
  
  /**
   * size of document cache (number of all flat paragraphs)
   */
  public int getNumberOfTextParagraph(int numberOfFlatParagraph) {
    return toTextMapping.get(numberOfFlatParagraph);
  }
  
  /**
   * size of text cache (without headers, footnotes, etc.)
   */
  public int textSize() {
    return toParaMapping.size();
  }
  
  /**
   * Gives back the start paragraph for text level check
   */
  public int getStartOfParaCheck(int numCurPara, int parasToCheck, boolean textIsChanged) {
    if (numCurPara < 0 || toParaMapping.size() <= numCurPara) {
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
  public int getEndOfParaCheck(int numCurPara, int parasToCheck, boolean textIsChanged) {
    if (numCurPara < 0 || toParaMapping.size() <= numCurPara) {
      return -1;
    }
    int headingAfter = -1;
    if(parasToCheck < -1) {
      return toParaMapping.size();
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
    if(headingAfter < numCurPara || headingAfter > toParaMapping.size()) {
      headingAfter = toParaMapping.size();
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
  public String getDocAsString(int numCurPara, int parasToCheck, boolean textIsChanged) {
    int startPos = getStartOfParaCheck(numCurPara, parasToCheck, textIsChanged);
    int endPos = getEndOfParaCheck(numCurPara, parasToCheck, textIsChanged);
    if(startPos < 0 || endPos < 0) {
      return "";
    }
    StringBuilder docText = new StringBuilder(fixLinebreak(getTextParagraph(startPos)));
    for (int i = startPos + 1; i < endPos; i++) {
      docText.append(OfficeTools.END_OF_PARAGRAPH).append(fixLinebreak(getTextParagraph(i)));
    }
    return docText.toString();
  }

  /**
   * Change manual linebreak to distinguish from end of paragraph
   */
  public static String fixLinebreak (String text) {
    return text.replaceAll(OfficeTools.SINGLE_END_OF_PARAGRAPH, OfficeTools.MANUAL_LINEBREAK);
  }

  /**
   * Change manual linebreak to distinguish from end of paragraph
   */
  public static String removeZeroWidthSpace (String text) {
    return text.replaceAll(OfficeTools.ZERO_WIDTH_SPACE, "");
  }

  /**
   * Gives Back the StartPosition of Paragraph
   */
  public int getStartOfParagraph(int nPara, int checkedPara, int parasToCheck, boolean textIsChanged) {
    if (nPara < 0 || toParaMapping.size() <= nPara) {
      return -1;
    }
    int startPos = getStartOfParaCheck(checkedPara, parasToCheck, textIsChanged);
    if(startPos < 0) {
      return -1;
    }
    int pos = 0;
    for (int i = startPos; i < nPara; i++) {
      pos += getTextParagraph(i).length() + OfficeTools.NUMBER_PARAGRAPH_CHARS;
    }
    return pos;
  }


}
