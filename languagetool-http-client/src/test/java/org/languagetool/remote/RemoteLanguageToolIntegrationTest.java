/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.remote;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.languagetool.server.HTTPSServer;
import org.languagetool.server.HTTPSServerConfig;
import org.languagetool.server.HTTPServer;
import org.languagetool.server.HTTPServerConfig;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;

public class RemoteLanguageToolIntegrationTest {

  private static final String serverUrl = "http://" + HTTPServerConfig.DEFAULT_HOST + ":" + HTTPTools.getDefaultPort();

  @Test
  @Disabled("for interactive use only")
  public void testPublicServer() throws MalformedURLException {
    RemoteLanguageTool lt = new RemoteLanguageTool(new URL("https://languagetool.org/api"));
    RemoteResult matches = lt.check("This is an test.", "en");
    System.out.println("matches: " + matches);
  }
  
  @Test
  public void testClient() throws MalformedURLException {
    HTTPServerConfig config = new HTTPServerConfig(HTTPTools.getDefaultPort());
    HTTPServer server = new HTTPServer(config);
    try {
      server.run();
      RemoteLanguageTool lt = new RemoteLanguageTool(new URL(serverUrl));
      MatcherAssert.assertThat(lt.check("This is a correct sentence.", "en").getMatches().size(), is(0));
      MatcherAssert.assertThat(lt.check("Sentence wiht a typo not detected.", "en").getMatches().size(), is(0));
      MatcherAssert.assertThat(lt.check("Sentence wiht a typo detected.", "en-US").getMatches().size(), is(1));
      MatcherAssert.assertThat(lt.check("A sentence with a error.", "en").getMatches().size(), is(1));
      MatcherAssert.assertThat(lt.check("Test escape: %", "en").getMatches().size(), is(0));

      RemoteResult result1 = lt.check("A sentence with a error, and and another one", "en");
      MatcherAssert.assertThat(result1.getLanguage(), is("English"));
      MatcherAssert.assertThat(result1.getLanguageCode(), is("en"));
      MatcherAssert.assertThat(result1.getRemoteServer().getSoftware(), is("LanguageTool"));
      Assertions.assertNotNull(result1.getRemoteServer().getVersion());
      MatcherAssert.assertThat(result1.getMatches().size(), is(2));
      MatcherAssert.assertThat(result1.getMatches().get(0).getRuleId(), is("EN_A_VS_AN"));
      MatcherAssert.assertThat(result1.getMatches().get(1).getRuleId(), is("ENGLISH_WORD_REPEAT_RULE"));

      CheckConfiguration disabledConfig = new CheckConfigurationBuilder("en").disabledRuleIds("EN_A_VS_AN").build();
      RemoteResult result2 = lt.check("A sentence with a error, and and another one", disabledConfig);
      MatcherAssert.assertThat(result2.getMatches().size(), is(1));
      MatcherAssert.assertThat(result2.getMatches().get(0).getRuleId(), is("ENGLISH_WORD_REPEAT_RULE"));

      CheckConfiguration enabledConfig = new CheckConfigurationBuilder("en").enabledRuleIds("EN_A_VS_AN").build();
      RemoteResult result3 = lt.check("A sentence with a error, and and another one", enabledConfig);
      MatcherAssert.assertThat(result3.getMatches().size(), is(2));

      CheckConfiguration enabledOnlyConfig = new CheckConfigurationBuilder("en").enabledRuleIds("EN_A_VS_AN").enabledOnly().build();
      RemoteResult result4 = lt.check("A sentence with a error, and and another one", enabledOnlyConfig);
      MatcherAssert.assertThat(result4.getMatches().size(), is(1));
      MatcherAssert.assertThat(result4.getMatches().get(0).getRuleId(), is("EN_A_VS_AN"));

      CheckConfiguration config1 = new CheckConfigurationBuilder().build();
      RemoteResult result5 = lt.check("Ein Satz in Deutsch, mit etwas mehr Text, damit es auch geht.", config1);
      MatcherAssert.assertThat(result5.getLanguage(), is("German (Germany)"));
      MatcherAssert.assertThat(result5.getLanguageCode(), is("de-DE"));

      RemoteResult result7 = lt.check("Das Häuser ist schön.", "de");
      MatcherAssert.assertThat(result7.getMatches().size(), is(1));
      MatcherAssert.assertThat(result7.getMatches().get(0).getRuleId(), is("DE_AGREEMENT"));

      try {
        System.err.println("=== Testing invalid language code - ignore the following exception: ===");
        lt.check("foo", "xy");
        Assertions.fail();
      } catch (RuntimeException e) {
        Assertions.assertTrue(e.getMessage().contains("is not a language code known to LanguageTool"));
      }
    } finally {
      server.stop();
    }
  }

  @Test
  public void testClientWithHTTPS() throws MalformedURLException, KeyManagementException, NoSuchAlgorithmException {
    disableCertChecks();
    String keyStore = RemoteLanguageToolIntegrationTest.class.getResource("/org/languagetool/remote/test-keystore.jks").getFile();
    HTTPSServerConfig config = new HTTPSServerConfig(HTTPTools.getDefaultPort(), false, new File(keyStore), "mytest");
    HTTPSServer server = new HTTPSServer(config, false, "localhost", Collections.singleton("127.0.0.1"));
    try {
      server.run();
      RemoteLanguageTool lt = new RemoteLanguageTool(new URL(serverUrl.replace("http:", "https:")));
      MatcherAssert.assertThat(lt.check("This is a correct sentence.", "en").getMatches().size(), is(0));
    } finally {
      server.stop();
    }
  }

  private void disableCertChecks() throws NoSuchAlgorithmException, KeyManagementException {
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

  @Test
  public void testInvalidServer() throws MalformedURLException {
    RuntimeException thrown =  Assertions.assertThrows(RuntimeException.class, () -> {
      RemoteLanguageTool lt = new RemoteLanguageTool(new URL("http://does-not-exist"));
      lt.check("foo", "en");
    });
  }

  @Test
  public void testWrongProtocol() throws MalformedURLException {
    RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
      String httpsUrl = "https://" + HTTPServerConfig.DEFAULT_HOST + ":" + HTTPServerConfig.DEFAULT_PORT;
      RemoteLanguageTool lt = new RemoteLanguageTool(new URL(httpsUrl));
      lt.check("foo", "en");
    });
  }

  @Test
  public void testInvalidProtocol() throws MalformedURLException {
    RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
      String httpsUrl = "ftp://" + HTTPServerConfig.DEFAULT_HOST + ":" + HTTPServerConfig.DEFAULT_PORT;
      RemoteLanguageTool lt = new RemoteLanguageTool(new URL(httpsUrl));
      lt.check("foo", "en");
    });
  }
  
  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  @Test
  public void testProtocolTypo() throws MalformedURLException {
    MalformedURLException thrown = Assertions.assertThrows(MalformedURLException.class, () -> {
      String httpsUrl = "htp://" + HTTPServerConfig.DEFAULT_HOST + ":" + HTTPServerConfig.DEFAULT_PORT;
      new RemoteLanguageTool(new URL(httpsUrl));  
    });
  }
}
