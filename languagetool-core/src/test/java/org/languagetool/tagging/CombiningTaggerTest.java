package org.languagetool.tagging;

import org.junit.Test;
import org.languagetool.JLanguageTool;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class CombiningTaggerTest {

  @Test
  public void testTagNoOverwrite() throws Exception {
    CombiningTagger tagger = getCombiningTagger(false, null);
    assertThat(tagger.tag("nosuchword").size(), is(0));
    List<TaggedWord> result = tagger.tag("fullform");
    assertThat(result.size(), is(2));
    String asString = getAsString(result);
    assertTrue(asString.contains("baseform1/POSTAG1"));
    assertTrue(asString.contains("baseform2/POSTAG2"));
  }

  @Test
  public void testTagOverwrite() throws Exception {
    CombiningTagger tagger = getCombiningTagger(true, null);
    assertThat(tagger.tag("nosuchword").size(), is(0));
    List<TaggedWord> result = tagger.tag("fullform");
    assertThat(result.size(), is(1));
    String asString = getAsString(result);
    assertTrue(asString.contains("baseform2/POSTAG2"));
  }

  @Test
  public void testTagRemoval() throws Exception {
    CombiningTagger tagger = getCombiningTagger(false, "/xx/removed.txt");
    assertThat(tagger.tag("nosuchword").size(), is(0));
    List<TaggedWord> result = tagger.tag("fullform");
    String asString = getAsString(result);
    assertFalse(asString.contains("baseform1/POSTAG1"));  // first tagged, but in removed.txt
    assertTrue(asString.contains("baseform2/POSTAG2"));
  }

  private CombiningTagger getCombiningTagger(boolean overwrite, String removalPath) throws IOException {
    ManualTagger tagger1 = new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream("/xx/added1.txt"));
    ManualTagger tagger2 = new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream("/xx/added2.txt"));
    ManualTagger removalTagger = null;
    if (removalPath != null) {
      removalTagger = new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream(removalPath));
    }
    return new CombiningTagger(tagger1, tagger2, removalTagger, overwrite);
  }

  private String getAsString(List<TaggedWord> result) {
    StringBuilder sb = new StringBuilder();
    for (TaggedWord taggedWord : result) {
      sb.append(taggedWord.getLemma());
      sb.append("/");
      sb.append(taggedWord.getPosTag());
      sb.append("\n");
    }
    return sb.toString();
  }

  @Test(expected = IOException.class)
  public void testInvalidFile() throws Exception {
    new ManualTagger(JLanguageTool.getDataBroker().getFromResourceDirAsStream("/xx/added-invalid.txt"));
  }

}