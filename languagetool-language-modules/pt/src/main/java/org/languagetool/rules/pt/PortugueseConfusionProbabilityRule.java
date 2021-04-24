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
package org.languagetool.rules.pt;

import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.ngrams.ConfusionProbabilityRule;

//import org.languagetool.rules.Categories;

import java.util.ResourceBundle;

/**
 * @since 3.6
 */
public class PortugueseConfusionProbabilityRule extends ConfusionProbabilityRule {

  public PortugueseConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    super(messages, languageModel, language);
    //super.setCategory(Categories.CONFUSED_WORDS.getCategory(messages));
    setDefaultOff();  
   /* Leave setDefaultOff() on Portuguese ngram rules. 
    * They have low priority for Portuguese because:
    * - they are not available for all users;
    * - there is no publicly available ngram data via LanguageTool (2016-12)
    *
    * addExamplePair(Example.wrong("Essa palavra tem <marker>assento</marker>."),
    *               Example.fixed("Essa palavra tem <marker>acento</marker>."));
    */
  }

}
