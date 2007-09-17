/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

public class VersionNumberTest extends TestCase {

  public void testVersionNumber() throws IOException {
    String buildFile = load("build.properties");
    Pattern p1 = Pattern.compile("version = ([0-9\\.]+(-dev)?)");
    Matcher m1 = p1.matcher(buildFile);
    m1.find();
    String javaFile = load("src/java/de/danielnaber/languagetool/JLanguageTool.java");
    Pattern p2 = Pattern.compile("VERSION = \"(.*?)\"");
    Matcher m2 = p2.matcher(javaFile);
    m2.find();
    assertEquals(m1.group(1), m2.group(1));
    //System.out.println(m1.group(1));
  }

  private String load(String filename) throws IOException {
    FileReader fr = new FileReader(filename);
    BufferedReader br = new BufferedReader(fr);
    StringBuffer sb = new StringBuffer();
    String line;    
    while ((line = br.readLine()) != null) {      
        sb.append(line);                
    }
    br.close();
    fr.close();
    return sb.toString();
  }

}
