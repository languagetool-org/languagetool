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
 * A small embedded HTTP server that checks text. Returns XML, prints debugging
 * to stdout/stderr.
 * 
 * @author Daniel Naber
 */
public class HTTPServer extends ContentOracle {

  /**
   * JLanguageTool instances for each language (created and configured on fist use).
   * Instances are organized by language and mother language.
   * This is like a tree: first level contain the Languages, next level contains JLanguageTool instances for each mother tongue.
   */
  private static Map<Language, Map<Language, JLanguageTool>> instances = new HashMap<Language, Map<Language, JLanguageTool>>();
  /**
   * The default port on which the server is running (8081).
   */
  public static final int DEFAULT_PORT = 8081;

  private static final int CONTEXT_SIZE = 40; // characters

  private Daemon daemon;
  private int port = DEFAULT_PORT;
  private boolean verbose;

  private static final Set<String> allowedIPs = new HashSet<String>();
  static {
    // accept only requests from localhost.
    // TODO: find a cleaner solution
    allowedIPs.add("/0:0:0:0:0:0:0:1"); // Suse Linux IPv6 stuff
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
   * 
   * @param verbose
   *          if true, the text to check will be displayed in case of exceptions
   *          (default: false)
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
      throw new PortBindingException(
          "LanguageTool server could not be started " + "on port " + port
              + ", maybe something else is running on that port already?");
  }

