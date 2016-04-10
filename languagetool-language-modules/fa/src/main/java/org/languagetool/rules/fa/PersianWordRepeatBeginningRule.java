/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Ebrahim Byagowi <ebrahim@gnu.org>
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

import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordRepeatBeginningRule;

/**
 * List of Persian adverbs for WordRepeatBeginningRule
 * 
 * @author Ebrahim Byagowi
 * @since 2.7
 */
public class PersianWordRepeatBeginningRule extends WordRepeatBeginningRule {

  private static final Set<String> ADVERBS = new HashSet<>();
  static {
    ADVERBS.add("هم");
    ADVERBS.add("همچنین");
    ADVERBS.add("نیز");
    ADVERBS.add("از یک سو");
    ADVERBS.add("از یک طرف");
    ADVERBS.add("از طرف ديگر");
    ADVERBS.add("بنابراین");
    ADVERBS.add("حتی");
    ADVERBS.add("چنانچه");
  }
  
  public PersianWordRepeatBeginningRule(ResourceBundle messages, Language language) {
    super(messages, language);
    addExamplePair(Example.wrong("همچنین، خیابان تقریباً کاملاً مسکونی است. <marker>همچنین</marker>، به افتخار یک شاعر نامگذاری شده‌است."),
                   Example.fixed("همچنین، خیابان تقریباً مسکونی است. <marker>این خیابان</marker> به افتخار یک شاعر نامگذاری شده‌است."));
  }
  
  @Override
  public String getId() {
    return "PERSIAN_WORD_REPEAT_BEGINNING_RULE";
  }
  
  @Override
  protected boolean isAdverb(AnalyzedTokenReadings token) {
    return ADVERBS.contains(token.getToken());
  }

}
