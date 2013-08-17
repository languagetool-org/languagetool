package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Danish;

public class DanishConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Danish();
	}

	@Override
	protected String createSampleText() {
		return "Se hjælpesiderne for dokumentation eller find svar på ofte stillede spørgsmål.";
	}

}
