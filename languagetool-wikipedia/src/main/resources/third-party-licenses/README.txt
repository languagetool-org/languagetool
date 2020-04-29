LanguageTool is published under the GNU Lesser General Public License (LGPL).
For more detailed license information, see COPYING.txt and README.md.

This is the list of libraries used by LanguageTool and their licenses:

annotations.jar: Apache License 2.0, http://search.maven.org/#search|ga|1|com.intellij.annotations
aho-corasick-double-array-trie.jar: Apache License 2.0, https://github.com/hankcs/AhoCorasickDoubleArrayTrie
bridj.jar: Copyright (c) 2010-2015 Olivier Chafik, BSD License (BSD.txt), https://github.com/nativelibs4java/BridJ
catalan-pos-dict.jar: Creative Commons Attribution-ShareAlike 4.0 International, https://creativecommons.org/licenses/by-sa/4.0/legalcode
cjftransform.jar: Apache License 2.0, http://code.google.com/p/cjftransform/
commons-*: Apache License 2.0, http://commons.apache.org/lang/
german-pos-dict.jar: Creative Commons Attribution-ShareAlike 4.0 International, https://creativecommons.org/licenses/by-sa/4.0/legalcode
guava.jar: Apache License 2.0, https://code.google.com/p/guava-libraries/
hamcrest-core.jar: BSD License, https://github.com/hamcrest
hppc.jar: Apache License 2.0, http://labs.carrotsearch.com/hppc.html
HanLP: Apache License 2.0, https://github.com/hankcs/HanLP
hunspell.jar: Apache License 2.0, https://gitlab.com/dumonts/hunspell-java
indriya.jar: BSD License, https://github.com/unitsofmeasurement/indriya
jakarta-regexp.jar: Apache License 2.0, http://jakarta.apache.org/regexp/
java-string-similarity, adapted in parts in languagetool-core/src/main/java/org/languagetool/rules/spelling/morfologik/suggestions_ordering/DetailedDamerauLevenstheinDistance.java: MIT License, https://github.com/tdebatty/java-string-similarity
jopt-simple.jar: MIT License, http://pholser.github.io/jopt-simple/
jsonic.jar: Apache License 2.0, http://jsonic.sourceforge.jp/
junit.jar: Common Public License v 1.0, CPL.txt, http://www.junit.org
jwordsplitter.jar: Apache License 2.0, http://www.danielnaber.de/jwordsplitter/
language-detector.jar: Apache License 2.0, https://github.com/optimaize/language-detector
log4j.jar: Apache License 2.0, http://logging.apache.org/log4j/1.2/
lucene-analyzers-common|core|queries|sandbox|test-framework.jar: Apache License 2.0, http://lucene.apache.org
lucene-gosen-ipadic.jar: LGPL v2.1, http://code.google.com/p/lucene-gosen/
morfologik-*.jar: Morfologik-license.txt, http://morfologik.blogspot.com, http://www.cs.put.poznan.pl/dweiss
morfologik-ukrainian.jar: Creative Commons Attribution-ShareAlike 4.0 International, https://creativecommons.org/licenses/by-sa/4.0/legalcode
opennlp-*: Apache License 2.0, http://opennlp.apache.org, http://opennlp.sourceforge.net/models-1.5
ptk-common.jar: Apache License 2.0, http://sweble.org
rats-runtime.jar: LGPL version 2.1, http://cs.nyu.edu/rgrimm/xtc/
segment.jar: segment-license.txt, http://sourceforge.net/projects/segment/
slf4j-api.jar: MIT License, http://www.slf4j.org
slf4j-nop.jar: MIT License, http://www.slf4j.org
swc-engine.jar: Apache License 2.0, http://sweble.org
swc-parser-lazy.jar: Apache License 2.0, http://sweble.org
SymSpell, included in languagetool-core/src/main/java/org/languagetool/rules/spelling/symspell/implementation/: MIT License, https://github.com/Lundez/JavaSymSpell
trove4j.jar: Lesser GNU Public License (LGPL) 2.1 or later, https://mvnrepository.com/artifact/org.jetbrains.intellij.deps/trove4j
unit-api.jar: BSD License, http://unitsofmeasurement.github.io
utils.jar: part of Sweble, Apache License 2.0, http://sweble.org

Other resources like dictionaries are not technically libraries, they may have 
different licenses (including GPL) without affecting LanguageTool's licensing. 
For the dictionaries that are used for spell checking, see
./org/languagetool/resource/<xx>/hunspell/README*
and
./org/languagetool/resource/<xx>/spelling/README* (<xx> = language code).
