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

package org.languagetool;

import java.io.*;
import java.net.URI;
import java.net.URL;

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

  public void testUsageMessage() throws Exception {
    try {
      String[] args = new String[] {"-h"};
      Main.main(args);
      fail("LT should have exited with status 0!");
    } catch (ExitException e) {
      String output = new String(this.out.toByteArray());
      assertTrue(output.contains("Usage: java -jar LanguageTool.jar"));
      assertEquals("Exit status", 1, e.status);
    }
  }

  public void testEnglishFile() throws Exception {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "en", uri.getPath()};
    
    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English") == 0);
    assertTrue(output.contains("1.) Line 1, column 9, Rule ID: EN_A_VS_AN"));
  }
  
  public void testEnglishFileAutoDetect() throws Exception {
	  final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
	  final URI uri = new URI (url.toString());
	  String[] args = new String[] {"-adl", uri.getPath()};
	  	  
	  Main.main(args);
	  String output = new String(this.out.toByteArray());
	  assertTrue(output.indexOf("Using English for file") == 0);
	  assertTrue(output.contains("1.) Line 1, column 9, Rule ID: EN_A_VS_AN"));
  }
  
  public void testEnglishStdInAutoDetect() throws Exception {
    final String test = "This is an test.";
    final byte[] b = test.getBytes();
    System.setIn(new ByteArrayInputStream(b));
    String[] args = new String[] {"-adl"};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Working on STDIN...") == 0);
    assertTrue(output.contains("Language used is: English"));
    assertTrue(output.contains("1.) Line 1, column 9, Rule ID: EN_A_VS_AN"));
  }
  
  public void testEnglishFileVerbose() throws Exception {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "en", "-v", uri.getPath()};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English") == 0);
    assertTrue(output.contains("1.) Line 1, column 9, Rule ID: EN_A_VS_AN"));
    String tagText = new String(this.err.toByteArray());
    assertTrue(tagText.contains("<S> This[this/DT]  is[be/VBZ]  an[a/DT]  test[test/NN].[./.,</S>]"));
  }
  
  public void testEnglishFileApplySuggestions() throws Exception {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "en", "--apply", uri.getPath()};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertEquals("This is a test.\n", output);
  }

  
  public void testEnglishStdIn1() throws Exception {
    final String test = "This is an test.";
    final byte[] b = test.getBytes();
    System.setIn(new ByteArrayInputStream(b));
    String[] args = new String[] {"-l", "en"};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English") == 0);
    assertTrue(output.contains("1.) Line 1, column 9, Rule ID: EN_A_VS_AN"));
  }

  public void testEnglishStdIn2() throws Exception {
    final String test = "This is an test.";
    final byte[] b = test.getBytes();
    System.setIn(new ByteArrayInputStream(b));
    String[] args = new String[] {"-l", "en", "-"};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English") == 0);
    assertTrue(output.contains("1.) Line 1, column 9, Rule ID: EN_A_VS_AN"));
  }
  
  public void testEnglishStdIn3() throws Exception {
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
  public void testEnglishLineMode() throws Exception {
    final String test = "This is what I mean\nand you know it.";
    final byte[] b = test.getBytes();
    System.setIn(new ByteArrayInputStream(b));
    String[] args = new String[] {"-l", "en", "-a", "-b", "-"};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertEquals("This is what I mean\nAnd you know it.\n", output);
  }

  //first line mode
  public void testEnglishParaMode() throws Exception {
    final String test = "This is what I mean\nand you know it.";
    final byte[] b = test.getBytes();
    System.setIn(new ByteArrayInputStream(b));
    String[] args = new String[] {"-l", "en", "-a", "-"};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertEquals("This is what I mean\nand you know it.\n", output);
  }
  
  public void testPolishStdInDefaultOff() throws Exception {
    final String test = "To jest test, który zrobiłem, który mi się podoba.";
    final byte[] b = test.getBytes();
    System.setIn(new ByteArrayInputStream(b));
    String[] args = new String[] {"-l", "pl", "-e", "PL_WORD_REPEAT", "-"};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: Polish") == 0);
    assertTrue(output.contains("Working on STDIN..."));
    assertTrue(output.contains("1.) Line 1, column 31, Rule ID: PL_WORD_REPEAT"));
  }
  
  public void testPolishSpelling() throws Exception {
	    final String test = "Zwuasdac?";
	    final byte[] b = test.getBytes();
	    System.setIn(new ByteArrayInputStream(b));
	    String[] args = new String[] {"-l", "pl", "-e", "MORFOLOGIK_RULE", "-"};

	    Main.main(args);
	    String output = new String(this.out.toByteArray());
	    assertTrue(output.indexOf("Expected text language: Polish") == 0);
	    assertTrue(output.contains("Working on STDIN..."));
	    assertTrue(output.contains("1.) Line 1, column 1, Rule ID: MORFOLOGIK_RULE"));
	  }

  
  public void testEnglishFileRuleDisabled() throws Exception {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "en", "-d", "EN_A_VS_AN", uri.getPath()};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English") == 0);
    assertTrue(!output.contains("Rule ID: EN_A_VS_AN"));
  }

  public void testEnglishFileRuleEnabled() throws Exception {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "en", "-e", "EN_A_VS_AN", uri.getPath()};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English") == 0);
    assertTrue(output.contains("Rule ID: EN_A_VS_AN"));
  }

  public void testEnglishFileFakeRuleEnabled() throws Exception {
    final String test = "Zwuasdac?";
    final byte[] b = test.getBytes();
    System.setIn(new ByteArrayInputStream(b));
    String[] args = new String[] {"-l", "en", "-e", "FOO_BAR_BLABLA", "-"};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English") == 0);
  }

  
  
  public void testEnglishFileAPI() throws Exception {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "en", "--api", uri.getPath()};

    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") == 0);
    assertTrue(output.contains("<error fromy=\"0\" fromx=\"8\" toy=\"0\" tox=\"11\" ruleId=\"EN_A_VS_AN\" " +
            "msg=\"Use 'a' instead of 'an' if the following word doesn't start with a vowel sound, e.g. 'a sentence', " +
            "'a university'\" replacements=\"a\" context=\"This is an test. \" contextoffset=\"8\" errorlength=\"2\"/>"));
  }
  
  public void testGermanFileWithURL() throws Exception {

	    File input = createTempFile();
	    // Populate the file with data.
	    PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(input), "UTF-8"));
	    w.println("Ward ihr zufrieden damit?");
	    w.close();

	    String[] args = new String[] {"-l", "de", "--api", input.getAbsolutePath()};

	    Main.main(args);
	    String output = new String(this.out.toByteArray());
	    assertTrue(output.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") == 0);
	    assertTrue(output.contains("ruleId=\"WARD_VS_WART\" subId=\"1\""));
	    //check URL part
	    assertTrue(output.contains("url=\"http://www.korrekturen.de/beliebte_fehler/ward.shtml\""));
	    
	    //now check in normal mode and check for URL
	    args = new String[] {"-l", "de", input.getAbsolutePath()};
	    Main.main(args);
	    output = new String(this.out.toByteArray());
	    assertTrue(output.contains("More info: http://www.korrekturen.de/beliebte_fehler/ward.shtml"));
	  }
 
  
  public void testPolishFileAPI() throws Exception {
    File input = createTempFile();

    // Populate the file with data.
    PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(input), "UTF-8"));
    w.println("To jest świnia która się ślini.");
    w.close();

    String[] args = new String[] {"-l", "pl", "--api", "-c", "utf-8", input.getAbsolutePath()};

    Main.main(args);
    String output = new String(this.out.toByteArray(),"UTF-8");
    assertTrue(output.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") == 0);
    assertTrue(output.contains("<error fromy=\"0\" fromx=\"8\" toy=\"0\" tox=\"21\" ruleId=\"BRAK_PRZECINKA_KTORY\" subId=\"5\""));
    //This tests whether XML encoding is actually UTF-8:
    assertTrue(output.contains("msg=\"Brak przecinka w tym fragmencie zdania. Przecinek prawdopodobnie należy postawić tak: 'świnia, która'.\" replacements=\"świnia, która\" "));
    assertTrue(output.contains("context=\"To jest świnia która się ślini. \" contextoffset=\"8\" errorlength=\"12\"/>"));
  }
  
  public void testPolishLineNumbers() throws Exception {
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

    String[] args = new String[] {"-l", "pl", "-c", "utf-8", input.getAbsolutePath()};

    Main.main(args);
    String output = new String(this.out.toByteArray(),"UTF-8");
    assertTrue(output.indexOf("Expected text language: Polish") == 0);
    assertTrue(output.contains("Line 8, column 1, Rule ID: BRAK_PRZECINKA_KTORY"));
  }

  private File createTempFile() throws IOException {
    File input = File.createTempFile(MainTest.class.getName(), ".txt");
    input.deleteOnExit();
    return input;
  }

  public void testEnglishTagger() throws Exception {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "en", "--taggeronly", uri.getPath()};
    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English") == 0);
    assertTrue(output.contains("<S> This[this/DT]  is[be/VBZ]  an[a/DT]  test[test/NN].[./.,</S>]"));
  }

  public void testBitextMode() throws Exception {
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
    assertTrue(output.contains("Message: Hint: \"aktualny\" (Polish) means \"current\", \"(the) latest\", \"up-to-date\" (English). Did you mean 'rzeczywisty'?"));
    assertTrue(output.contains("Line 1, column 32, Rule ID: ACTUAL"));
    assertTrue(output.contains("Line 3, column 4, Rule ID: TRANSLATION_LENGTH"));
  }
  
  public void testBitextModeWithDisabledRule() throws Exception {
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
    assertTrue(output.contains("Message: Hint: \"aktualny\" (Polish) means \"current\", \"(the) latest\", \"up-to-date\" (English). Did you mean 'rzeczywisty'?"));
    assertTrue(output.contains("Line 1, column 32, Rule ID: ACTUAL"));
    assertFalse(output.contains("Rule ID: TRANSLATION_LENGTH"));
  }
  
  public void testBitextModeWithEnabledRule() throws Exception {
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
    assertFalse(output.contains("Message: Hint: \"aktualny\" (Polish) means \"current\", \"(the) latest\", \"up-to-date\" (English). Did you mean 'rzeczywisty'?"));
    assertFalse(output.contains("Line 1, column 32, Rule ID: ACTUAL"));
    assertTrue(output.contains("Rule ID: TRANSLATION_LENGTH"));
  }
  
  public void testBitextModeApply() throws Exception {
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
  
  public void testListUnknown() throws Exception {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "pl", "-u", uri.getPath()};
    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: Polish") == 0);
    assertTrue(output.contains("Unknown words: [This, an, is]"));
  }
  
  public void testNoListUnknown() throws Exception {
    final URL url = this.getClass().getResource(ENGLISH_TEST_FILE);
    final URI uri = new URI (url.toString());
    String[] args = new String[] {"-l", "pl", uri.getPath()};
    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: Polish") == 0);
    assertTrue(!output.contains("Unknown words: [This, an, is]"));
  }
  
  public void testLangWithCountryVariant() throws Exception {
    File input = createTempFile();
    // Populate the file with data.
    PrintWriter w = new PrintWriter(new OutputStreamWriter(new FileOutputStream(input), "UTF-8"));
    w.println("This is modelling.");
    w.close();
    String[] args = new String[] {"-l", "en-US", input.getAbsolutePath()};
    Main.main(args);
    String output = new String(this.out.toByteArray());
    assertTrue(output.indexOf("Expected text language: English (US)") == 0);
    assertTrue(output.contains("HUNSPELL_RULE"));
  }

}
