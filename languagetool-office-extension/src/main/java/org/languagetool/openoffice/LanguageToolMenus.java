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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.languagetool.JLanguageTool;
import org.languagetool.gui.Configuration;
import org.languagetool.openoffice.OfficeTools.DocumentType;
import org.languagetool.openoffice.stylestatistic.StatAnDialog;

import com.sun.star.awt.MenuEvent;
import com.sun.star.awt.MenuItemStyle;
import com.sun.star.awt.XMenuBar;
import com.sun.star.awt.XMenuListener;
import com.sun.star.awt.XPopupMenu;
import com.sun.star.beans.Property;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XIndexContainer;
import com.sun.star.frame.XController;
import com.sun.star.frame.XModel;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.XTextRange;
import com.sun.star.ui.ActionTriggerSeparatorType;
import com.sun.star.ui.ContextMenuExecuteEvent;
import com.sun.star.ui.ContextMenuInterceptorAction;
import com.sun.star.ui.XContextMenuInterception;
import com.sun.star.ui.XContextMenuInterceptor;
import com.sun.star.uno.Any;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.view.XSelectionSupplier;

/**
 * Class of menus adding dynamic components 
 * to header menu and to context menu
 * @since 5.0
 * @author Fred Kruse
 */
public class LanguageToolMenus {
  
  private static final ResourceBundle MESSAGES = JLanguageTool.getMessageBundle();
  private static final int SUBMENU_ID_DIFF = 21;

  private static boolean debugMode;   //  should be false except for testing
  private static boolean debugModeTm;  //  should be false except for testing
  private static boolean isRunning = false;
  
  private XComponentContext xContext;
  private XComponent xComponent;
  private SingleDocument document;
  private Configuration config;
  private boolean isRemote;
  private LTHeadMenu ltHeadMenu;
  @SuppressWarnings("unused")
  private ContextMenuInterceptor ltContextMenu;

  LanguageToolMenus(XComponentContext xContext, XComponent xComponent, SingleDocument document, Configuration config) {
    debugMode = OfficeTools.DEBUG_MODE_LM;
    debugModeTm = OfficeTools.DEBUG_MODE_TM;
    this.document = document;
    this.xContext = xContext;
    this.xComponent = xComponent;
    setConfigValues(config);
    if (document.getDocumentType() == DocumentType.WRITER) {
      ltHeadMenu = new LTHeadMenu(xComponent);
    }
    ltContextMenu = new ContextMenuInterceptor(xComponent);
    if (debugMode) {
      MessageHandler.printToLogFile("LanguageToolMenus initialised");
    }
  }
  
  void setConfigValues(Configuration config) {
    this.config = config;
    isRemote = config.doRemoteCheck();
  }
  
  void removeListener() {
    ltHeadMenu.removeListener();
  }
  
  void addListener() {
    ltHeadMenu.addListener();
  }
  
  /**
   * Class to add or change some items of the LT head menu
   */
  private class LTHeadMenu implements XMenuListener {
    // If anything on the position of LT menu is changed the following has to be changed
    private static final String TOOLS_COMMAND = ".uno:ToolsMenu";             //  Command to open tools menu
    private static final String COMMAND_BEFORE_LT_MENU = ".uno:LanguageMenu";   //  Command for Language Menu (LT menu is installed after)
                                                      //  Command to Switch Off/On LT
    private static final String LT_STATISTICAL_ANALYSES_COMMAND = "service:org.languagetool.openoffice.Main?statisticalAnalyses";   
    private static final String LT_RESET_IGNORE_PERMANENT_COMMAND = "service:org.languagetool.openoffice.Main?resetIgnorePermanent";   
    private static final String LT_TOGGLE_BACKGROUND_CHECK_COMMAND = "service:org.languagetool.openoffice.Main?toggleBackgroundCheck";   
    private static final String LT_Options_COMMAND = "service:org.languagetool.openoffice.Main?configure";   
    private static final String LT_PROFILE_COMMAND = "service:org.languagetool.openoffice.Main?profileChangeTo:";
    private final static String LT_ACTIVATE_RULE = "service:org.languagetool.openoffice.Main?activateRule_";
    
