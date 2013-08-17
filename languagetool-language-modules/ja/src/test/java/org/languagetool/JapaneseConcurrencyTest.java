package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Japanese;

public class JapaneseConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Japanese();
	}

	@Override
	protected String createSampleText() {
		return "私はガラスを食べられます。それは私を傷つけませ";
	}
}
