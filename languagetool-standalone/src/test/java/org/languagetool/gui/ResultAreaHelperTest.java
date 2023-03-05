package org.languagetool.gui;

import org.junit.Test;
import org.languagetool.JLanguageTool;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import static org.mockito.Mockito.*;

public class ResultAreaHelperTest {
  ResultAreaHelper resultAreaHelper = mock(ResultAreaHelper.class);
  JFrame jf = new JFrame();
  JTextArea textArea = new JTextArea();
  UndoRedoSupport undoRedo = new UndoRedoSupport(textArea, JLanguageTool.getMessageBundle());
  LanguageToolSupport ltSupport = new LanguageToolSupport(jf, textArea, undoRedo);
  LanguageToolEvent event = new LanguageToolEvent(ltSupport, LanguageToolEvent.Type.CHECKING_FINISHED, null);

  @Test
  public void testHandleRuleLinkClick() throws IOException {
    resultAreaHelper.handleRuleLinkClick("http://languagetool.org/deactivate/EN_A_VS_AN");
    verify(resultAreaHelper, times(1)).handleRuleLinkClick("http://languagetool.org/deactivate/EN_A_VS_AN");
  }

  @Test
  public void testHyperLinkUpdate() throws URISyntaxException, MalformedURLException {
    URL url = new URI("http://languagetool.org/deactivate/EN_A_VS_AN").toURL();
    HyperlinkEvent event = new HyperlinkEvent(url, HyperlinkEvent.EventType.ACTIVATED, url);
    resultAreaHelper.hyperlinkUpdate(event);
    verify(resultAreaHelper, times(1)).hyperlinkUpdate(event);
  }

  @Test
  public void testFilterRuleMatches(){
    resultAreaHelper.filterRuleMatches(event.getSource().getMatches());
    verify(resultAreaHelper, times(1)).filterRuleMatches(event.getSource().getMatches());
  }

  @Test
  public void testDisplayResult(){
    resultAreaHelper.displayResult("This is an example input.", event.getSource().getMatches());
    verify(resultAreaHelper, times(1)).displayResult("This is an example input.", event.getSource().getMatches());
//    verify(resultAreaHelper, times(1)).getRuleMatchHtml(event.getSource().getMatches(), "This is an example input.");
//    verify(resultAreaHelper, times(1)).filterRuleMatches(event.getSource().getMatches());
  }

}
