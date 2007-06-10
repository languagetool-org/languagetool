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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.prolixtech.jaminid.ContentOracle;
import com.prolixtech.jaminid.Daemon;
import com.prolixtech.jaminid.ProtocolResponseHeader;
import com.prolixtech.jaminid.Request;
import com.prolixtech.jaminid.Response;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * A small embedded HTTP server that checks text. Returns XML, prints
 * debugging to stdout/stderr.
 * 
 * @author Daniel Naber
 */
public class HTTPServer extends ContentOracle {

  /**
   * The default port on which the server is running (8081).
   */
  public static final int DEFAULT_PORT = 8081;
  
  private static final int CONTEXT_SIZE = 40;   // characters

  private Daemon daemon;
  private int port = DEFAULT_PORT;
  private boolean verbose = false;
  
  private static final Set<String> allowedIPs = new HashSet<String>();
  static {
    // accept only requests from localhost.
    // TODO: find a cleaner solution
    allowedIPs.add("/0:0:0:0:0:0:0:1");   // Suse Linux IPv6 stuff
    allowedIPs.add("/127.0.0.1");
  }

  /**
   * Prepare a server - use run() to start it.
   */
  public HTTPServer() {
  }

  /**
   * Prepare a server on the given port - use run() to start it.
   */
  public HTTPServer(int port) {
    this(port, false);
  }

  /**
   * Prepare a server on the given port - use run() to start it.
   * @param verbose if true, the text to check will be displayed in case 
   * of exceptions (default: false)
   */
  public HTTPServer(int port, boolean verbose) {
    this.port = port;
    this.verbose = verbose;
  }

  /**
   * Start the server.
   */
  public void run() {
    System.out.println("Starting server on port " + port + "...");
    daemon = new Daemon(port, this);
    if (daemon.isRunning()) 
      System.out.println("Server started");
    else
      throw new RuntimeException("Server could not be started");
  }

  public String demultiplex(Request connRequest, Response connResponse) {
    long timeStart = System.currentTimeMillis();
    String text = null;
    try {
      if ("".equals(connRequest.getLocation())) {
        connResponse.setStatus(403);
        throw new RuntimeException("Error: Access to " + connRequest.getLocation() + " denied");
      }
      if (allowedIPs.contains(connRequest.getIPAddressString())) {
        String langParam = connRequest.getParamOrNull("language");
        if (langParam == null)
          throw new IllegalArgumentException("Missing 'language' parameter");
        Language lang = Language.getLanguageForShortName(langParam);
        if (lang == null)
          throw new IllegalArgumentException("Unknown language '" +langParam+ "'");
        // TODO: create only once per language?!
        // TODO: how to take options from the client?
        JLanguageTool lt = new JLanguageTool(lang);
        lt.activateDefaultPatternRules();
        lt.activateDefaultFalseFriendRules();
        text = connRequest.getParamOrNull("text");
        if (text == null)
          throw new IllegalArgumentException("Missing 'text' parameter");
        print("Checking " + text.length() + " characters of text, language " + langParam);
        List<RuleMatch> matches = lt.check(text);
        connResponse.setHeaderLine(ProtocolResponseHeader.Content_Type, "text/xml");
        // TODO: how to set the encoding to utf-8 if we can just return a String?
        connResponse.setHeaderLine(ProtocolResponseHeader.Content_Encoding, System.getProperty("file.encoding"));
        String response = StringTools.ruleMatchesToXML(matches, text, CONTEXT_SIZE);
        print("Check done in " + (System.currentTimeMillis()-timeStart) + "ms");
        return response;
      } else {
        connResponse.setStatus(403);
        throw new RuntimeException("Error: Access from " + connRequest.getIPAddressString() + " denied");
      }
    } catch (Exception e) {
      if (verbose)
        print("Exceptions was caused by this text: " + text);
      e.printStackTrace();
      connResponse.setStatus(500);
      // escape input to avoid XSS attacks:
      return "Error: " + StringTools.escapeXML(e.toString());
    }
  }

  private void print(String s) {
    System.out.println(getDate() + " " + s);
  }

  private String getDate() {
    SimpleDateFormat sdf = new SimpleDateFormat();
    return sdf.format(new Date());
  }
  
  /**
   * Stop the server process.
   */
  public void stop() {
    System.out.println("Stopping server...");
    daemon.tearDown();
    System.out.println("Server stopped");
  }

  private static void printUsageAndExit() {
    System.out.println("Usage: HTTPServer [-p|--port port]");
    System.exit(1);
  }

  /**
   * Start the server from command line.
   * Usage: <tt>HTTPServer [-v|--verbose] [-p|--port port]</tt>
   */
  public static void main(String[] args) {
    if (args.length > 3) {
      printUsageAndExit();
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
    HTTPServer server = new HTTPServer(port, verbose);
    server.run();
  }

}
