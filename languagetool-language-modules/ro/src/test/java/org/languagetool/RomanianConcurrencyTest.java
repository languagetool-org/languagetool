package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Romanian;

public class RomanianConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Romanian();
	}

	@Override
	protected String createSampleText() {
		return "Puteți adăuga un articol în lista de articole cerute pentru ca un alt colaborator să-l înceapă.";
	}
}
