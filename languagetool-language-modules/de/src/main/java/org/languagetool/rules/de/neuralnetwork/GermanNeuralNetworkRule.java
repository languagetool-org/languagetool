package org.languagetool.rules.de.neuralnetwork;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.*;

abstract class GermanNeuralNetworkRule extends Rule {
    private static final int CONTEXT_LENGTH = 5;
    private static final double DELTA = 1;
    private static final double MIN_SCORE = .5;

    IClassifier classifier;

    public GermanNeuralNetworkRule(ResourceBundle messages) {
        super.setCategory(Categories.TYPOS.getCategory(messages));
    }

    abstract List<String> getSubjects();

    @Override
    public String getDescription() {
        return "Möglicher Tippfehler '" + getSubjects().get(0) + "'/'" + getSubjects().get(1) + "'";
    }

    private Suggestion getSuggestion(double[] y) {
        String suggestion = getSubjects().get(y[0] > y[1] ? 0 : 1);
        if(Math.abs(y[0] - y[1]) < DELTA || Math.max(y[0], y[1]) < MIN_SCORE) {
            return new Suggestion(suggestion, true);
        } else {
            return new Suggestion(suggestion, false);
        }
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        List<RuleMatch> ruleMatches = new ArrayList<>();
        AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
        for(int i = CONTEXT_LENGTH/2; i < tokens.length - CONTEXT_LENGTH/2; i++) {
            String token = tokens[i].getToken();
            if(getSubjects().contains(token)) {
                final String[] context = getContext(tokens, i);
                final double[] y = classifier.getScores(context);
                final Suggestion suggestion = getSuggestion(y);
                if(!suggestion.matches(token)) {
                    ruleMatches.add(createRuleMatch(tokens[i], suggestion, y));
                }
            }
        }
        return toRuleMatchArray(ruleMatches);
    }

    @NotNull
    private String[] getContext(AnalyzedTokenReadings[] tokens, int center) {
        String[] context = new String[CONTEXT_LENGTH - 1];
        for(int i = 0; i < CONTEXT_LENGTH/2; i++) {
            context[i] = tokens[center - CONTEXT_LENGTH/2 + i].getToken();
        }
        for(int i = 0; i < CONTEXT_LENGTH/2; i++) {
            context[CONTEXT_LENGTH/2 + i] = tokens[center + 1 + i].getToken();
        }
        return context;
    }

    @NotNull
    private RuleMatch createRuleMatch(AnalyzedTokenReadings token, Suggestion suggestion, double[] y) {
        String msg = "Mögliche Verwechslung von '" + getSubjects().get(0) + "' und '" + getSubjects().get(1) + "'. " + certaintiesToString(y);
        if(suggestion.isUnsure()) {
            msg = "(Geringe Sicherheit) " + msg;
        }
        int pos = token.getStartPos();
        RuleMatch ruleMatch = new RuleMatch(this, pos, pos + token.getToken().length(), msg);
        ruleMatch.setSuggestedReplacement(suggestion.toString());
        return ruleMatch;
    }

    private String certaintiesToString(double[] y) {
        return String.format("[%4.2f, %4.2f]", y[0], y[1]);
    }
}