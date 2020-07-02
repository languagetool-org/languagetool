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

import org.jetbrains.annotations.Nullable;

import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Information about Paragraphs of LibreOffice/OpenOffice documents
 * on the basis of the LO/OO view cursor
 * @since 4.9
 * @author Fred Kruse
 */
public class ViewCursorTools {
  
  private final XDesktop xDesktop;

  ViewCursorTools(XComponentContext xContext) {
    xDesktop = OfficeTools.getDesktop(xContext);
  }

  /** 
   * Returns ViewCursor 
   * Returns null if it fails
   */
  @Nullable
  public XTextViewCursor getViewCursor() {
    try {
      XComponent xCurrentComponent = xDesktop.getCurrentComponent();
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
      XTextViewCursor xVCursor = getViewCursor();
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
