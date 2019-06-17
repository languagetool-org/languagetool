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

import org.jetbrains.annotations.Nullable;

import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.XComponent;
import com.sun.star.text.TextMarkupType;
import com.sun.star.text.XFlatParagraph;
import com.sun.star.text.XFlatParagraphIterator;
import com.sun.star.text.XFlatParagraphIteratorProvider;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Information about Paragraphs of LibreOffice/OpenOffice documents
 * on the basis of the LO/OO FlatParagraphs
 * @since 4.0
 * @author Fred Kruse
 */
public class FlatParagraphTools {
  
  private static final boolean debugMode = false;   //  should be false except for testing
  
  private final XFlatParagraphIterator xFlatParaIter;
  private final XFlatParagraph xFlatPara;
  
  FlatParagraphTools(XComponentContext xContext) {
    xFlatParaIter = getXFlatParagraphIterator(xContext);
    xFlatPara = getFlatParagraph();
  }

  /**
   * Returns XFlatParagraphIterator 
   * Returns null if it fails
   */
  @Nullable
  private XFlatParagraphIterator getXFlatParagraphIterator(XComponentContext xContext) {
    try {
      XComponent xCurrentComponent = OfficeTools.getCurrentComponent(xContext);
      if (xCurrentComponent == null) {
        return null;
      }
      XFlatParagraphIteratorProvider xFlatParaItPro 
          = UnoRuntime.queryInterface(XFlatParagraphIteratorProvider.class, xCurrentComponent);
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
   * Returns FlatParagraph
   * Returns null if it fails
   */
  @Nullable
  private XFlatParagraph getFlatParagraph() {
    try {
      if (xFlatParaIter == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("!?! FlatParagraphIterator == null");
        }
        return null;
      }
      return xFlatParaIter.getLastPara();
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
    
  /**
   * is true if FlatParagraph is from Automatic Iteration
   * else is false and at failure
   */
  public boolean isFlatParaFromIter() {
    try {
    if (xFlatPara == null) {
      if (debugMode) {
        MessageHandler.printToLogFile("!?! FlatParagraph == null");
      }
      return false;
    }
      return xFlatParaIter.getParaBefore(xFlatPara) != null || xFlatParaIter.getParaAfter(xFlatPara) != null;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return false;          // Return false as method failed
    }
  }

  /**
   * Returns Current Paragraph Number from FlatParagaph
   * Returns -1 if it fails
   */
  int getCurNumFlatParagraphs() {
    try {
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("!?! FlatParagraph == null");
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
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("!?! FlatParagraph == null");
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
   * Returns Number of all FlatParagraphs of Document
   * Returns negative value if it fails
   */
  int getNumberOfAllFlatPara() {
    try {
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("!?! FlatParagraph == null");
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
        MessageHandler.printToLogFile("!?! FlatParagraph == null");
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
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("!?! FlatParagraph == null");
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
  void markFlatParasAsChecked(int from, int to, List<Boolean> isChecked) {
    try {
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("!?! FlatParagraph == null");
        }
        return;
      }
      if (isChecked == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("!?! List isChecked == null");
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
  public List<Boolean> isChecked(List<Integer> changedParas, int nDiv) {
    List<Boolean> isChecked = new ArrayList<>();
    try {
      if (xFlatPara == null) {
        if (debugMode) {
          MessageHandler.printToLogFile("!?! FlatParagraph == null");
        }
        return isChecked;
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
  
}
