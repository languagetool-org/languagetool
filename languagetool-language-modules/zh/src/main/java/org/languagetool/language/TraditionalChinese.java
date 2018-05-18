package org.languagetool.language;

public class TraditionalChinese extends Chinese{

  @Override
  public String getName() {
    return "Chinese (Traditional)";
  }

  @Override
  public String[] getCountries() {
    return new String[]{"TW"};
  }

}
