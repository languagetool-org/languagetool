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
    RemoteLanguageTool lt = new FakeRemoteLanguageTool(new URL("http://fake"));
    RemoteResult result1 = lt.check("some text, reply is hard-coded anyway", "en");
    runAsserts(result1);
    CheckConfiguration config = new CheckConfigurationBuilder("en").autoDetectLanguage().build();
    RemoteResult result2 = lt.check("some text, reply is hard-coded anyway", config);
    runAsserts(result2);
  }

  private void runAsserts(RemoteResult result) {
    assertThat(result.getLanguage(), is("English (US)"));
    assertThat(result.getLanguageCode(), is("en-US"));
    assertThat(result.getRemoteServer().getSoftware(), is("LanguageTool"));
    assertThat(result.getRemoteServer().getVersion(), is("3.4-SNAPSHOT"));
    assertThat(result.getRemoteServer().getBuildDate(), is("2016-04-07 22:01"));
    assertThat(result.getMatches().size(), is(1));
    RemoteRuleMatch match1 = result.getMatches().get(0);
    // required:
    assertThat(match1.getRuleId(), is("EN_A_VS_AN"));
    assertThat(match1.getMessage(), is("Use 'an' instead of 'a' if the following word starts with a vowel sound, e.g. 'an article', 'an hour'"));
    assertThat(match1.getRuleSubId().isPresent(), is(false));
    assertThat(match1.getContext(), is("It happened a hour ago."));
    assertThat(match1.getContextOffset(), is(12));
    assertThat(match1.getErrorLength(), is(1));
    assertThat(match1.getErrorOffset(), is(12));
    // optional:
    assertThat(match1.getReplacements().get().toString(), is("[an]"));
    assertThat(match1.getCategory().get(), is("Miscellaneous"));
    assertThat(match1.getCategoryId().get(), is("MISC"));
    assertThat(match1.getLocQualityIssueType().get(), is("misspelling"));
    assertThat(match1.getShortMessage().get(), is("Wrong article"));
    assertThat(match1.getUrl().isPresent(), is(false));
  }

  static class FakeRemoteLanguageTool extends RemoteLanguageTool {

    FakeRemoteLanguageTool(URL serverUrl) {
      super(serverUrl);
    }

    @Override
    HttpURLConnection getConnection(byte[] postData) {
      URL fakeUrl;
      try {
        fakeUrl = new URL("https://fake");
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
      return new HttpURLConnection(fakeUrl) {
        @Override public void disconnect() {}
        @Override public boolean usingProxy() { return false; }
        @Override public void connect() throws IOException {}
        @Override public int getResponseCode() { return HTTP_OK; }
        @Override
        public InputStream getInputStream() throws IOException {
          String response = "<matches software=\"LanguageTool\" version=\"3.4-SNAPSHOT\" buildDate=\"2016-04-07 22:01\">" +
                            "<language shortname=\"en-US\" name=\"English (US)\"/>" +
                            "<error fromy=\"0\" fromx=\"12\" toy=\"0\" tox=\"13\" ruleId=\"EN_A_VS_AN\"" +
                            "  msg=\"Use 'an' instead of 'a' if the following word starts with a vowel sound, e.g. 'an article', 'an hour'\"" +
                            "  shortmsg=\"Wrong article\" replacements=\"an\" context=\"It happened a hour ago.\" " +
                            "  contextoffset=\"12\" offset=\"12\" errorlength=\"1\" category=\"Miscellaneous\" " +
                            "  categoryid=\"MISC\" locqualityissuetype=\"misspelling\"/>" +
                            "</matches>";
          return new ByteArrayInputStream(response.getBytes());
        }
      };
    }
    
  }
  
}
