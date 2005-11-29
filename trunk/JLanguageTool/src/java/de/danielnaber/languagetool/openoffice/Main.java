/* JLanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.openoffice;

import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.sun.star.frame.XDesktop;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.task.XJobExecutor;
import com.sun.star.text.XText;
import com.sun.star.text.XTextDocument;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;

/**
 * Basic OpenOffice.org integration.
 * 
 * @author Daniel Naber
 */
public class Main {

  public static final String version = "0.5";

  public static class _Main extends WeakBase implements XJobExecutor, XServiceInfo {

    static private final String __serviceName = "de.danielnaber.languagetool.openoffice.Main";

    private XTextDocument xTextDoc;

    public _Main(XComponentContext xCompContext) {
      try {
        XMultiComponentFactory xMCF = xCompContext.getServiceManager();
        Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xCompContext);
        XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
        XComponent xComponent = xDesktop.getCurrentComponent();
        xTextDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xComponent);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }

    public void trigger(String sEvent) {
      if (sEvent.equals("execute")) {
        try {
          String text = getText();
          checkText(text);
        } catch (Throwable e) {
          e.printStackTrace();
        }
      } else {
        System.err.println("Sorry, don't know what to do, sEvent = " + sEvent);
      }
    }

    public void initialize(Object[] object) {
      if (object == null) object = null;        // avoid compiler warning
    }

    public String[] getSupportedServiceNames() {
      return getServiceNames();
    }

    public static String[] getServiceNames() {
      String[] sSupportedServiceNames = { __serviceName };
      return sSupportedServiceNames;
    }

    public boolean supportsService(String sServiceName) {
      return sServiceName.equals(__serviceName);
    }

    public String getImplementationName() {
      return _Main.class.getName();
    }

    private String getText() {
      XText text = xTextDoc.getText();
      return text.getString();
    }

    private void checkText(String text) throws IOException, ParserConfigurationException, SAXException {
      // TODO: show splash screen, as init takes some time?
      // TODO: use document language
      JLanguageTool langTool = new JLanguageTool(Language.ENGLISH);
      langTool.activateDefaultPatternRules();
      List ruleMatches = langTool.check(text);
      if (ruleMatches.size() == 0) {
        JOptionPane.showMessageDialog(null, "No errors and warnings found");
        // TODO: display language setting used etc.
      } else {
        OOoDialog dialog = new OOoDialog(xTextDoc, ruleMatches, text);
        dialog.show();
      }
    }
    
  }

  public static XSingleComponentFactory __getComponentFactory(String sImplName) {
    XSingleComponentFactory xFactory = null;
    if (sImplName.equals(_Main.class.getName()))
      xFactory = Factory.createComponentFactory(_Main.class, _Main.getServiceNames());
    return xFactory;
  }

  public static boolean __writeRegistryServiceInfo(XRegistryKey regKey) {
    return Factory.writeRegistryServiceInfo(_Main.class.getName(), _Main.getServiceNames(), regKey);
  }

}
