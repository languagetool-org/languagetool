/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2025 Stefan Viol (https://stevio.de)
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

import com.sun.net.httpserver.Headers;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class JwtTest {

  private HTTPServerConfig httpServerConfig = new HTTPServerConfig(8080);

  @Test
  public void getLimitsWithJwtTokenTest() {
    UserLimits userLimits = UserLimits.getLimitsWithJwtToken(httpServerConfig, "", "", "");
    checkDefaults(userLimits, JwtContent.NONE);
  }

  @Test
  public void getUserLimitsTest() {

    Map<String, String> params = new HashMap<>();
    UserLimits userLimits = ServerTools.getUserLimits(params, httpServerConfig, null);
    checkDefaults(userLimits, null);

    Headers headers = new Headers();
    headers.add("Authorization", "Bearer: kdsajgtfoi43hjrt9i342htfg0eqhj0-49jrtfg9o0jnm32-0er34jghg908hn");

    UserLimits withAuthHeader = ServerTools.getUserLimits(params, httpServerConfig, ServerTools.getAuthHeader(headers));
    checkDefaults(withAuthHeader, JwtContent.NONE);

    params.put("username", "user");
    params.put("tokenV2", "0815-token");

    UserLimits withAuthHeaderAndUser = ServerTools.getUserLimits(params, httpServerConfig, ServerTools.getAuthHeader(headers));
    checkDefaults(withAuthHeaderAndUser, JwtContent.NONE);
  }

  private void checkDefaults(UserLimits userLimits, JwtContent expectedJwtContent) {
    assertEquals(Integer.MAX_VALUE, userLimits.getMaxTextLength());
    assertEquals(-1, userLimits.getMaxCheckTimeMillis());
    assertFalse(userLimits.hasPremium());
    assertNull(userLimits.getDictCacheSize());
    assertNull(userLimits.getPremiumUid());
    assertFalse(userLimits.getSkipLimits());
    assertNull(userLimits.getRequestsPerDay());
    assertEquals(LimitEnforcementMode.DISABLED, userLimits.getLimitEnforcementMode());
    assertEquals(expectedJwtContent, userLimits.getJwtContent());
  }
}
