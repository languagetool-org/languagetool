package org.languagetool.tools;

import junit.framework.TestCase;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.rules.WordRepeatRule;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class ToolsTest extends TestCase {

  private ByteArrayOutputStream out;
  private PrintStream stdout;
  private PrintStream stderr;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    this.stdout = System.out;
    this.stderr = System.err;
    this.out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();      
    System.setOut(new PrintStream(this.out));
    System.setErr(new PrintStream(err));
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
    System.setOut(this.stdout);
    System.setErr(this.stderr);
  }

  public void testCheck() throws IOException, ParserConfigurationException, SAXException {
    final JLanguageTool tool = new JLanguageTool(Language.DEMO);
    tool.activateDefaultPatternRules();
    tool.activateDefaultFalseFriendRules();
 
    int matches = Tools.checkText("Foo.", tool);
    String output = new String(this.out.toByteArray());
    assertEquals(0, output.indexOf("Time:"));
    assertEquals(0, matches);

    tool.disableRule("test_unification_with_negation");
    tool.addRule(new WordRepeatRule(TestTools.getEnglishMessages(), Language.DEMO));
    matches = Tools.checkText("To jest problem problem.", tool);
    output = new String(this.out.toByteArray());
    assertTrue(output.contains("Rule ID: WORD_REPEAT_RULE"));
    assertEquals(1, matches);
  }

}
