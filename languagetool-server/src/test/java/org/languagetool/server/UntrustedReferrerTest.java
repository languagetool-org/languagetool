/*
 * LanguageTool, a natural language style checker
 * Copyright (c) 2024.  Stefan Viol (https://stevio.de)
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  USA
 */

package org.languagetool.server;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class UntrustedReferrerTest {

  private OkHttpClient client = new OkHttpClient()
    .newBuilder()
    .readTimeout(300, TimeUnit.SECONDS)
    .connectTimeout(300, TimeUnit.SECONDS)
    .writeTimeout(300, TimeUnit.SECONDS)
    .build();

  @Test
  @Ignore("Needs a local remote-rule file")
  public void runUntrustedReferrerTest() throws Exception {
    HTTPServerConfig serverConfig = new HTTPServerConfig(HTTPTestTools.getDefaultPort(), true);

    serverConfig.setUntrustedReferrers(Arrays.asList("foo.org", "bar.org", "baz.org", "language.com"));
    serverConfig.remoteRulesConfigFile = new File("/home/stefan/Dokumente/Test-Config-Files/test-untrusted.json");

    HTTPServer server = new HTTPServer(serverConfig, false, HTTPServerConfig.DEFAULT_HOST, null);
    assertFalse(server.isRunning());
    try {
      server.run();
      assertTrue(server.isRunning());
      runRequest("http://foo.org", false);
      runRequest("http://foo.org/something", false);
      runRequest("https://foo.org", false);
      runRequest("https://foo.org/something", false);
      runRequest("foo.org", false);
      runRequest("bar.org", false);
      runRequest("https://languagetool.org/", true);
      runRequest("http://languagetool.org/", true);
      runRequest("languagetool", true);
      runRequest("https://www.languagetool.org/", true);
    } finally {
      server.stop();
      assertFalse(server.isRunning());
    }
  }

  private void runRequest(String referrer, Boolean expectedMatch) throws IOException {
    Request request = new Request.Builder()
      .url("http://localhost:8081/v2/check?text=Ich%20möchte,%20dass%20das%20funktioniert&language=de-DE")
      .addHeader("Referer", referrer)
      .build();
    try (Response response = client.newCall(request).execute()) {
      if (response.body() != null) {
        String body = response.body().string();
        if (expectedMatch) {
          assertTrue(body.contains("AI_DE_GGEC"));
        } else {
          assertFalse(body.contains("AI_DE_GGEC"));
        }
      }
    }
  }
}
