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
import org.junit.Before;
import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.language.*;
import org.languagetool.tools.StringTools;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashSet;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class HTTPServerTest {

  private static final int MAX_LENGTH = 50_000;  // needs to be in sync with server conf!
  
  private static final String LOAD_TEST_URL = "http://localhost:<PORT>/v2/check";
  //private static final String LOAD_TEST_URL = "https://api.languagetool.org/v2/check";
  //private static final String LOAD_TEST_URL = "https://languagetool.org/api/v2/check";
  
  @Before
  public void setup() {
    DatabaseLogger.getInstance().disableLogging();
  }

  @Test
  public void testHTTPServer() throws Exception {
    HTTPServer server = new HTTPServer(new HTTPServerConfig(HTTPTestTools.getDefaultPort(), true));
    assertFalse(server.isRunning());
    try {
      server.run();
      assertTrue(server.isRunning());
      runTranslatedMessageTest();
      runTestsV2();
      runDataTests();
    } finally {
      server.stop();
      assertFalse(server.isRunning());
    }
  }

  @Test
  public void translationSuggestions() throws Exception {
    File configFile = File.createTempFile("translationSuggestions", "txt");
    configFile.deleteOnExit();

    File beolingus = new File("../languagetool-standalone/src/test/resources/beolingus_test.txt");
    assertTrue(beolingus.exists());
    Files.write(configFile.toPath(), Collections.singletonList("beolingusFile=" + beolingus.getAbsolutePath().replace('\\', '/')));  // path works under Windows and Linux

    HTTPServer server = new HTTPServer(new HTTPServerConfig(new String[]{
      "--port", String.valueOf(HTTPTestTools.getDefaultPort()),
      "--config", configFile.getPath()
    }));
    server.run();
    try {
      String resultWithTranslation = checkV2(new AmericanEnglish(), new GermanyGerman(), "Please let us meet in my Haus");
      assertTrue(resultWithTranslation, resultWithTranslation.contains("house"));
    } finally {
      server.stop();
      assertFalse(server.isRunning());
    }
  }

  void runTranslatedMessageTest() throws IOException {
    String result1 = checkV2(Languages.getLanguageForShortCode("fr"), "C'est unx.");
    assertTrue(result1.contains("Faute de frappe possible trouvée"));
    assertFalse(result1.contains("Possible spelling mistake found"));

    String result2 = checkV2(Languages.getLanguageForShortCode("es"), "unx");
    assertTrue(result2.contains("Se ha encontrado un posible error ortográfico."));
    assertFalse(result2.contains("Possible spelling mistake found"));

    String result3 = checkV2(Languages.getLanguageForShortCode("nl"), "unx");
    assertTrue(result3.contains("Er is een mogelijke spelfout gevonden."));
    assertFalse(result3.contains("Possible spelling mistake found"));
  }

  void runTestsV2() throws IOException, SAXException, ParserConfigurationException {
    // no error:
    String emptyResultPattern = ".*\"matches\":\\[\\].*";
    German german = new GermanyGerman();
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
    URL url = new URL("http://localhost:" + HTTPTestTools.getDefaultPort() + "/v2/languages");
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
    //assertTrue(checkV2(english, german, "My handy is broken.").contains("EN_FOR_DE_SPEAKERS_FALSE_FRIENDS"));  // only works with ngrams
    assertFalse(checkV2(english, german, "We will berate you").contains("BERATE"));  // not active anymore now that we have EN_FOR_DE_SPEAKERS_FALSE_FRIENDS
    assertTrue(plainTextCheck("/v2/check", german, english, "Man sollte ihn nicht so beraten.", "&level=picky").contains("BERATE"));
    assertTrue(plainTextCheck("/v2/check", polish, english, "To jest frywolne.", "&level=picky").contains("FRIVOLOUS"));
      
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
            english, german, "We will will do so", disableAvsAn, nothing, false).contains("ENGLISH_WORD_REPEAT_RULE"));
    //..unless explicitly stated.
    assertTrue(!checkWithOptionsV2(
        english, german, "We will berate you", disableAvsAn, nothing, true).contains("BERATE"));
    
    
    //test if two rules get enabled as well
    String[] twoRules = {"EN_A_VS_AN", "ENGLISH_WORD_REPEAT_RULE"};
    
    String resultEn = checkWithOptionsV2(
            english, german, "This is an test. We will will do so.", twoRules, nothing, false);
    assertTrue("Result: " + resultEn, resultEn.contains("EN_A_VS_AN"));
    assertTrue("Result: " + resultEn, resultEn.contains("ENGLISH_WORD_REPEAT_RULE"));

    //check two disabled options
    String result3 = checkWithOptionsV2(
            english, german, "This is an test. We will will do so.", nothing, twoRules, false);
    assertFalse("Result: " + result3, result3.contains("EN_A_VS_AN"));
    assertFalse("Result: " + result3, result3.contains("ENGLISH_WORD_REPEAT_RULE"));
    
    //two disabled, one enabled, so enabled wins
    String result4 = checkWithOptionsV2(
            english, german, "This is an test. We will will do so.", disableAvsAn, twoRules, false);
    assertTrue("Result: " + result4, result4.contains("EN_A_VS_AN"));
    assertFalse("Result: " + result4, result4.contains("ENGLISH_WORD_REPEAT_RULE"));

    String result5 = checkV2(null, "This is a test of the language detection.");
    assertTrue("Result: " + result5, result5.contains("\"en-US\""));

    String result6 = checkV2(null, "This is a test of the language detection.", "&preferredVariants=de-DE,en-GB");
    assertTrue("Result: " + result6, result6.contains("\"en-GB\""));

    // fallback not working anymore, now giving confidence rating; tested in TextCheckerTest
    //String result7 = checkV2(null, "x");  // too short for auto-fallback, will use fallback
    //assertTrue("Result: " + result7, result7.contains("\"en-US\""));

    String res = check("text", "/v2/check", english, null, "A text.", "&sourceLanguage=de-DE&sourceText=Text");
    assertTrue(res.contains("DIFFERENT_PUNCTUATION"));   // bitext rule actually active
  }

  private void runDataTests() throws IOException {
    English english = new AmericanEnglish();
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

    // Text:
    // This is <xyz>an test</xyz>. Yet another error error.
    //              ^^                         ^^^^^^^^^^^
    String res1 = dataTextCheck(english, null, "{\"annotation\": [" +
            "{\"text\": \"This is \"}, {\"markup\": \"<xyz>\"}, {\"text\": \"an test\"}, {\"markup\": \"</xyz>\"}, {\"text\": \". Yet another error error.\"}]}", "");
    assertTrue(res1.contains("EN_A_VS_AN"));
    assertTrue(res1.contains("\"offset\":13"));
    assertTrue(res1.contains("\"length\":2"));
    assertTrue(res1.contains("ENGLISH_WORD_REPEAT_RULE"));
    assertTrue(res1.contains("\"offset\":40"));
    assertTrue(res1.contains("\"length\":11"));
    assertFalse(res1.contains("MORFOLOGIK_RULE_EN_US"));  // "xyz" would be an error, but it's ignored

    // Text:
    // This is a test.<p>Another text.
    // -> Markup must not just be ignored but also be replaced with whitespace.
    String res2 = dataTextCheck(english, null, "{\"annotation\": [" +
            "{\"text\": \"This is a test.\"}, {\"markup\": \"<p>\", \"interpretAs\": \"\\n\\n\"}," +
            "{\"text\": \"Another text.\"}]}\"", "");
    //System.out.println("RES3: " + res2);
    assertFalse(res2.contains("SENTENCE_WHITESPACE"));

    // Text:
    //   A test.<p attrib>Another text text.
    // This is what is checked internally:
    //   A test.\n\nAnother text text.
    String res3 = dataTextCheck(english, null, "{\"annotation\": [" +
            "{\"text\": \"A test.\"}, {\"markup\": \"<p attrib>\", \"interpretAs\": \"\\n\\n\"}," +
            "{\"text\": \"Here comes text text.\"}]}\"", "");
    //System.out.println("RES4: " + res3);
    assertFalse(res3.contains("SENTENCE_WHITESPACE"));
    assertTrue(res3.contains("ENGLISH_WORD_REPEAT_RULE"));
    assertTrue(res3.contains("\"offset\":28"));

    // Text:
    //   A test.<p>Another text text.</p><p>A hour ago.
    // This is what is checked internally:
    //   A test.\n\nAnother text text.\n\nA hour ago.
    String res4 = dataTextCheck(english, null, "{\"annotation\": [" +
            "{\"text\": \"A test.\"}, {\"markup\": \"<p>\", \"interpretAs\": \"\\n\\n\"}," +
            "{\"text\": \"Here comes text text.\"}," +
            "{\"markup\": \"</p><p>\", \"interpretAs\": \"\\n\\n\"}, {\"text\": \"A hour ago.\"}" +
            "]}\"", "");
    //System.out.println("RES5: " + res4);
    assertFalse(res4.contains("SENTENCE_WHITESPACE"));
    assertTrue(res4.contains("ENGLISH_WORD_REPEAT_RULE"));
    assertTrue(res4.contains("\"offset\":21"));
    assertTrue(res4.contains("EN_A_VS_AN"));
    assertTrue(res4.contains("\"offset\":38"));
    
    try {
      dataTextCheck(english, null, "{\"annotation\": [{\"text\": \"An\", \"markup\": \"foo\"}]}", "");
      fail();
    } catch (IOException ignore) {}
    try {
      dataTextCheck(english, null, "{\"annotation\": [{\"bla\": \"An\"}]}", "");
      fail();
    } catch (IOException ignore) {}
    try {
      dataTextCheck(english, null, "{\"text\": \"blah\", \"annotation\": \"foo\"}", "");
      fail();
    } catch (IOException ignore) {}
    try {
      dataTextCheck(english, null, "{\"annotation\": [{\"text\": \"An\", \"interpretAs\": \"foo\"}]}", "");
      fail();
    } catch (IOException ignore) {}
  }

  @Test
  public void testTimeout() {
    HTTPServerConfig config = new HTTPServerConfig(HTTPTestTools.getDefaultPort(), false);
    config.setMaxCheckTimeMillisAnonymous(1);
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
        if (!expected.toString().contains(" 500 ")) {
          fail("Expected exception with error 500, got: " + expected);
        }
      }
    } finally {
      server.stop();
    }
  }

  @Test
  public void testHealthcheck() throws Exception {
    HTTPServerConfig config = new HTTPServerConfig(HTTPTestTools.getDefaultPort(), false);
    HTTPServer server = new HTTPServer(config, false);
    try {
      server.run();
      URL url = new URL("http://localhost:<PORT>/v2/healthcheck".replace("<PORT>", String.valueOf(HTTPTestTools.getDefaultPort())));
      InputStream stream = (InputStream)url.getContent();
      String response = StringTools.streamToString(stream, "UTF-8");
      assertThat(response, is("OK"));
    } finally {
      server.stop();
    }
  }

  @Test
  public void testAccessDenied() throws Exception {
    HTTPServer server = new HTTPServer(new HTTPServerConfig(HTTPTestTools.getDefaultPort()), false, new HashSet<>());
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
    HTTPServer server = new HTTPServer(new HTTPServerConfig(HTTPTestTools.getDefaultPort()), false);
    try {
      server.run();
      try {
        System.out.println("=== Testing 'enabledOnly parameter' now, please ignore the following exception ===");
        URL url = new URL("http://localhost:" + HTTPTestTools.getDefaultPort() + "/?text=foo&language=en-US&disabled=EN_A_VS_AN&enabledOnly=yes");
        HTTPTestTools.checkAtUrl(url);
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
  public void testServerUrlSetting() throws Exception {
    HTTPServerConfig config = new HTTPServerConfig(HTTPTestTools.getDefaultPort());
    String prefix = "/languagetool-api/";
    config.setServerURL(prefix);
    HTTPServer server = new HTTPServer(config, false);
    try {
      server.run();
      HTTPTestTools.checkAtUrl(new URL("http://localhost:" + HTTPTestTools.getDefaultPort() + prefix + "v2/check?text=Test&language=en"));
    } finally {
      server.stop();
    }
  }

  @Test
  public void testMissingLanguageParameter() throws Exception {
    HTTPServer server = new HTTPServer(new HTTPServerConfig(HTTPTestTools.getDefaultPort()), false);
    try {
      server.run();
      try {
        System.out.println("=== Testing 'missing language parameter' now, please ignore the following exception ===");
        URL url = new URL("http://localhost:" + HTTPTestTools.getDefaultPort() + "/?text=foo");
        HTTPTestTools.checkAtUrl(url);
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
    String urlOptions = urlPrefix + "?language=" + (lang == null ? "auto" : lang.getShortCodeWithCountryAndVariant());
    urlOptions += "&disabledRules=HUNSPELL_RULE&" + typeName + "=" + URLEncoder.encode(text, "UTF-8"); // latin1 is not enough for languages like polish, romanian, etc
    if (motherTongue != null) {
      urlOptions += "&motherTongue=" + motherTongue.getShortCode();
    }
    urlOptions += parameters;
    URL url = new URL("http://localhost:" + HTTPTestTools.getDefaultPort() + urlOptions);
    return HTTPTestTools.checkAtUrl(url);
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
    URL url = new URL("http://localhost:" + HTTPTestTools.getDefaultPort() + urlOptions);
    return HTTPTestTools.checkAtUrl(url);
  }

  /**
   * Same as {@link #checkV1(Language, String)} but using HTTP POST method instead of GET
   */
  String checkByPOST(Language lang, String text) throws IOException {
    return checkByPOST(lang.getShortCodeWithCountryAndVariant(), text);
  }

  /**
   * Same as {@link #checkV1(Language, String)} but using HTTP POST method instead of GET; overloaded to allow language detection (langCode = 'auto')
   */
  String checkByPOST(String langCode, String text) throws IOException {
    String postData = "language=" + langCode + "&text=" + URLEncoder.encode(text, "UTF-8"); // latin1 is not enough for languages like Polish, Romanian, etc
    URL url = new URL(LOAD_TEST_URL.replace("<PORT>", String.valueOf(HTTPTestTools.getDefaultPort())));
    try {
      return HTTPTestTools.checkAtUrlByPost(url, postData);
    } catch (IOException e) {
      if (text.length() > MAX_LENGTH) {
        // this is expected, log it anyway:
        System.err.println("Got expected error on long text (" + text.length() + " chars): " + e.getMessage());
        return "";
      } else {
        System.err.println("Got error from " + url + " (" + langCode + ", " +
                           text.length() + " chars): " + e.getMessage() + ", text was (" + text.length() +  " chars): '" + StringUtils.abbreviate(text, 100) + "'");
        return "";
      }
    }
  }
}