    XPopupMenu ltMenu = null;
    short toolsId = 0;
    short ltId = 0;
    short switchOffId = 0;
    short switchOffPos = 0;
    private XPopupMenu toolsMenu = null;
    private XPopupMenu xProfileMenu = null;
    private XPopupMenu xActivateRuleMenu = null;
    private List<String> definedProfiles = null;
    private String currentProfile = null;
    
    public LTHeadMenu(XComponent xComponent) {
      XMenuBar menubar = null;
      menubar = OfficeTools.getMenuBar(xComponent);
      if (menubar == null) {
        MessageHandler.printToLogFile("LanguageToolMenus: LTHeadMenu: Menubar is null");
        return;
      }
      for (short i = 0; i < menubar.getItemCount(); i++) {
        toolsId = menubar.getItemId(i);
        String command = menubar.getCommand(toolsId);
        if (TOOLS_COMMAND.equals(command)) {
          toolsMenu = menubar.getPopupMenu(toolsId);
          break;
        }
      }
      if (toolsMenu == null) {
        MessageHandler.printToLogFile("LanguageToolMenus: LTHeadMenu: Tools Menu is null");
        return;
      }
      for (short i = 0; i < toolsMenu.getItemCount(); i++) {
        String command = toolsMenu.getCommand(toolsMenu.getItemId(i));
        if (COMMAND_BEFORE_LT_MENU.equals(command)) {
          ltId = toolsMenu.getItemId((short) (i + 1));
          ltMenu = toolsMenu.getPopupMenu(ltId);
          break;
        }
      }
      if (ltMenu == null) {
        MessageHandler.printToLogFile("LanguageToolMenus: LTHeadMenu: LT Menu is null");
        return;
      }
      
      for (short i = 0; i < ltMenu.getItemCount(); i++) {
        String command = ltMenu.getCommand(ltMenu.getItemId(i));
        if (LT_Options_COMMAND.equals(command)) {
          switchOffId = (short)102;
          switchOffPos = (short)(i - 1);
          break;
        }
      }
      if (switchOffId == 0) {
        MessageHandler.printToLogFile("LanguageToolMenus: LTHeadMenu: switchOffId not found");
        return;
      }
      ltMenu.insertItem((short)(switchOffId + 1), MESSAGES.getString("loStatisticalAnalysis") + " ...", 
          (short)0, switchOffPos);
      ltMenu.setCommand(switchOffId, LT_STATISTICAL_ANALYSES_COMMAND);
      switchOffPos++;
      ltMenu.insertItem(switchOffId, MESSAGES.getString("loMenuResetIgnorePermanent"), (short)0, switchOffPos);
      ltMenu.setCommand(switchOffId, LT_RESET_IGNORE_PERMANENT_COMMAND);
      switchOffId--;
      switchOffPos++;
      ltMenu.insertItem(switchOffId, MESSAGES.getString("loMenuEnableBackgroundCheck"), (short)0, switchOffPos);
      ltMenu.setCommand(switchOffId, LT_TOGGLE_BACKGROUND_CHECK_COMMAND);
      toolsMenu.addMenuListener(this);
      ltMenu.addMenuListener(this);
      if (debugMode) {
        MessageHandler.printToLogFile("LanguageToolMenus: LTHeadMenu: Menu listener set");
      }
    }
    
    void removeListener() {
      toolsMenu.removeMenuListener(this);
    }
    
    void addListener() {
      toolsMenu.addMenuListener(this);
    }
    
    /**
     * Set the dynamic parts of the LT menu
     * placed as submenu at the LO/OO tools menu
     */
    private void setLtMenu() {
      if (document.getMultiDocumentsHandler().isBackgroundCheckOff()) {
        ltMenu.setItemText(switchOffId, MESSAGES.getString("loMenuEnableBackgroundCheck"));
      } else {
        ltMenu.setItemText(switchOffId, MESSAGES.getString("loMenuDisableBackgroundCheck"));
      }
      short profilesId = (short)(switchOffId + 10);
      short profilesPos = (short)(switchOffPos + 2);
      if (ltMenu.getItemId(profilesPos) != profilesId) {
        setProfileMenu(profilesId, profilesPos);
      }
      int nProfileItems = setProfileItems();
      setActivateRuleMenu((short)(switchOffPos + 3), (short)(switchOffId + 11), (short)(switchOffId + SUBMENU_ID_DIFF + nProfileItems));
    }
      