  public String demultiplex(Request connRequest, Response connResponse) {
    long timeStart = System.currentTimeMillis();
    String text = null;
    try {
      if (StringTools.isEmpty(connRequest.getLocation())) {
        connResponse.setStatus(403);
        throw new RuntimeException("Error: Access to "
            + connRequest.getLocation() + " denied");
      }
      if (allowedIPs.contains(connRequest.getIPAddressString())) {
    	// TODO: temporary fix until jaminid bug is fixed (it seams that non-asci characters are not handled correctly) 
    	// see https://sourceforge.net/tracker/?func=detail&aid=2876507&group_id=127764&atid=709370
	    fixRequestParamMap(connRequest);
	    
	    // return content base on request string.
	    // Refactror this when the number of known request types gets too big.   

	    // request type: list known languages
	    if (connRequest.getLocation().endsWith("/Languages")) {
	      connResponse.setHeaderLine(ProtocolResponseHeader.Content_Type, "text/xml");
	      connResponse.setHeaderLine(ProtocolResponseHeader.Content_Encoding, "UTF-8");
          return getSupportedLanguagesAsXML();
	    }
	    
	    // request type: grammar checking (default type)
        String langParam = connRequest.getParamOrNull("language");
        if (langParam == null)
          throw new IllegalArgumentException("Missing 'language' parameter");
        Language lang = Language.getLanguageForShortName(langParam);
        if (lang == null)
          throw new IllegalArgumentException("Unknown language '" + langParam
              + "'");
        String motherTongueParam = connRequest.getParamOrNull("motherTongue");
		Language motherTongue = null;
        if (null != motherTongueParam)
          motherTongue = Language.getLanguageForShortName(motherTongueParam);
        JLanguageTool lt = getLanguageToolInstance(lang, motherTongue);
        // TODO: how to take options from the client?
        // TODO: customize lt here after reading client options
        text = connRequest.getParamOrNull("text");
        if (text == null)
          throw new IllegalArgumentException("Missing 'text' parameter");
        print("Checking " + text.length() + " characters of text, language "
            + langParam);
        List<RuleMatch> matches = lt.check(text);
        connResponse.setHeaderLine(ProtocolResponseHeader.Content_Type,
            "text/xml");
        // TODO: how to set the encoding to utf-8 if we can just return a
        // String?
        connResponse.setHeaderLine(ProtocolResponseHeader.Content_Encoding,
            "UTF-8");
        String response = StringTools.ruleMatchesToXML(matches, text,
            CONTEXT_SIZE, StringTools.XmlPrintMode.NORMAL_XML);
        print("Check done in " + (System.currentTimeMillis() - timeStart)
            + "ms");
        return response;
      }
      connResponse.setStatus(403);
      throw new RuntimeException("Error: Access from "
          + connRequest.getIPAddressString() + " denied");
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
   * Private fix until jaminid bug is fixed (it seams that non-asci characters are not handled correctly) 
   * see https://sourceforge.net/tracker/?func=detail&aid=2876507&group_id=127764&atid=709370
   * 
   * @param connRequest the Request object from jaminid ContentOracle. 
   * @throws UnsupportedEncodingException If character encoding needs to be consulted, but named character encoding is not supported.
   */
  private void fixRequestParamMap(final Request connRequest) throws UnsupportedEncodingException {
    Map<String, String> paramMap = getParamMap(connRequest);
    connRequest.getParamMap().clear();
    connRequest.getParamMap().putAll(paramMap);
  }

  /**
   * Private fix until jaminid bug is fixed (it seams that non-asci characters are not handled correctly) 
   * see https://sourceforge.net/tracker/?func=detail&aid=2876507&group_id=127764&atid=709370
   * Method to get the requst parameters from the request string. The default implementation can't handle 
   * the UTF-8 characters (like șțîâ). We just use  URLDecoder.decode() instead of the default unescape private method.   
   * @param connRequest the Request object from jaminid ContentOracle.
   * @return the parameters map.
   * @throws UnsupportedEncodingException If character encoding needs to be consulted, but named character encoding is not supported
   */
  private Map<String, String> getParamMap(Request connRequest) throws UnsupportedEncodingException {
    Map<String, String> paramMap = new HashMap<String, String>();
    if (null == connRequest) 
      return paramMap;
    String requestStr = null;
    if (!StringTools.isEmpty(connRequest.getBody())) {
     requestStr = connRequest.getBody(); // POST
    } else {
      requestStr = connRequest.getParamString(); // GET
    }
    if (StringTools.isEmpty(requestStr))
      return paramMap;

    String[] comps = requestStr.split("&");
    for (int i = 0; i < comps.length; i++) {
      int equalsLoc = comps[i].indexOf("=");
        if (equalsLoc > 0) {
          paramMap.put(comps[i].substring(0, equalsLoc),
            URLDecoder.decode(comps[i].substring(equalsLoc + 1), "UTF-8"));
          // TODO: Find some way to determine the encoding used on client-side
          // maybe "Accept-Charset" request header could be used.
          // UTF-8 will work on most platforms and browsers.
        } else {
          paramMap.put(comps[i], "");
        }
    }
    return paramMap;	
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
  private JLanguageTool getLanguageToolInstance(Language lang, Language motherTongue) 
          throws Exception {
    Map<Language, JLanguageTool> languageTools = instances.get(lang);
    if (null == languageTools) {
      // first call using this language
      languageTools = new HashMap<Language, JLanguageTool>();
      instances.put(lang, languageTools);
    }
    JLanguageTool languageTool = languageTools.get(motherTongue);
    if (null == languageTool) {
      print("Creating JLanguageTool instance for language " + lang + ((null != motherTongue)?(" and mother tongue " + motherTongue):""));
      JLanguageTool newLanguageTool = new JLanguageTool(lang, motherTongue);
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
    List<Language> languages = Arrays.asList(Language.REAL_LANGUAGES);
    Collections.sort(languages, 
      new Comparator<Language>() {@Override
        public int compare(Language o1, Language o2) {
		  return o1.getName().compareTo(o2.getName());
		}
      });
    StringBuilder xmlBuffer = new StringBuilder("<?xml version='1.0' encoding='UTF-8'?>\n<languages>\n");
    for (Language lang : languages) {
     xmlBuffer.append(String.format("\t<language name=\"%s\" abbr=\"%s\" /> \n", lang.getName(), lang.getShortName()));
    }
    xmlBuffer.append("</languages>\n");
    return xmlBuffer.toString();
  }
	
  /**
   * Start the server from command line. Usage:
   * <tt>HTTPServer [-v|--verbose] [-p|--port port]</tt>
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
