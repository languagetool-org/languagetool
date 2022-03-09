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

import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPages;
import com.sun.star.drawing.XDrawPagesSupplier;
import com.sun.star.drawing.XDrawView;
import com.sun.star.drawing.XMasterPageTarget;
import com.sun.star.drawing.XMasterPagesSupplier;
import com.sun.star.drawing.XShape;
import com.sun.star.drawing.XShapes;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.Locale;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.presentation.XHandoutMasterSupplier;
import com.sun.star.presentation.XPresentationPage;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.uno.UnoRuntime;

/**
 * Some tools to get information of LibreOffice Impress context
 * @since 5.4
 * @author Fred Kruse
 */
public class OfficeDrawTools {

  /** 
   * get the page count for standard pages
   */
  public static int getDrawPageCount(XComponent xComponent) {
    XDrawPagesSupplier xDrawPagesSupplier = UnoRuntime.queryInterface(XDrawPagesSupplier.class, xComponent);
    XDrawPages xDrawPages = xDrawPagesSupplier.getDrawPages();
    return xDrawPages.getCount();
  }

  /** 
   * get draw page by index
   */
  public static XDrawPage getDrawPageByIndex(XComponent xComponent, int nIndex)
      throws com.sun.star.lang.IndexOutOfBoundsException, com.sun.star.lang.WrappedTargetException {
    XDrawPagesSupplier xDrawPagesSupplier = UnoRuntime.queryInterface(XDrawPagesSupplier.class, xComponent);
    XDrawPages xDrawPages = xDrawPagesSupplier.getDrawPages();
    return UnoRuntime.queryInterface(XDrawPage.class, xDrawPages.getByIndex( nIndex ));
  }

  /** 
   * creates and inserts a draw page into the giving position,
   * the method returns the new created page
   */
  public static XDrawPage insertNewDrawPageByIndex(XComponent xComponent, int nIndex) throws Exception {
    XDrawPagesSupplier xDrawPagesSupplier = UnoRuntime.queryInterface(XDrawPagesSupplier.class, xComponent);
    XDrawPages xDrawPages = xDrawPagesSupplier.getDrawPages();
    return xDrawPages.insertNewByIndex( nIndex );
  }

  /** 
   * get size of the given page
   */
  public static Size getPageSize( XDrawPage xDrawPage ) 
      throws com.sun.star.beans.UnknownPropertyException, com.sun.star.lang.WrappedTargetException {
    XPropertySet xPageProperties = UnoRuntime.queryInterface( XPropertySet.class, xDrawPage );
    return new Size(
        ((Integer)xPageProperties.getPropertyValue( "Width" )).intValue(),
        ((Integer)xPageProperties.getPropertyValue( "Height" )).intValue() );
  }

  /** 
   * get the page count for master pages
   */
  public static int getMasterPageCount(XComponent xComponent) {
    XMasterPagesSupplier xMasterPagesSupplier = UnoRuntime.queryInterface(XMasterPagesSupplier.class, xComponent);
    XDrawPages xDrawPages = xMasterPagesSupplier.getMasterPages();
    return xDrawPages.getCount();
  }

  /** 
   * get master page by index
   */
  public static XDrawPage getMasterPageByIndex(XComponent xComponent, int nIndex)
      throws com.sun.star.lang.IndexOutOfBoundsException, com.sun.star.lang.WrappedTargetException {
    XMasterPagesSupplier xMasterPagesSupplier = UnoRuntime.queryInterface(XMasterPagesSupplier.class, xComponent);
    XDrawPages xDrawPages = xMasterPagesSupplier.getMasterPages();
    return UnoRuntime.queryInterface(XDrawPage.class, xDrawPages.getByIndex( nIndex ));
  }

  /** 
   * creates and inserts a new master page into the giving position,
   * the method returns the new created page
   */
  public static XDrawPage insertNewMasterPageByIndex(XComponent xComponent, int nIndex) {
    XMasterPagesSupplier xMasterPagesSupplier = UnoRuntime.queryInterface(XMasterPagesSupplier.class, xComponent);
    XDrawPages xDrawPages = xMasterPagesSupplier.getMasterPages();
    return xDrawPages.insertNewByIndex( nIndex );
  }

