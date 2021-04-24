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
import org.languagetool.rules.spelling.symspell.implementation.SuggestionStage.Node;

import java.util.Arrays;
// A growable list of elements that's optimized to support adds, but not deletes,
// of large numbers of elements, storing data in a way that's friendly to the garbage
// collector (not backed by a monolithic array object), and can grow without needing
// to copy the entire backing array contents from the old backing array to the new.
public class ChunkArray<T>
{
    private static int chunkSize = 4096;//this must be a power of 2, otherwise can't optimize row and col functions
    private static int divShift = 12;   // number of bits to shift right to do division by chunkSize (the bit position of chunkSize)
    public SuggestionStage.Node[][] values;             // Note: Node (SymSpell.SuggestionStage.Node) is found in SymSpell.SymSpell.java.
    public int count;

    ChunkArray(int initialCapacity)
    {
        int chunks = (initialCapacity + chunkSize - 1) / chunkSize;
        values = new SuggestionStage.Node[chunks][];
        for (int i = 0; i < values.length; i++) values[i] = new Node[chunkSize];
    }

    public int add(Node value)
    {
        if (count == capacity()) {
            Node[][] newValues = Arrays.copyOf(values, values.length + 1);
            newValues[values.length] = new Node[chunkSize];
            values = newValues;
        }

        values[row(count)][col(count)] = value;
        count++;
        return count - 1;
    }

    public void clear()
    {
        count = 0;
    }

    public Node getValues(int index) {
        return values[row(index)][col(index)];
    }
    public void setValues(int index, Node value){
        values[row(index)][col(index)] = value;
    }
    public void setValues(int index, Node value, Node[][] list){
        list[row(index)][col(index)] = value;
    }

    private int row(int index) { return index >> divShift; } // same as index / chunkSize
    private int col(int index) { return index & (chunkSize - 1); } //same as index % chunkSize
    private int capacity() { return values.length * chunkSize; }
}
