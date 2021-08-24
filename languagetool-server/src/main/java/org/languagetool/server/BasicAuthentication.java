/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Fabian Richter
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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class BasicAuthentication {
  private final String user;
  private final String password;

  public BasicAuthentication(String authHeader) {
    if (!authHeader.startsWith("Basic ")) {
      throw new AuthException("Expected Basic Authentication");
    }
    String authEncoded = authHeader.substring("Basic ".length());
    Charset cs = StandardCharsets.UTF_8;
    ByteBuffer authDecodedBytes = ByteBuffer.wrap(Base64.getDecoder().decode(authEncoded.getBytes(cs)));
    String authDecoded = cs.decode(authDecodedBytes).toString();
    String[] authParts = authDecoded.split(":", 2);
    if (authParts.length != 2 || authParts[0].trim().isEmpty() || authParts[1].trim().isEmpty()) {
      throw new AuthException("Expected Basic Authentication");
    }
    user = authParts[0];
    password = authParts[1];
  }

  public String getUser() {
    return user;
  }

  public String getPassword() {
    return password;
  }
}