  /** 
   * sets given masterpage at the drawpage
   */
  public static void setMasterPage(XDrawPage xDrawPage, XDrawPage xMasterPage) {
    XMasterPageTarget xMasterPageTarget = UnoRuntime.queryInterface(XMasterPageTarget.class, xDrawPage);
    xMasterPageTarget.setMasterPage( xMasterPage );
  }

  /** 
   * test if a Presentation Document is supported.
   * This is important, because only presentation documents
   * have notes and handout pages
   */
  public static boolean isImpressDocument(XComponent xComponent) {
    XServiceInfo xInfo = UnoRuntime.queryInterface(XServiceInfo.class, xComponent);
    return xInfo.supportsService("com.sun.star.presentation.PresentationDocument");
  }

  /** 
   * in impress documents each normal draw page has a corresponding notes page
   */
  public static XDrawPage getNotesPage(XDrawPage xDrawPage) {
    XPresentationPage aPresentationPage = UnoRuntime.queryInterface(XPresentationPage.class, xDrawPage);
    return aPresentationPage.getNotesPage();
  }

  /** 
   * in impress each documents has one handout page
   */
  public static XDrawPage getHandoutMasterPage(XComponent xComponent) {
    XHandoutMasterSupplier aHandoutMasterSupplier = UnoRuntime.queryInterface(XHandoutMasterSupplier.class, xComponent);
    return aHandoutMasterSupplier.getHandoutMasterPage();
  }

  /**
   * get shapes of a page
   */
  public static XShapes getShapes(XDrawPage xPage) {
    return UnoRuntime.queryInterface( XShapes.class, xPage );
  }
  
  /**
   * get all paragraphs from Text of a shape
   */
  private static void getAllParagraphsFromText(List<String> paragraphs, List<Locale> locales, List<Integer> pageBegins, 
      XText xText) throws UnknownPropertyException, WrappedTargetException {
    if (xText != null) {
      XTextCursor xTextCursor = xText.createTextCursor();
      xTextCursor.gotoStart(false);
      String sText = xText.getString();
      int kStart = 0;
      int k;
      for (k = 0; k < sText.length(); k++) {
        if (sText.charAt(k) == OfficeTools.SINGLE_END_OF_PARAGRAPH.charAt(0)) {
          paragraphs.add(sText.substring(kStart, k));
          xTextCursor.goRight((short)(k - kStart), true);
          XPropertySet xParaPropSet = UnoRuntime.queryInterface(XPropertySet.class, xTextCursor);
          locales.add((Locale) xParaPropSet.getPropertyValue("CharLocale"));
          xTextCursor.goRight((short)1, false);
          kStart = k + 1;
        }
      }
      if (k > kStart) {
        paragraphs.add(sText.substring(kStart, k));
        xTextCursor.goRight((short)(k - kStart), true);
        XPropertySet xParaPropSet = UnoRuntime.queryInterface(XPropertySet.class, xTextCursor);
        locales.add((Locale) xParaPropSet.getPropertyValue("CharLocale"));
      }
    }
  }
  
