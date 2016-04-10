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

import org.junit.Test;
import org.languagetool.server.HTTPServer;
import org.languagetool.server.HTTPServerConfig;

import java.net.MalformedURLException;
import java.net.URL;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class RemoteLanguageToolIntegrationTest {

  private static final String serverUrl = "http://" + HTTPServerConfig.DEFAULT_HOST + ":" + HTTPServerConfig.DEFAULT_PORT;

  @Test
  public void testClient() throws MalformedURLException {
    HTTPServer server = new HTTPServer();
    try {
      server.run();
      RemoteLanguageTool lt = new RemoteLanguageTool(new URL(serverUrl));
      assertThat(lt.check("This is a correct sentence.", "en").getMatches().size(), is(0));
      assertThat(lt.check("Sentence wiht a typo not detected.", "en").getMatches().size(), is(0));
      assertThat(lt.check("Sentence wiht a typo detected.", "en-US").getMatches().size(), is(1));
      assertThat(lt.check("A sentence with a error.", "en").getMatches().size(), is(1));

      RemoteResult result1 = lt.check("A sentence with a error, and and another one", "en");
      assertThat(result1.getLanguage(), is("English"));
      assertThat(result1.getLanguageCode(), is("en"));
      assertThat(result1.getRemoteServer().getSoftware(), is("LanguageTool"));
      assertNotNull(result1.getRemoteServer().getVersion());
      assertNotNull(result1.getRemoteServer().getBuildDate());
      assertThat(result1.getMatches().size(), is(2));
      assertThat(result1.getMatches().get(0).getRuleId(), is("EN_A_VS_AN"));
      assertThat(result1.getMatches().get(1).getRuleId(), is("ENGLISH_WORD_REPEAT_RULE"));

      RemoteResult result2 = lt.checkWithLanguageGuessing("Ein Satz in Deutsch, mit etwas mehr Text, damit es auch geht.", "en");
      assertThat(result2.getLanguage(), is("German (Germany)"));
      assertThat(result2.getLanguageCode(), is("de-DE"));
      
      RemoteResult result3 = lt.checkWithLanguageGuessing("x", "fr");  // too short, fallback will be used
      assertThat(result3.getLanguage(), is("French"));
      assertThat(result3.getLanguageCode(), is("fr"));
      
      try {
        System.err.println("=== Testing invalid language code - ignore the following exception: ===");
        lt.check("foo", "xy");
        fail();
      } catch (RuntimeException e) {
        assertTrue(e.getMessage().contains("is not a language code known to LanguageTool"));
      }
    } finally {
      server.stop();
    }
  }
  
  @Test(expected=RuntimeException.class)
  public void testInvalidServer() throws MalformedURLException {
    RemoteLanguageTool lt = new RemoteLanguageTool(new URL("http://does-not-exist"));
    lt.check("foo", "en");
  }

  @Test(expected=RuntimeException.class)
  public void testWrongProtocol() throws MalformedURLException {
    String httpsUrl = "https://" + HTTPServerConfig.DEFAULT_HOST + ":" + HTTPServerConfig.DEFAULT_PORT;
    RemoteLanguageTool lt = new RemoteLanguageTool(new URL(httpsUrl));
    lt.check("foo", "en");
  }

  @Test(expected=RuntimeException.class)
  public void testInvalidProtocol() throws MalformedURLException {
    String httpsUrl = "ftp://" + HTTPServerConfig.DEFAULT_HOST + ":" + HTTPServerConfig.DEFAULT_PORT;
    RemoteLanguageTool lt = new RemoteLanguageTool(new URL(httpsUrl));
    lt.check("foo", "en");
  }
  
  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  @Test(expected=MalformedURLException.class)
  public void testProtocolTypo() throws MalformedURLException {
    String httpsUrl = "htp://" + HTTPServerConfig.DEFAULT_HOST + ":" + HTTPServerConfig.DEFAULT_PORT;
    new RemoteLanguageTool(new URL(httpsUrl));
  }
  
}
