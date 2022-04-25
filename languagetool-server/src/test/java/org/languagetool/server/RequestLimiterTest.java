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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.fail;

public class RequestLimiterTest {

  private final HTTPServerConfig config = new HTTPServerConfig();

  @Test
  public void testIsAccessOkay() throws Exception {
    RequestLimiter limiter = new RequestLimiter(3, 0, 1, 2);
    String firstIp = "192.168.10.1";
    String secondIp = "192.168.10.2";
    Map<String, List<String>> firstHeader = new HashMap<>();
    Map<String, List<String>> secondHeader = new HashMap<>();
    secondHeader.put("User-Agent", Collections.singletonList("Test"));
    Map<String, String> params = new HashMap<>();
    assertOkay(limiter, firstIp, params, firstHeader);
    assertOkay(limiter, firstIp, params, firstHeader);
    assertOkay(limiter, firstIp, params, firstHeader);
    assertException(limiter, firstIp, params, firstHeader);
    assertOkay(limiter, firstIp, params, secondHeader);
    assertOkay(limiter, firstIp, params, secondHeader);
    assertException(limiter, firstIp, params, secondHeader);
    assertOkay(limiter, secondIp, params, firstHeader);
    assertOkay(limiter, secondIp, params, secondHeader);
    Thread.sleep(1050);
    assertOkay(limiter, firstIp, params, firstHeader);
    assertOkay(limiter, secondIp, params, firstHeader);
    assertOkay(limiter, secondIp, params, firstHeader);
    assertOkay(limiter, secondIp, params, firstHeader);
    assertException(limiter, secondIp, params, firstHeader);
  }

  @Test
  public void testIsAccessOkayWithFingerprintDisabled() throws Exception {
    RequestLimiter limiter = new RequestLimiter(3, 0, 1, 0);
    String firstIp = "192.168.10.1";
    String secondIp = "192.168.10.2";
    Map<String, List<String>> firstHeader = new HashMap<>();
    Map<String, List<String>> secondHeader = new HashMap<>();
    secondHeader.put("User-Agent", Collections.singletonList("Test"));
    Map<String, String> params = new HashMap<>();
    assertOkay(limiter, firstIp, params, firstHeader);
    assertOkay(limiter, firstIp, params, firstHeader);
    assertOkay(limiter, firstIp, params, firstHeader);
    assertException(limiter, firstIp, params, firstHeader);
    assertException(limiter, firstIp, params, secondHeader);
    assertOkay(limiter, secondIp, params, firstHeader);
    assertOkay(limiter, secondIp, params, secondHeader);
    Thread.sleep(1050);
    assertOkay(limiter, firstIp, params, firstHeader);
    assertOkay(limiter, secondIp, params, firstHeader);
    assertOkay(limiter, secondIp, params, firstHeader);
    assertOkay(limiter, secondIp, params, firstHeader);
    assertException(limiter, secondIp, params, firstHeader);
  }

  @Test
  public void testIsAccessOkayWithByteLimitNoFingerprint() throws Exception {
    RequestLimiter limiter = new RequestLimiter(10, 35, 1, 0);
    String firstIp = "192.168.10.1";
    String secondIp = "192.168.10.2";
    Map<String, List<String>> firstHeader = new HashMap<>();
    Map<String, List<String>> secondHeader = new HashMap<>();
    secondHeader.put("User-Agent", Collections.singletonList("Test"));
    Map<String, String> params = new HashMap<>();
    params.putIfAbsent("text", "0123456789");
    assertOkay(limiter, firstIp, params, firstHeader);  // 10 bytes
    assertOkay(limiter, firstIp, params, firstHeader);  // 20 bytes
    assertOkay(limiter, firstIp, params, firstHeader);  // 30 bytes
    assertException(limiter, firstIp, params, firstHeader);  // 40 bytes!
    assertException(limiter, firstIp, params, secondHeader);
    assertOkay(limiter, secondIp, params, firstHeader);
    assertOkay(limiter, secondIp, params, secondHeader);
    Thread.sleep(1050);
    assertOkay(limiter, firstIp, params, firstHeader);
    assertOkay(limiter, firstIp, params, secondHeader);
    assertOkay(limiter, secondIp, params, firstHeader);
    assertOkay(limiter, secondIp, params, secondHeader);
  }

