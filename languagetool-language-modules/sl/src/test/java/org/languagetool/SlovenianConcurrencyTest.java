package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Slovenian;

public class SlovenianConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Slovenian();
	}

	@Override
	protected String createSampleText() {
		return "Iz Wikipedije, proste enciklopedije";
	}

}
