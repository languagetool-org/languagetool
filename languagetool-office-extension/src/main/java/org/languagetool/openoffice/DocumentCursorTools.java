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

import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
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
 * on the basis of the LO/OO text and view cursor
 * @since 4.0
 * @author Fred Kruse
 */
class DocumentCursorTools {
  
  private final XParagraphCursor xPCursor;
  private final XTextViewCursor xVCursor;
  
  DocumentCursorTools(XComponentContext xContext) {
    xPCursor = getParagraphCursor(xContext);
    xVCursor = getViewCursor(xContext);
  }

  /** 
   * Returns the text cursor (if any)
   * Returns null if it fails
   */
  @Nullable
  private XTextCursor getCursor(XComponentContext xContext) {
    try {
      XTextDocument curDoc = OfficeTools.getCurrentDocument(xContext);
      if (curDoc == null) {
        return null;
      }
      XText xText = curDoc.getText();
      if (xText == null) {
        return null;
      }
      else return xText.createTextCursor();
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }

  /** 
   * Returns ParagraphCursor from TextCursor 
   * Returns null if it fails
   */
  @Nullable
  private XParagraphCursor getParagraphCursor(XComponentContext xContext) {
    try {
      XTextCursor xCursor = getCursor(xContext);
      if (xCursor == null) {
        return null;
      }
      return UnoRuntime.queryInterface(XParagraphCursor.class, xCursor);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /** 
   * Returns ParagraphCursor from TextCursor 
   * Returns null if it fails
   */
  @Nullable
  public XParagraphCursor getParagraphCursor() {
    return xPCursor;
  }
  
  /** 
   * Returns ViewCursor 
   * Returns null if it fails
   */
  @Nullable
  private XTextViewCursor getViewCursor(XComponentContext xContext) {
    try {
      XComponent xCurrentComponent = OfficeTools.getCurrentComponent(xContext);
      if (xCurrentComponent == null) {
        return null;
      }
      XModel xModel = UnoRuntime.queryInterface(XModel.class, xCurrentComponent);
      if (xModel == null) {
        return null;
      }
      XController xController = xModel.getCurrentController();
      if (xController == null) {
        return null;
      }
      XTextViewCursorSupplier xViewCursorSupplier =
          UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController);
      if (xViewCursorSupplier == null) {
        return null;
      }
      return xViewCursorSupplier.getViewCursor();
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /** 
   * Returns a Paragraph cursor from ViewCursor 
   * Returns null if method fails
   */
  XParagraphCursor getParagraphCursorFromViewCursor() {
    try {
      if (xVCursor == null) {
        return null;
      }
      XText xDocumentText = xVCursor.getText();
      if (xDocumentText == null) {
        return null;
      }
      XTextCursor xModelCursor = xDocumentText.createTextCursorByRange(xVCursor.getStart());
      if (xModelCursor == null) {
        return null;
      }
      return UnoRuntime.queryInterface(XParagraphCursor.class, xModelCursor);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;             // Return null as method failed
    }
  }
  
  /** 
   * Returns Number of all Paragraphs of Document without footnotes etc.  
   * Returns 0 if it fails
   */
  int getNumberOfAllTextParagraphs() {
    try {
      if (xPCursor == null) {
        return 0;
      }
      xPCursor.gotoStart(false);
      int nPara = 1;
      while (xPCursor.gotoNextParagraph(false)) nPara++;
      return nPara;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return 0;              // Return 0 as method failed
    }
  }

  /** 
   * Returns all Paragraphs of Document without footnotes etc.  
   * Returns null if it fails
   */
  @Nullable
  List<String> getAllTextParagraphs() {
    try {
      List<String> allParas = new ArrayList<>();
      if (xPCursor == null) {
        return null;
      }
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
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }

  /** 
   * Returns Paragraph number under ViewCursor 
   * Returns a negative value if it fails
   */
  int getViewCursorParagraph() {
    try {
      XParagraphCursor xParagraphCursor = getParagraphCursorFromViewCursor();
      if (xParagraphCursor == null) {
        return -1;
      }
      int pos = 0;
      while (xParagraphCursor.gotoPreviousParagraph(false)) pos++;
      return pos;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return -5;             // Return negative value as method failed
    }
  }
  
  /** 
   * Returns character number in paragraph
   * Returns a negative value if it fails
   */
  int getViewCursorCharacter() {
    try {
      XParagraphCursor xParagraphCursor = getParagraphCursorFromViewCursor();
      if (xParagraphCursor == null) {
        return -1;
      }
      xParagraphCursor.gotoStartOfParagraph(true);
      return xParagraphCursor.getString().length();
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return -2;             // Return negative value as method failed
    }
  }
  
}
  
