package org.languagetool.gui;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.identifier.LanguageIdentifier;
import org.languagetool.language.identifier.LanguageIdentifierService;
import org.mockito.Mock;

import javax.swing.*;
import java.io.IOException;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class ResultAreaHelperTest {
  ResultAreaHelper resultAreaHelper = mock(ResultAreaHelper.class);
  @Mock
  LanguageToolSupport languageToolSupport;


  @Test
  public void testHandleRuleLinkClick() throws IOException {

    resultAreaHelper.handleRuleLinkClick("http://languagetool.org/deactivate/EN_A_VS_AN");
    verify(resultAreaHelper, times(1)).handleRuleLinkClick("http://languagetool.org/deactivate/EN_A_VS_AN");
//    JFrame jf = new JFrame();
//    JTextArea textArea = new JTextArea();
//    UndoRedoSupport undoRedo = new UndoRedoSupport(textArea, JLanguageTool.getMessageBundle());
//    languageToolSupport = new LanguageToolSupport(jf, textArea, undoRedo);
//    verify(languageToolSupport, atLeastOnce()).disableRule("EN_A_VS_AN");
  }

  @Test
  public void testGetDisabledRulesHTML(){

    when(resultAreaHelper.getDisabledRulesHtml()).thenReturn("<br>Deactivated rules - click to activate again: <a href=\"http://languagetool.org/reactivate/EN_A_VS_AN\">Use of 'a' vs. 'an'</a>, <a href=\"http://languagetool.org/reactivate/ENGLISH_WORD_REPEAT_RULE\">Word repetition (e.g. 'will will')</a><br>");
    verify(resultAreaHelper, times(1)).getDisabledRulesHtml();
  }

}
