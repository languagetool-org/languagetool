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
package de.danielnaber.languagetool.dev;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Command line tool to list supported languages and their number of rules.
 * 
 * @author Daniel Naber
 */
public class Overview {

  public static void main(String[] args) throws IOException {
    Overview prg = new Overview();
    prg.run();
  }
  
  private Overview() {
    // no constructor
  }
  
  private void run() throws IOException {
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      if (Language.LANGUAGES[i] != Language.DEMO) {
        Language lang = Language.LANGUAGES[i];
        System.out.println(lang.getName());
        String xmlFile = "rules" + File.separator + lang.getShortName() + File.separator + "grammar.xml";
        File f = new File(xmlFile);
        if (!f.exists()) {
          System.out.println("  0 XML rules");
        } else {
          // count XML rules:
          String xmlRules = StringTools.readFile(xmlFile);
          xmlRules = xmlRules.replaceAll("(?s)<!--.*?-->", "");
          int pos = 0;
          int count = 0;
          while (pos != -1) {
            pos = xmlRules.indexOf("<rule", pos+1);
            if (pos == -1)
              break;
            count++;
          }
          System.out.println("  " + count + " XML rules");
        }
        // count Java rules:
        File dir = new File("src/java/de/danielnaber/languagetool/rules/" + lang.getShortName());
        if (!dir.exists()) {
          System.out.println("  0 Java rules");
        } else {
          File[] javaRules = dir.listFiles(new JavaFilter());
          System.out.println("  " + (javaRules.length-1) + " java rules");    // minus 1: one is always "<Language>Rule.java"
        }
      }
    }
  }

}

class JavaFilter implements FileFilter {

  public boolean accept(File f) {
    if (f.getName().endsWith(".java"))
      return true;
    return false;
  }

}
