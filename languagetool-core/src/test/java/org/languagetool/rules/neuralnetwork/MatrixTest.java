package org.languagetool.rules.neuralnetwork;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class MatrixTest {

    @Test
    public void matrixFromListTest() {
        String matrixString = "1 2\n3 4\n5 6";
        Matrix expectedMatrix = new Matrix(new float[][]{{1,2},{3,4},{5,6}});
        Matrix matrix = new Matrix(Arrays.asList(matrixString.split("\n")));
        assertEquals(expectedMatrix, matrix);
    }

    @Test
    public void matMulTest() {
        final Matrix a = new Matrix(new float[][]{{1,2},{3,4},{5,6}});
        final Matrix b = new Matrix(new float[][]{{1},{2}});
        final Matrix c = new Matrix(new float[][]{{5},{11},{17}});
        assertEquals(c, a.mul(b));
    }

    @Test
    public void matAddTest() {
        Matrix a = new Matrix(new float[][]{{1,2},{3,4},{5,6}});
        Matrix b = new Matrix(new float[][]{{1,2},{3,4},{5,7}});
        Matrix c = new Matrix(new float[][]{{2,4},{6,8},{10,13}});
        assertEquals(c, a.add(b));
    }

}