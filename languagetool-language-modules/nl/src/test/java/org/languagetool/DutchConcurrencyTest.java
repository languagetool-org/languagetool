package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Dutch;

public class DutchConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Dutch();
	}

	@Override
	protected String createSampleText() {
		return "lekkere frikandel";
	}

}
