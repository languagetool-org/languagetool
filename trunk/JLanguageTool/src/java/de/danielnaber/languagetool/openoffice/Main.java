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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XEnumeration;
import com.sun.star.frame.XDesktop;
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
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Configuration;
import de.danielnaber.languagetool.rules.Rule;

/**
 * OpenOffice.org integration.
 * 
 * @author Daniel Naber
 */
public class Main {

  public static final String version = JLanguageTool.VERSION;

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
    
    public _Main(final XComponentContext xCompContext) {
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

    public void trigger(final String sEvent) {
      try {
        if (sEvent.equals("execute")) {
          TextToCheck textToCheck = getText();
          checkText(textToCheck);
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
        showError(e);
      }
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
      return Language.getLanguageForShortName(charLocale.Language);
    }
    
    private TextToCheck getText() {
      com.sun.star.container.XEnumerationAccess xParaAccess = (com.sun.star.container.XEnumerationAccess) UnoRuntime
          .queryInterface(com.sun.star.container.XEnumerationAccess.class, xTextDoc.getText());
      if (xParaAccess == null) {
        System.err.println("xParaAccess == null");
        return new TextToCheck(new ArrayList<String>(), false);
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
      return new TextToCheck(paragraphs, false);   // FIXME: "false"
    }

    private void checkText(final TextToCheck textToCheck) {
      if (textToCheck == null)
        return;
      ProgressDialog progressDialog = new ProgressDialog();
      Language docLanguage = getLanguage();
      CheckerThread checkerThread = new CheckerThread(textToCheck.paragraphs, docLanguage, config, 
          baseDir, progressDialog);
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
        if (textToCheck.isSelection) {
          msg = "No errors or warnings found in selected text " + "(language: " + docLanguage.getName() + ")";  
        } else {
          msg = "No errors or warnings found " + "(document language: " + docLanguage.getName() + ")";  
        }
        DialogThread dt = new DialogThread(msg);
        dt.start();
        // TODO: display number of active rules etc?
      } else {
        ResultDialogThread dialog = new ResultDialogThread(config,
            checkerThread.getLanguageTool().getAllRules(),
            xTextDoc, checkedParagraphs, xViewCursor);
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
    JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
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
      if ("Footnote".equals(type)) {
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

class ResultDialogThread extends Thread {

  private Configuration configuration;
  private List<Rule> rules;
  private XTextDocument xTextDoc;
  private List<CheckedParagraph> checkedParagraphs;
  private XTextViewCursor xViewCursor;

  ResultDialogThread(final Configuration configuration, final List<Rule> rules, final XTextDocument xTextDoc,
      final List<CheckedParagraph> checkedParagraphs, final XTextViewCursor xViewCursor) {
    this.configuration = configuration;
    this.rules = rules;
    this.xTextDoc = xTextDoc;
    this.checkedParagraphs = checkedParagraphs;
    this.xViewCursor = xViewCursor;
  }
  
  public void run() {
    OOoDialog dialog = new OOoDialog(configuration, rules,
        xTextDoc, checkedParagraphs, xViewCursor);
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
