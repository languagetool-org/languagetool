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
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.languagetool.openoffice.SingleCheck.SentenceErrors;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.XStringKeyMap;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.text.TextMarkupType;
import com.sun.star.text.XFlatParagraph;
import com.sun.star.text.XFlatParagraphIterator;
import com.sun.star.text.XFlatParagraphIteratorProvider;
import com.sun.star.text.XMarkingAccess;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.uno.UnoRuntime;

/**
 * Information about Paragraphs of LibreOffice/OpenOffice documents
 * on the basis of the LO/OO FlatParagraphs
 * @since 4.0
 * @author Fred Kruse
 */
public class FlatParagraphTools {
  
  private static boolean debugMode; //  should be false except for testing
  
  private XFlatParagraphIterator xFlatParaIter;
  private XFlatParagraph lastFlatPara;
  private XComponent xComponent;
  
  FlatParagraphTools(XComponent xComponent) {
    debugMode = OfficeTools.DEBUG_MODE_FP;
    this.xComponent = xComponent;
    xFlatParaIter = getXFlatParagraphIterator(xComponent);
    lastFlatPara = getCurrentFlatParagraph();
  }

  /**
   * Returns XFlatParagraphIterator 
   * Returns null if it fails
   */
  @Nullable
  private XFlatParagraphIterator getXFlatParagraphIterator(XComponent xComponent) {
    try {
      if (xComponent == null) {
        return null;
      }
      XFlatParagraphIteratorProvider xFlatParaItPro 
          = UnoRuntime.queryInterface(XFlatParagraphIteratorProvider.class, xComponent);
      if (xFlatParaItPro == null) {
        return null;
      }
      return xFlatParaItPro.getFlatParagraphIterator(TextMarkupType.PROOFREADING, true);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /**
   * Initialize XFlatParagraphIterator
   * Set the new iterator only if it is not null
   */
  public void init() {
    XFlatParagraphIterator tmpFlatParaIter = getXFlatParagraphIterator(xComponent);
    if (tmpFlatParaIter != null) {
      xFlatParaIter = tmpFlatParaIter;
    }
  }
  
  /**
   * Returns current FlatParagraph
   * Set lastFlatPara if current FlatParagraph is not null
   * Returns null if it fails
   */
  @Nullable
  private XFlatParagraph getCurrentFlatParagraph() {
    try {
      if (xFlatParaIter == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("getCurrentFlatParagraph: FlatParagraphIterator == null");
        }
        return null;
      }
      XFlatParagraph tmpFlatPara = xFlatParaIter.getNextPara();
      if (tmpFlatPara != null) {
        lastFlatPara = tmpFlatPara;
      }
      return tmpFlatPara;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
    
  /**
   * Returns last FlatParagraph not null
   * Set lastFlatPara if current FlatParagraph is not null
   * Returns null if it fails
   */
  @Nullable
  private XFlatParagraph getLastFlatParagraph() {
    getCurrentFlatParagraph();
    return lastFlatPara;
  }
    
  /**
   * is true if FlatParagraph is from Automatic Iteration
   */
  public boolean isFlatParaFromIter() {
    return (getCurrentFlatParagraph() != null);
  }

  /**
   * Change text of flat paragraph nPara 
   * delete characters between nStart and nStart + nLen, insert newText at nStart
   */
  public XFlatParagraph getFlatParagraphAt (int nPara) {
    try {
      XFlatParagraph xFlatPara = getLastFlatParagraph();
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("getFlatParagraphAt: FlatParagraph == null");
        }
        return null;
      }
      XFlatParagraph tmpFlatPara = xFlatPara;
      while (tmpFlatPara != null) {
        xFlatPara = tmpFlatPara;
        tmpFlatPara = xFlatParaIter.getParaBefore(tmpFlatPara);
      }
      int num = 0;
      while (xFlatPara != null && num < nPara) {
        xFlatPara = xFlatParaIter.getParaAfter(xFlatPara);
        num++;
      }
      if (xFlatPara == null) {
        MessageHandler.printToLogFile("getFlatParagraphAt: FlatParagraph == null; n = " + num + "; nPara = " + nPara);
        return null;
      }
      return xFlatPara;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;             // Return null as method failed
    }
  }
  
  /**
   * return text of current paragraph
   * return null if it fails
   */
  public String getCurrentParaText() {
    XFlatParagraph xFlatPara = getCurrentFlatParagraph();
    if (xFlatPara == null) {
      if (debugMode) {
        MessageHandler.printToLogFile("isCurrentFlatPara: FlatParagraph == null");
      }
      return null;
    }
    return xFlatPara.getText();
  }

  /**
   * Returns Current Paragraph Number from FlatParagaph
   * Returns -1 if it fails
   */
  int getCurNumFlatParagraph() {
    try {
      XFlatParagraph xFlatPara = getCurrentFlatParagraph();
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("getCurNumFlatParagraph: FlatParagraph == null");
        }
        return -1;
      }
      int pos = -1;
      XFlatParagraph tmpXFlatPara = xFlatPara;
      while (tmpXFlatPara != null) {
        tmpXFlatPara = xFlatParaIter.getParaBefore(tmpXFlatPara);
        pos++;
      }
      return pos;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return -1;           // Return -1 as method failed
    }
  }

  /**
   * Returns Text of all FlatParagraphs of Document
   * Returns null if it fails
   */
  @Nullable
  public ParagraphContainer getAllFlatParagraphs(Locale docLocale) {
    try {
      XFlatParagraph xFlatPara = getLastFlatParagraph();
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("getAllFlatParagraphs: FlatParagraph == null");
        }
        return null;
      }
      List<String> allParas = new ArrayList<>();
      List<Locale> locales = new ArrayList<>();
      List<int[]> footnotePositions = new ArrayList<>();
      XFlatParagraph tmpFlatPara = xFlatPara;
      Locale locale = null;
      while (tmpFlatPara != null) {
        String text = tmpFlatPara.getText();
        int len = text.length();
        allParas.add(0, text);
        footnotePositions.add(0, getPropertyValues("FootnotePositions", tmpFlatPara));
        // add just one local for the whole paragraph
        locale = getPrimaryParagraphLanguage(tmpFlatPara, len, docLocale, locale);
        locales.add(0, locale);
        tmpFlatPara = xFlatParaIter.getParaBefore(tmpFlatPara);
      }
      tmpFlatPara = xFlatParaIter.getParaAfter(xFlatPara);
      while (tmpFlatPara != null) {
        String text = tmpFlatPara.getText();
        int len = text.length();
        allParas.add(text);
        footnotePositions.add(getPropertyValues("FootnotePositions", tmpFlatPara));
        locale = getPrimaryParagraphLanguage(tmpFlatPara, len, docLocale, locale);
        locales.add(locale);
        if (debugMode) {
          printPropertyValueInfo(tmpFlatPara);
        }
        tmpFlatPara = xFlatParaIter.getParaAfter(tmpFlatPara);
      }
      return new ParagraphContainer(allParas, locales, footnotePositions);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /**
   * Get the language of paragraph 
   * @throws IllegalArgumentException 
   */
  private static Locale getParagraphLanguage(XFlatParagraph flatPara, int first, int len) throws IllegalArgumentException {
    Locale locale = flatPara.getLanguageOfText(first, len);
    if (locale == null || locale.Language.isEmpty()) {
      locale = flatPara.getPrimaryLanguageOfText(first, len);
    }
    return locale;
  }
  /**
   * Try to get the main language of paragraph 
   * @throws IllegalArgumentException 
   */
  public static Locale getPrimaryParagraphLanguage(XFlatParagraph flatPara, int len, Locale docLocale, Locale lastLocale) throws IllegalArgumentException {
    if (docLocale != null) {
      return docLocale;
    }
    if (debugMode) {
      MessageHandler.printToLogFile("len = " + len + "; lastLocale: " + (lastLocale == null ? "null" : lastLocale.Language));
      String textPortions = "";
      int[] portions = flatPara.getLanguagePortions();
      for (int i : portions) {
        textPortions += (i + ", ");
      }
      MessageHandler.printToLogFile("text portions(" + portions.length + "): " + textPortions);
      MessageHandler.printToLogFile("'" + flatPara.getText() +"'");
    }
    if (len == 0 && lastLocale != null) {
      return lastLocale;
    }
    if (len < 6) {
      Locale locale = getParagraphLanguage(flatPara, 0, len);
      if (debugMode) {
        MessageHandler.printToLogFile("len = " + len + "; locale: " + locale.Language);
      }
      return locale;
    }
    Locale locale1 = getParagraphLanguage(flatPara, 0, len/3);
    Locale locale2 = getParagraphLanguage(flatPara, len/3, len/3);
    if (OfficeTools.isEqualLocale(locale1, locale2)) {
      if (debugMode) {
        MessageHandler.printToLogFile("len = " + len + "; locale: " + locale1.Language);
      }
      return locale1;
    }
    Locale locale3 = getParagraphLanguage(flatPara, 2*len/3, len/3);
    if (OfficeTools.isEqualLocale(locale2, locale3)) {
      if (debugMode) {
        MessageHandler.printToLogFile("len = " + len + "; locale: " + locale2.Language);
      }
      return locale2;
    }
    if (debugMode) {
      MessageHandler.printToLogFile("len = " + len + "; locale: " + locale1.Language);
    }
    return locale1;
  }

  /**
   * Returns Number of all FlatParagraphs of Document from current FlatParagraph
   * Returns negative value if it fails
   */
  int getNumberOfAllFlatPara() {
    try {
      XFlatParagraph xFlatPara = getLastFlatParagraph();
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("getNumberOfAllFlatPara: FlatParagraph == null");
        }
        return -1;
      }
      XFlatParagraph tmpFlatPara = xFlatPara;
      int num = 0;
      while (tmpFlatPara != null) {
        tmpFlatPara = xFlatParaIter.getParaBefore(tmpFlatPara);
        num++;
      }
      tmpFlatPara = xFlatPara;
      num--;
      while (tmpFlatPara != null) {
        tmpFlatPara = xFlatParaIter.getParaAfter(tmpFlatPara);
        num++;
      }
      return num;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return -1;             // Return -1 as method failed
    }
  }

  /** 
   * Returns positions of properties by name 
   */
  private int[] getPropertyValues(String propName, XFlatParagraph xFlatPara) {
    if (xFlatPara == null) {
      if (debugMode) {
        MessageHandler.printToLogFile("getPropertyValues: FlatParagraph == null");
      }
      return  new int[]{};
    }
    XPropertySet paraProps = UnoRuntime.queryInterface(XPropertySet.class, xFlatPara);
    if (paraProps == null) {
      MessageHandler.printToLogFile("XPropertySet == null");
      return  new int[]{};
    }
    Object propertyValue;
    try {
      propertyValue = paraProps.getPropertyValue(propName);
      if (propertyValue instanceof int[]) {
        return (int[]) propertyValue;
      } else {
        MessageHandler.printToLogFile("Not of expected type int[]: " + propertyValue + ": " + propertyValue);
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);
    }
    return new int[]{};
  }
  
  /** 
   * Print property values to logfile
   */
  private void printPropertyValueInfo(XFlatParagraph xFlatPara) {
    if (xFlatPara == null) {
      MessageHandler.printToLogFile("printPropertyValues: FlatParagraph == null");
      return;
    }
    XPropertySet paraProps = UnoRuntime.queryInterface(XPropertySet.class, xFlatPara);
    if (paraProps == null) {
      MessageHandler.printToLogFile("XPropertySet == null");
      return;
    }
    MessageHandler.printToLogFile("Property Value Info:");
    try {
      XPropertySetInfo propertySetInfo = paraProps.getPropertySetInfo();
      
      for (Property property : propertySetInfo.getProperties()) {
        MessageHandler.printToLogFile("Name : " + property.Name + "; Type : " + property.Type.getTypeName() + "; Attributes : " + property.Attributes + "; Handle : " + property.Handle);
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);
    }
    return;
  }
  
  /**
   * Marks all paragraphs as checked with exception of the paragraphs "from" to "to"
   */
  void setFlatParasAsChecked(int from, int to, List<Boolean> isChecked) {
    try {
      XFlatParagraph xFlatPara = getLastFlatParagraph();
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("setFlatParasAsChecked: FlatParagraph == null");
        }
        return;
      }
      if (isChecked == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("setFlatParasAsChecked: List isChecked == null");
        }
        isChecked  = new ArrayList<>();
      }
      XFlatParagraph tmpFlatPara = xFlatPara;
      XFlatParagraph startFlatPara = xFlatPara;
      while (tmpFlatPara != null) {
        startFlatPara = tmpFlatPara;
        tmpFlatPara = xFlatParaIter.getParaBefore(tmpFlatPara);
      }
      tmpFlatPara = startFlatPara;
      int num = 0;
      while (tmpFlatPara != null && num < from) {
        if (debugMode && num >= isChecked.size()) {
          MessageHandler.printToLogFile("!?! List isChecked == null");
        }
        if (num < isChecked.size() && isChecked.get(num)) {
          tmpFlatPara.setChecked(TextMarkupType.PROOFREADING, true);
        }
        tmpFlatPara = xFlatParaIter.getParaAfter(tmpFlatPara);
        num++;
      }
      while (tmpFlatPara != null && num < to) {
        tmpFlatPara.setChecked(TextMarkupType.PROOFREADING, false);
        tmpFlatPara = xFlatParaIter.getParaAfter(tmpFlatPara);
        num++;
      }
      while (tmpFlatPara != null) {
        if (debugMode && num >= isChecked.size()) {
          MessageHandler.printToLogFile("!?! List isChecked == null");
        }
        if (num < isChecked.size() && isChecked.get(num)) {
          tmpFlatPara.setChecked(TextMarkupType.PROOFREADING, true);
        }
        tmpFlatPara = xFlatParaIter.getParaAfter(tmpFlatPara);
        num++;
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
    }
  }
  
  /**
   * Marks all paragraphs as checked
   */
  void setFlatParasAsChecked() {
    try {
      XFlatParagraph xFlatPara = getLastFlatParagraph();
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("setFlatParasAsChecked: FlatParagraph == null");
        }
        return;
      }
      XFlatParagraph tmpFlatPara = xFlatPara;
      while (tmpFlatPara != null) {
        tmpFlatPara.setChecked(TextMarkupType.PROOFREADING, true);
        tmpFlatPara = xFlatParaIter.getParaBefore(tmpFlatPara);
      }
      tmpFlatPara = xFlatPara;
      while (tmpFlatPara != null) {
        tmpFlatPara.setChecked(TextMarkupType.PROOFREADING, true);
        tmpFlatPara = xFlatParaIter.getParaAfter(tmpFlatPara);
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
    }
  }
  
  /**
   * Get information of checked status of all paragraphs
   */
  List<Boolean> isChecked(List<Integer> changedParas, int nDiv) {
    List<Boolean> isChecked = new ArrayList<>();
    try {
      XFlatParagraph xFlatPara = getLastFlatParagraph();
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("isChecked: FlatParagraph == null");
        }
        return null;
      }
      XFlatParagraph tmpFlatPara = xFlatPara;
      XFlatParagraph startFlatPara = xFlatPara;
      while (tmpFlatPara != null) {
        startFlatPara = tmpFlatPara;
        tmpFlatPara = xFlatParaIter.getParaBefore(tmpFlatPara);
      }
      tmpFlatPara = startFlatPara;
      for (int i = 0; tmpFlatPara != null; i++) {
        boolean dontCheck = (changedParas == null || !changedParas.contains(i - nDiv))
            && tmpFlatPara.isChecked(TextMarkupType.PROOFREADING);
        isChecked.add(dontCheck);
        tmpFlatPara = xFlatParaIter.getParaAfter(tmpFlatPara);
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
    }
    return isChecked;
  }
  
  /**
   * Set marks to changed paragraphs
   * if override is true existing marks are removed and marks are new set
   * else the marks are added to the existing marks
   */

  public void markParagraphs(Map<Integer, List<SentenceErrors>> changedParas, DocumentCache docCache, boolean override, XParagraphCursor cursor) {
    try {
      if (changedParas == null || changedParas.isEmpty() || docCache == null) {
        return;
      }
      XFlatParagraph xFlatPara = getLastFlatParagraph();
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("markParagraphs: FlatParagraph == null");
        }
        return;
      }
      if (override) {
        cursor.gotoStart(false);
      }
      XFlatParagraph tmpFlatPara = xFlatPara;
      XFlatParagraph startFlatPara = xFlatPara;
      while (tmpFlatPara != null) {
        startFlatPara = tmpFlatPara;
        tmpFlatPara = xFlatParaIter.getParaBefore(tmpFlatPara);
      }
      tmpFlatPara = startFlatPara;
      int num = 0;
      int nMarked = 0;
