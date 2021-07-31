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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class FakeHttpExchange extends HttpExchange {

  public FakeHttpExchange() {
    this("get");
  }

  public FakeHttpExchange(String method) {
    this.method = method;
  }

  private final String method;
  
  private final ByteArrayOutputStream bos = new ByteArrayOutputStream();

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
    return method;
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
    return bos;
  }
  @Override
  public void sendResponseHeaders(int i, long l) {
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

  public String getOutput() {
    return new String(bos.toByteArray(), StandardCharsets.UTF_8);
  }

}
