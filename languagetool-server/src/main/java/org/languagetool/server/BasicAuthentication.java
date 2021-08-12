/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2019 Fabian Richter
 *  * All rights reserved - not part of the Open Source edition
 *
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
