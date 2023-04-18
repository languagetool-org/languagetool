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
package org.languagetool.rules;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;

import javax.measure.UnconvertibleException;
import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Volume;
import java.io.IOException;
import java.math.RoundingMode;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static tech.units.indriya.unit.Units.*;

/**
 * Base class providing support for detecting, parsing and converting between measurements in different units
 * @since 4.3
 */
@SuppressWarnings("unchecked")
public abstract class AbstractUnitConversionRule extends Rule {
  
  protected static final Unit<Mass> POUND = KILOGRAM.multiply(0.45359237);
  protected static final Unit<Mass> OUNCE = POUND.divide(12);

  protected static final Unit<Length> FEET = METRE.multiply(0.3048);
  protected static final Unit<Length> YARD = FEET.multiply(3);
  protected static final Unit<Length> INCH = FEET.divide(12);
  protected static final Unit<Length> MILE = FEET.multiply(5280);

  protected static final Unit<Volume> US_QUART = LITRE.multiply(0.946352946);
  protected static final Unit<Volume> US_GALLON = US_QUART.multiply(4);
  protected static final Unit<Volume> US_PINT = US_QUART.divide(2);
  protected static final Unit<Volume> US_CUP = US_QUART.divide(4);
  protected static final Unit<Volume> US_FL_OUNCE = US_QUART.divide(32);

  protected static final Unit<Volume> IMP_PINT = LITRE.multiply(0.5682612532);
  protected static final Unit<Volume> IMP_QUART = IMP_PINT.multiply(2);
  protected static final Unit<Volume> IMP_GALLON = IMP_QUART.multiply(4);
  protected static final Unit<Volume> IMP_FL_OUNCE = IMP_PINT.divide(20);

  protected static final Unit<Temperature> FAHRENHEIT = CELSIUS.multiply(5.0/9.0).shift(-32);
  // limit size of matched number to (possibly) avoid hangups
  // we need a different regex for including a word boundary (\b), instead of just prepending that
  // because otherwise negative numbers aren't correctly recognized
  protected static final String NUMBER_REGEX = "(-?[0-9]{1,32}[0-9,.]{0,32})";
  protected static final String NUMBER_REGEX_WITH_BOUNDARY = "(-?\\b[0-9]{1,32}[0-9,.]{0,32})";

  protected final Pattern numberRangePart = Pattern.compile(NUMBER_REGEX_WITH_BOUNDARY + "$");
  
  private static final double DELTA = 1e-2;
  private static final double ROUNDING_DELTA = 0.05;
  private static final int MAX_SUGGESTIONS = 5;
  private static final int WHITESPACE_LIMIT = 5;

  protected Map<Pattern, Unit> unitPatterns = new LinkedHashMap<>();  // use LinkedHashMap for stable iteration order

  // for patterns that require a custom number parsing function
  protected Map<Pattern, Map.Entry<Unit, Function<MatchResult, Double>>> specialPatterns = new HashMap<>();
  protected Map<Unit, List<String>> unitSymbols = new HashMap<>();
  // for recognizing conversions made by this rule or the user
  protected List<Pattern> convertedPatterns = new ArrayList<>();
  // units to use for conversions
  protected final List<Unit> metricUnits = new ArrayList<>();

  protected enum Message {
    SUGGESTION,
    CHECK,
    CHECK_UNKNOWN_UNIT,
    UNIT_MISMATCH
  }

  private final static List<Pattern> antiPatterns = Arrays.asList(
          Pattern.compile("\\s?\\d+'\\d\\d\\d\\s?"),   // "100'000", thousands separator in de-CH
          Pattern.compile("\\d+[-‐–]\\d+"),   // "3-5 pounds"
          Pattern.compile("\\d+/\\d+"),   // "1/4 mile"
          Pattern.compile("\\d+:\\d+"),   // "A 2:1 cup"
          Pattern.compile("Pfund Sterling"),   // "1.800 Pfund Sterling" (German)
          Pattern.compile("\\d+⁄\\d+")    // "1⁄4 cup" (it's not the standard slash)
  );

