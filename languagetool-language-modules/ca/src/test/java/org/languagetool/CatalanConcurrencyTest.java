package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Catalan;

public class CatalanConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Catalan();
	}

	@Override
	protected String createSampleText() {
		return "Si tot i així encara no apareix, potser la pàgina ha estat suprimida.";
	}
}