    /**
     * Set the profile menu
     * if there are more than one profiles defined at the LT configuration file
     */
    private void setProfileMenu(short profilesId, short profilesPos) {
      ltMenu.insertItem(profilesId, MESSAGES.getString("loMenuChangeProfiles"), MenuItemStyle.AUTOCHECK, profilesPos);
      xProfileMenu = OfficeTools.getPopupMenu(xContext);
      if (xProfileMenu == null) {
        MessageHandler.printToLogFile("LanguageToolMenus: setProfileMenu: Profile menu == null");
        return;
      }
      
      xProfileMenu.addMenuListener(this);

      ltMenu.setPopupMenu(profilesId, xProfileMenu);
    }
    
    /**
     * Set the items for different profiles 
     * if there are more than one defined at the LT configuration file
     */
    private int setProfileItems() {
      currentProfile = config.getCurrentProfile();
      definedProfiles = config.getDefinedProfiles();
      definedProfiles.sort(null);
      if (xProfileMenu != null) {
        xProfileMenu.removeItem((short)0, xProfileMenu.getItemCount());
        short nId = (short) (switchOffId + SUBMENU_ID_DIFF);
        short nPos = 0;
        xProfileMenu.insertItem(nId, MESSAGES.getString("guiUserProfile"), (short) 0, nPos);
        xProfileMenu.setCommand(nId, LT_PROFILE_COMMAND);
        if (currentProfile == null || currentProfile.isEmpty()) {
          xProfileMenu.enableItem(nId , false);
        } else {
          xProfileMenu.enableItem(nId , true);
        }
        if (definedProfiles != null) {
          for (int i = 0; i < definedProfiles.size(); i++) {
            nId++;
            nPos++;
            xProfileMenu.insertItem(nId, definedProfiles.get(i), (short) 0, nPos);
            xProfileMenu.setCommand(nId, LT_PROFILE_COMMAND + definedProfiles.get(i));
            if (currentProfile != null && currentProfile.equals(definedProfiles.get(i))) {
              xProfileMenu.enableItem(nId , false);
            } else {
              xProfileMenu.enableItem(nId , true);
            }
          }
        }
      }
      return (definedProfiles == null ? 1 : definedProfiles.size() + 1);
    }

    /**
     * Run the actions defined in the profile menu
     */
    private void runProfileAction(String profile) {
      List<String> definedProfiles = config.getDefinedProfiles();
      if (profile != null && (definedProfiles == null || !definedProfiles.contains(profile))) {
        MessageHandler.showMessage("profile '" + profile + "' not found");
      } else {
        try {
          List<String> saveProfiles = new ArrayList<>();
          saveProfiles.addAll(config.getDefinedProfiles());
          config.initOptions();
          config.loadConfiguration(profile == null ? "" : profile);
          config.setCurrentProfile(profile);
          config.addProfiles(saveProfiles);
          config.saveConfiguration(document.getLanguage());
          document.getMultiDocumentsHandler().resetConfiguration();
        } catch (IOException e) {
          MessageHandler.showError(e);
        }
      }
    }
    
    /**
     * Set Activate Rule Submenu
     */
    
    private void setActivateRuleMenu(short pos, short id, short submenuStartId) {
      Map<String, String> deactivatedRulesMap = document.getMultiDocumentsHandler().getDisabledRulesMap(null);
      if (!deactivatedRulesMap.isEmpty()) {
        if (ltMenu.getItemId(pos) != id) {
          ltMenu.insertItem(id, MESSAGES.getString("loContextMenuActivateRule"), MenuItemStyle.AUTOCHECK, pos);
          xActivateRuleMenu = OfficeTools.getPopupMenu(xContext);
          if (xActivateRuleMenu == null) {
            MessageHandler.printToLogFile("LanguageToolMenus: setActivateRuleMenu: activate rule menu == null");
            return;
          }
          xActivateRuleMenu.addMenuListener(this);
          ltMenu.setPopupMenu(id, xActivateRuleMenu);
        }
        xActivateRuleMenu.removeItem((short) 0, xActivateRuleMenu.getItemCount());
        short nId = submenuStartId;
        short nPos = 0;
        for (String ruleId : deactivatedRulesMap.keySet()) {
          xActivateRuleMenu.insertItem(nId, deactivatedRulesMap.get(ruleId), (short) 0, nPos);
          xActivateRuleMenu.setCommand(nId, LT_ACTIVATE_RULE + ruleId);
          xActivateRuleMenu.enableItem(nId , true);
          nId++;
          nPos++;
        }
      } else if (xActivateRuleMenu != null) {
        ltMenu.removeItem(pos, (short)1);
        xActivateRuleMenu.removeItem((short) 0, xActivateRuleMenu.getItemCount());
        xActivateRuleMenu = null;
      }
    }

