package org.languagetool;

import jep.Jep;
import jep.SharedInterpreter;
import org.junit.Ignore;
import org.junit.Test;

public class SpacyTest {

  @Test
  @Ignore("interactive test only")
  public void testSpaceIntegration() {
    try (Jep jep = new SharedInterpreter()) {
      jep.runScript("/home/dnaber/lt/git/languagetool/spacy-test.py");
      String text = "The red fox jumped over the lazy dog.";
      jep.eval("result = chunking('" + text + "')");
      Object result = jep.getValue("result");
      System.out.println("-->" + result);
    }
  }
}
