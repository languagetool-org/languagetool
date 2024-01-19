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

import java.util.Arrays;
import java.util.ResourceBundle;

import org.languagetool.JLanguageTool;
import org.languagetool.gui.Configuration;

import com.sun.star.awt.Point;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XIndexContainer;
import com.sun.star.frame.XController;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XLayoutManager;
import com.sun.star.frame.XModel;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ui.DockingArea;
import com.sun.star.ui.ItemStyle;
import com.sun.star.ui.ItemType;
import com.sun.star.ui.UIElementType;
import com.sun.star.ui.XModuleUIConfigurationManagerSupplier;
import com.sun.star.ui.XUIConfigurationManager;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/** 
 *  Class to add a dynamic LanguageTool Toolbar
 *  since 6.4
 */
public class LtToolbar {
  
  private static final ResourceBundle MESSAGES = JLanguageTool.getMessageBundle();
  private static final String WRITER_SERVICE = "com.sun.star.text.TextDocument";
  private static final String LT_TOOLBAR_URL = "private:resource/toolbar/org.languagetool.openoffice.Main.toolbar";
  private XComponentContext xContext;
  private SingleDocument document;

  LtToolbar(XComponentContext xContext, SingleDocument document) {
    this.xContext = xContext;
    this.document = document;
    makeToolbar();
  }
  
