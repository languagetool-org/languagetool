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

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Some tools to use services and interfaces of LibreOffice/OpenOffice
 * temporary class - will be integrated in OfficeTools
 * @since 5.1
 * @author Fred Kruse
 */
public class OfficeTools2 {

  /**
   *  dispatch an internal LO/OO command
   *  cmd does not include the ".uno:" substring; e.g. pass "Zoom" not ".uno:Zoom"
   */
  public static boolean dispatchCmd(String cmd, XComponentContext xContext) {
    return dispatchCmd(cmd, null);
  } 


  public static boolean dispatchCmd(String cmd, PropertyValue[] props, XComponentContext xContext) {
    try {
      if (xContext == null) {
        return false;
      }
      XMultiComponentFactory xMCF = UnoRuntime.queryInterface(XMultiComponentFactory.class,
              xContext.getServiceManager());
      if (xMCF == null) {
        return false;
      }
      Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
      if (desktop == null) {
        return false;
      }
      XDesktop xdesktop = UnoRuntime.queryInterface(XDesktop.class, desktop);
      if (xdesktop == null) {
        return false;
      }
      
      Object helper = xMCF.createInstanceWithContext("com.sun.star.frame.DispatchHelper", xContext);
      if (helper == null) {
        return false;
      }
      XDispatchHelper dispatchHelper = UnoRuntime.queryInterface(XDispatchHelper.class, helper);
      if (dispatchHelper == null) {
        return false;
      }
  
      XDispatchProvider provider = UnoRuntime.queryInterface(XDispatchProvider.class, xdesktop.getCurrentFrame());

      dispatchHelper.executeDispatch(provider, (".uno:" + cmd), "", 0, props);

      return true;
    } catch (Throwable t) {
      MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
      return false;
    }
  }

}
