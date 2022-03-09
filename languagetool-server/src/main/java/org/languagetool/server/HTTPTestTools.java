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

import org.languagetool.tools.StringTools;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class HTTPTestTools {

  private HTTPTestTools() {
  }

  /**
   * Get default port, but considering property {@code lt.default.port}.
   */
  public static int getDefaultPort() {
    String defaultPort = System.getProperty("lt.default.port");
    return defaultPort != null ? Integer.parseInt(defaultPort) : 8081; // see HTTPServerConfig.DEFAULT_PORT
  }

  /**
   * For testing, we disable all checks because we use a self-signed certificate on the server
   * side and we want this test to run everywhere without importing the certificate into the JVM's trust store.
   *
   * See http://stackoverflow.com/questions/2893819/telling-java-to-accept-self-signed-ssl-certificate
   */
  public static void disableCertChecks() throws NoSuchAlgorithmException, KeyManagementException {
    TrustManager[] trustAllCerts = {
            new X509TrustManager() {
              @Override
              public X509Certificate[] getAcceptedIssuers() {
                return null;
              }
              @Override
              public void checkClientTrusted(X509Certificate[] certs, String authType) {}
              @Override
              public void checkServerTrusted(X509Certificate[] certs, String authType) {}
            }
    };
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
  }

  public static String checkAtUrl(URL url) throws IOException {
    try {
      InputStream stream = (InputStream)url.getContent();
      return StringTools.streamToString(stream, "UTF-8");
    } catch (ConnectException e) {
      throw new RuntimeException("Could not connect to " + url, e);
    }
  }

  public static String checkAtUrlByPost(URL url, String postData) throws IOException {
    return checkAtUrlByPost(url, postData, new HashMap<>());
  }
  
  public static String checkAtUrlByPost(URL url, String postData, Map<String, String> properties) throws IOException {
    String keepAlive = System.getProperty("http.keepAlive");
    try {
      System.setProperty("http.keepAlive", "false");  // without this, there's an overhead of about 1 second - not sure why
      URLConnection connection = url.openConnection();
      for (Map.Entry<String, String> entry : properties.entrySet()) {
        connection.setRequestProperty(entry.getKey(), entry.getValue());
      }
      connection.setDoOutput(true);
      try (Writer writer = new OutputStreamWriter(connection.getOutputStream(), UTF_8)) {
        writer.write(postData);
        writer.flush();
        return StringTools.streamToString(connection.getInputStream(), "UTF-8");
      }
    } finally {
      if (keepAlive != null) {
        System.setProperty("http.keepAlive", keepAlive);
      }
    }
  }


  static class TestData {

    public static final String TOKEN_V2_1 = "4472b043ce935018e1a5bf5ef4b8a21b";
    public static final Long USER_GROUP_1 = 1L;
    public static final String NAME1 = "One";
    protected static final String USERNAME1 = "test@test.de";
    protected static final String API_KEY1 = "foo";
    protected static final long USER_ID1 = 1;
    protected static final Date PREMIUM_FROM1 = Date.valueOf("1970-01-01");
    protected static final String NAME2 = "Two";
    protected static final String USERNAME2 = "two@test.de";
    protected static final String API_KEY2 = "foo-two";
    protected static final Date PREMIUM_FROM2 = Date.valueOf("2000-01-01");
    protected static final long USER_ID2 = 2;
    protected static final String USERNAME3 = "free-account@example.com";
    protected static final String NAME3 = "Three";
    protected static final String PASSWORD3 = "password";
    protected static final long USER_ID3 = 3;
  }
}
