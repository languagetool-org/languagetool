/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.dev.bigdata;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.languagetool.language.English;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.en.GoogleStyleWordTokenizer;
import org.languagetool.tokenizers.SentenceTokenizer;
import org.languagetool.tokenizers.Tokenizer;

import java.io.IOException;
import java.util.List;

/**
 * Index ngrams from CommonCrawl plain text (e.g. http://data.statmt.org/ngrams/)
 * in Hadoop.
 */
public final class CommonCrawlNGramJob {

  private static final int MAX_TOKEN_LENGTH = 20;

  public static class TokenizerMapper extends Mapper<Object, Text, Text, LongWritable> {

    private final SentenceTokenizer sentenceTokenizer;
    private final Tokenizer wordTokenizer;
    private final Text word = new Text();
    private final LongWritable count = new LongWritable();

    public TokenizerMapper() {
      this.sentenceTokenizer = new English().getSentenceTokenizer();
      this.wordTokenizer = new GoogleStyleWordTokenizer();
    }

    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      List<String> sentences = sentenceTokenizer.tokenize(value.toString());
      for (String sentence : sentences) {
        indexSentence(sentence, context);
      }
    }

    private void write(String ngram, Context context) throws IOException, InterruptedException {
      word.set(ngram);
      count.set(1);
      context.write(word, count);
    }

    private void indexSentence(String sentence, Context context) throws IOException, InterruptedException {
      List<String> tokens = wordTokenizer.tokenize(sentence);
      tokens.add(0, LanguageModel.GOOGLE_SENTENCE_START);
      tokens.add(LanguageModel.GOOGLE_SENTENCE_END);
      String prevPrev = null;
      String prev = null;
      for (String token : tokens) {
        if (token.trim().isEmpty()) {
          continue;
        }
        if (token.length() <= MAX_TOKEN_LENGTH) {
          write(token, context);
        }
        if (prev != null && token.length() <= MAX_TOKEN_LENGTH && prev.length() <= MAX_TOKEN_LENGTH) {
          write(prev + " " + token, context);
        }
        if (prevPrev != null && token.length() <= MAX_TOKEN_LENGTH && prev.length() <= MAX_TOKEN_LENGTH && prevPrev.length() <= MAX_TOKEN_LENGTH) {
          write(prevPrev + " " + prev + " " + token, context);
        }
        prevPrev = prev;
        prev = token;
      }
    }

  }

  public static class LongSumReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
    
    private final LongWritable result = new LongWritable();

    @Override
    public void reduce(Text key, Iterable<LongWritable> values, Context context) throws IOException, InterruptedException {
      long sum = 0;
      for (LongWritable val : values) {
        sum += val.get();
      }
      result.set(sum);
      context.write(key, result);
    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    conf.set("io.compression.codecs","io.sensesecure.hadoop.xz.XZCodec");
    Job job = Job.getInstance(conf, "CommonCrawl ngram indexer (see http://data.statmt.org/ngrams/)");
    job.setJarByClass(CommonCrawlNGramJob.class);
    job.setMapperClass(TokenizerMapper.class);
    job.setCombinerClass(LongSumReducer.class);
    job.setReducerClass(LongSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(LongWritable.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
  
}
