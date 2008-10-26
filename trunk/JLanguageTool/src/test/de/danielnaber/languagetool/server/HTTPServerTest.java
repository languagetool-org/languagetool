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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

import junit.framework.TestCase;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.XMLValidator;
import de.danielnaber.languagetool.tools.StringTools;

public class HTTPServerTest extends TestCase {

  public void testHTTPServer() {
    HTTPServer server = new HTTPServer();
    try {
      server.run();
      // no error:
      String enc = System.getProperty("file.encoding");
      assertEquals("<?xml version=\"1.0\" encoding=\""+enc+"\"?>\n<matches>\n</matches>\n", check(Language.GERMAN, ""));
      assertEquals("<?xml version=\"1.0\" encoding=\""+enc+"\"?>\n<matches>\n</matches>\n", check(Language.GERMAN, "Ein kleiner test"));
      // one error:
      assertTrue(check(Language.GERMAN, "ein kleiner test").indexOf("UPPERCASE_SENTENCE_START") != -1);
      // two errors:
      String result = check(Language.GERMAN, "ein kleiner test. Und wieder Erwarten noch was: \u00f6\u00e4\u00fc\u00df.");
      assertTrue(result.indexOf("UPPERCASE_SENTENCE_START") != -1);
      assertTrue(result.indexOf("WIEDER_WILLEN") != -1);
      assertTrue("Expected special chars, got: '" + result+ "'",
          result.indexOf("\u00f6\u00e4\u00fc\u00df") != -1);   // special chars are intact
      XMLValidator validator = new XMLValidator();
      validator.validateXMLString(result, "/resource/api-output.dtd", "matches");
      validator.checkSimpleXMLString(result);
      //System.err.println(result);
      // make sure XML chars are escaped in the result to avoid invalid XML
      // and XSS attacks:
      assertTrue(check(Language.GERMAN, "bla <script>").indexOf("<script>") == -1);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      server.stop();
    }
  }

  private String check(Language lang, String text) throws IOException {
    String urlOptions = "/?language=" + lang.getShortName();
    urlOptions += "&text=" + URLEncoder.encode(text, "latin1");
    URL url = new URL("http://localhost:" + HTTPServer.DEFAULT_PORT + urlOptions);
    InputStream stream = (InputStream)url.getContent();
    String result = StringTools.streamToString(stream);
    return result;
  }
  
}
