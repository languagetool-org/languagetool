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
import com.auth0.jwt.interfaces.Claim;

import java.io.UnsupportedEncodingException;
import java.util.Objects;

/**
 * @since 4.0
 */
class UserLimits {

  private int maxTextLength;
  private long maxCheckTimeMillis;
  
  static UserLimits getDefaultLimits(HTTPServerConfig config) {
    return new UserLimits(config.maxTextLength, config.maxCheckTimeMillis);
  }
  
  static UserLimits getLimitsFromToken(HTTPServerConfig config, String token) {
    Objects.requireNonNull(token);
    try {
      String secretKey = config.getSecretTokenKey();
      if (secretKey == null) {
        throw new RuntimeException("You specified a 'token' parameter but this server doesn't accept tokens");
      }
      Algorithm algorithm = Algorithm.HMAC256(secretKey);
      JWT.require(algorithm).build().verify(token);
      Claim maxTextLength = JWT.decode(token).getClaim("maxTextLength");
      if (maxTextLength.isNull()) {
        return new UserLimits(config.maxTextLength, config.maxCheckTimeMillis);
      } else {
        return new UserLimits(maxTextLength.asInt(), config.maxCheckTimeMillis);
      }
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
  
  UserLimits(int maxTextLength, long maxCheckTimeMillis) {
    this.maxTextLength = maxTextLength;
    this.maxCheckTimeMillis = maxCheckTimeMillis;
  }

  int getMaxTextLength() {
    return maxTextLength;
  }

  long getMaxCheckTimeMillis() {
    return maxCheckTimeMillis;
  }
}
