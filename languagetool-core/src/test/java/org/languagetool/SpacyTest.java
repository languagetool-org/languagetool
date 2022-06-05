package org.languagetool;

import jep.Jep;
import jep.SharedInterpreter;
import org.junit.Test;

public class SpacyTest {

  @Test
  public void testSpaceIntegration() {
    try (Jep jep = new SharedInterpreter()) {
      jep.runScript("/home/dnaber/lt/git/languagetool/spacy-test.py");
      String text = "The red fox jumped over the lazy dog.";
      jep.eval("result = chunking('" + text + "')");
      Object result = jep.getValue("result");
      System.out.println("-->" + result);
    }
    // Convert the java object returned by Jep and print it out
    /*println(result.asInstanceOf[ArrayList[Object]].asScala
      .map(_.asInstanceOf[java.util.List[String]].asScala.mkString(", "))
      .mkString("|"))*/
  }
}
