/*
 * LanguageTool, a natural language style checker
 * Copyright (c) 2023.  Stefan Viol (https://stevio.de)
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

package org.languagetool.server.utils;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public enum TelemetryProvider {

  INSTANCE;

  private final OpenTelemetry openTelemetry;
  private final Tracer tracer;

  TelemetryProvider() {
    Resource resource = Resource
            .getDefault()
            .merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "languagetool-server")));

    SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(LoggingSpanExporter.create()))
            .addSpanProcessor(SimpleSpanProcessor.create(OtlpJsonLoggingSpanExporter.create()))
//            .addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder().build()).build())
            .setResource(resource)
            .setClock(Clock.getDefault())
            .setSampler(Sampler.alwaysOn())
            .build();

    openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .buildAndRegisterGlobal();
    tracer = openTelemetry.getTracer("LanguageTool-Server");
  }

  /**
   * @param spanName     unique name of the span
   * @param attributes   custom attributes
   * @param wrappedValue function called within the span
   * @return the return value of wrappedValue function
   * @since 6.2   * @param spanName
   */
  public Object createSpan(String spanName, Attributes attributes, WrappedValue<?> wrappedValue) throws Exception {
    Span span = createSpan(spanName, attributes);
    try (Scope scope = span.makeCurrent()) {
      return wrappedValue.call();
    } finally {
      span.end();
    }
  }

  /**
   * @param spanName    unique name of the span
   * @param attributes  custom attributes
   * @param wrappedVoid function called within the span
   * @since 6.2
   */
  public void createSpan(String spanName, Attributes attributes, WrappedVoid wrappedVoid) throws Exception{
    Span span = createSpan(spanName, attributes);
    try (Scope scope = span.makeCurrent()) {
      wrappedVoid.call();
    } finally {
      span.end();
    }
  }

  public OpenTelemetry getOpenTelemetry() {
    return openTelemetry;
  }

  private Span createSpan(String spanName, Attributes attributes) {
    return tracer.spanBuilder(spanName)
            .setAllAttributes(attributes)
            .startSpan();
  }
}
