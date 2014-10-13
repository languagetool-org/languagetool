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
package org.languagetool.server;

import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.XMLValidator;
import org.languagetool.language.*;
import org.languagetool.tools.StringTools;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;

import static org.junit.Assert.*;

public class HTTPServerTest {

  @Ignore("already gets tested by sub class HTTPServerLoadTest")
  @Test
  public void testHTTPServer() throws Exception {
    final HTTPServer server = new HTTPServer();
    assertFalse(server.isRunning());
    try {
      server.run();
      assertTrue(server.isRunning());
      runTests();
    } finally {
      server.stop();
      assertFalse(server.isRunning());
    }
  }

  void runTests() throws IOException, SAXException, ParserConfigurationException {
    // no error:
    final String matchAttr = "software=\"LanguageTool\" version=\"[1-9].*?\" buildDate=\".*?\"";
    final String emptyResultPattern = "<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>\n<matches " + matchAttr + ">\n<language shortname=\"de\" name=\"German\"/>\n</matches>\n";
    final German german = new German();
    final String result1 = check(german, "");
    assertTrue("Got " + result1 + ", expected " + emptyResultPattern, result1.matches(emptyResultPattern));
    final String result2 = check(german, "Ein kleiner test");
    assertTrue("Got " + result2 + ", expected " + emptyResultPattern, result2.matches(emptyResultPattern));
    // one error:
    assertTrue(check(german, "ein kleiner test.").contains("UPPERCASE_SENTENCE_START"));
    // two errors:
    final String result = check(german, "ein kleiner test. Und wieder Erwarten noch was: \u00f6\u00e4\u00fc\u00df.");
    assertTrue(result.contains("UPPERCASE_SENTENCE_START"));
    assertTrue(result.contains("WIEDER_WILLEN"));
    assertTrue("Expected special chars, got: '" + result + "'",
            result.contains("\u00f6\u00e4\u00fc\u00df"));   // special chars are intact
    final XMLValidator validator = new XMLValidator();
    validator.validateXMLString(result, JLanguageTool.getDataBroker().getResourceDir() + "/api-output.dtd", "matches");
    validator.checkSimpleXMLString(result);
    //System.err.println(result);
    // make sure XML chars are escaped in the result to avoid invalid XML
    // and XSS attacks:
    assertTrue(!check(german, "bla <script>").contains("<script>"));

    // other tests for special characters
    final String germanSpecialChars = check(german, "ein kleiner test. Und wieder Erwarten noch was: öäüß+ öäüß.");
    assertTrue("Expected special chars, got: '" + germanSpecialChars + "'", germanSpecialChars.contains("öäüß+"));
    final String romanianSpecialChars = check(new Romanian(), "bla bla șțîâă șțîâă și câteva caractere speciale");
    assertTrue("Expected special chars, got: '" + romanianSpecialChars + "'", romanianSpecialChars.contains("șțîâă"));
    final Polish polish = new Polish();
    final String polishSpecialChars = check(polish, "Mówiła długo, żeby tylko mówić mówić długo.");
    assertTrue("Expected special chars, got: '" + polishSpecialChars+ "'", polishSpecialChars.contains("mówić"));
    // test http POST
    assertTrue(checkByPOST(new Romanian(), "greșit greșit").contains("greșit"));
    // test supported language listing
    final URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/Languages");
    final String languagesXML = StringTools.streamToString((InputStream) url.getContent(), "UTF-8");
    if (!languagesXML.contains("Romanian") || !languagesXML.contains("English")) {
      fail("Error getting supported languages: " + languagesXML);
    }
    if (!languagesXML.contains("abbr=\"de\"") || !languagesXML.contains("abbrWithVariant=\"de-DE\"")) {
      fail("Error getting supported languages: " + languagesXML);
    }
    // tests for "&" character
    final English english = new English();
    assertTrue(check(english, "Me & you you").contains("&"));
    // tests for mother tongue (copy from link {@link FalseFriendRuleTest})   
    assertTrue(check(english, german, "We will berate you").contains("BERATE"));
    assertTrue(check(german, english, "Man sollte ihn nicht so beraten.").contains("BERATE"));
    assertTrue(check(polish, english, "To jest frywolne.").contains("FRIVOLOUS"));
      
    //tests for bitext
    assertTrue(bitextCheck(polish, english, "This is frivolous.", "To jest frywolne.").contains("FRIVOLOUS"));
    assertTrue(!bitextCheck(polish, english, "This is something else.", "To jest frywolne.").contains("FRIVOLOUS"));
    
    //test for no changed if no options set
    final String[] nothing = {};
    assertEquals(check(english, german, "We will berate you"), 
        checkWithOptions(english, german, "We will berate you", nothing, nothing, false));
    
    //disabling
    final String[] disableAvsAn = {"EN_A_VS_AN"};
    assertTrue(!checkWithOptions(
            english, german, "This is an test", nothing, disableAvsAn, false).contains("an test"));

    //enabling
    assertTrue(checkWithOptions(
            english, german, "This is an test", disableAvsAn, nothing, false).contains("an test"));
    //should also mean _NOT_ disabling all other rules...
    assertTrue(checkWithOptions(
            english, german, "We will berate you", disableAvsAn, nothing, false).contains("BERATE"));
    //..unless explicitly stated.
    assertTrue(!checkWithOptions(
        english, german, "We will berate you", disableAvsAn, nothing, true).contains("BERATE"));
    
    
    //test if two rules get enabled as well
    final String[] twoRules = {"EN_A_VS_AN", "BERATE"};
    
    String resultEn = checkWithOptions(
            english, german, "This is an test. We will berate you.", twoRules, nothing, false);
    assertTrue(resultEn.contains("EN_A_VS_AN"));
    assertTrue(resultEn.contains("BERATE"));

    //check two disabled options
    String result3 = checkWithOptions(
            english, german, "This is an test. We will berate you.", nothing, twoRules, false);
    assertFalse(result3.contains("EN_A_VS_AN"));
    assertFalse(result3.contains("BERATE"));
    
    //two disabled, one enabled, so enabled wins
    String result4 = checkWithOptions(
            english, german, "This is an test. We will berate you.", disableAvsAn, twoRules, false);
    assertTrue(result4.contains("EN_A_VS_AN"));
    assertFalse(result4.contains("BERATE"));
  }

