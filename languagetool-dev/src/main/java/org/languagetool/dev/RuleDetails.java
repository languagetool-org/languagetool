/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.dev;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Extends a CSV with a column of rule ids with rule descriptions and categories
 */
public class RuleDetails {
  private final List<Rule> rules;

  public RuleDetails(Language lang, @Nullable String ngramPath) throws IOException {
    JLanguageTool lt = new JLanguageTool(lang);
    if (ngramPath != null) {
      lt.activateLanguageModelRules(new File(ngramPath));
    }

    rules = lt.getAllRules();
  }

  @Nullable
  private Rule getRuleById(String ruleId) {
    return rules.stream()
      .filter(r -> r.getId().equals(ruleId))
      .findFirst().orElse(null);
  }

  public static void main(String[] args) throws ParseException, IOException {
    Options options = new Options();
    options.addRequiredOption("l", "language", true, "Language for rules");
    options.addRequiredOption("f", "file", true, "Input file");
    options.addRequiredOption("o", "output", true, "Output file");
    options.addRequiredOption("c", "column", true, "Column in input file");
    options.addOption("n", "ngramPath", true, "Ngram path to activate ngram rules");

    CommandLine cmd = new DefaultParser().parse(options, args);

    String langCode = cmd.getOptionValue('l');
    String inputFile = cmd.getOptionValue('f');
    String outputFile = cmd.getOptionValue('o');
    String column = cmd.getOptionValue('c');
    String ngramPath = cmd.hasOption('n') ? cmd.getOptionValue('n') : null;

    RuleDetails details = new RuleDetails(Languages.getLanguageForShortCode(langCode), ngramPath);

    CSVFormat format = CSVFormat.RFC4180.withFirstRecordAsHeader();

    try (CSVParser parser = CSVParser.parse(new File(inputFile), Charset.defaultCharset(), format)) {
      try (CSVPrinter printer = new CSVPrinter(new BufferedWriter(new FileWriter(outputFile)), format)) {
        Map<String, Integer> oldHeader = parser.getHeaderMap();
        List<String> newHeader = new ArrayList<>(Collections.nCopies(oldHeader.size(), null));

        for (Map.Entry<String, Integer> entry : oldHeader.entrySet()) {
          newHeader.set(entry.getValue(), entry.getKey());
        }
        newHeader.add("description");
        newHeader.add("category");
        printer.printRecord(newHeader);

        if (!oldHeader.containsKey(column)) {
          throw new RuntimeException("Input file does not contain specified column " + column);
        }

        List<CSVRecord> records = parser.getRecords();


        records.stream().sequential().map(record -> {
          String ruleId = record.get(column);
          Rule rule = details.getRuleById(ruleId);
          List<String> transformedValues = new ArrayList<>();
          record.iterator().forEachRemaining(transformedValues::add);
          if (rule == null) {
            transformedValues.add("");
            transformedValues.add("");
          } else {
            transformedValues.add(rule.getDescription());
            transformedValues.add(rule.getCategory().getId().toString());
          }
          return transformedValues;
        }).forEachOrdered(values -> {
          try {
            printer.printRecord(values);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
      }
    }

  }
}
