/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashSet;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.XMLValidator;
import de.danielnaber.languagetool.tools.StringTools;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;

public class HTTPServerTest extends TestCase {

  public void testHTTPServer() throws Exception {
    final HTTPServer server = new HTTPServer();
    try {
      server.run();
      runTests();
    } finally {
      server.stop();
    }
  }

  void runTests() throws IOException, SAXException, ParserConfigurationException {
    // no error:
    final String enc = "UTF-8";
    assertEquals("<?xml version=\"1.0\" encoding=\""+enc+"\"?>\n<matches>\n</matches>\n", check(Language.GERMAN, ""));
    assertEquals("<?xml version=\"1.0\" encoding=\""+enc+"\"?>\n<matches>\n</matches>\n", check(Language.GERMAN, "Ein kleiner test"));
    // one error:
    assertTrue(check(Language.GERMAN, "ein kleiner test").indexOf("UPPERCASE_SENTENCE_START") != -1);
    // two errors:
    final String result = check(Language.GERMAN, "ein kleiner test. Und wieder Erwarten noch was: \u00f6\u00e4\u00fc\u00df.");
    assertTrue(result.indexOf("UPPERCASE_SENTENCE_START") != -1);
    assertTrue(result.indexOf("WIEDER_WILLEN") != -1);
    assertTrue("Expected special chars, got: '" + result+ "'",
        result.indexOf("\u00f6\u00e4\u00fc\u00df") != -1);   // special chars are intact
    final XMLValidator validator = new XMLValidator();
    validator.validateXMLString(result, JLanguageTool.getDataBroker().getResourceDir() + "/api-output.dtd", "matches");
    validator.checkSimpleXMLString(result);
    //System.err.println(result);
    // make sure XML chars are escaped in the result to avoid invalid XML
    // and XSS attacks:
    assertTrue(check(Language.GERMAN, "bla <script>").indexOf("<script>") == -1);

    // other tests for special characters
    final String germanSpecialChars = check(Language.GERMAN, "ein kleiner test. Und wieder Erwarten noch was: öäüß öäüß.");
    assertTrue("Expected special chars, got: '" + germanSpecialChars + "'", germanSpecialChars.contains("öäüß"));
    final String romanianSpecialChars = check(Language.ROMANIAN, "bla bla șțîâă șțîâă și câteva caractere speciale");
    assertTrue("Expected special chars, got: '" + romanianSpecialChars + "'", romanianSpecialChars.contains("șțîâă"));
    final String polishSpecialChars = check(Language.POLISH, "Mówiła długo, żeby tylko mówić mówić długo.");
    assertTrue("Expected special chars, got: '" + polishSpecialChars+ "'", polishSpecialChars.contains("mówić"));
    // test http POST
    assertTrue(checkByPOST(Language.ROMANIAN, "greșit greșit").indexOf("greșit") != -1);
    // test supported language listing
    final URL url = new URL("http://localhost:" + HTTPServer.DEFAULT_PORT + "/Languages");
    final String languagesXML = StringTools.streamToString((InputStream) url.getContent());
    if (!languagesXML.contains("Romanian") || !languagesXML.contains("English")) {
      fail("Error getting supported languages: " + languagesXML);
    }
    // tests for "&" character
    assertTrue(check(Language.ENGLISH, "Me & you you").contains("&"));
    // tests for mother tongue (copy from link {@link FalseFriendRuleTest})   
    assertTrue(check(Language.ENGLISH, Language.GERMAN, "We will berate you").indexOf("BERATE") != -1);
    assertTrue(check(Language.GERMAN, Language.ENGLISH, "Man sollte ihn nicht so beraten.").indexOf("BERATE") != -1);
    assertTrue(check(Language.POLISH, Language.ENGLISH, "To jest frywolne.").indexOf("FRIVOLOUS") != -1);
    //tests for bitext
    assertTrue(bitextCheck(Language.POLISH, Language.ENGLISH, "This is frivolous.", "To jest frywolne.").indexOf("FRIVOLOUS") != -1);
    assertTrue(bitextCheck(Language.POLISH, Language.ENGLISH, "This is something else.", "To jest frywolne.").indexOf("FRIVOLOUS") == -1);
  }

  public void testAccessDenied() throws Exception {
    final HTTPServer server = new HTTPServer(HTTPServer.DEFAULT_PORT, false, new HashSet<String>());
    try {
      server.run();
      try {
        System.out.println("Testing 'access denied' check now");
        check(Language.GERMAN, "no ip address allowed, so this cannot work");
        fail();
      } catch (IOException expected) {
      }
    } finally {
      server.stop();
    }
  }
  
  private String check(Language lang, String text) throws IOException {
    return check(lang, null, text);
  }
  
  private String bitextCheck(Language lang, Language motherTongue, String sourceText, String text) throws IOException {
    String urlOptions = "/?language=" + lang.getShortName();
    urlOptions += "&srctext=" + URLEncoder.encode(sourceText, "UTF-8"); 
    urlOptions += "&text=" + URLEncoder.encode(text, "UTF-8"); // latin1 is not enough for languages like polish, romanian, etc
    if (null != motherTongue) {
      urlOptions += "&motherTongue="+motherTongue.getShortName();
    }
    final URL url = new URL("http://localhost:" + HTTPServer.DEFAULT_PORT + urlOptions);
    final InputStream stream = (InputStream)url.getContent();
    final String result = StringTools.streamToString(stream);
    return result;
  }
  
  private String check(Language lang, Language motherTongue, String text) throws IOException {
    String urlOptions = "/?language=" + lang.getShortName();
    urlOptions += "&text=" + URLEncoder.encode(text, "UTF-8"); // latin1 is not enough for languages like polish, romanian, etc
    if (null != motherTongue) {
    	urlOptions += "&motherTongue=" + motherTongue.getShortName();
    }
    final URL url = new URL("http://localhost:" + HTTPServer.DEFAULT_PORT + urlOptions);
    final InputStream stream = (InputStream)url.getContent();
    final String result = StringTools.streamToString(stream);
    return result;
  }
  
  /**
   * Same as {@link #check(Language, String)} but using HTTP POST method instead of GET
   */
  private String checkByPOST(Language lang, String text) throws IOException {
    String postData = "language=" + lang.getShortName();
    postData += "&text=" + URLEncoder.encode(text, "UTF-8"); // latin1 is not enough for languages like polish, romanian, etc
    final URL url = new URL("http://localhost:" + HTTPServer.DEFAULT_PORT);
    final URLConnection connection = url.openConnection();
    connection.setDoOutput(true);
    final OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
    wr.write(postData);
    wr.flush();
    final String result = StringTools.streamToString(connection.getInputStream());
    return result;
  }
  
}
