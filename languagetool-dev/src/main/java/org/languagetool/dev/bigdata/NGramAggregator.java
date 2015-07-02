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

import java.io.IOException;

/**
 * Aggregate Google ngram data (http://storage.googleapis.com/books/ngrams/books/datasetsv2.html)
 * in Hadoop. Based on the Hadoop word counter example.
 */
public final class NGramAggregator {

  private static final int MIN_YEAR = 1910;

  private NGramAggregator() {
  }

  public static class TokenizerMapper extends Mapper<Object, Text, Text, LongWritable> {
    
    private final Text word = new Text();
    private final LongWritable count = new LongWritable();

    @Override
    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
      String[] parts = value.toString().split("\t");
      int year = Integer.parseInt(parts[1]);
      if (year >= MIN_YEAR) {
        word.set(parts[0]);
        count.set(Long.parseLong(parts[2]));
        context.write(word, count);
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
    Job job = Job.getInstance(conf, "Google ngram aggregator");
    job.setJarByClass(NGramAggregator.class);
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
