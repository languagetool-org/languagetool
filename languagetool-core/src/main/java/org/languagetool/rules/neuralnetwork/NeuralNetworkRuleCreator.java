/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Markus Brenneis
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
package org.languagetool.rules.neuralnetwork;

import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.ScoredConfusionSet;
import org.languagetool.rules.ScoredConfusionSetLoader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public abstract class NeuralNetworkRuleCreator {

  private static final String CONFUSION_SET_FILENAME = "neuralnetwork/confusion_sets.txt";

  private NeuralNetworkRuleCreator() {}

  public static List<Rule> createRules(ResourceBundle messages, Language language, Word2VecModel word2vecModel) throws IOException {
    List<ScoredConfusionSet> confusionSets;
    try(InputStream confusionSetsStream = new FileInputStream(word2vecModel.getPath() + File.separator + CONFUSION_SET_FILENAME)) {
      confusionSets = ScoredConfusionSetLoader.loadConfusionSet(confusionSetsStream);
    } catch (FileNotFoundException e) {
      System.err.println("Warning: " + CONFUSION_SET_FILENAME + " not found for " + language.getShortCode());
      return new ArrayList<>(0);
    }
    List<Rule> neuralNetworkRules = new ArrayList<>();
    for(ScoredConfusionSet confusionSet : confusionSets) {
      neuralNetworkRules.add(new NeuralNetworkRule(messages, language, confusionSet, word2vecModel));
    }
    return neuralNetworkRules;
  }

}

