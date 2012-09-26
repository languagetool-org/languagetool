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
package org.languagetool.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sun.net.httpserver.HttpServer;

/**
 * A small embedded HTTP server that checks text. Returns XML, prints debugging
 * to stdout/stderr. Note that by default the server only accepts connections from 
 * localhost for security reasons.
 * 
 * @author Daniel Naber
 * @author Ankit
 */
public class HTTPServer {

  public static final String DEFAULT_HOST = "localhost";
  /** The default port on which the server is running (8081). */
  public static final int DEFAULT_PORT = 8081;

  private static final Set<String> DEFAULT_ALLOWED_IPS = new HashSet<String>(Arrays.asList(
          "0:0:0:0:0:0:0:1",     // Suse Linux IPv6 stuff
          "0:0:0:0:0:0:0:1%0",   // some(?) Mac OS X
          "127.0.0.1"
  ));

  private final int port;
  private final HttpServer server;
  
  /**
   * Prepare a server on the given port - use run() to start it. Accepts
   * connections from localhost only.
   */
  public HTTPServer() {
    this(DEFAULT_PORT);
  }

  /**
   * Prepare a server on localhost on the given port - use run() to start it. Accepts
   * connections from localhost only.
   * @throws PortBindingException if we cannot bind to the given port, e.g. because something else is running there
   */
  public HTTPServer(int port) {
    this(port, false);
  }

  /**
   * Prepare a server on localhost on the given port - use run() to start it. Accepts
   * connections from localhost only.
   * @param verbose if true, the text to be checked will be displayed in case of exceptions
   * @throws PortBindingException if we cannot bind to the given port, e.g. because something else is running there
   */
  public HTTPServer(int port, boolean verbose) {
    this(port, verbose, false, DEFAULT_ALLOWED_IPS);
  }

  
  /**
   * Prepare a server on localhost on the given port - use run() to start it. Accepts
   * connections from localhost only.
   * @param verbose if true, the text to be checked will be displayed in case of exceptions
   * @param runInternally if true, then the server was started from the GUI.
   * @throws PortBindingException if we cannot bind to the given port, e.g. because something else is running there
   */
  public HTTPServer(int port, boolean verbose, boolean runInternally) {	
    this(port, verbose, runInternally, DEFAULT_HOST, DEFAULT_ALLOWED_IPS);    
  }

  
  /**
   * Prepare a server on localhost on the given port - use run() to start it. The server will bind to localhost.
   * @param verbose if true, the text to be checked will be displayed in case of exceptions
   * @param runInternally if true, then the server was started from the GUI.
   * @param allowedIps the IP addresses from which connections are allowed or <code>null</code> to allow any host
   * @throws PortBindingException if we cannot bind to the given port, e.g. because something else is running there
   */
  public HTTPServer(int port, boolean verbose, boolean runInternally, Set<String> allowedIps) {
    this(port, verbose, runInternally, DEFAULT_HOST, allowedIps);
  }
  
  /**
   * Prepare a server on the given host and port - use run() to start it.
   * @param verbose if true, the text to be checked will be displayed in case of exceptions
   * @param runInternally if true, then the server was started from the GUI.
   * @param host the host to bind to, e.g. <code>"localhost"</code> or <code>null</code> to bind to any host
   * @param allowedIps the IP addresses from which connections are allowed or <code>null</code> to allow any host
   * @throws PortBindingException if we cannot bind to the given port, e.g. because something else is running there
   * @since 1.7
   */
  public HTTPServer(int port, boolean verbose, boolean runInternally, String host, Set<String> allowedIps) {
    this.port = port;
    try {
      if (host == null) {
        server = HttpServer.create(new InetSocketAddress(port), 0);
      } else {
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
      }
      server.createContext("/", new LanguageToolHttpHandler(verbose, allowedIps, runInternally));
    } catch (Exception e) {
      throw new PortBindingException(
          "LanguageTool server could not be started on host '" + host + "', port " + port
          + " - maybe something else is running on that port already?", e);
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
    if (args.length > 3 || usageRequested(args)) {
      System.out.println("Usage: " + HTTPServer.class.getSimpleName() + " [-p|--port port] [--public]");
      System.out.println("  -p, --port  port to bind to, defaults to " + DEFAULT_PORT + " if not specified");
      System.out.println("  --public    allow this server process to be connected from anywhere (not recommended)");
      System.exit(1);
    }
    boolean verbose = false;
    boolean publicAccess = false;
    final boolean runInternal = false;
    int port = DEFAULT_PORT;
    for (int i = 0; i < args.length; i++) {
      if ("-p".equals(args[i]) || "--port".equals(args[i])) {
        port = Integer.parseInt(args[++i]);
      } else if ("-v".equals(args[i]) || "--verbose".equals(args[i])) {
        verbose = true;
      } else if ("--public".equals(args[i])) {
        publicAccess = true;
      }
    }
    try {
      final HTTPServer server;
      if (publicAccess) {
        System.out.println("WARNING: running in public mode, LanguageTool API can be accessed without restrictions!");
        server = new HTTPServer(port, verbose, runInternal, null, null);
      } else {
        server = new HTTPServer(port, verbose, runInternal, DEFAULT_HOST, DEFAULT_ALLOWED_IPS);
      }
      server.run();
    } catch (Exception e) {
      throw new RuntimeException("Could not start LanguageTool HTTP server on " + DEFAULT_HOST + ", port " + port, e);
    }
  }

  private static boolean usageRequested(String[] args) {
    return args.length == 1 && (args[0].equals("-h") || args[0].equals("--help"));
  }

}

