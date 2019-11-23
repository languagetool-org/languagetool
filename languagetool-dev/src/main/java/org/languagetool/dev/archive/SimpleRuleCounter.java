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
package org.languagetool.dev.archive;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
//import org.languagetool.language.English;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternToken;

import java.util.ArrayList;
//import java.util.Collections;
import java.util.List;

import static java.util.Comparator.comparing;

/**
 * Count "simple" rules, i.e. those that use only 'regex', 'case_sensitive'
 * and 'skip="-1"' attributes.
 */
public class SimpleRuleCounter {

    private void run(List<Language> languages) {
        List<Language> sortedLanguages = new ArrayList<>(languages);
        sortedLanguages.sort(comparing(Language::getName));
        for (Language language : sortedLanguages) {
            if (language.isVariant()) {
                continue;
            }
            JLanguageTool lt = new JLanguageTool(language);
            List<Rule> allRules = lt.getAllActiveRules();
            countForLanguage(allRules, language);
        }
    }

    private void countForLanguage(List<Rule> allRules, Language language) {
        int simpleCount = 0;
        for (Rule rule : allRules) {
            boolean isSimple = true;
            if (rule instanceof PatternRule) {
                PatternRule patternRule = (PatternRule) rule;
                List<PatternToken> tokens = patternRule.getPatternTokens();
                for (PatternToken token : tokens) {
                    if (!isSimple(token)) {
                        isSimple = false;
                        break;
                    }
                }
                if (isSimple) {
                    simpleCount++;
                    //System.out.println("Simple: " + patternRule.getId());
                    //System.out.println(patternRule.toXML());
                    //System.out.println("-------------------------");
                }
            }
        }
        float percent = (float)simpleCount / allRules.size() * 100;
        //System.out.printf(simpleCount + "/" + allRules.size() + " = %.0f%% for " + language + "\n", percent);
        System.out.printf("%.0f%% for " + language + "\n", percent);
    }

    private boolean isSimple(PatternToken t) {
        return !(t.getNegation() || t.getPOSNegation() || t.hasAndGroup() || t.hasExceptionList() || 
                 t.hasNextException() || t.hasOrGroup() || t.isInflected() || t.isPOStagRegularExpression() ||
                 t.getPOStag() != null || t.isReferenceElement() || t.isSentenceStart() ||
                 t.getSkipNext() != 0);
    }

    public static void main(String[] args) {
        SimpleRuleCounter finder = new SimpleRuleCounter();
        finder.run(Languages.get());
        //finder.run(Collections.singletonList(new GermanyGerman()));
        //finder.run(Collections.singletonList(new English()));
    }
}
