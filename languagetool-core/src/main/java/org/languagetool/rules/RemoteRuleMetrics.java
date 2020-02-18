/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;

public final class RemoteRuleMetrics {

  private RemoteRuleMetrics() {
    throw new IllegalStateException("RemoteRuleMetrics should only be used via static methods.");
  }

  public enum RequestResult {
    SUCCESS,
    SKIPPED,
    TIMEOUT,
    INTERRUPTED,
    DOWN,
    ERROR
  }

  // TODO: provide configuration as info?

  @Deprecated
  private static final Counter requests = Counter.build("languagetool_remote_rule_requests_total",
    "Amount of requests sent for the given rule").labelNames("rule_id", "result").register();
  private static final Counter retries = Counter.build("languagetool_remote_rule_retries_total",
    "Amount of retries for the given rule").labelNames("rule_id").register();

  @Deprecated
  private static final Counter requestDuration = Counter.build("languagetool_remote_rule_request_duration_seconds_total",
    "Time spent waiting for requests of the given rule").labelNames("rule_id").register();

  private static final Counter downtime = Counter.build("languagetool_remote_rule_downtime_seconds_total",
    "Time remote rules were deactivated because of errors").labelNames("rule_id").register();

  // supersedes requests, requestDuration
  private static final Summary requestTimeSummary = Summary.build("languagetool_remote_rule_request_duration_seconds",
    "Request duration summary").labelNames("rule_id", "result")
    .quantile(0.5, 0.05).quantile(0.9, 0.01).register();

  private static final Summary requestSizeSummary = Summary.build("languagetool_remote_rule_request_size",
    "Request size summary").labelNames("rule_id", "result")
    .quantile(0.5, 0.05).quantile(0.9, 0.01).register();

  private static final Gauge failures = Gauge.build("languagetool_remote_rule_consecutive_failures",
    "Amount of consecutive failures").labelNames("rule_id").register();

  private static final Gauge up = Gauge.build("languagetool_remote_rule_up",
    "Status of remote rule").labelNames("rule_id").register();

  public static void request(String rule, int numRetries, long nanoseconds, long characters, RequestResult result) {
    requests.labels(rule, result.name().toLowerCase()).inc();
    requestTimeSummary.labels(rule, result.name().toLowerCase()).observe((double) nanoseconds / 1e9);
    requestSizeSummary.labels(rule, result.name().toLowerCase()).observe(characters);
    retries.labels(rule).inc(numRetries);
    requestDuration.labels(rule).inc((double) nanoseconds / 1e9);
  }

  public static void failures(String rule, int count) {
    failures.labels(rule).set(count);
  }

  public static void up(String rule, boolean isUp) {
    up.labels(rule).set(isUp ? 1 : 0);
  }

  public static void downtime(String rule, long milliseconds) {
    downtime.labels(rule).inc(milliseconds / 1000.0);
  }

}
