package org.languagetool.dev.bigdata;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.languagetool.dev.bigdata.ConfusionFileIndenter.indent;

public class ConfusionFileIndenterTest {
  
  @Test
  public void indentWithCommentsTest() {
    List<String> lines = Arrays.asList("mir; mit; 1.50 # p=0.994, r=0.658, tp=775, tn=1173, fp=5, fn=403, 178+1000, 2017-10-23",
            "nach; noch; 1.75 # p=0.990, r=0.504, tp=1009, tn=1990, fp=10, fn=991, 1000+1000, 2017-10-23");
    String result = indent(lines);
    String expected = "mir; mit; 1.50                                                                    # p=0.994, r=0.658, tp=775, tn=1173, fp=5, fn=403, 178+1000, 2017-10-23\n"
            + "nach; noch; 1.75                                                                  # p=0.990, r=0.504, tp=1009, tn=1990, fp=10, fn=991, 1000+1000, 2017-10-23\n";
    assertThat(result, is(expected));
  }
  
  @Test
  public void indentWithoutCommentsTest() {
    List<String> lines = Arrays.asList("mir; mit; 1.50",  "nach; noch; 1.75");
    String result = indent(lines);
    String expected = "mir; mit; 1.50\nnach; noch; 1.75\n";
    assertThat(result, is(expected));
  }

  @Test
  public void indentCommentedLineTest() {
    List<String> lines = Arrays.asList("#mir; mit; 1.50 # p=0.994, r=0.658, tp=775, tn=1173, fp=5, fn=403, 178+1000, 2017-10-23",
            "nach; noch; 1.75# p=0.990, r=0.504, tp=1009, tn=1990, fp=10, fn=991, 1000+1000, 2017-10-23");
    String result = indent(lines);
    String expected = "#mir; mit; 1.50                                                                   # p=0.994, r=0.658, tp=775, tn=1173, fp=5, fn=403, 178+1000, 2017-10-23\n"
            + "nach; noch; 1.75                                                                  # p=0.990, r=0.504, tp=1009, tn=1990, fp=10, fn=991, 1000+1000, 2017-10-23\n";
    assertThat(result, is(expected));
  }

  @Test
  public void indentLongLineTest() {
    List<String> lines = Collections.singletonList("fielen|wie in 'Die Kinder fielen hin.'; vielen|wie in 'Wir helfen vielen Menschen.'; 0.50 # p=0.994, r=0.715, tp=805, tn=1121, fp=5, fn=321, 126+1000, 2017-09-24\n");
    String result = indent(lines);
    String expected = "fielen|wie in 'Die Kinder fielen hin.'; vielen|wie in 'Wir helfen vielen Menschen.'; 0.50 # p=0.994, r=0.715, tp=805, tn=1121, fp=5, fn=321, 126+1000, 2017-09-24\n\n";
    assertThat(result, is(expected));
  }

}