/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2020 Fabian Richter
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

package org.languagetool.server;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import io.prometheus.client.Counter;

public class InstrumentedAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

  public static final String COUNTER_NAME = "languagetool_logback_appender_total";
  
  private static final Counter COUNTER;

  static {
    COUNTER = Counter.build().name(COUNTER_NAME)
      .help("Logback log statements at various log levels")
      .labelNames("level", "logger", "marker", "exception")
      .register();
  }

  /**
   * Create a new instrumented appender using the default registry.
   */
  public InstrumentedAppender() {
  }


  @Override
  public void start() {
    super.start();
  }

  @Override
  protected void append(ILoggingEvent event) {
    String marker = "";
    if (event.getMarker() != null) {
      marker = event.getMarker().getName();
    }
    String exception = "";
    if (event.getThrowableProxy() != null) {
      exception = event.getThrowableProxy().getClassName();
    }
    COUNTER.labels(event.getLevel().toString(), event.getLoggerName(), marker, exception).inc();
  }
}
