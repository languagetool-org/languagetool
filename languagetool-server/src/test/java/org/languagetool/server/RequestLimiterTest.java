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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RequestLimiterTest {
  
  @Test
  public void testIsAccessOkay() throws Exception {
    RequestLimiter limiter = new RequestLimiter(3, 0, 2);
    String firstIp = "192.168.10.1";
    String secondIp = "192.168.10.2";
    Map<String, String> params = new HashMap<>();
    assertTrue(limiter.isAccessOkay(firstIp, params));
    assertTrue(limiter.isAccessOkay(firstIp, params));
    assertTrue(limiter.isAccessOkay(firstIp, params));
    assertFalse(limiter.isAccessOkay(firstIp, params));
    assertTrue(limiter.isAccessOkay(secondIp, params));
    Thread.sleep(2500);
    assertTrue(limiter.isAccessOkay(firstIp, params));
    assertTrue(limiter.isAccessOkay(secondIp, params));
    assertTrue(limiter.isAccessOkay(secondIp, params));
    assertTrue(limiter.isAccessOkay(secondIp, params));
    assertFalse(limiter.isAccessOkay(secondIp, params));
  }
  
  @Test
  public void testIsAccessOkayWithByteLimit() throws Exception {
    RequestLimiter limiter = new RequestLimiter(10, 35, 2);
    String firstIp = "192.168.10.1";
    String secondIp = "192.168.10.2";
    Map<String, String> params = new HashMap<>();
    params.putIfAbsent("text", "0123456789");
    assertTrue(limiter.isAccessOkay(firstIp, params));  // 10 bytes
    assertTrue(limiter.isAccessOkay(firstIp, params));  // 20 bytes
    assertTrue(limiter.isAccessOkay(firstIp, params));  // 30 bytes
    assertFalse(limiter.isAccessOkay(firstIp, params));  // 40 bytes!
    assertTrue(limiter.isAccessOkay(secondIp, params));
    Thread.sleep(2500);
    assertTrue(limiter.isAccessOkay(firstIp, params));
    assertTrue(limiter.isAccessOkay(secondIp, params));
  }
  
}
