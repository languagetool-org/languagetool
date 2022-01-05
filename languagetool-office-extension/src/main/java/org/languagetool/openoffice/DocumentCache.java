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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.languagetool.openoffice.FlatParagraphTools.ParagraphContainer;
import org.languagetool.openoffice.OfficeDrawTools.ImpressParagraphContainer;

import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;

/**
 * Class to store the Text of a LO document (document cache)
 * @since 5.0
 * @author Fred Kruse
 */
public class DocumentCache implements Serializable {
  
  private static final long serialVersionUID = 2L;

  private static boolean debugMode;                         //  should be false except for testing

  private List<String> paragraphs = null;                   //  stores the flat paragraphs of document
  private List<Integer> chapterBegins = null;               //  stores the paragraphs formated as headings; is used to subdivide the document in chapters
  private List<SerialLocale> locales = null;                //  stores the language of the paragraphs;
  private List<int[]> footnotes = null;                     //  stores the footnotes of the paragraphs;
  private List<Integer> toTextMapping = new ArrayList<>();  //  Mapping from FlatParagraph to DocumentCursor
  private List<Integer> toParaMapping = new ArrayList<>();  //  Mapping from DocumentCursor to FlatParagraph
  private int defaultParaCheck;
  private boolean isImpress = false;
  private boolean isReset = false;

  DocumentCache(DocumentCursorTools docCursor, FlatParagraphTools flatPara, int defaultParaCheck, 
      Locale docLocale, XComponent xComponent, boolean isImpress) {
    debugMode = OfficeTools.DEBUG_MODE_DC;
    this.defaultParaCheck = defaultParaCheck;
    this.isImpress = isImpress;
    if (isImpress) {
      resetImpressCache(xComponent);
    } else {
      reset(docCursor, flatPara, docLocale);
    }
  }
  
  DocumentCache(DocumentCache in) {
    paragraphs = new ArrayList<String>(in.paragraphs);
    chapterBegins = new ArrayList<Integer>(in.chapterBegins);
    locales = new ArrayList<SerialLocale>(in.locales);
    footnotes = new ArrayList<int[]>(in.footnotes);
    toTextMapping = new ArrayList<Integer>(in.toTextMapping);
    toParaMapping = new ArrayList<Integer>(in.toParaMapping);
    defaultParaCheck = in.defaultParaCheck;
    isImpress = in.isImpress;
  }
  
  DocumentCache(List<String> paragraphs, List<String> textParagraphs, List<int[]> footnotes, Locale locale) {
    this.paragraphs = paragraphs;
    this.footnotes = footnotes;
    chapterBegins = new ArrayList<Integer>();
    locales = new ArrayList<SerialLocale>();
    for (int i = 0; i < paragraphs.size(); i++) {
      locales.add(new SerialLocale(locale));
    }
    mapParagraphs(textParagraphs);
  }
  
  /**
   * reset the document cache
   * load the actual state of the document into the cache
   * is only used for writer documents 
   */
  public synchronized void reset(DocumentCursorTools docCursor, FlatParagraphTools flatPara, Locale docLocale) {
    try {
      isReset = true;
      List<String> textParas = docCursor.getAllTextParagraphs();
      ParagraphContainer paragraphContainer = null;
      if (textParas != null) {
        chapterBegins = docCursor.getParagraphHeadings();
        paragraphContainer = flatPara.getAllFlatParagraphs(docLocale);
        if (paragraphContainer == null) {
          MessageHandler.printToLogFile("paragraphContainer == null - ParagraphCache not initialised");
          paragraphs = null;
          isReset = false;
          return;
        }
        paragraphs = paragraphContainer.paragraphs;
        locales = new ArrayList<SerialLocale>();
        for (Locale locale :  paragraphContainer.locales) {
          locales.add(new SerialLocale(locale)) ;
        }
        footnotes = paragraphContainer.footnotePositions;
      }
      if (paragraphs == null) {
        MessageHandler.printToLogFile("paragraphs == null - ParagraphCache not initialised");
        isReset = false;
        return;
      }
      mapParagraphs(textParas);
    } finally {
      isReset = false;
    }
  }

