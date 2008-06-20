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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.JOptionPane;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
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
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.AboutDialog;
import de.danielnaber.languagetool.gui.Configuration;
import de.danielnaber.languagetool.gui.Tools;
import de.danielnaber.languagetool.rules.Rule;

//FIXME: remove as soon OOo 3 has spell-grammar dialog working

/**
 * OpenOffice.org integration.
 * 
 * @author Daniel Naber
 */
public class OldChecker {

  public static final String version = JLanguageTool.VERSION;

  public static class _Main extends WeakBase implements XJobExecutor, XServiceInfo {

    private static final String __serviceName = "de.danielnaber.languagetool.openoffice.OldChecker";
    // use a different name than the stand-alone version to avoid conflicts:
    private static final String CONFIG_FILE = ".languagetool-ooo.cfg";

    private XTextDocument xTextDoc;
    private XTextViewCursor xViewCursor;
    
    private File homeDir;
    private Configuration config;
    
    private ResourceBundle messages = null;

    /** Testing only. */
    public _Main() throws IOException {
      homeDir = new File(".");
      config = new Configuration(homeDir, CONFIG_FILE);
      messages = JLanguageTool.getMessageBundle();
    }
    
    public _Main(final XComponentContext xCompContext) {
      try {
        XMultiComponentFactory xMCF = xCompContext.getServiceManager();
        Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xCompContext);
        XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
        XComponent xComponent = xDesktop.getCurrentComponent();
        xTextDoc = (XTextDocument) UnoRuntime.queryInterface(XTextDocument.class, xComponent);
        homeDir = getHomeDir();
        config = new Configuration(homeDir, CONFIG_FILE);
        messages = JLanguageTool.getMessageBundle();        
      } catch (Throwable e) {
        writeError(e);
        e.printStackTrace();
      }
    }

    public void trigger(final String sEvent) {
      if (!javaVersionOkay()) {
        return;
      }
      try {
        if (sEvent.equals("execute")) {
          TextToCheck textToCheck = getText();
          checkText(textToCheck);
        } else if (sEvent.equals("configure")) {
          Language lang = getLanguage();
          if (lang == null)
            return;
          ConfigThread configThread = new ConfigThread(lang, config);
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
        } else if (sEvent.equals("about")) {
          AboutDialogThread aboutthread = new AboutDialogThread(messages);
          aboutthread.start();
        } else {
          System.err.println("Sorry, don't know what to do, sEvent = " + sEvent);
        }        
      } catch (Throwable e) {
        showError(e);
      }
    }

    private boolean javaVersionOkay() {
      String version = System.getProperty("java.version");
      if (version != null && (version.startsWith("1.0") || version.startsWith("1.1")
          || version.startsWith("1.2") || version.startsWith("1.3") || version.startsWith("1.4"))) {
        DialogThread dt = new DialogThread("Error: LanguageTool requires Java 1.5 or later. Current version: " + version);
        dt.start();
        return false;
      }    
      return true;
    }

    private void writeError(final Throwable e) {
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

    @SuppressWarnings("unused")
    public void initialize(final Object[] object) {
    }

    public String[] getSupportedServiceNames() {
      return getServiceNames();
    }

    public static String[] getServiceNames() {
      String[] sSupportedServiceNames = { __serviceName };
      return sSupportedServiceNames;
    }

    public boolean supportsService(final String sServiceName) {
      return sServiceName.equals(__serviceName);
    }

    public String getImplementationName() {
      return _Main.class.getName();
    }

    private Language getLanguage() {
      if (xTextDoc == null)
        return Language.ENGLISH; // for testing with local main() method only
      Locale charLocale;
      try {
        // look at the global document language
        /* TODO: make this work
        XDocumentInfoSupplier xdis = (XDocumentInfoSupplier)
          UnoRuntime.queryInterface(XDocumentInfoSupplier.class, xTextDoc);
        XPropertySet docInfo = (XPropertySet) UnoRuntime.queryInterface
          (XPropertySet.class, xdis.getDocumentInfo()); 
        Object lang = docInfo.getPropertyValue("language");
        */
        // just look at the first position in the document and assume that this character's
        // language is the language of the whole document:
        XTextCursor textCursor = xTextDoc.getText().createTextCursor();
        textCursor.gotoStart(false);
        XPropertySet xCursorProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class,
            textCursor);
        charLocale = (Locale) xCursorProps.getPropertyValue("CharLocale");
        boolean langIsSupported = false;
        for (int i = 0; i < Language.LANGUAGES.length; i++) {
          if (Language.LANGUAGES[i].getShortName().equals(charLocale.Language)) {
            langIsSupported= true;
            break;
          }
        }
        if (!langIsSupported) {
          // FIXME: i18n
          DialogThread dt = new DialogThread("Error: Sorry, the document language '" +charLocale.Language+ 
              "' is not supported by LanguageTool.");
          dt.start();
          return null;
        }
        //checkTables();
      } catch (UnknownPropertyException e) {
        throw new RuntimeException(e);
      } catch (WrappedTargetException e) {
        throw new RuntimeException(e);
      }
      return Language.getLanguageForShortName(charLocale.Language);
    }
    
