package org.languagetool;

import org.junit.Test;
import org.languagetool.language.Chinese;
import org.languagetool.language.SimplifiedChinese;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

public class JLanguageToolTest {

    private JLanguageTool languageTool = new JLanguageTool(new SimplifiedChinese());
    /**
     * Demo of using java-api.
     * @link http://wiki.languagetool.org/java-api
     */
    @Test
    public void demoCodeForHomepage() throws IOException {
        String[] text = {"他消毁了文件.", "他们定婚了。"};
        for (String t : text) {
            List<RuleMatch> matches = languageTool.check(t);
            for (RuleMatch match: matches) {
                System.out.println("Potential error at " +
                        match.getFromPos() + "-" + match.getToPos() + ": " +
                        match.getMessage());
                System.out.println("Suggested correction: " +
                        match.getSuggestedReplacements());
            }
        }
    }

}