  @Test
  public void testIsAccessOkayWithByteLimit() throws Exception {
    RequestLimiter limiter = new RequestLimiter(10, 35, 1, 2);
    String firstIp = "192.168.10.1";
    String secondIp = "192.168.10.2";
    Map<String, List<String>> firstHeader = new HashMap<>();
    Map<String, List<String>> secondHeader = new HashMap<>();
    secondHeader.put("User-Agent", Collections.singletonList("Test"));
    Map<String, String> params = new HashMap<>();
    params.putIfAbsent("text", "0123456789");
    assertOkay(limiter, firstIp, params, firstHeader);  // 10 bytes
    assertOkay(limiter, firstIp, params, firstHeader);  // 20 bytes
    assertOkay(limiter, firstIp, params, firstHeader);  // 30 bytes
    assertException(limiter, firstIp, params, firstHeader);  // 40 bytes!
    assertOkay(limiter, firstIp, params, secondHeader);
    assertOkay(limiter, firstIp, params, secondHeader);
    assertOkay(limiter, firstIp, params, secondHeader);
    assertException(limiter, firstIp, params, secondHeader);  // 80 bytes!
    assertOkay(limiter, secondIp, params, firstHeader);
    assertOkay(limiter, secondIp, params, secondHeader);
    Thread.sleep(1050);
    assertOkay(limiter, firstIp, params, firstHeader);
    assertOkay(limiter, firstIp, params, secondHeader);
    assertOkay(limiter, secondIp, params, firstHeader);
    assertOkay(limiter, secondIp, params, secondHeader);
  }

  @Test
  public void testTextLevelChecksCountLess() {
    RequestLimiter limiter = new RequestLimiter(100, 35, 100, 2);
    String firstIp = "192.168.10.1";
    Map<String, List<String>> firstHeader = new HashMap<>();
    Map<String, String> params = new HashMap<>();
    params.putIfAbsent("text", "0123456789");
    assertOkay(limiter, firstIp, params, firstHeader);  // 10 bytes
    assertOkay(limiter, firstIp, params, firstHeader);  // 20 bytes
    assertOkay(limiter, firstIp, params, firstHeader);  // 30 bytes
    params.put("mode", "textLevelOnly");
    assertOkay(limiter, firstIp, params, firstHeader);  // +10 bytes! but text level only, counts only a tenth of that, so okay (31 bytes)
    params.put("mode", "all");
    assertException(limiter, firstIp, params, firstHeader);  // 41 bytes!
    assertOkayWithSkippingLimits(limiter, firstIp, params, firstHeader);
  }

  private void assertOkay(RequestLimiter limiter, String ip, Map<String, String> params, Map<String, List<String>> header) {
    try {
      limiter.checkAccess(ip, params, header, UserLimits.getDefaultLimits(config));
    } catch (TooManyRequestsException e) {
      fail();
    }
  }

  private void assertOkayWithSkippingLimits(RequestLimiter limiter, String ip, Map<String, String> params, Map<String, List<String>> header) {
    try {
      limiter.checkAccess(ip, params, header, new UserLimits(true));
    } catch (TooManyRequestsException e) {
      fail();
    }
  }

  private void assertException(RequestLimiter limiter, String ip, Map<String, String> params, Map<String, List<String>> header) {
    try {
      limiter.checkAccess(ip, params, header, UserLimits.getDefaultLimits(config));
      fail();
    } catch (TooManyRequestsException ignored) {}
  }
  
}
