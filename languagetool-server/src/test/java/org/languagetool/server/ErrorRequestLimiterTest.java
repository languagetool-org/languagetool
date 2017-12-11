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

import org.junit.Test;

import static org.junit.Assert.*;

public class ErrorRequestLimiterTest {
  
  @Test
  public void testErrorLimiter() throws InterruptedException {
    ErrorRequestLimiter limiter = new ErrorRequestLimiter(3, 1);
    String ip1 = "192.168.0.1";
    String ip2 = "192.168.0.2";
    assertTrue(limiter.wouldAccessBeOkay(ip1));
    assertTrue(limiter.wouldAccessBeOkay(ip2));
    limiter.logAccess(ip1);
    limiter.logAccess(ip1);
    limiter.logAccess(ip1);
    limiter.logAccess(ip1);
    assertFalse(limiter.wouldAccessBeOkay(ip1));
    assertTrue(limiter.wouldAccessBeOkay(ip2));
    Thread.sleep(1050);
    assertTrue(limiter.wouldAccessBeOkay(ip1));
  }

}