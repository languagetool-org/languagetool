/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

/**
 * Information about LibreOffice/OpenOffice documents
 * needed for full text search
 * @since 4.0
 * 
 * @author Fred Kruse
 */
import java.util.ArrayList;
import java.util.List;

import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.text.TextMarkupType;
import com.sun.star.text.XFlatParagraph;
import com.sun.star.text.XFlatParagraphIterator;
import com.sun.star.text.XFlatParagraphIteratorProvider;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

public class LODocument {

	/** Returns the current XDesktop */
	public static XDesktop getCurrentDesktop(XComponentContext xContext) {
		if(xContext == null) return null;
		XMultiComponentFactory xMCF = (XMultiComponentFactory) UnoRuntime.queryInterface(XMultiComponentFactory.class,
			xContext.getServiceManager());
      Object desktop = null;
		try {
			desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
		} catch (Exception e) {
			return null;
		}
      return (XDesktop) UnoRuntime.queryInterface(com.sun.star.frame.XDesktop.class, desktop);
	}
	
	/** Returns the current XComponent */
  private static XComponent getCurrentComponent(XComponentContext xContext) {
  	XDesktop xdesktop = getCurrentDesktop(xContext);
   	if(xdesktop == null) return null;
   	else return (XComponent) xdesktop.getCurrentComponent();
  }
    
  /** Returns the current frame */
  public static XFrame getCurrentFrame(XComponentContext xContext) {
  	XComponent curcomp = getCurrentComponent(xContext);
   	if (curcomp == null) return null;
   	else {
      XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, curcomp);
      return xModel.getCurrentController().getFrame();
    }
  }

  /** Returns the current text document (if any) */
  public static XTextDocument getCurrentDocument(XComponentContext xContext) {
  	XComponent curcomp = getCurrentComponent(xContext);
   	if (curcomp == null) return null;
   	else return (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, curcomp);
  }

  /** Returns the text cursor (if any) */
  public static XTextCursor getCursor(XComponentContext xContext) {
   	XTextDocument curdoc = getCurrentDocument(xContext);
    if (curdoc == null) return null;
    XText xText = curdoc.getText();
    if (xText == null) return null;
    else return xText.createTextCursor();
  }

  /** Returns ParagraphCursor from TextCursor */
  public static XParagraphCursor getParagraphCursor(XComponentContext xContext) {
    XTextCursor xcursor = getCursor(xContext);
    if(xcursor == null) return null;
    return (XParagraphCursor) UnoRuntime.queryInterface(XParagraphCursor.class, xcursor);
  }
  
  /** Returns Number of all Paragraphs of Document without footnotes etc.  */
  public static int getNumberOfAllTextParagraphs(XComponentContext xContext) {
    XParagraphCursor xpcursor = getParagraphCursor(xContext);
    if (xpcursor == null) return 0;
    xpcursor.gotoStart(false);
    int npara = 1;
    while (xpcursor.gotoNextParagraph(false)) npara++;
    return npara;
  }

  /** Returns ViewCursor */
  public static XTextViewCursor getViewCursor(XComponentContext xContext) {
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

  /** Returns Paragraph number under ViewCursor */
  public static int getViewCursorParagraph(XComponentContext xContext) {
    XTextViewCursor xViewCursor = getViewCursor(xContext);
    if(xViewCursor == null) return -4;
    XText xDocumentText = xViewCursor.getText();
    if(xDocumentText == null) return -3;
    XTextCursor xModelCursor = xDocumentText.createTextCursorByRange(xViewCursor.getStart());
    if(xModelCursor == null) return -2;
    XParagraphCursor xParagraphCursor = (XParagraphCursor)UnoRuntime.queryInterface(
        XParagraphCursor.class, xModelCursor);
    if(xParagraphCursor == null) return -1;
    int pos = 0;
    while (xParagraphCursor.gotoPreviousParagraph(false)) pos++;
    return pos;
  }

  /** Returns Number of Paragraph from FlatParagaph */
  public static int getNumFlatParagraphs(XComponentContext xContext) {
    XComponent xCurrentComponent = getCurrentComponent(xContext);
    if(xCurrentComponent == null) return -3;
    XFlatParagraphIteratorProvider xFlatParaItPro 
        = UnoRuntime.queryInterface(XFlatParagraphIteratorProvider.class, xCurrentComponent);
    if(xFlatParaItPro == null) return -2;
    try {
      XFlatParagraphIterator xFlatParaIter 
          = xFlatParaItPro.getFlatParagraphIterator(TextMarkupType.PROOFREADING, true);
      XFlatParagraph xFlatPara = xFlatParaIter.getLastPara();
      if(xFlatPara == null) return -1;
      int pos = -1;
      while (xFlatPara != null) {
        xFlatPara = xFlatParaIter.getParaBefore(xFlatPara);
        pos++;
      }
      return pos;
    } catch (Throwable t) {
      Main.showError(t);
      return 0;
    }
  }

  /** Returns all Paragraphs of Document */
  public static List<String> getAllParagraphs(XComponentContext xContext) {
    List<String> allParas = new ArrayList<String>();
    XFlatParagraph lastFlatPara = null;
    XComponent xCurrentComponent = getCurrentComponent(xContext);
    if(xCurrentComponent == null) return allParas;
    XFlatParagraphIteratorProvider xFlatParaItPro = UnoRuntime.queryInterface(XFlatParagraphIteratorProvider.class, xCurrentComponent);
    if(xFlatParaItPro == null) return allParas;
    try {
      XFlatParagraphIterator xFlatParaIter = xFlatParaItPro.getFlatParagraphIterator(TextMarkupType.PROOFREADING, true);
      XFlatParagraph xFlatPara = xFlatParaIter.getLastPara();
      if(xFlatPara == null) return allParas;
      while (xFlatPara != null) {
        lastFlatPara = xFlatPara;
        xFlatPara = xFlatParaIter.getParaBefore(xFlatPara);
      }
      xFlatPara = lastFlatPara;
      while (xFlatPara != null) {
        allParas.add(xFlatPara.getText());
        xFlatPara = xFlatParaIter.getParaAfter(xFlatPara);
      }
    } catch (Throwable t) {
      Main.showError(t);
    }
    return allParas;
  }

  /** Returns Number of all Paragraphs of Document / Returns < 0 on Error  */
  public static int getNumberOfAllParagraphs(XComponentContext xContext) {
    XFlatParagraph lastFlatPara = null;
    XComponent xCurrentComponent = getCurrentComponent(xContext);
    if(xCurrentComponent == null) return -3;
    XFlatParagraphIteratorProvider xFlatParaItPro 
        = UnoRuntime.queryInterface(XFlatParagraphIteratorProvider.class, xCurrentComponent);
    if(xFlatParaItPro == null) return -2;
    try {
      XFlatParagraphIterator xFlatParaIter 
          = xFlatParaItPro.getFlatParagraphIterator(TextMarkupType.PROOFREADING, true);
      XFlatParagraph xFlatPara = xFlatParaIter.getLastPara();
      if(xFlatPara == null) return -1;
      lastFlatPara = xFlatPara;
      int num = 0;
      while (xFlatPara != null) {
        xFlatPara = xFlatParaIter.getParaBefore(xFlatPara);
        num++;
      }
      xFlatPara = lastFlatPara;
      num--;
      while (xFlatPara != null) {
        xFlatPara = xFlatParaIter.getParaAfter(xFlatPara);
        num++;
      }
      return num;
    } catch (Throwable t) {
      Main.showError(t);
      return -4;
    }
  }
  
}
  
