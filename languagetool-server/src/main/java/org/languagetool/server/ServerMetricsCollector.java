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
import io.prometheus.client.guava.cache.CacheMetricsCollector;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

import java.io.IOException;
import java.util.Map;

public class ServerMetricsCollector {

  public enum RequestErrorType {
    QUEUE_FULL,
    TOO_MANY_ERRORS,
    MAX_CHECK_TIME,
    MAX_TEXT_SIZE,
    INVALID_REQUEST
  }
  
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
    String clientLabel = client != null ? client : "unknown";
    String langLabel = language != null ? language.getShortCode() : "unknown";
    String modeLabel = mode != null ? mode.name() : "unknown";

    checkCounter.labels(langLabel, clientLabel, modeLabel).inc();
    matchCounter.labels(langLabel, clientLabel, modeLabel).inc(matchCount);
    charactersCounter.labels(langLabel, clientLabel, modeLabel).inc(textSize);
    computationTimeCounter.labels(langLabel, clientLabel, modeLabel).inc((double) milliseconds / 1000.0);

    ruleMatches.forEach((ruleId, ruleMatchCount) -> {
      ruleMatchCounter.labels(langLabel, ruleId).inc(ruleMatchCount);
    });
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
