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

import java.io.InputStream;

public class SingleLayerClassifier implements Classifier {

  private final Embedding embedding;
  private final Matrix W_fc1;
  private final Matrix b_fc1;

  public SingleLayerClassifier(Embedding embedding, InputStream WPath, InputStream bPath) {
    this.embedding = embedding;
    W_fc1 = new Matrix(WPath);
    b_fc1 = new Matrix(bPath).transpose();
  }

  @Override
  public float[] getScores(String[] context) {
    return embedding.lookup(context).mul(W_fc1).add(b_fc1).row(0);
  }

}
