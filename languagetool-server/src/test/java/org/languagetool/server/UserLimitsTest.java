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

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class UserLimitsTest {

  @Test
  public void testDefaultLimits() throws Exception {
    HTTPServerConfig config = new HTTPServerConfig();
    UserLimits defLimits = UserLimits.getDefaultLimits(config);
    assertThat(defLimits.getMaxTextLength(), is(Integer.MAX_VALUE));
    assertThat(defLimits.getMaxCheckTimeMillis(), is(-1L));
    assertNull(defLimits.getPremiumUid());
  }
  
  @Test
  public void testLimitsFromToken1() throws Exception {
    HTTPServerConfig config = new HTTPServerConfig();
    config.setSecretTokenKey("foobar");
    // See TextCheckerText.makeToken():
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vZm9vYmFyIiwiaWF0IjoxNTA3MjM5ODgxLCJtYXhUZXh0TGVuZ3RoIjozMH0.nqmxKqIBoL7k1OLbfMm9lVqR5XvPIV0hERzWvM-otq8";
    UserLimits limits = UserLimits.getLimitsFromToken(config, token);
    assertThat(limits.getMaxTextLength(), is(30));
    assertThat(limits.getMaxCheckTimeMillis(), is(-1L));
    assertNull(limits.getPremiumUid());
  }
  
  @Test
  public void testLimitsFromToken2() throws Exception {
    HTTPServerConfig config = new HTTPServerConfig();
    config.setSecretTokenKey("foobar");
    // See TextCheckerText.makeToken():
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjEyMzQsInByZW1pdW0iOnRydWUsImlzcyI6Imh0dHA6Ly9mb29iYXIiLCJpYXQiOjE1MDcyNDAyMzYsIm1heFRleHRMZW5ndGgiOjUwfQ.MCEuhvTiuci2d35NhTqV3mRZ0zaCKMHWU2k-tipviMY";
    UserLimits limits = UserLimits.getLimitsFromToken(config, token);
    assertThat(limits.getMaxTextLength(), is(50));
    assertThat(limits.getMaxCheckTimeMillis(), is(-1L));
    assertThat(limits.getPremiumUid(), is(1234L));
  }
  
  @Test
  @Ignore("would require network access to languagetoolplus.com and the server's secret key")
  public void testGetLimitsFromUserAccount() throws Exception {
    HTTPServerConfig config = new HTTPServerConfig();
    config.setSecretTokenKey("fixme");
    UserLimits limits = UserLimits.getLimitsFromUserAccount(config, "fixme", "fixme");
    assertThat(limits.getMaxTextLength(), is(25000));
    assertThat(limits.getMaxCheckTimeMillis(), is(-1L));
    assertThat(limits.getPremiumUid(), is(1L));
  }
  
}