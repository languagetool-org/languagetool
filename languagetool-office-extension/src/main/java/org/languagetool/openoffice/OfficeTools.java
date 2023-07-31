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

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.AbstractStatisticSentenceStyleRule;
import org.languagetool.rules.AbstractStatisticStyleRule;
import org.languagetool.rules.AbstractStyleTooOftenUsedWordRule;
import org.languagetool.rules.ReadabilityRule;
import org.languagetool.rules.Rule;

import com.sun.star.awt.XMenuBar;
import com.sun.star.awt.XPopupMenu;
import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertySetInfo;
import com.sun.star.container.XNameAccess;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XLayoutManager;
import com.sun.star.frame.XModel;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.linguistic2.XProofreadingIterator;
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
public class OfficeTools {
  
  public enum DocumentType {
    WRITER,       //  Writer document
    IMPRESS,      //  Impress document
    CALC,         //  Calc document
    UNSUPPORTED   //  unsupported document
  }
  
  public enum RemoteCheck {
    NONE,         //  no remote check
    ALL,          //  spell and grammar check
    ONLY_SPELL,   //  only spell check
    ONLY_GRAMMAR  //  only grammar check
  }
    
  public static final String EXTENSION_MAINTAINER = "Fred Kruse";
  public static final String LT_SERVICE_NAME = "org.languagetool.openoffice.Main";
  public static final int PROOFINFO_UNKNOWN = 0;
  public static final int PROOFINFO_GET_PROOFRESULT = 1;
  public static final int PROOFINFO_MARK_PARAGRAPH = 2;

  public static final String END_OF_PARAGRAPH = "\n\n";   //  Paragraph Separator like in standalone GUI
  public static final int NUMBER_PARAGRAPH_CHARS = END_OF_PARAGRAPH.length();  //  number of end of paragraph characters
  public static final String SINGLE_END_OF_PARAGRAPH = "\n";
  public static final String MANUAL_LINEBREAK = "\r";     //  to distinguish from paragraph separator
  public static final String ZERO_WIDTH_SPACE = "\u200B"; // Used to mark footnotes, functions, etc.
  public static final char ZERO_WIDTH_SPACE_CHAR = '\u200B'; // Used to mark footnotes, functions, etc.
  public static final String IGNORE_LANGUAGE = "zxx";     // Used from LT to mark automatic generated text like indexes
  public static final String LOG_LINE_BREAK = System.lineSeparator();  //  LineBreak in Log-File (MS-Windows compatible)
  public static final int MAX_SUGGESTIONS = 15;           // Number of suggestions maximal shown in LO/OO
  public static final int NUMBER_TEXTLEVEL_CACHE = 4;     // Number of caches for matches of text level rules
  public static final String MULTILINGUAL_LABEL = "99-";  // Label added in front of variant to indicate a multilingual paragraph (returned is the main language)
  public static final int CHECK_MULTIPLIKATOR = 40;       //  Number of minimum checks for a first check run
  public static final int CHECK_SHAPES_TIME = 1000;       //  time interval to run check for changes in text inside of shapes
  public static int TIME_TOLERANCE = 100;                 //  Minimal milliseconds to show message in TM debug mode
  
  public static int DEBUG_MODE_SD = 0;            //  Set Debug Mode for SingleDocument
  public static int DEBUG_MODE_SC = 0;            //  Set Debug Mode for SingleCheck
  public static int DEBUG_MODE_CR = 0;            //  Set Debug Mode for CheckRequest
  public static boolean DEBUG_MODE_MD = false;    //  Activate Debug Mode for MultiDocumentsHandler
  public static boolean DEBUG_MODE_DC = false;    //  Activate Debug Mode for DocumentCache
  public static boolean DEBUG_MODE_FP = false;    //  Activate Debug Mode for FlatParagraphTools
  public static boolean DEBUG_MODE_LM = false;    //  Activate Debug Mode for LanguageToolMenus
  public static boolean DEBUG_MODE_TQ = false;    //  Activate Debug Mode for TextLevelCheckQueue
  public static boolean DEBUG_MODE_LD = false;    //  Activate Debug Mode for LtDictionary
  public static boolean DEBUG_MODE_CD = false;    //  Activate Debug Mode for SpellAndGrammarCheckDialog
  public static boolean DEBUG_MODE_IO = false;    //  Activate Debug Mode for Cache save to file
  public static boolean DEBUG_MODE_SR = false;    //  Activate Debug Mode for SortedTextRules
  public static boolean DEBUG_MODE_TM = false;    //  Activate Debug Mode for time measurements
  public static boolean DEVELOP_MODE_ST = false;  //  Activate Development Mode to test sorted text IDs
  public static boolean DEVELOP_MODE = false;     //  Activate Development Mode

