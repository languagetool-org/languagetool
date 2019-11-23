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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Word2VecModel {

  private final Embedding embedding;
  private final File path;

  public Word2VecModel(String path) throws FileNotFoundException {
    Dictionary dictionary = new org.languagetool.rules.neuralnetwork.Dictionary(new FileInputStream(path + File.separator + "dictionary.txt"));
    Matrix embedding = new Matrix(new FileInputStream(path + File.separator + "final_embeddings.txt"));
    this.embedding = new Embedding(dictionary, embedding);
    this.path = new File(path);
  }

  public Embedding getEmbedding() {
    return embedding;
  }

  public File getPath() {
    return path;
  }

}
