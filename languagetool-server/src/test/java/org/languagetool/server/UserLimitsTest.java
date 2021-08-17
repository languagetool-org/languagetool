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

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.Premium;

import java.util.Objects;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class UserLimitsTest {

  protected HTTPServerConfig config;

  @Before
  public void setUp() {
    config = new HTTPServerConfig(HTTPTools.getDefaultPort());
    UserDictTest.configureTestDatabase(config);
    DatabaseAccess.init(config);
    DatabaseAccess.getInstance().deleteTestTables();
    DatabaseAccess.getInstance().createAndFillTestTables();
  }

  @After
  public void tearDown() {
    DatabaseAccess.getInstance().deleteTestTables();
    DatabaseAccess.getInstance().shutdownCompact();
    DatabaseAccess.reset();
  }

  @Test
  public void testDefaultLimits() {
    HTTPServerConfig config = new HTTPServerConfig();
    UserLimits defLimits = UserLimits.getDefaultLimits(config);
    assertThat(defLimits.getMaxTextLength(), is(Integer.MAX_VALUE));
    assertThat(defLimits.getMaxCheckTimeMillis(), is(-1L));
    assertNull(defLimits.getPremiumUid());
    assertNull(defLimits.getDictCacheSize());
  }

  @Test
  public void testLimitsFromToken1() {
    HTTPServerConfig config = new HTTPServerConfig();
    config.setSecretTokenKey("foobar");
    // See TextCheckerText.makeToken():
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vZm9vYmFyIiwiaWF0IjoxNTA3MjM5ODgxLCJtYXhUZXh0TGVuZ3RoIjozMH0.nqmxKqIBoL7k1OLbfMm9lVqR5XvPIV0hERzWvM-otq8";
    UserLimits limits = UserLimits.getLimitsFromToken(config, token);
    assertThat(limits.getMaxTextLength(), is(30));
    assertThat(limits.getMaxCheckTimeMillis(), is(-1L));
    assertNull(limits.getPremiumUid());
    assertNull(limits.getDictCacheSize());
  }
  
  @Test
  public void testLimitsFromToken2() {
    HTTPServerConfig config = new HTTPServerConfig();
    config.setSecretTokenKey("foobar");
    // See TextCheckerText.makeToken():
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjEyMzQsInByZW1pdW0iOnRydWUsImlzcyI6Imh0dHA6Ly9mb29iYXIiLCJpYXQiOjE1MDcyNDAyMzYsIm1heFRleHRMZW5ndGgiOjUwfQ.MCEuhvTiuci2d35NhTqV3mRZ0zaCKMHWU2k-tipviMY";
    UserLimits limits = UserLimits.getLimitsFromToken(config, token);
    assertThat(limits.getMaxTextLength(), is(50));
    assertThat(limits.getMaxCheckTimeMillis(), is(-1L));
    assertThat(limits.getPremiumUid(), is(1234L));
    assertNull(limits.getDictCacheSize());
  }
  
  @Test
  public void testLimitsFromToken3() {
    HTTPServerConfig config = new HTTPServerConfig();
    config.setSecretTokenKey("foobar");
    // See TextCheckerText.makeToken():
    String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1aWQiOjQyLCJwcmVtaXVtIjp0cnVlLCJpc3MiOiJodHRwOi8vZm9vYmFyIiwiZGljdENhY2hlU2l6ZSI6MTAwMDAsImlhdCI6MTUzNzM1MDAwOSwibWF4VGV4dExlbmd0aCI6NTAwMH0.utunN5ZXGWR6kk7GkTeDDUkUvxiJivsYc8HEOqyTk28";
    UserLimits limits = UserLimits.getLimitsFromToken(config, token);
    assertThat(limits.getMaxTextLength(), is(5000));
    assertThat(limits.getMaxCheckTimeMillis(), is(-1L));
    assertThat(limits.getPremiumUid(), is(42L));
    assertThat(limits.getDictCacheSize(), is(10000L));
  }

  @Test
  @Ignore
  public void generatePasswordHashForTest() {
    System.out.println(BCrypt.with(BCrypt.Version.VERSION_2Y).hashToString(12, "password".toCharArray()));
  }


  @Test
  public void testGetLimitsFromUserAccount() {
    if (!Premium.isPremiumVersion()) {
      return;
    }
    config.setMaxTextLengthAnonymous(100);
    config.setMaxTextLengthLoggedIn(1000);
    config.setMaxTextLengthPremium(10000);
    try {
      UserLimits.getLimitsFromUserAccount(config, UserDictTest.USERNAME3, UserDictTest.PASSWORD3 + "foobar");
      fail("Expected AuthException");
    } catch(AuthException ignored) {
      try {
        UserLimits.getLimitsFromUserAccount(config, "foobar" + UserDictTest.USERNAME3, UserDictTest.PASSWORD3);
        fail("Expected AuthException");
      } catch(AuthException ignored2) {
        UserLimits limits = UserLimits.getLimitsFromUserAccount(config, UserDictTest.USERNAME3, UserDictTest.PASSWORD3);
        assertThat(limits.getMaxTextLength(), is(config.getMaxTextLengthLoggedIn()));
        assertThat(limits.getPremiumUid(), is(UserDictTest.USER_ID3));
        assertThat(limits.hasPremium(), is(false));
      }
    }
  }
  
  @Test
  public void testGetLimitsFromUserAccountViaApiKey() {
    if (!Premium.isPremiumVersion()) {
      return;
    }
    config.setMaxTextLengthAnonymous(100);
    config.setMaxTextLengthLoggedIn(1000);
    config.setMaxTextLengthPremium(10000);
    try {
      UserLimits.getLimitsByApiKey(config, UserDictTest.USERNAME1, UserDictTest.API_KEY1 + "foobar");
      fail("Expected AuthException");
    } catch(AuthException ignored) {
      UserLimits limits = UserLimits.getLimitsByApiKey(config, UserDictTest.USERNAME1, UserDictTest.API_KEY1);
      assertThat(limits.getMaxTextLength(), is(config.getMaxTextLengthPremium()));
      assertThat(limits.getPremiumUid(), is(UserDictTest.USER_ID1));
      assertThat(limits.hasPremium(), is(true));
    }
  }

  @Test
  public void testGetLimitsFromUserAccountViaAddonToken() {
    if (!Premium.isPremiumVersion()) {
      return;
    }
    config.setMaxTextLengthAnonymous(100);
    config.setMaxTextLengthLoggedIn(1000);
    config.setMaxTextLengthPremium(10000);
    try {
      UserLimits.getLimitsByAddonToken(config, UserDictTest.USERNAME1, UserDictTest.TOKEN_V2_1 + "foobar");
      fail("Expected AuthException");
    } catch(AuthException ignored) {
      UserLimits limits = UserLimits.getLimitsByAddonToken(config, UserDictTest.USERNAME1, UserDictTest.TOKEN_V2_1);
      assertThat(limits.getMaxTextLength(), is(config.getMaxTextLengthPremium()));
      assertThat(limits.getPremiumUid(), is(UserDictTest.USER_ID1));
      assertThat(limits.hasPremium(), is(true));
    }
  }

  @Test
  public void testUserGroup() {
    if (!Premium.isPremiumVersion()) {
      return;
    }
    UserLimits limits1 = UserLimits.getLimitsByAddonToken(config, UserDictTest.USERNAME1, UserDictTest.TOKEN_V2_1);
    assertEquals(Objects.requireNonNull(limits1.getAccount()).getUserGroup(),
      UserDictTest.USER_GROUP_1);
    UserLimits limits2 = UserLimits.getLimitsByApiKey(config, UserDictTest.USERNAME2, UserDictTest.API_KEY2);
    assertEquals(Objects.requireNonNull(limits2.getAccount()).getUserGroup(), UserDictTest.USER_GROUP_1);
    UserLimits limits3 = UserLimits.getLimitsFromUserAccount(config, UserDictTest.USERNAME3, UserDictTest.PASSWORD3);
    assertNull(Objects.requireNonNull(limits3.getAccount()).getUserGroup());
  }

}
