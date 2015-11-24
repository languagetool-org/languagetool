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
  
  protected final Options options = new Options();

  public BuilderOptions() {
    init();
  }
  
  private void init() {
    Option option = new Option(OUTPUT_OPTION, true, "output file");
    option.setRequired(true);
    options.addOption(option);
    
    option = new Option(INPUT_OPTION, true, "plain text dictionary file, e.g. created from a Hunspell dictionary by 'unmunch'");
    option.setRequired(true);
    options.addOption(option);

    option = new Option(INFO_OPTION, true, "*.info properties file, see http://wiki.languagetool.org/developing-a-tagger-dictionary");
    option.setRequired(true);
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
      formatter.printHelp( clazz.getName(), options );
      System.exit(1);
      throw e; // should never happen - just to make compiler happy
    }
  }

}
