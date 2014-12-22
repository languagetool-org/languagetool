package org.languagetool.tagging;

import org.junit.Test;
import org.languagetool.JLanguageTool;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CombiningTaggerTest {

  @Test
  public void testTag() throws Exception {
    ManualTagger tagger1 = new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream("/xx/added1.txt"));
    ManualTagger tagger2 = new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream("/xx/added2.txt"));
    CombiningTagger tagger = new CombiningTagger(tagger1, tagger2);
    assertThat(tagger.tag("nosuchword").size(), is(0));
    List<TaggedWord> result = tagger.tag("fullform");
    assertThat(result.size(), is(2));
    StringBuilder sb = new StringBuilder();
    for (TaggedWord taggedWord : result) {
      sb.append(taggedWord.getLemma());
      sb.append("/");
      sb.append(taggedWord.getPosTag());
      sb.append("\n");
    }
    assertTrue(sb.toString().contains("baseform1/POSTAG1"));
    assertTrue(sb.toString().contains("baseform2/POSTAG2"));
  }

  @Test(expected = IOException.class)
  public void testInvalidFile() throws Exception {
    new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream("/xx/added-invalid.txt"));
 }

}