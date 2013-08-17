package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Malayalam;

public class MalayalamConcurrencyTest extends AbstractLanguageConcurrencyTest {

	@Override
	protected Language createLanguage() {
		return new Malayalam();
	}

	@Override
	protected String createSampleText() {
		return "വിക്കിപീഡിയ, ഒരു സ്വതന്ത്ര വിജ്ഞാനകോശം.";
	}
}
