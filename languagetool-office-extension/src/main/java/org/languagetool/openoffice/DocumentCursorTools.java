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
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
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
import com.sun.star.text.XTextFramesSupplier;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextSection;
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
  
  public static enum TextType {
    NORMAL,
    HEADING,
    AUTOMATIC
  };
  
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
      } else {
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
   * Returns Number of all Text Paragraphs of Document without footnotes etc.  
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
   * Give back a list of positions of deleted characters 
   * or null if there are no
   */
  private static List<Integer> getDeletedCharacters(XParagraphCursor xPCursor) {
    if (xPCursor == null) {
      MessageHandler.printToLogFile("DocumentCursorTools: Properties: ParagraphCursor == null");
      return null;
    }
    List<Integer> deletePositions = new ArrayList<Integer>();
    int num = 0;
    try {
      XEnumerationAccess xParaEnumAccess = UnoRuntime.queryInterface(XEnumerationAccess.class, xPCursor);
      XEnumeration xParaEnum = xParaEnumAccess.createEnumeration();
      while (xParaEnum.hasMoreElements()) {
        XEnumerationAccess xEnumAccess = UnoRuntime.queryInterface(XEnumerationAccess.class, xParaEnum.nextElement());
        XEnumeration xEnum = xEnumAccess.createEnumeration();
        boolean isDelete = false;
        while (xEnum.hasMoreElements()) {
          XTextRange xPortion = (XTextRange) UnoRuntime.queryInterface(XTextRange.class, xEnum.nextElement());
          XPropertySet xTextPortionPropertySet = UnoRuntime.queryInterface(XPropertySet.class, xPortion);
          String textPortionType = (String) xTextPortionPropertySet.getPropertyValue("TextPortionType");
          if (textPortionType.equals("Redline")) {
            String redlineType = (String) xTextPortionPropertySet.getPropertyValue("RedlineType");
            if (redlineType.equals("Delete")) {
              isDelete = !isDelete;
            }
          } else {
            int portionLen = xPortion.getString().length();
            if (isDelete) {
              for (int i = num; i < num + portionLen; i++) {
                deletePositions.add(i);
              }
            }
            num += portionLen;
          }
        }
      }
    } catch (Throwable e) {
      MessageHandler.printException(e);
    }
    if (deletePositions.isEmpty()) {
      return null;
    }
    return deletePositions;
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
      List<Integer> automaticTextParagraphs = new ArrayList<Integer>();
      List<List<Integer>> deletedCharacters = new ArrayList<List<Integer>>();
      if (xPCursor == null) {
        return null;
      }
      int paraNum = 0;
      xPCursor.gotoStart(false);
      xPCursor.gotoStartOfParagraph(false);
      xPCursor.gotoEndOfParagraph(true);
      allParas.add(xPCursor.getString());
      deletedCharacters.add(getDeletedCharacters(xPCursor));
      TextType textType = getTextType();
      if (textType == TextType.HEADING) {
        headingNumbers.add(paraNum);
      } else if (textType == TextType.AUTOMATIC) {
        headingNumbers.add(paraNum);
        automaticTextParagraphs.add(paraNum);
      } 
      while (xPCursor.gotoNextParagraph(false)) {
        xPCursor.gotoStartOfParagraph(false);
        xPCursor.gotoEndOfParagraph(true);
        allParas.add(xPCursor.getString());
        deletedCharacters.add(getDeletedCharacters(xPCursor));
        paraNum++;
        textType = getTextType();
        if (textType == TextType.HEADING) {
          headingNumbers.add(paraNum);
        } else if (textType == TextType.AUTOMATIC) {
          headingNumbers.add(paraNum);
          automaticTextParagraphs.add(paraNum);
        } 
      }
      return new DocumentText(allParas, headingNumbers, automaticTextParagraphs, deletedCharacters);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }

  /**
   * Paragraph is Header or Title
   */
  private TextType getTextType() {
    String paraStyleName;
    XPropertySet xParagraphPropertySet = null;
    try {
      xParagraphPropertySet = UnoRuntime.queryInterface(XPropertySet.class, xPCursor.getStart());
      paraStyleName = (String) xParagraphPropertySet.getPropertyValue("ParaStyleName");
    } catch (Throwable e) {
      MessageHandler.printException(e);
      return TextType.NORMAL;
    }
    try {
      XTextSection xTextSection = UnoRuntime.queryInterface(XTextSection.class, xParagraphPropertySet.getPropertyValue("TextSection"));
      xParagraphPropertySet = UnoRuntime.queryInterface(XPropertySet.class, xTextSection);
      if((boolean) xParagraphPropertySet.getPropertyValue("IsProtected")) {
        return TextType.AUTOMATIC;
      }
    } catch (Throwable e) {
    }
    if (paraStyleName.startsWith("Heading") || paraStyleName.equals("Title") || paraStyleName.equals("Subtitle")) {
      return TextType.HEADING;
    }
    else if (paraStyleName.startsWith("Contents")) {
      return TextType.AUTOMATIC;
    } else {
      return TextType.NORMAL;
    }
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
  private static List<String> addAllParagraphsOfText(XText xText, List<String> sText, List<List<Integer>> deletedCharacters) {
    XTextCursor xTextCursor = xText.createTextCursor();
    XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
    xParagraphCursor.gotoStart(false);
    do {
      xParagraphCursor.gotoStartOfParagraph(false);
      xParagraphCursor.gotoEndOfParagraph(true);
      sText.add(xParagraphCursor.getString());
      deletedCharacters.add(getDeletedCharacters(xParagraphCursor));
    } while (xParagraphCursor.gotoNextParagraph(false));
    return sText;
  }
  
  /** 
   * Get the number of all paragraphs of XText
   */
  private static int getNumberOfAllParagraphsOfText(XText xText) {
    int num = 0;
    XTextCursor xTextCursor = xText.createTextCursor();
    XParagraphCursor xParagraphCursor = UnoRuntime.queryInterface(XParagraphCursor.class, xTextCursor);
    xParagraphCursor.gotoStart(false);
    do {
      num++;
    } while (xParagraphCursor.gotoNextParagraph(false));
    return num;
  }
  
  /** 
   * Returns all paragraphs of all text frames of a document
   */
  public DocumentText getTextOfAllFrames() {
    try {
      List<String> sText = new ArrayList<String>();
      List<Integer> headingNumbers = new ArrayList<Integer>();
      List<List<Integer>> deletedCharacters = new ArrayList<List<Integer>>();
      XTextFramesSupplier xTextFrameSupplier = UnoRuntime.queryInterface(XTextFramesSupplier.class, curDoc);
      XNameAccess xNamedFrames = xTextFrameSupplier.getTextFrames();
      for (String name : xNamedFrames.getElementNames()) {
        List<String> sTxt = new ArrayList<String>();
        List<List<Integer>> delCharacters = new ArrayList<List<Integer>>();
        Object o = xNamedFrames.getByName(name);
        XText xFrameText = UnoRuntime.queryInterface(XText.class,  o);
        addAllParagraphsOfText(xFrameText, sTxt, delCharacters);
        for (int i = 0; i < headingNumbers.size(); i++) {
          headingNumbers.set(i, headingNumbers.get(i) + sTxt.size());
        }
        headingNumbers.add(0, 0);
        sText.addAll(0, sTxt);
        deletedCharacters.addAll(0, delCharacters);
      }
      return new DocumentText(sText, headingNumbers, new ArrayList<Integer>(), deletedCharacters);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /** 
   * Returns all paragraphs of all text frames of a document
   */
  public int getNumberOfAllFrames() {
    try {
      int num = 0;
      if (curDoc != null) {
        XTextFramesSupplier xTextFrameSupplier = UnoRuntime.queryInterface(XTextFramesSupplier.class, curDoc);
        XNameAccess xNamedFrames = xTextFrameSupplier.getTextFrames();
        for (String name : xNamedFrames.getElementNames()) {
          Object o = xNamedFrames.getByName(name);
          XText xFrameText = UnoRuntime.queryInterface(XText.class,  o);
          num += getNumberOfAllParagraphsOfText(xFrameText);
        }
      }
      return num;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return 0;           // Return 0 as method failed
    }
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
      List<List<Integer>> deletedCharacters = new ArrayList<List<Integer>>();
      if (xTables != null) {
        // Get all Tables of Document
        for (int i = 0; i < xTables.getCount(); i++) {
          XTextTable xTable = UnoRuntime.queryInterface(XTextTable.class, xTables.getByIndex(i));
          // Get all Cells of Tables
          for (String cellName : xTable.getCellNames()) {
            XText xTableText = UnoRuntime.queryInterface(XText.class, xTable.getCellByName(cellName) );
            headingNumbers.add(sText.size());
            addAllParagraphsOfText(xTableText, sText, deletedCharacters);
          }
        }
      }
      return new DocumentText(sText, headingNumbers, new ArrayList<Integer>(), deletedCharacters);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /** 
   * Returns the number of paragraphs of all cells of all tables of a document
   */
  public int getNumberOfAllTables() {
    try {
      int num = 0;
      XIndexAccess xTables = getIndexAccessOfAllTables();
      if (xTables != null) {
        // Get all Tables of Document
        for (int i = 0; i < xTables.getCount(); i++) {
          XTextTable xTable = UnoRuntime.queryInterface(XTextTable.class, xTables.getByIndex(i));
          // Get all Cells of Tables
          for (String cellName : xTable.getCellNames()) {
            XText xTableText = UnoRuntime.queryInterface(XText.class, xTable.getCellByName(cellName) );
            num += getNumberOfAllParagraphsOfText(xTableText);
          }
        }
      }
      return num;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return 0;           // Return 0 as method failed
    }
  }
  
  /** 
   * Returns all paragraphs of all footnotes of a document
   */
  public DocumentText getTextOfAllFootnotes() {
    List<String> sText = new ArrayList<String>();
    List<Integer> headingNumbers = new ArrayList<Integer>();
    List<List<Integer>> deletedCharacters = new ArrayList<List<Integer>>();
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
          addAllParagraphsOfText(xFootnoteText, sText, deletedCharacters);
        }
      }
      return new DocumentText(sText, headingNumbers, new ArrayList<Integer>(), deletedCharacters);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /** 
   * Returns the number of paragraphs of all footnotes of a document
   */
  public int getNumberOfAllFootnotes() {
    try {
      int num = 0;
      if (curDoc != null) {
        // Get the XFootnotesSupplier interface of the document
        XFootnotesSupplier xFootnoteSupplier = UnoRuntime.queryInterface(XFootnotesSupplier.class, curDoc );
        // Get an XIndexAccess of Footnotes
        XIndexAccess xFootnotes = UnoRuntime.queryInterface(XIndexAccess.class, xFootnoteSupplier.getFootnotes());
        for (int i = 0; i < xFootnotes.getCount(); i++) {
          XFootnote xFootnote = UnoRuntime.queryInterface(XFootnote.class, xFootnotes.getByIndex(i));
          XText xFootnoteText = UnoRuntime.queryInterface(XText.class, xFootnote);
          num += getNumberOfAllParagraphsOfText(xFootnoteText);
        }
      }
      return num;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return 0;           // Return 0 as method failed
    }
  }
  
  /** 
   * Returns all paragraphs of all endnotes of a document
   */
  public DocumentText getTextOfAllEndnotes() {
    List<String> sText = new ArrayList<String>();
    List<Integer> headingNumbers = new ArrayList<Integer>();
    List<List<Integer>> deletedCharacters = new ArrayList<List<Integer>>();
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
          addAllParagraphsOfText(xFootnoteText, sText, deletedCharacters);
        }
      }
      return new DocumentText(sText, headingNumbers, new ArrayList<Integer>(), deletedCharacters);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  /** 
   * Returns number of paragraphs of all endnotes of a document
   */
  public int getNumberOfAllEndnotes() {
    try {
      int num = 0;
      if (curDoc != null) {
        // Get the XEndnotesSupplier interface of the document
        XEndnotesSupplier xEndnoteSupplier = UnoRuntime.queryInterface(XEndnotesSupplier.class, curDoc );
        // Get an XIndexAccess of Endnotes
        XIndexAccess xEndnotes = UnoRuntime.queryInterface(XIndexAccess.class, xEndnoteSupplier.getEndnotes());
        for (int i = 0; i < xEndnotes.getCount(); i++) {
          XFootnote xEndnote = UnoRuntime.queryInterface(XFootnote.class, xEndnotes.getByIndex(i));
          XText xFootnoteText = UnoRuntime.queryInterface(XText.class, xEndnote);
          num += getNumberOfAllParagraphsOfText(xFootnoteText);
        }
      }
      return num;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return 0;           // Return 0 as method failed
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
      List<List<Integer>> deletedCharacters = new ArrayList<List<Integer>>();
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
                  addAllParagraphsOfText(xHeaderText, sText, deletedCharacters);
                }
              }
            }
          }
        }
      }
      return new DocumentText(sText, headingNumbers, new ArrayList<Integer>(), deletedCharacters);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }

  /** 
   * Returns the number of paragraphs of headers and footers of a document
   */
  public int getNumberOfAllHeadersAndFooters() {
    try {
      int num = 0;
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
                  num += getNumberOfAllParagraphsOfText(xHeaderText);
                }
              }
            }
          }
        }
      }
      return num;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
      return 0;           // Return 0 as method failed
    }
  }

  /** 
   * get the paragraph cursor
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
   * get positions of deleted characters from a text paragraph
   */
  public List<Integer> getDeletedCharactersOfTextParagraph(TextParagraph textPara) {
    XParagraphCursor xPCursor = getParagraphCursor(textPara);
    if (xPCursor == null) {
      return null;
    }
    xPCursor.gotoStartOfParagraph(false);
    xPCursor.gotoEndOfParagraph(true);
    return getDeletedCharacters(xPCursor);
  }
  
  /**
   * Class to give back the text and the headings under the specified cursor
   */
  public class DocumentText {
    List<String> paragraphs;
    List<Integer> headingNumbers;
    List<Integer> automaticTextParagraphs;
    List<List<Integer>> deletedCharacters;
    
    DocumentText() {
      this.paragraphs = new ArrayList<String>();
      this.headingNumbers = new ArrayList<Integer>();
      this.automaticTextParagraphs = new ArrayList<Integer>();
      this.deletedCharacters = new ArrayList<List<Integer>>();
    }
    
    DocumentText(List<String> paragraphs, List<Integer> headingNumbers, List<Integer> automaticTextParagraphs, List<List<Integer>> deletedCharacters) {
      this.paragraphs = paragraphs;
      this.headingNumbers = headingNumbers;
      this.automaticTextParagraphs = automaticTextParagraphs;
      this.deletedCharacters = deletedCharacters;
    }
  }
  
}
  
