/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.errorcorpus;

import org.junit.Ignore;
import org.junit.Test;
import org.neuroph.core.NeuralNetwork;
import org.neuroph.core.learning.DataSet;
import org.neuroph.core.learning.DataSetRow;
import org.neuroph.nnet.Hopfield;
import org.neuroph.nnet.Perceptron;

import java.util.Arrays;

public class TrainingDataGeneratorTest {
  
  @Test
  @Ignore("Interactive use only")
  public void testNeuroph() {
    NeuralNetwork neuralNetwork = new Perceptron(2, 1);
    DataSet trainingSet = new DataSet(2, 1);
    trainingSet.addRow(new DataSetRow(new double[]{0, 0}, new double[]{0}));
    trainingSet.addRow(new DataSetRow(new double[]{0, 1}, new double[]{1}));
    trainingSet.addRow(new DataSetRow(new double[]{1, 0}, new double[]{1}));
    trainingSet.addRow(new DataSetRow(new double[]{1, 1}, new double[]{1}));
    neuralNetwork.learn(trainingSet);
    neuralNetwork.save("/tmp/or_perceptron.nnet");

    NeuralNetwork neuralNetwork2 = NeuralNetwork.load("/tmp/or_perceptron.nnet");
    //neuralNetwork2.setInput(1, 1); // 1.0
    //neuralNetwork2.setInput(0, 0); // 0.0
    neuralNetwork2.setInput(1, 0); // 1.0 
    neuralNetwork2.calculate();
    double[] networkOutput = neuralNetwork2.getOutput();
    System.out.println(Arrays.toString(networkOutput));
  }

  @Test
  @Ignore("Interactive use only")
  public void testNeuroph2() {
    NeuralNetwork neuralNetwork = new Perceptron(2, 1);
    //NeuralNetwork neuralNetwork = new Hopfield(2);
    DataSet trainingSet = new DataSet(2, 1);
    float max = 4546;
    trainingSet.addRow(new DataSetRow(new double[]{235/max, 0}, new double[]{0}));
    trainingSet.addRow(new DataSetRow(new double[]{4546/max, 263/max}, new double[]{1}));
    //trainingSet.addRow(new DataSetRow(new double[]{1, 0}, new double[]{1}));
    //trainingSet.addRow(new DataSetRow(new double[]{1, 1}, new double[]{1}));
    neuralNetwork.learn(trainingSet);
    neuralNetwork.save("/tmp/or_perceptron.nnet");
  }
}
