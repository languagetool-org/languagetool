package org.languagetool.rules.neuralnetwork;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DictionaryTest {

  @Test
  public void mapFromStringTest() {
    String dictionary = "{'a': 3, 'asa': 1, 'UNK': 42}";
    Dictionary dict = new Dictionary(dictionary);
    assertEquals(new Integer(1), dict.get("asa"));
    assertEquals(new Integer(3), dict.safeGet("a"));
    assertEquals(new Integer(42), dict.safeGet("foo"));
  }

}
