package org.languagetool.rules.spelling.symspell.implementation;//        MIT License
//
//        Copyright (c) 2018 Hampus Londögård
//
//        Permission is hereby granted, free of charge, to any person obtaining a copy
//        of this software and associated documentation files (the "Software"), to deal
//        in the Software without restriction, including without limitation the rights
//        to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//        copies of the Software, and to permit persons to whom the Software is
//        furnished to do so, subject to the following conditions:
//
//        The above copyright notice and this permission notice shall be included in all
//        copies or substantial portions of the Software.
//
//        THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//        IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//        FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//        AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//        LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//        OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//        SOFTWARE.

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class SymSpellDemo {
    private int termIndex = 0;
    private int countIndex = 1;
    private String path ="data/frequency_dictionary_en_82_765.txt";
    private SymSpell.Verbosity suggestionVerbosity = SymSpell.Verbosity.All; //Top, Closest, All
    private int maxEditDistanceLookup; //max edit distance per lookup (maxEditDistanceLookup<=maxEditDistanceDictionary)
    private SymSpell symSpell;

    private SymSpellDemo(int maxEditDistanceLookup) throws FileNotFoundException {
        symSpell = new SymSpell(-1, maxEditDistanceLookup, -1, 10);//, (byte)18);
        this.maxEditDistanceLookup = maxEditDistanceLookup;
        if(!symSpell.loadDictionary(path, termIndex, countIndex))throw new FileNotFoundException("File not found");
    }

    private List<SuggestItem> lookup(String input){
        return symSpell.lookup(input, suggestionVerbosity, maxEditDistanceLookup);
    }

    private SuggestItem lookupCompound(String input){
        return symSpell.lookupCompound(input, maxEditDistanceLookup).get(0);
    }

    public static void main(String[] args) throws IOException {
        SymSpellDemo symSpell = new SymSpellDemo(3);
        //verbosity=Top: the suggestion with the highest term frequency of the suggestions of smallest edit distance found
        //verbosity=Closest: all suggestions of smallest edit distance found, the suggestions are ordered by term frequency
        //verbosity=All: all suggestions <= maxEditDistance, the suggestions are ordered by edit distance, then by term frequency (slower, no early termination)
        // IE All is the only one to give suggestions if a word with exact match is found.

        String inputTerm;
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            System.out.println("Enter input:");
            inputTerm = br.readLine();
            List<SuggestItem> suggestions = symSpell.lookup(inputTerm);
            SuggestItem compound = symSpell.lookupCompound(inputTerm);

            suggestions.stream()
                    .limit(10)
                    .forEach(suggestion -> System.out.println("Lookup suggestion: " + suggestion.term + " " + suggestion.distance + " " + suggestion.count));
            System.out.println("LookupCompound: " + compound.term);
        }
    }
}
