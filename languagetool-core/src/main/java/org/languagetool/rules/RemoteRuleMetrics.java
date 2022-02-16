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

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.prometheus.client.Histogram;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public final class RemoteRuleMetrics {
  private static final Logger logger = LoggerFactory.getLogger(RemoteRuleMetrics.class);

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

  private static final double[] WAIT_BUCKETS = {
    .05, .1, .2, .3, .4, .5, .75, 1., 2., 5., 7.5, 10., 15.
  };

  private static final double[] LATENCY_BUCKETS = {
    0.025, 0.05, .1, .25, .5, .75, 1., 2., 4., 6., 8., 10., 15.
  };

  private static final double[] SIZE_BUCKETS = {
    25, 100, 500, 1000, 2500, 5000, 10000, 20000, 40000
  };


  private static final Histogram wait = Histogram
    .build("languagetool_remote_rule_wait_seconds", "Time spent waiting on remote rule results/timeouts")
    .labelNames("language")
    .buckets(WAIT_BUCKETS)
    .register();

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

  public static void request(String rule, long startNanos, long characters, RequestResult result) {
    long delta = System.nanoTime() - startNanos;
    requestLatency.labels(rule, result.name().toLowerCase()).observe((double) delta / 1e9);
    requestThroughput.labels(rule, result.name().toLowerCase()).observe(characters);
  }

  public static void wait(String langCode, long milliseconds) {
    wait.labels(langCode).observe(milliseconds / 1000.0);
  }

  @ApiStatus.Internal
  public static void inCircuitBreaker(long deadlineStartNanos, RemoteRule rule, String ruleKey, long chars, Callable<RemoteRuleResult> fetchResults) throws InterruptedException {
    try {
      rule.circuitBreaker().executeCallable(fetchResults);
    } catch (InterruptedException e) {
      logger.info("Failed to fetch result from remote rule '{}' - interrupted.", ruleKey);
      request(ruleKey, deadlineStartNanos, chars, RequestResult.INTERRUPTED);
      throw e;
    } catch (CancellationException e) {
      logger.info("Failed to fetch result from remote rule '{}' - cancelled.", ruleKey);
      request(ruleKey, deadlineStartNanos, chars, RequestResult.INTERRUPTED);
    } catch (TimeoutException e) {
      long timeout = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - deadlineStartNanos);
      logger.info("Failed to fetch result from remote rule '{}' - timed out ({}ms, {} chars).",
        ruleKey, timeout, chars);
      request(ruleKey, deadlineStartNanos, chars, RequestResult.TIMEOUT);
    } catch (CallNotPermittedException e) {
      logger.info("Failed to fetch result from remote rule '{}' - circuitbreaker active, rule marked as down.", ruleKey);
      request(ruleKey, deadlineStartNanos, chars, RequestResult.DOWN);
    } catch (Exception e) {
      if (ExceptionUtils.indexOfThrowable(e, TimeoutException.class) != -1) {
        long timeout = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - deadlineStartNanos);
        logger.info("Failed to fetch result from remote rule '{}' - timed out with exception {} ({}ms, {} chars).",
          ruleKey, e, timeout, chars);
        request(ruleKey, deadlineStartNanos, chars, RequestResult.TIMEOUT);
      } else {
        logger.warn("Failed to fetch result from remote rule '" + ruleKey + "' - error while executing rule.", e);
        request(ruleKey, deadlineStartNanos, chars, RequestResult.ERROR);
      }
    }
  }
}
