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

import org.encog.ml.data.MLData;
import org.encog.ml.data.basic.BasicMLData;
import org.encog.neural.networks.BasicNetwork;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static junit.framework.TestCase.assertTrue;

public class MachineLearningTest {
  
  @Test
  public void test() throws IOException {
    MachineLearning ml = new MachineLearning();
    // XOR:
    ml.addData(0, 0, 0, 0);
    ml.addData(1, 1, 0, 0);
    ml.addData(1, 0, 1, 0);
    ml.addData(0, 1, 1, 0);
    File tempFile = File.createTempFile(MachineLearningTest.class.getSimpleName(), ".tmp");
    try {
      ml.train(tempFile);
      assertTrue(tempFile.exists());
      assertTrue(tempFile.length() > 200);
      BasicNetwork loadedNet = (BasicNetwork) ml.load(tempFile);
      assertTrue(compute(loadedNet, 0, 0).getData(0) < 0.5);
      assertTrue(compute(loadedNet, 1, 0).getData(0) > 0.5);
      assertTrue(compute(loadedNet, 0, 1).getData(0) > 0.5);
      assertTrue(compute(loadedNet, 1, 1).getData(0) < 0.5);
    } finally {
      tempFile.delete();
    }
  }

  private MLData compute(BasicNetwork loadedNet, double val1, double val2) {
    return loadedNet.compute(new BasicMLData(new double[]{val1, val2, 0}));
  }
}
