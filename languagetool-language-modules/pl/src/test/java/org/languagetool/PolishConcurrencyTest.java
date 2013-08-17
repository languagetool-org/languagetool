package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Polish;

public class PolishConcurrencyTest extends AbstractLanguageConcurrencyTest {

	@Override
	protected Language createLanguage() {
		return new Polish();
	}

	@Override
	protected String createSampleText() {
		return "Wiedźmin – postać stworzona przez polskiego pisarza fantasy Andrzeja Sapkowskiego.";
	}

}
