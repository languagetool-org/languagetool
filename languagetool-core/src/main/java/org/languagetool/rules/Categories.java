package org.languagetool.rules;

import java.util.Objects;
import java.util.ResourceBundle;

/**
 * Pre-defined rule categories.
 * @since 3.3
 */
public final class Categories {

  /** Rules about detecting uppercase words where lowercase is required and vice versa. */
  public static final Categories CASING = make("CASING", "category_case");

  /** Rules about spelling terms as one word or as as separate words. */
  public static final Categories COMPOUNDING = make("COMPOUNDING", "category_compounding");

  public static final Categories GRAMMAR = make("GRAMMAR", "category_grammar");

  /** Spelling issues. */
  public static final Categories TYPOS = make("TYPOS", "category_typo");

  public static final Categories PUNCTUATION = make("PUNCTUATION", "category_punctuation");

  /** Problems like incorrectly used dash or quote characters. */
  public static final Categories TYPOGRAPHY = make("TYPOGRAPHY", "category_typography");

  /** Words that are easily confused, like 'there' and 'their' in English. */
  public static final Categories CONFUSED_WORDS = make("CONFUSED_WORDS", "category_confused_words");

  public static final Categories REPETITIONS = make("REPETITIONS", "category_repetitions");
  
  public static final Categories REDUNDANCY = make("REDUNDANCY", "category_redundancy");
  
  public static final Categories REPETITIONS_STYLE = make("STYLE", "cateogry_repetitions_style");

  /** General style issues not covered by other categories, like overly verbose wording. */
  public static final Categories STYLE = make("STYLE", "category_style");


  /** Created to match PLAIN_ENGLISH XML category. */
  public static final Categories PLAIN_ENGLISH = make("PLAIN_ENGLISH", "category_plain_english");

  public static final Categories GENDER_NEUTRALITY = make("GENDER_NEUTRALITY", "category_gender_neutrality");

  /** Logic, content, and consistency problems. */
  public static final Categories SEMANTICS = make("SEMANTICS", "category_semantics");

  /** Colloquial style. */
  public static final Categories COLLOQUIALISMS = make("COLLOQUIALISMS", "category_colloquialism");

  /** Regionalisms: words used only in another language variant or used with different meanings. */
  public static final Categories REGIONALISMS = make("REGIONALISMS", "category_regionalisms");

  /** False friends: words easily confused by language learners because a similar word exists in their native language. */
  public static final Categories FALSE_FRIENDS = make("FALSE_FRIENDS", "category_false_friend");

  /** Rules that only make sense when editing Wikipedia (typically turned off by default in LanguageTool). */
  public static final Categories WIKIPEDIA = make("WIKIPEDIA", "category_wikipedia");

  /** Miscellaneous rules that don't fit elsewhere. */
  public static final Categories MISC = make("MISC", "category_misc");

  private static Categories make(String id, String message) {
    return new Categories(id, message);
  }

  private final String id;
  private final String messageKey;
  
  private Categories(String id, String messageKey) {
    this.id = Objects.requireNonNull(id);
    this.messageKey = Objects.requireNonNull(messageKey);
  }

  public CategoryId getId() {
    return new CategoryId(id);
  }

  public Category getCategory(ResourceBundle messages) {
    return new Category(new CategoryId(id), messages.getString(messageKey));
  }
}
