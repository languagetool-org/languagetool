package org.languagetool.tokenizers;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.tagging.Tagger;

/**
 * Test class for {@link ManualTaggerAdapter}.
 * @author Ionuț Păduraru
 */
public class ManualTaggerAdapterTest extends TestCase {

	final static String TEST_DATA = 
			"# some test data\n" +
					"inflectedform11\tlemma1\tPOS1\n" +
					"inflectedform121\tlemma1\tPOS2\n" +
					"inflectedform122\tlemma1\tPOS2\n" +
					"inflectedform123\tlemma1\tPOS3\n" +
					"inflectedform2\tlemma2\tPOS1a\n" +
					"inflectedform2\tlemma2\tPOS1b\n" +
					"inflectedform2\tlemma2\tPOS1c\n" +
					"inflectedform3\tlemma3a\tPOS3a\n" +
					"inflectedform3\tlemma3b\tPOS3b\n" +
					"inflectedform3\tlemma3c\tPOS3c\n" +
					"inflectedform3\tlemma3d\tPOS3d\n";
	

	protected Tagger tagger;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		tagger = new ManualTaggerAdapter(new ManualTagger(new ByteArrayInputStream(TEST_DATA.getBytes("UTF-8"))));
	}
	
	public void testMultipleLemma() throws Exception {
		List<String> l = Arrays.asList("inflectedform3");
		List<AnalyzedTokenReadings> analyzedTokenReadings = tagger.tag(l);
		assertNotNull(analyzedTokenReadings);
		assertEquals(1, analyzedTokenReadings.size());
		
		final AnalyzedTokenReadings analyzedTokenReading = analyzedTokenReadings.get(0);
		assertEquals("inflectedform3", analyzedTokenReading.getToken());
		assertNotNull(analyzedTokenReading.getReadings());
		assertEquals(4, analyzedTokenReading.getReadingsLength());
		
		
		AnalyzedToken analyzedToken;
		
		analyzedToken = analyzedTokenReading.getReadings().get(0);
		assertEquals("inflectedform3", analyzedToken.getToken());
		assertEquals("lemma3a", analyzedToken.getLemma());
		assertEquals("POS3a", analyzedToken.getPOSTag());
		
		analyzedToken = analyzedTokenReading.getReadings().get(1);
		assertEquals("inflectedform3", analyzedToken.getToken());
		assertEquals("lemma3b", analyzedToken.getLemma());
		assertEquals("POS3b", analyzedToken.getPOSTag());
		
		analyzedToken = analyzedTokenReading.getReadings().get(2);
		assertEquals("inflectedform3", analyzedToken.getToken());
		assertEquals("lemma3c", analyzedToken.getLemma());
		assertEquals("POS3c", analyzedToken.getPOSTag());
		
		analyzedToken = analyzedTokenReading.getReadings().get(3);
		assertEquals("inflectedform3", analyzedToken.getToken());
		assertEquals("lemma3d", analyzedToken.getLemma());
		assertEquals("POS3d", analyzedToken.getPOSTag());
	}
	
	public void testMultiplePOS() throws Exception {
		List<String> l = Arrays.asList("inflectedform2");
		List<AnalyzedTokenReadings> analyzedTokenReadings = tagger.tag(l);
		assertNotNull(analyzedTokenReadings);
		assertEquals(1, analyzedTokenReadings.size());
		AnalyzedTokenReadings analyzedTokenReading = analyzedTokenReadings.get(0);
		assertEquals("inflectedform2", analyzedTokenReading.getToken());
		assertNotNull(analyzedTokenReading.getReadings());
		assertEquals(3,analyzedTokenReading.getReadingsLength());
		AnalyzedToken analyzedToken;
		
		analyzedToken = analyzedTokenReading.getReadings().get(0);
		assertEquals("POS1a", analyzedToken.getPOSTag());
		assertEquals("inflectedform2", analyzedToken.getToken());
		assertEquals("lemma2", analyzedToken.getLemma());
		
		analyzedToken = analyzedTokenReading.getReadings().get(1);
		assertEquals("POS1b", analyzedToken.getPOSTag());
		assertEquals("inflectedform2", analyzedToken.getToken());
		assertEquals("lemma2", analyzedToken.getLemma());
		
		analyzedToken = analyzedTokenReading.getReadings().get(2);
		assertEquals("POS1c", analyzedToken.getPOSTag());
		assertEquals("inflectedform2", analyzedToken.getToken());
		assertEquals("lemma2", analyzedToken.getLemma());
	}

	public void testMultipleWords() throws Exception {
		List<String> l = Arrays.asList("inflectedform2", "inflectedform3");
		List<AnalyzedTokenReadings> analyzedTokenReadings = tagger.tag(l);
		assertNotNull(analyzedTokenReadings);
		assertEquals(2, analyzedTokenReadings.size());
		
		AnalyzedTokenReadings analyzedTokenReading;
		
		analyzedTokenReading = analyzedTokenReadings.get(0);
		assertEquals("inflectedform2", analyzedTokenReading.getToken());
		assertNotNull(analyzedTokenReading.getReadings());
		assertEquals(3,analyzedTokenReading.getReadingsLength());
		// analyzedTokenReading.getReadings are tested by #testMultipleLemma() 
		 
		analyzedTokenReading = analyzedTokenReadings.get(1);
		assertEquals("inflectedform3", analyzedTokenReading.getToken());
		assertNotNull(analyzedTokenReading.getReadings());
		assertEquals(4, analyzedTokenReading.getReadingsLength());
		// analyzedTokenReading.getReadings are tested by #testMultiplePOS()  
	}
	
}
