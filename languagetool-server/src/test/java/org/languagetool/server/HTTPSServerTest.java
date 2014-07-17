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

import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.language.German;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.URL;
import java.net.URLEncoder;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.languagetool.server.HTTPServerConfig.DEFAULT_PORT;

public class HTTPSServerTest {

  private static final String KEYSTORE = "/org/languagetool/server/test-keystore.jks";
  private static final String KEYSTORE_PASSWORD = "mytest";

  @Test
  public void runRequestLimitationTest() throws Exception {
    HTTPTools.disableCertChecks();
    final HTTPSServerConfig serverConfig = new HTTPSServerConfig(HTTPServerConfig.DEFAULT_PORT, false, getKeystoreFile(), KEYSTORE_PASSWORD, 2, 120);
    final HTTPSServer server = new HTTPSServer(serverConfig, false, HTTPServerConfig.DEFAULT_HOST, null);
    try {
      server.run();
      check(new German(), "foo");
      check(new German(), "foo");
      try {
        System.out.println("=== Testing too many requests now, please ignore the following error ===");
        String result = check(new German(), "foo");
        fail("Expected exception not thrown, got this result instead: '" + result + "'");
      } catch (IOException expected) {}
    } finally {
      server.stop();
    }
  }
  
  @Test
  public void testHTTPSServer() throws Exception {
    HTTPTools.disableCertChecks();
    final HTTPSServerConfig config = new HTTPSServerConfig(getKeystoreFile(), KEYSTORE_PASSWORD);
    config.setMaxTextLength(500);
    final HTTPSServer server = new HTTPSServer(config, false, HTTPServerConfig.DEFAULT_HOST, null);
    try {
      server.run();
      runTests();
    } finally {
      server.stop();
    }
  }

  private File getKeystoreFile() {
    final URL keystore = HTTPSServerTest.class.getResource(KEYSTORE);
    if (keystore == null) {
      throw new RuntimeException("Not found in classpath : " + KEYSTORE);
    }
    return new File(keystore.getFile());
  }

  private void runTests() throws IOException {
    try {
      final String httpPrefix = "http://localhost:" + HTTPServerConfig.DEFAULT_PORT + "/";
      HTTPTools.checkAtUrl(new URL(httpPrefix + "?text=a+test&language=en"));
      fail("HTTP should not work, only HTTPS");
    } catch (SocketException expected) {}

    final String httpsPrefix = "https://localhost:" + HTTPServerConfig.DEFAULT_PORT + "/";

    final String result = HTTPTools.checkAtUrl(new URL(httpsPrefix + "?text=a+test.&language=en"));
    assertTrue("Got " + result, result.contains("UPPERCASE_SENTENCE_START"));

    final StringBuilder longText = new StringBuilder();
    while (longText.length() < 490) {
      longText.append("B ");
    }
    final String result2 = HTTPTools.checkAtUrl(new URL(httpsPrefix + "?text=" + encode(longText.toString()) + "&language=en"));
    assertTrue("Got " + result2, !result2.contains("UPPERCASE_SENTENCE_START"));
    assertTrue("Got " + result2, result2.contains("PHRASE_REPETITION"));

    final String overlyLongText = longText.toString() + " and some more to get over the limit of 500";
    try {
      System.out.println("=== Now checking text that is too long, please ignore the following exception ===");
      HTTPTools.checkAtUrl(new URL(httpsPrefix + "?text=" + encode(overlyLongText) + "&language=en"));
      fail();
    } catch (IOException expected) {}
  }

  private String check(Language lang, String text) throws IOException {
    String urlOptions = "/?language=" + lang.getShortName();
    urlOptions += "&disabled=HUNSPELL_RULE&text=" + URLEncoder.encode(text, "UTF-8"); // latin1 is not enough for languages like polish, romanian, etc
    final URL url = new URL("https://localhost:" + DEFAULT_PORT + urlOptions);
    return HTTPTools.checkAtUrl(url);
  }
  
  private String encode(String text) throws UnsupportedEncodingException {
    return URLEncoder.encode(text, "utf-8");
  }

}
