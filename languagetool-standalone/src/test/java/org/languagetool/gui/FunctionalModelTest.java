package org.languagetool.gui;

import org.junit.Test;
import org.languagetool.JLanguageTool;

import javax.swing.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;

import org.languagetool.Language;
import org.languagetool.gui.Main;

public class FunctionalModelTest {
  ResourceBundle messages = JLanguageTool.getMessageBundle();
  @Test
  public void testSimpleText(){
    JTextArea jTextArea = new JTextArea("This is a test");
    UndoRedoSupport undoRedoSupport = new UndoRedoSupport(jTextArea, messages);
    LanguageToolSupport ltSupport = new LanguageToolSupport(new JFrame(), jTextArea, undoRedoSupport);
    System.out.println(ltSupport);
//    Method fireEvent = ltSupport.getClass().getMethod("fireEvent");
  }

  public void addEventListener(LanguageToolSupport ltSupport){
    ltSupport.addLanguageToolListener(new LanguageToolListener() {
      @Override
      public void languageToolEventOccurred(LanguageToolEvent event) {
        if (event.getType() == LanguageToolEvent.Type.CHECKING_STARTED) {
          String msg = org.languagetool.tools.Tools.i18n(messages, "checkStart");
          System.out.println(msg);
//          statusLabel.setText(msg);
//          if (event.getCaller() == getFrame()) {
//            setWaitCursor();
//            checkAction.setEnabled(false);
//          }
        } else if (event.getType() == LanguageToolEvent.Type.CHECKING_FINISHED) {
//          if (event.getCaller() == getFrame()) {
//            checkAction.setEnabled(true);
//            unsetWaitCursor();
//          }
          String msg = org.languagetool.tools.Tools.i18n(messages, "checkDone", event.getSource().getMatches().size(), event.getElapsedTime());
          System.out.println(msg);
//          statusLabel.setText(msg);
        } else if (event.getType() == LanguageToolEvent.Type.LANGUAGE_CHANGED) {
//          languageBox.selectLanguage(ltSupport.getLanguage());
          System.out.println("LANGUAGE_CHANGED");
        } else if (event.getType() == LanguageToolEvent.Type.RULE_ENABLED) {
          //this will trigger a check and the result will be updated by
          //the CHECKING_FINISHED event
          System.out.println("RULE_ENABLED");
        } else if (event.getType() == LanguageToolEvent.Type.RULE_DISABLED) {
          String msg = org.languagetool.tools.Tools.i18n(messages, "checkDoneNoTime", event.getSource().getMatches().size());
          System.out.println(msg);
//          statusLabel.setText(msg);
        }
      }
    });
  }

  //    LocalStorage localStorage = new LocalStorage();
//    LocaleBean bean = localStorage.loadProperty("gui.locale", LocaleBean.class);
//    if(bean != null) {
//      Locale.setDefault(bean.asLocale());
//    }
//    Main prg = new Main(localStorage);
//    SwingUtilities.invokeLater(new Runnable() {
//      @Override
//      public void run() {
//        try {
//          prg.createGUI();
////          prg.showGUI();
//        } catch (Exception e) {
//          Tools.showError(e);
//        }
//      }
//    });
//    Class<Main> aClass = (Class<Main>) prg.getClass();
//    Field ltSupport = aClass.getDeclaredField("ltSupport");
//    System.out.println(ltSupport);
}
