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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import com.sun.net.httpserver.*;
import java.net.InetSocketAddress;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.rules.bitext.BitextRule;
import de.danielnaber.languagetool.tools.StringTools;
import de.danielnaber.languagetool.tools.Tools;

import java.net.HttpURLConnection;
/**
 * A small embedded HTTP server that checks text. Returns XML, prints debugging
 * to stdout/stderr.
 * 
 * @author Daniel Naber
 * @modified by Ankit
 */
class LanguageToolHttpHandler implements HttpHandler {

  /**
   * JLanguageTool instances for each language (created and configured on first use).
   * Instances are organized by language and mother language.
   * This is like a tree: first level contain the Languages, next level contains JLanguageTool instances for each mother tongue.
   */
  private static final Map<Language, Map<Language, JLanguageTool>> instances = new HashMap<Language, Map<Language, JLanguageTool>>();
  private static final Set<String> allowedIPs = new HashSet<String>();
  static {
    // accept only requests from localhost.
    // TODO: find a cleaner solution
    allowedIPs.add("/0:0:0:0:0:0:0:1"); // Suse Linux IPv6 stuff
    allowedIPs.add("/0:0:0:0:0:0:0:1%0"); // some(?) Mac OS X
    allowedIPs.add("/127.0.0.1");
  }
  private static final int CONTEXT_SIZE = 40; // characters

