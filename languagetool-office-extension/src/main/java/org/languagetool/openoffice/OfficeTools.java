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

import com.sun.star.awt.XMenuBar;
import com.sun.star.awt.XPopupMenu;
import com.sun.star.beans.Property;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XLayoutManager;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.linguistic2.XSearchableDictionaryList;
import com.sun.star.text.XTextDocument;
import com.sun.star.ui.XUIElement;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Some tools to get information of LibreOffice/OpenOffice document context
 * @since 4.3
 * @author Fred Kruse
 */
class OfficeTools {
  
  public static final int PROOFINFO_UNKNOWN = 0;
  public static final int PROOFINFO_GET_PROOFRESULT = 1;
  public static final int PROOFINFO_MARK_PARAGRAPH = 2;

  public static final String END_OF_PARAGRAPH = "\n\n";  //  Paragraph Separator like in standalone GUI
  public static final int NUMBER_PARAGRAPH_CHARS = END_OF_PARAGRAPH.length();  //  number of end of paragraph characters
  public static final String SINGLE_END_OF_PARAGRAPH = "\n";
  public static final String MANUAL_LINEBREAK = "\r";  //  to distinguish from paragraph separator
  public static final String ZERO_WIDTH_SPACE = "\u200B";  // Used to mark footnotes
  public static final String LOG_LINE_BREAK = System.lineSeparator();  //  LineBreak in Log-File (MS-Windows compatible)
  public static final int MAX_SUGGESTIONS = 15;  // Number of suggestions maximal shown in LO/OO
  public static final int NUMBER_TEXTLEVEL_CACHE = 2;  // Number of caches for matches of text level rules

  public static int DEBUG_MODE_SD = 0;
  public static boolean DEBUG_MODE_MD = false;
  public static boolean DEBUG_MODE_DC = false;
  public static boolean DEBUG_MODE_FP = false;
  public static boolean DEBUG_MODE_LM = false;
  public static boolean DEBUG_MODE_TQ = false;
  public static boolean DEBUG_MODE_LD = false;
  public static boolean DEVELOP_MODE = false;

  private static final String MENU_BAR = "private:resource/menubar/menubar";
  private static final String LOG_DELIMITER = ",";