  @Test
  public void testTimeout() throws Exception {
    HTTPServerConfig config = new HTTPServerConfig(HTTPTools.getDefaultPort(), false);
    config.setMaxCheckTimeMillis(1);
    final HTTPServer server = new HTTPServer(config, false);
    try {
      server.run();
      try {
        System.out.println("=== Testing timeout now, please ignore the following exception ===");
        check(new GermanyGerman(), "Einq Tesz miit fieln Fehlan, desshalb sehee laagnsam bee dr Rechtschriebpürfung");
        fail("Check was expected to be stopped because it took too long");
      } catch (IOException expected) {}
    } finally {
      server.stop();
    }
  }

  @Test
  public void testAccessDenied() throws Exception {
    final HTTPServer server = new HTTPServer(new HTTPServerConfig(HTTPTools.getDefaultPort()), false, new HashSet<String>());
    try {
      server.run();
      try {
        System.out.println("=== Testing 'access denied' check now, please ignore the following exception ===");
        check(new German(), "no ip address allowed, so this cannot work");
        fail();
      } catch (IOException expected) {}
    } finally {
      server.stop();
    }
  }
  
  @Test
  public void testEnabledOnlyParameter() throws Exception {
    final HTTPServer server = new HTTPServer(new HTTPServerConfig(HTTPTools.getDefaultPort()), false);
    try {
      server.run();
      try {
        System.out.println("=== Testing 'enabledOnly parameter' now, please ignore the following exception ===");
        final URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/?text=foo&language=en-US&disabled=EN_A_VS_AN&enabledOnly=yes");
        HTTPTools.checkAtUrl(url);
        fail();
      } catch (IOException expected) {}
    } finally {
      server.stop();
    }
  }

  @Test
  public void testMissingLanguageParameter() throws Exception {
    final HTTPServer server = new HTTPServer(new HTTPServerConfig(HTTPTools.getDefaultPort()), false);
    try {
      server.run();
      try {
        System.out.println("=== Testing 'missing language parameter' now, please ignore the following exception ===");
        final URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/?text=foo");
        HTTPTools.checkAtUrl(url);
        fail();
      } catch (IOException expected) {}
    } finally {
      server.stop();
    }
  }

  private String bitextCheck(Language lang, Language motherTongue, String sourceText, String text) throws IOException {
    String urlOptions = "/?language=" + lang.getShortName();
    urlOptions += "&srctext=" + URLEncoder.encode(sourceText, "UTF-8");
    urlOptions += "&text=" + URLEncoder.encode(text, "UTF-8"); // latin1 is not enough for languages like polish, romanian, etc
    if (motherTongue != null) {
      urlOptions += "&motherTongue=" + motherTongue.getShortName();
    }
    final URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + urlOptions);
    return HTTPTools.checkAtUrl(url);
  }

  private String check(Language lang, String text) throws IOException {
    return check(lang, null, text);
  }

  protected String check(Language lang, Language motherTongue, String text) throws IOException {
    String urlOptions = "/?language=" + lang.getShortName();
    urlOptions += "&disabled=HUNSPELL_RULE&text=" + URLEncoder.encode(text, "UTF-8"); // latin1 is not enough for languages like polish, romanian, etc
    if (motherTongue != null) {
      urlOptions += "&motherTongue=" + motherTongue.getShortName();
    }
    final URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + urlOptions);
    return HTTPTools.checkAtUrl(url);
  }

  private String checkWithOptions(Language lang, Language motherTongue, String text,
                                  String[] enabledRules, String[] disabledRules, boolean useEnabledOnly) throws IOException {
    String urlOptions = "/?language=" + lang.getShortName();
    urlOptions += "&text=" + URLEncoder.encode(text, "UTF-8"); // latin1 is not enough for languages like polish, romanian, etc
    if (motherTongue != null) {
      urlOptions += "&motherTongue=" + motherTongue.getShortName();
    }
    if (disabledRules.length > 0) {
      urlOptions += "&disabled=" + StringUtils.join(disabledRules, ",");
    }
    if (enabledRules.length > 0) {
      urlOptions += "&enabled=" + StringUtils.join(enabledRules, ",");
    }
    if (useEnabledOnly) {
      urlOptions += "&enabledOnly=yes";
    }
    final URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + urlOptions);
    return HTTPTools.checkAtUrl(url);
  }
  
  /**
   * Same as {@link #check(Language, String)} but using HTTP POST method instead of GET
   */
  protected String checkByPOST(Language lang, String text) throws IOException {
    final String postData = "language=" + lang.getShortName() + "&text=" + URLEncoder.encode(text, "UTF-8"); // latin1 is not enough for languages like Polish, Romanian, etc
    final URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort());
    return HTTPTools.checkAtUrlByPost(url, postData);
  }

}
