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
import org.languagetool.tools.StringTools;

import java.io.*;
import java.net.*;
import java.util.LinkedHashMap;
import java.util.Map;
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
  
  static UserLimits getLimitsFromUserAccount(HTTPServerConfig config, String username, String password) {
    Objects.requireNonNull(username);
    Objects.requireNonNull(password);
    try {
      URL url = new URL("https://languagetoolplus.com/token");
      Map<String,Object> params = new LinkedHashMap<>();
      params.put("username", username);
      params.put("password", password);
      StringBuilder postData = new StringBuilder();
      for (Map.Entry<String,Object> param : params.entrySet()) {
        if (postData.length() != 0) postData.append('&');
        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"))
                .append('=')
                .append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
      }
      byte[] postDataBytes = postData.toString().getBytes("UTF-8");
      HttpURLConnection conn = (HttpURLConnection)url.openConnection();
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.getOutputStream().write(postDataBytes);
      String token = StringTools.streamToString(conn.getInputStream(), "UTF-8");
      return getLimitsFromToken(config, token);
    } catch (java.io.IOException e) {
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

  @Override
  public String toString() {
    return "maxTextLength=" + maxTextLength +
            ", maxCheckTimeMillis=" + maxCheckTimeMillis;
  }
  
}
