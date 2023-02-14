package org.languagetool.gui;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.identifier.LanguageIdentifier;
import org.languagetool.language.identifier.LanguageIdentifierService;
import org.languagetool.rules.RuleMatch;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FunctionalTest {
  JFrame jf = new JFrame();
  JTextArea textArea = new JTextArea();
  LanguageIdentifier langIdentifier = LanguageIdentifierService.INSTANCE.getDefaultLanguageIdentifier(0, null, null, null);
  UndoRedoSupport undoRedo = new UndoRedoSupport(textArea, JLanguageTool.getMessageBundle());
  LanguageToolSupport ltSupport = new LanguageToolSupport(jf, textArea, undoRedo);

  @Test
  public void testSetTextFromDefault(){
    textArea.setText("Today's whether is bad");
    assertEquals(ltSupport.getTextComponent().getText(), "Today's whether is bad");
  }

  @Test
  public void testLanguageChange() {
    textArea.setText("Aujourd'hui est-ce mauvais");
    Language newLang = langIdentifier.detectLanguage(textArea.getText());
    ltSupport.setLanguage(newLang);
    assertEquals(newLang, ltSupport.getLanguage());
  }

  @Test
  public void testSetTextFromResult(){
    textArea.setText("Second test content");
    assertEquals(ltSupport.getTextComponent().getText(), "Second test content");
  }
  @Test
  public void testLanguageToolCheck() throws IOException {
    textArea.setText("This is a example input to to show you how LanguageTool works.");
    List<RuleMatch> rules = ltSupport.getLanguageTool().check("This is a example input to to show you how LanguageTool works.");
    assertTrue(rules.size() == 2);
  }

  @Test
  public void testShowResult() throws IOException {
    textArea.setText("This is a example input to to show you how LanguageTool works.");
    List<RuleMatch> rules = ltSupport.getLanguageTool().check("This is a example input to to show you how LanguageTool works.");
    List<String> expect = Arrays.asList("EN_A_VS_AN:8-9:Use <suggestion>an</suggestion> instead of 'a' if the following word starts with a vowel sound, e.g. 'an article', 'an hour'.",
      "ENGLISH_WORD_REPEAT_RULE:24-29:Possible typo: you repeated a word");
    for (int i = 0; i < rules.size(); i++) {
      assertEquals(rules.get(i).toString(), expect.get(i));
    }
  }

  @Test
  public void testDisableRuleFunction() {
    textArea.setText("This is is a test");
    ltSupport.disableRule("ENGLISH_WORD_REPEAT_RULE"); // disables rule
    Set<String> disabledRules = ltSupport.getLanguageTool().getDisabledRules();

    assertTrue(disabledRules.contains("ENGLISH_WORD_REPEAT_RULE")); // should be in disabled rules set because it was disabled
  }

  @Test
  public void testEnableRuleFunction() {
    textArea.setText("This is is a test");
    Set<String> disabledRules = ltSupport.getLanguageTool().getDisabledRules();

    assertTrue(!disabledRules.contains("ENGLISH_WORD_REPEAT_RULE")); // should not be in disabled rules set because it was not disabled
  }

}
