package org.languagetool.dev;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;


public class GenerateIrishWordformsTest {
  private static final Map<String, String[]> nounGuesses = new HashMap<>();
  static {
    nounGuesses.put("óir", new String[]{"m", "óir", "óra", "óirí", "óirí"});
    nounGuesses.put("eoir", new String[]{"m", "eoir", "eora", "eoirí", "eoirí"});
    nounGuesses.put("eálaí", new String[]{"m", "eálaí", "eálaí", "eálaithe", "eálaithe"});
  }

  @Test
  public void testGetEndingsRegex() {
    assertEquals("(.+)(eálaí|eoir|óir)$", GenerateIrishWordforms.getEndingsRegex(nounGuesses));
  }

  @Test
  public void testGuessIrishFSTNounClassSimple() {
    assertEquals("Nm3-1", GenerateIrishWordforms.guessIrishFSTNounClassSimple("blagadóir"));
  }

  @Test
  public void testExtractEnWiktionaryNounTemplate() {
    String a = "bádóirí, type: {{ga-decl-m3|b|ádóir|ádóra|ádóirí}}";
    Map<String, String> aMap = GenerateIrishWordforms.extractEnWiktionaryNounTemplate(a);
    assertEquals("b", aMap.get("stem"));
    assertEquals("ádóirí", aMap.get("pl.gen"));
  }
}
