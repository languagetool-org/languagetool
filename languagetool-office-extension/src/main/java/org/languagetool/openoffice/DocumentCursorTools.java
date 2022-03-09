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

import com.sun.star.beans.Property;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.lang.XComponent;
import com.sun.star.style.XStyleFamiliesSupplier;
import com.sun.star.text.XFootnote;
import com.sun.star.text.XFootnotesSupplier;
import com.sun.star.text.XEndnotesSupplier;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextTablesSupplier;
import com.sun.star.uno.UnoRuntime;

/**
 * Information about Paragraphs of LibreOffice/OpenOffice documents
 * on the basis of the LO/OO text cursor
 * @since 4.0
 * @author Fred Kruse
 */
class DocumentCursorTools {
  
  final static String HeaderFooterTypes[] = { "HeaderText", 
      "HeaderTextRight",
      "HeaderTextLeft",
      "HeaderTextFirst", 
      "FooterText", 
      "FooterTextRight", 
      "FooterTextLeft",
      "FooterTextFirst" 
  };

  private XParagraphCursor xPCursor;
  private XTextCursor xTextCursor;
  private XTextDocument curDoc;
  
  DocumentCursorTools(XComponent xComponent) {
    curDoc = UnoRuntime.queryInterface(XTextDocument.class, xComponent);
    xTextCursor = getCursor(xComponent);
    xPCursor = getParagraphCursor(xComponent);
  }
  
  /**
   * document is disposed: set all class variables to null
   */
  public void setDisposed() {
    xPCursor = null;
    xTextCursor = null;
    curDoc = null;
  }

