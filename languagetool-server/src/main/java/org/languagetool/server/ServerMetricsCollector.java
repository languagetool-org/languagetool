/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Fabian Richter
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

import com.google.common.cache.Cache;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import io.prometheus.client.guava.cache.CacheMetricsCollector;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

import java.io.IOException;
import java.util.*;

public class ServerMetricsCollector {

  public static final String UNKNOWN = "unknown";

  public enum RequestErrorType {
    QUEUE_FULL,
    TOO_MANY_ERRORS,
    MAX_CHECK_TIME,
    MAX_TEXT_SIZE,
    INVALID_REQUEST
  }

  private static final double[] LATENCY_BUCKETS = new double[] {
    .025, .05, .075, .1, .125, .15, .175, .2, .25, .3, .35, .4, .45, .5, .75, 1.,
    1.25, 1.5, 1.75, 2., 2.5, 3., 4., 5., 7.5, 10., 15.
  };

  private static final double[] SIZE_BUCKETS = new double[] {
    25, 50, 100, 150, 200, 250, 300, 400, 500, 750, 1000, 2500, 5000, 7500, 10000, 15000, 20000, 30000, 40000
  };

  // client is user-provided data, don't create unlimited amount of time series in prometheus
  private static final Set<String> CLIENTS = new HashSet<>(Arrays.asList(
    "webextension-chrome-ng",
    "webextension-firefox-ng",
    "ltorg",
    "sdl-trados-addon-public",
    "webextension-opera-ng",
    "ltplus",
    "googledocs",
    "webextension-firefox",
    "webextension-chrome",
    "mswordpremiumJS-demo",
    "webextension-unknown-ng",
    "java-http-client",
    "msword",
    "androidspell",
    "mswordpremiumJS",
    "webextension-edge-ng"
  ));
  private static final String CLIENT_OTHER = "other";

  private static final ServerMetricsCollector collector = new ServerMetricsCollector();
  
  private static HTTPServer server;

  private final Counter checkCounter = Counter
    .build("languagetool_checks_total", "Total text checks")
    .labelNames("language", "client", "mode").register();
  private final Counter charactersCounter = Counter
    .build("languagetool_characters_total", "Total processed characters")
    .labelNames("language", "client", "mode").register();
  private final Counter matchCounter = Counter
    .build("languagetool_check_matches_total", "Total amount of rule matches")
    .labelNames("language", "client", "mode").register();
  private final Counter computationTimeCounter = Counter
    .build("languagetool_computation_time_seconds_total", "Total computation time, in seconds")
    .labelNames("language", "client", "mode").register();


  // no client label for these for now, combined with buckets -> combinatorial explosion
  private final Histogram checkLatency = Histogram
    .build("languagetool_check_latency_seconds", "Histogram of check times")
    .buckets(LATENCY_BUCKETS)
    .labelNames("language", "mode").register();
  private final Histogram checkSize = Histogram
    .build("languagetool_check_size_characters", "Histogram of check sizes")
    .buckets(SIZE_BUCKETS).labelNames("language", "mode").register();

  private final Counter ruleMatchCounter = Counter
    .build("languagetool_rule_matches_total", "Total amount of matches of a given rule")
    .labelNames("language", "rule_id").register();

  private final Counter requestErrorCounter = Counter
    .build("languagetool_request_errors_total", "Various request errors")
    .labelNames("reason").register();

  // TODO add label for route, method, ...?
  private final Counter httpRequestCounter = Counter
    .build("languagetool_http_requests_total", "Received HTTP requests").register();

  private final Counter httpResponseCounter = Counter
    .build("languagetool_http_responses_total", "HTTP responses by code")
    .labelNames("code").register();

  private final Counter failedHealthcheckCounter = Counter
    .build("languagetool_failed_healthchecks_total", "Failed healthchecks").register();

  private final Gauge hiddenMatchesServerEnabled = Gauge
    .build("languagetool_hidden_matches_server_enabled", "Configuration of hidden matches server").register();
  private final Gauge hiddenMatchesServerStatus = Gauge
    .build("languagetool_hidden_matches_server_up", "Status of hidden matches server").register();

  private final CacheMetricsCollector cacheMetrics = new CacheMetricsCollector().register();


  public static void init(int port) throws IOException {
    DefaultExports.initialize();
    server = new HTTPServer(port, true);
  }

  public static void stop() {
    server.stop();
  }

  public static ServerMetricsCollector getInstance() {
    return collector;
  }

  public void monitorCache(String name, Cache cache) {
    cacheMetrics.addCache(name, cache);
  }

  public void logHiddenServerConfiguration(boolean enabled) {
    hiddenMatchesServerEnabled.set(enabled ? 1.0 : 0.0);
  }

  public void logHiddenServerStatus(boolean up) {
    hiddenMatchesServerStatus.set(up ? 1.0 : 0.0);
  }

  public void logCheck(Language language, long milliseconds, int textSize, int matchCount,
                       JLanguageTool.Mode mode, @Nullable String client, Map<String, Integer> ruleMatches) {
    String clientLabel = cleanClientLabel(client);
    String langLabel = language != null ? language.getShortCode() : UNKNOWN;
    String modeLabel = mode != null ? mode.name() : UNKNOWN;

    checkCounter.labels(langLabel, clientLabel, modeLabel).inc();
    matchCounter.labels(langLabel, clientLabel, modeLabel).inc(matchCount);

    charactersCounter.labels(langLabel, clientLabel, modeLabel).inc(textSize);
    checkSize.labels(langLabel, modeLabel).observe(textSize);

    double seconds = (double) milliseconds / 1000.0;
    computationTimeCounter.labels(langLabel, clientLabel, modeLabel).inc(seconds);
    checkLatency.labels(langLabel, modeLabel).observe(seconds);

    ruleMatches.forEach((ruleId, ruleMatchCount) -> ruleMatchCounter.labels(langLabel, ruleId).inc(ruleMatchCount));
  }

  @NotNull
  private String cleanClientLabel(@Nullable String client) {
    String clientLabel;
    if (client != null && !client.equals("-")) {
      if (CLIENTS.contains(client)) {
        clientLabel = client;
      } else {
        clientLabel = CLIENT_OTHER;
      }
    } else {
      clientLabel = UNKNOWN;
    }
    return clientLabel;
  }

  public void logRequestError(RequestErrorType type) {
    requestErrorCounter.labels(type.name().toLowerCase()).inc();
  }

  public void logRequest() {
    httpRequestCounter.inc();
  }

  public void logResponse(int httpCode) {
    httpResponseCounter.labels(String.valueOf(httpCode)).inc();
  }

  public void logFailedHealthcheck() {
    failedHealthcheckCounter.inc();
  }

}