  /**
   * Returns the XDesktop
   * Returns null if it fails
   */
  @Nullable
  static XDesktop getDesktop(XComponentContext xContext) {
    try {
      if (xContext == null) {
        return null;
      }
      XMultiComponentFactory xMCF = UnoRuntime.queryInterface(XMultiComponentFactory.class,
              xContext.getServiceManager());
      if (xMCF == null) {
        return null;
      }
      Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
      if (desktop == null) {
        return null;
      }
      return UnoRuntime.queryInterface(XDesktop.class, desktop);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }

  /** 
   * Returns the current XComponent 
   * Returns null if it fails
   */
  @Nullable
  static XComponent getCurrentComponent(XComponentContext xContext) {
    try {
      XDesktop xdesktop = getDesktop(xContext);
      if(xdesktop == null) {
        return null;
      }
      else return xdesktop.getCurrentComponent();
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
    
  /**
   * Returns the current text document (if any) 
   * Returns null if it fails
   */
  @Nullable
  static XTextDocument getCurrentDocument(XComponentContext xContext) {
    try {
      XComponent curComp = getCurrentComponent(xContext);
      if (curComp == null) {
        return null;
      }
      else return UnoRuntime.queryInterface(XTextDocument.class, curComp);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  static void printPropertySet (Object o) {
    XPropertySet propSet = UnoRuntime.queryInterface(XPropertySet.class, o);
    if (propSet == null) {
      MessageHandler.printToLogFile("XPropertySet == null");
      return;
    }
    XPropertySetInfo propertySetInfo = propSet.getPropertySetInfo();
    MessageHandler.printToLogFile("PropertySet:");
    for (Property property : propertySetInfo.getProperties()) {
      MessageHandler.printToLogFile("Name: " + property.Name + ", Type: " + property.Type.getTypeName());
    }
  }
  
  /**
   * Returns the searchable dictionary list
   * Returns null if it fails
   */
  @Nullable
  static XSearchableDictionaryList getSearchableDictionaryList(XComponentContext xContext) {
    try {
      if (xContext == null) {
        return null;
      }
      XMultiComponentFactory xMCF = UnoRuntime.queryInterface(XMultiComponentFactory.class,
              xContext.getServiceManager());
      if (xMCF == null) {
        return null;
      }
      Object dictionaryList = xMCF.createInstanceWithContext("com.sun.star.linguistic2.DictionaryList", xContext);
      if (dictionaryList == null) {
        return null;
      }
      return UnoRuntime.queryInterface(XSearchableDictionaryList.class, dictionaryList);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }


  static XMenuBar getMenuBar(XComponentContext xContext) {
    try {
      XDesktop desktop = OfficeTools.getDesktop(xContext);
      if (desktop == null) {
        return null;
      }
      XFrame frame = desktop.getCurrentFrame();
      if (frame == null) {
        return null;
      }
      XPropertySet propSet = UnoRuntime.queryInterface(XPropertySet.class, frame);
      if (propSet == null) {
        return null;
      }
      XLayoutManager layoutManager = UnoRuntime.queryInterface(XLayoutManager.class,  propSet.getPropertyValue("LayoutManager"));
      if (layoutManager == null) {
        return null;
      }
      XUIElement oMenuBar = layoutManager.getElement(MENU_BAR); 
      XPropertySet props = UnoRuntime.queryInterface(XPropertySet.class, oMenuBar); 
      return UnoRuntime.queryInterface(XMenuBar.class,  props.getPropertyValue("XMenuBar"));
    } catch (Throwable t) {
      MessageHandler.printException(t);
    }
    return null;
  }
  
  /**
   * Returns a empty Popup Menu 
   * Returns null if it fails
   */
  @Nullable
  static XPopupMenu getPopupMenu(XComponentContext xContext) {
    try {
      if (xContext == null) {
        return null;
      }
      XMultiComponentFactory xMCF = UnoRuntime.queryInterface(XMultiComponentFactory.class,
              xContext.getServiceManager());
      if (xMCF == null) {
        return null;
      }
      Object oPopupMenu = xMCF.createInstanceWithContext("com.sun.star.awt.PopupMenu", xContext);
      if (oPopupMenu == null) {
        return null;
      }
      return UnoRuntime.queryInterface(XPopupMenu.class, oPopupMenu);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }
  
  static void setLogLevel(String logLevel) {
    if (logLevel != null) {
      String[] levels = logLevel.split(LOG_DELIMITER);
      for (String level : levels) {
        if(level.equals("1") || level.equals("2") || level.equals("3") || level.startsWith("all:")) {
          int numLevel;
          if (level.startsWith("all:")) {
            String[] levelAll = level.split(":");
            if(levelAll.length != 2) {
              continue;
            }
            numLevel = Integer.parseInt(levelAll[1]);
          } else {
            numLevel = Integer.parseInt(level);
          }
          if(numLevel > 0) {
            DEBUG_MODE_MD = true;
            DEBUG_MODE_TQ = true;
            DEBUG_MODE_FP = true;
            if (DEBUG_MODE_SD == 0) {
              DEBUG_MODE_SD = numLevel;
            }
          }
          if (numLevel > 1) {
            DEBUG_MODE_DC = true;
            DEBUG_MODE_LM = true;
          }
        } else if(level.startsWith("sd:")) {
          String[] levelSD = level.split(":");
          if(levelSD.length != 2) {
            continue;
          }
          int numLevel = Integer.parseInt(levelSD[1]);
          if (numLevel > 0) {
            DEBUG_MODE_SD = numLevel;
          }
        } else if(level.equals("md")) {
          DEBUG_MODE_MD = true;
        } else if(level.equals("dc")) {
          DEBUG_MODE_DC = true;
        } else if(level.equals("fp")) {
          DEBUG_MODE_FP = true;
        } else if(level.equals("lm")) {
          DEBUG_MODE_LM = true;
        } else if(level.equals("tq")) {
          DEBUG_MODE_TQ = true;
        } else if(level.equals("ld")) {
          DEBUG_MODE_LD = true;
        } else if(level.equals("dev")) {
          DEVELOP_MODE = true;
        }
      }
    }
  }

}
