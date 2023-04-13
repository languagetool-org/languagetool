/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * A lightweight XML indentation helper for grammar.xml files. Unlike other XML
 * indenters, this will really only change the indentation (spaces at start of each line)
 * and *not* insert or remove line breaks.
 */
public class XmlIndenter {

  private static final int INDENT = 2;

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + XmlIndenter.class.getSimpleName() + " <xmlFile>");
      System.exit(1);
    }
    List<String> lines = Files.readAllLines(Paths.get(args[0]), StandardCharsets.UTF_8);
    boolean inCategory = false;
    boolean inRuleGroup = false;
    boolean inRule = false;
    boolean inAntiPattern = false;
    boolean inPattern = false;
    boolean inMarker = false;
    boolean inAnd = false;
    boolean inUnify = false;
    boolean inUnifyIgnore = false;
    boolean inToken = false;
    for (String line : lines) {
      String origLine = line;
      line = line.trim();
      if (line.contains("</marker>") && (inPattern || inAntiPattern)) { inMarker = false; }
      if (line.startsWith("</antipattern>")) { inAntiPattern = false; }
      if (line.startsWith("</pattern>")) { inPattern = false; }
      if (line.startsWith("</rule>")) { inRule = false; }
      if (line.startsWith("</rulegroup")) { inRuleGroup = false; }
      if (line.startsWith("</category")) { inCategory = false; }
      if (line.startsWith("</and")) { inAnd = false; }
      if (line.startsWith("</unify>")) { inUnify = false; }
      if (line.startsWith("</unify-ignore>")) { inUnifyIgnore = false; }
      int level = INDENT + (inCategory ? INDENT : 0) + (inRuleGroup ? INDENT : 0) + (inRule ? INDENT : 0) +
        (inPattern ? INDENT : 0) + (inAntiPattern ? INDENT : 0) + (inMarker ? INDENT : 0) + (inToken ? INDENT : 0) +
        (inAnd ? INDENT : 0) + (inUnify ? INDENT : 0) + (inUnifyIgnore ? INDENT : 0);
      if (line.startsWith("<category") || line.startsWith("</category")) {
        level = INDENT;
      }
      if (line.equals("</token>")) {
        level -= INDENT;
      }
      String indentSpaces = StringUtils.repeat(' ', level);
      if (!line.isEmpty() && (inCategory || line.startsWith("<category") || line.startsWith("</category"))) {
        System.out.println(indentSpaces + line);
      } else {
        System.out.println(origLine);
      }
      if (line.startsWith("<category")) { inCategory = true; }
      if (line.startsWith("<rulegroup")) { inRuleGroup = true; }
      if (line.startsWith("<rule ") || line.startsWith("<rule>")) { inRule = true; }
      if (line.startsWith("<pattern")) { inPattern = true; }
      if (line.startsWith("<antipattern") && !line.contains("</antipattern")) { inAntiPattern = true; }
      if (line.contains("<marker>") && !line.contains("</marker>") && (inPattern || inAntiPattern)) { inMarker = true; }
      if (line.contains("<and>")) { inAnd = true; }
      if (line.contains("<unify>") || line.contains("<unify ")) { inUnify = true; }
      if (line.contains("<unify-ignore>")) { inUnifyIgnore = true; }
      if (line.contains("</token>") || (line.contains("<token") && line.contains("/>")) && (inPattern || inAntiPattern)) { inToken = false; }
      if (line.contains("<token") && !line.contains("/>") && !line.contains("</token>") && (inPattern || inAntiPattern)) {
        inToken = true;
      }
    }
  }
}
