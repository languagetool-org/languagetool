package org.languagetool.language;

public class SimplifiedChinese extends Chinese {

  @Override
  public String getName() {
      return "Chinese (Simplified)";
  }

  @Override
  public String[] getCountries() {
    return  new String[]{"CN"};
  }

}