  /**
   * Map text paragraphs to flat paragraphs
   * is only used for writer documents 
   */
  private void mapParagraphs(List<String> textParas) {
    if (debugMode) {
      MessageHandler.printToLogFile("\n\nNot mapped paragraphs:");
    }
    if (textParas != null && !textParas.isEmpty()) {
      int n = 0; 
      for (int i = 0; i < paragraphs.size(); i++) {
        if ((footnotes != null && i < footnotes.size() && footnotes.get(i).length > 0)
            || (n < textParas.size() && (paragraphs.get(i).equals(textParas.get(n)) 
                || removeZeroWidthSpace(paragraphs.get(i)).equals(textParas.get(n))))) {
          toTextMapping.add(n);
          toParaMapping.add(i);
          n++;
        } else {
          toTextMapping.add(-1);
          if (debugMode) {
            MessageHandler.printToLogFile("\nFlat("  + i + "): '" + paragraphs.get(i));
            if (n < textParas.size()) {
              MessageHandler.printToLogFile("Doc("  + n + "): '" + textParas.get(n));
            }
          }  
        }
      }
      prepareChapterBegins();
      isReset = false;
      if (debugMode) {
        MessageHandler.printToLogFile("\n\ntoParaMapping:");
        for (int i = 0; i < toParaMapping.size(); i++) {
          MessageHandler.printToLogFile("Doc: " + i + " Flat: " + toParaMapping.get(i)
          + OfficeTools.LOG_LINE_BREAK + getTextParagraph(i));
        }
        MessageHandler.printToLogFile("\n\ntoTextMapping:");
        for (int i = 0; i < toTextMapping.size(); i++) {
          MessageHandler.printToLogFile("Flat: " + i + " Doc: " + toTextMapping.get(i) + " locale: " + locales.get(i).Language + "-" + locales.get(i).Country);
        }
        MessageHandler.printToLogFile("\n\nheadings:");
        for (int i = 0; i < chapterBegins.size(); i++) {
          MessageHandler.printToLogFile("Num: " + i + " Heading: " + chapterBegins.get(i));
        }
        MessageHandler.printToLogFile("\nNumber of Flat Paragraphs: " + paragraphs.size());
        MessageHandler.printToLogFile("Number of Text Paragraphs: " + toParaMapping.size());
        MessageHandler.printToLogFile("Number of footnotes: " + footnotes.size());
        MessageHandler.printToLogFile("Number of locales: " + locales.size());
      }
    }
  }
  
  /**
   * reset the document cache for impress documents
   */
  public void resetImpressCache(XComponent xComponent) {
    ImpressParagraphContainer container = OfficeDrawTools.getAllParagraphs(xComponent);
    paragraphs = container.paragraphs;
    chapterBegins = container.pageBegins;
    locales = new ArrayList<SerialLocale>();
    for (Locale locale :  container.locales) {
      locales.add(new SerialLocale(locale)) ;
    }
    footnotes = new ArrayList<>();
    for (int i = 0; i < paragraphs.size(); i++) {
      toTextMapping.add(i);
      toParaMapping.add(i);
      footnotes.add(new int[0]);
    }
  }

