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

import org.junit.jupiter.api.Test;
import org.hamcrest.MatcherAssert;
import org.languagetool.tools.StringTools;
import org.languagetool.tools.Tools;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.core.Is.is;

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
    MatcherAssert.assertThat(result3.getMatches().get(0).getUrl().get(), is("https://fake.org/foo"));
  }

  private void runAsserts(RemoteResult result) {
    MatcherAssert.assertThat(result.getLanguage(), is("English (US)"));
    MatcherAssert.assertThat(result.getLanguageCode(), is("en-US"));
    MatcherAssert.assertThat(result.getRemoteServer().getSoftware(), is("LanguageTool"));
    MatcherAssert.assertThat(result.getRemoteServer().getVersion(), is("3.4-SNAPSHOT"));
    MatcherAssert.assertThat(result.getRemoteServer().getBuildDate().get(), is("2016-05-27 12:04"));
    MatcherAssert.assertThat(result.getMatches().size(), is(1));
    RemoteRuleMatch match1 = result.getMatches().get(0);
    MatcherAssert.assertThat(match1.getRuleId(), is("EN_A_VS_AN"));
    MatcherAssert.assertThat(match1.getMessage(), is("Use \"an\" instead of 'a' if the following word starts with a vowel sound, e.g. 'an article', 'an hour'"));
    MatcherAssert.assertThat(match1.getRuleSubId().isPresent(), is(false));
    MatcherAssert.assertThat(match1.getContext(), is("It happened a hour ago."));
    MatcherAssert.assertThat(match1.getContextOffset(), is(12));
    MatcherAssert.assertThat(match1.getErrorLength(), is(1));
    MatcherAssert.assertThat(match1.getErrorOffset(), is(12));
    MatcherAssert.assertThat(match1.getReplacements().get().toString(), is("[an]"));
    MatcherAssert.assertThat(match1.getCategory().get(), is("Miscellaneous"));
    MatcherAssert.assertThat(match1.getCategoryId().get(), is("MISC"));
    MatcherAssert.assertThat(match1.getLocQualityIssueType().get(), is("misspelling"));
    MatcherAssert.assertThat(match1.getShortMessage().get(), is("Wrong article"));
    MatcherAssert.assertThat(match1.getUrl().isPresent(), is(false));
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
