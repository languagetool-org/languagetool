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
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Information about Paragraphs of LibreOffice/OpenOffice documents
 * on the basis of the LO/OO ViewCursor
 * @since 4.0
 * @author Fred Kruse
 */
class LOCursor {
  
  private XParagraphCursor xPCursor = null;
  private XTextViewCursor xVCursor = null;
  
  LOCursor(XComponentContext xContext) {
    xPCursor = getParagraphCursor(xContext);
    xVCursor = getViewCursor(xContext);
  }

  /**
   * Returns the current XDesktop
   */
  private static XDesktop getCurrentDesktop(XComponentContext xContext) {
    try {
      if (xContext == null) return null;
      XMultiComponentFactory xMCF = UnoRuntime.queryInterface(XMultiComponentFactory.class,
              xContext.getServiceManager());
      if (xMCF == null) return null;
      Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
      if (desktop == null) return null;
      return UnoRuntime.queryInterface(XDesktop.class, desktop);
    } catch (Exception e) {
      return null;
    }
  }

  /** Returns the current XComponent */
  private static XComponent getCurrentComponent(XComponentContext xContext) {
    try {
      XDesktop xdesktop = getCurrentDesktop(xContext);
      if(xdesktop == null) return null;
      else return xdesktop.getCurrentComponent();
    } catch (Exception e) {
      return null;
    }
  }
    
  /** Returns the current text document (if any) */
  private static XTextDocument getCurrentDocument(XComponentContext xContext) {
    try {
      XComponent curcomp = getCurrentComponent(xContext);
      if (curcomp == null) return null;
      else return UnoRuntime.queryInterface(XTextDocument.class, curcomp);
    } catch (Exception e) {
      return null;
    }
}

  /** Returns the text cursor (if any) */
  private static XTextCursor getCursor(XComponentContext xContext) {
    try {
      XTextDocument curdoc = getCurrentDocument(xContext);
      if (curdoc == null) return null;
      XText xText = curdoc.getText();
      if (xText == null) return null;
      else return xText.createTextCursor();
    } catch (Exception e) {
      return null;
    }
}

  /** Returns ParagraphCursor from TextCursor */
  private static XParagraphCursor getParagraphCursor(XComponentContext xContext) {
    try {
      XTextCursor xcursor = getCursor(xContext);
      if(xcursor == null) return null;
      return UnoRuntime.queryInterface(XParagraphCursor.class, xcursor);
    } catch (Exception e) {
      return null;
    }
}
  
  /** Returns ViewCursor */
  private static XTextViewCursor getViewCursor(XComponentContext xContext) {
    try {
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
    } catch (Exception e) {
      return null;
    }
}

 /** Returns Number of all Paragraphs of Document without footnotes etc.  */
  public int getNumberOfAllTextParagraphs() {
    try {
      if (xPCursor == null) return 0;
      xPCursor.gotoStart(false);
      int npara = 1;
      while (xPCursor.gotoNextParagraph(false)) npara++;
      return npara;
    } catch (Exception e) {
      return 0;
    }
}

  /** Returns all Paragraphs of Document without footnotes etc.  */
  public List<String> getAllTextParagraphs() {
    try {
      List<String> allParas = new ArrayList<>();
      if (xPCursor == null) return null;
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
    } catch (Exception e) {
      return null;
    }
 }

  /** Returns Paragraph number under ViewCursor */
  public int getViewCursorParagraph() {
    try {
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
    } catch (Exception e) {
      return -1;
    }
  }

  
}
  