  /**
   * wait till reset is finished
   */
  public boolean isFinished() {
    while (isReset) {
      try {
        Thread.sleep(5);
      } catch (InterruptedException e) {
        MessageHandler.showError(e);
        return false;
      }
    }
    return paragraphs == null ? false: true;
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
   * set Flat Paragraph and Locale at Index
   */
  public void setFlatParagraph(int n, String sPara, Locale locale) {
    paragraphs.set(n, sPara);
    locales.set(n, new SerialLocale(locale));
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
    return locales.get(n).toLocale();
  }
  
  /**
   * set Locale of Flat Paragraph by Index
   */
  public void setFlatParagraphLocale(int n, Locale locale) {
    locales.set(n, new SerialLocale(locale));
  }
  
  /**
   * get Locale of Text Paragraph by Index
   */
  public Locale getTextParagraphLocale(int n) {
    return locales.get(toParaMapping.get(n)).toLocale();
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
   * set footnotes of Flat Paragraph by Index
   */
  public void setFlatParagraphFootnotes(int n, int[] footnotePos) {
    footnotes.set(n, footnotePos);
  }
  
  /**
   * get footnotes of Text Paragraph by Index
   */
  public void setTextParagraphFootnotes(int n, int[] footnotePos) {
    footnotes.set(toParaMapping.get(n), footnotePos);
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
   * get Number of Text Paragraph from Number of Flat Paragraph
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
   * size of text cache (without headers, footnotes, etc.)
   */
  public boolean isEqual(int n, String text, Locale locale) {
    return ((n < 0 || n >= locales.size() || locales.get(n) == null) ? false :
        (locales.get(n).equalsLocale(locale) && text.equals(paragraphs.get(n))));
  }
  
  /**
   * Gives back the start paragraph for text level check
   */
  public int getStartOfParaCheck(int numCurPara, int parasToCheck, boolean checkOnlyParagraph, boolean useQueue, boolean addParas) {
    if (numCurPara < 0 || toParaMapping.size() <= numCurPara) {
      return -1;
    }
    if (parasToCheck < -1) {
      return 0;
    }
    if (parasToCheck == 0) {
      return numCurPara;
    }
    int headingBefore = 0;
    for (int heading : chapterBegins) {
      if (heading > numCurPara) {
        break;
      } 
      headingBefore = heading;
    }
    if (headingBefore == numCurPara || parasToCheck < 0 || (useQueue && !checkOnlyParagraph)) {
      return headingBefore;
    }
    int startPos = numCurPara - parasToCheck;
    if (addParas) {
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
  public int getEndOfParaCheck(int numCurPara, int parasToCheck, boolean checkOnlyParagraph, boolean useQueue, boolean addParas) {
    if (numCurPara < 0 || toParaMapping.size() <= numCurPara) {
      return -1;
    }
    if (parasToCheck < -1) {
      return toParaMapping.size();
    }
    int headingAfter = -1;
    if (parasToCheck == 0) {
      return numCurPara + 1;
    }
    for (int heading : chapterBegins) {
      headingAfter = heading;
      if (heading > numCurPara) {
        break;
      }
    }
    if (headingAfter <= numCurPara || headingAfter > toParaMapping.size()) {
      headingAfter = toParaMapping.size();
    }
    if (parasToCheck < 0 || (useQueue && !checkOnlyParagraph)) {
      return headingAfter;
    }
    int endPos = numCurPara + 1 + parasToCheck;
    if (!checkOnlyParagraph) {
      endPos += defaultParaCheck;
    } 
    if (addParas) {
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
  public String getDocAsString(int numCurPara, int parasToCheck, boolean checkOnlyParagraph, boolean useQueue, boolean hasFootnotes) {
    int startPos = getStartOfParaCheck(numCurPara, parasToCheck, checkOnlyParagraph, useQueue, true);
    int endPos = getEndOfParaCheck(numCurPara, parasToCheck, checkOnlyParagraph, useQueue, true);
    if (startPos < 0 || endPos < 0 || (hasFootnotes && getTextParagraph(startPos).isEmpty() && getTextParagraphFootnotes(startPos).length > 0)) {
      return "";
    }
    StringBuilder docText = new StringBuilder(fixLinebreak(SingleCheck.removeFootnotes(getTextParagraph(startPos), 
        (hasFootnotes ? getTextParagraphFootnotes(startPos) : null))));
    for (int i = startPos + 1; i < endPos; i++) {
      docText.append(OfficeTools.END_OF_PARAGRAPH).append(fixLinebreak(SingleCheck.removeFootnotes(getTextParagraph(i), 
          (hasFootnotes ? getTextParagraphFootnotes(i) : null))));
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
  public int getStartOfParagraph(int nPara, int checkedPara, int parasToCheck, boolean textIsChanged, boolean useQueue, boolean hasFootnotes) {
    if (nPara < 0 || toParaMapping.size() <= nPara) {
      return -1;
    }
    int startPos = getStartOfParaCheck(checkedPara, parasToCheck, textIsChanged, useQueue, true);
    if (startPos < 0) {
      return -1;
    }
    int pos = 0;
    for (int i = startPos; i < nPara; i++) {
      pos += SingleCheck.removeFootnotes(getTextParagraph(i), 
          (hasFootnotes ? getTextParagraphFootnotes(i) : null)).length() + OfficeTools.NUMBER_PARAGRAPH_CHARS;
    }
    return pos;
  }
  
  /**
   *  Add the next chapter begin after Heading 
   *  and changes of language to the chapter begins
   */
  private void prepareChapterBegins() {
    List<Integer> prepChBegins = new ArrayList<Integer>(chapterBegins);
    for (int begin : chapterBegins) {
      if (!prepChBegins.contains(begin + 1)) {
        prepChBegins.add(begin + 1);
      }
    }
    if (locales.size() > 0) {
      SerialLocale lastLocale = locales.get(0);
      for (int i = 1; i < locales.size(); i++) {
        if (locales != null && !locales.get(i).equalsLocale(lastLocale)) {
          int nText = getNumberOfTextParagraph(i);
          if (nText >= 0) {
            if (!prepChBegins.contains(nText)) {
              prepChBegins.add(nText);
            }
            lastLocale = locales.get(i);
            if (debugMode) {
              MessageHandler.printToLogFile("Paragraph("  + i + "): Locale changed to: " + lastLocale.Language + (lastLocale.Country == null ? "" : ("-" + lastLocale.Country)));
            }
          }
        }
      }
    }
    prepChBegins.sort(null);
    chapterBegins = prepChBegins;
  }

  /**
   * Class of serializable locale
   * needed to save cache
   */
  class SerialLocale implements Serializable {
     
    private static final long serialVersionUID = 1L;
    String Country;
    String Language;
    String Variant;
   
    SerialLocale(Locale locale) {
      this.Country = locale.Country;
      this.Language = locale.Language;
      this.Variant = locale.Variant;
    }
    
    /**
     * return the language as Locale
     */
    Locale toLocale() {
      return new Locale(Language, Country, Variant); 
    }

    /**
     * True if the Language is the same as Locale
     */
    boolean equalsLocale(Locale locale) {
      return ((locale == null || Language == null || Country == null || Variant == null)? false : 
          Language.equals(locale.Language) && Country.equals(locale.Country) && Variant.equals(locale.Variant));
    }

    boolean equalsLocale(SerialLocale locale) {
      return ((locale == null || Language == null || Country == null || Variant == null)? false : 
          Language.equals(locale.Language) && Country.equals(locale.Country) && Variant.equals(locale.Variant));
    }

  }

}
