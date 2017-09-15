package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Serbian;

public class SerbianConcurrencyTest extends AbstractLanguageConcurrencyTest {

  @Override
  protected Language createLanguage() {
    return new Serbian();
  }

  @Override
  protected String createSampleText() {
    return "Материјал из Википедије, слободне енциклопедије.";
  }
}
