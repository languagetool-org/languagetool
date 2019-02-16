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

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.tools.StringTools;

import java.io.*;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @since 4.0
 */
class UserLimits {

  private int maxTextLength;
  private long maxCheckTimeMillis;
  private Long premiumUid;

  private static final LoadingCache<Account, String> cache = CacheBuilder.newBuilder()
          .expireAfterWrite(15, TimeUnit.MINUTES)
          .build(new CacheLoader<Account, String>() {
            @Override
            public String load(@NotNull Account account) throws IOException {
              return getTokenFromServer(account.username, account.password);
            }
          });

  static UserLimits getDefaultLimits(HTTPServerConfig config) {
    return new UserLimits(config.maxTextLength, config.maxCheckTimeMillis, null);
  }
  
  /**
   * Get limits from the JWT key itself, no database access needed.
   */
  static UserLimits getLimitsFromToken(HTTPServerConfig config, String jwtToken) {
    Objects.requireNonNull(jwtToken);
    try {
      String secretKey = config.getSecretTokenKey();
      if (secretKey == null) {
        throw new RuntimeException("You specified a 'token' parameter but this server doesn't accept tokens");
      }
      Algorithm algorithm = Algorithm.HMAC256(secretKey);
      DecodedJWT decodedToken;
      try {
        JWT.require(algorithm).build().verify(jwtToken);
        decodedToken = JWT.decode(jwtToken);
      } catch (JWTDecodeException e) {
        throw new AuthException("Could not decode token '" + jwtToken + "'", e);
      }
      Claim maxTextLengthClaim = decodedToken.getClaim("maxTextLength");
      Claim premiumClaim = decodedToken.getClaim("premium");
      boolean hasPremium = !premiumClaim.isNull() && premiumClaim.asBoolean();
      Claim uidClaim = decodedToken.getClaim("uid");
      long uid = uidClaim.isNull() ? -1 : uidClaim.asLong();
      return new UserLimits(
              maxTextLengthClaim.isNull() ? config.maxTextLength : maxTextLengthClaim.asInt(),
              config.maxCheckTimeMillis,
              hasPremium ? uid : null);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Get limits from the api key itself, database access is needed.
   */
  public static UserLimits getLimitsByApiKey(HTTPServerConfig config, String username, String apiKey) {
    DatabaseAccess db = DatabaseAccess.getInstance();
    Long id = db.getUserId(username, apiKey);
    return new UserLimits(config.maxTextLengthWithApiKey, config.maxCheckTimeWithApiKeyMillis, id);
  }

  /**
   * Special case that checks user on languagetoolplus.com.
   */
  static UserLimits getLimitsFromUserAccount(HTTPServerConfig config, String username, String password) {
    Objects.requireNonNull(username);
    Objects.requireNonNull(password);
    String token = cache.getUnchecked(new Account(username, password));
    return getLimitsFromToken(config, token);
  }

  @NotNull
  private static String getTokenFromServer(String username, String password) {
    String url = "https://languagetoolplus.com/token";
    try {
      Map<String,Object> params = new LinkedHashMap<>();
      params.put("username", username);
      params.put("password", password);
      StringBuilder postData = new StringBuilder();
      for (Map.Entry<String,Object> param : params.entrySet()) {
        if (postData.length() != 0) {
          postData.append('&');
        }
        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"))
                .append('=')
                .append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
      }
      byte[] postDataBytes = postData.toString().getBytes("UTF-8");
      HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
      try {
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);
        return StringTools.streamToString(conn.getInputStream(), "UTF-8");
      } catch (IOException e) {
        if (conn.getResponseCode() == 403) {
          throw new AuthException("Could not get token for user '" + username + "' from " + url + ", invalid username or password (code: 403)", e);
        } else {
          throw new RuntimeException("Could not get token for user '" + username + "' from " + url, e);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not get token for user '" + username + "' from " + url, e);
    }
  }

  private UserLimits(int maxTextLength, long maxCheckTimeMillis, Long premiumUid) {
    this.maxTextLength = maxTextLength;
    this.maxCheckTimeMillis = maxCheckTimeMillis;
    this.premiumUid = premiumUid;
  }

  int getMaxTextLength() {
    return maxTextLength;
  }

  long getMaxCheckTimeMillis() {
   return maxCheckTimeMillis;
  }

  @Nullable
  Long getPremiumUid() {
    return premiumUid;
  }

  @Override
  public String toString() {
    return "premiumUid=" + premiumUid + ", maxTextLength=" + maxTextLength +
            ", maxCheckTimeMillis=" + maxCheckTimeMillis;
  }

  static class Account {

    private String username;
    private String password;

    Account(String username, String password) {
      this.username = Objects.requireNonNull(username);
      this.password = Objects.requireNonNull(password);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Account account = (Account) o;
      return Objects.equals(username, account.username) && Objects.equals(password, account.password);
    }

    @Override
    public int hashCode() {
      return Objects.hash(username, password);
    }
  }

}
