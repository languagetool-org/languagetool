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

import com.sun.star.frame.XDesktop;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.text.TextMarkupType;
import com.sun.star.text.XFlatParagraph;
import com.sun.star.text.XFlatParagraphIterator;
import com.sun.star.text.XFlatParagraphIteratorProvider;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Information about Paragraphs of LibreOffice/OpenOffice documents
 * on the basis of the LO/OO FlatParagraph
 * @since 4.0
 * @author Fred Kruse
 */
public class LOFlatParagraph {
  
  private final XFlatParagraphIterator xFlatParaIter;
  private final XFlatParagraph xFlatPara;
  
  LOFlatParagraph(XComponentContext xContext) throws Exception {
    xFlatParaIter = getXFlatParagraphIterator(xContext);
    xFlatPara = getFlatParagraph(xFlatParaIter);
  }

  private static XDesktop getCurrentDesktop(XComponentContext xContext) throws Exception {
    if (xContext == null) return null;
    XMultiComponentFactory xMCF = UnoRuntime.queryInterface(XMultiComponentFactory.class,
            xContext.getServiceManager());
    if (xMCF == null) return null;
    Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
    if (desktop == null) return null;
    return UnoRuntime.queryInterface(XDesktop.class, desktop);
  }

  /** Returns the current XComponent */
  private static XComponent getCurrentComponent(XComponentContext xContext) throws Exception {
    XDesktop xdesktop = getCurrentDesktop(xContext);
    if(xdesktop == null) return null;
    else return xdesktop.getCurrentComponent();
  }
    
  /** Returns XFlatParagraphIterator */
  private static XFlatParagraphIterator getXFlatParagraphIterator (XComponentContext xContext) throws Exception {
    XComponent xCurrentComponent = getCurrentComponent(xContext);
    if(xCurrentComponent == null) return null;
    XFlatParagraphIteratorProvider xFlatParaItPro 
        = UnoRuntime.queryInterface(XFlatParagraphIteratorProvider.class, xCurrentComponent);
    if(xFlatParaItPro == null) return null;
    return xFlatParaItPro.getFlatParagraphIterator(TextMarkupType.PROOFREADING, true);
  }
  
  /** Returns FlatParagraph */
  private static XFlatParagraph getFlatParagraph(XFlatParagraphIterator xFlatParaIter) {
    if(xFlatParaIter == null) return null;
    return xFlatParaIter.getLastPara();
  }
    
  /** Is FlatParagraph from Automatic Iteration */
  public boolean isFlatParaFromIter() throws IllegalArgumentException {
    if(xFlatParaIter == null || xFlatPara == null) return false;
    if(xFlatParaIter.getParaBefore(xFlatPara) != null 
        || xFlatParaIter.getParaAfter(xFlatPara) != null) return true;
    return false;
  }

  /** Returns Current Paragraph Number from FlatParagaph */
  public int getCurNumFlatParagraphs() throws IllegalArgumentException {
    if(xFlatParaIter == null || xFlatPara == null) return -1;
    int pos = -1;
    XFlatParagraph tmpXFlatPara = xFlatPara;
    while (tmpXFlatPara != null) {
      tmpXFlatPara = xFlatParaIter.getParaBefore(tmpXFlatPara);
      pos++;
    }
    return pos;
  }

  /** Returns Text of all FlatParagraphs of Document */
  public List<String> getAllFlatParagraphs() throws IllegalArgumentException {
    List<String> allParas = new ArrayList<>();
    if(xFlatParaIter == null || xFlatPara == null) return allParas;
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
  }

  /** Returns Number of all FlatParagraphs of Document / Returns < 0 on Error  */
  public int getNumberOfAllFlatPara() throws IllegalArgumentException {
    if(xFlatParaIter == null || xFlatPara == null) return -1;
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
  }
  
}