  private URL buildURLForExplanation(String original) {
    try {
      String query = URLEncoder.encode("convert " + original + " to metric", "utf-8");
      return new URL("http://www.wolframalpha.com/input/?i=" + query);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Override in subclasses
   * @return locale-specific number format
   */
  protected NumberFormat getNumberFormat() {
    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(2);
    df.setRoundingMode(RoundingMode.HALF_UP);
    return df;
  }

  /**
   * Override in subclasses
   */
  protected String getMessage(Message message) {
    switch(message) {
      case CHECK:
        return "This unit conversion doesn't seem right. Do you want to correct it automatically?";
      case SUGGESTION:
        return "Writing for an international audience? Consider adding the metric equivalent.";
      case CHECK_UNKNOWN_UNIT:
        return "This unit conversion doesn't seem right, unable to recognize the used unit.";
      case UNIT_MISMATCH:
        return "These units don't seem to be compatible.";
      default:
        throw new RuntimeException("Unknown message type: " + message);
    }
  }

  /**
   * Override in subclasses
   */
  protected String getShortMessage(Message message) {
    switch(message) {
      case CHECK:
        return "Incorrect unit conversion. Correct it?";
      case SUGGESTION:
        return "Add metric equivalent?";
      case CHECK_UNKNOWN_UNIT:
        return "Unknown unit used in conversion.";
      case UNIT_MISMATCH:
        return "Units incompatible.";
      default:
        throw new RuntimeException("Unknown message type: " + message);
    }
  }

  /**
   * Format suggestion.
   * @param original matched in the text
   * @param converted computed by this rule
   */
  protected String getSuggestion(String original, String converted) {
    return original + " (" + converted + ")";
  }

  /**
   * Override in subclasses.
   * @return formatting of rounded numbers according to locale
   */
  protected String formatRounded(String s) {
    return "ca. " + s;
  }

  /**
   * Associate a notation with a given unit.
   * @param pattern Regex for recognizing the unit. Word boundaries and numbers are added to this pattern by addUnit itself.
   * @param base Unit to associate with the pattern
   * @param symbol Suffix used for suggestion.
   * @param factor Convenience parameter for prefixes for metric units, unit is multiplied with this. Defaults to 1 if not used.
   * @param metric Register this notation for suggestion.
   */
  protected void addUnit(String pattern, Unit base, String symbol, double factor, boolean metric) {
    Unit unit = base.multiply(factor);
    unitPatterns.put(Pattern.compile(NUMBER_REGEX_WITH_BOUNDARY + "[\\s\u00A0]{0," + WHITESPACE_LIMIT + "}" + pattern + "\\b"), unit);
    unitSymbols.putIfAbsent(unit, new ArrayList<>());
    unitSymbols.get(unit).add(symbol);
    if (metric && !metricUnits.contains(unit)) {
      metricUnits.add(unit);
    }
  }

  protected AbstractUnitConversionRule(ResourceBundle messages) {
    setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);

    addUnit("kg", KILOGRAM, "kg", 1e0, true);
    addUnit("g", KILOGRAM, "g", 1e-3, true);
    addUnit("t", KILOGRAM, "t", 1e3, true);

    addUnit("lb", POUND, "lb", 1, false);
    addUnit("oz", OUNCE, "oz", 1, false);

    addUnit("mi", MILE, "mi", 1, false);
    addUnit("yd", YARD, "yd", 1, false);
    // negative lookahead here to avoid matching "'s" and so on
    addUnit("(?:ft|′|')(?!(\\w|\\d))", FEET, "ft", 1, false);
    // removed 'in', " because of many false positives
    addUnit("(?:inch|″)(?!(\\w|\\d))", INCH, "inch", 1, false);

    addUnit("(?:km/h|kmh)", KILOMETRE_PER_HOUR, "km/h", 1, true);
    addUnit("(?:mph)", MILE.divide(HOUR), "mph", 1, false);

    addUnit("km", METRE, "km", 1e3, true);
    addUnit("m", METRE, "m",   1e0, true);
    //addUnit("dm", METRE, "dm", 1e-1,  /*true*/); // Metric, but not commonly used
    addUnit("cm", METRE, "cm", 1e-2, true);
    addUnit("mm", METRE, "mm", 1e-3, true);
    addUnit("µm", METRE, "µm", 1e-6, true);
    addUnit("nm", METRE, "nm", 1e-9, true);

    addUnit("m(?:\\^2|2|²)", SQUARE_METRE, "m²", 1, true);
    addUnit("ha", SQUARE_METRE, "ha", 1e4, true);
    addUnit("a", SQUARE_METRE, "a", 1e2, true);
    addUnit("km(?:\\^2|2|²)", SQUARE_METRE, "km²", 1e6, true);
    //addUnit("dm(?:\\^2|2|²)", SQUARE_METRE, "dm²", 1e-2,  false/*true*/); // Metric, but not commonly used
    addUnit("cm(?:\\^2|2|²)", SQUARE_METRE, "cm²", 1e-4, true);
    addUnit("mm(?:\\^2|2|²)", SQUARE_METRE, "mm²", 1e-6, true);
    addUnit("µm(?:\\^2|2|²)", SQUARE_METRE, "µm²", 1e-12, true);
    addUnit("nm(?:\\^2|2|²)", SQUARE_METRE, "nm²", 1e-18, true);

    addUnit("(?:sq|square) (?:in(?:ch)?|inches)", INCH.multiply(INCH), "sq in", 1, false);
    addUnit("(?:inches|in|inch) (?:\\^2|2|²)", INCH.multiply(INCH), "in²", 1, false);

    addUnit("(?:sq|square) (?:ft|feet|foot)", FEET.multiply(FEET), "sq ft", 1, false);
    addUnit("sf", FEET.multiply(FEET), "sf", 1, false);
    addUnit("ft(?:\\^2|2|²)", FEET.multiply(FEET), "ft²", 1, false);

    addUnit("(?:sq|square) (?:yds?|yards?)", YARD.multiply(YARD), "sq yd", 1, false);
    addUnit("(?:yards?|yds?)(?:\\^2|2|²)", YARD.multiply(YARD), "yd²", 1, false);

    addUnit("m(?:\\^3|3|³)", CUBIC_METRE, "m³", 1, true);
    addUnit("km(?:\\^3|3|³)", CUBIC_METRE, "km³", 1e9, true);
    //addUnit("dm(?:\\^3|3|³)", CUBIC_METRE, "dm³", 1e-3,  false/*true*/); // Metric, but not commonly used
    addUnit("cm(?:\\^3|3|³)", CUBIC_METRE, "cm³", 1e-6, true);
    addUnit("mm(?:\\^3|3|³)", CUBIC_METRE, "mm³", 1e-9, true);
    addUnit("µm(?:\\^3|3|³)", CUBIC_METRE, "µm³", 1e-18, true);
    addUnit("nm(?:\\^3|3|³)", CUBIC_METRE, "nm³", 1e-27, true);

    addUnit("(?:cubic|cu) (?:feet|ft|foot)", FEET.multiply(FEET).multiply(FEET), "cubic feet", 1, false);
    addUnit("(?:feet|ft|foot)(?:\\^3|3|³)", FEET.multiply(FEET).multiply(FEET), "ft³", 1, false);

    addUnit("(?:cubic|cu) (?:inch|in|inches)", INCH.multiply(INCH).multiply(INCH), "cubic inch", 1, false);
    addUnit("(?:inch|in)(?:\\^3|3|³)", INCH.multiply(INCH).multiply(INCH), "inch³", 1, false);

    addUnit("(?:cubic|cu) (?:yards?|yds?)", YARD.multiply(YARD).multiply(YARD), "cubic yard", 1, false);
    addUnit("(?:yard|yd)(?:\\^3|3|³)", YARD.multiply(YARD).multiply(YARD), "yard³", 1, false);

    addUnit("l", LITRE, "l", 1, true);
    addUnit("ml", LITRE, "ml", 1e-3, true);

    addUnit("°F", FAHRENHEIT, "°F", 1, false);
    addUnit("°C", CELSIUS, "°C", 1, true);

    convertedPatterns.add(Pattern.compile("\\s*\\((?:ca. )?" + NUMBER_REGEX + "\\s*([^)]+)\\s*\\)"));

    // recognizes 5'6" = 5 feet + 6 inches = 5.5 feet
    Function<MatchResult, Double> parseFeetAndInch = match -> {
      double feet, inch;
      try {
        feet = getNumberFormat().parse(match.group(1)).doubleValue();
      } catch (ParseException e) {
        return null;
      }
      try {
        inch = getNumberFormat().parse(match.group(2)).doubleValue();
      } catch (ParseException e) {
        inch = 0.0;
      }
      return feet + inch / 12.0;
    };
    Map.Entry<Unit, Function<MatchResult, Double>> feetAndInchEntry = new AbstractMap.SimpleImmutableEntry<>( FEET, parseFeetAndInch );
    specialPatterns.put(Pattern.compile("(?:(?<=[^º°\\d]))\\s(\\d+)(?:ft|′|')\\s*(\\d+)\\s*(?:in|\"|″)?"), feetAndInchEntry);
    specialPatterns.put(Pattern.compile("(?:(?<=[^º°\\d\\s]))(\\d+)(?:ft|′|')\\s*(\\d+)\\s*(?:in|\"|″)?"), feetAndInchEntry);
  }

  /**
   * @param value number to convert
   * @param unit unit used in text
   * @return suggestions of the given number converted into metric units, sorted by naturalness
   *         or null if conversion is not necessary / was not possible
   */
  @Nullable
  protected List<Map.Entry<Unit, Double>> getMetricEquivalent(double value, @NotNull Unit unit) {
    LinkedList<Map.Entry<Unit, Double>> conversions = new LinkedList<>();
    for (Unit metric : metricUnits) {
      if (unit.equals(metric)) { // don't convert to itself
        return null;
      }
      if (unit.isCompatible(metric)) {
        Double converted = unit.getConverterTo(metric).convert(value);
        conversions.add(new AbstractMap.SimpleImmutableEntry<>(metric, converted));
      }
    }
    sortByNaturalness(conversions);
    if (conversions.isEmpty()) {
      return null;
    } else {
      return conversions;
    }
  }

  @Nullable
  protected List<String> formatMeasurement(double value, @NotNull Unit unit) {
    List<Map.Entry<Unit, Double>> equivalents = getMetricEquivalent(value, unit);
    if (equivalents == null) {
      return null;
    }
    List<String> formatted = getFormattedConversions(equivalents);
    if (formatted.isEmpty()) {
      return null;
    }
    return formatted;
  }

  /**
   * Adds different formatted variants of the given conversions up to MAX_SUGGESTIONS.
   * @param conversions as computed by getMetricEquivalent
   * @return formatted numbers, with various units and unit symbols, rounded to integers or according to getNumberFormat
   */
  @NotNull
  private List<String> getFormattedConversions(List<Map.Entry<Unit, Double>> conversions) {
    List<String> formatted = new ArrayList<>();
    for (Map.Entry<Unit, Double> equivalent : conversions) {
      Unit metric = equivalent.getKey();
      double converted = equivalent.getValue();
      long rounded = Math.round(converted);
      for (String symbol : unitSymbols.getOrDefault(metric, new ArrayList<>())) {
        if (formatted.size() > MAX_SUGGESTIONS) {
          break;
        }
        if (Math.abs(converted - rounded) / Math.abs(converted) < ROUNDING_DELTA && rounded != 0) {
          String formattedStr = formatRounded(getNumberFormat().format(rounded) + " " + symbol);
          if (!formatted.contains(formattedStr)) {
            formatted.add(formattedStr);
          }
        }
        String formattedNumber = getNumberFormat().format(converted);
        String formattedStr = formattedNumber + " " + symbol;
        // TODO: be cleverer than !equals("0"), can prevent valid conversions
        if (!formatted.contains(formattedStr) && !formattedNumber.equals("0")) {
          formatted.add(formattedStr);
        }
      }
    }
    return formatted;
  }

  private void sortByNaturalness(List<Map.Entry<Unit, Double>> conversions) {
    conversions.sort((a, b) -> { // sort according to "naturalness" of this unit, i.e. numbers not being too small/large
      DoubleUnaryOperator naturalness = number -> { // smaller score -> better
        double abs = Math.abs(number);
        if (abs < 1.0) {
          return 1.0 / (abs * abs * 2);
        } else if (abs < 100) {
          return abs - 50;
        } else {
          return abs * abs;
        }
      };
      double scoreA = naturalness.applyAsDouble(a.getValue());
      double scoreB = naturalness.applyAsDouble(b.getValue());
      return Double.compare(scoreA, scoreB);
    });
  }

  private void matchUnits(AnalyzedSentence sentence, List<RuleMatch> matches, List<Map.Entry<Integer, Integer>> ignoreRanges, boolean isMetric) {
    for (Pattern unitPattern : unitPatterns.keySet()) { // find specific unit through lookup of pattern
      if (metricUnits.contains(unitPatterns.get(unitPattern)) != isMetric) {
        continue;
      }
      Matcher unitMatcher = unitPattern.matcher(sentence.getText());
      while (unitMatcher.find()) {
        boolean ignore = false;
        for (Map.Entry<Integer, Integer> range : ignoreRanges) {
          if (unitMatcher.start() >= range.getKey() && unitMatcher.end() <= range.getValue()) {
            ignore = true;
            break;
          }
        }
        if (!ignore) {
          tryConversion(sentence, matches, unitPattern, null, null, unitMatcher, ignoreRanges);
        }
      }
    }
  }


  protected boolean detectNumberRange(AnalyzedSentence sentence, Matcher matcher) {
    boolean hyphenInNumber = matcher.group(1).startsWith("-");
    if (!hyphenInNumber) {
      return false;
    }
    String textBefore = sentence.getText().substring(0, matcher.start());
    return numberRangePart.matcher(textBefore).find();
  }

  private void tryConversion(AnalyzedSentence sentence, List<RuleMatch> matches, Pattern unitPattern, Double customValue, Unit customUnit, Matcher unitMatcher, List<Map.Entry<Integer, Integer>> ignoreRanges) {
    Map.Entry<Integer, Integer> range = new AbstractMap.SimpleImmutableEntry<>(
      unitMatcher.start(), unitMatcher.end());
    ignoreRanges.add(range);
    // search for an existing conversion, e.g. "5 miles (8km)"
    String convertedInText = null;
    int convertedOffset = unitMatcher.end();
    Matcher convertedMatcher = null;
    for (Pattern convertedPattern : convertedPatterns) {
      convertedMatcher = convertedPattern.matcher(sentence.getText().substring(convertedOffset));
      if (convertedMatcher.find() && convertedMatcher.start() == 0) {
        convertedInText = convertedMatcher.group(0);
        break;
      }
    }
    // customValue/unit are used with patterns in specialPatterns, where unit and value are already extracted
    Unit unit = unitPatterns.getOrDefault(unitPattern, customUnit);
    double value;
    if (customValue == null) {
      try {
        String valueAsString = unitMatcher.group(1);
        // remove hyphen at start if it belongs to a range (e.g 1-5 miles)
        // see https://github.com/languagetool-org/languagetool/issues/2170
        // TODO convert whole range, not only end
        if (detectNumberRange(sentence, unitMatcher)) {
          valueAsString = valueAsString.substring(1);
        }
        value = getNumberFormat().parse(valueAsString).doubleValue();
      } catch (ParseException e) {
        return;
      }
    } else {
      value = customValue;
    }
    List<String> converted = formatMeasurement(value, unit);
    if (converted == null && convertedInText == null) {
      // no conversion necessary, e.g. already metric
    } else if (convertedInText == null) { // no conversion found -> suggest one
      RuleMatch match = new RuleMatch(this, sentence, unitMatcher.start(), unitMatcher.end(),
        getMessage(Message.SUGGESTION), getShortMessage(Message.SUGGESTION));
      List<String> suggestions = converted.stream()
        .map(formatted -> getSuggestion(unitMatcher.group(0), formatted))
        .collect(Collectors.toList());
      match.setSuggestedReplacements(suggestions);
      match.setUrl(buildURLForExplanation(unitMatcher.group(0)));
      matches.add(match);
    } else { // check given conversion for accuracy
      Map.Entry<Integer, Integer> convertedRange = new AbstractMap.SimpleImmutableEntry<>(
        convertedMatcher.start(0) + convertedOffset, convertedMatcher.end(0) + convertedOffset);
      ignoreRanges.add(convertedRange);

      // already using one of our conversions?
      String finalConvertedInText = convertedInText.trim();
      String convertedTrimmed = finalConvertedInText.substring(1, finalConvertedInText.length()-1);
      if (converted != null && converted.stream().anyMatch(s -> s.equals(convertedTrimmed))) {
        return;
      }
      Optional<Pattern> convertedUnitPattern = unitPatterns.keySet().stream()
        .filter(pattern -> pattern.matcher(finalConvertedInText).find())
        .findFirst();
      if (convertedUnitPattern.isPresent()) { // known unit used for conversion
        Unit convertedUnit = unitPatterns.get(convertedUnitPattern.get());
        Double convertedValueInText;
        try {
          convertedValueInText = getNumberFormat().parse(convertedMatcher.group(1)).doubleValue();
          if (convertedMatcher.group().trim().matches("\\(\\d+ (feet|ft) \\d+ inch\\)")) {
            // e.g. "(2 ft 6 inch)" would be interpreted as just "2 ft", given a wrong suggestion
            return;
          }
        } catch (ParseException e) {
          return;
        }
        if (converted == null) { // already metric, check conversion in convertedUnit / convertedValueInText (order may be reversed)
          List<String> reverseConverted = null;
          try {
            double unitConverted = unit.getConverterTo(convertedUnit).convert(value);
            double diff = Math.abs(unitConverted - convertedValueInText);
            if (diff > DELTA) {
              RuleMatch match = new RuleMatch(this, sentence,
                convertedMatcher.start(1) + convertedOffset, convertedMatcher.end(1) + convertedOffset,
                getMessage(Message.CHECK), getShortMessage(Message.CHECK));
              match.setUrl(buildURLForExplanation(convertedTrimmed));
              List<Map.Entry<Unit, Double>> numbers = new ArrayList<>();
              numbers.add(new AbstractMap.SimpleImmutableEntry<>(convertedUnit, unitConverted));
              reverseConverted = getFormattedConversions(numbers);
              if (reverseConverted.stream().anyMatch(s -> s.equals(convertedTrimmed))) {
                return;
              }
              match.setSuggestedReplacements(reverseConverted);
              matches.add(match);
            }
          } catch (UnconvertibleException e) {
            RuleMatch match = new RuleMatch(this, sentence, unitMatcher.start(), convertedMatcher.end() + convertedOffset,
              getMessage(Message.UNIT_MISMATCH), getShortMessage(Message.UNIT_MISMATCH));
            if (reverseConverted != null) {
              match.setSuggestedReplacements(reverseConverted);
            }
            match.setUrl(buildURLForExplanation(convertedTrimmed));
            matches.add(match);
          }
        } else { // found conversion to metric, check for accuracy
          List<Map.Entry<Unit, Double>> metricEquivalents = getMetricEquivalent(value, unit);
          if (metricEquivalents == null || metricEquivalents.isEmpty()) {
            return;
          }
          Map.Entry<Unit, Double> metricEquivalent = metricEquivalents.get(0);
          Unit metricUnit = metricEquivalent.getKey();
          Double convertedValueComputed = metricEquivalent.getValue();
          String original = unitMatcher.group(0);
          List<String> corrected = converted.stream()
            .map(suggestion -> getSuggestion(original, suggestion)).collect(Collectors.toList());
          if (!(convertedUnit.equals(metricUnit) && Math.abs(convertedValueInText - convertedValueComputed) < DELTA)) {
            RuleMatch match = new RuleMatch(this, sentence,
              unitMatcher.start(), convertedMatcher.end(0) + convertedOffset,
              getMessage(Message.CHECK), getShortMessage(Message.CHECK));
            match.setSuggestedReplacements(corrected);
            match.setUrl(buildURLForExplanation(unitMatcher.group(0)));
            matches.add(match);
          }
        }
      } else if (converted != null) { // unknown unit used for conversion
        RuleMatch match = new RuleMatch(this, sentence,
          convertedMatcher.start(1) + convertedOffset, convertedMatcher.end(2) + convertedOffset,
          getMessage(Message.CHECK_UNKNOWN_UNIT), getShortMessage(Message.CHECK_UNKNOWN_UNIT));
        match.setSuggestedReplacements(converted);
        matches.add(match);
      }
    }
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> matches = new ArrayList<>();
    List<Map.Entry<Integer, Integer>> ignoreRanges = new LinkedList<>();

    // handle special patterns where simple number parsing is not enough, e.g. 5'6"
    for (Pattern specialPattern : specialPatterns.keySet()) {
      Matcher matcher = specialPattern.matcher(sentence.getText());
      while (matcher.find()) {
        MatchResult result = matcher.toMatchResult();
        Double value = specialPatterns.get(specialPattern).getValue().apply(result);
        Unit unit = specialPatterns.get(specialPattern).getKey();
        if (value == null) {
          continue;
        }
        boolean ignore = false;
        for (Map.Entry<Integer, Integer> range : ignoreRanges) {
          if (matcher.start() >= range.getKey() && matcher.end() <= range.getValue()) {
            ignore = true;
            break;
          }
        }
        if (!ignore) {
          tryConversion(sentence, matches, specialPattern, value, unit, matcher, ignoreRanges);
        }
      }
    }

    // check for numbers with a given set of units (e.g. imperial)

    // two runs: first metric units, so that ignore ranges are set up properly
    // then match other units
    // should fix sentences like 10 km (5 miles), where 5 miles matches first and matching 10 km first would have prevented that
    // there should be no influence on other results
    matchUnits(sentence, matches, ignoreRanges, true);
    matchUnits(sentence, matches, ignoreRanges, false);
    Map<Integer, RuleMatch> matchesByStart = new HashMap<>();
    // deduplicate matches with equal start, longer match should win, e.g. miles per hour over just miles
    for (RuleMatch match : matches) {
      matchesByStart.compute(match.getFromPos(), (pos, other) ->
        other == null ? match :
        match.getToPos() > other.getToPos() ? match : other);
    }
    if (matches.size() > 0) {
      removeAntiPatternMatches(sentence, matchesByStart);
    }
    return matchesByStart.values().toArray(new RuleMatch[0]);
  }

  private void removeAntiPatternMatches(AnalyzedSentence sentence, Map<Integer, RuleMatch> matchesByStart) {
    for (Pattern antiPattern : antiPatterns) {
      String text = sentence.getText();
      Matcher matcher = antiPattern.matcher(text);
      int pos = 0;
      while (pos < text.length() && matcher.find(pos)) {
        matchesByStart.entrySet().removeIf(entry ->
                matcher.start() <= entry.getValue().getFromPos() && matcher.end() >= entry.getValue().getFromPos() ||
                matcher.start() <= entry.getValue().getToPos() && matcher.end() >= entry.getValue().getToPos()
        );
        pos = matcher.end() + 1;
      }
    }
  }

}
