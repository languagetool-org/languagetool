package org.languagetool.language;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SerbianTest {

  private final Serbian serbian = new Serbian();
  private final List<String> ruleFiles = new ArrayList<>();

  @Before
  public void setUp() {
    ruleFiles.add("/org/languagetool/rules/sr/grammar.xml");
  }

  @Test
  public void getRuleFileNames() throws Exception {
    assertEquals( ruleFiles, serbian.getRuleFileNames() );
  }

}