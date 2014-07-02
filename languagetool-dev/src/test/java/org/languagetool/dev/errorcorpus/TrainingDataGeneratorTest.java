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
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataPair;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.junit.Ignore;
import org.junit.Test;

public class TrainingDataGeneratorTest {
  
  @Test
  @Ignore("Interactive use only")
  public void testEncog() {

    double[][] XOR_INPUT = { { 0.0, 0.0 }, { 1.0, 0.0 }, { 0.0, 1.0 }, { 1.0, 1.0 } };
    double[][] XOR_IDEAL = { { 0.0 }, { 1.0 }, { 1.0 }, { 0.0 } };
    
    BasicNetwork network = new BasicNetwork();
    network.addLayer(new BasicLayer(null,true,2));
    network.addLayer(new BasicLayer(new ActivationSigmoid(),true,3));
    network.addLayer(new BasicLayer(new ActivationSigmoid(),false,1));
    network.getStructure().finalizeStructure();
    network.reset();

    MLDataSet trainingSet = new BasicMLDataSet(XOR_INPUT, XOR_IDEAL);

    final ResilientPropagation train = new ResilientPropagation(network, trainingSet);
    int epoch = 1;
    do {
      train.iteration();
      System.out.println("Epoch #" + epoch + " Error:" + train.getError());
      epoch++;
    } while(train.getError() > 0.01);
    train.finishTraining();
    
    System.out.println("dump:");
    System.out.println(network.dumpWeights());
    System.out.println("-----");
    
    // test the neural network:
    System.out.println("Neural Network Results:");
    for(MLDataPair pair: trainingSet ) {
      final MLData output = network.compute(pair.getInput());
      System.out.println(pair.getInput().getData(0) + "," + pair.getInput().getData(1)
              + ", actual=" + output.getData(0) + ",ideal=" + pair.getIdeal().getData(0));
    }

    Encog.getInstance().shutdown();
  }

}
