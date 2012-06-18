/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.spelling.morfologik;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Category;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tools.StringTools;

import morfologik.speller.Speller;
import morfologik.stemming.Dictionary;

public abstract class MorfologikSpellerRule extends SpellingCheckRule {

    private Speller speller;
    
    private Locale conversionLocale = Locale.getDefault();
    
    /**
     * Get the filename, e.g., <tt>/resource/pl/spelling.dict</tt>.
     */
    public abstract String getFileName();
    
    public static final String RULE_ID = "MORFOLOGIK_RULE";
    
    public MorfologikSpellerRule(ResourceBundle messages, Language language) {
        super(messages, language);
        super.setCategory(new Category(messages.getString("category_typo")));
    }

    @Override
    public String getId() {
        return RULE_ID;
    }

    @Override
    public String getDescription() {
        return messages.getString("desc_spelling");
    }
    
    public void setLocale(Locale locale) {
        conversionLocale = locale;
      }
    
    @Override
    public RuleMatch[] match(AnalyzedSentence text) throws IOException {
        
        final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
        final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
        //lazy init
        if (speller == null) {                                   
            if (JLanguageTool.getDataBroker().resourceExists(getFileName())) {
                final URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(getFileName());
                speller = new Speller(Dictionary.read(url));
            } else {
                return toRuleMatchArray(ruleMatches);
            }
        }
        for (AnalyzedTokenReadings token : tokens) {
            final String word = token.getToken();
            boolean isAlphabetic = true;
            if (word.length() == 1) { // dictionaries usually do not contain punctuation               
                isAlphabetic = StringTools.isAlphabetic(word.charAt(0));
              }              
            if (word.length() > 0 && isAlphabetic && !token.isImmunized()
                    && !speller.isInDictionary(word)
                    && !speller.isInDictionary(word.toLowerCase(conversionLocale))) {
                final List<String> suggestions = new ArrayList<String>();                
                suggestions.addAll(speller.findReplacements(word));
                if (!word.toLowerCase(conversionLocale).equals(word)) {
                    suggestions.addAll(speller.findReplacements(word.toLowerCase(conversionLocale)));
                }
                suggestions.addAll(speller.replaceRunOnWords(word));
                final RuleMatch ruleMatch = new RuleMatch(this, 
                        token.getStartPos(), token.getStartPos() + word.length(),
                        messages.getString("spelling"),
                        messages.getString("desc_spelling_short"));
                if (!suggestions.isEmpty()) {
                    ruleMatch.setSuggestedReplacements(suggestions);
                }
                ruleMatches.add(ruleMatch);
            }
        }
        return toRuleMatchArray(ruleMatches);
    }

}
