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

import java.awt.Image;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.imageio.ImageIO;

import org.json.JSONArray;
import org.json.JSONObject;
import org.languagetool.JLanguageTool;
import org.languagetool.gui.Configuration;
import org.languagetool.openoffice.MessageHandler;
import org.languagetool.openoffice.MultiDocumentsHandler;
import org.languagetool.openoffice.OfficeTools;

import com.sun.star.lang.Locale;


/**
 * Class to communicate with a AI API
 * @since 6.5
 * @author Fred Kruse
 */
public class AiRemote {
  
  private static final ResourceBundle messages = JLanguageTool.getMessageBundle();
  public final static String TRANSLATE_INSTRUCTION = "loAiTranslateInstruction";
  public final static String CORRECT_INSTRUCTION = "loAiCorrectInstruction";
  public final static String STYLE_INSTRUCTION = "loAiStyleInstruction";
  public final static String EXPAND_INSTRUCTION = "loAiExpandInstruction";
  
  public static enum AiCommand { CorrectGrammar, ImproveStyle, ExpandText, GeneralAi };

/*
  public final static String CORRECT_INSTRUCTION = "Correct following text";
  public final static String STYLE_INSTRUCTION = "Rephrase following text";
  public final static String EXPAND_INSTRUCTION = "Expand following text";
*/
  private enum AiType { EDITS, COMPLETIONS, CHAT }
  
  private final static Map<String, String> commands = new HashMap<>();
  private static String lastLang = null;
  
  boolean debugModeTm = true;
  boolean debugMode = OfficeTools.DEBUG_MODE_AI;
  
  private final String apiKey;
  private final String model;
  private final String url;
  private final AiType aiType;
  
  public AiRemote(Configuration config) {
    apiKey = config.aiApiKey();
    model = config.aiModel();
    url = config.aiUrl();
    if (url.endsWith("/edits/") || url.endsWith("/edits")) {
      aiType = AiType.EDITS;
    } else if (url.endsWith("/chat/completions/") || url.endsWith("/chat/completions")) {
      aiType = AiType.CHAT;
    } else if (url.endsWith("/completions/") || url.endsWith("/completions")) {
      aiType = AiType.COMPLETIONS;
    } else {
      aiType = AiType.CHAT;
    }
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

  public String runInstruction(String instruction, String text, Locale locale, boolean onlyOneParagraph) {
    if (instruction == null || text == null) {
      return null;
    }
    instruction = instruction.trim();
    text = text.trim();
    if (onlyOneParagraph && (instruction.isEmpty() || text.isEmpty())) {
      return "";
    }
    if (instruction.isEmpty()) {
      if (text.isEmpty()) {
        return text;
      }
      instruction = text;
      text = null;
    } else if (text.isEmpty()) {
      text = null;
    }
    long startTime = 0;
    if (debugModeTm) {
      startTime = System.currentTimeMillis();
    }
    if (debugMode) {
      MessageHandler.printToLogFile("AiRemote: runInstruction: Ask AI started! URL: " + url);
    }
//    String langName = getLanguageName(locale);
    String langName = locale.Language;
    String org = text == null ? instruction : text;
    if (text != null) {
      text = text.replace("\n", "\r").replace("\r", " ").replace("\"", "\\\"");
    }
    String urlParameters;
//    instruction = addLanguageName(instruction, locale);
    if (aiType == AiType.CHAT) {
      urlParameters = "{\"model\": \"" + model + "\", " 
//          + "\"response_format\": { \"type\": \"json_object\" }, "
          + "\"language\": \"" + langName + "\", "
          + "\"messages\": [ { \"role\": \"user\", "
          + "\"content\": \"" + instruction + (text == null ? "" : ": {" + text + "}") + "\" } ], "
          + "\"seed\": 1, "
          + "\"temperature\": 0.7}";
    } else if (aiType == AiType.EDITS) {
      urlParameters = "{\"model\": \"" + model + "\", " 
//          + "\"response_format\": { \"type\": \"json_object\" },"
          + "\"instruction\": \"" + instruction + "\","
          + "\"input\": \"" + text + "\", "
//          + "\"seed\": 1, "
          + "\"temperature\": 0.7}";
    } else {
      urlParameters = "{\"model\": \"" + model + "\", " 
//          + "\"response_format\": { \"type\": \"json_object\" },"
          + "\"prompt\": \"" + instruction + ": {" + text + "}\", "
//          + "\"seed\": 1, "
          + "\"temperature\": 0.7}";
    }

    byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
    
    URL checkUrl;
    try {
      checkUrl = new URL(url);
    } catch (MalformedURLException e) {
      MessageHandler.showError(e);
      return null;
    }
    if (debugMode) {
      MessageHandler.printToLogFile("AiRemote: runInstruction: postData: " + urlParameters);
    }
    HttpURLConnection conn = getConnection(postData, checkUrl);
    try {
      if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
        try (InputStream inputStream = conn.getInputStream()) {
          String out = readStream(inputStream, "utf-8");
          out = parseJasonOutput(out);
          if (out == null) {
            return null;
          }
          out = filterOutput (out, org, instruction, onlyOneParagraph);
          if (debugModeTm) {
            long runTime = System.currentTimeMillis() - startTime;
            MessageHandler.printToLogFile("AiRemote: runInstruction: Time to generate Answer: " + runTime);
          }
          return out;
        }
      } else {
        try (InputStream inputStream = conn.getErrorStream()) {
          String error = readStream(inputStream, "utf-8");
          MessageHandler.showMessage("Got error: " + error + " - HTTP response code " + conn.getResponseCode());
        }
      }
    } catch (ConnectException e) {
      MessageHandler.showMessage("Could not connect to server at: " + url);
      MessageHandler.showError(e);
    } catch (Exception e) {
      MessageHandler.showError(e);
    } finally {
      conn.disconnect();
    }
    return null;
  }
  