  /**
   * get all paragraphs of a impress document
   */
  public static ParagraphContainer getAllParagraphs(XComponent xComponent) {
    List<String> paragraphs = new ArrayList<>();
    List<Locale> locales = new ArrayList<>();
    List<Integer> pageBegins = new ArrayList<>();
    int nPara = 0;
    try {
      int pageCount = OfficeDrawTools.getDrawPageCount(xComponent);
      for (int i = 0; i < pageCount; i++) {
        XDrawPage xDrawPage = null;
        for (int n = 0; n < 2; n++) {
          if (n == 0) {
            xDrawPage = OfficeDrawTools.getDrawPageByIndex(xComponent, i);
          } else {
            xDrawPage = getNotesPage(xDrawPage);
          }
          pageBegins.add(nPara);
          XShapes xShapes = OfficeDrawTools.getShapes(xDrawPage);
          int nShapes = xShapes.getCount();
          for(int j = 0; j < nShapes; j++) {
            Object oShape = xShapes.getByIndex(j);
            XShape xShape = UnoRuntime.queryInterface(XShape.class, oShape);
            if (xShape != null) {
              XText xText = UnoRuntime.queryInterface(XText.class, xShape);
              getAllParagraphsFromText(paragraphs, locales, pageBegins, xText);
            } else {
              MessageHandler.printToLogFile("OfficeDrawTools: getAllParagraphs: xShape " + j + " is null");
            }
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    OfficeDrawTools o = new OfficeDrawTools();
    return o.new ParagraphContainer(paragraphs, locales, pageBegins);
  }
  
  /**
   * find the Paragraph to change in a shape and change the text
   * returns -1 if it was found
   * returns the last number of paragraph otherwise
   */
  private static int changeTextOfParagraphInText(int nParaCount, int nPara, int beginn, int length, String replace, XText xText) {
    if (xText != null) {
      XTextCursor xTextCursor = xText.createTextCursor();
      String sText = xText.getString();
      if (nParaCount == nPara) {
        xTextCursor.gotoStart(false);
        xTextCursor.goRight((short)beginn, false);
        xTextCursor.goRight((short)length, true);
        xTextCursor.setString(replace);
        return -1;
      }
      int lastParaEnd = 0;
      for (int k = 0; k < sText.length(); k++) {
        if (sText.charAt(k) == OfficeTools.SINGLE_END_OF_PARAGRAPH.charAt(0)) {
          nParaCount++;
          lastParaEnd = k;
          if (nParaCount == nPara) {
            xTextCursor.gotoStart(false);
            xTextCursor.goRight((short)(beginn + k + 1), false);
            xTextCursor.goRight((short)length, true);
            xTextCursor.setString(replace);
            //  Note: The faked change of position is a workaround to trigger the notification of a change
            return -1;
          }
        }
      }
      if (lastParaEnd < sText.length() - 1) {
        nParaCount++;
      }
    }
    return nParaCount;
  }

  /**
   * change the text of a paragraph
   */
  public static void changeTextOfParagraph(int nPara, int beginn, int length, String replace, XComponent xComponent) {
    try {
      int nParaCount = 0;
      int pageCount = OfficeDrawTools.getDrawPageCount(xComponent);
      for (int i = 0; i < pageCount; i++) {
        XDrawPage xDrawPage = null;
        for (int n = 0; n < 2; n++) {
          if (n == 0) {
            xDrawPage = OfficeDrawTools.getDrawPageByIndex(xComponent, i);
          } else {
            xDrawPage = getNotesPage(xDrawPage);
          }
          XShapes xShapes = OfficeDrawTools.getShapes(xDrawPage);
          int nShapes = xShapes.getCount();
          for(int j = 0; j < nShapes; j++) {
            Object oShape = xShapes.getByIndex(j);
            XShape xShape = UnoRuntime.queryInterface(XShape.class, oShape);
            if (xShape != null) {
              XText xText = UnoRuntime.queryInterface(XText.class, xShape);
              nParaCount = changeTextOfParagraphInText(nParaCount, nPara, beginn, length, replace, xText);
              if (nParaCount < 0) {
                //  Note: The faked change of position is a workaround to trigger the notification of a change
                Point p = xShape.getPosition();
                xShape.setPosition(p);
                return;
              }
            } else {
              MessageHandler.printToLogFile("OfficeDrawTools: changeTextOfParagraph: xShape " + j + " is null");
            }
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }

  /**
   * find the Paragraph to change in a shape and change the locale
   * returns -1 if it was found
   * returns the last number of paragraph otherwise
   */
  private static int changeLocaleOfParagraphInText(int nParaCount, int nPara, int beginn, int length, Locale locale, 
      XText xText) throws UnknownPropertyException, PropertyVetoException, IllegalArgumentException, WrappedTargetException {
    if (xText != null) {
      XTextCursor xTextCursor = xText.createTextCursor();
      String sText = xText.getString();
      if (nParaCount == nPara) {
        xTextCursor.gotoStart(false);
        xTextCursor.goRight((short)beginn, false);
        xTextCursor.goRight((short)length, true);
        XPropertySet xParaPropSet = UnoRuntime.queryInterface(XPropertySet.class, xTextCursor);
        xParaPropSet.setPropertyValue("CharLocale", locale);
        return -1;
      }
      int lastParaEnd = 0;
      for (int k = 0; k < sText.length(); k++) {
        if (sText.charAt(k) == OfficeTools.SINGLE_END_OF_PARAGRAPH.charAt(0)) {
          nParaCount++;
          lastParaEnd = k;
          if (nParaCount == nPara) {
            xTextCursor.gotoStart(false);
            xTextCursor.goRight((short)(beginn + k + 1), false);
            xTextCursor.goRight((short)length, true);
            XPropertySet xParaPropSet = UnoRuntime.queryInterface(XPropertySet.class, xTextCursor);
            xParaPropSet.setPropertyValue("CharLocale", locale);
            return -1;
          }
        }
      }
      if (lastParaEnd < sText.length() - 1) {
        nParaCount++;
      }
    }
    return nParaCount;
  }
  
  /**
   * change the language of a paragraph
   */
  public static void setLanguageOfParagraph(int nPara, int beginn, int length, Locale locale, XComponent xComponent) {
    try {
      int nParaCount = 0;
      int pageCount = OfficeDrawTools.getDrawPageCount(xComponent);
      for (int i = 0; i < pageCount; i++) {
        XDrawPage xDrawPage = null;
        for (int n = 0; n < 2; n++) {
          if (n == 0) {
            xDrawPage = OfficeDrawTools.getDrawPageByIndex(xComponent, i);
          } else {
            xDrawPage = getNotesPage(xDrawPage);
          }
          XShapes xShapes = OfficeDrawTools.getShapes(xDrawPage);
          int nShapes = xShapes.getCount();
          for(int j = 0; j < nShapes; j++) {
            Object oShape = xShapes.getByIndex(j);
            XShape xShape = UnoRuntime.queryInterface(XShape.class, oShape);
            if (xShape != null) {
              XText xText = UnoRuntime.queryInterface(XText.class, xShape);
              nParaCount = changeLocaleOfParagraphInText(nParaCount, nPara, beginn, length, locale, xText);
              if (nParaCount < 0) {
                //  Note: The faked change of position is a workaround to trigger the notification of a change
                Point p = xShape.getPosition();
                xShape.setPosition(p);
                return;
              }
            } else {
              MessageHandler.printToLogFile("OfficeDrawTools: setLanguageOfParagraph: xShape " + j + " is null");
            }
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }

  /**
   * find the Paragraph in a shape
   * returns -1 if it was found
   * returns the last number of paragraph otherwise
   */
  private static int findParaInText(int nParaCount, int nPara, XText xText) {
    if (xText != null) {
      String sText = xText.getString();
      if (nParaCount == nPara && sText.length() > 0) {
        return -1;
      }
      int lastParaEnd = 0;
      for (int k = 0; k < sText.length(); k++) {
        if (sText.charAt(k) == OfficeTools.SINGLE_END_OF_PARAGRAPH.charAt(0)) {
          nParaCount++;
          lastParaEnd = k;
          if (nParaCount == nPara) {
            return -1;
          }
        }
      }
      if (lastParaEnd < sText.length() - 1) {
        nParaCount++;
      }
    }
    return nParaCount;
  }
  
  /**
   * set the current draw page for a given paragraph
   */
  public static void setCurrentPage(int nPara, XComponent xComponent) {
    try {
      XModel xModel = UnoRuntime.queryInterface(XModel.class, xComponent);
      XController xController = xModel.getCurrentController();
      XDrawView xDrawView = UnoRuntime.queryInterface(XDrawView.class, xController);
      int nParaCount = 0;
      int pageCount = OfficeDrawTools.getDrawPageCount(xComponent);
      for (int i = 0; i < pageCount; i++) {
        XDrawPage xDrawPage = null;
        for (int n = 0; n < 2; n++) {
          if (n == 0) {
            xDrawPage = OfficeDrawTools.getDrawPageByIndex(xComponent, i);
          } else {
            xDrawPage = getNotesPage(xDrawPage);
          }
          XShapes xShapes = OfficeDrawTools.getShapes(xDrawPage);
          int nShapes = xShapes.getCount();
          for(int j = 0; j < nShapes; j++) {
            Object oShape = xShapes.getByIndex(j);
            XShape xShape = UnoRuntime.queryInterface(XShape.class, oShape);
            if (xShape != null) {
              XText xText = UnoRuntime.queryInterface(XText.class, xShape);
              nParaCount = findParaInText(nParaCount, nPara, xText);
              if (nParaCount < 0) {
                xDrawView.setCurrentPage(xDrawPage);
                return;
              }
            } else {
              MessageHandler.printToLogFile("OfficeDrawTools: setCurrentPage: xShape " + j + " is null");
            }
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }

  /**
   * get the first paragraph of current draw page
   */
  public static int getParagraphFromCurrentPage(XComponent xComponent) {
    int nParaCount = 0;
    try {
      XModel xModel = UnoRuntime.queryInterface(XModel.class, xComponent);
      XController xController = xModel.getCurrentController();
      XDrawView xDrawView = UnoRuntime.queryInterface(XDrawView.class, xController);
      XDrawPage xCurrentDrawPage = xDrawView.getCurrentPage();
      int pageCount = OfficeDrawTools.getDrawPageCount(xComponent);
      for (int i = 0; i < pageCount; i++) {
        XDrawPage xDrawPage = null;
        for (int n = 0; n < 2; n++) {
          if (n == 0) {
            xDrawPage = OfficeDrawTools.getDrawPageByIndex(xComponent, i);
          } else {
            xDrawPage = getNotesPage(xDrawPage);
          }
          if (xDrawPage.equals(xCurrentDrawPage)) {
            return nParaCount;
          }
          XShapes xShapes = OfficeDrawTools.getShapes(xDrawPage);
          int nShapes = xShapes.getCount();
          for(int j = 0; j < nShapes; j++) {
            Object oShape = xShapes.getByIndex(j);
            XShape xShape = UnoRuntime.queryInterface(XShape.class, oShape);
            if (xShape != null) {
              XText xText = UnoRuntime.queryInterface(XText.class, xShape);
              nParaCount = findParaInText(nParaCount, -1, xText);
            } else {
              MessageHandler.printToLogFile("OfficeDrawTools: getParagraphFromCurrentPage: xShape " + j + " is null");
            }
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return nParaCount;
  }

  /**
   * true: if paragraph is in a notes page
   */
  public static boolean isParagraphInNotesPage(int nPara, XComponent xComponent) {
    int nParaCount = 0;
    try {
      int pageCount = OfficeDrawTools.getDrawPageCount(xComponent);
      for (int i = 0; i < pageCount; i++) {
        XDrawPage xDrawPage = null;
        for (int n = 0; n < 2; n++) {
          if (n == 0) {
            xDrawPage = OfficeDrawTools.getDrawPageByIndex(xComponent, i);
          } else {
            xDrawPage = getNotesPage(xDrawPage);
          }
          XShapes xShapes = OfficeDrawTools.getShapes(xDrawPage);
          int nShapes = xShapes.getCount();
          for(int j = 0; j < nShapes; j++) {
            Object oShape = xShapes.getByIndex(j);
            XShape xShape = UnoRuntime.queryInterface(XShape.class, oShape);
            if (xShape != null) {
              XText xText = UnoRuntime.queryInterface(XText.class, xShape);
              nParaCount = findParaInText(nParaCount, nPara, xText);
              if (nParaCount < 0) {
                return (n == 0 ? false : true);
              }
            } else {
              MessageHandler.printToLogFile("OfficeDrawTools: isParagraphInNotesPage: xShape " + j + " is null");
            }
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return false;
  }
  
  public static Locale getDocumentLocale(XComponent xComponent) {
    try {
      int pageCount = OfficeDrawTools.getDrawPageCount(xComponent);
      for (int i = 0; i < pageCount; i++) {
        XDrawPage xDrawPage = null;
        for (int n = 0; n < 2; n++) {
          if (n == 0) {
            xDrawPage = OfficeDrawTools.getDrawPageByIndex(xComponent, i);
          } else {
            xDrawPage = getNotesPage(xDrawPage);
          }
          XShapes xShapes = OfficeDrawTools.getShapes(xDrawPage);
          int nShapes = xShapes.getCount();
          for(int j = 0; j < nShapes; j++) {
            Object oShape = xShapes.getByIndex(j);
            XShape xShape = UnoRuntime.queryInterface(XShape.class, oShape);
            if (xShape != null) {
              XText xText = UnoRuntime.queryInterface(XText.class, xShape);
              XTextCursor xTextCursor = xText.createTextCursor();
              XPropertySet xParaPropSet = UnoRuntime.queryInterface(XPropertySet.class, xTextCursor);
              return ((Locale) xParaPropSet.getPropertyValue("CharLocale"));
            } else {
              MessageHandler.printToLogFile("OfficeDrawTools: getDocumentLocale: xShape " + j + " is null");
            }
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    return null;
  }
  
  public class ParagraphContainer {
    public List<String> paragraphs;
    public List<Locale> locales;
    public List<Integer> pageBegins;
    
    ParagraphContainer(List<String> paragraphs, List<Locale> locales, List<Integer> pageBegins) {
      this.paragraphs = paragraphs;
      this.locales = locales;
      this.pageBegins = pageBegins;
    }
  }
  
}


