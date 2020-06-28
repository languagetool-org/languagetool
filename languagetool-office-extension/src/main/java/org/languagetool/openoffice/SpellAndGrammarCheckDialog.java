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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.Rule;

import com.sun.star.awt.XDialog;
import com.sun.star.awt.XDialogEventHandler;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.lang.Locale;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.text.TextMarkupType;
import com.sun.star.uno.Exception;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.util.XURLTransformer;

/**
 * Class defines the spell and grammar check dialog
 * @since 5.1
 * @author Fred Kruse
 */
public class SpellAndGrammarCheckDialog extends Thread {
  
  private XComponentContext xContext;
  
  SpellAndGrammarCheckDialog(XComponentContext xContext, MultiDocumentsHandler mdh) {
    this.xContext = xContext;
  }
  
  @Override
  public void run() {
    CheckDialog checkDialog = new CheckDialog(xContext);
    checkDialog.show();
  }

  public void nextError() {
  }

  public class CheckDialog implements XDialogEventHandler {
    private XDialog dialog;
    private String[] supportedActions = new String[] { 
        "languageChanged",
        "textChanged",
        "alternativeSelected",
        "moreInformation",
        "ignoreOnce",
        "ignoreAll",
        "deactivateRule",
        "addToDictionary",
        "Change",
        "Help",
        "Options",
        "Undo",
        "Close" };
    
    public CheckDialog(XComponentContext xContext) {
      MessageHandler.printToLogFile("SpellAndGrammarCheckDialog called");
      this.dialog = createDialog("SpellAndGrammarCheckDialog.xdl", xContext, this);
      if (dialog == null) {
        MessageHandler.printToLogFile("SpellAndGrammarCheckDialog == null");
      }
    }
  
    public void show() {
      dialog.execute();
    }
    
    private void closeDialog() {
      dialog.endExecute();
    }
    
    @Override
    public boolean callHandlerMethod(XDialog dialog, Object eventObject, String actionName) throws WrappedTargetException {
      if (actionName.equals("Close")) {
        closeDialog();
      } else {
        return false; // Event was not handled
      }
      return true;
    }
  
    @Override
    public String[] getSupportedMethodNames() {
      return supportedActions;
    }
    
    /**
     * Create a dialog from an xdl file.
     *
     * @param xdlFile - The filename in the `dialog` folder
     * @param context
     * @return XDialog
     */
    public XDialog createDialog(String xdlFile, XComponentContext context, XDialogEventHandler handler) {
      Object oDialogProvider;
      try {
        oDialogProvider = context.getServiceManager().createInstanceWithContext("com.sun.star.awt.DialogProvider2",
            context);
        XDialogProvider2 xDialogProv = (XDialogProvider2) UnoRuntime.queryInterface(XDialogProvider2.class,
            oDialogProvider);
        File dialogFile = getDialogFilePath(xdlFile, context);
        MessageHandler.printToLogFile("Dialog File: " + dialogFile.getAbsolutePath());
        if (xDialogProv == null) {
          MessageHandler.printToLogFile("xDialogProv == null");
        }
        String sUrl = convertToURL(context, dialogFile);
        MessageHandler.printToLogFile("Dialog URL: " + sUrl);
        return xDialogProv.createDialogWithHandler(sUrl, handler);
      } catch (Throwable t) {
        MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
        return null;
      }
    }
  
    /** 
     * Returns a URL to be used with XDialogProvider to create a dialog 
     */
    public String convertToURL(XComponentContext xContext, File dialogFile) {
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
     * Returns a file path for a file in the installed extension, or null on failure.
     */
    public File getDialogFilePath(String file, XComponentContext xContext) {
      XPackageInformationProvider xPackageInformationProvider = PackageInformationProvider.get(xContext);
      String[][] extsTable = xPackageInformationProvider.getExtensionList();
      for (int i = 0; i < extsTable.length; i++) {
        MessageHandler.printToLogFile((i+1) + ".ID: " + extsTable[i][0]);
        MessageHandler.printToLogFile("   Loc: " + xPackageInformationProvider.getPackageLocation(extsTable[i][0]));
      }
      String location = xPackageInformationProvider.getPackageLocation("org.openoffice.languagetool.oxt");
      Object oTransformer;
      try {
        oTransformer = xContext.getServiceManager().createInstanceWithContext("com.sun.star.util.URLTransformer", xContext);
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
      XURLTransformer xTransformer = (XURLTransformer)UnoRuntime.queryInterface(XURLTransformer.class, oTransformer);
      com.sun.star.util.URL[] oURL = new com.sun.star.util.URL[1];
      oURL[0] = new com.sun.star.util.URL();
      file = location + "/" + file;
      oURL[0].Complete = file;
      MessageHandler.printToLogFile("File path: " + file);
      xTransformer.parseStrict(oURL);
      MessageHandler.printToLogFile("File path (after parse): " + oURL[0].Complete);
      URL url;
      try {
        url = new URL(oURL[0].Complete);
      } catch (Throwable t) {
        MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
        return null;
      }
      File f;
      try {
        f = new File(url.toURI());
      } catch (Throwable t) {
        MessageHandler.printException(t);     // all Exceptions thrown by UnoRuntime.queryInterface are caught
        return null;
      }
      return f;
    }
  }
  
  /** 
   * Class for spell checking in spell and grammar dialog
  */
  public class ExtensionSpellChecker {

    private static final String spellingError = "Spelling Error";
    private static final String spellRuleId = "SpellingError";
    private XComponentContext xContext;
    private JLanguageTool langtool;
    private LinguisticServices linguServices;
     
    ExtensionSpellChecker(XComponentContext xContext, JLanguageTool langtool) {
      this.xContext = xContext;
      this.langtool = langtool;
      linguServices = new LinguisticServices(xContext);
    }

    public SingleProofreadingError[] getSpellErrors(String sentence, Locale lang) {
      try {
        List<SingleProofreadingError> errorArray = new ArrayList<SingleProofreadingError>();
        AnalyzedSentence analyzedSentence = langtool.getAnalyzedSentence(sentence);
        AnalyzedTokenReadings[] tokens = analyzedSentence.getTokensWithoutWhitespace();
        for (AnalyzedTokenReadings token : tokens) {
          if(!token.isNonWord() && linguServices.isCorrectSpell(token.getToken(), lang)) {
            SingleProofreadingError aError = new SingleProofreadingError();
            aError.nErrorType = TextMarkupType.SPELLCHECK;
            aError.aFullComment = spellingError;
            aError.aShortComment = aError.aFullComment;
            aError.nErrorStart = token.getStartPos();
            aError.nErrorLength = token.getEndPos() - token.getStartPos();
            aError.aRuleIdentifier = spellRuleId;
            errorArray.add(aError);
            String[] alternatives = linguServices.getSpellAlternatives(token.getToken(), lang);
            if (alternatives != null) {
              aError.aSuggestions = alternatives;
            } else {
              aError.aSuggestions = new String[0];
            }
          }
        }
        return errorArray.toArray(new SingleProofreadingError[0]);
      } catch (Throwable t) {
        MessageHandler.showError(t);
      }
      return null;
    }
   
  } 

}
