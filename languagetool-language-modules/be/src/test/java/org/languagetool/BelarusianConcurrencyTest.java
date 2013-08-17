package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Belarusian;

public class BelarusianConcurrencyTest extends AbstractLanguageConcurrencyTest {

	@Override
	protected Language createLanguage() {
		return new Belarusian();
	}

	@Override
	protected String createSampleText() {
		return "Гэтая старонка была сцёртая. Ніжэй паказаны журнал сціранняў і пераносаў для гэтайстаронкі.";
	}
}
