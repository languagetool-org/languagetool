package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.AustralianEnglish;

public class AustralianEnglishConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new AustralianEnglish();
	}

	@Override
	protected String createSampleText() {
		return "A sentence with a error in the Hitchhiker's Guide tot he Galaxy";
	}
}