  public  static final String CONFIG_FILE = "Languagetool.cfg";
  public  static final String OOO_CONFIG_FILE = "Languagetool-ooo.cfg";
  private static final String OLD_CONFIG_FILE = ".languagetool-ooo.cfg";
  private static final String LOG_FILE = "LanguageTool.log";
  public  static final String STATISTICAL_ANALYZES_CONFIG_FILE = "LT_Statistical_Analyzes.cfg";

  private static final String VENDOR_ID = "languagetool.org";
  private static final String APPLICATION_ID = "LanguageTool";
  private static final String CACHE_ID = "cache";
  private static String OFFICE_EXTENSION_ID = null;
  private static OfficeProductInfo OFFICE_PRODUCT_INFO = null;

  private static final String MENU_BAR = "private:resource/menubar/menubar";
  private static final String LOG_DELIMITER = ",";
  

  private static final double LT_HEAP_LIMIT_FACTOR = 0.9;
  private static double MAX_HEAP_SPACE = -1;
  private static double LT_HEAP_LIMIT = -1;
  
  private final static int MAX_LO_WAITS = 3000;
  private static int numLoWaits = 0;
  private static Object waitObj = new Object();

/*
  private static final long KEY_RELEASE_TOLERANCE = 500;
  private static long lastKeyRelease = 0;
*/
  
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
  public static XComponent getCurrentComponent(XComponentContext xContext) {
    try {
      XDesktop xdesktop = getDesktop(xContext);
      if (xdesktop == null) {
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
      MessageHandler.printToLogFile("OfficeTools: printPropertySet: XPropertySet == null");
      return;
    }
    XPropertySetInfo propertySetInfo = propSet.getPropertySetInfo();
    MessageHandler.printToLogFile("OfficeTools: printPropertySet: PropertySet:");
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

  /**
   * Returns the searchable dictionary list
   * Returns null if it fails
   */
  @Nullable
  static XProofreadingIterator getProofreadingIterator(XComponentContext xContext) {
    try {
      if (xContext == null) {
        return null;
      }
      XMultiComponentFactory xMCF = UnoRuntime.queryInterface(XMultiComponentFactory.class,
              xContext.getServiceManager());
      if (xMCF == null) {
        return null;
      }
      Object proofreadingIterator = xMCF.createInstanceWithContext("com.sun.star.linguistic2.ProofreadingIterator", xContext);
      if (proofreadingIterator == null) {
        return null;
      }
      return UnoRuntime.queryInterface(XProofreadingIterator.class, proofreadingIterator);
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }
  }

  /**
   * Get the menu bar of LO/OO
   * Returns null if it fails
   */
  static XMenuBar getMenuBar(XComponent xComponent) {
    try {
      XModel xModel = UnoRuntime.queryInterface(XModel.class, xComponent);
      if (xModel == null) {
        MessageHandler.printToLogFile("SingleDocument: setDokumentListener: XModel not found!");
        return null;
      }
      XController xController = xModel.getCurrentController();
      if (xController == null) {
        MessageHandler.printToLogFile("SingleDocument: setDokumentListener: XController not found!");
        return null;
      }
      XFrame frame = xController.getFrame();
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
  
  /**
   *  dispatch an internal LO/OO command
   */
  public static boolean dispatchCmd(String cmd, XComponentContext xContext) {
    return dispatchCmd(cmd, new PropertyValue[0], xContext);
  } 

  /**
   *  dispatch an internal LO/OO command
   *  cmd does not include the ".uno:" substring; e.g. pass "Zoom" not ".uno:Zoom"
   */
  public static boolean dispatchUnoCmd(String cmd, XComponentContext xContext) {
    return dispatchCmd((".uno:" + cmd), new PropertyValue[0], xContext);
  } 

  /**
   * Dispatch a internal LO/OO command
   */
  public static boolean dispatchCmd(String cmd, PropertyValue[] props, XComponentContext xContext) {
    try {
      if (xContext == null) {
        MessageHandler.printToLogFile("OfficeTools: dispatchCmd: xContext == null");
        return false;
      }
      XMultiComponentFactory xMCF = UnoRuntime.queryInterface(XMultiComponentFactory.class,
              xContext.getServiceManager());
      if (xMCF == null) {
        MessageHandler.printToLogFile("OfficeTools: dispatchCmd: xMCF == null");
        return false;
      }
      Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
      if (desktop == null) {
        MessageHandler.printToLogFile("OfficeTools: dispatchCmd: desktop == null");
        return false;
      }
      XDesktop xdesktop = UnoRuntime.queryInterface(XDesktop.class, desktop);
      if (xdesktop == null) {
        MessageHandler.printToLogFile("OfficeTools: dispatchCmd: xdesktop == null");
        return false;
      }
      Object helper = xMCF.createInstanceWithContext("com.sun.star.frame.DispatchHelper", xContext);
      if (helper == null) {
        MessageHandler.printToLogFile("OfficeTools: dispatchCmd: helper == null");
        return false;
      }
      XDispatchHelper dispatchHelper = UnoRuntime.queryInterface(XDispatchHelper.class, helper);
      if (dispatchHelper == null) {
        MessageHandler.printToLogFile("OfficeTools: dispatchCmd: dispatchHelper == null");
        return false;
      }
      XDispatchProvider provider = UnoRuntime.queryInterface(XDispatchProvider.class, xdesktop.getCurrentFrame());
      if (provider == null) {
        MessageHandler.printToLogFile("OfficeTools: dispatchCmd: provider == null");
        return false;
      }
      dispatchHelper.executeDispatch(provider, cmd, "", 0, props);
      return true;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return false;
    }
  }
  
  /**
   *  Get a String from local
   */
  static String localeToString(Locale locale) {
    if (locale == null) {
      return null;
    }
    return locale.Language + (locale.Country.isEmpty() ? "" : "-" + locale.Country) + (locale.Variant.isEmpty() ? "" : "-" + locale.Variant);
  }

  /**
   *  return true if two locales are equal  
   */
  static boolean isEqualLocale(Locale locale1, Locale locale2) {
    return (locale1.Language.equals(locale2.Language) && locale1.Country.equals(locale2.Country) 
        && locale1.Variant.equals(locale2.Variant));
  }

  /**
   *  return true if the list of locales contains the locale
   */
  static boolean containsLocale(List<Locale> locales, Locale locale) {
    for (Locale loc : locales) {
      if (isEqualLocale(loc, locale)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns old configuration file
   */
  public static File getOldConfigFile() {
    String homeDir = System.getProperty("user.home");
    if (homeDir == null) {
      MessageHandler.showError(new RuntimeException("Could not get home directory"));
      return null;
    }
    return new File(homeDir, OLD_CONFIG_FILE);
  }

  /**
   * Returns directory to store every information for LT office extension
   * @since 4.7
   */
  public static File getLOConfigDir() {
    return getLOConfigDir(null);
  }

  public static File getLOConfigDir(XComponentContext xContext) {
    if (OFFICE_EXTENSION_ID == null) {
      if (xContext == null) {
        OFFICE_EXTENSION_ID = "LibreOffice";
      } else {
        OFFICE_EXTENSION_ID = getOfficeProductInfo(xContext).ooName;
      }
    }
    String userHome = null;
    File directory;
    try {
      userHome = System.getProperty("user.home");
    } catch (SecurityException ex) {
    }
    if (userHome == null) {
      MessageHandler.showError(new RuntimeException("Could not get home directory"));
      directory = null;
    } else if (SystemUtils.IS_OS_WINDOWS) {
      // Path: \\user\<YourUserName>\AppData\Roaming\languagetool.org\LanguageTool\LibreOffice  
      File appDataDir = null;
      try {
        String appData = System.getenv("APPDATA");
        if (!StringUtils.isEmpty(appData)) {
          appDataDir = new File(appData);
        }
      } catch (SecurityException ex) {
      }
      if (appDataDir != null && appDataDir.isDirectory()) {
        String path = VENDOR_ID + "\\" + APPLICATION_ID + "\\" + OFFICE_EXTENSION_ID + "\\";
        directory = new File(appDataDir, path);
      } else {
        String path = "Application Data\\" + VENDOR_ID + "\\" + APPLICATION_ID + "\\" + OFFICE_EXTENSION_ID + "\\";
        directory = new File(userHome, path);
      }
    } else if (SystemUtils.IS_OS_LINUX) {
      // Path: /home/<YourUserName>/.config/LanguageTool/LibreOffice  
      File appDataDir = null;
      try {
        String xdgConfigHome = System.getenv("XDG_CONFIG_HOME");
        if (!StringUtils.isEmpty(xdgConfigHome)) {
          appDataDir = new File(xdgConfigHome);
          if (!appDataDir.isAbsolute()) {
            //https://specifications.freedesktop.org/basedir-spec/basedir-spec-latest.html
            //All paths set in these environment variables must be absolute.
            //If an implementation encounters a relative path in any of these
            //variables it should consider the path invalid and ignore it.
            appDataDir = null;
          }
        }
      } catch (SecurityException ex) {
      }
      if (appDataDir != null && appDataDir.isDirectory()) {
        String path = APPLICATION_ID + "/" + OFFICE_EXTENSION_ID + "/";
        directory = new File(appDataDir, path);
      } else {
        String path = ".config/" + APPLICATION_ID + "/" + OFFICE_EXTENSION_ID + "/";
        directory = new File(userHome, path);
      }
    } else if (SystemUtils.IS_OS_MAC_OSX) {
      String path = "Library/Application Support/" + APPLICATION_ID + "/" + OFFICE_EXTENSION_ID + "/";
      directory = new File(userHome, path);
    } else {
      String path = "." + APPLICATION_ID + "/" + OFFICE_EXTENSION_ID + "/";
      directory = new File(userHome, path);
    }
    if (directory != null && !directory.exists()) {
      directory.mkdirs();
    }
    return directory;
  }
  
  /**
   * Returns log file 
   */
  public static String getLogFilePath() {
    return getLogFilePath(null);
  }

  public static String getLogFilePath(XComponentContext xContext) {
    return new File(getLOConfigDir(xContext), LOG_FILE).getAbsolutePath();
  }
  
  /**
   * Returns statistical analyzes configuration file 
   */
  public static String getStatisticalConfigFilePath() {
    return new File(getLOConfigDir(), STATISTICAL_ANALYZES_CONFIG_FILE).getAbsolutePath();
  }

  /**
   * Returns directory to saves caches
   * @since 5.2
   */
  public static File getCacheDir() {
    return getCacheDir(null);
  }
  
  public static File getCacheDir(XComponentContext xContext) {
    File cacheDir = new File(getLOConfigDir(xContext), CACHE_ID);
    if (cacheDir != null && !cacheDir.exists()) {
      cacheDir.mkdirs();
    }
    return cacheDir;
  }
  
  private static double getMaxHeapSpace() {
    if(MAX_HEAP_SPACE < 0) {
      MAX_HEAP_SPACE = Runtime.getRuntime().maxMemory();
    }
    return MAX_HEAP_SPACE;
  }
  
  private static double getHeapLimit(double maxHeap) {
    if(LT_HEAP_LIMIT < 0) {
      LT_HEAP_LIMIT = maxHeap * LT_HEAP_LIMIT_FACTOR;
    }
    return LT_HEAP_LIMIT;
  }
  
  public static double getCurrentHeapRatio() {
    return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / LT_HEAP_LIMIT;
  }
  
  public static boolean isHeapLimitReached() {
    long usedHeap = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    return (LT_HEAP_LIMIT < usedHeap);
  }
  
  /**
   * Get information about Java as String
   */
  public static String getJavaInformation () {
    return "Java-Version: " + System.getProperty("java.version") + ", max. Heap-Space: " + ((int) (getMaxHeapSpace()/1048576)) +
        " MB, LT Heap Space Limit: " + ((int) (getHeapLimit(getMaxHeapSpace())/1048576)) + " MB";
  }

  /**
   * Get information about LanguageTool
   */
  public static String getLtInformation () {
    String txt = JLanguageTool.VERSION;
    if (JLanguageTool.VERSION.contains("SNAPSHOT")) {
      txt += " - " + JLanguageTool.BUILD_DATE + ", " + JLanguageTool.GIT_SHORT_ID;
    }
    return txt;
  }

  /**
   * Get LanguageTool Image
   */
  public static Image getLtImage() {
    try {
      URL url = OfficeTools.class.getResource("/images/LanguageToolSmall.png");
      return ImageIO.read(url);
    } catch (IOException e) {
      MessageHandler.showError(e);
    }
    return null;
  }
  
  /**
   * Get LanguageTool Image
   */
  public static ImageIcon getLtImageIcon(boolean big) {
    URL url;
    if (big) {
      url = OfficeTools.class.getResource("/images/LanguageToolBig.png");
    } else {
      url = OfficeTools.class.getResource("/images/LanguageToolSmall.png");
    }
    return new ImageIcon(url);
  }

  /**
   * get information of LO/OO office product
   */
  public static OfficeProductInfo getOfficeProductInfo(XComponentContext xContext) {
    if (OFFICE_PRODUCT_INFO == null) {
      OFFICE_PRODUCT_INFO = readOfficeProductInfo(xContext);
    }
    return OFFICE_PRODUCT_INFO;
  }

  /**
   * read information of LO/OO office product from system
   */
  private static OfficeProductInfo readOfficeProductInfo(XComponentContext xContext) {
    try {
      if (xContext == null) {
        return null;
      }
      XMultiServiceFactory xMSF = UnoRuntime.queryInterface(XMultiServiceFactory.class, xContext.getServiceManager());
      if (xMSF == null) {
        MessageHandler.printToLogFile("XMultiServiceFactory == null");
        return null;
      }
      Object oConfigProvider = xMSF.createInstance("com.sun.star.configuration.ConfigurationProvider");
      XMultiServiceFactory confMsf = UnoRuntime.queryInterface(XMultiServiceFactory.class, oConfigProvider);

      final String sView = "com.sun.star.configuration.ConfigurationAccess";

      Object args[] = new Object[1];
      PropertyValue aPathArgument = new PropertyValue();
      aPathArgument.Name = "nodepath";
      aPathArgument.Value = "org.openoffice.Setup/Product";
      args[0] = aPathArgument;
      Object oConfigAccess =  confMsf.createInstanceWithArguments(sView, args);
      XNameAccess xName = UnoRuntime.queryInterface(XNameAccess.class, oConfigAccess);
      
      aPathArgument.Value = "org.openoffice.Setup/L10N";
      Object oConfigAccess1 =  confMsf.createInstanceWithArguments(sView, args);
      
      XNameAccess xName1 = UnoRuntime.queryInterface(XNameAccess.class, oConfigAccess1);
      
      return (new OfficeProductInfo(xName.getByName("ooName"), xName.getByName("ooSetupVersion"), 
          xName.getByName("ooSetupExtension"), xName.getByName("ooVendor"), xName1.getByName("ooLocale"), System.getProperty("os.arch")));
      
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;           // Return null as method failed
    }

  }
  
  /**
   * Get a boolean value from an Object
   */
  public static boolean getBooleanValue(Object o) {
    if (o != null && o instanceof Boolean) {
      return ((Boolean) o).booleanValue();
    }
    return false;
  }
  
  /**
   * timestamp for last key release
   *//*
  public static void setKeyReleaseTime(long time) {
    lastKeyRelease = time;
  }
*/
  public static void waitForLO() {
    while (DocumentCursorTools.isBusy() || ViewCursorTools.isBusy() || FlatParagraphTools.isBusy()) {
      try {
        synchronized (waitObj) {
          numLoWaits++;
          if (numLoWaits > MAX_LO_WAITS) {
            MessageHandler.printToLogFile("waitForLO: Wait for more than " + MAX_LO_WAITS/100 + " seconds, "
                + "DocumentCursorTools.isBusy: " + DocumentCursorTools.isBusy() + ", "
                + "ViewCursorTools.isBusy: " + ViewCursorTools.isBusy() + ", "
                + "FlatParagraphTools.isBusy: " + FlatParagraphTools.isBusy() + ": "
                + "Free Lock and continue.");
            if (DocumentCursorTools.isBusy()) {
              DocumentCursorTools.reset();
            }
            if (ViewCursorTools.isBusy()) {
              ViewCursorTools.reset();
            }
            if (FlatParagraphTools.isBusy()) {
              FlatParagraphTools.reset();
            }
          }
        }
        Thread.sleep(10);
      } catch (InterruptedException e) {
        MessageHandler.printException(e);
      }
    }
  }
/*  
  public static void waitForLoDic() {
    long spellDiff = KEY_RELEASE_TOLERANCE - System.currentTimeMillis() + lastKeyRelease;
    while (DocumentCursorTools.isBusy() || ViewCursorTools.isBusy() || FlatParagraphTools.isBusy() || spellDiff > 0) {
      try {
        Thread.sleep(spellDiff < 10 ? 10 : spellDiff);
      } catch (InterruptedException e) {
        MessageHandler.printException(e);
      }
      spellDiff = KEY_RELEASE_TOLERANCE - System.currentTimeMillis() + lastKeyRelease;
    }
  }
*/  
  public static void waitForLtDictionary() {
    while (LtDictionary.isActivating()) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        MessageHandler.printException(e);
      }
    }
  }
  
  public static boolean hasStatisticalStyleRules(Language lang) {
    try {
      for (Rule rule : lang.getRelevantRules(JLanguageTool.getMessageBundle(), null, lang, null)) {
        if (rule instanceof AbstractStatisticSentenceStyleRule || rule instanceof AbstractStatisticStyleRule ||
            rule instanceof ReadabilityRule || rule instanceof AbstractStyleTooOftenUsedWordRule) {
          return true;
        }
      }
    } catch (IOException e) {
    }
    return false;
  }
  
  /**
   * Handle logLevel for debugging and development
   */
  static void setLogLevel(String logLevel) {
    if (logLevel != null) {
      String[] levels = logLevel.split(LOG_DELIMITER);
      for (String level : levels) {
        if (level.equals("1") || level.equals("2") || level.equals("3") || level.startsWith("all:")) {
          int numLevel;
          if (level.startsWith("all:")) {
            String[] levelAll = level.split(":");
            if (levelAll.length != 2) {
              continue;
            }
            numLevel = Integer.parseInt(levelAll[1]);
          } else {
            numLevel = Integer.parseInt(level);
          }
          if (numLevel > 0) {
            DEBUG_MODE_MD = true;
            DEBUG_MODE_TQ = true;
            if (DEBUG_MODE_SD == 0) {
              DEBUG_MODE_SD = numLevel;
            }
            if (DEBUG_MODE_SC == 0) {
              DEBUG_MODE_SC = numLevel;
            }
            if (DEBUG_MODE_CR == 0) {
              DEBUG_MODE_CR = numLevel;
            }
          }
          if (numLevel > 1) {
            DEBUG_MODE_DC = true;
            DEBUG_MODE_LM = true;
          }
          if (numLevel > 2) {
            DEBUG_MODE_FP = true;
          }
        } else if (level.startsWith("sd:") || level.startsWith("sc:") || level.startsWith("cr:")) {
          String[] levelSD = level.split(":");
          if (levelSD.length != 2) {
            continue;
          }
          int numLevel = Integer.parseInt(levelSD[1]);
          if (numLevel > 0) {
            if (levelSD[0].equals("sd")) {
              DEBUG_MODE_SD = numLevel;
            } else if (levelSD[0].equals("sc")) {
              DEBUG_MODE_SC = numLevel;
            } else if (levelSD[0].equals("cr")) {
              DEBUG_MODE_CR = numLevel;
            }
          }
        } else if (level.equals("md")) {
          DEBUG_MODE_MD = true;
        } else if (level.equals("dc")) {
          DEBUG_MODE_DC = true;
        } else if (level.equals("fp")) {
          DEBUG_MODE_FP = true;
        } else if (level.equals("lm")) {
          DEBUG_MODE_LM = true;
        } else if (level.equals("tq")) {
          DEBUG_MODE_TQ = true;
        } else if (level.equals("ld")) {
          DEBUG_MODE_LD = true;
        } else if (level.equals("cd")) {
          DEBUG_MODE_CD = true;
        } else if (level.equals("io")) {
          DEBUG_MODE_IO = true;
        } else if (level.equals("sr")) {
          DEBUG_MODE_SR = true;
        } else if (level.startsWith("tm")) {
          String[] levelTm = level.split(":");
          if (levelTm[0].equals("tm")) {
            DEBUG_MODE_TM = true;
            if(levelTm.length > 1) {
              int time = Integer.parseInt(levelTm[1]);
              if (time >= 0) {
                TIME_TOLERANCE = time;
              }
            }
          }
        } else if (level.equals("st")) {
          DEVELOP_MODE_ST = true;
        } else if (level.equals("dev")) {
          DEVELOP_MODE = true;
        }
      }
    }
  }

  public static class OfficeProductInfo {
    public final String ooName;
    public final String ooVersion;
    public final String ooExtension;
    public final String ooVendor;
    public final String ooLocale;
    public final String osArch;
    
    OfficeProductInfo(Object name, Object version, Object extension, Object vendor, Object locale, Object arch) {
      ooName = new String((String) name);
      ooVersion = new String((String) version);
      ooExtension = new String((String) extension);
      ooVendor = new String((String) vendor);
      ooLocale = new String((String) locale);
      osArch = new String((String) arch);
    }
    
    OfficeProductInfo(String name, String version, String extension, String vendor, String locale, String arch) {
      ooName = new String(name);
      ooVersion = new String(version);
      ooExtension = new String(extension);
      ooVendor = new String(vendor);
      ooLocale = new String(locale);
      osArch = new String(arch);
    }
  }
}
