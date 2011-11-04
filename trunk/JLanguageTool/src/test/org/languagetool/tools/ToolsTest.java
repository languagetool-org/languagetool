package de.danielnaber.languagetool.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.bitext.BitextRule;

public class ToolsTest extends TestCase {

  private ByteArrayOutputStream out;
  private PrintStream stdout;
  private PrintStream stderr;

  public void setUp() throws Exception {
    super.setUp();
    this.stdout = System.out;
    this.stderr = System.err;
    this.out = new ByteArrayOutputStream();
    final ByteArrayOutputStream err = new ByteArrayOutputStream();      
    System.setOut(new PrintStream(this.out));
    System.setErr(new PrintStream(err));
  }

  public void tearDown() throws Exception {
    super.tearDown();
    System.setOut(this.stdout);
    System.setErr(this.stderr);
  }

  public void testCheck() throws IOException, ParserConfigurationException, SAXException {
    final JLanguageTool tool = new JLanguageTool(Language.POLISH);
    tool.activateDefaultPatternRules();
    tool.activateDefaultFalseFriendRules();
 
    int matches = Tools.checkText("To jest całkowicie prawidłowe zdanie.", tool);
    String output = new String(this.out.toByteArray());
    assertEquals(0, output.indexOf("Time:"));
    assertEquals(0, matches);

    matches = Tools.checkText("To jest jest problem.", tool);
    output = new String(this.out.toByteArray());
    assertTrue(output.contains("Rule ID: WORD_REPEAT_RULE"));
    assertEquals(1, matches);
  }

  public void testCorrect() throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool tool = new JLanguageTool(Language.POLISH);
    tool.activateDefaultPatternRules();
    tool.activateDefaultFalseFriendRules();

    String correct = Tools.correctText("To jest całkowicie prawidłowe zdanie.", tool);
    assertEquals("To jest całkowicie prawidłowe zdanie.", correct);
    correct = Tools.correctText("To jest jest problem.", tool);
    assertEquals("To jest problem.", correct);

    // more sentences, need to apply more suggestions > 1 in subsequent sentences
    correct = Tools.correctText("To jest jest problem. Ale to już już nie jest problem.", tool);
    assertEquals("To jest problem. Ale to już nie jest problem.", correct);
    correct = Tools.correctText("To jest jest problem. Ale to już już nie jest problem. Tak sie nie robi. W tym zdaniu brakuje przecinka bo go zapomniałem.", tool);
    assertEquals("To jest problem. Ale to już nie jest problem. Tak się nie robi. W tym zdaniu brakuje przecinka, bo go zapomniałem.", correct);
    
    //now English
    tool = new JLanguageTool(Language.ENGLISH);
    tool.activateDefaultPatternRules();
    tool.activateDefaultFalseFriendRules();

    assertEquals("This is a test.", Tools.correctText("This is an test.", tool));

  }
  
  public void testBitextCheck() throws IOException, ParserConfigurationException, SAXException {
    final JLanguageTool srcTool = new JLanguageTool(Language.ENGLISH);    
    final JLanguageTool trgTool = new JLanguageTool(Language.POLISH);    
    trgTool.activateDefaultPatternRules();
    
    final List<BitextRule> rules = Tools.getBitextRules(Language.ENGLISH, Language.POLISH);            
    
    int matches = Tools.checkBitext(
        "This is a perfectly good sentence.",
        "To jest całkowicie prawidłowe zdanie.", srcTool, trgTool, rules,
        false, StringTools.XmlPrintMode.NORMAL_XML);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Time:") == 0);
    assertEquals(0, matches);

    matches = Tools.checkBitext(
        "This is not actual.", 
        "To nie jest aktualne.", 
        srcTool, trgTool, 
        rules, false, StringTools.XmlPrintMode.NORMAL_XML);        
    output = new String(this.out.toByteArray());
    assertTrue(output.contains("Rule ID: ACTUAL"));
    assertEquals(1, matches);
  }  
}
