/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia.atom;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @since 2.4
 */
class DatabaseConfig {

  private final String url;
  private final String user;
  private final String password;

  DatabaseConfig(String propFile) throws IOException {
    Properties properties = new Properties();
    try (FileInputStream fis = new FileInputStream(propFile)) {
      properties.load(fis);
    }
    this.url = getRequiredProperty(properties, "dbUrl");
    this.user = getRequiredProperty(properties, "dbUser");
    this.password = getRequiredProperty(properties, "dbPassword");
  }

  DatabaseConfig(String dbUrl, String dbUser, String dbPassword) {
    this.url = dbUrl;
    this.user = dbUser;
    this.password = dbPassword;
  }

  private String getRequiredProperty(Properties properties, String propName) {
    String value = properties.getProperty(propName);
    if (value == null) {
      throw new RuntimeException("Property key '" + propName + "' not found");
    }
    return value;
  }

  String getUrl() {
    return url;
  }

  String getUser() {
    return user;
  }

  String getPassword() {
    return password;
  }

}
