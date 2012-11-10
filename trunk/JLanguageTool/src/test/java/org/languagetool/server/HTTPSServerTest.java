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
import org.languagetool.tools.StringTools;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class HTTPSServerTest {

  private static final String KEYSTORE = "/org/languagetool/test-keystore.jks";
  private static final String KEYSTORE_PASSWORD = "mytest";

  @Test
  public void testHTTPSServer() throws Exception {
    disableCertChecks();
    final URL keystore = HTTPSServerTest.class.getResource(KEYSTORE);
    if (keystore == null) {
      throw new RuntimeException("Not found in classpath : " + KEYSTORE);
    }
    final File keyStoreFile = new File(keystore.getFile());
    final HTTPSServerConfig config = new HTTPSServerConfig(keyStoreFile, KEYSTORE_PASSWORD);
    final HTTPSServer server = new HTTPSServer(config, false, HTTPServerConfig.DEFAULT_HOST, null);
    try {
      server.run();
      runTests();
    } finally {
      server.stop();
    }
  }

  /**
   * For testing, we disable all checks because we use a self-signed certificate on the server
   * side and we want this test to run everywhere without importing the certificate into the JVM's trust store.
   *
   * See http://stackoverflow.com/questions/2893819/telling-java-to-accept-self-signed-ssl-certificate
   */
  private void disableCertChecks() throws NoSuchAlgorithmException, KeyManagementException {
    final TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
              public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
              }
              public void checkClientTrusted(
                      java.security.cert.X509Certificate[] certs, String authType) {
              }
              public void checkServerTrusted(
                      java.security.cert.X509Certificate[] certs, String authType) {
              }
            }
    };
    final SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
  }

  private void runTests() throws IOException {
    final String httpsPrefix = "https://localhost:" + HTTPServerConfig.DEFAULT_PORT + "/";
    try {
      final String httpPrefix = "http://localhost:" + HTTPServerConfig.DEFAULT_PORT + "/";
      checkAtUrl(new URL(httpPrefix + "?text=a+test&language=en"));
      fail("HTTP should not work, only HTTPS");
    } catch (SocketException expected) {}

    final String result = checkAtUrl(new URL(httpsPrefix + "?text=a+test&language=en"));
    assertTrue("Got " + result, result.contains("UPPERCASE_SENTENCE_START"));
  }

  private String checkAtUrl(URL url) throws IOException {
    final InputStream stream = (InputStream)url.getContent();
    final String result = StringTools.streamToString(stream, "UTF-8");
    return result;
  }

}
