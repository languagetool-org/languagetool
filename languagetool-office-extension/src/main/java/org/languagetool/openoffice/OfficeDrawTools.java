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
import com.sun.star.lang.Locale;
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
  public static XDrawPage insertNewDrawPageByIndex( XComponent xComponent, int nIndex ) throws Exception {
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
  public static XShapes getShapes (XDrawPage xPage) {
    return UnoRuntime.queryInterface( XShapes.class, xPage );
  }
  
  /**
   * get all paragraphs of a impress document
   */
  public static ImpressParagraphContainer getAllParagraphs(XComponent xComponent) {
    List<String> paragraphs = new ArrayList<>();
    List<Locale> locales = new ArrayList<>();
    List<Integer> pageBegins = new ArrayList<>();
    int nPara = 0;
    try {
      int pageCount = OfficeDrawTools.getDrawPageCount(xComponent);
      for (int i = 0; i < pageCount; i++) {
        pageBegins.add(nPara);
        XDrawPage xDrawPage = OfficeDrawTools.getDrawPageByIndex(xComponent, i);
        XShapes xShapes = OfficeDrawTools.getShapes(xDrawPage);
        int nShapes = xShapes.getCount();
        for(int j = 0; j < nShapes; j++) {
          Object oShape = xShapes.getByIndex(j);
          XShape xShape = UnoRuntime.queryInterface(XShape.class, oShape);
          if (xShape != null) {
            XText xText = UnoRuntime.queryInterface(XText.class, xShape);
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
          } else {
            MessageHandler.printToLogFile("xShape " + j + " is null");
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
    OfficeDrawTools o = new OfficeDrawTools();
    return o.new ImpressParagraphContainer(paragraphs, locales, pageBegins);
  }
  
  /**
   * change the text of a paragraph
   */
  public static void changeTextOfParagraph(int nPara, int beginn, int length, String replace, XComponent xComponent) {
    try {
      int nParaCount = 0;
      int pageCount = OfficeDrawTools.getDrawPageCount(xComponent);
      for (int i = 0; i < pageCount; i++) {
        XDrawPage xDrawPage = OfficeDrawTools.getDrawPageByIndex(xComponent, i);
        XShapes xShapes = OfficeDrawTools.getShapes(xDrawPage);
        int nShapes = xShapes.getCount();
        for(int j = 0; j < nShapes; j++) {
          Object oShape = xShapes.getByIndex(j);
          XShape xShape = UnoRuntime.queryInterface(XShape.class, oShape);
          if (xShape != null) {
            XText xText = UnoRuntime.queryInterface(XText.class, xShape);
            if (xText != null) {
              XTextCursor xTextCursor = xText.createTextCursor();
              String sText = xText.getString();
              if (nParaCount == nPara) {
                xTextCursor.gotoStart(false);
                xTextCursor.goRight((short)beginn, false);
                xTextCursor.goRight((short)length, true);
                xTextCursor.setString(replace);
                //  Note: The faked change of position is a workaround to trigger the notification of a change
                Point p = xShape.getPosition();
                xShape.setPosition(p);
                return;
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
                    Point p = xShape.getPosition();
                    xShape.setPosition(p);
                    return;
                  }
                }
              }
              if (lastParaEnd < sText.length() - 1) {
                nParaCount++;
              }
            }
          } else {
            MessageHandler.printToLogFile("xShape " + j + " is null");
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }

  /**
   * set the view cursor inside of a paragraph
   */
  public static void setViewCursor(int nChar, int nPara, XComponent xComponent) {
    try {
      XModel xModel = UnoRuntime.queryInterface(XModel.class, xComponent);
      XController xController = xModel.getCurrentController();
      XDrawView xDrawView = UnoRuntime.queryInterface(XDrawView.class, xController);
      int nParaCount = 0;
      int pageCount = OfficeDrawTools.getDrawPageCount(xComponent);
      for (int i = 0; i < pageCount; i++) {
        XDrawPage xDrawPage = OfficeDrawTools.getDrawPageByIndex(xComponent, i);
        XShapes xShapes = OfficeDrawTools.getShapes(xDrawPage);
        int nShapes = xShapes.getCount();
        for(int j = 0; j < nShapes; j++) {
          Object oShape = xShapes.getByIndex(j);
          XShape xShape = UnoRuntime.queryInterface(XShape.class, oShape);
          if (xShape != null) {
            XText xText = UnoRuntime.queryInterface(XText.class, xShape);
            if (xText != null) {
              String sText = xText.getString();
              if (nParaCount == nPara) {
                xDrawView.setCurrentPage(xDrawPage);
                return;
              }
              int lastParaEnd = 0;
              for (int k = 0; k < sText.length(); k++) {
                if (sText.charAt(k) == OfficeTools.SINGLE_END_OF_PARAGRAPH.charAt(0)) {
                  nParaCount++;
                  lastParaEnd = k;
                  if (nParaCount == nPara) {
                    xDrawView.setCurrentPage(xDrawPage);
                    return;
                  }
                }
              }
              if (lastParaEnd < sText.length() - 1) {
                nParaCount++;
              }
            }
          } else {
            MessageHandler.printToLogFile("xShape " + j + " is null");
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }

  /**
   * change the language of a paragraph
   */
  public static void setLanguageOfParagraph(int nPara, int beginn, int length, Locale locale, XComponent xComponent) {
    try {
      int nParaCount = 0;
      int pageCount = OfficeDrawTools.getDrawPageCount(xComponent);
      for (int i = 0; i < pageCount; i++) {
        XDrawPage xDrawPage = OfficeDrawTools.getDrawPageByIndex(xComponent, i);
        XShapes xShapes = OfficeDrawTools.getShapes(xDrawPage);
        int nShapes = xShapes.getCount();
        for(int j = 0; j < nShapes; j++) {
          Object oShape = xShapes.getByIndex(j);
          XShape xShape = UnoRuntime.queryInterface(XShape.class, oShape);
          if (xShape != null) {
            XText xText = UnoRuntime.queryInterface(XText.class, xShape);
            if (xText != null) {
              XTextCursor xTextCursor = xText.createTextCursor();
              String sText = xText.getString();
              if (nParaCount == nPara) {
                xTextCursor.gotoStart(false);
                xTextCursor.goRight((short)beginn, false);
                xTextCursor.goRight((short)length, true);
                XPropertySet xParaPropSet = UnoRuntime.queryInterface(XPropertySet.class, xTextCursor);
                xParaPropSet.setPropertyValue("CharLocale", locale);
                return;
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
                    return;
                  }
                }
              }
              if (lastParaEnd < sText.length() - 1) {
                nParaCount++;
              }
            }
          } else {
            MessageHandler.printToLogFile("xShape " + j + " is null");
          }
        }
      }
    } catch (Throwable t) {
      MessageHandler.showError(t);
    }
  }

  public class ImpressParagraphContainer {
    public List<String> paragraphs;
    public List<Locale> locales;
    public List<Integer> pageBegins;
    
    ImpressParagraphContainer(List<String> paragraphs, List<Locale> locales, List<Integer> pageBegins) {
      this.paragraphs = paragraphs;
      this.locales = locales;
      this.pageBegins = pageBegins;
    }
  }
  
}


