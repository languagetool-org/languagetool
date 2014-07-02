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

import org.encog.Encog;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.BasicML;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.PersistBasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.persist.EncogPersistor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @since 2.7
 */
class MachineLearning implements AutoCloseable {
  
  private static final double MAX_ERROR = 0.01;
  private static final double MAX_ITERATIONS = 10_000;

  private final MLDataSet trainingSet = new BasicMLDataSet();
  

  void addData(double idealValue, double... input) {
    BasicMLData idealData = new BasicMLData(new double[] {idealValue});
    trainingSet.add(new BasicMLData(input), idealData);
  }
  
  void train(File outputFile) throws IOException {
    BasicNetwork network = new BasicNetwork();
    network.addLayer(new BasicLayer(null, true, 2));
    network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 3));
    network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 1));
    network.getStructure().finalizeStructure();
    network.reset();

    ResilientPropagation train = new ResilientPropagation(network, trainingSet);
    int epoch = 1;
    do {
      train.iteration();
      if (epoch % 100 == 0) {
        System.out.println("Epoch #" + epoch + " Error:" + train.getError());
      }
      epoch++;
      if (epoch >= MAX_ITERATIONS) {
        System.err.println("Warn: maximum iterations (" + MAX_ITERATIONS + ") reached, stopping training");
        break;
      }
    } while (train.getError() > MAX_ERROR);
    train.finishTraining();

    EncogPersistor persistor = new PersistBasicNetwork();
    try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
      persistor.save(outputStream, network);
    }
  }

  BasicML load(File inputFile) throws IOException {
    EncogPersistor persistor = new PersistBasicNetwork();
    try (FileInputStream inputStream = new FileInputStream(inputFile)) {
      Object read = persistor.read(inputStream);
      return (BasicML)read;
    }
  }

  @Override
  public void close() {
    Encog.getInstance().shutdown();
  }
}
