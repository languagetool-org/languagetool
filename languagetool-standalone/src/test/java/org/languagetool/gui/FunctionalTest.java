package org.languagetool.gui;

import org.junit.Test;
import static org.junit.Assert.*;
import org.languagetool.JLanguageTool;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;

import org.languagetool.Language;
import org.languagetool.gui.Main;

public class FunctionalTest {
  ResourceBundle messages = JLanguageTool.getMessageBundle();
  
  @Test
  public void testDisableRuleFunction() throws IOException {
    JTextArea jTextArea = new JTextArea("This is is a test");
    UndoRedoSupport undoRedoSupport = new UndoRedoSupport(jTextArea, messages);
    LanguageToolSupport ltSupport = new LanguageToolSupport(new JFrame(), jTextArea, undoRedoSupport);

    ltSupport.disableRule("ENGLISH_WORD_REPEAT_RULE"); // disables rule
    Set<String> disabledRules = ltSupport.getLanguageTool().getDisabledRules();

    assertTrue(disabledRules.contains("ENGLISH_WORD_REPEAT_RULE")); // should be in disabled rules set because it was disabled
  }

  @Test
  public void testEnableRuleFunction() throws IOException {
    JTextArea jTextArea = new JTextArea("This is is a test");
    UndoRedoSupport undoRedoSupport = new UndoRedoSupport(jTextArea, messages);
    LanguageToolSupport ltSupport = new LanguageToolSupport(new JFrame(), jTextArea, undoRedoSupport);

    Set<String> disabledRules = ltSupport.getLanguageTool().getDisabledRules();

    assertTrue(!disabledRules.contains("ENGLISH_WORD_REPEAT_RULE")); // should not be in disabled rules set because it was not disabled
  }

}
