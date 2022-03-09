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

import org.languagetool.openoffice.OfficeDrawTools.ParagraphContainer;

import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.sheet.XSpreadsheet;
import com.sun.star.sheet.XSpreadsheetDocument;
import com.sun.star.sheet.XSpreadsheetView;
import com.sun.star.sheet.XSpreadsheets;
import com.sun.star.table.XCell;
import com.sun.star.uno.UnoRuntime;

/**
 * Tools to get information of LibreOffice Calc context
 * @since 5.7
 * @author Fred Kruse
 */
public class OfficeSpreadsheetTools {
  
  private final static int MAX_TABLE_ROWS = 50;
  private final static int MAX_TABLE_COLS = 50;
  
  /** 
   * test if the document is a Spreadsheet Document
   */
  public static boolean isSpreadsheetDocument(XComponent xComponent) {
    XServiceInfo xInfo = UnoRuntime.queryInterface(XServiceInfo.class, xComponent);
    return xInfo.supportsService("com.sun.star.sheet.SpreadsheetDocument");
  }

  /**
   * get the number of last filled Column of a Sheet
   */
  private static int getFilledColumnCount(XSpreadsheet xSheet, int maxRows) {
    for (int nCol = MAX_TABLE_COLS - 1; nCol > 0; nCol--) {
      for (int nRow = 0; nRow < maxRows; nRow++) {
        String sContent = null;
        try {
          sContent = xSheet.getCellByPosition(nCol, nRow).getFormula();
        } catch (IndexOutOfBoundsException e) {
          MessageHandler.printException(e);
        }
        if (sContent != null && !sContent.isEmpty()) {
          return nCol + 1;
        }
      }
    }
    return 1;
  }
  
  /**
   * get the number of last filled Row of a Sheet
   */
  private static int getFilledRowCount(XSpreadsheet xSheet, int maxCols) {
    for (int nRow = MAX_TABLE_ROWS - 1; nRow > 0; nRow--) {
      for (int nCol = 0; nCol < maxCols; nCol++) {
        String sContent = null;
        try {
          sContent = xSheet.getCellByPosition(nCol, nRow).getFormula();
        } catch (IndexOutOfBoundsException e) {
          MessageHandler.printException(e);
        }
        if (sContent != null && !sContent.isEmpty()) {
          return nRow + 1;
        }
      }
    }
    return 1;
  }
  
