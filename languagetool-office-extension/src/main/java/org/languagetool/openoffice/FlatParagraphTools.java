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

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XStringKeyMap;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.text.TextMarkupDescriptor;
import com.sun.star.text.TextMarkupType;
import com.sun.star.text.XFlatParagraph;
import com.sun.star.text.XFlatParagraphIterator;
import com.sun.star.text.XFlatParagraphIteratorProvider;
import com.sun.star.text.XMarkingAccess;
import com.sun.star.text.XMultiTextMarkup;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.uno.UnoRuntime;

/**
 * Information about Paragraphs of LibreOffice/OpenOffice documents
 * on the basis of the LO/OO FlatParagraphs
 * @since 4.0
 * @author Fred Kruse
 */
public class FlatParagraphTools {
  
  private static final boolean debugMode = false;   //  should be false except for testing
  
  private final XFlatParagraphIterator xFlatParaIter;
  private XFlatParagraph lastFlatPara;
  private XComponent xComponent;
  
  FlatParagraphTools(XComponent xComponent) {
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
      XFlatParagraph tmpFlatPara = xFlatParaIter.getLastPara();
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
   * else is false and at failure
   */
  public boolean isFlatParaFromIter() {
    return (getCurrentFlatParagraph() != null);
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
  public List<String> getAllFlatParagraphs() {
    try {
      XFlatParagraph xFlatPara = getLastFlatParagraph();
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("getAllFlatParagraphs: FlatParagraph == null");
        }
        return null;
      }
      List<String> allParas = new ArrayList<>();
      XFlatParagraph tmpFlatPara = xFlatPara;
      while (tmpFlatPara != null) {
        allParas.add(0, tmpFlatPara.getText());
        tmpFlatPara = xFlatParaIter.getParaBefore(tmpFlatPara);
      }
      tmpFlatPara = xFlatParaIter.getParaAfter(xFlatPara);
      while (tmpFlatPara != null) {
        allParas.add(tmpFlatPara.getText());
        tmpFlatPara = xFlatParaIter.getParaAfter(tmpFlatPara);
      }
      return allParas;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }

  /**
   * Returns Number of all FlatParagraphs of Document from current FlatParagraph
   * Returns negative value if it fails
   */
  int getNumberOfAllFlatPara() {
    try {
      XFlatParagraph xFlatPara = getCurrentFlatParagraph();
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
   * Returns the absolute positions of all footnotes (and endnotes) of the text
   */
  List<int[]> getFootnotePositions() {
    List<int[]> paraPositions = new ArrayList<>();
    try {
      XFlatParagraph xFlatPara = getLastFlatParagraph();
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("getFootnotePositions: FlatParagraph == null");
        }
        return paraPositions;
      }

      XFlatParagraph tmpFlatPara = xFlatPara;
      XFlatParagraph lastFlatPara = xFlatPara;

      while (tmpFlatPara != null) {
        lastFlatPara = tmpFlatPara;
        tmpFlatPara = xFlatParaIter.getParaBefore(tmpFlatPara);
      }
      tmpFlatPara = lastFlatPara;
      while (tmpFlatPara != null) {
        int[] footnotePositions = getPropertyValues("FootnotePositions", tmpFlatPara);
        paraPositions.add(footnotePositions);
        tmpFlatPara = xFlatParaIter.getParaAfter(tmpFlatPara);
      }
      return paraPositions;
    } catch (Throwable t) {
      MessageHandler.printException(t);        // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return paraPositions;     // Return empty list as method failed
    }
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
        if(num < isChecked.size() && isChecked.get(num)) {
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
        if(num < isChecked.size() && isChecked.get(num)) {
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

  public void markParagraphs(Map<Integer, SingleProofreadingError[]> changedParas, int nDiv, boolean override) {
    try {
      if(changedParas == null || changedParas.isEmpty()) {
        return;
      }
      XFlatParagraph xFlatPara = getLastFlatParagraph();
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("markParagraphs: FlatParagraph == null");
        }
        return;
      }
      XParagraphCursor cursor = null;
      if(override) {
        DocumentCursorTools docCursor = new DocumentCursorTools(xComponent);
        if(docCursor != null) {
          cursor = docCursor.getParagraphCursor();
        }
        if (cursor == null) {
          MessageHandler.printToLogFile("cursor == null");
        }
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
      while (tmpFlatPara != null && nMarked < changedParas.size()) {
        if(changedParas.containsKey(num - nDiv)) {
          if(override) {
            setMarksToOneParagraph(tmpFlatPara, changedParas.get(num - nDiv), cursor);
          } else {
            addMarksToOneParagraph(tmpFlatPara, changedParas.get(num - nDiv));
          }
          nMarked++;
        }
        if (override && cursor != null && num >= nDiv) {
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
   * add marks to existing marks of a paragraph
   */
  private void addMarksToOneParagraph(XFlatParagraph flatPara, SingleProofreadingError[] pErrors) {
    XStringKeyMap props = flatPara.getMarkupInfoContainer();
    for(SingleProofreadingError pError : pErrors) {
      props = flatPara.getMarkupInfoContainer();
      PropertyValue[] properties = pError.aProperties;
      int color = -1;
      for(PropertyValue property : properties) {
        if("LineColor".equals(property.Name)) {
          color = (int) property.Value;
        }
      }
      if(color >= 0) {
        try {
          props.insertValue("LineColor", color);
        } catch (Throwable t) {
          MessageHandler.printException(t);
        }
      }
      flatPara.commitStringMarkup(TextMarkupType.PROOFREADING, pError.aRuleIdentifier, 
          pError.nErrorStart, pError.nErrorLength, props);
    }
  }

  /**
   * overrides existing marks of a paragraph
   */
  private void setMarksToOneParagraph(XFlatParagraph flatPara, SingleProofreadingError[] pErrors, XParagraphCursor cursor) {
    XMultiTextMarkup xMultiTextMarkup;
    try {
      xMultiTextMarkup = UnoRuntime.queryInterface(XMultiTextMarkup.class, flatPara);
      if (xMultiTextMarkup == null) {
        MessageHandler.printToLogFile("xMultiTextMarkup == null");
        return;
      }
    } catch (Throwable e) {
      MessageHandler.printException(e);
      return;
    }
    if(cursor != null) {
      XMarkingAccess xMarkingAccess = UnoRuntime.queryInterface(XMarkingAccess.class, cursor);
      if (xMarkingAccess == null) {
        MessageHandler.printToLogFile("xMarkingAccess == null");
      } else {
        xMarkingAccess.invalidateMarkings(TextMarkupType.PROOFREADING);
        flatPara.setChecked(TextMarkupType.PROOFREADING, true);
      }
    }
    XStringKeyMap props;
    TextMarkupDescriptor textMarkups[] = new TextMarkupDescriptor[pErrors.length + 1];
    for(int i = 0; i < pErrors.length; i++) {
      SingleProofreadingError pError = pErrors[i];
      props = flatPara.getMarkupInfoContainer();
      PropertyValue[] properties = pError.aProperties;
      int color = -1;
      for(PropertyValue property : properties) {
        if("LineColor".equals(property.Name)) {
          color = (int) property.Value;
        }
      }
      if(color >= 0) {
        try {
          props.insertValue("LineColor", color);
        } catch (Throwable t) {
          MessageHandler.printException(t);
        }
      }
      TextMarkupDescriptor textMarkup = new TextMarkupDescriptor();
      textMarkup.nType = TextMarkupType.PROOFREADING;
      textMarkup.nOffset = pError.nErrorStart;
      textMarkup.nLength = pError.nErrorLength;
      textMarkup.aIdentifier = pError.aRuleIdentifier;
      textMarkup.xMarkupInfoContainer = props;
      textMarkups[i] = textMarkup;
    }
    props = flatPara.getMarkupInfoContainer();
    TextMarkupDescriptor textMarkup = new TextMarkupDescriptor();
    textMarkup.nType = TextMarkupType.SENTENCE;
    textMarkup.nOffset = 0;
    textMarkup.nLength = flatPara.getText().length();
    textMarkup.aIdentifier = new String ("Sentence");
    textMarkup.xMarkupInfoContainer = props;
    textMarkups[pErrors.length] = textMarkup;
    try {
      xMultiTextMarkup.commitMultiTextMarkup(textMarkups);
    } catch (IllegalArgumentException t) {
      MessageHandler.printException(t);
    }
  }
}
