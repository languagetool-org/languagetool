package org.languagetool.rules.neuralnetwork;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ScoredConfusionSet;
import org.languagetool.tools.Tools;

import java.io.*;
import java.util.*;

public class NeuralNetworkRule extends Rule {
  private final List<String> subjects;
  private final List<Optional<String>> descriptions;
  private double minScore;

  private static final int CONTEXT_LENGTH = 5;
  protected IClassifier classifier;

  private final String id;

  public NeuralNetworkRule(ResourceBundle messages, Language language, ScoredConfusionSet confusionSet, File weightsDirectory, Dictionary dictionary, Matrix embedding) throws FileNotFoundException {
    super(messages);
    super.setCategory(Categories.TYPOS.getCategory(messages));

    this.subjects = confusionSet.getConfusionTokens();
    this.descriptions = confusionSet.getTokenDescriptions();
    this.minScore = confusionSet.getScore();

    final InputStream WPath = new FileInputStream(pathFor(weightsDirectory, "W_fc1.txt"));
    final InputStream bPath = new FileInputStream(pathFor(weightsDirectory, "b_fc1.txt"));
    classifier = new Classifier(dictionary, embedding, WPath, bPath);

    this.id = language.getShortCode().toUpperCase() + "_" + subjects.get(0).toUpperCase() + "_VS_" + subjects.get(1).toUpperCase() + "_NEURALNETWORK";
  }

  public NeuralNetworkRule(ResourceBundle messages, Language language, ScoredConfusionSet confusionSet, IClassifier classifier) {
    super(messages);
    super.setCategory(Categories.TYPOS.getCategory(messages));

    this.subjects = confusionSet.getConfusionTokens();
    this.descriptions = confusionSet.getTokenDescriptions();
    this.minScore = confusionSet.getScore();

    this.classifier = classifier;

    this.id = language.getShortCode().toUpperCase() + "_" + subjects.get(0).toUpperCase() + "_VS_" + subjects.get(1).toUpperCase() + "_NEURALNETWORK";
  }

  private String pathFor(File weightsDirectory, String filename) {
    String folderName = String.join("_", subjects);
    return weightsDirectory.getAbsolutePath() + File.separator + folderName + File.separator + filename;
  }

  public List<String> getSubjects() {
    return subjects;
  }

  protected double getMinScore() {
    return minScore;
  }

  public void setMinScore(double minScore) {
    this.minScore = minScore;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getDescription() {
    // TODO use resource
    return "Possible Typo '" + getSubjects().get(0) + "'/'" + getSubjects().get(1) + "'";
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
    String msg;
    if (descriptions.get(0).isPresent() && descriptions.get(1).isPresent()) {
      msg = Tools.i18n(messages, "neural_network_suggest_with_description", subjects.get(0), descriptions.get(0).get(), subjects.get(1), descriptions.get(1).get());
    } else {
      msg = Tools.i18n(messages, "neural_network_suggest", subjects.get(0), subjects.get(1));
    }
    if(suggestion.isUnsure()) {
      msg = "(low certainty) " + msg;
    }
    return msg + " " + certaintiesToString(y);
  }

  private String certaintiesToString(double[] y) {
    return String.format("[%4.2f, %4.2f]", y[0], y[1]);
  }
}
