/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2021 Fabian Richter
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

package org.languagetool.tools;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.micrometer.tagged.TaggedCircuitBreakerMetrics;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;

/**
 * Keep a central registry of circuit breakers so that we can expose metrics for all of them
 */
public class CircuitBreakers {

  private static final CircuitBreakers instance = new CircuitBreakers();
  private final CircuitBreakerRegistry registry;

  private CircuitBreakers() {
    registry = CircuitBreakerRegistry.ofDefaults();
    MeterRegistry metricsRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT,
      CollectorRegistry.defaultRegistry, Clock.SYSTEM);
    TaggedCircuitBreakerMetrics.ofCircuitBreakerRegistry(registry).bindTo(metricsRegistry);
  }

  public static CircuitBreakerRegistry registry() {
    return instance.registry;
  }
}