  public void handle(HttpExchange t) throws IOException {
    final Map<String, String> parameters = new HashMap<String, String>();
    synchronized (instances) {

      final URI requestedUri = t.getRequestURI();

      if ("post".equalsIgnoreCase(t.getRequestMethod())) { //POST
        final InputStreamReader isr = new InputStreamReader(t.getRequestBody(), "utf-8");
        try {
          final BufferedReader br = new BufferedReader(isr);
          try {
            final String query = br.readLine();
            parseQuery(query, parameters);
          } finally {
            br.close();
          }
        } finally {
          isr.close();
        }
      } else {   // GET
        final String query = requestedUri.getRawQuery();
        parseQuery(query, parameters);
      }

      final long timeStart = System.currentTimeMillis();
      String text = null;
      String sourceText = null;
      try {

        if (StringTools.isEmpty(requestedUri.getRawPath())) {
          t.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, 0);
          throw new RuntimeException("Error: Access to " + requestedUri.getPath() + " denied");
        }

        if (allowedIPs.contains(t.getRemoteAddress().getAddress().toString())) {

          // request type: list known languages
          if (requestedUri.getRawPath().endsWith("/Languages")) {
            t.getResponseHeaders().set("Content-Type", "text/xml");
            t.getResponseHeaders().set("Content_Encoding", "UTF-8");
            final String response = getSupportedLanguagesAsXML();
            t.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes().length);
            t.getResponseBody().write(response.getBytes());
            t.close();
          } else {
            // request type: grammar checking (default type)
            final String langParam = parameters.get("language");

            if (langParam == null) {
              throw new IllegalArgumentException("Missing 'language' parameter");
            }
            final Language lang = Language.getLanguageForShortName(langParam);
            if (lang == null) {
              throw new IllegalArgumentException("Unknown language '" + langParam + "'");
            }
            final String motherTongueParam = parameters.get("motherTongue");
            Language motherTongue = null;
            if (null != motherTongueParam) {
              motherTongue = Language.getLanguageForShortName(motherTongueParam);
            }
            
            // TODO: how to take options from the client?
            // TODO: customize lt here after reading client options

            text = parameters.get("text");

            if (text == null) {
              throw new IllegalArgumentException("Missing 'text' parameter");
            }
            
            List<RuleMatch> matches = null;
            
            sourceText = parameters.get("srctext");
            if (sourceText == null) {
              final JLanguageTool lt = getLanguageToolInstance(lang, motherTongue);
              print("Checking " + text.length() + " characters of text, language " + langParam);
              matches = lt.check(text);
            } else {
              
              if (motherTongueParam == null) {
                throw new IllegalArgumentException("Missing 'motherTongue' for bilingual checks");
              }
              
              print("Checking bilingual text, with source length" + sourceText.length() +
                  "and target length "+ text.length() + " (characters), source language " +
                  motherTongue + "and target language " + langParam);
              final JLanguageTool trglt = getLanguageToolInstance(lang, null);
              final JLanguageTool srclt = getLanguageToolInstance(motherTongue, null);
              final List<BitextRule> bRules = Tools.getBitextRules(motherTongue, lang);
              matches = Tools.checkBitext(sourceText, text, srclt, trglt, bRules);
              
            }
            t.getResponseHeaders().set("Content-Type", "text/xml");
            t.getResponseHeaders().set("Content_Encoding", "UTF-8");

            final String response = StringTools.ruleMatchesToXML(matches, text,
                CONTEXT_SIZE, StringTools.XmlPrintMode.NORMAL_XML);

            print("Check done in " + (System.currentTimeMillis() - timeStart) + "ms");

            t.sendResponseHeaders(HttpURLConnection.HTTP_OK, response.getBytes().length);
            t.getResponseBody().write(response.getBytes());
            t.close();

          }
        } else {
          t.sendResponseHeaders(HttpURLConnection.HTTP_FORBIDDEN, 0);
          throw new RuntimeException("Error: Access from " + t.getRemoteAddress().toString() + " denied");
        }
      } catch (Exception e) {
        if (HTTPServer.verbose) {
          print("Exceptions was caused by this text: " + text);
        }
        e.printStackTrace();
        final String response = "Error: " + StringTools.escapeXML(e.toString());
        t.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, response.getBytes().length);
        t.getResponseBody().write(response.getBytes());
        t.close();
      }
    }


  }

  private void print(String s) {
    System.out.println(getDate() + " " + s);
  }

  private String getDate() {
    final SimpleDateFormat sdf = new SimpleDateFormat();
    return sdf.format(new Date());
  }

  /**
   * Find or create a JLanguageTool instance for a specific language and mother tongue.
   * The instance will be reused. If any customization is required (like disabled rules), 
   * it will be done after acquiring this instance.
   * 
   * @param lang the language to be used.
   * @param motherTongue the user's mother tongue or <code>null</code>
   * @return a JLanguageTool instance for a specific language and mother tongue.
   * @throws Exception when JLanguageTool creation failed
   */
  private JLanguageTool getLanguageToolInstance(Language lang, Language motherTongue) throws Exception {
    Map<Language, JLanguageTool> languageTools = instances.get(lang);
    if (null == languageTools) {
      // first call using this language
      languageTools = new HashMap<Language, JLanguageTool>();
      instances.put(lang, languageTools);
    }
    final JLanguageTool languageTool = languageTools.get(motherTongue);
    if (null == languageTool) {
      print("Creating JLanguageTool instance for language " + lang + ((null != motherTongue) ? (" and mother tongue " + motherTongue) : ""));
      final JLanguageTool newLanguageTool = new JLanguageTool(lang, motherTongue);
      newLanguageTool.activateDefaultPatternRules();
      newLanguageTool.activateDefaultFalseFriendRules();
      languageTools.put(motherTongue, newLanguageTool);
      return newLanguageTool;
    }
    return languageTool;
  }

  /**
   * Construct an xml string containing all supported languages. <br/>The xml format is:<br/>
   * &lt;languages&gt;<br/>
   *	&nbsp;&nbsp;&lt;language name="Catalan" abbr="ca" /&gt;<br/> 
   *    &nbsp;&nbsp;&lt;language name="Dutch" abbr="nl" /&gt;<br/>
   *    &nbsp;&nbsp;...<br/>
   *  &lt;languages&gt;<br/>
   *  The languages are alphabetically sorted.  
   * @return an xml string containing all supported languages.
   */
  public static String getSupportedLanguagesAsXML() {
    final List<Language> languages = Arrays.asList(Language.REAL_LANGUAGES);
    Collections.sort(languages, new Comparator<Language>() {
      public int compare(Language o1, Language o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    final StringBuilder xmlBuffer = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>\n<languages>\n");
    for (Language lang : languages) {
      xmlBuffer.append(String.format("\t<language name=\"%s\" abbr=\"%s\" /> \n", lang.getName(), lang.getShortName()));
    }
    xmlBuffer.append("</languages>\n");
    return xmlBuffer.toString();
  }

  private void parseQuery(String query, Map<String, String> parameters) throws UnsupportedEncodingException {

    if (query != null) {
      final String[] pairs = query.split("[&]");

      for (String pair : pairs) {
        final String param = pair.substring(0, pair.indexOf("="));
        String key = null;
        String value;
        if (param != null) {
          key = URLDecoder.decode(param, System.getProperty("file.encoding"));
        }
        if (pair.substring(pair.indexOf("=") + 1) == null || pair.substring(pair.indexOf("=") + 1).equals("")) {
          value = "";
        } else {
          value = URLDecoder.decode(pair.substring(pair.indexOf("=") + 1), "UTF-8");
        }
        value = value.replaceAll("\\+", " ");
        parameters.put(key, value);
      }
    }
  }
}

/**
 * Start the server from command line. Usage:
 * <tt>HTTPServer [-v|--verbose] [-p|--port port]</tt>
 */
public class HTTPServer {// implements Runnable{

  com.sun.net.httpserver.HttpServer server;
  /**
   * The default port on which the server is running (8081).
   */
  public static final int DEFAULT_PORT = 8081;
  private int port = DEFAULT_PORT;
  public static boolean verbose;

  private static void printUsageAndExit() {
    System.out.println("Usage: HTTPServer [-p|--port port]");
    System.exit(1);
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
   *
   * @param verbose
   *          if true, the text to check will be displayed in case of exceptions
   *          (default: false)
   */
  public HTTPServer(int port, boolean verbose) {
    this.port = port;
    HTTPServer.verbose = verbose;
  }

  /**
   * Start the server.
   */
  public void run() {
    try {
      server = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(port), 0);
      server.createContext("/", new LanguageToolHttpHandler());
      System.out.println("Starting server on port " + port + "...");
      server.start();
      System.out.print("Server started");
    } catch (Exception e) {
      throw new PortBindingException(
          "LanguageTool server could not be started " + "on port " + port
          + ", maybe something else is running on that port already?");

    }

  }

  /**
   * Stop the server process.
   **/
  public void stop() {
    if (server != null) {
      System.out.println("Stopping server ");
      server.stop(0);
      System.out.println("Server stopped");
    }
  }

  public static void main(String[] args) {
    if (args.length > 3) {
      printUsageAndExit();
    }
    HTTPServer.verbose = false;
    int port = DEFAULT_PORT;
    for (int i = 0; i < args.length; i++) {
      if ("-p".equals(args[i]) || "--port".equals(args[i])) {
        port = Integer.parseInt(args[++i]);
      } else if ("-v".equals(args[i]) || "--verbose".equals(args[i])) {
        verbose = true;
      }
    }
    try {
      final com.sun.net.httpserver.HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
      server.createContext("/", new LanguageToolHttpHandler());
      server.start();
    } catch (Exception e) {
      // TODO: what to do?
    }

  }
}

