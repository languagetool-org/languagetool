package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Spanish;

public class SpanishConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Spanish();
	}

	@Override
	protected String createSampleText() {
		return "También puede que la página que buscas haya sido borrada.";
	}
}
