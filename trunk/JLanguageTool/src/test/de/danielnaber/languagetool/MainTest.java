/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

package de.danielnaber.languagetool;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Tests the basic features of the command-line interface.
 *
 * @author Marcin Miłkowski
 */
public class MainTest extends AbstractSecurityTestCase {

  private static final String ENGLISH_TEST_FILE = "test-en.txt";
  
  private ByteArrayOutputStream out;
  private ByteArrayOutputStream err;
  private PrintStream stdout;
  private PrintStream stderr;

  public MainTest(String testName) {
    super(testName);
  }

  public void setUp() throws Exception {
    super.setUp();
    this.stdout = System.out;
    this.stderr = System.err;
    this.out = new ByteArrayOutputStream();
    this.err = new ByteArrayOutputStream();      
    System.setOut(new PrintStream(this.out));
    System.setErr(new PrintStream(this.err));
  }

  public void tearDown() throws Exception {
    super.tearDown();
    System.setOut(this.stdout);
    System.setErr(this.stderr);
  }

  public void testUsageMessage() throws IOException, ParserConfigurationException, SAXException {
    try {
      String[] args = new String[] {"-h"};
      Main.main(args);
      fail("LT should have exited with status 0!");
    } catch (ExitException e) {
      String output = new String(this.out.toByteArray());
      assertTrue(output.indexOf("Usage: java de.danielnaber.languagetool.Main [-r|--recursive] [-v|--verbose") != -1);
      assertEquals("Exit status", 1, e.status);
    }
  }

  public void testEnglishFile() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    //System.err.println("###"+url);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "en", uri.getPath()};
    
