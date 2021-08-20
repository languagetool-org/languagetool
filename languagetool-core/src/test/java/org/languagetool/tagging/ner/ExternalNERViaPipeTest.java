package org.languagetool.tagging.ner;

import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ExternalNERViaPipeTest {
  
  @Test
  @Ignore("interactive testing only")
  public void testCommand() throws IOException {
    ExternalNERViaPipe pipe = new ExternalNERViaPipe(new File("/home/dnaber/lt/git/ner-cmdline/start.sh"));
    List<ExternalNERViaPipe.Span> res = pipe.runNER("I am Peter Jones");
    System.out.println(res);
  }

  @Test
  public void testParseBuffer() throws IOException {
    ExternalNERViaPipe pipe = new ExternalNERViaPipe();
    List<ExternalNERViaPipe.Span> res = pipe.parseBuffer("This/O/0/4 is/O/5/7 Peter/PERSON/8/13 's/O/13/15 job/O/16/19 ./O/19/20");
    assertThat(res.size(), is(1));
    assertThat(res.get(0).getStart(), is(8));
    assertThat(res.get(0).getEnd(), is(13));
    //System.out.println(res);
  }

}
