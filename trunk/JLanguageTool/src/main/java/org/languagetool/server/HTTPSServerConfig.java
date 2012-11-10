/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import java.io.File;

/**
 * @since 2.0
 */
public class HTTPSServerConfig extends HTTPServerConfig {

  private File keystore;
  private String keyStorePassword;

  /**
   * @param keystore a Java keystore file as created with the <tt>keytool</tt> command
   * @param keyStorePassword the password for the keystore
   */
  public HTTPSServerConfig(File keystore, String keyStorePassword) {
    super(DEFAULT_PORT, false);
    this.keystore = keystore;
    this.keyStorePassword = keyStorePassword;
  }

  /**
   * @param serverPort the port to bind to
   * @param verbose when set to <tt>true</tt>, the input text will be logged in case there is an exception
   * @param keystore a Java keystore file as created with the <tt>keytool</tt> command
   * @param keyStorePassword the password for the keystore
   */
  public HTTPSServerConfig(int serverPort, boolean verbose, File keystore, String keyStorePassword) {
    super(serverPort, verbose);
    this.keystore = keystore;
    this.keyStorePassword = keyStorePassword;
  }

  /**
   * Parse command line options.
   */
  HTTPSServerConfig(String[] args) {
    super(args);
    for (int i = 0; i < args.length; i++) {
      if ("--keystore".equals(args[i])) {
        keystore = new File(args[++i]);
      } else if ("--password".equals(args[i])) {
        keyStorePassword = args[++i];
      }
    }
    if (keystore == null) {
      throw new IllegalArgumentException("Parameter --keystore must be set");
    }
    if (keyStorePassword == null) {
      throw new IllegalArgumentException("Parameter --password must be set");
    }
  }

  public File getKeystore() {
    return keystore;
  }

  public String getKeyStorePassword() {
    return keyStorePassword;
  }
}
