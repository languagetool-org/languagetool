package org.languagetool.rules.neuralnetwork;

import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.ScoredConfusionSet;
import org.languagetool.rules.ScoredConfusionSetLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public abstract class NeuralNetworkRuleCreator {
  private NeuralNetworkRuleCreator() {}

  private static final String CONFUSION_SET_FILENAME = "neuralnetwork/confusion_sets.txt";

  public static List<Rule> createRules(ResourceBundle messages, Language language, Word2VecModel word2vecModel) {
    List<ScoredConfusionSet> confusionSets;
    try(InputStream confusionSetsStream = new FileInputStream(word2vecModel.getPath() + File.separator + CONFUSION_SET_FILENAME)) {
      confusionSets = ScoredConfusionSetLoader.loadConfusionSet(confusionSetsStream);
    } catch (IOException e) {
      System.err.println("Error: " + CONFUSION_SET_FILENAME + " not found for " + language.getShortCode() + ".");
      return new ArrayList<>(0);
    }

    List<Rule> neuralNetworkRules = new ArrayList<>();
    for(ScoredConfusionSet confusionSet : confusionSets) {
      neuralNetworkRules.add(new NeuralNetworkRule(messages, language, confusionSet, word2vecModel.getDictionary(), word2vecModel.getEmbedding()));
    }

    return neuralNetworkRules;
  }
}

