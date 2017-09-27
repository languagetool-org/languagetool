package org.languagetool.language;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class SerbianTest {

  private final Serbian serbian = new Serbian();
  private final List<String> ruleFiles = new ArrayList<>();
  private static final List<String> RULE_FILE_NAMES = Arrays.asList(
          "grammar.xml", // Tested method adds this by default
          "grammar-barbarism.xml",
          "grammar-logical.xml",
          "grammar-punctuation.xml",
          "grammar-spelling.xml",
          "grammar-style.xml"
  );

  @Before
  public void setUp() {
    final String dirBase = "/org/languagetool/rules/sr/";
    for (final String ruleFileName : RULE_FILE_NAMES) {
      ruleFiles.add( dirBase + ruleFileName );
    }
  }

  @Test
  public void getRuleFileNames() throws Exception {
    assertEquals( ruleFiles, serbian.getRuleFileNames() );
  }

}