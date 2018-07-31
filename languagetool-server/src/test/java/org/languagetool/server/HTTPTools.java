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
import java.net.ConnectException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

final class HTTPTools {

  private HTTPTools() {
  }

  /**
   * Get default port, but considering property {@code lt.default.port}.
   */
  public static int getDefaultPort() {
    String defaultPort = System.getProperty("lt.default.port");
    return defaultPort != null ? Integer.parseInt(defaultPort) : HTTPServerConfig.DEFAULT_PORT;
  }

  /**
   * For testing, we disable all checks because we use a self-signed certificate on the server
   * side and we want this test to run everywhere without importing the certificate into the JVM's trust store.
   *
   * See http://stackoverflow.com/questions/2893819/telling-java-to-accept-self-signed-ssl-certificate
   */
  static void disableCertChecks() throws NoSuchAlgorithmException, KeyManagementException {
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

  static String checkAtUrl(URL url) throws IOException {
    try {
      InputStream stream = (InputStream)url.getContent();
      return StringTools.streamToString(stream, "UTF-8");
    } catch (ConnectException e) {
      throw new RuntimeException("Could not connect to " + url, e);
    }
  }

  static String checkAtUrlByPost(URL url, String postData) throws IOException {
    return checkAtUrlByPost(url, postData, new HashMap<>());
  }
  
  static String checkAtUrlByPost(URL url, String postData, Map<String, String> properties) throws IOException {
    String keepAlive = System.getProperty("http.keepAlive");
    try {
      System.setProperty("http.keepAlive", "false");  // without this, there's an overhead of about 1 second - not sure why
      URLConnection connection = url.openConnection();
      for (Map.Entry<String, String> entry : properties.entrySet()) {
        connection.setRequestProperty(entry.getKey(), entry.getValue());
      }
      connection.setDoOutput(true);
      try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream())) {
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

}
