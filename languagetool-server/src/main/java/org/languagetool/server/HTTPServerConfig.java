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

/**
 * @since 2.0
 */
public class HTTPServerConfig {

  public static final String DEFAULT_HOST = "localhost";

  /** The default port on which the server is running (8081). */
  public static final int DEFAULT_PORT = 8081;

  protected boolean verbose = false;
  protected boolean publicAccess = false;
  protected int port = DEFAULT_PORT;

  public HTTPServerConfig() {
    this.port = DEFAULT_PORT;
    this.verbose = false;
    this.publicAccess = false;
  }

  /**
   * @param serverPort the port to bind to
   * @param verbose when set to <tt>true</tt>, the input text will be logged in case there is an exception
   */
  public HTTPServerConfig(int serverPort, boolean verbose) {
    this.port = serverPort;
    this.verbose = verbose;
    this.publicAccess = false;
  }

  /**
   * Parse command line options.
   */
  HTTPServerConfig(String[] args) {
    for (int i = 0; i < args.length; i++) {
      if ("-p".equals(args[i]) || "--port".equals(args[i])) {
        port = Integer.parseInt(args[++i]);
      } else if ("-v".equals(args[i]) || "--verbose".equals(args[i])) {
        verbose = true;
      } else if ("--public".equals(args[i])) {
        publicAccess = true;
      }
    }
  }

  /*
   * @param verbose if true, the text to be checked will be displayed in case of exceptions
   */
  public boolean isVerbose() {
    return verbose;
  }

  public boolean isPublicAccess() {
    return publicAccess;
  }

  public int getPort() {
    return port;
  }

}