/*
      for (int n : changedParas.keySet()) {
        List<SentenceErrors> listErrors = changedParas.get(n);
        String s = "";
        for (int i = 0; i < listErrors.size(); i++) {
          s += "(" + listErrors.get(i).sentenceStart + "," + listErrors.get(i).sentenceEnd + ") ";
        }
        MessageHandler.printToLogFile("Mark Para " + n + ": " + s);
      }
*/
      while (tmpFlatPara != null && nMarked < changedParas.size() && num < docCache.textSize()) {
        int nTextPara = docCache.getNumberOfTextParagraph(num);
        if (changedParas.containsKey(num)) {
          addMarksToOneParagraph(tmpFlatPara, changedParas.get(num), nTextPara < 0 ? null : cursor, override);
          if (debugMode) {
            MessageHandler.printToLogFile("mark Paragraph: " + num + ", Text: " + tmpFlatPara.getText());
          }
          nMarked++;
        }
        if (override && cursor != null && nTextPara >= 0) {
          cursor.gotoNextParagraph(false);
        }
        tmpFlatPara = xFlatParaIter.getParaAfter(tmpFlatPara);
        num++;
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
    }
  }
  
  /**
   * add marks to existing marks of current paragraph
   */
  public void markCurrentParagraph(List<SentenceErrors> errorList) {
    if (errorList == null || errorList.size() == 0) {
      return;
    }
    XFlatParagraph xFlatPara = getCurrentFlatParagraph();
    if (xFlatPara == null) {
      if (debugMode) {
        MessageHandler.printToLogFile("markParagraphs: FlatParagraph == null");
      }
      return;
    }
    addMarksToOneParagraph(xFlatPara, errorList, null, false);
  }
    
  /**
   * add marks to existing marks of a paragraph
   * if override: existing marks will be overridden
   */
  private void addMarksToOneParagraph(XFlatParagraph flatPara, List<SentenceErrors> errorList, XParagraphCursor cursor, boolean override) {
    boolean isChecked = flatPara.isChecked(TextMarkupType.PROOFREADING);
    if (override && cursor != null) {
      XMarkingAccess xMarkingAccess = UnoRuntime.queryInterface(XMarkingAccess.class, cursor);
      if (xMarkingAccess == null) {
        MessageHandler.printToLogFile("xMarkingAccess == null");
      } else {
        xMarkingAccess.invalidateMarkings(TextMarkupType.PROOFREADING);
        XComponent markComponent = UnoRuntime.queryInterface(XComponent.class, xMarkingAccess);
        if (markComponent != null) {
          markComponent.dispose();
        }
      }
    }
    for (SentenceErrors errors : errorList) {
      XStringKeyMap props;
      for (SingleProofreadingError pError : errors.sentenceErrors) {
        props = flatPara.getMarkupInfoContainer();
        PropertyValue[] properties = pError.aProperties;
        int color = -1;
        short type = -1;
        for (PropertyValue property : properties) {
          if ("LineColor".equals(property.Name)) {
            color = (int) property.Value;
          } else if ("LineType".equals(property.Name)) {
            type = (short) property.Value;
          }
        }
        try {
          if (color >= 0) {
            props.insertValue("LineColor", color);
          }
          if (type > 0) {
            props.insertValue("LineType", type);
          }
        } catch (Throwable t) {
          MessageHandler.printException(t);
        }
        flatPara.commitStringMarkup(TextMarkupType.PROOFREADING, pError.aRuleIdentifier, 
            pError.nErrorStart, pError.nErrorLength, props);
      }
      if (override) {
        props = flatPara.getMarkupInfoContainer();
        flatPara.commitStringMarkup(TextMarkupType.SENTENCE, "Sentence", errors.sentenceStart, errors.sentenceEnd - errors.sentenceStart, props);
      }
      if (isChecked) {
        flatPara.setChecked(TextMarkupType.PROOFREADING, true);
      }
    }
  }

  public class ParagraphContainer {
    public List<String> paragraphs;
    public List<Locale> locales;
    public List<int[]> footnotePositions;
    
    ParagraphContainer(List<String> paragraphs, List<Locale> locales, List<int[]> footnotePositions) {
      this.paragraphs = paragraphs;
      this.locales = locales;
      this.footnotePositions = footnotePositions;
    }
  }
  
  /**
   * Change text of flat paragraph nPara 
   * delete characters between nStart and nStart + nLen, insert newText at nStart
   */
  public void changeTextOfParagraph (int nPara, int nStart, int nLen, String newText) {
    try {
      XFlatParagraph xFlatPara = getLastFlatParagraph();
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("changeTextOfParagraph: FlatParagraph == null");
        }
        return;
      }
      XFlatParagraph tmpFlatPara = xFlatPara;
      while (tmpFlatPara != null) {
        xFlatPara = tmpFlatPara;
        tmpFlatPara = xFlatParaIter.getParaBefore(tmpFlatPara);
      }
      int num = 0;
      while (xFlatPara != null && num < nPara) {
        xFlatPara = xFlatParaIter.getParaAfter(xFlatPara);
        num++;
      }
      if (xFlatPara == null) {
        MessageHandler.printToLogFile("changeTextOfParagraph: FlatParagraph == null; n = " + num + "; nPara = " + nPara);
        return;
      }
      xFlatPara.changeText(nStart, nLen, newText, new PropertyValue[0]);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return;             // Return -1 as method failed
    }
  }
  
  /**
   * Change text of flat paragraph nPara 
   * delete characters between nStart and nStart + nLen, insert newText at nStart
   */
  public void setLanguageOfParagraph (int nPara, int nStart, int nLen, Locale locale) {
    try {
      XFlatParagraph xFlatPara = getLastFlatParagraph();
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("setLanguageOfParagraph: FlatParagraph == null");
        }
        return;
      }
      XFlatParagraph tmpFlatPara = xFlatPara;
      while (tmpFlatPara != null) {
        xFlatPara = tmpFlatPara;
        tmpFlatPara = xFlatParaIter.getParaBefore(tmpFlatPara);
      }
      int num = 0;
      while (xFlatPara != null && num < nPara) {
        xFlatPara = xFlatParaIter.getParaAfter(xFlatPara);
        num++;
      }
      if (xFlatPara == null) {
        MessageHandler.printToLogFile("setLanguageOfParagraph: FlatParagraph == null; n = " + num + "; nPara = " + nPara);
        return;
      }
      PropertyValue[] propertyValues = { new PropertyValue("CharLocale", -1, locale, PropertyState.DIRECT_VALUE) };
      xFlatPara.changeAttributes(nStart, nLen, propertyValues);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return;             // Return -1 as method failed
    }
  }
  
}
