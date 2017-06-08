package org.languagetool.rules.ru;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zaets on 25.05.2017.
 */
public class RussianVerbConjugationRule extends Rule {

    private static final Pattern PRONOUN = Pattern.compile("PNN:(.*):Nom:(.*)");
    private static final Pattern FUT_REAL_VERB = Pattern.compile("VB:(Fut|Real):(.*):(.*)");
    private static final Pattern PAST_VERB = Pattern.compile("VB:Past:(.*)");

    public RussianVerbConjugationRule(ResourceBundle messages) {
        super(messages);
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
            AnalyzedTokenReadings currentReading = tokenReadings[i];
            AnalyzedTokenReadings nextReading = tokenReadings[i + 1];
            AnalyzedToken currentLemmaTok = currentReading.getReadings().get(0);
            String currentToken = currentLemmaTok.getToken();
            String currentPosTag = currentLemmaTok.getPOSTag();
            if (currentToken != null && currentPosTag != null && !currentToken.isEmpty() && !currentPosTag.isEmpty()) {
                Matcher pronounMatcher = PRONOUN.matcher(currentPosTag);
                if (pronounMatcher.find()) {
                    Pair<String, String> pronounPair = new ImmutablePair<>(pronounMatcher.group(1), pronounMatcher.group(2));
                    AnalyzedToken nextLemmaTok = nextReading.getReadings().get(0);
                    String nextPosTag = nextLemmaTok.getPOSTag();
                    if(nextPosTag != null && !nextPosTag.isEmpty()) {
                        Matcher verbMatcher = FUT_REAL_VERB.matcher(nextPosTag);
                        if (verbMatcher.find()) {
                            Pair<String, String> verbPair = new ImmutablePair<>(verbMatcher.group(2), verbMatcher.group(3));
                            if (isConjugationInPresentOrFutureWrong(pronounPair, verbPair)) {
                                addRuleMatch(ruleMatches, currentReading, nextReading);
                            }
                        } else {
                            verbMatcher = PAST_VERB.matcher(nextPosTag);
                            if (verbMatcher.find()) {
                                if (isConjugationInPastWrong(pronounMatcher.group(1), verbMatcher.group(1))) {
                                    addRuleMatch(ruleMatches, currentReading, nextReading);
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

    private void addRuleMatch(List<RuleMatch> ruleMatches, AnalyzedTokenReadings currentReading, AnalyzedTokenReadings nextReading) {
        RuleMatch ruleMatch = new RuleMatch(this, currentReading.getStartPos(), nextReading.getEndPos(), "Неверное спряжение глагола или неверное местоимение", getShort());
        ruleMatches.add(ruleMatch);
    }

    protected String getShort() {
        return "Неверное спряжение глагола";
    }
}

