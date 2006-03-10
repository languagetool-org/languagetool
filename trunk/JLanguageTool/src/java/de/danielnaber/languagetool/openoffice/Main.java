/* LanguageTool, a natural language style checker 
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import javax.swing.JOptionPane;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XModel;
import com.sun.star.lang.Locale;
import com.sun.star.lang.WrappedTargetException;
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
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Configuration;

/**
 * OpenOffice.org integration.
 * 
 * @author Daniel Naber
 */
public class Main {

  public static final String version = "0.6";

  public static class _Main extends WeakBase implements XJobExecutor, XServiceInfo {

    static private final String __serviceName = "de.danielnaber.languagetool.openoffice.Main";

    private XTextDocument xTextDoc;
    private XTextViewCursor xViewCursor;
    
    private File baseDir;
    private Configuration config;

    /** Testing only. */
    public _Main() throws IOException {
      baseDir = new File(".");
      config = new Configuration(baseDir);
    }
    
    public _Main(XComponentContext xCompContext) {
      try {
        XMultiComponentFactory xMCF = xCompContext.getServiceManager();
        Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xCompContext);
        XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
        XComponent xComponent = xDesktop.getCurrentComponent();
        xTextDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xComponent);
        baseDir = getBaseDir();
        config = new Configuration(baseDir);
      } catch (Throwable e) {
        writeError(e);
        e.printStackTrace();
      }
    }

    public void trigger(String sEvent) {
      try {
        if (sEvent.equals("execute")) {
          try {
            String text = getText();
            checkText(text);
          } catch (Throwable e) {
            writeError(e);
            e.printStackTrace();
          }
        } else if (sEvent.equals("configure")) {
          ConfigThread configThread = new ConfigThread(getLanguage(), config, baseDir);
          configThread.start();
          while (true) {
            if (configThread.done()) {
              break;
            }
            try {
              Thread.sleep(100);
            } catch (InterruptedException e) {
              break;
            }
          }
        } else {
          System.err.println("Sorry, don't know what to do, sEvent = " + sEvent);
        }        
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }

    private void writeError(Throwable e) {
      FileWriter fw;
      try {
        fw = new FileWriter("languagetool.log");
        fw.write(e.toString() + "\r\n");
        StackTraceElement[] el = e.getStackTrace();
        for (int i = 0; i < el.length; i++) {
          fw.write(el[i].toString()+ "\r\n");
        }
        fw.close();
      } catch (IOException e1) {
        e1.printStackTrace();
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

    private Language getLanguage() {
      if (xTextDoc == null)
        return Language.ENGLISH; // for testing with local main() method only      // just look at the current position(?) in the document and assume that this character's
      Locale charLocale;
      try {
        // language is the language of the whole document:
        XPropertySet xCursorProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,
            xTextDoc.getText().createTextCursor());
        charLocale = (Locale) xCursorProps.getPropertyValue("CharLocale");
        boolean langIsSupported = false;
        for (int i = 0; i < Language.LANGUAGES.length; i++) {
          if (Language.LANGUAGES[i].getShortName().equals(charLocale.Language)) {
            langIsSupported= true;
            break;
          }
        }
        if (!langIsSupported) {
          JOptionPane.showMessageDialog(null, "Error: Sorry, the document language '" +charLocale.Language+ 
              "' is not supported by LanguageTool.");
          throw new IllegalArgumentException("Language is not supported: " + charLocale.Language);
        }
      } catch (UnknownPropertyException e) {
        throw new RuntimeException(e);
      } catch (WrappedTargetException e) {
        throw new RuntimeException(e);
      }
      return Language.getLanguageforShortName(charLocale.Language);
    }
    
    private String getText() {
      XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xTextDoc); 
      XController xController = xModel.getCurrentController(); 
      XTextViewCursorSupplier xViewCursorSupplier = 
        (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController); 
      //XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();
      xViewCursor = xViewCursorSupplier.getViewCursor();
      String textToCheck = xViewCursor.getString();     // user's current selection
      if (textToCheck.equals("")) {     // no selection = check complete text
        XText text = xTextDoc.getText();
        textToCheck = text.getString();
        xViewCursor = null;
      }
      // without this replacement selecting the error on Windows will not work correctly:
      textToCheck = textToCheck.replaceAll("\r\n", "\n");
      return textToCheck;
    }

    private void checkText(String text) {
      ProgressDialog progressDialog = new ProgressDialog();
      Language docLanguage = getLanguage();
      CheckerThread checkerThread = new CheckerThread(text, docLanguage, config, baseDir);
      checkerThread.start();
      while (true) {
        if (checkerThread.done()) {
          break;
        }
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          // nothing
        }
      }
      progressDialog.close();
      
      List ruleMatches = checkerThread.getRuleMatches();
      // TODO: why must these be wrapped in threads to avoid focus problems?
      if (ruleMatches.size() == 0) {
        // TODO: if only selection was checked, say so!
        DialogThread dt = new DialogThread("No errors and warnings found " +
                "(document language: " + docLanguage.getName() + ")");
        dt.start();
        // TODO: display number of active rules etc?
      } else {
        ResultDialogThread dialog = new ResultDialogThread(config,
            checkerThread.getLanguageTool().getAllRules(),
            xTextDoc, ruleMatches, text, xViewCursor);
        dialog.start();
      }
    }

    private File getBaseDir() throws IOException {
      java.net.URL url = Main.class.getResource("/de/danielnaber/languagetool/openoffice/Main.class");
      String urlString = url.getFile();
      urlString = URLDecoder.decode(urlString);
      File file = new File(urlString.substring("file:".length(), urlString.indexOf("!")));
      if (!file.exists()) {
        throw new IOException("File not found: " + file.getAbsolutePath());
      }
      return file.getParentFile();
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
  
  /** Testing only. */
  public static void main(String[] args) throws IOException {
    _Main m = new _Main();
    m.checkText("This is an test, don't berate yourself.");
  }

}

class DialogThread extends Thread {

  private String text;

  DialogThread(String text) {
    this.text = text;
  }
  
  public void run() {
    JOptionPane.showMessageDialog(null, text);
  }
  
}

class ResultDialogThread extends Thread {

  private Configuration configuration;
  private List rules;
  private XTextDocument xTextDoc;
  private List ruleMatches;
  private String text;
  private XTextViewCursor xViewCursor;

  ResultDialogThread(Configuration configuration, List rules, XTextDocument xTextDoc, List ruleMatches, String text,
      XTextViewCursor xViewCursor) {
    this.configuration = configuration;
    this.rules =rules;
    this.xTextDoc = xTextDoc;
    this.ruleMatches = ruleMatches;
    this.text = text;
    this.xViewCursor = xViewCursor;
  }
  
  public void run() {
    OOoDialog dialog = new OOoDialog(configuration, rules,
        xTextDoc, ruleMatches, text, xViewCursor);
    dialog.show();
  }
  
}

