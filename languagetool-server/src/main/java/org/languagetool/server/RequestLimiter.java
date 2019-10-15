/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.JLanguageTool;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Limit the maximum number of request per IP address for a given time range.
 */
class RequestLimiter {

  // TODO: this only works if the period covered is larger than requestLimitPeriodInSeconds -
  // we should add a sanity check that warns if that's not the case
  static final int REQUEST_QUEUE_SIZE = 1000;

  final List<RequestEvent> requestEvents = new CopyOnWriteArrayList<>();
  
  private final int ipFingerprintFactor;
  private final int requestLimit;
  private final int requestLimitInBytes;
  private final int requestLimitPeriodInSeconds;
  private final Long server;
  private DatabaseLogger logger;

  /**
   * @param requestLimit the maximum number of request per <tt>requestLimitPeriodInSeconds</tt>
   * @param requestLimitPeriodInSeconds the time period over which requests are considered, in seconds
   * @param ipFingerprintFactor allow limits x times larger per ip when fingerprints differ
   *                            (i.e. assume there may be a maximum of x users behind the same ip)
   */
  RequestLimiter(int requestLimit, int requestLimitInBytes, int requestLimitPeriodInSeconds, int ipFingerprintFactor) {
    this.requestLimit = requestLimit;
    this.requestLimitInBytes = requestLimitInBytes;
    this.requestLimitPeriodInSeconds = requestLimitPeriodInSeconds;
    this.ipFingerprintFactor = ipFingerprintFactor;
    this.logger = DatabaseLogger.getInstance();
    if (this.logger.isLogging()) {
      DatabaseAccess db = DatabaseAccess.getInstance();
      this.server = db.getOrCreateServerId();
    } else {
      this.server = null;
    }
  }

  RequestLimiter(int requestLimit, int requestLimitInBytes, int requestLimitPeriodInSeconds) {
    this(requestLimit, requestLimitInBytes, requestLimitPeriodInSeconds, 1);
  }

  /**
   * The maximum number of request per {@link #getRequestLimitPeriodInSeconds()}.
   */
  int getRequestLimit() {
    return requestLimit;
  }

  /**
   * The maximum number of request bytes per {@link #getRequestLimitPeriodInSeconds()}.
   * @since 4.0
   */
  int getRequestLimitInBytes() {
    return requestLimitInBytes;
  }

  /**
   * The time period over which requests are considered, in seconds.
   */
  int getRequestLimitPeriodInSeconds() {
    return requestLimitPeriodInSeconds;
  }

  String computeFingerprint(Map<String, List<String>> httpHeader, Map<String, String> parameters) {
    List<String> empty = Collections.singletonList("");
    String separator = "|";
    List<String> fields = new LinkedList<>();
    fields.add(String.join(separator, httpHeader.getOrDefault("User-Agent", empty)));
    fields.add(String.join(separator, httpHeader.getOrDefault("Accept-Language", empty)));
    fields.add(String.join(separator, httpHeader.getOrDefault("Referer", empty)));
    fields.add(String.join(separator, parameters.getOrDefault("textSessionId", "")));
    return String.join(separator, fields);
  }

  /**
   * @param ipAddress the client's IP address
   * @throws TooManyRequestsException if access is not allowed because the request limit is reached
   */
  void checkAccess(String ipAddress, Map<String, String> params, Map<String, List<String>> httpHeader) {
    int reqSize = getRequestSize(params);
    while (requestEvents.size() > REQUEST_QUEUE_SIZE) {
      requestEvents.remove(0);
    }
    requestEvents.add(new RequestEvent(ipAddress, new Date(), reqSize, computeFingerprint(httpHeader, params), ServerTools.getMode(params)));
    checkLimit(ipAddress, params, httpHeader);
  }

  private int getRequestSize(Map<String, String> params) {
    String text = params.get("text");
    if (text != null) {
      return text.length();
    } else {
      String data = params.get("data");
      if (data != null) {
        return data.length();
      }
    }
    return 0;
  }

  private Long getClientId(Map<String, String> parameters) {
    if (logger.isLogging()) {
      DatabaseAccess db = DatabaseAccess.getInstance();
      String paramValue = parameters.get("useragent");
      if (paramValue == null) {
        return null;
      }
      Long agentId = db.getOrCreateClientId(paramValue);
      return agentId;
    } else {
      return null;
    }
  }

  private String getReferer(Map<String, List<String>> httpHeader) {
    List<String> values = httpHeader.get("Referer");
    if (values == null || values.isEmpty()) {
      return null;
    }
    return values.get(0);
  }

  private String getUserAgent(Map<String, List<String>> httpHeader) {
    List<String> values = httpHeader.get("User-Agent");
    if (values == null || values.isEmpty()) {
      return null;
    }
    return values.get(0);
  }

