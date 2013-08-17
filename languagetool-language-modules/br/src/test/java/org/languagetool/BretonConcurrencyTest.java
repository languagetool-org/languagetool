package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Breton;

public class BretonConcurrencyTest extends AbstractLanguageConcurrencyTest {
	@Override
	protected Language createLanguage() {
		return new Breton();
	}

	@Override
	protected String createSampleText() {
		return "Ma oa bet krouet ar pennad-mañ ganeoc'h pellik zo, marteze eo bet diverketabaoe.";
	}
}
