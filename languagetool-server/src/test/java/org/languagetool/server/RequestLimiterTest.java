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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RequestLimiterTest {
  
  @Test
  public void testIsAccessOkay() throws Exception {
    RequestLimiter limiter = new RequestLimiter(3, 2);
    String firstIp = "192.168.10.1";
    String secondIp = "192.168.10.2";
    assertTrue(limiter.isAccessOkay(firstIp));
    assertTrue(limiter.isAccessOkay(firstIp));
    assertTrue(limiter.isAccessOkay(firstIp));
    assertFalse(limiter.isAccessOkay(firstIp));
    assertTrue(limiter.isAccessOkay(secondIp));
    Thread.sleep(2500);
    assertTrue(limiter.isAccessOkay(firstIp));
    assertTrue(limiter.isAccessOkay(secondIp));
    assertTrue(limiter.isAccessOkay(secondIp));
    assertTrue(limiter.isAccessOkay(secondIp));
    assertFalse(limiter.isAccessOkay(secondIp));
  }
  
}
