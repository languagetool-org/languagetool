package org.languagetool.rules.spelling.hunspell;

import dumonts.hunspell.bindings.HunspellLibrary;
import org.bridj.Pointer;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class Hunspell implements Closeable {
  private final Pointer<HunspellLibrary.Hunhandle> handle;
  private final Charset charset;
  
  private static final Map<LanguageAndPath, Hunspell> map = new HashMap<>();
  
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

  public Hunspell(Path dictionary, Path affix) {
    Pointer<Byte> aff = Pointer.pointerToCString(affix.toString());
    Pointer<Byte> dic = Pointer.pointerToCString(dictionary.toString());
    handle = HunspellLibrary.Hunspell_create(aff, dic);
    charset = Charset.forName(HunspellLibrary.Hunspell_get_dic_encoding(handle).getCString());
    if (this.handle == null) {
      throw new RuntimeException("Unable to create Hunspell instance");
    }
  }
  
  public synchronized static Hunspell getInstance(Path dictionary, Path affix) {
    LanguageAndPath key = new LanguageAndPath(dictionary, affix);
    Hunspell hunspell = map.get(key);
    if (hunspell != null) {
      return hunspell;
    }
    Hunspell newHunspell = new Hunspell(dictionary, affix);
    map.put(key, newHunspell);
    return newHunspell;
  }

  public static Hunspell forDictionaryInResources(String language, String resourcePath) {
    try {
      ClassLoader loader = Hunspell.class.getClassLoader();
      InputStream dictionaryStream = loader.getResourceAsStream(resourcePath + language + ".dic");
      InputStream affixStream = loader.getResourceAsStream(resourcePath + language + ".aff");
      if (dictionaryStream == null || affixStream == null) {
        throw new RuntimeException("Could not find dictionary for language \"" + language + "\" in classpath");
      }
      Path dictionary = Files.createTempFile(language, ".dic");
      Path affix = Files.createTempFile(language, ".aff");
      Files.copy(dictionaryStream, dictionary, StandardCopyOption.REPLACE_EXISTING);
      Files.copy(affixStream, affix, StandardCopyOption.REPLACE_EXISTING);
      return new Hunspell(dictionary, affix);
    } catch (IOException e) {
      throw new RuntimeException("Could not create temporary dictionaries for language \"" + language + "\"", e);
    }
  }

  public static Hunspell forDictionaryInResources(String language) {
    return forDictionaryInResources(language, "");
  }

  public boolean spell(String word) {
    if (handle == null) {
      throw new RuntimeException("Attempt to use hunspell instance after closing");
    }
    @SuppressWarnings("unchecked")
    Pointer<Byte> str = (Pointer<Byte>) Pointer.pointerToString(word, Pointer.StringType.C, charset);
    int result = HunspellLibrary.Hunspell_spell(handle, str);
    return result != 0;
  }

  public void add(String word) {
    if (handle == null) {
      throw new RuntimeException("Attempt to use hunspell instance after closing");
    }
    @SuppressWarnings("unchecked")
    Pointer<Byte> str = (Pointer<Byte>) Pointer.pointerToString(word, Pointer.StringType.C, charset);
    HunspellLibrary.Hunspell_add(handle, str);
  }

  public List<String> suggest(String word) {
    // Create pointer to native string
    @SuppressWarnings("unchecked")
    Pointer<Byte> str = (Pointer<Byte>) Pointer.pointerToString(word, Pointer.StringType.C, charset);
    // Create pointer to native string array
    Pointer<Pointer<Pointer<Byte>>> nativeSuggestionArray = Pointer.allocatePointerPointer(Byte.class);
    // Hunspell will allocate the array and fill it with suggestions
    int suggestionCount = HunspellLibrary.Hunspell_suggest(handle, nativeSuggestionArray, str);
    if (suggestionCount == 0) {
      // Return early and don't try to free the array
      return new ArrayList<>();
    }
    // Ask bridj for a `java.util.List` that wraps `nativeSuggestionArray`
    List<Pointer<Byte>> nativeSuggestionList = nativeSuggestionArray.get().validElements(suggestionCount).asList();
    // Convert C Strings to java strings
    List<String> suggestions = nativeSuggestionList.stream().map((p) -> p.getStringAtOffset(0, Pointer.StringType.C, charset)).collect(Collectors.toList());

    // We can free the underlying buffer now because Java's `String` owns it's own memory
    HunspellLibrary.Hunspell_free_list(handle, nativeSuggestionArray, suggestionCount);
    return suggestions;
  }

  public void close() {
    if (handle != null) {
      HunspellLibrary.Hunspell_destroy(handle);
    }
  }
}