    @Override
    public void disposing(EventObject event) {
    }
    @Override
    public void itemActivated(MenuEvent event) {
      if (event.MenuId == 0) {
        setLtMenu();
      }
    }
    @Override
    public void itemDeactivated(MenuEvent event) {
    }
    @Override
    public void itemHighlighted(MenuEvent event) {
    }
    @Override
    public void itemSelected(MenuEvent event) {
      try {
        if (debugMode) {
          MessageHandler.printToLogFile("LanguageToolMenus: setActivateRuleMenu: event id: " + ((int)event.MenuId));
        }
        if (event.MenuId == switchOffId) {
          if (document.getMultiDocumentsHandler().toggleNoBackgroundCheck()) {
            document.getMultiDocumentsHandler().resetCheck(); 
          }
        } else if (event.MenuId == switchOffId + 1) {
          document.resetIgnorePermanent();
        } else if (event.MenuId == switchOffId + 2) {
          StatAnDialog statAnDialog = new StatAnDialog(document);
          statAnDialog.start();
          return;
//          MessageHandler.showMessage("Statistical Analyses is not installed now!");
        } else if (event.MenuId == switchOffId + SUBMENU_ID_DIFF) {
          runProfileAction(null);
        } else if (event.MenuId > switchOffId + SUBMENU_ID_DIFF && event.MenuId <= switchOffId + SUBMENU_ID_DIFF + definedProfiles.size()) {
          runProfileAction(definedProfiles.get(event.MenuId - switchOffId - 22));
        } else if (event.MenuId > switchOffId + SUBMENU_ID_DIFF + definedProfiles.size()) {
          Map<String, String> deactivatedRulesMap = document.getMultiDocumentsHandler().getDisabledRulesMap(null);
          short j = (short)(switchOffId + SUBMENU_ID_DIFF + definedProfiles.size() + 1);
          for (String ruleId : deactivatedRulesMap.keySet()) {
            if(event.MenuId == j) {
              if (debugMode) {
                MessageHandler.printToLogFile("LanguageToolMenus: setActivateRuleMenu: activate rule: " + ruleId);
              }
              document.getMultiDocumentsHandler().activateRule(ruleId);
              return;
            }
            j++;
          }
        }
      } catch (IOException e) {
        MessageHandler.showError(e);
      }
    }

  }

  /** 
   * Class to add a LanguageTool Options item to the context menu
   * since 4.6
   */
  private class ContextMenuInterceptor implements XContextMenuInterceptor {
    
    private final static String IGNORE_ONCE_URL = "slot:201";
    private final static String ADD_TO_DICTIONARY_2 = "slot:2";
    private final static String ADD_TO_DICTIONARY_3 = "slot:3";
    private final static String LT_OPTIONS_URL = "service:org.languagetool.openoffice.Main?configure";
    private final static String LT_IGNORE_ONCE = "service:org.languagetool.openoffice.Main?ignoreOnce";
    private final static String LT_IGNORE_PERMANENT = "service:org.languagetool.openoffice.Main?ignorePermanent";
    private final static String LT_DEACTIVATE_RULE = "service:org.languagetool.openoffice.Main?deactivateRule";
    private final static String LT_ACTIVATE_RULE = "service:org.languagetool.openoffice.Main?activateRule_";
    private final static String LT_REMOTE_HINT = "service:org.languagetool.openoffice.Main?remoteHint";   
    private final static String LT_RENEW_MARKUPS = "service:org.languagetool.openoffice.Main?renewMarkups";
    private final static String LT_ADD_TO_DICTIONARY = "service:org.languagetool.openoffice.Main?addToDictionary_";
    private final static String LT_CHECKDIALOG = "service:org.languagetool.openoffice.Main?checkDialog";

