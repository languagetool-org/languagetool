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
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrustedSourcesTest {

  private OkHttpClient client = new OkHttpClient()
    .newBuilder()
    .readTimeout(300, TimeUnit.SECONDS)
    .connectTimeout(300, TimeUnit.SECONDS)
    .writeTimeout(300, TimeUnit.SECONDS)
    .build();

  @Test
  public void runUntrustedReferrerTest() throws Exception {
    StringBuilder propertiesFile = new StringBuilder();
    String remoteRuleFile = HTTPSServerConfigTest.class.getResource("remote_ts.json").getFile();
    propertiesFile.append("trustedSources=").append("\\\\b(?:workingAgent1|workingAgent2|workingAgent3)\\\\b");
    propertiesFile.append("\n");
    propertiesFile.append("remoteRulesFile=").append(remoteRuleFile);

    Path tempFile = Files.createTempFile("test_ts", ".properties");
    Files.write(tempFile, propertiesFile.toString().getBytes(), StandardOpenOption.WRITE);

    HTTPServerConfig serverConfig = new HTTPServerConfig(("--public --config " + tempFile.toAbsolutePath().toString()).split(" "));
    HTTPServer server = new HTTPServer(serverConfig, false, HTTPServerConfig.DEFAULT_HOST, null);
    assertFalse(server.isRunning());
    try {
      server.run();
      assertTrue(server.isRunning());
      runRequest("workingAgent1", true);
      runRequest("workingAgent2", true);
      runRequest("workingAgent2", true);
      runRequest("notWorkingAgent", false);
      runRequest("workingAgent", false);
      runRequest("workingAgent", false);
      runRequest("workingAgent", false);
      runRequest(null, false);
    } finally {
      server.stop();
      assertFalse(server.isRunning());
    }
  }

  private void runRequest(String ltAgent, Boolean expectedMatch) throws IOException {
    String agent = ltAgent == null ? "" : "&useragent=" + ltAgent;
    Request request = new Request.Builder()
      .url("http://localhost:8081/v2/check?text=Ich%20möchte,%20dass%20das%20funktioniert&language=de-DE" + agent)//missing punctuation
      .build();
    try (Response response = client.newCall(request).execute()) {
      if (response.body() != null) {
        String body = response.body().string();
        System.out.println(body);
        if (expectedMatch) {
          assertTrue(body.contains("Test match"));
        } else {
          assertFalse(body.contains("Test match"));
        }
      }
    }
  }
}
