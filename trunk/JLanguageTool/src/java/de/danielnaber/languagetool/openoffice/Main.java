package de.danielnaber.languagetool.openoffice;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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
import de.danielnaber.languagetool.rules.Rule;

/**
 * Tests for OOo integration -- NOT WORKING YET.
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
          // FIXME: use iteration over paragraphs instead?!:
          String text = getText();
          checkText(text);
          //System.out.println("text to check: " + text);
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
      // see http://perso.wanadoo.fr/moutou/MyUNODoc_HTML/UNOCppAPI8.html:
      /*XTextCursor cursor = text.createTextCursor();
      cursor.goRight((short)2, false);
      cursor.goRight((short)4, true);
      cursor.setString("foo2");*/
      //text.setString("foo!");
      // FIXME: make this work
      /*
      XEnumerationAccess xParaAccess = (XEnumerationAccess) UnoRuntime.queryInterface(
          XEnumerationAccess.class, xTextDoc);
      XEnumeration xParaEnum = xParaAccess.createEnumeration();
      // While there are paragraphs, do things to them
      while (xParaEnum.hasMoreElements()) {
        XServiceInfo xInfo = (XServiceInfo) UnoRuntime.queryInterface(XServiceInfo.class, xParaEnum
            .nextElement());
        if (!xInfo.supportsService("com.sun.star.text.TextTable")) {
          XPropertySet xSet = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, xInfo);
          xSet.setPropertyValue("ParaAdjust", com.sun.star.style.ParagraphAdjust.CENTER);
        }
      }*/
      return text.getString();
    }

    private void checkText(String text) throws IOException, ParserConfigurationException, SAXException {
      // TODO: show splash screen, as init takes some time?
      // TODO: use document language
      JLanguageTool langTool = new JLanguageTool(Language.ENGLISH);
      File defaultPatternFile = new File("rules/en/grammar.xml");
      List patternRules = new ArrayList();
      patternRules = langTool.loadPatternRules(defaultPatternFile.getAbsolutePath());
      for (Iterator iter = patternRules.iterator(); iter.hasNext();) {
        Rule rule = (Rule) iter.next();
        langTool.addRule(rule);
      }
      List ruleMatches = langTool.check(text);
      if (ruleMatches.size() == 0) {
        JOptionPane.showMessageDialog(null, "No errors and warnings found");
        // TODO: display language setting used etc.
      } else {
        OOoDialog dialog = new OOoDialog();
        dialog.show(ruleMatches, text);
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
