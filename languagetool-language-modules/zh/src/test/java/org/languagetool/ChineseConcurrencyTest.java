package org.languagetool;

import org.languagetool.language.AbstractLanguageConcurrencyTest;
import org.languagetool.language.Chinese;

public class ChineseConcurrencyTest extends AbstractLanguageConcurrencyTest {

	@Override
	protected Language createLanguage() {
		return new Chinese();
	}

	@Override
	protected String createSampleText() {
		return "维基百科，自由的百科全书";
	}

}
