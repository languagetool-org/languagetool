package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Italian;

public class ItalianConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Italian();
	}

	@Override
	protected String createSampleText() {
		return "Da Wikipedia, l'enciclopedia libera.";
	}

}
