
## Intro

Code-switching

Das ist echt gut.
This is really nice.

Das ist echt gut in Berlin.  (in Berlin = DE oder EN)
Das ist echt gut i Berlin.

Das ist echt nice.
Das ist echt nice von dir.
Das ist echt very nice.
Das ist echt very nice von dir.
I'm not going to do that, sagte der Kapitän.
"I'm not going to do that", sagte der Kapitän.

This is a test, sagte der Engländer.  
Tricky cases: what if a typo is at the language boundary?
 
This is a testx, sagte der Engländer.  
This is a test, sagtex der Engländer.

- Use both languages for suggestion corrections at that location?
- Use the languages that has a closer suggestion? (Would also require getting suggestions for both languages)

To simplify, what if we assign the same language to:
1. all tokens of a sentence?
2. all tokens up to a delimiter like `[;,.:?!"\n]`

## Evaluation

Translate DE to EN using deepl and create new sentences that are x% DE and y% EN.
Check whether we properly detect the code-switching position(s).


## Implementation

1. detect main language (we do that already)
2. detect whether the second language is used at all:
   iterate over all non-main language words (according to spell checker): count words which are valid words in second language
3. annotate words with their language + probability
4. create two texts and check them: main language text, second language text

### Code changes

- LanguageAnnotator - new class
- extend AnalyzedToken to take language
- extend JLanguageTool to take two languages
- test - maybe some rules need to be turned off because incomplete sentences might be checked
