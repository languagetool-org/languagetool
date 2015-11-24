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
