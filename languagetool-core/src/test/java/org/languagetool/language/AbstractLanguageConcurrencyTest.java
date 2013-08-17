package org.languagetool.language;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

public abstract class AbstractLanguageConcurrencyTest {	
	@Test
	public void testSpellCheckerFailure() throws Exception {
		System.setProperty(
				"javax.xml.parsers.SAXParserFactory",
				"com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
		
		final String txt = createSampleText();		
		final Language language = createLanguage();
		
		final Object syncLock = new Object();
		int threadCount = Runtime.getRuntime().availableProcessors() * 10;
		
		List<Thread> threads = new ArrayList<Thread>();
		synchronized (syncLock) {
			for (int i = 0; i < threadCount; i++) {
				Runnable r = new Runnable() {
					@Override
					public void run() {
						synchronized (syncLock) {
							syncLock.notifyAll();
						}
						
						for (int i = 0; i < 100; i++) {
							try {
								JLanguageTool tool = new JLanguageTool(language);
								Assert.assertNotNull(tool.check(txt));
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}
					}
				};
				Thread t = new Thread(r);
				t.start();
				threads.add(t);
			}
		}
		for (Thread t : threads) {
			t.join();
		}
	}
	
	protected abstract Language createLanguage();
	protected abstract String createSampleText();
}
