package org.languagetool.rules.en;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class NewAvsAnDataTest {

  NewAvsAnData newAvsAnData = new NewAvsAnData();
  public Set<String> requiresA = newAvsAnData.loadWords("/en/det_a.txt");

  public Set<String> requiresAn = newAvsAnData.loadWords("/en/det_an.txt");
  @Test
  public void testGetWordsRequiringA(){
    assertEquals(requiresA, newAvsAnData.getWordsRequiringA());
  }

  @Test
  public void testGetWordsRequiringAn(){
    assertEquals(requiresAn, newAvsAnData.getWordsRequiringAn());
  }
}
