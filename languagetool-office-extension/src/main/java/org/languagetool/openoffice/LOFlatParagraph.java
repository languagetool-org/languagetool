/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Fred Kruse
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

import com.sun.star.frame.XDesktop;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.text.TextMarkupType;
import com.sun.star.text.XFlatParagraph;
import com.sun.star.text.XFlatParagraphIterator;
import com.sun.star.text.XFlatParagraphIteratorProvider;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Information about Paragraphs of LibreOffice/OpenOffice documents
 * on the basis of the LO/OO FlatParagraph
 * @since 4.0
 * @author Fred Kruse
 */
public class LOFlatParagraph {
  
  private XFlatParagraphIterator xFlatParaIter = null;
  private XFlatParagraph xFlatPara = null;
  
  LOFlatParagraph(XComponentContext xContext) throws Exception {
    xFlatParaIter = getXFlatParagraphIterator(xContext);
    xFlatPara = getFlatParagraph(xFlatParaIter);
  }

  /**
   * Returns the current XDesktop
   * Returns null if it fails
   */
  @Nullable
  private static XDesktop getCurrentDesktop(XComponentContext xContext) throws Exception {
    try {
      if (xContext == null) {
        return null;
      }
      XMultiComponentFactory xMCF = UnoRuntime.queryInterface(XMultiComponentFactory.class,
              xContext.getServiceManager());
      if (xMCF == null) {
        return null;
      }
      Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
      if (desktop == null) {
        return null;
      }
      return UnoRuntime.queryInterface(XDesktop.class, desktop);
    } catch (Exception e) {
      printException(e);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }

  /** 
   * Returns the current XComponent 
   * Returns null if it fails
   */
  @Nullable
  private static XComponent getCurrentComponent(XComponentContext xContext) throws Exception {
    try {
      XDesktop xdesktop = getCurrentDesktop(xContext);
      if(xdesktop == null) {
        return null;
      }
      else return xdesktop.getCurrentComponent();
    } catch (Exception e) {
      printException(e);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
    
  /**
   * Returns XFlatParagraphIterator 
   * Returns null if it fails
   */
  @Nullable
  private static XFlatParagraphIterator getXFlatParagraphIterator (XComponentContext xContext) throws Exception {
    try {
      XComponent xCurrentComponent = getCurrentComponent(xContext);
      if(xCurrentComponent == null) {
        return null;
      }
      XFlatParagraphIteratorProvider xFlatParaItPro 
          = UnoRuntime.queryInterface(XFlatParagraphIteratorProvider.class, xCurrentComponent);
      if(xFlatParaItPro == null) {
        return null;
      }
      return xFlatParaItPro.getFlatParagraphIterator(TextMarkupType.PROOFREADING, true);
    } catch (Exception e) {
      printException(e);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /**
   * Returns FlatParagraph
   * Returns null if it fails
   */
  @Nullable
  private static XFlatParagraph getFlatParagraph(XFlatParagraphIterator xFlatParaIter) throws Exception {
    try {
    if(xFlatParaIter == null) {
      return null;
    }
    return xFlatParaIter.getLastPara();
    } catch (Exception e) {
      printException(e);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
    
  /** 
   * Prints Exception to default out  
   */
 private static void printException (Exception e) {
    //  TODO: Print exceptions in log-file
    System.out.println(e.getMessage());
  }

  /**
   * is true if FlatParagraph is from Automatic Iteration
   * else is false and at failure
   */
  public boolean isFlatParaFromIter() throws Exception {
    try {
    if(xFlatParaIter == null || xFlatPara == null) {
      return false;
    }
    if(xFlatParaIter.getParaBefore(xFlatPara) != null 
        || xFlatParaIter.getParaAfter(xFlatPara) != null) {
      return true;
    }
    return false;
    } catch (Exception e) {
      printException(e);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return false;          // Return false as method failed
    }
  }

  /**
   * Returns Current Paragraph Number from FlatParagaph
   * Returns -1 if it fails
   */
  public int getCurNumFlatParagraphs() throws Exception {
    try {
      if(xFlatParaIter == null || xFlatPara == null) {
        return -1;
      }
      int pos = -1;
      XFlatParagraph tmpXFlatPara = xFlatPara;
      while (tmpXFlatPara != null) {
        tmpXFlatPara = xFlatParaIter.getParaBefore(tmpXFlatPara);
        pos++;
      }
      return pos;
    } catch (Exception e) {
      printException(e);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return -1;           // Return -1 as method failed
    }
  }

  /**
   * Returns Text of all FlatParagraphs of Document
   * Returns null if it fails
   */
  @Nullable
  public List<String> getAllFlatParagraphs() throws Exception {
    try {
      List<String> allParas = new ArrayList<>();
      if(xFlatParaIter == null || xFlatPara == null) {
        return null;
      }
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
    } catch (Exception e) {
      printException(e);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }

  /**
   * Returns Number of all FlatParagraphs of Document
   * Returns negative value if it fails
   */
  public int getNumberOfAllFlatPara() throws Exception {
    try {
      if(xFlatParaIter == null || xFlatPara == null) {
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
    } catch (Exception e) {
      printException(e);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return -1;             // Return -1 as method failed
    }
  }
  
}
