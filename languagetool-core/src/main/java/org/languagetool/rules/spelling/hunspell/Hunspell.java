package org.languagetool.rules.spelling.hunspell;

import dumonts.hunspell.bindings.HunspellLibrary;
import org.bridj.Pointer;
import org.languagetool.JLanguageTool;
import org.languagetool.broker.ResourceDataBroker;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public final class Hunspell {
  static class LanguageAndPath {
    private final Path dictionary;
    private final Path affix;
    LanguageAndPath(Path dictionary, Path affix) {
      this.dictionary = Objects.requireNonNull(dictionary);
      this.affix = Objects.requireNonNull(affix);
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      LanguageAndPath that = (LanguageAndPath) o;
      return Objects.equals(dictionary, that.dictionary) &&
          Objects.equals(affix, that.affix);
    }
    @Override
    public int hashCode() {
      return Objects.hash(dictionary, affix);
    }
  }

  private static final Map<LanguageAndPath, HunspellDictionary> map = new HashMap<>();
  private static BiFunction<Path, Path, HunspellDictionary> hunspellDictionaryFactory = DumontsHunspellDictionary::new;

  public static void setHunspellDictionaryFactory(BiFunction<Path, Path, HunspellDictionary> factory) {
    hunspellDictionaryFactory = factory;
  }

  public static synchronized HunspellDictionary getDictionary(Path dictionary, Path affix) {
    LanguageAndPath key = new LanguageAndPath(dictionary, affix);
    HunspellDictionary hunspell = map.get(key);
    if (hunspell != null) {
      return hunspell;
    }
    HunspellDictionary newHunspell = hunspellDictionaryFactory.apply(dictionary, affix);
    map.put(key, newHunspell);
    return newHunspell;
  }

  public static HunspellDictionary forDictionaryInResources(String language, String resourcePath) {
    try {
      ResourceDataBroker broker = JLanguageTool.getDataBroker();
      InputStream dictionaryStream = broker.getAsStream(resourcePath + language + ".dic");
      InputStream affixStream = broker.getAsStream(resourcePath + language + ".aff");
      if (dictionaryStream == null || affixStream == null) {
        throw new RuntimeException("Could not find dictionary for language \"" + language + "\" in classpath");
      }
      Path dictionary = Files.createTempFile(language, ".dic");
      Path affix = Files.createTempFile(language, ".aff");
      Files.copy(dictionaryStream, dictionary, StandardCopyOption.REPLACE_EXISTING);
      Files.copy(affixStream, affix, StandardCopyOption.REPLACE_EXISTING);
      return hunspellDictionaryFactory.apply(dictionary, affix);
    } catch (IOException e) {
      throw new RuntimeException("Could not create temporary dictionaries for language \"" + language + "\"", e);
    }
  }

  public static HunspellDictionary forDictionaryInResources(String language) {
    return forDictionaryInResources(language, "");
  }
}
