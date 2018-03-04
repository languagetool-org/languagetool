/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.markup.AnnotatedTextBuilder;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TextCheckerTest {

  private final String english = "This is clearly an English text, should be easy to detect.";
  private final TextChecker checker = new V2TextChecker(new HTTPServerConfig(), false, null, new RequestCounter());

  @Test
  public void testMaxTextLength() throws Exception {
    Map<String, String> params = new HashMap<>();
    params.put("text", "not used");
    params.put("language", "en");
    HTTPServerConfig config1 = new HTTPServerConfig();
    config1.setMaxTextLength(10);
    TextChecker checker = new V2TextChecker(config1, false, null, new RequestCounter());
    try {
      checker.checkText(new AnnotatedTextBuilder().addText("longer than 10 chars").build(), new FakeHttpExchange(), params, null, null);
      fail();
    } catch (TextTooLongException ignore) {}
    try {
      params.put("token", "invalid");
      checker.checkText(new AnnotatedTextBuilder().addText("longer than 10 chars").build(), new FakeHttpExchange(), params, null, null);
      fail();
    } catch (RuntimeException ignore) {}
    String validToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwczpcL1wvbGFuZ3VhZ2V0b29scGx1cy5jb20iLCJpYXQiOjE1MDQ4NTY4NTQsInVpZCI6MSwibWF4VGV4dExlbmd0aCI6MTAwfQ._-8qpa99IWJiP_Zx5o-yVU11neW8lrxmLym1DdwPtIc";
    try {
      params.put("token", validToken);
      checker.checkText(new AnnotatedTextBuilder().addText("longer than 10 chars").build(), new FakeHttpExchange(), params, null, null);
      fail();
    } catch (RuntimeException expected) {
      // server not configured to accept tokens
    }
    try {
      config1.secretTokenKey = "foobar";
      checker.checkText(new AnnotatedTextBuilder().addText("longer than 10 chars").build(), new FakeHttpExchange(), params, null, null);
      fail();
    } catch (SignatureVerificationException ignore) {}

    config1.secretTokenKey = "foobar";
    // see test below for how to create a token:
    params.put("token", "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJodHRwOi8vZm9vYmFyIiwiaWF0IjoxNTA0ODU3NzAzLCJtYXhUZXh0TGVuZ3RoIjozMH0.2ijjMEhSyJPEc0fv91UtOJdQe8CMfo2U9dbXgHOkzr0");
    checker.checkText(new AnnotatedTextBuilder().addText("longer than 10 chars").build(), new FakeHttpExchange(), params, null, null);

    try {
      config1.secretTokenKey = "foobar";
      checker.checkText(new AnnotatedTextBuilder().addText("now it's even longer than 30 chars").build(), new FakeHttpExchange(), params, null, null);
      fail();
    } catch (TextTooLongException expected) {
      // too long even with claim from token, which allows 30 characters
    }
  }
  
  @Test
  @Ignore("use to create JWT test tokens for the other tests")
  public void makeToken() throws UnsupportedEncodingException {
    Algorithm algorithm = Algorithm.HMAC256("foobar");
    String token = JWT.create()
            .withIssuer("http://foobar")
            .withIssuedAt(new Date())
            .withClaim("maxTextLength", 30)
            //.withExpiresAt(new Date());
            .sign(algorithm);
    System.out.println(token);
  }
  
  @Test
  public void testDetectLanguageOfString() {
    assertThat(checker.detectLanguageOfString("", "en", Arrays.asList("en-GB")).getShortCodeWithCountryAndVariant(), is("en-GB"));
    assertThat(checker.detectLanguageOfString("X", "en", Arrays.asList("en-GB")).getShortCodeWithCountryAndVariant(), is("en-GB"));
    assertThat(checker.detectLanguageOfString("X", "en", Arrays.asList("en-ZA")).getShortCodeWithCountryAndVariant(), is("en-ZA"));
    assertThat(checker.detectLanguageOfString(english, "de", Arrays.asList("en-GB", "de-AT")).getShortCodeWithCountryAndVariant(), is("en-GB"));
    assertThat(checker.detectLanguageOfString(english, "de", Arrays.asList()).getShortCodeWithCountryAndVariant(), is("en-US"));
    assertThat(checker.detectLanguageOfString(english, "de", Arrays.asList("de-AT", "en-ZA")).getShortCodeWithCountryAndVariant(), is("en-ZA"));
    String german = "Das hier ist klar ein deutscher Text, sollte gut zu erkennen sein.";
    assertThat(checker.detectLanguageOfString(german, "fr", Arrays.asList("de-AT", "en-ZA")).getShortCodeWithCountryAndVariant(), is("de-AT"));
    assertThat(checker.detectLanguageOfString(german, "fr", Arrays.asList("de-at", "en-ZA")).getShortCodeWithCountryAndVariant(), is("de-AT"));
    assertThat(checker.detectLanguageOfString(german, "fr", Arrays.asList()).getShortCodeWithCountryAndVariant(), is("de-DE"));
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidPreferredVariant() {
    checker.detectLanguageOfString(english, "de", Arrays.asList("en"));  // that's not a variant
  }

  @Test(expected = RuntimeException.class)
  public void testInvalidPreferredVariant2() {
    checker.detectLanguageOfString(english, "de", Arrays.asList("en-YY"));  // variant doesn't exist
  }

  class FakeHttpExchange extends HttpExchange {
    @Override
    public Headers getRequestHeaders() {
      return new Headers();
    }
    @Override
    public Headers getResponseHeaders() {
      return new Headers();
    }
    @Override
    public URI getRequestURI() {
      return null;
    }
    @Override
    public String getRequestMethod() {
      return null;
    }
    @Override
    public HttpContext getHttpContext() {
      return null;
    }
    @Override
    public void close() {

    }
    @Override
    public InputStream getRequestBody() {
      return null;
    }
    @Override
    public OutputStream getResponseBody() {
      return new ByteArrayOutputStream();
    }
    @Override
    public void sendResponseHeaders(int i, long l) throws IOException {
    }
    @Override
    public InetSocketAddress getRemoteAddress() {
      return null;
    }
    @Override
    public int getResponseCode() {
      return 0;
    }
    @Override
    public InetSocketAddress getLocalAddress() {
      return null;
    }
    @Override
    public String getProtocol() {
      return null;
    }
    @Override
    public Object getAttribute(String s) {
      return null;
    }
    @Override
    public void setAttribute(String s, Object o) {
    }
    @Override
    public void setStreams(InputStream inputStream, OutputStream outputStream) {
    }
    @Override
    public HttpPrincipal getPrincipal() {
      return null;
    }
  }

}