    Main.main(args);
    String output = new String(this.out.toByteArray());
    //System.out.println("#>"+output);
    assertTrue(output.indexOf("Expected text language: English") == 0);
    assertTrue(output.indexOf("1.) Line 1, column 9, Rule ID: EN_A_VS_AN") != -1);
  }
  
  public void testEnglishFileAutoDetect() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
	  final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
	  final URI uri = new URI (url.toString());
	  String[] args = new String[] {"-adl", uri.getPath()};
	  	  
	  Main.main(args);
	  String output = new String(this.out.toByteArray());
	  assertTrue(output.indexOf("Using English for file") == 0);
	  assertTrue(output.indexOf("1.) Line 1, column 9, Rule ID: EN_A_VS_AN") != -1);
  }
  
  public void testEnglishStdInAutoDetect() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
      final String test = "This is an test.";
      final byte[] b = test.getBytes();
      System.setIn(new ByteArrayInputStream(b));
      String[] args = new String[] {"-adl"};

      Main.main(args);
      String output = new String(this.out.toByteArray());
      assertTrue(output.indexOf("Working on STDIN...") == 0);
      assertTrue(output.indexOf("Language used is: English") != -1);
      assertTrue(output.indexOf("1.) Line 1, column 9, Rule ID: EN_A_VS_AN") != -1);
  }
  
  public void testEnglishFileVerbose() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "en", "-v", uri.getPath()};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English") == 0);
    assertTrue(output.indexOf("1.) Line 1, column 9, Rule ID: EN_A_VS_AN") != -1);
    String tagText = new String(this.err.toByteArray());
    assertTrue(tagText.indexOf("<S> This[this/DT]  is[be/VBZ]  an[a/DT]  test[test/NN].[./.,</S>]") != -1);
  }
  
  public void testEnglishFileApplySuggestions() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "en", "--apply", uri.getPath()};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertEquals("This is a test.\n", output);
  }

  
  public void testEnglishStdIn1() throws IOException, ParserConfigurationException, SAXException {
    final String test = "This is an test.";
    final byte[] b = test.getBytes();
    System.setIn(new ByteArrayInputStream(b));
    String[] args = new String[] {"-l", "en"};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English") == 0);
    assertTrue(output.indexOf("1.) Line 1, column 9, Rule ID: EN_A_VS_AN") != -1);
  }

  public void testEnglishStdIn2() throws IOException, ParserConfigurationException, SAXException {
    final String test = "This is an test.";
    final byte[] b = test.getBytes();
    System.setIn(new ByteArrayInputStream(b));
    String[] args = new String[] {"-l", "en", "-"};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English") == 0);
    assertTrue(output.indexOf("1.) Line 1, column 9, Rule ID: EN_A_VS_AN") != -1);
  }
  
  public void testEnglishStdIn3() throws IOException, ParserConfigurationException, SAXException {
    final String test = "This is an test.";
    final byte[] b = test.getBytes();
    System.setIn(new ByteArrayInputStream(b));
    String[] args = new String[] {"-l", "en", "-a", "-"};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertEquals("This is a test.\n", output);
  }
  
  //test line mode vs. para mode
  //first line mode
  public void testEnglishLineMode() throws IOException, ParserConfigurationException, SAXException {    
    final String test = "This is what I mean\nand you know it.";
    final byte[] b = test.getBytes();
    System.setIn(new ByteArrayInputStream(b));
    String[] args = new String[] {"-l", "en", "-a", "-b", "-"};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertEquals("This is what I mean\nAnd you know it.\n", output);
  }

  //first line mode
  public void testEnglishParaMode() throws IOException, ParserConfigurationException, SAXException {    
    final String test = "This is what I mean\nand you know it.";
    final byte[] b = test.getBytes();
    System.setIn(new ByteArrayInputStream(b));
    String[] args = new String[] {"-l", "en", "-a", "-"};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertEquals("This is what I mean\nand you know it.\n", output);
  }
  
  public void testPolishStdInDefaultOff() throws IOException, ParserConfigurationException, SAXException {
    final String test = "To jest test, który zrobiłem, który mi się podoba.";
    final byte[] b = test.getBytes();
    System.setIn(new ByteArrayInputStream(b));
    String[] args = new String[] {"-l", "pl", "-e", "PL_WORD_REPEAT", "-"};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: Polish") == 0);
    assertTrue(output.indexOf("Working on STDIN...") != -1);
    assertTrue(output.indexOf("1.) Line 1, column 31, Rule ID: PL_WORD_REPEAT") != -1);
  }
  
  public void testEnglishFileRuleDisabled() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "en", "-d", "EN_A_VS_AN", uri.getPath()};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English") == 0);
    assertTrue(output.indexOf("Rule ID: EN_A_VS_AN") == -1);
  }

  public void testEnglishFileRuleEnabled() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "en", "-e", "EN_A_VS_AN", uri.getPath()};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English") == 0);
    assertTrue(output.indexOf("Rule ID: EN_A_VS_AN") != -1);
  }
  
  public void testEnglishFileAPI() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "en", "--api", uri.getPath()};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") == 0);
    assertTrue(output.indexOf("<error fromy=\"0\" fromx=\"8\" toy=\"0\" tox=\"11\" ruleId=\"EN_A_VS_AN\" msg=\"Use 'a' instead of 'an' if the following word doesn't start with a vowel sound, e.g. 'a sentence', 'a university'\" replacements=\"a\" context=\"This is an test. \" contextoffset=\"8\" errorlength=\"2\"/>") != -1);
  }
  
  public void testPolishFileAPI() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    File input = createTempFile();

    // Populate the file with data.
    PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(input), "UTF-8"));
    w.println("To jest świnia która się ślini.");
    w.close();

    String[] args = new String[] {"-l", "pl", "--api", input.getAbsolutePath()};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") == 0);
    assertTrue(output.indexOf("<error fromy=\"0\" fromx=\"8\" toy=\"0\" tox=\"21\" ruleId=\"BRAK_PRZECINKA_KTORY\" subId=\"5\"") != -1);
    //This tests whether XML encoding is actually UTF-8:
    assertTrue(output.indexOf("msg=\"Brak przecinka w tym fragmencie zdania. Przecinek prawdopodobnie należy postawić tak: 'świnia, która'.\" replacements=\"świnia, która\" ")  != -1);
    assertTrue(output.indexOf("context=\"To jest świnia która się ślini. \" contextoffset=\"8\" errorlength=\"12\"/>") != -1);
  }
  
  public void testPolishLineNumbers() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    File input = createTempFile();

    // Populate the file with data.
    PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(input), "UTF-8"));
    w.println("Test.");
    w.println("Test.");
    w.println("Test.");
    w.println("Test.");
    w.println("Test.");
    w.println("Test.");
    w.println("");
    w.println("Test który wykaże błąd.");
    w.close();

    String[] args = new String[] {"-l", "pl", input.getAbsolutePath()};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: Polish") == 0);
    assertTrue(output.indexOf("Line 8, column 1, Rule ID: BRAK_PRZECINKA_KTORY") != -1);
  }

  private File createTempFile() throws IOException {
    File input = File.createTempFile(MainTest.class.getName(), ".txt");
    input.deleteOnExit();
    return input;
  }

  public void testEnglishTagger()  throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "en", "--taggeronly", uri.getPath()};
    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English") == 0);
    assertTrue(output.indexOf("<S> This[this/DT]  is[be/VBZ]  an[a/DT]  test[test/NN].[./.,</S>]") != -1);
  }

  public void testBitextMode()  throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    File input = createTempFile();

    // Populate the file with data.
    PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(input), "UTF-8"));
    w.println("This is not actual.\tTo nie jest aktualne.");
    w.println("Test\tTest");
    w.println("ab\tVery strange data indeed, much longer than input");
    w.close();

    String[] args = new String[] {"-l", "pl", "--bitext", "-m", "en", input.getAbsolutePath()};
    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: Polish") == 0);
    assertTrue(output.indexOf(
        "Message: Hint: \"aktualny\" (Polish) means \"current\", \"(the) latest\", \"up-to-date\" (English). Did you mean 'rzeczywisty'?") != -1);
    assertTrue(output.indexOf("Line 1, column 32, Rule ID: ACTUAL") != -1);
    assertTrue(output.indexOf("Line 3, column 4, Rule ID: TRANSLATION_LENGTH") != -1);
  }
  
  public void testBitextModeWithDisabledRule()  throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    File input = createTempFile();

    // Populate the file with data.
    PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(input), "UTF-8"));
    w.println("this is not actual.\tTo nie jest aktualne.");
    w.println("test\tTest");
    w.println("ab\tVery strange data indeed, much longer than input");
    w.close();

    String[] args = new String[] {"-l", "pl", "--bitext", "-m", "en", "-d", "UPPERCASE_SENTENCE_START,TRANSLATION_LENGTH", input.getAbsolutePath()};
    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: Polish") == 0);
    assertTrue(output.indexOf(
        "Message: Hint: \"aktualny\" (Polish) means \"current\", \"(the) latest\", \"up-to-date\" (English). Did you mean 'rzeczywisty'?") != -1);
    assertTrue(output.indexOf("Line 1, column 32, Rule ID: ACTUAL") != -1);
    assertTrue(output.indexOf("Rule ID: TRANSLATION_LENGTH") == -1);
  }
  
  public void testBitextModeWithEnabledRule()  throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    File input = createTempFile();

    // Populate the file with data.
    PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(input), "UTF-8"));
    w.println("this is not actual.\tTo nie jest aktualne.");
    w.println("test\tTest");
    w.println("ab\tVery strange data indeed, much longer than input");
    w.close();

    String[] args = new String[] {"-l", "pl", "--bitext", "-m", "en", "-e", "TRANSLATION_LENGTH", input.getAbsolutePath()};
    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: Polish") == 0);
    assertTrue(output.indexOf(
        "Message: Hint: \"aktualny\" (Polish) means \"current\", \"(the) latest\", \"up-to-date\" (English). Did you mean 'rzeczywisty'?") == -1);
    assertTrue(output.indexOf("Line 1, column 32, Rule ID: ACTUAL") == -1);
    assertTrue(output.indexOf("Rule ID: TRANSLATION_LENGTH") != -1);
  }
  
  public void testBitextModeApply()  throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    File input = createTempFile();

    // Populate the file with data.
    PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(input), "UTF-8"));
    w.println("There is a dog.\tNie ma psa.");    
    w.close();

    String[] args = new String[] {"-l", "pl", "--bitext", "-m", "en", "--apply", input.getAbsolutePath()};
    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.startsWith("Istnieje psa."));
  }
  
  public void testListUnknown()  throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "pl", "-u", uri.getPath()};
    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: Polish") == 0);
    assertTrue(output.indexOf("Unknown words: [This, is]") != -1);
  }
  
  public void testNoListUnknown()  throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "pl", uri.getPath()};
    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: Polish") == 0);
    assertTrue(output.indexOf("Unknown words: [This, is]") == -1);
  }

}
