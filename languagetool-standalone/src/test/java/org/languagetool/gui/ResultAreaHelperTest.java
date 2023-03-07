package org.languagetool.gui;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

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
  JFrame jf = new JFrame();
  JTextArea textArea = new JTextArea();
  UndoRedoSupport undoRedo = new UndoRedoSupport(textArea, JLanguageTool.getMessageBundle());
  LanguageToolSupport ltSupport = new LanguageToolSupport(jf, textArea, undoRedo);
  LanguageToolEvent event = new LanguageToolEvent(ltSupport, LanguageToolEvent.Type.CHECKING_FINISHED, null);
  @Spy
  ResultAreaHelper spyResultAreaHelper = new ResultAreaHelper(JLanguageTool.getMessageBundle(), ltSupport, new JTextPane());

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testHandleRuleLinkClick() throws IOException {
    spyResultAreaHelper.handleRuleLinkClick("http://languagetool.org/deactivate/EN_A_VS_AN");
    verify(spyResultAreaHelper, times(1)).handleRuleLinkClick("http://languagetool.org/deactivate/EN_A_VS_AN");
  }

  @Test
  public void testHyperLinkUpdate() throws URISyntaxException, MalformedURLException {
    URL url = new URI("http://languagetool.org/deactivate/EN_A_VS_AN").toURL();
    HyperlinkEvent event = new HyperlinkEvent(url, HyperlinkEvent.EventType.ACTIVATED, url);
    spyResultAreaHelper.hyperlinkUpdate(event);
    verify(spyResultAreaHelper, times(1)).hyperlinkUpdate(event);
  }

  @Test
  public void testFilterRuleMatches(){
    spyResultAreaHelper.filterRuleMatches(event.getSource().getMatches());
    verify(spyResultAreaHelper, times(1)).filterRuleMatches(event.getSource().getMatches());
  }

  @Test
  public void testDisplayResult(){
    spyResultAreaHelper.displayResult("This is an example input.", event.getSource().getMatches());
    verify(spyResultAreaHelper, times(1)).displayResult("This is an example input.", event.getSource().getMatches());
    verify(spyResultAreaHelper, times(1)).getRuleMatchHtml(event.getSource().getMatches(), "This is an example input.");
    verify(spyResultAreaHelper, times(1)).filterRuleMatches(event.getSource().getMatches());
  }

}
