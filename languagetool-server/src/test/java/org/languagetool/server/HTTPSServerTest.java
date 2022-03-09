/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import org.junit.Before;
import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.language.German;
import org.languagetool.language.GermanyGerman;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HTTPSServerTest {

  private static final String KEYSTORE = "/org/languagetool/server/test-keystore.jks";
  private static final String KEYSTORE_PASSWORD = "mytest";

  @Before
  public void setup() {
    DatabaseLogger.getInstance().disableLogging();
  }

  @Test
  public void runRequestLimitationTest() throws Exception {
    HTTPTestTools.disableCertChecks();
    HTTPSServerConfig serverConfig = new HTTPSServerConfig(HTTPTestTools.getDefaultPort(), false, getKeystoreFile(), KEYSTORE_PASSWORD, 2, 120);
    serverConfig.setBlockedReferrers(Arrays.asList("http://foo.org", "bar.org"));
    HTTPSServer server = new HTTPSServer(serverConfig, false, HTTPServerConfig.DEFAULT_HOST, null);
    try {
      server.run();
      check(new GermanyGerman(), "foo");
      check(new GermanyGerman(), "foo");
      try {
        System.out.println("=== Testing too many requests now, please ignore the following error ===");
        String result = check(new German(), "foo");
        fail("Expected exception not thrown, got this result instead: '" + result + "'");
      } catch (IOException ignored) {}
    } finally {
      server.stop();
    }
  }
  
  @Test
  public void runReferrerLimitationTest() throws Exception {
    HTTPTestTools.disableCertChecks();
    HTTPSServerConfig serverConfig = new HTTPSServerConfig(HTTPTestTools.getDefaultPort(), false, getKeystoreFile(), KEYSTORE_PASSWORD);
    serverConfig.setBlockedReferrers(Arrays.asList("http://foo.org", "bar.org"));
    HTTPSServer server = new HTTPSServer(serverConfig, false, HTTPServerConfig.DEFAULT_HOST, null);
    try {
      server.run();

      HashMap<String, String> map = new HashMap<>();
      URL url = new URL("https://localhost:" + HTTPTestTools.getDefaultPort() + "/v2/check");
      try {
        map.put("Referer", "http://foo.org/myref");
        HTTPTestTools.checkAtUrlByPost(url, "language=en&text=a test", map);
        fail("Request should fail because of blocked referrer");
      } catch (IOException ignored) {}

      try {
        map.put("Referer", "http://bar.org/myref");
        HTTPTestTools.checkAtUrlByPost(url, "language=en&text=a test", map);
        fail("Request should fail because of blocked referrer");
      } catch (IOException ignored) {}
      try {
        map.put("Referer", "https://bar.org/myref");
        HTTPTestTools.checkAtUrlByPost(url, "language=en&text=a test", map);
        fail("Request should fail because of blocked referrer");
      } catch (IOException ignored) {}
      try {
        map.put("Referer", "https://www.bar.org/myref");
        HTTPTestTools.checkAtUrlByPost(url, "language=en&text=a test", map);
        fail("Request should fail because of blocked referrer");
      } catch (IOException ignored) {}
      map.put("Referer", "https://www.something-else.org/myref");
      HTTPTestTools.checkAtUrlByPost(url, "language=en&text=a test", map);

    } finally {
      server.stop();
    }
  }
  
  @Test
  public void testHTTPSServer() throws Exception {
    HTTPTestTools.disableCertChecks();
    HTTPSServerConfig config = new HTTPSServerConfig(HTTPTestTools.getDefaultPort(), false, getKeystoreFile(), KEYSTORE_PASSWORD);
    config.setMaxTextLengthAnonymous(500);
    HTTPSServer server = new HTTPSServer(config, false, HTTPServerConfig.DEFAULT_HOST, null);
    try {
      server.run();
      runTests();
    } finally {
      server.stop();
    }
  }

  private File getKeystoreFile() {
    URL keystore = HTTPSServerTest.class.getResource(KEYSTORE);
    if (keystore == null) {
      throw new RuntimeException("Not found in classpath : " + KEYSTORE);
    }
    return new File(keystore.getFile());
  }

  private void runTests() throws IOException {
    try {
      String httpPrefix = "http://localhost:" + HTTPTestTools.getDefaultPort() + "/";
      HTTPTestTools.checkAtUrl(new URL(httpPrefix + "?text=a+test&language=en"));
      fail("HTTP should not work, only HTTPS");
    } catch (SocketException ignored) {}

    String httpsPrefix = "https://localhost:" + HTTPTestTools.getDefaultPort() + "/v2/check";

    String result = HTTPTestTools.checkAtUrl(new URL(httpsPrefix + "?text=a+test.&language=en"));
    assertTrue("Got " + result, result.contains("UPPERCASE_SENTENCE_START"));

    StringBuilder longText = new StringBuilder();
    while (longText.length() < 490) {
      longText.append("Run ");
    }
    String result2 = HTTPTestTools.checkAtUrl(new URL(httpsPrefix + "?text=" + encode(longText.toString()) + "&language=en"));
    assertTrue("Got " + result2, !result2.contains("UPPERCASE_SENTENCE_START"));
    assertTrue("Got " + result2, result2.contains("PHRASE_REPETITION"));

    String overlyLongText = longText + " and some more to get over the limit of 500";
    try {
      System.out.println("=== Now checking text that is too long, please ignore the following exception ===");
      HTTPTestTools.checkAtUrl(new URL(httpsPrefix + "?text=" + encode(overlyLongText) + "&language=en"));
      fail();
    } catch (IOException expected) {
      if (!expected.toString().contains(" 413 ")) {
        fail("Expected exception with error 413, got: " + expected);
      }
    }
    
    String json = check("de", "This is an English text, but we specify German anyway");
    assertTrue("Got: " + json, json.contains("\"German\""));
    assertTrue("Got: " + json, json.contains("\"de\""));
    assertTrue("Got: " + json, json.contains("\"English (US)\""));
    assertTrue("Got: " + json, json.contains("\"en-US\""));
    
    String json2 = check("de-CH", "Die äußeren Wirklichkeit.");
    assertTrue("Got: " + json2, json2.contains("replacements\":[{\"value\":\"Die äussere Wirklichkeit\""));
    
    String json3 = check("de-DE", "Die äußeren Wirklichkeit.");
    assertTrue("Got: " + json3, json3.contains("replacements\":[{\"value\":\"Die äußere Wirklichkeit\""));
  }

  private String check(String langCode, String text) throws IOException {
    String urlOptions = "/v2/check?language=" + langCode;
    urlOptions += "&disabledRules=HUNSPELL_RULE&text=" + URLEncoder.encode(text, "UTF-8"); // latin1 is not enough for languages like polish, romanian, etc
    URL url = new URL("https://localhost:" + HTTPTestTools.getDefaultPort() + urlOptions);
    return HTTPTestTools.checkAtUrl(url);
  }

  private String check(Language lang, String text) throws IOException {
    return check(lang.getShortCode(), text);
  }
  
  private String encode(String text) throws UnsupportedEncodingException {
    return URLEncoder.encode(text, "utf-8");
  }

}
