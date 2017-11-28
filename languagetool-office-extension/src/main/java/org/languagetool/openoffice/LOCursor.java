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

import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Information about Paragraphs of LibreOffice/OpenOffice documents
 * on the basis of the LO/OO ViewCursor
 * @since 4.0
 * @author Fred Kruse
 */
class LOCursor {
  
  private final XParagraphCursor xPCursor;
  private final XTextViewCursor xVCursor;
  
  LOCursor(XComponentContext xContext) throws Exception {
    xPCursor = getParagraphCursor(xContext);
    xVCursor = getViewCursor(xContext);
  }

  /**
   * Returns the current XDesktop
   */
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
    
  /** Returns the current text document (if any) */
  private static XTextDocument getCurrentDocument(XComponentContext xContext) throws Exception {
    XComponent curcomp = getCurrentComponent(xContext);
    if (curcomp == null) return null;
    else return UnoRuntime.queryInterface(XTextDocument.class, curcomp);
  }

  /** Returns the text cursor (if any) */
  private static XTextCursor getCursor(XComponentContext xContext) throws Exception {
    XTextDocument curdoc = getCurrentDocument(xContext);
    if (curdoc == null) return null;
    XText xText = curdoc.getText();
    if (xText == null) return null;
    else return xText.createTextCursor();
  }

  /** Returns ParagraphCursor from TextCursor */
  private static XParagraphCursor getParagraphCursor(XComponentContext xContext) throws Exception {
    XTextCursor xcursor = getCursor(xContext);
    if(xcursor == null) return null;
    return UnoRuntime.queryInterface(XParagraphCursor.class, xcursor);
  }
  
  /** Returns ViewCursor */
  private static XTextViewCursor getViewCursor(XComponentContext xContext) throws Exception {
    XDesktop xDesktop = getCurrentDesktop(xContext);
    if(xDesktop == null) return null;
    XComponent xCurrentComponent = xDesktop.getCurrentComponent();
    if(xCurrentComponent == null) return null;
    XModel xModel = UnoRuntime.queryInterface(XModel.class, xCurrentComponent);
    if(xModel == null) return null;
    XController xController = xModel.getCurrentController();
    if(xController == null) return null;
    XTextViewCursorSupplier xViewCursorSupplier =
        UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController);
    if(xViewCursorSupplier == null) return null;
    return xViewCursorSupplier.getViewCursor();
  }

 /** Returns Number of all Paragraphs of Document without footnotes etc.  */
  public int getNumberOfAllTextParagraphs() throws Exception {
    if (xPCursor == null) return 0;
    xPCursor.gotoStart(false);
    int npara = 1;
    while (xPCursor.gotoNextParagraph(false)) npara++;
    return npara;
  }

  /** Returns all Paragraphs of Document without footnotes etc.  */
  public List<String> getAllTextParagraphs() throws Exception {
    List<String> allParas = new ArrayList<>();
    if (xPCursor == null) return allParas;
    xPCursor.gotoStart(false);
    xPCursor.gotoStartOfParagraph(false);
    xPCursor.gotoEndOfParagraph(true);
    allParas.add(xPCursor.getString());
    while (xPCursor.gotoNextParagraph(false)) {
      xPCursor.gotoStartOfParagraph(false);
      xPCursor.gotoEndOfParagraph(true);
      allParas.add(xPCursor.getString());
    }
    return allParas;
  }

  /** Returns Paragraph number under ViewCursor */
  public int getViewCursorParagraph() throws Exception {
    if(xVCursor == null) return -4;
    XText xDocumentText = xVCursor.getText();
    if(xDocumentText == null) return -3;
    XTextCursor xModelCursor = xDocumentText.createTextCursorByRange(xVCursor.getStart());
    if(xModelCursor == null) return -2;
    XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(
        XParagraphCursor.class, xModelCursor);
    if(xParagraphCursor == null) return -1;
    int pos = 0;
    while (xParagraphCursor.gotoPreviousParagraph(false)) pos++;
    return pos;
  }

  
}
  
