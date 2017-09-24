package org.languagetool.rules.de.neuralnetwork;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public abstract class GermanNeuralNetworkRule extends Rule {
    private static final int CONTEXT_LENGTH = 5;

    IClassifier classifier;

    protected static Dictionary dictionary;
    protected static Matrix embedding;

    static {
        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream dictionaryPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/dictionary.txt");
        dictionary = new Dictionary(dictionaryPath);
        final InputStream embeddingsPath = dataBroker.getFromResourceDirAsStream("/de/neuralnetwork/final_embeddings.txt");
        embedding = new Matrix(embeddingsPath);
    }

    protected GermanNeuralNetworkRule(ResourceBundle messages) {
        super.setCategory(Categories.TYPOS.getCategory(messages));
    }

    public abstract List<String> getSubjects();

    protected abstract double getMinScore();

    public abstract void setMinScore(double minScore);

    @Override
    public String getDescription() {
        return "Möglicher Tippfehler '" + getSubjects().get(0) + "'/'" + getSubjects().get(1) + "'";
    }

    private Suggestion getSuggestion(double[] y) {
        String suggestion;
        boolean unsure;
        if(y[0] > y[1]) {
            suggestion = getSubjects().get(0);
            unsure = !(y[0] > getMinScore() && y[1] < -getMinScore());
        } else {
            suggestion = getSubjects().get(1);
            unsure = !(y[1] > getMinScore() && y[0] < -getMinScore());
        }
        return new Suggestion(suggestion, unsure);
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
                    if (!suggestion.isUnsure()) {
                        ruleMatches.add(createRuleMatch(tokens[i], suggestion, y));
                    } else {
                        System.out.println("unsure: " + getMessage(suggestion, y) + Arrays.toString(context));
                    }
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
        String msg = getMessage(suggestion, y);
        int pos = token.getStartPos();
        RuleMatch ruleMatch = new RuleMatch(this, pos, pos + token.getToken().length(), msg);
        ruleMatch.setSuggestedReplacement(suggestion.toString());
        return ruleMatch;
    }

    @NotNull
    private String getMessage(Suggestion suggestion, double[] y) {
        String msg = "Mögliche Verwechslung von '" + getSubjects().get(0) + "' und '" + getSubjects().get(1) + "'. " + certaintiesToString(y);
        if(suggestion.isUnsure()) {
            msg = "(Geringe Sicherheit) " + msg;
        }
        return msg;
    }

    private String certaintiesToString(double[] y) {
        return String.format("[%4.2f, %4.2f]", y[0], y[1]);
    }
}
