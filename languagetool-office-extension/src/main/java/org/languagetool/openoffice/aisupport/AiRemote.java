/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.openoffice.aisupport;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import org.languagetool.gui.Configuration;
import org.languagetool.openoffice.MessageHandler;


/**
 * Class to communicate with a AI API
 * @since 6.5
 * @author Fred Kruse
 */
public class AiRemote {
  
  boolean debugModeTm = true;
  
  private String apiKey = "Bearer blah_blah";
  private String model = "gpt-35-turbo";
//  private String model = "gpt-4-turbo";
  private String url = "https://aiforcause.deepnight.tech/openai/";
  
  public AiRemote(Configuration config) {
    apiKey = config.aiApiKey();
    model = config.aiModel();
    url = config.aiUrl();
  }
  
  HttpURLConnection getConnection(byte[] postData, URL url) throws RuntimeException {
    try {
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setDoOutput(true);
      conn.setInstanceFollowRedirects(false);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("charset", "utf-8");
      conn.setRequestProperty("Authorization", apiKey);
      conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
      
      try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
        wr.write(postData);
      }
      return conn;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String answerQuestion(String question)  throws Throwable {
    if (question == null) {
      return "";
    }
    String text = question.trim();
    if (text.isEmpty()) {
      return text;
    }
    long startTime = 0;
    if (debugModeTm) {
      startTime = System.currentTimeMillis();
      MessageHandler.printToLogFile("Ask AI started");
    }
    text = text.replace("\n", "\r").replace("\"", "\\\"");
    
    String urlParameters = "{\"model\": \"" + model + "\", " 
        + "\"response_format\": { \"type\": \"json_object\" }, \"messages\": [ { \"role\": \"user\", "
        + "\"content\": \"" + text + "\" } ] }";
    
    byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
    
    URL checkUrl;
    try {
      checkUrl = new URL(url);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    HttpURLConnection conn = getConnection(postData, checkUrl);
    try {
      if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
        try (InputStream inputStream = conn.getInputStream()) {
          text = readStream(inputStream, "utf-8");
          text = parseJasonOutput(text);
          if (text == null) {
            return null;
          }
          text = text.replace("\n", "\r").replace("\r\r", "\r").replace("\\\"", "\"");
          if (debugModeTm) {
            long runTime = System.currentTimeMillis() - startTime;
            MessageHandler.printToLogFile("Time to generate Answer: " + runTime);
          }
          return text;
        }
      } else {
        try (InputStream inputStream = conn.getErrorStream()) {
          String error = readStream(inputStream, "utf-8");
          throw new RuntimeException("Got error: " + error + " - HTTP response code " + conn.getResponseCode());
        }
      }
    } catch (ConnectException e) {
      throw new RuntimeException("Could not connect to server at: " + url, e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      conn.disconnect();
    }
  }
  
  private String readStream(InputStream stream, String encoding) throws IOException {
    StringBuilder sb = new StringBuilder();
    try (InputStreamReader isr = new InputStreamReader(stream, encoding);
         BufferedReader br = new BufferedReader(isr)) {
      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line).append('\r');
      }
    }
    return sb.toString();
  }
  
  String parseJasonOutput(String text) {
    try {
      JSONObject jsonObject = new JSONObject(text);
      JSONArray choices;
      try {
        choices = jsonObject.getJSONArray("choices");
      } catch (Throwable t) {
        String error = jsonObject.getString("error");
        MessageHandler.showMessage(error);
        return null;
      }
      JSONObject choice = choices.getJSONObject(0);
      JSONObject message = choice.getJSONObject("message");
      String content = message.getString("content");
      MessageHandler.printToLogFile("text: " + text);
      MessageHandler.printToLogFile("content: " + content);
      try {
        JSONObject contentObject = new JSONObject(content);
        try {
          int nLastObj = contentObject.length() - 1;
          Set<String> keySet = contentObject.keySet();
          int i = 0;
          for (String key : keySet) {
            if ( i == nLastObj) {
              content = contentObject.getString(key);
              break;
            }
            i++;
          }
        } catch (Throwable t) {
          try {
            content = contentObject.toString();
          } catch (Throwable t1) {
            return content;
          }
        }
      } catch (Throwable t) {
        return content;
      }
      return content;
    } catch (Throwable t) {
      MessageHandler.showError(t);
      MessageHandler.showMessage(text);
    }
    return null;
  }
  
}