    public ContextMenuInterceptor() {}
    
    public ContextMenuInterceptor(XComponent xComponent) {
      try {
        XModel xModel = UnoRuntime.queryInterface(XModel.class, xComponent);
        if (xModel == null) {
          MessageHandler.printToLogFile("LanguageToolMenus: ContextMenuInterceptor: XModel not found!");
          return;
        }
        XController xController = xModel.getCurrentController();
        if (xController == null) {
          MessageHandler.printToLogFile("LanguageToolMenus: ContextMenuInterceptor: xController == null");
          return;
        }
        XContextMenuInterception xContextMenuInterception = UnoRuntime.queryInterface(XContextMenuInterception.class, xController);
        if (xContextMenuInterception == null) {
          MessageHandler.printToLogFile("LanguageToolMenus: ContextMenuInterceptor: xContextMenuInterception == null");
          return;
        }
        ContextMenuInterceptor aContextMenuInterceptor = new ContextMenuInterceptor();
        XContextMenuInterceptor xContextMenuInterceptor = 
            UnoRuntime.queryInterface(XContextMenuInterceptor.class, aContextMenuInterceptor);
        if (xContextMenuInterceptor == null) {
          MessageHandler.printToLogFile("LanguageToolMenus: ContextMenuInterceptor: xContextMenuInterceptor == null");
          return;
        }
        xContextMenuInterception.registerContextMenuInterceptor(xContextMenuInterceptor);
      } catch (Throwable t) {
        MessageHandler.printException(t);
      }
    }
  
