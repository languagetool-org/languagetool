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

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.Language;
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

  private static final int MAX_LENGTH = 50_000;  // needs to be in sync with server conf!
  
  private static final String LOAD_TEST_URL = "http://localhost:<PORT>/v2/check";
  //private static final String LOAD_TEST_URL = "https://api.languagetool.org/v2/check";
  //private static final String LOAD_TEST_URL = "https://languagetool.org/api/v2/check";

  @Ignore("already gets tested by sub class HTTPServerLoadTest")
  @Test
  public void testHTTPServer() throws Exception {
    HTTPServer server = new HTTPServer();
    assertFalse(server.isRunning());
    try {
      server.run();
      assertTrue(server.isRunning());
      runTestsV2();
      runDataTests();
    } finally {
      server.stop();
      assertFalse(server.isRunning());
    }
  }

  void runTestsV2() throws IOException, SAXException, ParserConfigurationException {
    // no error:
    String emptyResultPattern = ".*\"matches\":\\[\\].*";
    German german = new German();
    String result1 = checkV2(german, "");
    assertTrue("Got " + result1 + ", expected " + emptyResultPattern, result1.matches(emptyResultPattern));
    String result2 = checkV2(german, "Ein kleiner Test");
    assertTrue("Got " + result2 + ", expected " + emptyResultPattern, result2.matches(emptyResultPattern));
    // one error:
    assertTrue(checkV2(german, "ein kleiner test.").contains("UPPERCASE_SENTENCE_START"));
    // two errors:
    String result = checkV2(german, "ein kleiner test. Und wieder Erwarten noch was: \u00f6\u00e4\u00fc\u00df.");
    assertTrue("Got result without 'UPPERCASE_SENTENCE_START': " + result, result.contains("UPPERCASE_SENTENCE_START"));
    assertTrue("Got result without 'WIEDER_WILLEN': " + result, result.contains("WIEDER_WILLEN"));
    assertTrue("Expected special chars, got: '" + result + "'",
            result.contains("\u00f6\u00e4\u00fc\u00df"));   // special chars are intact
    assertTrue(checkV2(german, "bla <script>").contains("<script>"));  // no escaping of '<' and '>' needed, unlike in XML

    // other tests for special characters
    String germanSpecialChars = checkV2(german, "ein kleiner test. Und wieder Erwarten noch was: öäüß+ öäüß.");
    assertTrue("Expected special chars, got: '" + germanSpecialChars + "'", germanSpecialChars.contains("öäüß+"));
    String romanianSpecialChars = checkV2(new Romanian(), "bla bla șțîâă șțîâă și câteva caractere speciale");
    assertTrue("Expected special chars, got: '" + romanianSpecialChars + "'", romanianSpecialChars.contains("șțîâă"));
    Polish polish = new Polish();
    String polishSpecialChars = checkV2(polish, "Mówiła długo, żeby tylko mówić mówić długo.");
    assertTrue("Expected special chars, got: '" + polishSpecialChars+ "'", polishSpecialChars.contains("mówić"));
    // test http POST
    assertTrue(checkByPOST(new Romanian(), "greșit greșit").contains("greșit"));
    // test supported language listing
    URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/v2/languages");
    String languagesJson = StringTools.streamToString((InputStream) url.getContent(), "UTF-8");
    if (!languagesJson.contains("Romanian") || !languagesJson.contains("English")) {
      fail("Error getting supported languages: " + languagesJson);
    }
    if (!languagesJson.contains("\"de\"") || !languagesJson.contains("\"de-DE\"")) {
      fail("Error getting supported languages: " + languagesJson);
    }
    // tests for "&" character
    English english = new English();
    assertTrue(checkV2(english, "Me & you you").contains("&"));
    // tests for mother tongue (copy from link {@link FalseFriendRuleTest})   
    assertTrue(checkV2(english, german, "We will berate you").contains("BERATE"));
    assertTrue(checkV2(german, english, "Man sollte ihn nicht so beraten.").contains("BERATE"));
    assertTrue(checkV2(polish, english, "To jest frywolne.").contains("FRIVOLOUS"));
      
    //test for no changed if no options set
    String[] nothing = {};
    assertEquals(checkV2(english, german, "We will berate you"), 
        checkWithOptionsV2(english, german, "We will berate you", nothing, nothing, false));
    
    //disabling
    String[] disableAvsAn = {"EN_A_VS_AN"};
    assertTrue(!checkWithOptionsV2(
            english, german, "This is an test", nothing, disableAvsAn, false).contains("an test"));

    //enabling
    assertTrue(checkWithOptionsV2(
            english, german, "This is an test", disableAvsAn, nothing, false).contains("an test"));
    //should also mean _NOT_ disabling all other rules...
    assertTrue(checkWithOptionsV2(
            english, german, "We will berate you", disableAvsAn, nothing, false).contains("BERATE"));
    //..unless explicitly stated.
    assertTrue(!checkWithOptionsV2(
        english, german, "We will berate you", disableAvsAn, nothing, true).contains("BERATE"));
    
    
    //test if two rules get enabled as well
    String[] twoRules = {"EN_A_VS_AN", "BERATE"};
    
    String resultEn = checkWithOptionsV2(
            english, german, "This is an test. We will berate you.", twoRules, nothing, false);
    assertTrue("Result: " + resultEn, resultEn.contains("EN_A_VS_AN"));
    assertTrue("Result: " + resultEn, resultEn.contains("BERATE"));

    //check two disabled options
    String result3 = checkWithOptionsV2(
            english, german, "This is an test. We will berate you.", nothing, twoRules, false);
    assertFalse("Result: " + result3, result3.contains("EN_A_VS_AN"));
    assertFalse("Result: " + result3, result3.contains("BERATE"));
    
    //two disabled, one enabled, so enabled wins
    String result4 = checkWithOptionsV2(
            english, german, "This is an test. We will berate you.", disableAvsAn, twoRules, false);
    assertTrue("Result: " + result4, result4.contains("EN_A_VS_AN"));
    assertFalse("Result: " + result4, result4.contains("BERATE"));

    String result5 = checkV2(null, "This is a test of the language detection.");
    assertTrue("Result: " + result5, result5.contains("\"en-US\""));

    String result6 = checkV2(null, "This is a test of the language detection.", "&preferredVariants=de-DE,en-GB");
    assertTrue("Result: " + result6, result6.contains("\"en-GB\""));

    String result7 = checkV2(null, "x");  // too short for auto-fallback, will use fallback
    assertTrue("Result: " + result7, result7.contains("\"en-US\""));
  }

  private void runDataTests() throws IOException {
    English english = new English();
    assertTrue(dataTextCheck(english, null,
            "{\"text\": \"This is an test.\"}", "").contains("EN_A_VS_AN"));
    assertTrue(dataTextCheck(english, null,
            "{\"text\": \"This is an test.\", \"metaData\": {}}", "").contains("EN_A_VS_AN"));
    assertTrue(dataTextCheck(english, null,
            "{\"text\": \"This is an test.\", \"metaData\": {\"key\": \"val\"}}", "").contains("EN_A_VS_AN"));
    assertTrue(dataTextCheck(english, null,
            "{\"text\": \"This is an test.\", \"metaData\": {\"key\": \"val\", \"EmailToAddress\": \"My name <foo@bar.org>\"}}", "").contains("EN_A_VS_AN"));
    assertFalse(dataTextCheck(english, null,
            "{\"text\": \"This is a test.\"}", "").contains("EN_A_VS_AN"));
  }

  @Test
  public void testTimeout() throws Exception {
    HTTPServerConfig config = new HTTPServerConfig(HTTPTools.getDefaultPort(), false);
    config.setMaxCheckTimeMillis(1);
    HTTPServer server = new HTTPServer(config, false);
    try {
      server.run();
      try {
        System.out.println("=== Testing timeout now, please ignore the following exception ===");
        long t = System.currentTimeMillis();
        checkV2(new GermanyGerman(), "Einq Tesz miit fieln Fehlan, desshalb sehee laagnsam bee dr Rechtschriebpürfung. "+
                                     "hir stet noc mer text mt nochh meh feheln. vielleict brucht es soagr nohc mehrr, damt es klapt");
        fail("Check was expected to be stopped because it took too long (> 1ms), it took " +
                (System.currentTimeMillis()-t + "ms when measured from client side"));
      } catch (IOException expected) {
        if (!expected.toString().contains(" 503 ")) {
          fail("Expected exception with error 503, got: " + expected);
        }
      }
    } finally {
      server.stop();
    }
  }

  @Test
  public void testAccessDenied() throws Exception {
    HTTPServer server = new HTTPServer(new HTTPServerConfig(HTTPTools.getDefaultPort()), false, new HashSet<>());
    try {
      server.run();
      try {
        System.out.println("=== Testing 'access denied' check now, please ignore the following exception ===");
        checkV1(new German(), "no ip address allowed, so this cannot work");
        fail();
      } catch (IOException expected) {
        if (!expected.toString().contains(" 403 ")) {
          fail("Expected exception with error 403, got: " + expected);
        }
      }
      try {
        System.out.println("=== Testing 'access denied' check now, please ignore the following exception ===");
        checkV2(new German(), "no ip address allowed, so this cannot work");
        fail();
      } catch (IOException expected) {
        if (!expected.toString().contains(" 403 ")) {
          fail("Expected exception with error 403, got: " + expected);
        }
      }
    } finally {
      server.stop();
    }
  }
  
  @Test
  public void testEnabledOnlyParameter() throws Exception {
    HTTPServer server = new HTTPServer(new HTTPServerConfig(HTTPTools.getDefaultPort()), false);
    try {
      server.run();
      try {
        System.out.println("=== Testing 'enabledOnly parameter' now, please ignore the following exception ===");
        URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/?text=foo&language=en-US&disabled=EN_A_VS_AN&enabledOnly=yes");
        HTTPTools.checkAtUrl(url);
        fail();
      } catch (IOException expected) {
        if (!expected.toString().contains(" 400 ")) {
          fail("Expected exception with error 400, got: " + expected);
        }
      }
    } finally {
      server.stop();
    }
  }

  @Test
  public void testMissingLanguageParameter() throws Exception {
    HTTPServer server = new HTTPServer(new HTTPServerConfig(HTTPTools.getDefaultPort()), false);
    try {
      server.run();
      try {
        System.out.println("=== Testing 'missing language parameter' now, please ignore the following exception ===");
        URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + "/?text=foo");
        HTTPTools.checkAtUrl(url);
        fail();
      } catch (IOException expected) {
        if (!expected.toString().contains(" 400 ")) {
          fail("Expected exception with error 400, got: " + expected);
        }
      }
    } finally {
      server.stop();
    }
  }

  private String checkV1(Language lang, String text) throws IOException {
    return checkV1(lang, null, text);
  }

  private String checkV2(Language lang, String text) throws IOException {
    return checkV2(lang, (Language)null, text);
  }

  protected String checkV1(Language lang, Language motherTongue, String text) throws IOException {
    return plainTextCheck("/", lang, motherTongue, text, "");
  }

  protected String checkV2(Language lang, Language motherTongue, String text) throws IOException {
    return plainTextCheck("/v2/check", lang, motherTongue, text, "");
  }

  private String checkV2(Language lang, String text, String parameters) throws IOException {
    return plainTextCheck("/v2/check", lang, null, text, parameters);
  }

  private String plainTextCheck(String urlPrefix, Language lang, Language motherTongue, String text, String parameters) throws IOException {
    return check("text", urlPrefix, lang, motherTongue, text, parameters);
  }

  private String dataTextCheck(Language lang, Language motherTongue, String jsonData, String parameters) throws IOException {
    return check("data", "/v2/check", lang, motherTongue, jsonData, parameters);
  }

  private String check(String typeName, String urlPrefix, Language lang, Language motherTongue, String text, String parameters) throws IOException {
    String urlOptions = urlPrefix + "?language=" + (lang == null ? "auto" : lang.getShortCode());
    urlOptions += "&disabledRules=HUNSPELL_RULE&" + typeName + "=" + URLEncoder.encode(text, "UTF-8"); // latin1 is not enough for languages like polish, romanian, etc
    if (motherTongue != null) {
      urlOptions += "&motherTongue=" + motherTongue.getShortCode();
    }
    urlOptions += parameters;
    URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + urlOptions);
    return HTTPTools.checkAtUrl(url);
  }

  private String checkWithOptionsV2(Language lang, Language motherTongue, String text,
                                  String[] enabledRules, String[] disabledRules, boolean useEnabledOnly) throws IOException {
    String urlOptions = "/v2/check?language=" + lang.getShortCode();
    urlOptions += "&text=" + URLEncoder.encode(text, "UTF-8"); // latin1 is not enough for languages like polish, romanian, etc
    if (motherTongue != null) {
      urlOptions += "&motherTongue=" + motherTongue.getShortCode();
    }
    if (disabledRules.length > 0) {
      urlOptions += "&disabledRules=" + StringUtils.join(disabledRules, ",");
    }
    if (enabledRules.length > 0) {
      urlOptions += "&enabledRules=" + StringUtils.join(enabledRules, ",");
    }
    if (useEnabledOnly) {
      urlOptions += "&enabledOnly=yes";
    }
    URL url = new URL("http://localhost:" + HTTPTools.getDefaultPort() + urlOptions);
    return HTTPTools.checkAtUrl(url);
  }
  
  /**
   * Same as {@link #checkV1(Language, String)} but using HTTP POST method instead of GET
   */
  String checkByPOST(Language lang, String text) throws IOException {
    String postData = "language=" + lang.getShortCodeWithCountryAndVariant() + "&text=" + URLEncoder.encode(text, "UTF-8"); // latin1 is not enough for languages like Polish, Romanian, etc
    URL url = new URL(LOAD_TEST_URL.replace("<PORT>", String.valueOf(HTTPTools.getDefaultPort())));
    try {
      return HTTPTools.checkAtUrlByPost(url, postData);
    } catch (IOException e) {
      if (text.length() > MAX_LENGTH) {
        // this is expected, log it anyway:
        System.err.println("Got expected error on long text (" + text.length() + " chars): " + e.getMessage());
        return "";
      } else {
        System.err.println("Got error from " + url + " (" + lang.getShortCodeWithCountryAndVariant() + ", " +
                           text.length() + " chars): " + e.getMessage() + ", text was (" + text.length() +  " chars): '" + StringUtils.abbreviate(text, 100) + "'");
        return "";
      }
    }
  }

}
