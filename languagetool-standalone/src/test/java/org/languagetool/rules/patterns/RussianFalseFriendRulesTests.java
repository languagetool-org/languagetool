package org.languagetool.rules.patterns;


import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.*;
import org.languagetool.rules.RuleMatch;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by user on 26.03.17.
 */
public class RussianFalseFriendRulesTests {

    @Test
    public void testHintsForRussianSpeakers() throws IOException, ParserConfigurationException, SAXException {
        JLanguageTool langTool = new JLanguageTool(new English(), new Russian());

        //id="ACCURATE
        List<RuleMatch> matches = assertErrors(1, "You are always accurate and clean - " +
                "even if you are a little out of style.", langTool);
        assertEquals("[tidy, neat]", matches.get(0).getSuggestedReplacements().toString());
        assertErrors(0, "The handwriting on the letter was neat and feminine.", langTool);

        //id="ACTUAL"
        matches = assertErrors(1, "Shakespeare's stories are still actual today.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[relevant, topical, urgent]");
        assertErrors(0, "Education should be relevant to children's needs.", langTool);

        //id="ALLEY"
        matches = assertErrors(1, "We drove down a winding country alley.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[lane]");
        assertErrors(0, "It's one of the world's busiest shipping lanes.", langTool);

        //id="ANGINA"
        matches = assertErrors(1, "My patient has an angina", langTool);
        assertEquals("[tonsillitis]", matches.get(0).getSuggestedReplacements().toString());
        assertErrors(0, "My patient has a tonsillitis", langTool);

        //id="BRILLIANT"
        matches = assertErrors(1, "A brilliant is the hardest, most resilient gem of all.", langTool);
        assertEquals("[diamond]", matches.get(0).getSuggestedReplacements().toString());
        assertErrors(0, "Diamonds are the girls' best friend", langTool);

        //id="CABINET"
        matches = assertErrors(1, "I've repaired the computers in your cabinet, too.", langTool);
        assertEquals("[office, study]", matches.get(0).getSuggestedReplacements().toString());
        assertErrors(0, "They moved Alex's office upstairs.", langTool);

        //id="CAMERA"
        matches = assertErrors(1, "The pisoner is in the camera now", langTool);
        assertEquals("[cell, chamber]", matches.get(0).getSuggestedReplacements().toString());
        assertErrors(0, "This one guy, I was his cell mate for a few months.", langTool);

        //id="CARTON"
        matches = assertErrors(1, "A carton is used for making boxes.", langTool);
        assertEquals("[cardboard]", matches.get(0).getSuggestedReplacements().toString());
        assertErrors(0, "I made up a cardboard sign saying I was doing it for the homeless", langTool);

        //id="CHEF"
        matches = assertErrors(1, "My chef says the phones they use are secure", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[boss, manager]");
        assertErrors(0, "I'll ask my manager if I can leave work early tomorrow.", langTool);

        //id="CLAY"
        matches = assertErrors(1, "Put a bit of clay on both edges to stick them together.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[glue]");
        assertErrors(0, "You were the glue that held us together.", langTool);

        //id="CONCOURSE"
        matches = assertErrors(1, "Applicants face stiff concourse for university places this year.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[contest, competition]");
        assertErrors(0, "None of us would win a beauty contest, would we?", langTool);

        //id="DATA"
        matches = assertErrors(1, "What is the data you were born?", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[date]");
        assertErrors(0, "We agreed to finish the report at a later date.", langTool);

        //id="DECADE"
        matches = assertErrors(1, "We will end this work in the beginning of the second decade of January.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[10 days]");
        assertErrors(0, "We will end this work between 10th and 20th of January.", langTool);

        //id="FABRIC"
        matches = assertErrors(1, "She works at the textile fabric", langTool);
        assertEquals("[factory]", matches.get(0).getSuggestedReplacements().toString());
        assertErrors(0, "Charlie is the CEO of the chocolate factory.", langTool);

        //id="FAMILY"
        matches = assertErrors(1, "You said your name, but I asked a family.", langTool);
        assertEquals("[surname]", matches.get(0).getSuggestedReplacements().toString());
        assertErrors(0, "What is your surname?", langTool);

        //id="FART"
        matches = assertErrors(1, "I won this competition, it was such a fart.", langTool);
        assertEquals("[luck]", matches.get(0).getSuggestedReplacements().toString());
        assertErrors(0, "Good luck!", langTool);

        //id="GYM"
        matches = assertErrors(1, "I graduated from female gymnasium.", langTool);
        assertEquals("[grammar school]", matches.get(0).getSuggestedReplacements().toString());
        assertErrors(0, "A grammar school is a school for children aged between eleven " +
                "and 18 who have passed an examination that shows they are good at studying", langTool);

        //id="INSULT"
        matches = assertErrors(1, "He passed away from insult.", langTool);
        assertEquals("[stroke]", matches.get(0).getSuggestedReplacements().toString());
        assertErrors(0, "He suffered a stroke in 1988 that left him unable to speak.", langTool);

        //id="INTELLIGENCE"
        matches = assertErrors(1, "As a status-class, the intelligence grow by recruiting members of the working class.", langTool);
        assertEquals("[intelligentsia]", matches.get(0).getSuggestedReplacements().toString());
        assertErrors(0, "The Soviet intelligentsia arose from the ideological commitment.", langTool);

        //id="LIQUIDIZE"
        matches = assertErrors(1, "We need to liquidize those mistakes.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[eliminate]");
        assertErrors(0, "A move towards healthy eating could help eliminate heart disease.", langTool);

        //id="MARMALADE"
        matches = assertErrors(1, "Jill eats all her marmalade except the licorice ones.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[fruit jellies]");
        assertErrors(0, "These colorful fruit jellies can be cut to any size before being rolled in sugar.", langTool);

        //id="MULTIPLICATION"
        matches = assertErrors(1, "Thanks to computer multiplication, it is now possible to make cartoons much more quickly than in the past.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[animation]");
        assertErrors(0, "Older phones had simple animations.", langTool);

        //id="PRESERVATIVE"
        matches = assertErrors(1, "People use preservatives or other methods to avoid catching a disease, especially AIDS.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[condom]");
        assertErrors(0, "A primitive condom helped deter syphilis, but was uncomfortable and unreliable.", langTool);

        //id="PRETEND"
        matches = assertErrors(1, "I pretend for a bigger position.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[trying to get, to qualify]");
        assertErrors(0, "She doesn't qualify for maternity leave because she hasn't been in her job long enough.", langTool);

        //id="PROSPECT"
        matches = assertErrors(1, "I live on the main prospect.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[avenue, brochure]");
        assertErrors(0, "The broad avenues are shaded by splendid trees.", langTool);

        //id="REALIZE"
        matches = assertErrors(1, "Try to realize this method using other classes", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[sell, implement]");
        assertErrors(0, "We decided not to implement this interface.", langTool);

        //id="RECEIPT"
        matches = assertErrors(1, "Follow the receipt to prepare the cake.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[recipe, prescription]");
        assertErrors(0, "Do you know a good recipe for wholemeal bread?", langTool);

        //id="RESIN"
        matches = assertErrors(1, "The doll was made out of resin.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[rubber]");
        assertErrors(0, "Tyres are almost always made of rubber.", langTool);

        //id="STOOL"
        matches = assertErrors(1, "A stool is a seat for one person that has a back,", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[chair]");
        assertErrors(0, "Don't tip your chair back like that, you'll fall.", langTool);

        //id="SYMPATHY"
        matches = assertErrors(1, "I have a sympathy for you.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[interest, a liking for]");
        assertErrors(0, "She has a liking for these boys.", langTool);

        //id="TALON"
        matches = assertErrors(1, "If you have a talon from the newspaper, you can get a free beach towel.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[coupon]");
        assertErrors(0, "To find out more about our products, fill in the coupon and send it to us.", langTool);

        //id="TROOP"
        matches = assertErrors(1, "The troop was found in a ravine.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[corpse]");
        assertErrors(0, "The corpse was in an advanced stage of decomposition.", langTool);

        //id="VELVET"
        matches = assertErrors(1, "Velvet is made applying special technique.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[Corduroy]");
        assertErrors(0, "As a textile material, corduroy is considered a durable cloth.", langTool);

        //id="VIRTUOUS"
        matches = assertErrors(1, "He was a virtuous musician.", langTool);
        assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[masterly, perfect]");
        assertErrors(0, "He masterly played violin.", langTool);

    }

    private List<RuleMatch> assertErrors(int errorCount, String s, JLanguageTool langTool) throws IOException {
        List<RuleMatch> matches = langTool.check(s);
        //System.err.println(matches);
        assertEquals("Matches found: " + matches, errorCount, matches.size());
        return matches;
    }

}