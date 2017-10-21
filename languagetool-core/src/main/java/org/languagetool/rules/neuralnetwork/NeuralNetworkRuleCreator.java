package org.languagetool.rules.neuralnetwork;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.rules.Rule;
import org.languagetool.rules.ScoredConfusionSet;
import org.languagetool.rules.ScoredConfusionSetLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public abstract class NeuralNetworkRuleCreator {
  private NeuralNetworkRuleCreator() {}

  private static final String CONFUSION_SET_FILENAME = "neuralnetwork_confusion_sets.txt";

  public static List<Rule> createRules(ResourceBundle messages, Language language, Word2VecModel word2vecModel) {
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    InputStream confusionSetsStream = dataBroker.getFromResourceDirAsStream("/" + language.getShortCode() + "/" + CONFUSION_SET_FILENAME);
    File weightsDirectory = word2vecModel.getWeightsDirectory();

    List<ScoredConfusionSet> confusionSets;
    try {
      confusionSets = ScoredConfusionSetLoader.loadConfusionSet(confusionSetsStream);
    } catch (IOException e) {
      System.err.println("Error: " + CONFUSION_SET_FILENAME + " not found for " + language.getShortCode() + ".");
      return new ArrayList<>(0);
    }

    List<Rule> neuralNetworkRules = new ArrayList<>();
    for(ScoredConfusionSet confusionSet : confusionSets) {
      try {
        neuralNetworkRules.add(new NeuralNetworkRule(messages, language, confusionSet, weightsDirectory, word2vecModel.getDictionary(), word2vecModel.getEmbedding()));
      } catch (FileNotFoundException e) {
        System.err.println("Error while creating NeuralNetworkRule: " + e.getLocalizedMessage());
      }
    }

    return neuralNetworkRules;
  }
}

