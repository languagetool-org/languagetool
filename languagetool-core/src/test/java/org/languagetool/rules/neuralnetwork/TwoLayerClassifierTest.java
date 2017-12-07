package org.languagetool.rules.neuralnetwork;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class TwoLayerClassifierTest {

  @Test
  public void getScoresTest() throws Exception {
    Dictionary dictionary = new Dictionary("{'foo': 0, 'bar': 1, }");
    Matrix embedding = new Matrix(Arrays.asList("1 2 3", "3 4 5"));
    ByteArrayInputStream W_fc1 = new ByteArrayInputStream("3 3 4 1\n5 6 6 1\n3 3 4 1\n3 3 4 1\n5 2 6 1\n3 3 4 3\n3 3 4 5\n5 6 6 1\n3 5 4 1\n3 3 4 1\n5 6 7 1\n3 3 4 1".getBytes(StandardCharsets.UTF_8.name()));
    ByteArrayInputStream b_fc1 = new ByteArrayInputStream("1\n2\n3\n4".getBytes(StandardCharsets.UTF_8.name()));
    ByteArrayInputStream W_fc2 = new ByteArrayInputStream("1 1.1\n2.2 2\n3 3.5\n4.6 4".getBytes(StandardCharsets.UTF_8.name()));
    ByteArrayInputStream b_fc2 = new ByteArrayInputStream("-1\n-2".getBytes(StandardCharsets.UTF_8.name()));
    TwoLayerClassifier twoLayerClassifier = new TwoLayerClassifier(new Embedding(dictionary, embedding), W_fc1, b_fc1, W_fc2, b_fc2);

    float[] scores = twoLayerClassifier.getScores(new String[]{"foo", "bar", "foo", "foo"});

    assertEquals(1012.20f, scores[0], 0.01);
    assertEquals(1043.60f, scores[1], 0.01);
  }

}