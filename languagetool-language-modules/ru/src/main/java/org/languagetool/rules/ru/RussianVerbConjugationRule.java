/* LanguageTool, a natural language style checker
 * Copyright (C) 2010 Daniel Naber (http://www.languagetool.org)
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
package org.languagetool.rules.ru;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RussianVerbConjugationRule extends Rule {

    private static final Pattern PRONOUN = Pattern.compile("PNN:(.*):Nom:(.*)");
    private static final Pattern FUT_REAL_VERB = Pattern.compile("VB:(Fut|Real):(.*):(.*):(.*):(.*)");
    private static final Pattern PAST_VERB = Pattern.compile("VB:Past:(.*):(.*):(.*)");
    
    public RussianVerbConjugationRule(ResourceBundle messages) {
        super(messages);
        super.setCategory(Categories.GRAMMAR.getCategory(messages));
        addExamplePair(Example.wrong("<marker>Я идёт</marker>."),
        Example.fixed("<marker>Я иду</marker>."));
    }

    @Override
    public String getId() {
        return "RU_VERB_CONJUGATION";
    }

    @Override
    public String getDescription() {
        return "Согласование личных местоимений с глаголами";
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        List<RuleMatch> ruleMatches = new ArrayList<>();
        AnalyzedTokenReadings[] tokenReadings = sentence.getTokensWithoutWhitespace();
        for (int i = 1; i < tokenReadings.length - 1; i++) {
	    AnalyzedTokenReadings previousReading = tokenReadings[i-1];	
            AnalyzedTokenReadings currentReading = tokenReadings[i];
            AnalyzedTokenReadings nextReading = tokenReadings[i + 1];
	    AnalyzedToken previousLemmaTok = previousReading.getReadings().get(0);
            AnalyzedToken currentLemmaTok = currentReading.getReadings().get(0);
            String previousToken = previousLemmaTok.getToken();
	    String currentToken = currentLemmaTok.getToken();
            String currentPosTag = currentLemmaTok.getPOSTag();
            if (currentToken != null && currentPosTag != null && !currentToken.isEmpty() && !currentPosTag.isEmpty()) {
                Matcher pronounMatcher = PRONOUN.matcher(currentPosTag);
                if ((pronounMatcher.find()) && !(previousToken.equals("и")))  {
                    Pair<String, String> pronounPair = new ImmutablePair<>(pronounMatcher.group(1), pronounMatcher.group(2));
                    AnalyzedToken nextLemmaTok = nextReading.getReadings().get(0);
                    String next2Token;
                    if (i < tokenReadings.length - 2) {
                    AnalyzedTokenReadings next2Reading = tokenReadings[i + 2];
                    AnalyzedToken next2LemmaTok = next2Reading.getReadings().get(0);
                    next2Token = next2LemmaTok.getToken();
                    } else  {
                            next2Token = "";
                            }
                    String nextToken = nextLemmaTok.getToken();
                    String nextPosTag = nextLemmaTok.getPOSTag();
                    if(nextPosTag != null && !(nextPosTag.isEmpty()) && !(next2Token.equals("быть")) && !(nextToken.equals("целую")) ) {  //  "может быть"
                        Matcher verbMatcher = FUT_REAL_VERB.matcher(nextPosTag);
                        if (verbMatcher.find()) {
                            Pair<String, String> verbPair = new ImmutablePair<>(verbMatcher.group(4), verbMatcher.group(5));
                            if (isConjugationInPresentOrFutureWrong(pronounPair, verbPair)) {
                                addRuleMatch(ruleMatches, currentReading, nextReading, sentence);
                            }
                        } else {
                            verbMatcher = PAST_VERB.matcher(nextPosTag);
                            if (verbMatcher.find()) {
                                if (isConjugationInPastWrong(pronounMatcher.group(1), verbMatcher.group(3))) {
                                    addRuleMatch(ruleMatches, currentReading, nextReading, sentence);
                                }
                            }
                        }
                    }
                }
            }
        }
        return toRuleMatchArray(ruleMatches);
    }

    private boolean isConjugationInPresentOrFutureWrong(Pair<String, String> pronoun, Pair<String, String> verb) {
        if (!pronoun.getRight().equals(verb.getRight())) {
            return true;
        }
        if (Arrays.asList("Masc", "Fem", "Neut").contains(pronoun.getLeft())) {
            return "PL".equals(verb.getLeft());
        }
        return !pronoun.getLeft().equals(verb.getLeft());
    }

    private boolean isConjugationInPastWrong(String pronoun, String verb) {
        if ("Sin".equals(pronoun)) {
            return "PL".equals(verb) || "Neut".equals(verb);
        }
        return !pronoun.equals(verb);
    }

    private void addRuleMatch(List<RuleMatch> ruleMatches, AnalyzedTokenReadings currentReading, AnalyzedTokenReadings nextReading, AnalyzedSentence sentence) {
        RuleMatch ruleMatch = new RuleMatch(this, sentence, currentReading.getStartPos(), nextReading.getEndPos(), "Неверное спряжение глагола или неверное местоимение", getShort());
        ruleMatches.add(ruleMatch);
    }

    protected String getShort() {
        return "Неверное спряжение глагола";
    }
}

