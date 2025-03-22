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

public class EditDistance {
    public enum DistanceAlgorithm{
        Damerau
    }
    private String baseString;
    private DistanceAlgorithm algorithm;
    private int[] v0;
    private int[] v2;
    /// <summary>Create a new EditDistance object.</summary>
    /// <param name="baseString">The base string to which other strings will be compared.</param>
    /// <param name="algorithm">The desired edit distance algorithm.</param>
    public EditDistance(String baseString, DistanceAlgorithm algorithm)
    {
        this.baseString = baseString;
        this.algorithm = algorithm;
        if (this.baseString.isEmpty()) {
            this.baseString = null;
            return;
        }
        if (algorithm == DistanceAlgorithm.Damerau) {
            v0 = new int[baseString.length()];
            v2 = new int[baseString.length()]; // stores one level further back (offset by +1 position)
        }
    }
    // <summary>compare a string to the base string to determine the edit distance,
    /// using the previously selected algorithm.</summary>
    /// <param name="string2">The string to compare.</param>
    /// <param name="maxDistance">The maximum distance allowed.</param>
    /// <returns>The edit distance (or -1 if maxDistance exceeded).</returns>
    public int compare(String string2, int maxDistance) {
        switch (algorithm) {
            case Damerau: return DamerauLevenshteinDistance(string2, maxDistance);
        }
        throw new IllegalArgumentException("unknown DistanceAlgorithm");
    }
    // stores one level further back (offset by +1 position)
    /// <param name="string1">String being compared for distance.</param>
    /// <param name="string2">String being compared against other string.</param>
    /// <param name="maxDistance">The maximum edit distance of interest.</param>
    /// <returns>int edit distance, >= 0 representing the number of edits required
    /// to transform one string to the other, or -1 if the distance is greater than the specified maxDistance.</returns>
    public int DamerauLevenshteinDistance(String string2, int maxDistance) {
        if (baseString == null) return string2 == null ? 0 : string2.length(); //string2 ?? "").Length;
        if (string2 == null || string2.isEmpty()) return baseString.length();
        if(maxDistance == 0) return baseString.equals(string2) ? 0 : -1;

        // if strings of different lengths, ensure shorter string is in string1. This can result in a little
        // faster speed by spending more time spinning just the inner loop during the main processing.
        String string1;
        if (baseString.length() > string2.length()) {
            string1 = string2;
            string2 = baseString;
        } else {
            string1 = baseString;
        }
        int sLen = string1.length(); // this is also the minimum length of the two strings
        int tLen = string2.length();

        // suffix common to both strings can be ignored
        while ((sLen > 0) && (string1.charAt(sLen - 1) == string2.charAt(tLen - 1))) { sLen--; tLen--; }

        int start = 0;
        if ((string1.charAt(0) == string2.charAt(0)) || (sLen == 0)) { // if there'string1 a shared prefix, or all string1 matches string2'string1 suffix
            // prefix common to both strings can be ignored
            while ((start < sLen) && (string1.charAt(start) == string2.charAt(start))) start++;
            sLen -= start; // length of the part excluding common prefix and suffix
            tLen -= start;

            // if all of shorter string matches prefix and/or suffix of longer string, then
            // edit distance is just the delete of additional characters present in longer string
            if (sLen == 0) return tLen;

            string2 = string2.substring(start, start + tLen); // faster than string2[start+j] in inner loop below
        }
        int lenDiff = tLen - sLen;
        if ((maxDistance < 0) || (maxDistance > tLen)) {
            maxDistance = tLen;
        } else if (lenDiff > maxDistance) return -1;

        if (tLen > v0.length)
        {
            v0 = new int[tLen];
            v2 = new int[tLen];
        } else {
            for(int i = 0; i < tLen; i++) v2[i] = 0;    // Substituting Array.clear(v2, 0, tLen)
        }
        int j;
        for (j = 0; j < maxDistance; j++) v0[j] = j + 1;
        for (; j < tLen; j++) v0[j] = maxDistance + 1;

        int jStartOffset = maxDistance - (tLen - sLen);
        boolean haveMax = maxDistance < tLen;
        int jStart = 0;
        int jEnd = maxDistance;
        char sChar = string1.charAt(0);
        int current = 0;
        for (int i = 0; i < sLen; i++) {
            char prevsChar = sChar;
            sChar = string1.charAt(start+i);
            char tChar = string2.charAt(0);
            int left = i;
            current = left + 1;
            int nextTransCost = 0;
            // no need to look beyond window of lower right diagonal - maxDistance cells (lower right diag is i - lenDiff)
            // and the upper left diagonal + maxDistance cells (upper left is i)
            jStart += (i > jStartOffset) ? 1 : 0;
            jEnd += (jEnd < tLen) ? 1 : 0;
            for (j = jStart; j < jEnd; j++) {
                int above = current;
                int thisTransCost = nextTransCost;
                nextTransCost = v2[j];
                v2[j] = current = left; // cost of diagonal (substitution)
                left = v0[j];    // left now equals current cost (which will be diagonal at next iteration)
                char prevtChar = tChar;
                tChar = string2.charAt(j);
                if (sChar != tChar) {
                    if (left < current) current = left;   // insertion
                    if (above < current) current = above; // deletion
                    current++;
                    if ((i != 0) && (j != 0)
                            && (sChar == prevtChar)
                            && (prevsChar == tChar)) {
                        thisTransCost++;
                        if (thisTransCost < current) current = thisTransCost; // transposition
                    }
                }
                v0[j] = current;
            }
            if (haveMax && (v0[i + lenDiff] > maxDistance)) return -1;
        }
        return (current <= maxDistance) ? current : -1;
    }
}