  private String removeSurroundingBrackets(String out, String org) {
    if (out.startsWith("{") && out.endsWith("}")) {
      if (!org.startsWith("{") || !org.endsWith("}")) {
        return out.substring(1, out.length() - 1);
      }
    }
    return out;
  }
  
  private String filterOutput (String out, String org, String instruction, boolean onlyOneParagraph) {
    out = removeSurroundingBrackets(out, org);
    out = out.replace("\n", "\r").replace("\r\r", "\r").replace("\\\"", "\"").trim();
    if (onlyOneParagraph) {
      String[] inst = instruction.split("[-.:!?]");
      String[] parts = out.split("\r");
      String firstPart = parts[0].trim();
      if (parts.length > 1 && firstPart.endsWith(":") || firstPart.startsWith(inst[0].trim())) {
        out = parts[1].trim();
      } else {
        out = firstPart;
      }
      out = removeSurroundingBrackets(out, org);
      if (out.contains(":") && (!org.contains(":") || out.trim().startsWith(inst[0].trim()))) {
        parts = out.split(":");
        if (parts.length > 1) {
          out = parts[1];
          for (int i = 2; i < parts.length; i++) {
            out += ":" + parts[i];
          }
          out = removeSurroundingBrackets(out.trim(), org);
        }
      }
    }
    return out;
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
      String content;
      JSONObject choice = choices.getJSONObject(0);
      if (aiType == AiType.CHAT) {
        JSONObject message = choice.getJSONObject("message");
        content = message.getString("content");
      } else {
        content = choice.getString("text");
      }
      if (debugMode) {
        MessageHandler.printToLogFile("AiRemote: parseJasonOutput: text: " + text);
        MessageHandler.printToLogFile("AiRemote: parseJasonOutput: content: " + content);
      }
      try {
        JSONObject contentObject = new JSONObject(content);
        try {
          int nLastObj = contentObject.length() - 1;
          Set<String> keySet = contentObject.keySet();
          int i = 0;
          for (String key : keySet) {
            if ( i == nLastObj) {
              content = getString(new JSONObject(contentObject.getString(key)));
              break;
            }
            i++;
          }
        } catch (Throwable t) {
          try {
            content = getString(contentObject);
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
      return null;
    }
  }
  
  private String getString(JSONObject contentObject) {
    try {
      JSONObject subObject = new JSONObject(contentObject);
      return subObject.toString();
    } catch (Throwable t) {
    }
    return contentObject.toString();
  }
  
  public static String getInstruction(String mess, Locale locale) {
    if (locale == null || locale.Language == null || locale.Language.isEmpty()) {
      locale = new Locale("en", "US", "");
    }
/*
//    MessageHandler.printToLogFile("Get instruction for mess: " + mess + ", locale.Language: " + locale.Language);
    setCommands(locale);
    String instruction = commands.get(mess);
    if (instruction == null) {
      MessageHandler.showMessage("getInstruction: Instruction == null");
    }
    return (instruction);
*/
    ResourceBundle messages = JLanguageTool.getMessageBundle(MultiDocumentsHandler.getLanguage(locale));
    String instruction = messages.getString(mess) + " (language: " + locale.Language + ")"; 
    return instruction;
  }
  
  public static String getLanguageName(Locale locale) {
    String lang = (locale == null || locale.Language == null || locale.Language.isEmpty()) ? "en" : locale.Language;
    Locale langLocale = new Locale(lang, "", "");
    return MultiDocumentsHandler.getLanguage(langLocale).getName();
  }
  
  public static String addLanguageName(String instruction, Locale locale) {
    String langName = getLanguageName(locale);
    return instruction + " (language - " + langName + ")";
  }
  
  /**
   * Get Command file if exist - else default
   *//*
  private static InputStream getCommandsFileInputStream(Locale locale) {
    URL url = null;
    try {
      return AiRemote.class.getResourceAsStream("/commands/commands_" + locale.Language);
    } catch (Throwable e) {
    }
    if (url == null) {
      try {
        return AiRemote.class.getResourceAsStream("/commands/commands_en");
      } catch (Throwable e) {
        MessageHandler.showError(e);
      }
    }
    return null;
  }
  
  private static void setCommands(Locale locale) {
    if (lastLang != null && lastLang.equals(locale.Language)) {
//      MessageHandler.printToLogFile("Last language: " + lastLang + ", locale.Language: " + locale.Language);
      return;
    }
    lastLang = new String(locale.Language);
    InputStream input = getCommandsFileInputStream(locale);
    if (input == null) {
      MessageHandler.printToLogFile("commands file input == null");
      return;
    }
    commands.clear();
    BufferedReader in = null;
//    MessageHandler.printToLogFile("commands file: " + fileName);
    try {
//      in = new BufferedReader(new FileReader(fileName));
      in = new BufferedReader(new InputStreamReader(input));
      String row = null;
      while ((row = in.readLine()) != null) {
//        MessageHandler.printToLogFile("read row: " + row);
        String splitted[] = row.split("=");
        if (splitted.length == 2) {
//          MessageHandler.printToLogFile("add key: " + splitted[0] + ", command: " + splitted[1]);
          commands.put(splitted[0].trim(), splitted[1].trim());
        }
      }
    } catch (IOException e) {
      MessageHandler.showError(e);
    } finally {
      if (in != null)
        try {
          in.close();
        } catch (IOException e) {
        }
    }
  }
*/  
/*  
  public static String parseInstruction (String instruction, Locale locale) {
    String lang = (locale == null || locale.Language == null || locale.Language.isEmpty()) ? "en" : locale.Language;
    Locale langLocale = new Locale(lang, "", "");
    String languageName = MultiDocumentsHandler.getLanguage(langLocale).getName();
    return instruction.replace("{}", languageName);
  }
*/
}
