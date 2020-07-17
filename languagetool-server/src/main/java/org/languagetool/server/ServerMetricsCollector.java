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
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.guava.cache.CacheMetricsCollector;
import io.prometheus.client.hotspot.DefaultExports;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ServerMetricsCollector {

  public static final String UNKNOWN = "unknown";

  public enum RequestErrorType {
    QUEUE_FULL,
    TOO_MANY_ERRORS,
    MAX_CHECK_TIME,
    MAX_TEXT_SIZE,
    INVALID_REQUEST
  }

  private static final double[] LATENCY_BUCKETS = {
    .025, .05, .075, .1, .125, .15, .175, .2, .25, .3, .35, .4, .45, .5, .75, 1.,
    1.25, 1.5, 1.75, 2., 2.5, 3., 4., 5., 7.5, 10., 15.
  };

  private static final double[] SIZE_BUCKETS = {
    25, 50, 100, 150, 200, 250, 300, 400, 500, 750, 1000, 2500, 5000, 7500, 10000, 15000, 20000, 30000, 40000
  };

  // buckets for processing speed in chars/s;
  private static final double[] SPEED_BUCKETS = {
// expected min: e.g. 100 chars in 10s = 10 chars/s
    10, 50, 100, 150, 200, 250, 300, 350, 400, 450, 500,
    550, 600, 650, 700, 750, 800, 850, 900, 950,
    1000, 1100, 1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900,
    2000, 2100, 2200, 2300, 2400, 2500, 2600, 2700, 2800, 2900,
    3000, 3100, 3200, 3300, 3400, 3500, 3600, 3700, 3800, 3900,
    4000, 4100, 4200, 4300, 4400, 4500, 4600, 4700, 4800, 4900,
    5000, 5100, 5200, 5300, 5400, 5500, 5600, 5700, 5800, 5900,
    6000, 6100, 6200, 6300, 6400, 6500, 6600, 6700, 6800, 6900,
    7000, 7100, 7200, 7300, 7400, 7500, 7600, 7700, 7800, 7900,
    8000, 8100, 8200, 8300, 8400, 8500, 8600, 8700, 8800, 8900,
    9000, 9100, 9200, 9300, 9400, 9500, 9600, 9700, 9800, 9900,
    10000, 15000, 20000, 25000, 30000, 400000, 50000, 100000
// expected max: e.g. 100 chars in 1ms = 100,000 chars/s
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
  private final Histogram checkSpeed = Histogram
    .build("languagetool_check_speed_chars_per_second", "Histogram of relative check speed")
    .buckets(SPEED_BUCKETS).labelNames("language", "mode").register();

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
  private final Counter hiddenMatchesServerRequests = Counter
    .build("languagetool_hidden_matches_server_requests_total", "Number of hidden server requests by status")
    .labelNames("status").register();

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

  public void logHiddenServerRequest(boolean success) {
    if (hiddenMatchesServerStatus.get() == 0.0) {
      hiddenMatchesServerRequests.labels("down").inc();
    } else if (success) {
      hiddenMatchesServerRequests.labels("success").inc();
    } else {
      hiddenMatchesServerRequests.labels("failure").inc();
    }
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

    double speed = textSize / seconds;
    checkSpeed.labels(langLabel, modeLabel).observe(speed);

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
