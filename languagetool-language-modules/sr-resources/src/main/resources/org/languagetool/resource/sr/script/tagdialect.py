#!/usr/bin/env python3
# coding: utf-8

import os
import sys

"""
Program creates list of SQL UPDATE statements to label words in database
according to different dialects of Serbian language. Input file has format
of

label   wordform    lemma

Items are separated with one or more <TAB> characters.
"""

# Returns SQL UPDATE statement
def getUpdateStatement(dialect, wordform, lemma):
    return "UPDATE words SET dialect='{}' WHERE wordform='{}' AND lemma='{}';\n".format(
        dialect, wordform, lemma).encode('utf-8')


if __name__ == "__main__":
    inputFile = sys.argv[1]
    if not os.path.exists(inputFile):
        print("(EE) Input file '{}' does not exist, aborting".format(inputFile))
        sys.exit(1)
    outF = open("out.txt", "wb")
    # Dialect indicator - if 'e' or 'i' create UPDATE statements
    dialect = None
    ekavianLabels = ('Е', 'е', 'E', 'e',)
    jekavianLabels = ('И', 'и', 'I', 'i',)
    validDialects = ekavianLabels + jekavianLabels

    with open(inputFile) as f:
        for line in f:
            line = line.strip()
            if line in (None, ''):
                dialect = None
                continue
            tokens = line.split()
            if len(tokens) == 3:
                # Line with dialect indicator
                if tokens[0] in validDialects:
                    if tokens[0] in ekavianLabels:
                        dialect = 'e'
                    elif tokens[0] in jekavianLabels:
                        dialect = 'i'
                    else:
                        print("(WW) Unknown dialect '{}' where wordform='{}' and lemma='{}'".format(tokens[0], tokens[1], tokens[2]))
                        continue
                    outF.write( getUpdateStatement(dialect, tokens[1], tokens[2]))
                else:
                    dialect = None
            elif len(tokens) == 2:
                # Word form and lemma, check dialect
                if dialect in validDialects:
                    outF.write( getUpdateStatement(dialect, tokens[0], tokens[1]))
            else:
                print("(WW) Unknown line form, skipping: '{}'".format(line))
                continue
        f.close()
    outF.close()