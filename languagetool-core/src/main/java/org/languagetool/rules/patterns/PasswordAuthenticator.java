/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import org.jetbrains.annotations.Nullable;
import org.languagetool.tools.StringTools;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/**
 * Authenticator that extracts username and password from URL, e.g.
 * from {@code http://user:password@myhost.org/path}
 * @since 2.7
 */
public class PasswordAuthenticator extends Authenticator {

  @Override
  @Nullable
  protected PasswordAuthentication getPasswordAuthentication() {
    if (getRequestingURL() == null) {
      return null;
    }
    String userInfo = getRequestingURL().getUserInfo();
    if (StringTools.isEmpty(userInfo)) {
      return null;
    }
    String[] parts = userInfo.split(":");
    if (parts.length != 2) {
      throw new RuntimeException("Invalid userInfo format, expected 'user:password': " + userInfo);
    }
    String username = parts[0];
    String password = parts[1];
    return new PasswordAuthentication(username, password.toCharArray());
  }

}
