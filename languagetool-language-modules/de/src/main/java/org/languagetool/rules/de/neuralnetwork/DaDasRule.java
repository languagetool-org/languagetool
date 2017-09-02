package org.languagetool.rules.de.neuralnetwork;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class DaDasRule extends Rule {

    private final IClassifier classifier;
    private final List<String> subjects = Arrays.asList("da", "das");

    public DaDasRule(ResourceBundle messages) {
        super.setCategory(Categories.TYPOS.getCategory(messages));
        addExamplePair(Example.wrong("Danke für <marker>da</marker> Angebot."),
                Example.fixed("Danke für <marker>das</marker> Angebot."));
        addExamplePair(Example.wrong("Wie kommt man denn <marker>das</marker> hin?"),
                Example.fixed("Wie kommt man denn <marker>da</marker> hin?"));

        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        final InputStream dictionaryPath = dataBroker.getFromResourceDirAsStream("/de/dictionary.txt");
        final InputStream embeddingsPath = dataBroker.getFromResourceDirAsStream("/de/final_embeddings.txt");
        final InputStream WPath = dataBroker.getFromResourceDirAsStream("/de/W_fc1.txt");
        final InputStream bPath = dataBroker.getFromResourceDirAsStream("/de/b_fc1.txt");
        classifier = new Classifier(dictionaryPath, embeddingsPath, WPath, bPath);
    }

    @Override
    public String getId() {
        return "DE_DA_VS_DAS";
    }

    @Override
    public String getDescription() {
        return "Möglicher Tippfehler 'da/das'";
    }

    private Suggestion getSuggestion(double[] y) {
        final double DELTA = 1;
        final double MIN_SCORE = .5;
        String suggestion = subjects.get(y[0] > y[1] ? 0 : 1);
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
        for(int i = 2; i < tokens.length - 2; i++) { // TODO CONTEXT_LENGTH
            String token = tokens[i].getToken();
            if(subjects.contains(token)) {
                final String[] context = new String[]{tokens[i-2].getToken(), tokens[i-1].getToken(), tokens[i+1].getToken(), tokens[i+2].getToken()};
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
    private RuleMatch createRuleMatch(AnalyzedTokenReadings token, Suggestion suggestion, double[] y) {
        String msg = "Possible confusion of " + subjects.get(0) + " and " + subjects.get(1) + ". " + certaintiesToString(y);
        if(suggestion.isUnsure()) {
            msg = "(Low certainty) " + msg;
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