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

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.tools.StringTools;

/**
 * Uses JLanguageTol on the files of the BNC (British National Corpus).
 * 
 * @author Daniel Naber
 */
public class BNCCheck {

  public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
    if (args.length != 1) {
      System.out.println("Usage: BNCTest <directory>");
      System.exit(1);
    }
    BNCCheck prg = new BNCCheck();
    prg.run(new File(args[0]));
  }

  private Main prg;
  
  private BNCCheck() throws IOException, ParserConfigurationException, SAXException {
    prg = new Main(false, Language.ENGLISH, null);
    String[] disRules = new String[] {"UPPERCASE_SENTENCE_START", "COMMA_PARENTHESIS_WHITESPACE",
        "WORD_REPEAT_RULE", "DOUBLE_PUNCTUATION"};
    System.err.println("Note: disabling the following rules:");
    for (int i = 0; i < disRules.length; i++) {
      prg.getJLanguageTool().disableRule(disRules[i]);
      System.err.println(" " + disRules[i]);
    }
  }

  private void run(File file) throws IOException {
    if (file.isDirectory()) {
      File[] files = file.listFiles();
      for (int i = 0; i < files.length; i++) {
        run(new File(file, files[i].getName()));
      }
    } else {
      System.out.println("Checking " + file.getAbsolutePath());
      String text = StringTools.readFile(file.getAbsolutePath());
      text = text.replaceAll("(?s)<header.*?>.*?</header>", "");
      text = text.replaceAll("<w.*?>", "");
      text = text.replaceAll("<c.*?>", "");
      text = text.replaceAll("<.*?>", "");
      text = text.replaceAll(" +", " ");
      text = text.replaceAll("&bquo|&equo", "\"");
      text = text.replaceAll("&mdash;?", "--");
      text = text.replaceAll("&amp;?", "&");
      //System.out.println(text);
      prg.checkText(text);
    }
  }

}
