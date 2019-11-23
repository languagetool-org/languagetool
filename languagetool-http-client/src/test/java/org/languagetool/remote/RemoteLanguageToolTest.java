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
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class RemoteLanguageToolTest { 

  @Test
  public void testResultParsing() throws IOException {
    RemoteLanguageTool lt = new FakeRemoteLanguageTool("response.json");
    RemoteResult result1 = lt.check("some text, reply is hard-coded anyway", "en");
    runAsserts(result1);
    CheckConfiguration config = new CheckConfigurationBuilder().build();
    RemoteResult result2 = lt.check("some text, reply is hard-coded anyway", config);
    runAsserts(result2);
    RemoteLanguageTool lt2 = new FakeRemoteLanguageTool("response-with-url.json");
    RemoteResult result3 = lt2.check("some text, reply is hard-coded anyway", config);
    assertThat(result3.getMatches().get(0).getUrl().get(), is("https://fake.org/foo"));
  }

  private void runAsserts(RemoteResult result) {
    assertThat(result.getLanguage(), is("English (US)"));
    assertThat(result.getLanguageCode(), is("en-US"));
    assertThat(result.getRemoteServer().getSoftware(), is("LanguageTool"));
    assertThat(result.getRemoteServer().getVersion(), is("3.4-SNAPSHOT"));
    assertThat(result.getRemoteServer().getBuildDate().get(), is("2016-05-27 12:04"));
    assertThat(result.getMatches().size(), is(1));
    RemoteRuleMatch match1 = result.getMatches().get(0);
    assertThat(match1.getRuleId(), is("EN_A_VS_AN"));
    assertThat(match1.getMessage(), is("Use \"an\" instead of 'a' if the following word starts with a vowel sound, e.g. 'an article', 'an hour'"));
    assertThat(match1.getRuleSubId().isPresent(), is(false));
    assertThat(match1.getContext(), is("It happened a hour ago."));
    assertThat(match1.getContextOffset(), is(12));
    assertThat(match1.getErrorLength(), is(1));
    assertThat(match1.getErrorOffset(), is(12));
    assertThat(match1.getReplacements().get().toString(), is("[an]"));
    assertThat(match1.getCategory().get(), is("Miscellaneous"));
    assertThat(match1.getCategoryId().get(), is("MISC"));
    assertThat(match1.getLocQualityIssueType().get(), is("misspelling"));
    assertThat(match1.getShortMessage().get(), is("Wrong article"));
    assertThat(match1.getUrl().isPresent(), is(false));
  }

  private static class FakeRemoteLanguageTool extends RemoteLanguageTool {

    private final String jsonFile;

    FakeRemoteLanguageTool(String jsonFile) throws MalformedURLException {
      super(new URL("http://fake"));
      this.jsonFile = jsonFile;
    }

    @Override
    HttpURLConnection getConnection(byte[] postData, URL url) {
      URL fakeUrl = Tools.getUrl("https://fake");
      return new HttpURLConnection(fakeUrl) {
        @Override public void disconnect() {}
        @Override public boolean usingProxy() { return false; }
        @Override public void connect() throws IOException {}
        @Override public int getResponseCode() { return HTTP_OK; }
        @Override
        public InputStream getInputStream() throws IOException {
          String response = StringTools.readStream(RemoteLanguageToolTest.class.getResourceAsStream("/org/languagetool/remote/" + jsonFile), "utf-8");
          return new ByteArrayInputStream(response.getBytes());
        }
      };
    }
    
  }
  
}
