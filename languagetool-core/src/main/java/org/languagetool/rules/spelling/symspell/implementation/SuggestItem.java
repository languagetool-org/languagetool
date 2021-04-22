package org.languagetool.rules.spelling.symspell.implementation;
//        MIT License
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

import java.util.Comparator;

public class SuggestItem implements Comparator<SuggestItem>, Comparable<SuggestItem>
{
    /// <summary>The suggested correctly spelled word.</summary>
    public String term;
    /// <summary>Edit distance between searched for word and suggestion.</summary>
    public int distance;
    /// <summary>Frequency of suggestion in the dictionary (a measure of how common the word is).</summary>
    public long count;

    /// <summary>Create a new instance of SymSpell.SuggestItem.</summary>
    /// <param name="term">The suggested word.</param>
    /// <param name="distance">Edit distance from search word.</param>
    /// <param name="count">Frequency of suggestion in dictionary.</param>
    public SuggestItem(String term, int distance, long count) {
        this.term = term;
        this.distance = distance;
        this.count = count;
    }

    @Override
    public int compare(SuggestItem suggestItem, SuggestItem t1) {
        return suggestItem.compareTo(t1);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SuggestItem && term.equals(((SuggestItem) obj).term);
    }

    @Override
    public int hashCode()
    {
        return term.hashCode();
    }

    @Override
    public String toString()
    {
        return "{" + term + ", " + distance + ", " + count + "}";
    }

    @Override
    public int compareTo(SuggestItem other) {
        // order by distance ascending, then by frequency count descending
        if (this.distance == other.distance) return Long.compare(other.count, this.count);
        return Integer.compare(this.distance, other.distance);
    }

    public SuggestItem clone(){
        return new SuggestItem(this.term, this.distance, this.count);
    }
}
