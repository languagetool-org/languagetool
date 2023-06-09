/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ca;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.AbstractTextToNumberFilter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class TextToNumberFilter extends AbstractTextToNumberFilter {

  static {
    numbers.put("zero", (float) 0);
    numbers.put("mig", (float) 0.5);
    numbers.put("un", (float) 1);
    numbers.put("u", (float) 1);
    numbers.put("una", (float) 1);
    numbers.put("dos", (float) 2);
    numbers.put("dues", (float) 2);
    numbers.put("tres", (float) 3);
    numbers.put("quatre", (float) 4);
    numbers.put("cinc", (float) 5);
    numbers.put("sis", (float) 6);
    numbers.put("set", (float) 7);
    numbers.put("vuit", (float) 8);
    numbers.put("huit", (float) 8);
    numbers.put("nou", (float) 9);
    numbers.put("deu", (float) 10);
    numbers.put("onze", (float) 11);
    numbers.put("dotze", (float) 12);
    numbers.put("tretze", (float) 13);
    numbers.put("catorze", (float) 14);
    numbers.put("quinze", (float) 15);
    numbers.put("setze", (float) 16);
    numbers.put("disset", (float) 17);
    numbers.put("desset", (float) 17);
    numbers.put("dèsset", (float) 17);
    numbers.put("divuit", (float) 18);
    numbers.put("devuit", (float) 18);
    numbers.put("díhuit", (float) 18);
    numbers.put("dinou", (float) 19);
    numbers.put("denou", (float) 19);
    numbers.put("dènou", (float) 19);
    numbers.put("dèneu", (float) 19);
    numbers.put("vint", (float) 20);
    numbers.put("trenta", (float) 30);
    numbers.put("quaranta", (float) 40);
    numbers.put("cinquanta", (float) 50);
    numbers.put("seixanta", (float) 60);
    numbers.put("setanta", (float) 70);
    numbers.put("vuitanta", (float) 80);
    numbers.put("huitanta", (float) 80);
    numbers.put("noranta", (float) 90);
    multipliers.put("cent", (float) 100);
    multipliers.put("cents", (float) 100);
    multipliers.put("mil", (float) 1000);
    multipliers.put("milió", (float) 1000000);
    multipliers.put("milions", (float) 1000000);
    multipliers.put("bilió", (float) 10E12);
    multipliers.put("bilions", (float) 10E12);
    multipliers.put("trilió", (float) 10E18);
    multipliers.put("trilions", (float) 10E18);
  }
  
  @Override
  protected boolean isComma(String s) {
    return s.equalsIgnoreCase("comma") || s.equalsIgnoreCase("coma");
  }
  
  @Override
  protected boolean isPercentage(AnalyzedTokenReadings[] patternTokens, int i) {
    return patternTokens[i].getToken().equals("cent") && patternTokens[i - 1].getToken().toLowerCase().equals("per");
  }

  @Override
  protected List<String> tokenize(String s) {
    s.split("[i\\-]");
    return Arrays.asList(s.split("-"));
  };
  
  @Override
  protected String formatResult(String s) {
    return s.replace(".", ",");
  }
 

}