  /** 
   * Returns the text cursor (if any)
   * Returns null if it fails
   */
  @Nullable
  private XTextCursor getCursor(XComponent xComponent) {
    try {
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
      if (xTextCursor == null) {
        return null;
      }
      return UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /** 
   * Returns the TextCursor of the Document
   * Returns null if it fails
   */
  @Nullable
  public XTextCursor getTextCursor() {
    return xTextCursor;
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
  DocumentText getAllTextParagraphs() {
    try {
      List<String> allParas = new ArrayList<>();
      List<Integer> headingNumbers = new ArrayList<Integer>();
      if (xPCursor == null) {
        return null;
      }
      int paraNum = 0;
      xPCursor.gotoStart(false);
      xPCursor.gotoStartOfParagraph(false);
      xPCursor.gotoEndOfParagraph(true);
      allParas.add(xPCursor.getString());
      if (isHeadingOrTitle()) {
        headingNumbers.add(paraNum);
      }
      while (xPCursor.gotoNextParagraph(false)) {
        xPCursor.gotoStartOfParagraph(false);
        xPCursor.gotoEndOfParagraph(true);
        allParas.add(xPCursor.getString());
        paraNum++;
        if (isHeadingOrTitle()) {
          headingNumbers.add(paraNum);
        }
      }
      return new DocumentText(allParas, headingNumbers);
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
    return (paraStyleName.startsWith("Heading") || paraStyleName.startsWith("Contents") || paraStyleName.equals("Title") || paraStyleName.equals("Subtitle"));
  }
  
  /**
   * Print properties to log file for the actual position of cursor
   */
  void printProperties() {
    if (xPCursor == null) {
      MessageHandler.printToLogFile("DocumentCursorTools: Properties: ParagraphCursor == null");
      return;
    }
    XPropertySet xParagraphPropertySet = UnoRuntime.queryInterface(XPropertySet.class, xPCursor.getStart());
    Property[] properties = xParagraphPropertySet.getPropertySetInfo().getProperties();
    for (Property property : properties) {
      MessageHandler.printToLogFile("DocumentCursorTools: Properties: Name: " + property.Name + ", Type: " + property.Type);
    }
    try {
      MessageHandler.printToLogFile("DocumentCursorTools: Properties: ParaStyleName: " + xParagraphPropertySet.getPropertyValue("ParaStyleName"));
    } catch (Throwable e) {
      MessageHandler.printException(e);
    }
  }
  
  /** 
   * Add all paragraphs of XText to a list of strings
   */
  private static List<String> addAllParagraphsOfText(XText xText, List<String> sText) {
    XTextCursor xTextCursor = xText.createTextCursor();
    XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
    xParagraphCursor.gotoStart(false);
    do {
      xParagraphCursor.gotoStartOfParagraph(false);
      xParagraphCursor.gotoEndOfParagraph(true);
      sText.add(xParagraphCursor.getString());
    } while (xParagraphCursor.gotoNextParagraph(false));
    return sText;
  }
  
  /** 
   * Returns the Index Access to all tables of a document
   */
  private XIndexAccess getIndexAccessOfAllTables() {
    try {
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
   * Returns all paragraphs of all cells of all tables of a document
   */
  public DocumentText getTextOfAllTables() {
    try {
      List<String> sText = new ArrayList<String>();
      List<Integer> headingNumbers = new ArrayList<Integer>();
      XIndexAccess xTables = getIndexAccessOfAllTables();
      if (xTables != null) {
        // Get all Tables of Document
        for (int i = 0; i < xTables.getCount(); i++) {
          XTextTable xTable = UnoRuntime.queryInterface(XTextTable.class, xTables.getByIndex(i));
          // Get all Cells of Tables
          for (String cellName : xTable.getCellNames()) {
            XText xTableText = UnoRuntime.queryInterface(XText.class, xTable.getCellByName(cellName) );
            headingNumbers.add(sText.size());
            addAllParagraphsOfText(xTableText, sText);
          }
        }
      }
      return new DocumentText(sText, headingNumbers);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /** 
   * Returns all paragraphs of all footnotes of a document
   */
  public DocumentText getTextOfAllFootnotes() {
    List<String> sText = new ArrayList<String>();
    List<Integer> headingNumbers = new ArrayList<Integer>();
    try {
      if (curDoc != null) {
        // Get the XFootnotesSupplier interface of the document
        XFootnotesSupplier xFootnoteSupplier = UnoRuntime.queryInterface(XFootnotesSupplier.class, curDoc );
        // Get an XIndexAccess of Footnotes
        XIndexAccess xFootnotes = UnoRuntime.queryInterface(XIndexAccess.class, xFootnoteSupplier.getFootnotes());
        for (int i = 0; i < xFootnotes.getCount(); i++) {
          XFootnote xFootnote = UnoRuntime.queryInterface(XFootnote.class, xFootnotes.getByIndex(i));
          XText xFootnoteText = UnoRuntime.queryInterface(XText.class, xFootnote);
          headingNumbers.add(sText.size());
          addAllParagraphsOfText(xFootnoteText, sText);
        }
      }
      return new DocumentText(sText, headingNumbers);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /** 
   * Returns all paragraphs of all endnotes of a document
   */
  public DocumentText getTextOfAllEndnotes() {
    List<String> sText = new ArrayList<String>();
    List<Integer> headingNumbers = new ArrayList<Integer>();
    try {
      if (curDoc != null) {
        // Get the XEndnotesSupplier interface of the document
        XEndnotesSupplier xEndnoteSupplier = UnoRuntime.queryInterface(XEndnotesSupplier.class, curDoc );
        // Get an XIndexAccess of Endnotes
        XIndexAccess xEndnotes = UnoRuntime.queryInterface(XIndexAccess.class, xEndnoteSupplier.getEndnotes());
        for (int i = 0; i < xEndnotes.getCount(); i++) {
          XFootnote xEndnote = UnoRuntime.queryInterface(XFootnote.class, xEndnotes.getByIndex(i));
          XText xFootnoteText = UnoRuntime.queryInterface(XText.class, xEndnote);
          headingNumbers.add(sText.size());
          addAllParagraphsOfText(xFootnoteText, sText);
        }
      }
      return new DocumentText(sText, headingNumbers);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /** 
   * Returns the page property sets of of a document
   */
  private List<XPropertySet> getPagePropertySets() {
    try {
      List<XPropertySet> propertySets = new ArrayList<XPropertySet>();
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
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /** 
   * Returns all paragraphs of headers and footers of a document
   */
  public DocumentText getTextOfAllHeadersAndFooters() {
    try {
      List<String> sText = new ArrayList<String>();
      List<Integer> headingNumbers = new ArrayList<Integer>();
      List<XPropertySet> xPagePropertySets = getPagePropertySets();
      if (xPagePropertySets != null) {
        for (XPropertySet xPagePropertySet : xPagePropertySets) {
          if (xPagePropertySet != null) {
            boolean headerIsOn = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("HeaderIsOn"));
            boolean firstIsShared = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("FirstIsShared"));
            boolean headerIsShared = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("HeaderIsShared"));
            boolean footerIsOn = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("FooterIsOn"));
            boolean footerIsShared = OfficeTools.getBooleanValue(xPagePropertySet.getPropertyValue("FooterIsShared"));
            for (int i = 0; i < HeaderFooterTypes.length; i++) {
              if ((headerIsOn && ((i == 0 && headerIsShared) 
                  || ((i == 1 || i == 2) && !headerIsShared)
                  || (i == 3 && !firstIsShared)))
                  || (footerIsOn && ((i == 4 && footerIsShared) 
                      || ((i == 5 || i == 6) && !footerIsShared)
                      || (i == 7 && !firstIsShared)))) {
                XText xHeaderText = UnoRuntime.queryInterface(XText.class, xPagePropertySet.getPropertyValue(HeaderFooterTypes[i]));
                if (xHeaderText != null && !xHeaderText.getString().isEmpty()) {
                  if (!headingNumbers.contains(sText.size())) {
                    headingNumbers.add(sText.size());
                  }
                  addAllParagraphsOfText(xHeaderText, sText);
                }
              }
            }
          }
        }
      }
      return new DocumentText(sText, headingNumbers);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }

  /** 
   * get the paragraph cursor under the view cursor
   */
  public XParagraphCursor getParagraphCursor(TextParagraph textPara) {
    try {
      int type = textPara.type;
      int number = textPara.number;
      int nPara = 0;
      if (type == DocumentCache.CURSOR_TYPE_UNKNOWN) {
        return null;
      } else if (type == DocumentCache.CURSOR_TYPE_TEXT) {
        if (xPCursor == null) {
          return null;
        }
        xPCursor.gotoStart(false);
        while (nPara < number && xPCursor.gotoNextParagraph(false)) nPara++;
        return xPCursor;
      } else if (type == DocumentCache.CURSOR_TYPE_TABLE) {
        XTextTablesSupplier xTableSupplier = UnoRuntime.queryInterface(XTextTablesSupplier.class, curDoc);
        XIndexAccess xTables = UnoRuntime.queryInterface(XIndexAccess.class, xTableSupplier.getTextTables());
        if (xTables != null) {
          for (int i = 0; i < xTables.getCount(); i++) {
            XTextTable xTable = UnoRuntime.queryInterface(XTextTable.class, xTables.getByIndex(i));
            for (String cellName : xTable.getCellNames()) {
              XText xTableText = UnoRuntime.queryInterface(XText.class, xTable.getCellByName(cellName) );
              XTextCursor xTextCursor = xTableText.createTextCursor();
              XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
              xParagraphCursor.gotoStart(false);
              while (nPara < number && xParagraphCursor.gotoNextParagraph(false)){
                nPara++;
              }
              if (nPara == number) {
                return xParagraphCursor;
              }
              nPara++;
            }
          }
        }
      } else if (type == DocumentCache.CURSOR_TYPE_FOOTNOTE) {
        XFootnotesSupplier xFootnoteSupplier = UnoRuntime.queryInterface(XFootnotesSupplier.class, curDoc );
        XIndexAccess xFootnotes = UnoRuntime.queryInterface(XIndexAccess.class, xFootnoteSupplier.getFootnotes());
        if (xFootnotes != null) {
          for (int i = 0; i < xFootnotes.getCount(); i++) {
            XFootnote XFootnote = UnoRuntime.queryInterface(XFootnote.class, xFootnotes.getByIndex(i));
            XText xFootnoteText = UnoRuntime.queryInterface(XText.class, XFootnote);
            XTextCursor xTextCursor = xFootnoteText.createTextCursor();
            XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
            xParagraphCursor.gotoStart(false);
            while (nPara < number && xParagraphCursor.gotoNextParagraph(false)){
              nPara++;
            }
            if (nPara == number) {
              return xParagraphCursor;
            }
            nPara++;
          }
        }
      } else if (type == DocumentCache.CURSOR_TYPE_ENDNOTE) {
      XEndnotesSupplier xEndnotesSupplier = UnoRuntime.queryInterface(XEndnotesSupplier.class, curDoc );
      XIndexAccess xEndnotes = UnoRuntime.queryInterface(XIndexAccess.class, xEndnotesSupplier.getEndnotes());
      if (xEndnotes != null) {
        for (int i = 0; i < xEndnotes.getCount(); i++) {
          XFootnote xEndnote = UnoRuntime.queryInterface(XFootnote.class, xEndnotes.getByIndex(i));
          XText xEndnoteText = UnoRuntime.queryInterface(XText.class, xEndnote);
          XTextCursor xTextCursor = xEndnoteText.createTextCursor();
          XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
          xParagraphCursor.gotoStart(false);
          while (nPara < number && xParagraphCursor.gotoNextParagraph(false)){
            nPara++;
          }
          if (nPara == number) {
            return xParagraphCursor;
          }
          nPara++;
        }
      }
      } else if (type == DocumentCache.CURSOR_TYPE_ENDNOTE) {
        List<XPropertySet> xPagePropertySets = getPagePropertySets();
        XText lastHeaderText = null;
        for (XPropertySet xPagePropertySet : xPagePropertySets) {
          if (xPagePropertySet != null) {
            for (String headerFooter : DocumentCursorTools.HeaderFooterTypes) {
              XText xHeaderText = UnoRuntime.queryInterface(XText.class, xPagePropertySet.getPropertyValue(headerFooter));
              if (xHeaderText != null && !xHeaderText.getString().isEmpty() && (lastHeaderText == null || !lastHeaderText.equals(xHeaderText))) {
                XTextCursor xTextCursor = xHeaderText.createTextCursor();
                XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
                xParagraphCursor.gotoStart(false);
                while (nPara < number && xParagraphCursor.gotoNextParagraph(false)){
                  nPara++;
                }
                if (nPara == number) {
                  return xParagraphCursor;
                }
                nPara++;
                lastHeaderText = xHeaderText;
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
   * Class to give back the text and the headings under the specified cursor
   */
  public class DocumentText {
    List<String> paragraphs;
    List<Integer> headingNumbers;
    
    DocumentText(List<String> paragraphs, List<Integer> headingNumbers) {
      this.paragraphs = paragraphs;
      this.headingNumbers = headingNumbers;
    }
  }
  
}
  
