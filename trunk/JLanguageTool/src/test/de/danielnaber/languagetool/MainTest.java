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
import java.io.IOException;

import java.io.PrintStream;
import java.net.URISyntaxException;
import java.net.URI;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * 
 * @author Marcin Miłkowski
 * Tests the basic features of the command-line interface.
 *
 */
public class MainTest extends AbstractSecurityTestCase {

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
    }

    catch (ExitException e) {
      String output = new String(this.out.toByteArray());
      assertTrue(output.indexOf("Usage: java de.danielnaber.languagetool.Main [-r|--recursive] [-v|--verbose") != -1);
      assertEquals("Exit status", 1, e.status);
    }
  }

  public void testEnglishFile() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    try {
      final URL url = this.getClass().getResource("test-en.txt");
      final URI uri = new URI (url.toString());
      String[] args = new String[] {"-l", "en", uri.getPath()};

      Main.main(args);
      String output = new String(this.out.toByteArray());
      assertTrue(output.indexOf("Expected text language: English") == 0);
      assertTrue(output.indexOf("1.) Line 1, column 8, Rule ID: EN_A_VS_AN") != -1);  
    }
    catch (ExitException e) {                
      assertEquals("Exit status", 1, e.status);
    }
  }
  
  public void testEnglishStdIn1() throws IOException, ParserConfigurationException, SAXException {
    try {
      final String test = "This is an test.";
      final byte[] b = test.getBytes();
      System.setIn(new ByteArrayInputStream(b));
      String[] args = new String[] {"-l", "en"};

      Main.main(args);
      String output = new String(this.out.toByteArray());
      assertTrue(output.indexOf("Expected text language: English") == 0);
      assertTrue(output.indexOf("1.) Line 1, column 8, Rule ID: EN_A_VS_AN") != -1);
      
    }
    catch (ExitException e) {                
      assertEquals("Exit status", 1, e.status);
    }
  }

  public void testEnglishStdIn2() throws IOException, ParserConfigurationException, SAXException {
    try {
      final String test = "This is an test.";
      final byte[] b = test.getBytes();
      System.setIn(new ByteArrayInputStream(b));
      String[] args = new String[] {"-l", "en", "-"};

      Main.main(args);
      String output = new String(this.out.toByteArray());
      assertTrue(output.indexOf("Expected text language: English") == 0);
      assertTrue(output.indexOf("1.) Line 1, column 8, Rule ID: EN_A_VS_AN") != -1);      
    }
    catch (ExitException e) {                
      assertEquals("Exit status", 1, e.status);
    }
  }

  public void testPolishStdInDefaultOff() throws IOException, ParserConfigurationException, SAXException {
    try {
      final String test = "To jest test, który zrobiłem, który mi się podoba.";
      final byte[] b = test.getBytes();
      System.setIn(new ByteArrayInputStream(b));
      String[] args = new String[] {"-l", "pl", "-e", "PL_WORD_REPEAT", "-"};

      Main.main(args);
      String output = new String(this.out.toByteArray());
      assertTrue(output.indexOf("Expected text language: Polish") == 0);
      assertTrue(output.indexOf("Working on STDIN in a line mode") != -1);
      assertTrue(output.indexOf("1.) Line 1, column 30, Rule ID: PL_WORD_REPEAT") != -1);      
    }
    catch (ExitException e) {                
      assertEquals("Exit status", 1, e.status);
    }
  }
  
  public void testEnglishFileRuleDisabled() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    try {
      final URL url = this.getClass().getResource("test-en.txt");
      final URI uri = new URI (url.toString());
      String[] args = new String[] {"-l", "en", "-d", "EN_A_VS_AN", uri.getPath()};

      Main.main(args);
      String output = new String(this.out.toByteArray());
      assertTrue(output.indexOf("Expected text language: English") == 0);
      assertTrue(output.indexOf("Rule ID: EN_A_VS_AN") == -1);  
    }
    catch (ExitException e) {                
      assertEquals("Exit status", 1, e.status);
    }
  }

  public void testEnglishFileRuleEnabled() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    try {
      final URL url = this.getClass().getResource("test-en.txt");
      final URI uri = new URI (url.toString());
      String[] args = new String[] {"-l", "en", "-e", "EN_A_VS_AN", uri.getPath()};

      Main.main(args);
      String output = new String(this.out.toByteArray());
      assertTrue(output.indexOf("Expected text language: English") == 0);
      assertTrue(output.indexOf("Rule ID: EN_A_VS_AN") != -1);  
    }
    catch (ExitException e) {                
      assertEquals("Exit status", 1, e.status);
    }
  }
  
  public void testEnglishFileAPI() throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    try {
      final URL url = this.getClass().getResource("test-en.txt");
      final URI uri = new URI (url.toString());
      String[] args = new String[] {"-l", "en", "--api", uri.getPath()};

      Main.main(args);
      String output = new String(this.out.toByteArray());
      assertTrue(output.indexOf("<?xml version=\"1.0\" encoding=\"UTF-8\"?>") == 0);
      assertTrue(output.indexOf("<error fromy=\"0\" fromx=\"8\" toy=\"0\" tox=\"11\" ruleId=\"EN_A_VS_AN\" msg=\"Use 'a' instead of 'an' if the following word doesn't start with a vowel sound, e.g. 'a sentence', 'a university'\" replacements=\"a\" context=\"This is an test. \" contextoffset=\"8\" errorlength=\"2\"/>") != -1);  
    }
    catch (ExitException e) {                
      assertEquals("Exit status", 1, e.status);
    }
  }
  
  public void testEnglishTagger()  throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    try {
      final URL url = this.getClass().getResource("test-en.txt");
      final URI uri = new URI (url.toString());
      String[] args = new String[] {"-l", "en", "--taggeronly", uri.getPath()};
      Main.main(args);
      String output = new String(this.out.toByteArray());
      assertTrue(output.indexOf("Expected text language: English") == 0);
      assertTrue(output.indexOf("<S> This[this/DT,this/PDT]  is[be/VBZ]  an[a/DT]  test[test/JJ,test/NN,test/VB,test/VBP].[./.,</S>]") != -1);
    }
    catch (ExitException e) {             
      assertEquals("Exit status", 1, e.status);
    }
  }

  public void testListUnknown()  throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    try {
      final URL url = this.getClass().getResource("test-en.txt");
      final URI uri = new URI (url.toString());
      String[] args = new String[] {"-l", "pl", "-u", uri.getPath()};
      Main.main(args);
      String output = new String(this.out.toByteArray());
      assertTrue(output.indexOf("Expected text language: Polish") == 0);
      assertTrue(output.indexOf("Unknown words: [This, is]") != -1);
    }
    catch (ExitException e) {             
      assertEquals("Exit status", 1, e.status);
    }
  }
  
  public void testNoListUnknown()  throws URISyntaxException, IOException, ParserConfigurationException, SAXException {
    try {
      final URL url = this.getClass().getResource("test-en.txt");
      final URI uri = new URI (url.toString());
      String[] args = new String[] {"-l", "pl", uri.getPath()};
      Main.main(args);
      String output = new String(this.out.toByteArray());
      assertTrue(output.indexOf("Expected text language: Polish") == 0);
      assertTrue(output.indexOf("Unknown words: [This, is]") == -1);
    }
    catch (ExitException e) {             
      assertEquals("Exit status", 1, e.status);
    }
  }

}
