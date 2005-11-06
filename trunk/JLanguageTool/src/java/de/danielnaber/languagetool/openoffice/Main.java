package de.danielnaber.languagetool.openoffice;

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
      try {
        // FIXME: use iteration over paragraphs instead:
        String text = getText();
        System.out.println("text to check: " + text);
      } catch (Throwable e) {
        e.printStackTrace();
      }
    }

    public void initialize(Object[] object) {
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

      // ##########################################
      /*
      JLanguageTool langTool = new JLanguageTool(Language.ENGLISH);
      List ruleMatches = langTool.check(x.getString());
      System.out.println(ruleMatches + " matches");
      for (Iterator iter = ruleMatches.iterator(); iter.hasNext();) {
        RuleMatch match = (RuleMatch) iter.next();
        System.out.println("=====================================================");
        System.out.println(match);
      }*/
      // ##########################################

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
