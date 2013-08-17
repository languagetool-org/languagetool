package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Lithuanian;

public class LithuanianConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Lithuanian();
	}

	@Override
	protected String createSampleText() {
		return "Vikipedijoje nėra straipsnio norimu pavadinimu.";
	}

}
