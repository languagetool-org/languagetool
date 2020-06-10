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

import java.io.File;
import java.net.URL;

import com.sun.star.awt.MessageBoxType;
import com.sun.star.awt.Point;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XComboBox;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogEventHandler;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XMessageBox;
import com.sun.star.awt.XMessageBoxFactory;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XURLTransformer;

/**
 * Some tools to create and handle LibreOffice/OpenOffice - dialogs
 * @since 5.1
 * @author Fred Kruse
 */
public class DialogTools {

  final static String DIALOG_RESOURCES = "";
  
  /** 
   * Returns a URL to be used with XDialogProvider to create a dialog 
   */
  public static String convertToURL(XComponentContext xContext, File dialogFile) {
    String sURL = null;
    try {
      com.sun.star.ucb.XFileIdentifierConverter xFileConverter = (com.sun.star.ucb.XFileIdentifierConverter) UnoRuntime
          .queryInterface(com.sun.star.ucb.XFileIdentifierConverter.class, xContext.getServiceManager()
              .createInstanceWithContext("com.sun.star.ucb.FileContentProvider", xContext));
      sURL = xFileConverter.getFileURLFromSystemPath("", dialogFile.getAbsolutePath());
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return null;
    }
    return sURL;
  }

  /** 
   * Returns a button (XButton) from a dialog
   */
  public static XButton getButton(XDialog dialog, String componentId) {
    XControlContainer xDlgContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class,
        dialog);
    Object control = xDlgContainer.getControl(componentId);
    return (XButton) UnoRuntime.queryInterface(XButton.class, control);
  }

  /** 
   * Returns a text field (XTextComponent) from a dialog 
   */
  public static XTextComponent getEditField(XDialog dialog, String componentId) {
    XControlContainer xDlgContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class,
        dialog);
    Object control = xDlgContainer.getControl(componentId);
    return (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class, control);
  }

  /** 
   * Returns a Combo box (XComboBox) from a dialog 
   */
  public static XComboBox getCombobox(XDialog dialog, String componentId) {
    XControlContainer xDlgContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class,
        dialog);
    Object control = xDlgContainer.getControl(componentId);
    return (XComboBox) UnoRuntime.queryInterface(XComboBox.class, control);
  }
  
  /** 
   * Returns a List box (XListBox) from a dialog 
   */
  public static XListBox getListBox(XDialog dialog, String componentId) {
    XControlContainer xDlgContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class,
        dialog);
    Object control = xDlgContainer.getControl(componentId);
    return (XListBox) UnoRuntime.queryInterface(XListBox.class, control);
  }

  /** 
   * Returns a label (XFixedText) from a dialog 
   */
  public static XFixedText getLabel(XDialog dialog, String componentId) {
    XControlContainer xDlgContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class,
        dialog);
    Object control = xDlgContainer.getControl(componentId);
    return (XFixedText) UnoRuntime.queryInterface(XFixedText.class, control);
  }

  /** 
   * Enables / disables a button (XButton) in a dialog
   */
  public static void EnableButton(XDialog dialog, String componentId, boolean enable) {
    XControlContainer xDlgContainer = (XControlContainer) UnoRuntime.queryInterface(XControlContainer.class,
        dialog);
    // retrieve the control that we want to disable or enable
    XControl xControl = UnoRuntime.queryInterface(XControl.class, xDlgContainer.getControl(componentId));
    XPropertySet xModelPropertySet = UnoRuntime.queryInterface(XPropertySet.class, xControl.getModel());
    try {
      xModelPropertySet.setPropertyValue("Enabled", Boolean.valueOf(enable));
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return;
    }
  }
  
  /** 
   * Set the focus to an input field 
   */
  public static void SetFocus(XTextComponent editField) {
    XWindow xControlWindow = UnoRuntime.queryInterface(XWindow.class, editField);
    xControlWindow.setFocus();
  }

  /** 
   * Set the position of a dialog
   */
  public static void setPosition(XDialog dialog, int posX, int posY) {
    XControlModel xDialogModel = UnoRuntime.queryInterface(XControl.class, dialog).getModel();
    XPropertySet xPropSet = UnoRuntime.queryInterface(XPropertySet.class, xDialogModel);
    try {
      xPropSet.setPropertyValue("PositionX", posX);
      xPropSet.setPropertyValue("PositionY", posY);
    } catch (com.sun.star.lang.IllegalArgumentException | UnknownPropertyException | PropertyVetoException
        | WrappedTargetException e) {
      return;
    }
  }

  /** 
   * Get the position of a dialog
   */
  public static Point getPosition(XDialog dialog) {
    int posX = 0;
    int posY = 0;
    XControlModel xDialogModel = UnoRuntime.queryInterface(XControl.class, dialog).getModel();
    XPropertySet xPropSet = UnoRuntime.queryInterface(XPropertySet.class, xDialogModel);
    try {
      posX = (int) xPropSet.getPropertyValue("PositionX");
      posY = (int) xPropSet.getPropertyValue("PositionY");
    } catch (UnknownPropertyException | WrappedTargetException e) {
    }
    return new Point(posX, posY);
  }
  
  /** 
   * show an info message box
   */
  public static void showInfoMessage(XComponentContext context, XDialog dialog, String message) {
    showMessageBox(context, dialog, MessageBoxType.INFOBOX, "Info", message);
  }
  
  /** 
   * show a warning message box
   */
  public static void showWarningMessage(XComponentContext context, XDialog dialog, String message) {
    showMessageBox(context, dialog, MessageBoxType.WARNINGBOX, "Warnung", message);
  }

  /** 
   * show an error message box
   */
  public static void showErrorMessage(XComponentContext context, XDialog dialog, String message) {
    showMessageBox(context, dialog, MessageBoxType.ERRORBOX, "Fehler", message);
  }

  /** 
   * show a message box
   */
  public static void showMessageBox(XComponentContext context, XDialog dialog, MessageBoxType type, String sTitle, String sMessage) {
    XToolkit xToolkit;
    try {
      xToolkit = UnoRuntime.queryInterface(XToolkit.class,
            context.getServiceManager().createInstanceWithContext("com.sun.star.awt.Toolkit", context));
    } catch (Exception e) {
      return;
    }
    XMessageBoxFactory xMessageBoxFactory = UnoRuntime.queryInterface(XMessageBoxFactory.class, xToolkit);
    XWindowPeer xParentWindowPeer = UnoRuntime.queryInterface(XWindowPeer.class, dialog);
    XMessageBox xMessageBox = xMessageBoxFactory.createMessageBox(xParentWindowPeer, type,
                                  com.sun.star.awt.MessageBoxButtons.BUTTONS_OK, sTitle, sMessage);
    if (xMessageBox == null) {
      return;
    }
    xMessageBox.execute();
  }


}
