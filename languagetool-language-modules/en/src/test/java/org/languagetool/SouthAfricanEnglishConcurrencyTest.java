package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.SouthAfricanEnglish;

public class SouthAfricanEnglishConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new SouthAfricanEnglish();
	}

	@Override
	protected String createSampleText() {
		return "A sentence with a error in the Hitchhiker's Guide tot he Galaxy";
	}
}