  public void makeToolbar() {
    try {
      Configuration config = document.getMultiDocumentsHandler().getConfiguration();
      XUIConfigurationManager confMan = getUIConfigManagerDoc(xContext);
      if (confMan == null) {
        MessageHandler.printToLogFile("Cannot create configuration manager");
        return;
      }
      
      String toolbarName = LT_TOOLBAR_URL;
      
      XIndexContainer elementsContainer = confMan.createSettings();

      boolean hasStatisticalStyleRules;
      if (document.getMultiDocumentsHandler().isBackgroundCheckOff()) {
        hasStatisticalStyleRules = false;
      } else {
        hasStatisticalStyleRules = OfficeTools.hasStatisticalStyleRules(document.getLanguage());
      }
      
      int j = 0;
      PropertyValue[] itemProps = makeBarItem(LtMenus.LT_NEXT_ERROR_COMMAND, MESSAGES.getString("loMenuNextError"));
      elementsContainer.insertByIndex(j, itemProps);
      j++;
      itemProps = makeBarItem(LtMenus.LT_CHECKDIALOG_COMMAND, MESSAGES.getString("checkTextShortDesc"));
      elementsContainer.insertByIndex(j, itemProps);
      j++;
      itemProps = makeBarItem(LtMenus.LT_CHECKAGAINDIALOG_COMMAND, MESSAGES.getString("loMenuGrammarCheckAgain"));
      elementsContainer.insertByIndex(j, itemProps);
      j++;
      itemProps = makeBarItem(LtMenus.LT_REFRESH_CHECK_COMMAND, MESSAGES.getString("loContextMenuRefreshCheck"));
      elementsContainer.insertByIndex(j, itemProps);
      if (hasStatisticalStyleRules) {
        j++;
        itemProps = makeBarItem(LtMenus.LT_STATISTICAL_ANALYSES_COMMAND, MESSAGES.getString("loStatisticalAnalysis"));
        elementsContainer.insertByIndex(j, itemProps);
      }
      j++;
      if (document.getMultiDocumentsHandler().isBackgroundCheckOff()) {
        itemProps = makeBarItem(LtMenus.LT_BACKGROUND_CHECK_ON_COMMAND, MESSAGES.getString("loMenuEnableBackgroundCheck"));
      } else {
        itemProps = makeBarItem(LtMenus.LT_BACKGROUND_CHECK_OFF_COMMAND, MESSAGES.getString("loMenuDisableBackgroundCheck"));
      }
      elementsContainer.insertByIndex(j, itemProps);
      j++;
      itemProps = makeBarItem(LtMenus.LT_RESET_IGNORE_PERMANENT_COMMAND, MESSAGES.getString("loMenuResetIgnorePermanent"));
      elementsContainer.insertByIndex(j, itemProps);
/*        TODO: Add sub toolbars:
      if(!document.getMultiDocumentsHandler().getDisabledRulesMap(null).isEmpty()) {
        j++;
        itemProps = makeBarItem(LtMenus.LT_ACTIVATE_RULES_COMMAND, MESSAGES.getString("loContextMenuActivateRule"));
        elementsContainer.insertByIndex(j, itemProps);
      }
      if(config.getDefinedProfiles().size() > 1) {
        j++;
        itemProps = makeBarItem(LtMenus.LT_PROFILES_COMMAND, MESSAGES.getString("loMenuChangeProfiles"));
        elementsContainer.insertByIndex(j, itemProps);
      }
*/
      j++;
      itemProps = makeBarItem(LtMenus.LT_OPTIONS_COMMAND, MESSAGES.getString("loContextMenuOptions"));
      elementsContainer.insertByIndex(j, itemProps);
      j++;
      itemProps = makeBarItem(LtMenus.LT_ABOUT_COMMAND, MESSAGES.getString("loContextMenuAbout"));
      elementsContainer.insertByIndex(j, itemProps);
      
      if (confMan.hasSettings(toolbarName)) {
        confMan.replaceSettings(toolbarName, elementsContainer);
      } else {
        confMan.insertSettings(toolbarName, elementsContainer);
      }
/*        
      setToolbarName(confMan, LT_TOOLBAR_URL, "LanguageTool");
      for (PropertyValue[] propValList : confMan.getUIElementsInfo(UIElementType.TOOLBAR)) {
        MessageHandler.printToLogFile("\n");
        for (PropertyValue propVal : propValList) {
          MessageHandler.printToLogFile("Property: Name: " + propVal.Name + ", Handle: " + propVal.Handle 
            + ", Value: " + propVal.Value + ", State: " + propVal.State);
        }
      }
*/        
      XLayoutManager layoutManager = getLayoutManager();
      boolean exist = false;
      boolean isLocked = false;
      Point pos = null;
      
      if (layoutManager.getElement(toolbarName) != null) {
        exist = true;
        isLocked = layoutManager.isElementLocked(toolbarName);
        pos = layoutManager.getElementPos(toolbarName);
      }
      layoutManager.destroyElement(toolbarName);
      layoutManager.createElement(toolbarName);
      if (exist) {
        layoutManager.setElementPos(toolbarName, pos);
        if (isLocked) {
          layoutManager.lockWindow(toolbarName);
        }
      } else {
        layoutManager.dockWindow(toolbarName, DockingArea.DOCKINGAREA_RIGHT, new Point(1,0));
      }
      layoutManager.showElement(toolbarName);
/*
      XUIElement oLtBar = layoutManager.getElement(toolbarName);
      XUIElementSettings oLtBarSettings = UnoRuntime.queryInterface(XUIElementSettings.class, oLtBar);
      XIndexAccess oLtBarAccess = oLtBarSettings.getSettings(true);
      for (int i = 0; i < oLtBarAccess.getCount(); i++) {
        MessageHandler.printToLogFile("");
        PropertyValue[] propVal = (PropertyValue[]) oLtBarAccess.getByIndex(i);
        for (int k = 0; k < propVal.length; k++) {
          MessageHandler.printToLogFile(i + ".: Property: Name: " + propVal[k].Name + ", Handle: " + propVal[k].Handle 
              + ", Value: " + propVal[k].Value + ", State: " + propVal[k].State);
//          MessageHandler.printToLogFile("Access (" + i + "): " + oLtBarAccess.getByIndex(i));
        }
      }
      MessageHandler.printToLogFile("");
      XPropertySet props = UnoRuntime.queryInterface(XPropertySet.class, oLtBar);
      for (Property property : props.getPropertySetInfo().getProperties()) {
        MessageHandler.printToLogFile("Property: Name: " + property.Name + ", type: " + property.Type 
            + ", Value: " + props.getPropertyValue(property.Name));
      }
*/      
//      printUICmds(confMan, toolbarName);

//      MessageHandler.printToLogFile("XUIConfigurationManager created!");

    } catch (java.lang.Exception e) {
      MessageHandler.printException(e);
    }
  }