    /**
     * Add LT items to context menu
     */
    @Override
    public ContextMenuInterceptorAction notifyContextMenuExecute(ContextMenuExecuteEvent aEvent) {
      try {
        if (isRunning) {
          MessageHandler.printToLogFile("LanguageToolMenus: notifyContextMenuExecute: is running: no change in Menu");
          return ContextMenuInterceptorAction.IGNORED;
        }
        isRunning = true;
        long startTime = 0;
        if (debugModeTm) {
          startTime = System.currentTimeMillis();
          MessageHandler.printToLogFile("Generate context menu started");
        }
        XIndexContainer xContextMenu = aEvent.ActionTriggerContainer;
        if (debugMode) {
          MessageHandler.printToLogFile("LanguageToolMenus: notifyContextMenuExecute: get xContextMenu");
        }
        
        if (document.getDocumentType() == DocumentType.IMPRESS) {
          XMultiServiceFactory xMenuElementFactory = UnoRuntime.queryInterface(XMultiServiceFactory.class, xContextMenu);
          XPropertySet xNewMenuEntry = UnoRuntime.queryInterface(XPropertySet.class,
              xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
          xNewMenuEntry.setPropertyValue("Text", MESSAGES.getString("loContextMenuGrammarCheck"));
          xNewMenuEntry.setPropertyValue("CommandURL", LT_CHECKDIALOG);
          xContextMenu.insertByIndex(0, xNewMenuEntry);

          XPropertySet xSeparator = UnoRuntime.queryInterface(XPropertySet.class,
              xMenuElementFactory.createInstance("com.sun.star.ui.ActionTriggerSeparator"));
          xSeparator.setPropertyValue("SeparatorType", ActionTriggerSeparatorType.LINE);
          xContextMenu.insertByIndex(1, xSeparator);
          if (debugModeTm) {
            long runTime = System.currentTimeMillis() - startTime;
            if (runTime > OfficeTools.TIME_TOLERANCE) {
              MessageHandler.printToLogFile("Time to generate context menu (Impress): " + runTime);
            }
          }
          isRunning = false;
          if (debugMode) {
            MessageHandler.printToLogFile("LanguageToolMenus: notifyContextMenuExecute: execute modified for Impress");
          }
          return ContextMenuInterceptorAction.EXECUTE_MODIFIED;
        }
        
        int count = xContextMenu.getCount();
/*        
        if (debugMode) {
          for (int i = 0; i < count; i++) {
            Any a = (Any) xContextMenu.getByIndex(i);
            XPropertySet props = (XPropertySet) a.getObject();
            printProperties(props);
          }
        }
*/
        //  Add LT Options Item if a Grammar or Spell error was detected
        document.setMenuDocId();
        for (int i = 0; i < count; i++) {
          Any a = (Any) xContextMenu.getByIndex(i);
          XPropertySet props = (XPropertySet) a.getObject();
          String str = null;
          if (props.getPropertySetInfo().hasPropertyByName("CommandURL")) {
            str = props.getPropertyValue("CommandURL").toString();
          }
          if (str != null && IGNORE_ONCE_URL.equals(str)) {
            int n;  
            for (n = i + 1; n < count; n++) {
              a = (Any) xContextMenu.getByIndex(n);
              XPropertySet tmpProps = (XPropertySet) a.getObject();
              if (tmpProps.getPropertySetInfo().hasPropertyByName("CommandURL")) {
                str = tmpProps.getPropertyValue("CommandURL").toString();
              }
              if (ADD_TO_DICTIONARY_2.equals(str) || ADD_TO_DICTIONARY_3.equals(str)) {
//                ViewCursorTools viewCursor = new ViewCursorTools(xComponent);
//                String wrongWord = viewCursor.getViewCursorSelectedArea();
                String wrongWord = getSelectedWord(aEvent);
                if (debugMode) {
                  MessageHandler.printToLogFile("LanguageToolMenus: notifyContextMenuExecute: wrong word: " + wrongWord);
                }
                if (wrongWord != null && !wrongWord.isEmpty()) {
                  if (wrongWord.charAt(wrongWord.length() - 1) == '.') {
                    wrongWord= wrongWord.substring(0, wrongWord.length() - 1);
                  }
                  if (!wrongWord.isEmpty()) {
                    XMultiServiceFactory xMenuElementFactory = UnoRuntime.queryInterface(XMultiServiceFactory.class, xContextMenu);
                    XIndexContainer xSubMenuContainer = (XIndexContainer)UnoRuntime.queryInterface(XIndexContainer.class,
                        xMenuElementFactory.createInstance("com.sun.star.ui.ActionTriggerContainer"));
                    int j = 0;
                    for (String dict : LtDictionary.getUserDictionaries(xContext)) {
                      XPropertySet xNewSubMenuEntry = UnoRuntime.queryInterface(XPropertySet.class,
                          xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
                      xNewSubMenuEntry.setPropertyValue("Text", dict);
                      xNewSubMenuEntry.setPropertyValue("CommandURL", LT_ADD_TO_DICTIONARY + dict + ":" + wrongWord);
                      xSubMenuContainer.insertByIndex(j, xNewSubMenuEntry);
                      j++;
                    }
                    XPropertySet xNewMenuEntry = UnoRuntime.queryInterface(XPropertySet.class,
                        xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
                    xNewMenuEntry.setPropertyValue("Text", MESSAGES.getString("loContextMenuAddToDictionary"));
                    xNewMenuEntry.setPropertyValue( "SubContainer", (Object)xSubMenuContainer );
                    xContextMenu.removeByIndex(n);
                    xContextMenu.insertByIndex(n, xNewMenuEntry);
                  }
                }
                break;
              }
            }
            if (n >= count) {
              if (document.getCurrentNumberOfParagraph() >= 0) {
                props.setPropertyValue("CommandURL", LT_IGNORE_ONCE);
              }
              XMultiServiceFactory xMenuElementFactory = UnoRuntime.queryInterface(XMultiServiceFactory.class, xContextMenu);

              XPropertySet xNewMenuEntry3 = UnoRuntime.queryInterface(XPropertySet.class,
                  xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
              xNewMenuEntry3.setPropertyValue("Text", MESSAGES.getString("loContextMenuIgnorePermanent"));
              xNewMenuEntry3.setPropertyValue("CommandURL", LT_IGNORE_PERMANENT);
              xContextMenu.insertByIndex(i + 1, xNewMenuEntry3);
              
              XPropertySet xNewMenuEntry1 = UnoRuntime.queryInterface(XPropertySet.class,
                  xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
              xNewMenuEntry1.setPropertyValue("Text", MESSAGES.getString("loContextMenuDeactivateRule"));
              xNewMenuEntry1.setPropertyValue("CommandURL", LT_DEACTIVATE_RULE);
              xContextMenu.insertByIndex(i + 3, xNewMenuEntry1);
              
              int nId = i + 4;
/*              
              Map<String, String> deactivatedRulesMap = document.getMultiDocumentsHandler().getDisabledRulesMap(null);

              if (!deactivatedRulesMap.isEmpty()) {
                XIndexContainer xSubMenuContainer = (XIndexContainer)UnoRuntime.queryInterface(XIndexContainer.class,
                    xMenuElementFactory.createInstance("com.sun.star.ui.ActionTriggerContainer"));
                int j = 0;
                for (String ruleId : deactivatedRulesMap.keySet()) {
                  XPropertySet xNewSubMenuEntry = UnoRuntime.queryInterface(XPropertySet.class,
                      xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
                  xNewSubMenuEntry.setPropertyValue("Text", deactivatedRulesMap.get(ruleId));
                  xNewSubMenuEntry.setPropertyValue("CommandURL", LT_ACTIVATE_RULE + ruleId);
                  xSubMenuContainer.insertByIndex(j, xNewSubMenuEntry);
                  j++;
                }
                
                XPropertySet xNewMenuEntry3 = UnoRuntime.queryInterface(XPropertySet.class,
                    xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
                xNewMenuEntry3.setPropertyValue("Text", MESSAGES.getString("loContextMenuActivateRule"));
                xNewMenuEntry3.setPropertyValue( "SubContainer", (Object)xSubMenuContainer );
                xContextMenu.insertByIndex(i + 3, xNewMenuEntry3);
                nId++;
              }
*/              
              if (isRemote) {
                XPropertySet xNewMenuEntry2 = UnoRuntime.queryInterface(XPropertySet.class,
                    xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
                xNewMenuEntry2.setPropertyValue("Text", MESSAGES.getString("loMenuRemoteInfo"));
                xNewMenuEntry2.setPropertyValue("CommandURL", LT_REMOTE_HINT);
                xContextMenu.insertByIndex(nId, xNewMenuEntry2);
                nId++;
              }
              
              XPropertySet xNewMenuEntry4 = UnoRuntime.queryInterface(XPropertySet.class,
                  xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
              xNewMenuEntry4.setPropertyValue("Text", MESSAGES.getString("loContextMenuRenewMarkups"));
              xNewMenuEntry4.setPropertyValue("CommandURL", LT_RENEW_MARKUPS);
              xContextMenu.insertByIndex(nId, xNewMenuEntry4);
              nId++;

              XPropertySet xNewMenuEntry = UnoRuntime.queryInterface(XPropertySet.class,
                  xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
              xNewMenuEntry.setPropertyValue("Text", MESSAGES.getString("loContextMenuOptions"));
              xNewMenuEntry.setPropertyValue("CommandURL", LT_OPTIONS_URL);
              xContextMenu.insertByIndex(nId, xNewMenuEntry);
              if (debugModeTm) {
                long runTime = System.currentTimeMillis() - startTime;
                if (runTime > OfficeTools.TIME_TOLERANCE) {
                  MessageHandler.printToLogFile("Time to generate context menu (grammar error): " + runTime);
                }
              }
              isRunning = false;
              if (debugMode) {
                MessageHandler.printToLogFile("LanguageToolMenus: notifyContextMenuExecute: execute modified for Writer");
              }
              return ContextMenuInterceptorAction.EXECUTE_MODIFIED;
            }
          }
        }

        //  Add LT Options Item for context menu without grammar error
        XMultiServiceFactory xMenuElementFactory = UnoRuntime.queryInterface(XMultiServiceFactory.class, xContextMenu);
        XPropertySet xSeparator = UnoRuntime.queryInterface(XPropertySet.class,
            xMenuElementFactory.createInstance("com.sun.star.ui.ActionTriggerSeparator"));
        xSeparator.setPropertyValue("SeparatorType", ActionTriggerSeparatorType.LINE);
        xContextMenu.insertByIndex(count, xSeparator);
        
        int nId = count + 1;
        if (isRemote) {
          XPropertySet xNewMenuEntry2 = UnoRuntime.queryInterface(XPropertySet.class,
              xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
          xNewMenuEntry2.setPropertyValue("Text", MESSAGES.getString("loMenuRemoteInfo"));
          xNewMenuEntry2.setPropertyValue("CommandURL", LT_REMOTE_HINT);
          xContextMenu.insertByIndex(nId, xNewMenuEntry2);
          nId++;
        }

        XPropertySet xNewMenuEntry4 = UnoRuntime.queryInterface(XPropertySet.class,
            xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
        xNewMenuEntry4.setPropertyValue("Text", MESSAGES.getString("loContextMenuRenewMarkups"));
        xNewMenuEntry4.setPropertyValue("CommandURL", LT_RENEW_MARKUPS);
        xContextMenu.insertByIndex(nId, xNewMenuEntry4);
        nId++;

        XPropertySet xNewMenuEntry = UnoRuntime.queryInterface(XPropertySet.class,
            xMenuElementFactory.createInstance("com.sun.star.ui.ActionTrigger"));
        xNewMenuEntry.setPropertyValue("Text", MESSAGES.getString("loContextMenuOptions"));
        xNewMenuEntry.setPropertyValue("CommandURL", LT_OPTIONS_URL);
        xContextMenu.insertByIndex(nId, xNewMenuEntry);
        if (debugModeTm) {
          long runTime = System.currentTimeMillis() - startTime;
          if (runTime > OfficeTools.TIME_TOLERANCE) {
            MessageHandler.printToLogFile("Time to generate context menu (no grammar error): " + runTime);
          }
        }
        isRunning = false;
        if (debugMode) {
          MessageHandler.printToLogFile("LanguageToolMenus: notifyContextMenuExecute: execute modified for Writer (no grammar error)");
        }
        return ContextMenuInterceptorAction.EXECUTE_MODIFIED;

      } catch (Throwable t) {
        MessageHandler.printException(t);
      }
      isRunning = false;
      MessageHandler.printToLogFile("LanguageToolMenus: notifyContextMenuExecute: no change in Menu");
      return ContextMenuInterceptorAction.IGNORED;
    }
    
    /**
     * get selected word
     */
    private String getSelectedWord(ContextMenuExecuteEvent aEvent) {
      try {
        XSelectionSupplier xSelectionSupplier = aEvent.Selection;
        Object selection = xSelectionSupplier.getSelection();
        XIndexAccess xIndexAccess = UnoRuntime.queryInterface(XIndexAccess.class, selection);
        if (xIndexAccess == null) {
          MessageHandler.printToLogFile("LanguageToolMenus: getSelectedWord: xIndexAccess == null");
          return null;
        }
        XTextRange xTextRange = UnoRuntime.queryInterface(XTextRange.class, xIndexAccess.getByIndex(0));
        if (xTextRange == null) {
          MessageHandler.printToLogFile("LanguageToolMenus: getSelectedWord: xTextRange == null");
          return null;
        }
        return xTextRange.getString();
      } catch (Throwable t) {
        MessageHandler.printException(t);
      }
      return null;
    }

    /**
     * Print properties in debug mode
     */
    private void printProperties(XPropertySet props) throws UnknownPropertyException, WrappedTargetException {
      Property[] propInfo = props.getPropertySetInfo().getProperties();
      for (Property property : propInfo) {
        MessageHandler.printToLogFile("LanguageToolMenus: Property: Name: " + property.Name + ", Type: " + property.Type);
      }
      if (props.getPropertySetInfo().hasPropertyByName("Text")) {
        MessageHandler.printToLogFile("LanguageToolMenus: Property: Name: " + props.getPropertyValue("Text"));
      }
      if (props.getPropertySetInfo().hasPropertyByName("CommandURL")) {
        MessageHandler.printToLogFile("LanguageToolMenus: Property: CommandURL: " + props.getPropertyValue("CommandURL"));
      }
    }

  }
  

}
