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
import io.prometheus.client.Histogram;

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

  private static final double[] LATENCY_BUCKETS = {
    0.025, 0.05, .1, .25, .5, .75, 1., 2., 4., 6., 8., 10., 15.
  };

  private static final double[] SIZE_BUCKETS = {
    25, 100, 500, 1000, 2500, 5000, 10000, 20000, 40000
  };

  private static final Counter retries = Counter.build("languagetool_remote_rule_retries_total",
    "Amount of retries for the given rule").labelNames("rule_id").register();

  private static final Counter downtime = Counter.build("languagetool_remote_rule_downtime_seconds_total",
    "Time remote rules were deactivated because of errors").labelNames("rule_id").register();

  private static final Histogram requestLatency = Histogram
    .build("languagetool_remote_rule_request_latency_seconds", "Request duration summary")
    .labelNames("rule_id", "result")
    .buckets(LATENCY_BUCKETS)
    .register();

  private static final Histogram requestThroughput = Histogram
    .build("languagetool_remote_rule_request_throughput_characters", "Request size summary")
    .labelNames("rule_id", "result")
    .buckets(SIZE_BUCKETS)
    .register();

  private static final Gauge failures = Gauge.build("languagetool_remote_rule_consecutive_failures",
    "Amount of consecutive failures").labelNames("rule_id").register();

  private static final Gauge up = Gauge.build("languagetool_remote_rule_up",
    "Status of remote rule").labelNames("rule_id").register();

  public static void request(String rule, int numRetries, long nanoseconds, long characters, RequestResult result) {
    requestLatency.labels(rule, result.name().toLowerCase()).observe((double) nanoseconds / 1e9);
    requestThroughput.labels(rule, result.name().toLowerCase()).observe(characters);
    retries.labels(rule).inc(numRetries);
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
