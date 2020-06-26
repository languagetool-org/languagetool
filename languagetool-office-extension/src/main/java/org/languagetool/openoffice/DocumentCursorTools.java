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

import com.sun.star.beans.Property;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.XComponent;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.uno.UnoRuntime;

/**
 * Information about Paragraphs of LibreOffice/OpenOffice documents
 * on the basis of the LO/OO text cursor
 * @since 4.0
 * @author Fred Kruse
 */
class DocumentCursorTools {
  
  private final XParagraphCursor xPCursor;
  private final List<Integer> headerNumbers = new ArrayList<Integer>();
  
  DocumentCursorTools(XComponent xComponent) {
    xPCursor = getParagraphCursor(xComponent);
  }

  /** 
   * Returns the text cursor (if any)
   * Returns null if it fails
   */
  @Nullable
  private XTextCursor getCursor(XComponent xComponent) {
    try {
      XTextDocument curDoc = UnoRuntime.queryInterface(XTextDocument.class, xComponent);
      if (curDoc == null) {
        return null;
      }
      XText xText = curDoc.getText();
      if (xText == null) {
        return null;
      }
      else {
        XTextRange xStart = xText.getStart();
        try {
          return xText.createTextCursorByRange(xStart);
        } catch (Throwable t) {
          return null;           // Return null without message - is needed for documents without main text (e.g. only a table)
        }
      }
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
  private XParagraphCursor getParagraphCursor(XComponent xComponent) {
    try {
      XTextCursor xCursor = getCursor(xComponent);
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
   * @return 
   */
  @Nullable
  public XParagraphCursor getParagraphCursor() {
    return xPCursor;
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
      headerNumbers.clear();
      if (xPCursor == null) {
        return null;
      }
      int paraNum = 0;
      xPCursor.gotoStart(false);
      xPCursor.gotoStartOfParagraph(false);
      xPCursor.gotoEndOfParagraph(true);
      allParas.add(xPCursor.getString());
      if(isHeadingOrTitle()) {
        headerNumbers.add(paraNum);
      }
      while (xPCursor.gotoNextParagraph(false)) {
        xPCursor.gotoStartOfParagraph(false);
        xPCursor.gotoEndOfParagraph(true);
        allParas.add(xPCursor.getString());
        paraNum++;
        if(isHeadingOrTitle()) {
          headerNumbers.add(paraNum);
        }
      }
      return allParas;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }

  /**
   * Paragraph is Header or Title
   */
  private boolean isHeadingOrTitle() {
    String paraStyleName;
    try {
      XPropertySet xParagraphPropertySet = UnoRuntime.queryInterface(XPropertySet.class, xPCursor.getStart());
      paraStyleName = (String) xParagraphPropertySet.getPropertyValue("ParaStyleName");
    } catch (Throwable e) {
      MessageHandler.printException(e);
      return false;
    }
    return (paraStyleName.startsWith("Heading") || paraStyleName.equals("Title") || paraStyleName.equals("Subtitle"));
  }
  
  /**
   * Returns List of Paragraph numbers which are Headers or Title
   */
  List<Integer> getParagraphHeadings() {
    return headerNumbers;
  }
  
  void printProperties() {
    if (xPCursor == null) {
      MessageHandler.printToLogFile("Properties: ParagraphCursor == null");
      return;
    }
    XPropertySet xParagraphPropertySet = UnoRuntime.queryInterface(XPropertySet.class, xPCursor.getStart());
    Property[] properties = xParagraphPropertySet.getPropertySetInfo().getProperties();
    for(Property property : properties) {
      MessageHandler.printToLogFile("Properties: Name: " + property.Name + ", Type: " + property.Type);
    }
    try {
      MessageHandler.printToLogFile("!!! Properties: ParaStyleName: " + xParagraphPropertySet.getPropertyValue("ParaStyleName"));
    } catch (Throwable e) {
      MessageHandler.printException(e);
    }
  }
  
}
  
