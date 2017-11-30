/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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

/**
 * Limit the maximum number of request per IP address for a given time range.
 */
class ErrorRequestLimiter extends RequestLimiter {

  /**
   * @param requestLimit the maximum number of request per <tt>requestLimitPeriodInSeconds</tt>
   * @param requestLimitPeriodInSeconds the time period over which requests are considered, in seconds
   */
  ErrorRequestLimiter(int requestLimit, int requestLimitPeriodInSeconds) {
    super(requestLimit, 0, requestLimitPeriodInSeconds);
  }

  /**
   * @param ipAddress the client's IP address
   * @return true if access is allowed because the request limit is not reached yet
   */
  boolean wouldAccessBeOkay(String ipAddress) {
    try {
      checkLimit(ipAddress);
      return true;
    } catch (TooManyRequestsException e) {
      return false;
    }
  }
  
  /**
   * @param ipAddress the client's IP address
   */
  void logAccess(String ipAddress) {
    while (requestEvents.size() > REQUEST_QUEUE_SIZE) {
      requestEvents.remove(0);
    }
    requestEvents.add(new RequestEvent(ipAddress, new Date(), 0));
  }
  
}
