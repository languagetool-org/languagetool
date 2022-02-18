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
import org.languagetool.openoffice.DocumentCache.TextParagraph;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.lang.XComponent;
import com.sun.star.style.XStyleFamiliesSupplier;
import com.sun.star.text.XEndnotesSupplier;
import com.sun.star.text.XFootnote;
import com.sun.star.text.XFootnotesSupplier;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextTablesSupplier;
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
  
  private XDesktop xDesktop;

  ViewCursorTools(XComponentContext xContext) {
    xDesktop = OfficeTools.getDesktop(xContext);
  }

  /**
   * document is disposed: set all class variables to null
   */
  public void setDisposed() {
    xDesktop = null;
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
   * Returns text cursor from start of ViewCursor 
   * Returns null if method fails
   */
  XTextCursor getTextCursorBeginn() {
    try {
      XTextViewCursor xVCursor = getViewCursor();
      if (xVCursor == null) {
        return null;
      }
      XText xDocumentText = xVCursor.getText();
      if (xDocumentText == null) {
        return null;
      }
      return xDocumentText.createTextCursorByRange(xVCursor.getStart());
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;             // Return null as method failed
    }
  }
  
  /** 
   * Returns text cursor from end of ViewCursor 
   * Returns null if method fails
   */
  XTextCursor getTextCursorEnd() {
    try {
      XTextViewCursor xVCursor = getViewCursor();
      if (xVCursor == null) {
        return null;
      }
      XText xDocumentText = xVCursor.getText();
      if (xDocumentText == null) {
        return null;
      }
      return xDocumentText.createTextCursorByRange(xVCursor.getEnd());
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;             // Return null as method failed
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
  String getViewCursorParagraphText() {
    try {
      XParagraphCursor xParagraphCursor = getParagraphCursorFromViewCursor();
      if (xParagraphCursor == null) {
        return null;
      }
      xParagraphCursor.gotoStartOfParagraph(false);
      xParagraphCursor.gotoEndOfParagraph(true);
      return xParagraphCursor.getString();
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;                          // Return null value as method failed
    }
  }
  
  /** 
   * Replace a part of Paragraph under ViewCursor 
   */
  void setViewCursorParagraphText(int nStart, int nLength, String replace) {
    try {
      XParagraphCursor xParagraphCursor = getParagraphCursorFromViewCursor();
      if (xParagraphCursor == null) {
        return;
      }
      xParagraphCursor.gotoStartOfParagraph(false);
      xParagraphCursor.goRight((short) nStart, false);
      xParagraphCursor.goRight((short) nLength, true);
      xParagraphCursor.setString(replace);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
    }
  }
  
  /** 
   * Returns the text document for the current document
   */
  private XTextDocument getTextDocument() {
    if (xDesktop == null) {
      return null;
    }
    XComponent xComponent = xDesktop.getCurrentComponent();
    if (xComponent == null) {
      return null;
    }
    return UnoRuntime.queryInterface(XTextDocument.class, xComponent);
  }
  
  /**
   * Return a List of all Page Styles
   */
  
  private List<XPropertySet> getPagePropertySets() {
    try {
      List<XPropertySet> propertySets = new ArrayList<XPropertySet>();
      XTextDocument curDoc = getTextDocument();
      if (curDoc == null) {
        return null;
      }
      XStyleFamiliesSupplier xSupplier =  UnoRuntime.queryInterface(XStyleFamiliesSupplier.class, curDoc);
      XNameAccess xNameAccess = xSupplier.getStyleFamilies();
      if (xNameAccess == null) {
        return null;
      }
      XNameContainer pageStyleCon = UnoRuntime.queryInterface(XNameContainer.class, xNameAccess.getByName("PageStyles"));
      if (pageStyleCon == null) {
        return null;
      }
      for (String name : pageStyleCon.getElementNames()) {
        XPropertySet xPageStandardProps = UnoRuntime.queryInterface(XPropertySet.class, pageStyleCon.getByName(name));
        if (xPageStandardProps != null) {
          propertySets.add(UnoRuntime.queryInterface(XPropertySet.class, xPageStandardProps));
        }
      }
      return propertySets;
    } catch (Throwable e) {
      MessageHandler.printException(e);
      return null;
    }
  }
  
  /** 
   * get the paragraph under the view cursor
   */
  public TextParagraph getViewCursorParagraph() {
    try {
      XTextViewCursor vCursor = getViewCursor();
      if (vCursor == null) {
        return new TextParagraph(DocumentCache.CURSOR_TYPE_UNKNOWN, -1);
      }
      XText xViewCursorText = vCursor.getText();
      if (xViewCursorText == null) {
        return new TextParagraph(DocumentCache.CURSOR_TYPE_UNKNOWN, -1);
      }
      XTextDocument curDoc = getTextDocument();
      if (curDoc == null) {
        return new TextParagraph(DocumentCache.CURSOR_TYPE_UNKNOWN, -1);
      }
      //  Test if cursor position is in document text
      XText xText = curDoc.getText();
      if (xViewCursorText.equals(xText)) {
        XTextCursor xTextCursor = xText.createTextCursorByRange(vCursor.getStart());
        if (xTextCursor == null) {
          return new TextParagraph(DocumentCache.CURSOR_TYPE_UNKNOWN, -1);
        }
        XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
        if (xParagraphCursor == null) {
          return new TextParagraph(DocumentCache.CURSOR_TYPE_UNKNOWN, -1);
        }
        int pos = 0;
        while (xParagraphCursor.gotoPreviousParagraph(false)) pos++;
        return new TextParagraph(DocumentCache.CURSOR_TYPE_TEXT, pos);
      }
      //  Test if cursor position is in table
      XTextTablesSupplier xTableSupplier = UnoRuntime.queryInterface(XTextTablesSupplier.class, curDoc);
      XIndexAccess xTables = UnoRuntime.queryInterface(XIndexAccess.class, xTableSupplier.getTextTables());
      if (xTables != null) {
        int nLastPara = 0;
        for (int i = 0; i < xTables.getCount(); i++) {
          XTextTable xTable = UnoRuntime.queryInterface(XTextTable.class, xTables.getByIndex(i));
          for (String cellName : xTable.getCellNames()) {
            XText xTableText = UnoRuntime.queryInterface(XText.class, xTable.getCellByName(cellName) );
            if (xViewCursorText.equals(xTableText)) {
              XTextCursor xTextCursor = xTableText.createTextCursorByRange(vCursor.getStart());
              XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
              int pos = 0;
              while (xParagraphCursor.gotoPreviousParagraph(false)) pos++;
              return new TextParagraph(DocumentCache.CURSOR_TYPE_TABLE, pos + nLastPara);
            } else {
              XTextCursor xTextCursor = xTableText.createTextCursor();
              XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
              xParagraphCursor.gotoStart(false);
              nLastPara++;
              while (xParagraphCursor.gotoNextParagraph(false)){
                nLastPara++;
              }
            }
          }
        }
      }
      //  Test if cursor position is at footnote
      XFootnotesSupplier xFootnoteSupplier = UnoRuntime.queryInterface(XFootnotesSupplier.class, curDoc );
      XIndexAccess xFootnotes = UnoRuntime.queryInterface(XIndexAccess.class, xFootnoteSupplier.getFootnotes());
      if (xFootnotes != null) {
        int nLastPara = 0;
        for (int i = 0; i < xFootnotes.getCount(); i++) {
          XFootnote XFootnote = UnoRuntime.queryInterface(XFootnote.class, xFootnotes.getByIndex(i));
          XText xFootnoteText = UnoRuntime.queryInterface(XText.class, XFootnote);
          if (xViewCursorText.equals(xFootnoteText)) {
            XTextCursor xTextCursor = xFootnoteText.createTextCursorByRange(vCursor.getStart());
            XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
            int pos = 0;
            while (xParagraphCursor.gotoPreviousParagraph(false)) pos++;
            return new TextParagraph(DocumentCache.CURSOR_TYPE_FOOTNOTE, pos + nLastPara);
          } else {
            XTextCursor xTextCursor = xFootnoteText.createTextCursor();
            XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
            xParagraphCursor.gotoStart(false);
            nLastPara++;
            while (xParagraphCursor.gotoNextParagraph(false)){
              nLastPara++;
            }
          }
        }
      }
      //  Test if cursor position is at endnote
      XEndnotesSupplier xEndnotesSupplier = UnoRuntime.queryInterface(XEndnotesSupplier.class, curDoc );
      XIndexAccess xEndnotes = UnoRuntime.queryInterface(XIndexAccess.class, xEndnotesSupplier.getEndnotes());
      if (xEndnotes != null) {
        int nLastPara = 0;
        for (int i = 0; i < xEndnotes.getCount(); i++) {
          XFootnote xEndnote = UnoRuntime.queryInterface(XFootnote.class, xEndnotes.getByIndex(i));
          XText xEndnoteText = UnoRuntime.queryInterface(XText.class, xEndnote);
          if (xViewCursorText.equals(xEndnoteText)) {
            XTextCursor xTextCursor = xEndnoteText.createTextCursorByRange(vCursor.getStart());
            XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
            int pos = 0;
            while (xParagraphCursor.gotoPreviousParagraph(false)) pos++;
            return new TextParagraph(DocumentCache.CURSOR_TYPE_ENDNOTE, pos + nLastPara);
          } else {
            XTextCursor xTextCursor = xEndnoteText.createTextCursor();
            XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
            xParagraphCursor.gotoStart(false);
            nLastPara++;
            while (xParagraphCursor.gotoNextParagraph(false)){
              nLastPara++;
            }
          }
        }
      }
      //  Test if cursor position is at Header/Footer
      List<XPropertySet> xPagePropertySets = getPagePropertySets();
      int nLastPara = 0;
      for (XPropertySet xPagePropertySet : xPagePropertySets) {
        if (xPagePropertySet != null) {
          boolean firstIsShared = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("FirstIsShared"));
          boolean headerIsOn = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("HeaderIsOn"));
          boolean headerIsShared = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("HeaderIsShared"));
          boolean footerIsOn = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("FooterIsOn"));
          boolean footerIsShared = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("FooterIsShared"));
          for (int i = 0; i < DocumentCursorTools.HeaderFooterTypes.length; i++) {
            if ((headerIsOn && ((i == 0 && headerIsShared) 
                || ((i == 1 || i == 2) && !headerIsShared)
                || (i == 3 && !firstIsShared)))
                || (footerIsOn && ((i == 4 && footerIsShared) 
                    || ((i == 5 || i == 6) && !footerIsShared)
                    || (i == 7 && !firstIsShared)))) {
              XText xHeaderText = UnoRuntime.queryInterface(XText.class, xPagePropertySet.getPropertyValue(DocumentCursorTools.HeaderFooterTypes[i]));
              if (xHeaderText != null && !xHeaderText.getString().isEmpty()) {
                if (xViewCursorText.equals(xHeaderText)) {
                  XTextCursor xTextCursor = xHeaderText.createTextCursorByRange(vCursor.getStart());
                  XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
                  int pos = 0;
                  while (xParagraphCursor.gotoPreviousParagraph(false)) pos++;
                  return new TextParagraph(DocumentCache.CURSOR_TYPE_HEADER_FOOTER, pos + nLastPara);
                } else {
                  XTextCursor xTextCursor = xHeaderText.createTextCursor();
                  XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
                  xParagraphCursor.gotoStart(false);
                  nLastPara++;
                  while (xParagraphCursor.gotoNextParagraph(false)){
                    nLastPara++;
                  }
                }
              }
            }
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
    }
    return new TextParagraph(DocumentCache.CURSOR_TYPE_UNKNOWN, -1);
  }
  
  /**
   * Get paragraph cursor under view cursor
   */
  public XParagraphCursor getParagraphCursorUnderViewCursor() {
    try {
      XTextViewCursor vCursor = getViewCursor();
      if (vCursor == null) {
        return null;
      }
      XText xViewCursorText = vCursor.getText();
      if (xViewCursorText == null) {
        return null;
      }
      XTextDocument curDoc = getTextDocument();
      if (curDoc == null) {
        return null;
      }
      //  Test if cursor position is in document text
      XText xText = curDoc.getText();
      if (xViewCursorText.equals(xText)) {
        XTextCursor xTextCursor = xText.createTextCursorByRange(vCursor.getStart());
        if (xTextCursor == null) {
          return null;
        }
        return UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
      }
      //  Test if cursor position is in table
      XTextTablesSupplier xTableSupplier = UnoRuntime.queryInterface(XTextTablesSupplier.class, curDoc);
      XIndexAccess xTables = UnoRuntime.queryInterface(XIndexAccess.class, xTableSupplier.getTextTables());
      if (xTables != null) {
        for (int i = 0; i < xTables.getCount(); i++) {
          XTextTable xTable = UnoRuntime.queryInterface(XTextTable.class, xTables.getByIndex(i));
          for (String cellName : xTable.getCellNames()) {
            XText xTableText = UnoRuntime.queryInterface(XText.class, xTable.getCellByName(cellName) );
            if (xViewCursorText.equals(xTableText)) {
              XTextCursor xTextCursor = xTableText.createTextCursorByRange(vCursor.getStart());
              return UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
            }
          }
        }
      }
      //  Test if cursor position is at footnote
      XFootnotesSupplier xFootnoteSupplier = UnoRuntime.queryInterface(XFootnotesSupplier.class, curDoc );
      XIndexAccess xFootnotes = UnoRuntime.queryInterface(XIndexAccess.class, xFootnoteSupplier.getFootnotes());
      if (xFootnotes != null) {
        for (int i = 0; i < xFootnotes.getCount(); i++) {
          XFootnote XFootnote = UnoRuntime.queryInterface(XFootnote.class, xFootnotes.getByIndex(i));
          XText xFootnoteText = UnoRuntime.queryInterface(XText.class, XFootnote);
          if (xViewCursorText.equals(xFootnoteText)) {
            XTextCursor xTextCursor = xFootnoteText.createTextCursorByRange(vCursor.getStart());
            return UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
          }
        }
      }
      //  Test if cursor position is at endnote
      XEndnotesSupplier xEndnotesSupplier = UnoRuntime.queryInterface(XEndnotesSupplier.class, curDoc );
      XIndexAccess xEndnotes = UnoRuntime.queryInterface(XIndexAccess.class, xEndnotesSupplier.getEndnotes());
      if (xEndnotes != null) {
        for (int i = 0; i < xEndnotes.getCount(); i++) {
          XFootnote xEndnote = UnoRuntime.queryInterface(XFootnote.class, xEndnotes.getByIndex(i));
          XText xEndnoteText = UnoRuntime.queryInterface(XText.class, xEndnote);
          if (xViewCursorText.equals(xEndnoteText)) {
            XTextCursor xTextCursor = xEndnoteText.createTextCursorByRange(vCursor.getStart());
            return UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
          }
        }
      }
      //  Test if cursor position is at Header/Footer
      List<XPropertySet> xPagePropertySets = getPagePropertySets();
      for (XPropertySet xPagePropertySet : xPagePropertySets) {
        if (xPagePropertySet != null) {
          boolean firstIsShared = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("FirstIsShared"));
          boolean headerIsOn = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("HeaderIsOn"));
          boolean headerIsShared = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("HeaderIsShared"));
          boolean footerIsOn = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("FooterIsOn"));
          boolean footerIsShared = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("FooterIsShared"));
          for (int i = 0; i < DocumentCursorTools.HeaderFooterTypes.length; i++) {
            if ((headerIsOn && ((i == 0 && headerIsShared) 
                || ((i == 1 || i == 2) && !headerIsShared)
                || (i == 3 && !firstIsShared)))
                || (footerIsOn && ((i == 4 && footerIsShared) 
                    || ((i == 5 || i == 6) && !footerIsShared)
                    || (i == 7 && !firstIsShared)))) {
              XText xHeaderText = UnoRuntime.queryInterface(XText.class, xPagePropertySet.getPropertyValue(DocumentCursorTools.HeaderFooterTypes[i]));
              if (xHeaderText != null && !xHeaderText.getString().isEmpty()) {
                if (xViewCursorText.equals(xHeaderText)) {
                  XTextCursor xTextCursor = xHeaderText.createTextCursorByRange(vCursor.getStart());
                  return UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
                }
              }
            }
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
    }
    return null;
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
      xParagraphCursor.collapseToStart();
      xParagraphCursor.gotoStartOfParagraph(true);
      return xParagraphCursor.getString().length();
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return -2;             // Return negative value as method failed
    }
  }
  
  /**
   * Set the view cursor if the paragraph is inside of text region
   */
  private static int setViewCursorToParaIfFits(int xChar, int numPara, int nLastPara, XText xText, XTextViewCursor vCursor) {
    XTextCursor xTextCursor = xText.createTextCursor();
    XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
    if (xParagraphCursor != null) {
      xParagraphCursor.gotoStart(false);
      while (nLastPara < numPara && xParagraphCursor.gotoNextParagraph(false)){
        nLastPara++;
      }
      if (numPara == nLastPara) {
        xParagraphCursor.gotoStartOfParagraph(false);
        xParagraphCursor.gotoEndOfParagraph(true);
        vCursor.gotoRange(xParagraphCursor.getStart(), false);
        vCursor.goRight((short)xChar, false);
      }
    } else {
      MessageHandler.printToLogFile("ViewCursorTools: setViewCursorToParaIfFits: xParagraphCursor == null");
    }
    return nLastPara;
  }
  
  /**
   * set the view cursor to header or footer paragraph
   */
  public void setViewCursorToHeaderFooter(int xChar, int numPara) {
    try {
      XTextViewCursor vCursor = getViewCursor();
      if (vCursor != null) {
        List<XPropertySet> xPagePropertySets = getPagePropertySets();
        int nLastPara = 0;
        for (XPropertySet xPagePropertySet : xPagePropertySets) {
          if (xPagePropertySet != null) {
            boolean firstIsShared = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("FirstIsShared"));
            boolean headerIsOn = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("HeaderIsOn"));
            boolean headerIsShared = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("HeaderIsShared"));
            boolean footerIsOn = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("FooterIsOn"));
            boolean footerIsShared = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("FooterIsShared"));
            for (int i = 0; i < DocumentCursorTools.HeaderFooterTypes.length; i++) {
              if ((headerIsOn && ((i == 0 && headerIsShared) 
                  || ((i == 1 || i == 2) && !headerIsShared)
                  || (i == 3 && !firstIsShared)))
                  || (footerIsOn && ((i == 4 && footerIsShared) 
                      || ((i == 5 || i == 6) && !footerIsShared)
                      || (i == 7 && !firstIsShared)))) {
                XText xHeaderText = UnoRuntime.queryInterface(XText.class, xPagePropertySet.getPropertyValue(DocumentCursorTools.HeaderFooterTypes[i]));
                if (xHeaderText != null && !xHeaderText.getString().isEmpty()) {
                  nLastPara = setViewCursorToParaIfFits(xChar, numPara, nLastPara, xHeaderText, vCursor);
                  if (nLastPara >= numPara) {
                    return;
                  }
                  nLastPara++;
                }
              }
            }
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);
    }
  }
  
  /** 
   * Returns the Index Access to all tables of a document
   */
  public XIndexAccess getIndexAccessOfAllTables() {
    try {
      XTextDocument curDoc = getTextDocument();
      if (curDoc == null) {
        return null;
      }
      // Get the TextTablesSupplier interface of the document
      XTextTablesSupplier xTableSupplier = UnoRuntime.queryInterface(XTextTablesSupplier.class, curDoc);
      // Get an XIndexAccess of TextTables
      return UnoRuntime.queryInterface(XIndexAccess.class, xTableSupplier.getTextTables());
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /** 
   * Set the cursor to a paragraph inside a cell of a table
   */
  public void setViewCursorToParagraphOfTable(int xChar, int numPara) {
    try {
      XTextViewCursor vCursor = getViewCursor();
      if (vCursor != null) {
        XIndexAccess xTables = getIndexAccessOfAllTables();
        if (xTables == null) {
          return;
        }
        int nLastPara = 0;
        for (int i = 0; i < xTables.getCount(); i++) {
          XTextTable xTable = UnoRuntime.queryInterface(XTextTable.class, xTables.getByIndex(i));
          for (String cellName : xTable.getCellNames()) {
            XText xTableText = UnoRuntime.queryInterface(XText.class, xTable.getCellByName(cellName) );
            nLastPara = setViewCursorToParaIfFits(xChar, numPara, nLastPara, xTableText, vCursor);
            if (nLastPara == numPara) {
              return;
            }
            nLastPara++;
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
    }
  }
  
  /** 
   * Set the cursor to a paragraph of footnote
   */
  public void setViewCursorToParagraphOfFootnote(int xChar, int numPara) {
    try {
      XTextViewCursor vCursor = getViewCursor();
      if (vCursor != null) {
        XTextDocument curDoc = getTextDocument();
        if (curDoc == null) {
          return;
        }
        XFootnotesSupplier xFootnoteSupplier = UnoRuntime.queryInterface(XFootnotesSupplier.class, curDoc );
        XIndexAccess xFootnotes = UnoRuntime.queryInterface(XIndexAccess.class, xFootnoteSupplier.getFootnotes());
        int nLastPara = 0;
        for (int i = 0; i < xFootnotes.getCount(); i++) {
          XFootnote XFootnote = UnoRuntime.queryInterface(XFootnote.class, xFootnotes.getByIndex(i));
          XText xFootnoteText = UnoRuntime.queryInterface(XText.class, XFootnote);
          nLastPara = setViewCursorToParaIfFits(xChar, numPara, nLastPara, xFootnoteText, vCursor);
          if (nLastPara >= numPara) {
            return;
          }
          nLastPara++;
        }
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
    }
  }
  
  /** 
   * Set the cursor to a paragraph of endnote
   */
  public void setViewCursorToParagraphOfEndnote(int xChar, int numPara) {
    try {
      XTextViewCursor vCursor = getViewCursor();
      if (vCursor != null) {
        XTextDocument curDoc = getTextDocument();
        if (curDoc == null) {
          return;
        }
        XEndnotesSupplier xEndnotesSupplier = UnoRuntime.queryInterface(XEndnotesSupplier.class, curDoc );
        XIndexAccess xFootnotes = UnoRuntime.queryInterface(XIndexAccess.class, xEndnotesSupplier.getEndnotes());
        int nLastPara = 0;
        for (int i = 0; i < xFootnotes.getCount(); i++) {
          XFootnote xEndnote = UnoRuntime.queryInterface(XFootnote.class, xFootnotes.getByIndex(i));
          XText xEndnoteText = UnoRuntime.queryInterface(XText.class, xEndnote);
          nLastPara = setViewCursorToParaIfFits(xChar, numPara, nLastPara, xEndnoteText, vCursor);
          if (nLastPara >= numPara) {
            return;
          }
          nLastPara++;
        }
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
    }
  }
  
  /**
   * Set the view cursor to paragraph paraNum 
   */
  public void setDocumentTextViewCursor(int xChar, int paraNum)  {
    try {
      XTextViewCursor vCursor = getViewCursor();
      if (vCursor != null) {
        XTextDocument curDoc = getTextDocument();
        XText xText = curDoc.getText();
        XTextCursor xTextCursor = xText.createTextCursor();
        XParagraphCursor pCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
        if (pCursor != null) {
          pCursor.gotoStart(false);
          for (int i = 0; i < paraNum && pCursor.gotoNextParagraph(false); i++) {
          }
          pCursor.gotoStartOfParagraph(false);
          vCursor.gotoRange(pCursor.getStart(), false);
          vCursor.goRight((short)xChar, false);
        }
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);
    }
  }
  
  /**
   * Set view cursor to paragraph paraNum 
   */
  public void setTextViewCursor(int xChar, TextParagraph yPara)  {
    try {
      if (yPara.type == DocumentCache.CURSOR_TYPE_TEXT) {
        setDocumentTextViewCursor(xChar, yPara.number);
      } else if (yPara.type == DocumentCache.CURSOR_TYPE_TABLE) {
        setViewCursorToParagraphOfTable(xChar, yPara.number);
      } else if (yPara.type == DocumentCache.CURSOR_TYPE_FOOTNOTE) {
        setViewCursorToParagraphOfFootnote(xChar, yPara.number);
      } else if (yPara.type == DocumentCache.CURSOR_TYPE_ENDNOTE) {
        setViewCursorToParagraphOfEndnote(xChar, yPara.number);
      } else if (yPara.type == DocumentCache.CURSOR_TYPE_HEADER_FOOTER) {
        setViewCursorToHeaderFooter(xChar, yPara.number);
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);
    }
  }
  
  


}
