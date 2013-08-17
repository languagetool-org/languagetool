package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Asturian;

public class AsturianConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Asturian();
	}

	@Override
	protected String createSampleText() {
		return "L'asturianu ye una llingua romancep ropia d'Asturies, perteneciente al subgrupu asturllionés.";
	}
}
