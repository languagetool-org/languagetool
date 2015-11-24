package org.languagetool.tools;

import org.apache.commons.cli.Option;

class BuilderWithFreqOptions extends BuilderOptions {
  public static final String FREQ_OPTION = "freq";

  public BuilderWithFreqOptions() {
    init();
  }
  
  private void init() {
    Option option = new Option(FREQ_OPTION, true, "optional .xml file with a frequency wordlist, see http://wiki.languagetool.org/developing-a-tagger-dictionary");
    option.setRequired(false);
    options.addOption(option);
  }
  
}
