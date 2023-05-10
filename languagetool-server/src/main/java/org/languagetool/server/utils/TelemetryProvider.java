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

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

public enum TelemetryProvider {

  INSTANCE;

  private final OpenTelemetry openTelemetry;
  private final Tracer tracer;

  TelemetryProvider() {
    openTelemetry = GlobalOpenTelemetry.get();
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
    span.setStatus(StatusCode.OK);
    try (Scope scope = span.makeCurrent()) {
      return wrappedValue.call();
    } catch (Exception ex) {
      span.recordException(ex);
      span.setStatus(StatusCode.ERROR);
      throw ex;
    } finally{
      span.end();
    }
  }

  /**
   * @param spanName    unique name of the span
   * @param attributes  custom attributes
   * @param wrappedVoid function called within the span
   * @since 6.2
   */
  public void createSpan(String spanName, Attributes attributes, WrappedVoid wrappedVoid) throws Exception {
    Span span = createSpan(spanName, attributes);
    span.setStatus(StatusCode.OK);
    try (Scope scope = span.makeCurrent()) {
      wrappedVoid.call();
    } catch (Exception ex) {
      span.recordException(ex);
      span.setStatus(StatusCode.ERROR);
      throw ex;
    } finally {
      span.end();
    }
  }

  /**
   * To use the openTelemetry instance directly without the helper functions
   * @since 6.2
   * @return a opentelemetry instance
   */
  public OpenTelemetry getOpenTelemetry() {
    return openTelemetry;
  }

  /**
   * @param spanName
   * @param attributes
   * @return a Span
   * @since 6.2
   */
  public Span createSpan(String spanName, Attributes attributes) {
    return tracer.spanBuilder(spanName)
            .setAllAttributes(attributes)
            .startSpan();
  }
}