  private void setToolbarName(XUIConfigurationManager confMan, String toolbarUrl, String name) 
                  throws IllegalArgumentException {
    for (PropertyValue[] propValList : confMan.getUIElementsInfo(UIElementType.TOOLBAR)) {
      if ("ResourceURL".equals(propValList[0].Name) && toolbarUrl.equals(propValList[0].Value)
          && "UIName".equals(propValList[1].Name)) {
        propValList[1].Value = new String(name);
        return;
      }
    }
  }
  
  private PropertyValue[] makeBarItem(String cmd, String itemName) {
    // propertiees for a toolbar item using a name and an image
    // problem: image does not appear next to text on toolbar
    PropertyValue[] props = new PropertyValue[5];

    props[0] = new PropertyValue();
    props[0].Name = "CommandURL";
    props[0].Value = cmd;

    props[1] = new PropertyValue();
    props[1].Name = "Label";
    props[1].Value = itemName;

    props[2] = new PropertyValue();
    props[2].Name = "Type";
    props[2].Value = ItemType.DEFAULT;  // 0;

    props[3] = new PropertyValue();
    props[3].Name = "Visible";
    props[3].Value = true;

    props[4] = new PropertyValue();
    props[4].Name = "Style";
    props[4].Value = ItemStyle.DRAW_FLAT + ItemStyle.ALIGN_LEFT + 
                     ItemStyle.AUTO_SIZE + ItemStyle.ICON;
//                         + ItemStyle.TEXT;

    return props;
  }
  
  private XUIConfigurationManager getUIConfigManagerDoc(XComponentContext xContext) throws Exception {

    XMultiComponentFactory mcFactory = xContext.getServiceManager();
  
    Object o = mcFactory.createInstanceWithContext("com.sun.star.ui.ModuleUIConfigurationManagerSupplier", xContext);     
    XModuleUIConfigurationManagerSupplier xSupplier = 
        UnoRuntime.queryInterface(XModuleUIConfigurationManagerSupplier.class, o);
  
    return xSupplier.getUIConfigurationManager(WRITER_SERVICE);
  }
  
  private XLayoutManager getLayoutManager() {
    try {
      XComponent xComponent = OfficeTools.getCurrentComponent(xContext);
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
      return UnoRuntime.queryInterface(XLayoutManager.class,  propSet.getPropertyValue("LayoutManager"));
    } catch (Exception e) {
      MessageHandler.printException(e);
    }
    return null;
  }

  public static void printUICmds(XUIConfigurationManager configMan, String uiElemName) 
      throws IllegalArgumentException, NoSuchElementException, IndexOutOfBoundsException, WrappedTargetException {
    // print every command used by the toolbar whose resource name is uiElemName
    XIndexAccess settings = configMan.getSettings(uiElemName, true);
    int numSettings = settings.getCount();
    MessageHandler.printToLogFile("No. of elements in \"" + uiElemName + "\" toolbar: " + numSettings);
    for (int i = 0; i < numSettings; i++) { 
      PropertyValue[] settingProps =  UnoRuntime.queryInterface(PropertyValue[].class, settings.getByIndex(i));
      // Props.showProps("Settings " + i, settingProps);
      MessageHandler.printToLogFile("Properties for \"Settings" + i + "\":");
      if (settingProps == null)
        MessageHandler.printToLogFile("  none found");
      else {
        for (PropertyValue prop : settingProps) {
          MessageHandler.printToLogFile("  " + prop.Name + ": " + propValueToString(prop.Value));
        }
        MessageHandler.printToLogFile("");
      }
    }
  //    Object val = Props.getValue("CommandURL", settingProps);
  //    MessageHandler.printToLogFile(i + ") " + propValueToString(val));
  }

  public static String propValueToString(Object val) {
    if (val == null) {
      return null;
    }
    if (val instanceof String[]) {
      return Arrays.toString((String[])val);
    } else if (val instanceof PropertyValue[]) {
      PropertyValue[] ps = (PropertyValue[])val;
      StringBuilder sb = new StringBuilder("[");
      for (PropertyValue p : ps) {
        sb.append("    " + p.Name + " = " + p.Value);
      }
      sb.append("  ]");
      return sb.toString();
    } else {
      return val.toString();
    }
  }
  
}