  /**
   * true if formula is a text
   */
  private static boolean isNotTextFormat(String s) {
    s = s.trim();
    if (!s.isEmpty()) {
      char ch0 = s.charAt(0);
      if (ch0 == '=') {
        return true;
      }
      if ((Character.isDigit(ch0) || (s.length() > 1 && s.charAt(1) == '-' && Character.isDigit(s.charAt(1)))) && !s.contains(" ")) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * returns the text of all cells as paragraphs
   */
  public static ParagraphContainer getAllParagraphs(XComponent xComponent) {
    if (xComponent == null) {
      MessageHandler.printToLogFile("OfficeSpreadsheetTools: OfficeSpreadsheetTools: xComponent == null");
      return null;
    }
    XSpreadsheetDocument xSpreadsheetDocument = UnoRuntime.queryInterface(XSpreadsheetDocument.class, xComponent);
    if (xSpreadsheetDocument == null) {
      MessageHandler.printToLogFile("OfficeSpreadsheetTools: OfficeSpreadsheetTools: xSpreadsheetDocument == null");
      return null;
    }
    XPropertySet xSpreadsheetPropertySet = UnoRuntime.queryInterface(XPropertySet.class, xSpreadsheetDocument);
    try {
      Locale locale = (Locale) xSpreadsheetPropertySet.getPropertyValue("CharLocale");
      XSpreadsheets xSheets = xSpreadsheetDocument.getSheets();
      if (xSheets == null) {
        MessageHandler.printToLogFile("OfficeSpreadsheetTools: OfficeSpreadsheetTools: xSheets == null");
        return null;
      }
      XIndexAccess xSheetsIA = UnoRuntime.queryInterface(XIndexAccess.class, xSheets);
      if (xSheetsIA == null) {
        MessageHandler.printToLogFile("OfficeSpreadsheetTools: OfficeSpreadsheetTools: xSheetsIA == null");
        return null;
      }
      List<String> paragraphs = new ArrayList<String>();
      List<Locale> locales = new ArrayList<Locale>();
      List<Integer> pageBegins = new ArrayList<Integer>();
      int nPara = 0;
      for (int i = 0; i < xSheetsIA.getCount(); i++) {
        XSpreadsheet xSheet = UnoRuntime.queryInterface(XSpreadsheet.class, xSheetsIA.getByIndex(i));
        if (xSheet == null) {
          MessageHandler.printToLogFile("OfficeSpreadsheetTools: OfficeSpreadsheetTools: xSheet == null");
          return null;
        }
        boolean isEmptyText = false;
        int maxRows = getFilledRowCount(xSheet, MAX_TABLE_COLS);
        int maxCols = getFilledColumnCount(xSheet, maxRows);
        for (int nCol = 0; nCol < maxCols; nCol++) {
          pageBegins.add(nPara);
          for (int nRow = 0; nRow < maxRows; nRow++) {
            XCell xCell = xSheet.getCellByPosition(nCol, nRow);
            String text = xCell.getFormula();
            if (!text.isEmpty() && isNotTextFormat(text)) {
              text = "";
            }
            if (text.isEmpty() && !isEmptyText) {
              isEmptyText = true;
              if (!pageBegins.contains(nPara)) {
                pageBegins.add(nPara);
              }
            } else if (!text.isEmpty() && isEmptyText) {
              isEmptyText = false;
              if (!pageBegins.contains(nPara)) {
                pageBegins.add(nPara);
              }
            }
            paragraphs.add(text);
            locales.add(locale);
            nPara++;
          }
        }
      }
      OfficeDrawTools o = new OfficeDrawTools();
      return o.new ParagraphContainer(paragraphs, locales, pageBegins);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
    }
    return null;
  }

  /**
   * set a new text into a cell
   */
  public static void setTextofCell(int nPara, String replace, XComponent xComponent) {
    if (xComponent == null) {
      MessageHandler.printToLogFile("OfficeSpreadsheetTools: setTextofCell: xComponent == null");
      return;
    }
    XSpreadsheetDocument xSpreadsheetDocument = UnoRuntime.queryInterface(XSpreadsheetDocument.class, xComponent);
    if (xSpreadsheetDocument == null) {
      MessageHandler.printToLogFile("OfficeSpreadsheetTools: setTextofCell: xSpreadsheetDocument == null");
      return;
    }
    try {
      XSpreadsheets xSheets = xSpreadsheetDocument.getSheets();
      if (xSheets == null) {
        MessageHandler.printToLogFile("OfficeSpreadsheetTools: setTextofCell: xSheets == null");
        return;
      }
      XIndexAccess xSheetsIA = UnoRuntime.queryInterface(XIndexAccess.class, xSheets);
      if (xSheetsIA == null) {
        MessageHandler.printToLogFile("OfficeSpreadsheetTools: setTextofCell: xSheetsIA == null");
        return;
      }
      int nParaCount = 0;
      for (int i = 0; i < xSheetsIA.getCount(); i++) {
        XSpreadsheet xSheet = UnoRuntime.queryInterface(XSpreadsheet.class, xSheetsIA.getByIndex(i));
        if (xSheet == null) {
          MessageHandler.printToLogFile("OfficeSpreadsheetTools: setTextofCell: xSheet == null");
          return;
        }
        int maxRows = getFilledRowCount(xSheet, MAX_TABLE_COLS);
        int maxCols = getFilledColumnCount(xSheet, maxRows);
        for (int nCol = 0; nCol < maxCols; nCol++) {
          for (int nRow = 0; nRow < maxRows; nRow++) {
            if (nPara == nParaCount) {
              XCell xCell = xSheet.getCellByPosition(nCol, nRow);
              xCell.setFormula(replace);
              return;
            }
            nParaCount++;
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
    }
  }

  /**
   * set the current visual sheet
   */
  public static void setCurrentSheet(int nPara, XComponent xComponent) {
    try {
      XSpreadsheetDocument xSpreadsheetDocument = UnoRuntime.queryInterface(XSpreadsheetDocument.class, xComponent);
      if (xSpreadsheetDocument == null) {
        MessageHandler.printToLogFile("OfficeSpreadsheetTools: setCurrentSheet: xSpreadsheetDocument == null");
        return;
      }
      XModel xModel = UnoRuntime.queryInterface(XModel.class, xSpreadsheetDocument);
      XController xController = xModel.getCurrentController();
      XSpreadsheetView xSpreadsheetView = UnoRuntime.queryInterface(XSpreadsheetView.class, xController);
      if (xSpreadsheetView == null) {
        MessageHandler.printToLogFile("OfficeSpreadsheetTools: setCurrentSheet: xSpreadsheetView == null");
        return;
      }
      XSpreadsheets xSheets = xSpreadsheetDocument.getSheets();
      if (xSheets == null) {
        MessageHandler.printToLogFile("OfficeSpreadsheetTools: setCurrentSheet: xSheets == null");
        return;
      }
      XIndexAccess xSheetsIA = UnoRuntime.queryInterface(XIndexAccess.class, xSheets);
      if (xSheetsIA == null) {
        MessageHandler.printToLogFile("OfficeSpreadsheetTools: setCurrentSheet: xSheetsIA == null");
        return;
      }
      int nParaCount = 0;
      int nSheets = xSheetsIA.getCount();
      for (int i = 0; i < nSheets; i++) {
        XSpreadsheet xSheet = UnoRuntime.queryInterface(XSpreadsheet.class, xSheetsIA.getByIndex(i));
        if (xSheet == null) {
          MessageHandler.printToLogFile("OfficeSpreadsheetTools: setCurrentSheet: xSheet == null");
          return;
        }
        if (i == nSheets - 1 || nParaCount == nPara) {
          xSpreadsheetView.setActiveSheet(xSheet);
          return;
        }
        int maxRows = getFilledRowCount(xSheet, MAX_TABLE_COLS);
        int maxCols = getFilledColumnCount(xSheet, maxRows);
        for (int nCol = 0; nCol < maxCols; nCol++) {
          for (int nRow = 0; nRow < maxRows; nRow++) {
            if (nParaCount == nPara) {
              xSpreadsheetView.setActiveSheet(xSheet);
              return;
            }
            nParaCount++;
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }

  /**
   * get the first paragraph from current visual sheet
   */
  public static int getParagraphFromCurrentSheet(XComponent xComponent) {
    try {
      XSpreadsheetDocument xSpreadsheetDocument = UnoRuntime.queryInterface(XSpreadsheetDocument.class, xComponent);
      if (xSpreadsheetDocument == null) {
        MessageHandler.printToLogFile("OfficeSpreadsheetTools: getParagraphFromCurrentSheet: xSpreadsheetDocument == null");
        return -1;
      }
      XModel xModel = UnoRuntime.queryInterface(XModel.class, xSpreadsheetDocument);
      XController xController = xModel.getCurrentController();
      XSpreadsheetView xSpreadsheetView = UnoRuntime.queryInterface(XSpreadsheetView.class, xController);
      if (xSpreadsheetView == null) {
        MessageHandler.printToLogFile("OfficeSpreadsheetTools: getParagraphFromCurrentSheet: xSpreadsheetView == null");
        return -1;
      }
      XSpreadsheet xCurrentSheet = xSpreadsheetView.getActiveSheet();
      XPropertySet xSheetPropertySet = UnoRuntime.queryInterface(XPropertySet.class, xCurrentSheet);
      String currentSheetName = (String) xSheetPropertySet.getPropertyValue("AbsoluteName");
      XSpreadsheets xSheets = xSpreadsheetDocument.getSheets();
      if (xSheets == null) {
        MessageHandler.printToLogFile("OfficeSpreadsheetTools: getParagraphFromCurrentSheet: xSheets == null");
        return -1;
      }
      XIndexAccess xSheetsIA = UnoRuntime.queryInterface(XIndexAccess.class, xSheets);
      if (xSheetsIA == null) {
        MessageHandler.printToLogFile("OfficeSpreadsheetTools: getParagraphFromCurrentSheet: xSheetsIA == null");
        return -1;
      }
      int nParaCount = 0;
      int nSheets = xSheetsIA.getCount();
      MessageHandler.printToLogFile("OfficeSpreadsheetTools: getParagraphFromCurrentSheet: nSheets = " + nSheets);
      for (int i = 0; i < nSheets; i++) {
        XSpreadsheet xSheet = UnoRuntime.queryInterface(XSpreadsheet.class, xSheetsIA.getByIndex(i));
        if (xSheet == null) {
          MessageHandler.printToLogFile("OfficeSpreadsheetTools: getParagraphFromCurrentSheet: xSheet == null");
          return -1;
        }
        xSheetPropertySet = UnoRuntime.queryInterface(XPropertySet.class, xSheet);
        String sheetName = (String) xSheetPropertySet.getPropertyValue("AbsoluteName");
        if (currentSheetName.equals(sheetName)) {
          return nParaCount;
        }
        MessageHandler.printToLogFile("OfficeSpreadsheetTools: getParagraphFromCurrentSheet: xCurrentSheet = " + xCurrentSheet + ", xSheet = " + xSheet);
        int maxRows = getFilledRowCount(xSheet, MAX_TABLE_COLS);
        int maxCols = getFilledColumnCount(xSheet, maxRows);
        for (int nCol = 0; nCol < maxCols; nCol++) {
          for (int nRow = 0; nRow < maxRows; nRow++) {
            nParaCount++;
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return -1;
  }

  /**
   * set the Language of the Spreadsheet
   * NOTE: the change of language affects always the whole spreadsheet
   */
  public static void setLanguageOfSpreadsheet(Locale locale, XComponent xComponent) {
    if (xComponent == null) {
      MessageHandler.printToLogFile("OfficeSpreadsheetTools: setLanguageOfSpreadsheet: xComponent == null");
      return;
    }
    XSpreadsheetDocument xSpreadsheetDocument = UnoRuntime.queryInterface(XSpreadsheetDocument.class, xComponent);
    if (xSpreadsheetDocument == null) {
      MessageHandler.printToLogFile("OfficeSpreadsheetTools: setLanguageOfSpreadsheet: xSpreadsheetDocument == null");
      return;
    }
    XPropertySet xSpreadsheetPropertySet = UnoRuntime.queryInterface(XPropertySet.class, xSpreadsheetDocument);
    try {
      xSpreadsheetPropertySet.setPropertyValue("CharLocale", locale);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
    }
  }
  
  /**
   * Get local of the Spreadsheet
   */
  public static Locale getDocumentLocale(XComponent xComponent) {
    if (xComponent == null) {
      MessageHandler.printToLogFile("OfficeSpreadsheetTools: getDocumentLocale: xComponent == null");
      return null;
    }
    XSpreadsheetDocument xSpreadsheetDocument = UnoRuntime.queryInterface(XSpreadsheetDocument.class, xComponent);
    if (xSpreadsheetDocument == null) {
      MessageHandler.printToLogFile("OfficeSpreadsheetTools: getDocumentLocale: xSpreadsheetDocument == null");
      return null;
    }
    XPropertySet xSpreadsheetPropertySet = UnoRuntime.queryInterface(XPropertySet.class, xSpreadsheetDocument);
    try {
      return (Locale) xSpreadsheetPropertySet.getPropertyValue("CharLocale");
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions XWordCursorthrown by UnoRuntime.queryInterface are caught
    }
    return null;
  }


}
