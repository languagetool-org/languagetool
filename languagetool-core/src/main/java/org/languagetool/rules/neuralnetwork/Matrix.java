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
import java.util.Arrays;
import java.util.List;

public class Matrix {

  private float[][] m;

  public Matrix(InputStream stream) {
    List<String> rows = ResourceReader.readAllLines(stream);
    fromLines(rows);
  }

  Matrix(float[] row) {
    m = new float[][]{row};
  }

  Matrix(List<String> rows) {
    fromLines(rows);
  }

  Matrix(float[][] matrix) {
    m = matrix;
  }

  private void fromLines(List<String> rows) {
    final int nRows = rows.size();
    final int nCols = rows.get(0).split(" ").length;

    m = new float[nRows][nCols];

    for (int i = 0; i < nRows; i++) {
      String[] row = rows.get(i).split(" ");
      for (int j = 0; j < nCols; j++) {
        m[i][j] = Float.parseFloat(row[j]);
      }
    }
  }

  float[] row(int n) {
    return Arrays.copyOf(m[n], m[n].length);
  }

  int rows() {
    return m.length;
  }

  int columns() {
    return m[0].length;
  }

  void printDimension() {
    System.out.println(m.length + "/" + m[0].length);
  }

  Matrix mul(Matrix that) {
    float[][] a = this.m;
    float[][] b = that.m;

    final int rowsA = a.length;
    final int colsA = a[0].length;
    final int rowsB = b.length;
    final int colsB = b[0].length;

    if (colsA != rowsB) {
      throw new ArithmeticException("Matrix with " + colsA + " columns cannot be multiplied with matrix with " + colsB + " rows");
    }

    float[][] c = new float[rowsA][colsB];

    for (int i = 0; i < rowsA; i++) {
      for (int j = 0; j < colsB; j++) {
        for (int k = 0; k < colsA; k++) {
          c[i][j] += a[i][k] * b[k][j];
        }
      }
    }

    return new Matrix(c);
  }

  Matrix add(Matrix that) {
    float[][] a = this.m;
    float[][] b = that.m;

    final int rowsA = a.length;
    final int colsA = a[0].length;
    final int rowsB = b.length;
    final int colsB = b[0].length;

    if (rowsA != rowsB) throw new ArithmeticException("Matrix with " + rowsA + " rows cannot be added to a matrix with " + rowsB + " rows");
    if (colsA != colsB) throw new ArithmeticException("Matrix with " + colsA + " columns cannot be added to a matrix with " + colsB + " columns");

    float[][] c = new float[rowsA][colsA];

    for (int i = 0; i < rowsA; i++) {
      for (int j = 0; j < colsB; j++) {
        c[i][j] = a[i][j] + b[i][j];
      }
    }

    return new Matrix(c);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof Matrix) {
      return Arrays.deepEquals(m, ((Matrix) obj).m);
    }
    return false;
  }

  public Matrix transpose() {
    int rows = m.length;
    int cols = m[0].length;
    float[][] b = new float[cols][rows];

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        b[j][i] = m[i][j];
      }
    }

    return new Matrix(b);
  }

  public Matrix relu() {
    int rows = m.length;
    int cols = m[0].length;
    float[][] b = new float[rows][cols];

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        b[i][j] = m[i][j] < 0 ? 0 : m[i][j];
      }
    }

    return new Matrix(b);
  }
}