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
import io.prometheus.client.Info;
import io.prometheus.client.Histogram;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.guava.cache.CacheMetricsCollector;
import io.prometheus.client.hotspot.DefaultExports;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

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
    0.025, 0.05, .1, .25, .5, .75, 1., 2., 4., 6., 8., 10., 15.
  };

  private static final double[] SIZE_BUCKETS = {
    25, 100, 500, 1000, 2500, 5000, 10000, 20000, 40000
  };

  // buckets for processing speed in chars/s;
  private static final double[] SPEED_BUCKETS = {
    10, 100, 500, 1000, 2500, 5000, 7500, 10000, 20000, 50000
  };

  private static final ServerMetricsCollector collector = new ServerMetricsCollector();
  
  private static HTTPServer server;

  private final Counter matchCounter = Counter
    .build("languagetool_check_matches_total", "Total amount of rule matches")
    .labelNames("language", "mode").register();

  // TODO: deprecate - replaced by histograms below
  private final Counter checkCounter = Counter
    .build("languagetool_checks_total", "Total text checks")
    .labelNames("language", "mode").register();
  private final Counter charactersCounter = Counter
    .build("languagetool_characters_total", "Total processed characters")
    .labelNames("language", "mode").register();
  private final Counter computationTimeCounter = Counter
    .build("languagetool_computation_time_seconds_total", "Total computation time, in seconds")
    .labelNames("language", "mode").register();


  // need to be very careful about cardinality with those
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
  private final Counter hiddenMatchesServerMatches = Counter
    .build("languagetool_hidden_matches_server_matches_total", "Number of matches returned by hidden matches server")
    .labelNames("language").register();

  private final Info buildInfo = Info
    .build("languagetool_build", "Build information").register();

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

  public ServerMetricsCollector() {
    buildInfo.info("version", Objects.toString(JLanguageTool.VERSION), "buildDate", Objects.toString(JLanguageTool.BUILD_DATE), "revision", Objects.toString(JLanguageTool.GIT_SHORT_ID), "premium", Objects.toString(String.valueOf(JLanguageTool.isPremiumVersion())));
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

  public void logHiddenServerRequest(boolean success, Language language, int matches) {
    if (hiddenMatchesServerStatus.get() == 0.0) {
      hiddenMatchesServerRequests.labels("down").inc();
    } else if (success) {
      hiddenMatchesServerRequests.labels("success").inc();
      String langLabel = language != null ? language.getShortCode() : UNKNOWN;
      hiddenMatchesServerMatches.labels(langLabel).inc(matches);
    } else {
      hiddenMatchesServerRequests.labels("failure").inc();
    }
  }

  public void logCheck(Language language, long milliseconds, int textSize, int matchCount,
                       JLanguageTool.Mode mode) {
    String langLabel = language != null ? language.getShortCode() : UNKNOWN;
    String modeLabel = mode != null ? mode.name() : UNKNOWN;

    checkCounter.labels(langLabel, modeLabel).inc();
    matchCounter.labels(langLabel, modeLabel).inc(matchCount);

    // split metrics: measure latency by language/mode, but not combined
    // so we can e.g. notice latency increases in a language or
    // create an alert for check speed of ALL_BUT_TEXT_LEVEL_ONLY requests

    charactersCounter.labels(langLabel, modeLabel).inc(textSize);
    checkSize.labels(langLabel, "").observe(textSize);
    checkSize.labels("", modeLabel).observe(textSize);

    double seconds = (double) milliseconds / 1000.0;
    computationTimeCounter.labels(langLabel, modeLabel).inc(seconds);
    checkLatency.labels(langLabel, "").observe(seconds);
    checkLatency.labels("", modeLabel).observe(seconds);

    double speed = textSize / seconds;
    checkSpeed.labels(langLabel, "").observe(speed);
    checkSpeed.labels("", modeLabel).observe(speed);
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
