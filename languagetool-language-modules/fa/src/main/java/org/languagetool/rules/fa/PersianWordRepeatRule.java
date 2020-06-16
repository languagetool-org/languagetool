/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.fa;

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordRepeatRule;

/**
 * Word repeat rule for English, to avoid false alarms in the generic word repetition rule.
 * 
 * @since 2.7
 */
public class PersianWordRepeatRule extends WordRepeatRule {

  public PersianWordRepeatRule(ResourceBundle messages, Language language) {
    super(messages, language);
    addExamplePair(Example.wrong("این کار <marker>برای برای</marker> تو بود."),
                   Example.fixed("این کار <marker>برای</marker> تو بود."));
  }

  @Override
  public String getId() {
    return "PERSIAN_WORD_REPEAT_RULE";
  }

  @Override
  public boolean ignore(AnalyzedTokenReadings[] tokens, int position) {
    if (wordRepetitionOf("لی", tokens, position)) {
      return true;   // "من لی لی را دیدم"
    }
    if (wordRepetitionOf("سی", tokens, position)) {
      return true;   // "آب درون بطری ۱۰۰ سی سی بود"
    }
    if (wordRepetitionOf("لک", tokens, position)) {
      return true;   // "لک لک یک پرنده است"
    }
    if (wordRepetitionOf("ریز", tokens, position)) {
      return true;   // "غذایش را ریز ریز کرد"
    }
    if (wordRepetitionOf("جز", tokens, position)) {
      return true;   // "جز جز این کتاب را بلدم"
    }
    if (wordRepetitionOf("کل", tokens, position)) {
      return true;   // "با من کل کل نکن"
    }
    return false;
  }

  private boolean wordRepetitionOf(String word, AnalyzedTokenReadings[] tokens, int position) {
    return position > 0 && tokens[position - 1].getToken().equals(word) && tokens[position].getToken().equals(word);
  }

}