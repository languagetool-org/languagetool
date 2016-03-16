/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tools;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.NotNull;

class BuilderOptions {
  public static final String INFO_OPTION = "info";
  public static final String OUTPUT_OPTION = "o";
  public static final String INPUT_OPTION = "i";
  public static final String FREQ_OPTION = "freq";
  public static final String FREQ_HELP = "optional .xml file with a frequency wordlist, " 
      + "see http://wiki.languagetool.org/developing-a-tagger-dictionary";
  public static final String INFO_HELP = "*.info properties file, " 
      + "see http://wiki.languagetool.org/developing-a-tagger-dictionary";
  public static final String TAB_INPUT_HELP = "tab-separated plain-text dictionary file " 
      + "with format: wordform<tab>lemma<tab>postag";

  protected final Options options = new Options();

  BuilderOptions() {
    Option option = new Option(OUTPUT_OPTION, true, "output file");
    option.setRequired(true);
    options.addOption(option);
  }
  
  public void addOption(String opt, boolean hasArg, String description, boolean isRequired) {
    Option option = new Option(opt, hasArg, description);
    option.setRequired(isRequired);
    options.addOption(option);
  }
  
  @NotNull
  public CommandLine parseArguments(String[] args, Class<? extends DictionaryBuilder> clazz) throws ParseException {
    try {
      CommandLineParser parser = new BasicParser();
      CommandLine cmd = parser.parse(options, args);
      return cmd;
    } catch (ParseException e) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(clazz.getName(), options);
      System.exit(1);
      throw e; // should never happen - just to make compiler happy
    }
  }

}
