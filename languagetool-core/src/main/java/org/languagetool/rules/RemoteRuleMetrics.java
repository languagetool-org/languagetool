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

  private static final double[] LATENCY_BUCKETS = new double[] {
    .01, .02, .03, .04, .05, .06, .07, .08, .09,
    .10, .11, .12, .13, .14, .15, .16, .17, .18, .19,
    .20, .22, .24, .26, .28, .30, .32, .34, .36, .38,
    .40, .42, .44, .46, .48, .50, .52, .54, .56, .58,
    .60, .62, .64, .66, .68, .70, .72, .74, .76, .78,
    .80, .82, .84, .86, .88, .90, .92, .94, .96, .98,
    1.0, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9,
    2.0, 2.1, 2.2, 2.3, 2.4, 2.5, 2.6, 2.7, 2.8, 2.9,
    3.0, 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9,
    4.0, 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9,
    5.0, 5.5, 6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0, 9.5,
    10., 11., 12., 13., 14., 15., 16., 17., 18., 19.,
  };

  private static final double[] SIZE_BUCKETS = new double[] {
    25, 50, 100, 150, 200, 250, 300, 400, 500, 750, 1000, 2000, 3000, 4000, 5000, 7500, 10000, 15000, 20000, 30000, 40000
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
