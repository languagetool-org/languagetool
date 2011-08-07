/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.server;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A small embedded HTTP server that checks text. Returns XML, prints debugging
 * to stdout/stderr. Note that by default the server only accepts connections from 
 * localhost for security reasons.
 * 
 * @author Daniel Naber
 * @modified by Ankit
 */
public class HTTPServer {

  /** The default port on which the server is running (8081). */
  public static final int DEFAULT_PORT = 8081;

  private static final Set<String> DEFAULT_ALLOWED_IPS= new HashSet<String>(Arrays.asList(
          "0:0:0:0:0:0:0:1",     // Suse Linux IPv6 stuff
          "0:0:0:0:0:0:0:1%0",   // some(?) Mac OS X
          "127.0.0.1"
  ));

  private final int port;

  private HttpServer server;
  
  /**
   * Prepare a server on the given port - use run() to start it. Accepts
   * connections from localhost only.
   */
  public HTTPServer() {
    this(DEFAULT_PORT);
  }

  /**
   * Prepare a server on the given port - use run() to start it. Accepts
   * connections from localhost only.
   */
  public HTTPServer(int port) {
    this(port, false);
  }

  /**
   * Prepare a server on the given port - use run() to start it. Accepts
   * connections from localhost only.
   * @param verbose if true, the text to be checked will be displayed in case of exceptions
   */
  public HTTPServer(int port, boolean verbose) {
    this(port, verbose, DEFAULT_ALLOWED_IPS);
  }
  
  /**
   * Prepare a server on the given port - use run() to start it.
   * @param verbose if true, the text to be checked will be displayed in case of exceptions
   * @param allowedIps the IP addresses from which connections are allowed
   * @throws PortBindingException if we cannot bind to the given port, probably because something else is running there
   */
  public HTTPServer(int port, boolean verbose, Set<String> allowedIps) {
    this.port = port;
    try {
      server = HttpServer.create(new InetSocketAddress(port), 0);
      server.createContext("/", new LanguageToolHttpHandler(verbose, allowedIps));
    } catch (Exception e) {
      throw new PortBindingException(
          "LanguageTool server could not be started on port " + port
          + ", maybe something else is running on that port already?", e);
    }
  }

  /**
   * Start the server.
   */
  public void run() {
    System.out.println("Starting server on port " + port + "...");
    server.start();
    System.out.println("Server started");
  }

  /**
   * Stop the server.
   */
  public void stop() {
    if (server != null) {
      System.out.println("Stopping server");
      server.stop(0);
      System.out.println("Server stopped");
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length > 3) {
      System.out.println("Usage: " + HTTPServer.class.getSimpleName() + " [-p|--port port]");
      System.exit(1);
    }
    boolean verbose = false;
    int port = DEFAULT_PORT;
    for (int i = 0; i < args.length; i++) {
      if ("-p".equals(args[i]) || "--port".equals(args[i])) {
        port = Integer.parseInt(args[++i]);
      } else if ("-v".equals(args[i]) || "--verbose".equals(args[i])) {
        verbose = true;
      }
    }
    try {
      final HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
      server.createContext("/", new LanguageToolHttpHandler(verbose, DEFAULT_ALLOWED_IPS));
      server.start();
      System.out.println("Started LanguageTool HTTP server on port " + port + ".");
    } catch (Exception e) {
      throw new RuntimeException("Could not start LanguageTool HTTP server on port " + port, e);
    }
  }
  
}

