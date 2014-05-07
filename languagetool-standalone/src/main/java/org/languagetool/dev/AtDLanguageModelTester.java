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
package org.languagetool.dev;

import org.dashnine.preditor.LanguageModel;
import sleep.runtime.Scalar;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Try the AtD language model.
 */
class AtDLanguageModelTester {

  private void run() throws IOException, ClassNotFoundException {
    String file = "/prg/atd/models/model.bin";   // After the Deadline's language model file
    System.out.println("Loading language model (may take some time)...");
    try(FileInputStream fis = new FileInputStream(file)) {
      ObjectInputStream oos = new ObjectInputStream(fis);
      Object c = oos.readObject();
      Scalar s = (Scalar)c;
      
      LanguageModel lm = (LanguageModel) s.objectValue();
      printProbability(lm, "a", "house");
      printProbability(lm, "an", "house");
    }
  }

  private void printProbability(LanguageModel lm, String previous, String word) {
    System.out.println(previous + " " + word + " => " + lm.Pbigram1(previous, word));
  }

  public static void main(String[] args) throws Exception {
    AtDLanguageModelTester importer = new AtDLanguageModelTester();
    importer.run();
  }
}