  void checkLimit(String ipAddress, Map<String, String> parameters, Map<String, List<String>> httpHeader) {
    int requestsByIp = 0;
    int requestSizeByIp = 0;
    int requestsByFingerprint = 0;
    int requestSizeByFingerprint = 0;
    // all requests before this date are considered old:
    Date thresholdDate = new Date(System.currentTimeMillis() - requestLimitPeriodInSeconds * 1000L);
    String fingerprint = computeFingerprint(httpHeader, parameters);
    String referer = getReferer(httpHeader);
    String userAgent = getUserAgent(httpHeader);
    Long clientId = getClientId(parameters);
    for (RequestEvent event : requestEvents) {
      if (event.ip.equals(ipAddress) && event.date.after(thresholdDate)) {
        // text level rules cause much less load, so count them accordingly
        float modeFactor = event.mode == JLanguageTool.Mode.TEXTLEVEL_ONLY ? 0.1f : 1f;
        requestsByIp++;
        requestSizeByIp += event.getSizeInBytes() * modeFactor;
        if (event.fingerprint.equals(fingerprint)) {
          requestsByFingerprint++;
          requestSizeByFingerprint += event.getSizeInBytes() * modeFactor;
        }
        if (requestLimit > 0 && requestsByFingerprint > requestLimit) {
          String msg = "limit: " + requestLimit + " / " + requestLimitPeriodInSeconds + ", requests: "  + requestsByIp + ", ip: " + ipAddress + ", fingerprint: " + fingerprint;
          logger.log(new DatabaseAccessLimitLogEntry("MaxRequestPerPeriodFingerprint", server, clientId, null, msg, referer, userAgent));
          throw new TooManyRequestsException("Client request limit of " + requestLimit + " requests per " +
            requestLimitPeriodInSeconds + " seconds exceeded"); }
        if (requestLimit > 0 && requestsByIp > requestLimit * ipFingerprintFactor) {
          String msg = "limit: " + requestLimit * ipFingerprintFactor + " / " + requestLimitPeriodInSeconds + ", requests: "  + requestsByIp + ", ip: " + ipAddress + ", fingerprint: " + fingerprint;
          logger.log(new DatabaseAccessLimitLogEntry("MaxRequestPerPeriodIp", server, clientId, null, msg, referer, userAgent));
          throw new TooManyRequestsException("IP request limit of " + requestLimit * ipFingerprintFactor + " requests per " +
            requestLimitPeriodInSeconds + " seconds exceeded");
        }
        if (event.mode == JLanguageTool.Mode.TEXTLEVEL_ONLY) {
          if (requestLimitInBytes > 0 && requestSizeByFingerprint > requestLimitInBytes) {
            String msg = "limit in Mode.TEXTLEVEL_ONLY: " + requestLimitInBytes + " / " + requestLimitPeriodInSeconds + ", request size: "  + requestSizeByIp + ", ip: " + ipAddress + ", fingerprint: " + fingerprint;
            logger.log(new DatabaseAccessLimitLogEntry("MaxRequestSizePerPeriodFingerprint", server, clientId, null, msg, referer, userAgent));
            throw new TooManyRequestsException("Client request size limit of " + requestLimitInBytes + " bytes per " +
              requestLimitPeriodInSeconds + " seconds exceeded in text-level checks");
          }
          if (requestLimitInBytes > 0 && requestSizeByIp > requestLimitInBytes * ipFingerprintFactor) {
            String msg = "limit in Mode.TEXTLEVEL_ONLY: " + requestLimitInBytes * ipFingerprintFactor + " / " + requestLimitPeriodInSeconds + ", request size: "  + requestSizeByIp + ", ip: " + ipAddress + ", fingerprint: " + fingerprint;
            logger.log(new DatabaseAccessLimitLogEntry("MaxRequestSizePerPeriodIp", server, clientId, null, msg, referer, userAgent));
            throw new TooManyRequestsException("IP request size limit of " + requestLimitInBytes * ipFingerprintFactor + " bytes per " +
              requestLimitPeriodInSeconds + " seconds exceeded in text-level checks");
          }
        } else {
          if (requestLimitInBytes > 0 && requestSizeByFingerprint > requestLimitInBytes) {
            String msg = "limit: " + requestLimitInBytes + " / " + requestLimitPeriodInSeconds + ", request size: "  + requestSizeByIp + ", ip: " + ipAddress + ", fingerprint: " + fingerprint;
            logger.log(new DatabaseAccessLimitLogEntry("MaxRequestSizePerPeriodFingerprint", server, clientId, null, msg, referer, userAgent));
            throw new TooManyRequestsException("Client request size limit of " + requestLimitInBytes + " bytes per " +
              requestLimitPeriodInSeconds + " seconds exceeded");
          }
          if (requestLimitInBytes > 0 && requestSizeByIp > requestLimitInBytes * ipFingerprintFactor) {
            String msg = "limit: " + requestLimitInBytes * ipFingerprintFactor + " / " + requestLimitPeriodInSeconds + ", request size: "  + requestSizeByIp + ", ip: " + ipAddress + ", fingerprint: " + fingerprint;
            logger.log(new DatabaseAccessLimitLogEntry("MaxRequestSizePerPeriodIp", server, clientId, null, msg, referer, userAgent));
            throw new TooManyRequestsException("IP request size limit of " + requestLimitInBytes * ipFingerprintFactor + " bytes per " +
              requestLimitPeriodInSeconds + " seconds exceeded");
          }
        }
      }
    }
  }
  
  protected static class RequestEvent {

    private final String ip;
    private final Date date;
    private final int sizeInBytes;
    private final String fingerprint;
    private final JLanguageTool.Mode mode;

    RequestEvent(String ip, Date date, int sizeInBytes, String fingerprint, JLanguageTool.Mode mode) {
      this.ip = ip;
      this.date = new Date(date.getTime());
      this.sizeInBytes = sizeInBytes;
      this.fingerprint = fingerprint;
      this.mode = mode;
    }

    protected Date getDate() {
      return date;
    }
    
    int getSizeInBytes() {
      return sizeInBytes;
    }

  }

}
