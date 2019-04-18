package org.languagetool.server;

import io.prometheus.client.Counter;
import io.prometheus.client.exporter.HTTPServer;
import io.prometheus.client.hotspot.DefaultExports;

import java.io.IOException;

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
    .build("languagetool_checks_total", "Total text checks").register();
  private final Counter charactersCounter = Counter
    .build("languagetool_characters_total", "Total processed characters").register();
  private final Counter computationTimeCounter = Counter
    .build("languagetool_computation_time_seconds_total", "Total computation time, in seconds").register();

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

  public void logCheck(long milliseconds, int textSize) {
    checkCounter.inc();
    charactersCounter.inc(textSize);
    computationTimeCounter.inc((double) milliseconds / 1000.0);
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
