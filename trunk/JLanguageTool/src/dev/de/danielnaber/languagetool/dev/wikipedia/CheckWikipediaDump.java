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

/*
 *
 * Created on 21.12.2006
 */
package de.danielnaber.languagetool.dev.wikipedia;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;

/**
 * Command-line tool that checks texts from Wikipedia (download "pages-articles.xml.bz2" from
 * http://download.wikimedia.org/backup-index.html, e.g.
 * http://download.wikimedia.org/dewiki/latest/dewiki-latest-pages-articles.xml.bz2)
 * and stores the result in a database.
 * 
 * @author Daniel Naber
 */
public class CheckWikipediaDump {

  private CheckWikipediaDump() {
    // no public constructor
  }
  
  public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
    final CheckWikipediaDump prg = new CheckWikipediaDump();
    if (args.length < 3 || args.length > 4) {
      System.err.println("Usage: CheckWikipediaDump <propertyFile> <language> <filename> [maxArticleCheck]");
      System.err.println("\tpropertyFile a file to set database access properties. Use '-' to print results to stdout.");
      System.err.println("\tlanguage languagecode like 'en' or 'de'");
      System.err.println("\tfilename path to unpacked Wikipedia XML dump");
      System.err.println("\tmaxArticleCheck optional: maximum number of articles to check");
      System.exit(1);
    }
    int maxArticles = 0;
    if (args.length == 4) {
      maxArticles = Integer.parseInt(args[3]);
    }
    File propFile = null;
    if (!"-".equals(args[0])) {
      propFile = new File(args[0]);
      if (!propFile.exists() || propFile.isDirectory()) {
        throw new IOException("file not found or isn't a file: " + propFile.getAbsolutePath());
      }
    }
    prg.run(propFile, args[1], args[2], maxArticles);
  }
  
  private void run(File propFile, String language, String textFilename, int maxArticles) 
      throws IOException, SAXException, ParserConfigurationException {
    final File file = new File(textFilename);
    if (!file.exists() || !file.isFile()) {
      throw new IOException("File doesn't exist or isn't a file: " + textFilename);
    }
    final Language lang = Language.getLanguageForShortName(language);
    if (lang == null) {
      System.err.println("Language not supported: " + language);
      System.exit(1);
    }
    final JLanguageTool languageTool = new JLanguageTool(lang);
    languageTool.activateDefaultPatternRules();
    // useful settings (avoid false alarms) because text extraction
    // from Wikipedia isn't clean yet:
    languageTool.disableRule("DE_CASE");    // too many false hits
    languageTool.disableRule("UNPAIRED_BRACKETS");
    languageTool.disableRule("UPPERCASE_SENTENCE_START");
    languageTool.disableRule("WORD_REPEAT_RULE");
    languageTool.disableRule("COMMA_PARENTHESIS_WHITESPACE");
    languageTool.disableRule("WHITESPACE_RULE");
    languageTool.disableRule("EN_QUOTES");        // en
    languageTool.disableRule("CUDZYSLOW_DRUKARSKI");  // pl
    languageTool.disableRule("POMIŠLJAJ_1");  // sl
    languageTool.disableRule("POMIŠLJAJ_2");  // sl
    languageTool.disableRule("POMIŠLJAJ_3");  // sl
    /*
    List rules = lt.getAllRules();
    for (Iterator iter = rules.iterator(); iter.hasNext();) {
      Rule element = (Rule) iter.next();
      lt.disableRule(element.getId());
    }
    lt.enableRule("DE_AGREEMENT");
    */
    System.err.println("These rules are disabled: " + languageTool.getDisabledRules());
    final Date dumpDate = getDumpDate(file);
    System.out.println("Dump date: " + dumpDate + ", language: " + language);
    final BaseWikipediaDumpHandler handler;
    if (propFile != null) {
      handler = new DatabaseDumpHandler(languageTool, maxArticles, dumpDate,
                language, propFile, lang); 
    } else {
      handler = new OutputDumpHandler(languageTool, maxArticles, dumpDate,
              language, lang); 
    }
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    final SAXParser saxParser = factory.newSAXParser();
    saxParser.parse(file, handler);
  }

  private Date getDumpDate(File file) throws IOException {
    final String filename = file.getName();
    final String[] parts = filename.split("-");
    if (parts.length < 3) {
      throw new IOException("Unexpected filename format: " + file.getName());
    }
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    try {
      return sdf.parse(parts[1]);
    } catch (ParseException e) {
      throw new IOException("Unexpected date format: " + parts[1], e);
    }
  }

}
