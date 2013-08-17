package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.NewZealandEnglish;

public class NewZealandEnglishConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new NewZealandEnglish();
	}

	@Override
	protected String createSampleText() {
		return "A sentence with a error in the Hitchhiker's Guide tot he Galaxy";
	}
}