    // commented out due to focus problems (clicking "OK" doesn't work, only Escape closes the dialog):
    /*
    private void checkTables() {
      XTextTablesSupplier xTablesSupplier = (XTextTablesSupplier) UnoRuntime.queryInterface(
          XTextTablesSupplier.class, xTextDoc);
      XNameAccess xNamedTables = xTablesSupplier.getTextTables();
      XIndexAccess xIndexedTables = (XIndexAccess) UnoRuntime.queryInterface(XIndexAccess.class,
          xNamedTables);
      if (xIndexedTables.getCount() > 0) {
        DialogThread dt = new DialogThread("Warning: The text contains tables, this may lead to the " +
            "wrong text marked due to a known bug.");
        dt.start();
        try {
          dt.join();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }*/

    private TextToCheck getText() {
      com.sun.star.container.XEnumerationAccess xParaAccess = (com.sun.star.container.XEnumerationAccess) UnoRuntime
          .queryInterface(com.sun.star.container.XEnumerationAccess.class, xTextDoc.getText());
      if (xParaAccess == null) {
        System.err.println("xParaAccess == null");
        return new TextToCheck(new ArrayList<String>(), false);
      }
      
      XModel xModel = (XModel)UnoRuntime.queryInterface(XModel.class, xTextDoc);
      if (xModel == null) {
        // FIXME: i18n
        DialogThread dt = new DialogThread("Sorry, only text documents are supported");
        dt.start();
        return null;
      }
      XController xController = xModel.getCurrentController(); 
      XTextViewCursorSupplier xViewCursorSupplier = 
        (XTextViewCursorSupplier)UnoRuntime.queryInterface(XTextViewCursorSupplier.class, xController); 
      xViewCursor = xViewCursorSupplier.getViewCursor();
      String textToCheck = xViewCursor.getString();     // user's current selection
      if (textToCheck.equals("")) {     // no selection = check complete text
        //System.err.println("check complete text");
      } else {
        //System.err.println("check selected text");
        List<String> l = new ArrayList<String>();
        // FIXME: if footnotes with a number greater than "9" occur in the selected text
        // they mess up the error marking in OOoDialog because they appear as "10" etc.
        // but the code assumes we need to use goRight() once per character...
        l.add(textToCheck);
        return new TextToCheck(l, true);
      }
      
      List<String> paragraphs = new ArrayList<String>();
      try {
        for (com.sun.star.container.XEnumeration xParaEnum = xParaAccess.createEnumeration(); xParaEnum.hasMoreElements();) {
          Object para = xParaEnum.nextElement();
          String paraString = getParagraphContent(para);
          if (paraString == null) {
            paragraphs.add("");
          } else {
            paragraphs.add(paraString);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      return new TextToCheck(paragraphs, false);
    }

    private void checkText(final TextToCheck textToCheck) {
      if (textToCheck == null)
        return;
      Language docLanguage = getLanguage();
      if (docLanguage == null)
        return;
      ProgressDialog progressDialog = new ProgressDialog(messages);
      CheckerThread checkerThread = new CheckerThread(textToCheck.paragraphs, docLanguage, config, 
          progressDialog);
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
      
      List<CheckedParagraph> checkedParagraphs = checkerThread.getRuleMatches();
      // TODO: why must these be wrapped in threads to avoid focus problems?
      if (checkedParagraphs.size() == 0) {
        String msg;
        String translatedLangName = messages.getString(docLanguage.getShortName());
        if (textToCheck.isSelection) {
          msg = Tools.makeTexti18n(messages, "guiNoErrorsFoundSelectedText", new String[] {translatedLangName});  
        } else {
          msg = Tools.makeTexti18n(messages, "guiNoErrorsFound", new String[] {translatedLangName});  
        }
        DialogThread dt = new DialogThread(msg);
        dt.start();
        // TODO: display number of active rules etc?
      } else {
        ResultDialogThread dialog;
        if (textToCheck.isSelection) {
          dialog = new ResultDialogThread(config,
              checkerThread.getLanguageTool().getAllRules(),
              xTextDoc, checkedParagraphs, xViewCursor, textToCheck);
        } else {
          dialog = new ResultDialogThread(config,
              checkerThread.getLanguageTool().getAllRules(),
              xTextDoc, checkedParagraphs, null, null);
        }
        dialog.start();
      }
    }

    private File getHomeDir() {
      final String homeDir = System.getProperty("user.home");
      if (homeDir == null) {
        throw new RuntimeException("Could not get home directory");
      }
      return new File(homeDir);
    }

  }

  public static XSingleComponentFactory __getComponentFactory(final String sImplName) {
    XSingleComponentFactory xFactory = null;
    if (sImplName.equals(_Main.class.getName()))
      xFactory = Factory.createComponentFactory(_Main.class, _Main.getServiceNames());
    return xFactory;
  }

  public static boolean __writeRegistryServiceInfo(final XRegistryKey regKey) {
    return Factory.writeRegistryServiceInfo(_Main.class.getName(), _Main.getServiceNames(), regKey);
  }

  static void showError(final Throwable e) {
    String msg = "An error has occured:\n" + e.toString() + "\nStacktrace:\n";
    StackTraceElement[] elem = e.getStackTrace();
    for (int i = 0; i < elem.length; i++) {
      msg += elem[i].toString() + "\n";
    }
    DialogThread dt = new DialogThread(msg);
    dt.start();
    e.printStackTrace();
    throw new RuntimeException(e);
  }

  static String getParagraphContent(Object para) throws NoSuchElementException, WrappedTargetException, UnknownPropertyException {
    if (para == null) {
      // TODO: ??
      return null;
    }
    com.sun.star.container.XEnumerationAccess xPortionAccess = (com.sun.star.container.XEnumerationAccess) UnoRuntime
        .queryInterface(com.sun.star.container.XEnumerationAccess.class, para);
    if (xPortionAccess == null) {
      System.err.println("xPortionAccess is null");
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (XEnumeration portionEnum = xPortionAccess.createEnumeration(); portionEnum.hasMoreElements();) {
      Object textPortion = portionEnum.nextElement();
      XPropertySet textProps = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, textPortion);
      String type = (String)textProps.getPropertyValue("TextPortionType");
      if ("Footnote".equals(type) || "DocumentIndexMark".equals(type)) {
        // a footnote reference appears as one character in the text. we don't use a whitespace
        // because we don't want to trigger the "no whitespace before comma" rule in this case:
        // my footnote¹, foo bar
        sb.append("1");
      } else {
        XTextRange xtr = (XTextRange) UnoRuntime.queryInterface(XTextRange.class, textPortion);
        sb.append(xtr.getString());
      }
    }
    return sb.toString();
  }

  /** Testing only. */
  public static void main(final String[] args) throws IOException {
    _Main m = new _Main();
    List<String> paras = new ArrayList<String>();
    paras.add("This is an test, don't berate yourself.");
    TextToCheck ttc = new TextToCheck(paras, false);
    m.checkText(ttc);
  }

}

class DialogThread extends Thread {

  private String text;

  DialogThread(final String text) {
    this.text = text;
  }
  
  public void run() {
    JOptionPane.showMessageDialog(null, text);
  }
  
}

class AboutDialogThread extends Thread {

  private ResourceBundle messages;

  AboutDialogThread(final ResourceBundle messages) {
    this.messages = messages;
  }
  
  public void run() {
    AboutDialog about = new AboutDialog(messages);
    about.show();
  }
  
}

class ResultDialogThread extends Thread {

  private Configuration configuration;
  private List<Rule> rules;
  private XTextDocument xTextDoc;
  private List<CheckedParagraph> checkedParagraphs;
  private XTextViewCursor xViewCursor;
  private TextToCheck textTocheck;

  ResultDialogThread(final Configuration configuration, final List<Rule> rules, final XTextDocument xTextDoc,
      final List<CheckedParagraph> checkedParagraphs, final XTextViewCursor xViewCursor,
      final TextToCheck textTocheck) {
    this.configuration = configuration;
    this.rules = rules;
    this.xTextDoc = xTextDoc;
    this.checkedParagraphs = checkedParagraphs;
    this.xViewCursor = xViewCursor;
    this.textTocheck = textTocheck;
  }
  
  public void run() {
    OOoDialog dialog;
    if (xViewCursor == null)
      dialog = new OOoDialog(configuration, rules, xTextDoc, checkedParagraphs);
    else
      dialog = new OOoDialog(configuration, rules, xTextDoc, checkedParagraphs, xViewCursor, textTocheck);
    dialog.show();
  }
  
}

class TextToCheck {
  
  List<String> paragraphs;
  boolean isSelection;
  
  TextToCheck(final List<String> paragraphs, final boolean isSelection) {
    this.paragraphs = paragraphs;
    this.isSelection = isSelection;
  }
   
}
