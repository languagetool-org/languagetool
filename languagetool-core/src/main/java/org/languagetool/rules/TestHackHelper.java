package org.languagetool.rules;

import java.util.Arrays;
import java.util.List;

public class TestHackHelper {
  public TestHackHelper() {
  }

  public boolean isJUnitTest() {
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    List<StackTraceElement> list = Arrays.asList(stackTrace);
    for (StackTraceElement element : list) {
      if (element.getClassName().startsWith("org.junit.") ||
              element.getClassName().equals("org.languagetool.rules.patterns.PatternRuleTest")) {
        return true;
      }
    }
    return false;
  }
}