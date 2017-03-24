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

import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Limit the maximum number of request per IP address for a given time range.
 */
class RequestLimiter {

  private static final int API_REQUEST_QUEUE_SIZE = 1000;

  private final List<RequestEvent> requestEvents = new CopyOnWriteArrayList<>();
  private final int requestLimit;
  private final int requestLimitPeriodInSeconds;

  /**
   * @param requestLimit the maximum number of request per <tt>requestLimitPeriodInSeconds</tt>
   * @param requestLimitPeriodInSeconds the time period over which requests are considered, in seconds
   */
  RequestLimiter(int requestLimit, int requestLimitPeriodInSeconds) {
    this.requestLimit = requestLimit;
    this.requestLimitPeriodInSeconds = requestLimitPeriodInSeconds;
  }

  /**
   * The maximum number of request per {@link #getRequestLimitPeriodInSeconds()}.
   */
  int getRequestLimit() {
    return requestLimit;
  }

  /**
   * The time period over which requests are considered, in seconds.
   */
  int getRequestLimitPeriodInSeconds() {
    return requestLimitPeriodInSeconds;
  }

  /**
   * @param ipAddress the client's IP address
   * @return true if access is allowed because the request limit is not reached yet
   */
  boolean isAccessOkay(String ipAddress) {
    while (requestEvents.size() > API_REQUEST_QUEUE_SIZE) {
      requestEvents.remove(0);
    }
    requestEvents.add(new RequestEvent(ipAddress, new Date()));
    return !limitReached(ipAddress);
  }
  
  private boolean limitReached(String ipAddress) {
    int requestsByIp = 0;
    // all requests before this date are considered old:
    Date thresholdDate = new Date(System.currentTimeMillis() - requestLimitPeriodInSeconds * 1000);
    for (RequestEvent requestEvent : requestEvents) {
      if (requestEvent.ip.equals(ipAddress) && requestEvent.date.after(thresholdDate)) {
        requestsByIp++;
        if (requestsByIp > requestLimit) {
          return true;
        }
      }
    }
    return false;
  }
  
  static class RequestEvent {

    private final String ip;
    private final Date date;

    RequestEvent(String ip, Date date) {
      this.ip = ip;
      this.date = new Date(date.getTime());
    }
  }

}
