#!/usr/bin/env python3
# coding: utf-8

# Program transforms Serbian word corpus into number of files.
# Each file has structure similar to main word corpus file
# with some differences:
#
# 1. Words in Latin alphabet are transliterated into Cyrillic
# 2. Each word is stored in file <XX>-words.txt, where <XX> is
# Latin equivalent of Cyrillic letters

import argparse
import logging
import re
import os
import sys

"""
Program reads morphologic dictionary from a single file, line by line, splitting entries into
multiple files. File where each line will go is determined by first letter of lemma. Example:

If lemma == "apple", then whole line goes to file "a-words.txt" etc.

If entries are in Croatian Latin script, they will be converted into Serbian Cyrillic script.
"""

_args_, _logger_, _l2comp_, _l2conv_, _ciregex_, _freqs_, _cirdict_ = None, None, None, None, None, list(), None
_freqmap_ = dict()

LOG_FORMAT = '%(asctime)-15s %(levelname)s %(message)s'
CYR_LETTERS = {
    'а' : 'a',
    'б' : 'be',
    'в' : 've',
    'г' : 'ge',
    'д' : 'de',
    'ђ' : 'dje',
    'е' : 'e',
    'ж' : 'zhe',
    'з' : 'ze',
    'и' : 'i',
    'ј' : 'je',
    'к' : 'ka',
    'л' : 'ell',
    'љ' : 'lje',
    'м' : 'em',
    'н' : 'en',
    'њ' : 'nje',
    'о' : 'o',
    'п' : 'pe',
    'р' : 'er',
    'с' : 'es',
    'т' : 'te',
    'ћ' : 'tshe',
    'у' : 'u',
    'ф' : 'ef',
    'х' : 'ha',
    'ц' : 'ce',
    'ч' : 'ch',
    'џ' : 'dzhe',
    'ш' : 'sha',
    'misc' : 'misc', # For miscellaneous "words"
    'bad' : 'bad' # For words containing foreign letters
}

# Types of regex to match lines in input file
# Regex is selectable from command line using parameter "-r"
REGEX_TYPE = {
    "lex" : "^([!\"\'\(\),\-\.:;\?]|[a-zčćžšđâîôûﬂǌüöäø’A-ZČĆŽŠĐ0-9_\-]+)\s+([!\"\'\(\),\-\.:;\?]|[a-zčćžšđâîôûﬂǌüöäø’A-ZČĆŽŠĐ0-9_\-]+)\s+([a-zA-Z0-9\-]+)\s+(\d+)*"
}

BAD_GROUPS = ('ü', 'ö', 'ä', 'ø', 'аа', 'ии', 'уу', 'цх', 'тз', 'цз', 'q', 'w', 'x', 'y', 'Q', 'W', 'X', 'Y', 'Ä', 'Ü', 'Ö', 'è', 'à', 'фф', 'бб', 'зз', 'лл', 'мм', 'нн', 'пп', 'рр', 'сс', 'тт', 'гх', 'тх', 'хх')

# Map holding transliterated Cyrillic letters pointing to
# descriptors of opened files
WORD_FILES = {}

# List (or better: tupple) of Latin letters and ligatures
LAT_LIST = (u"Đ", u"Dž", u"DŽ", u"LJ", u"Lj", u"NJ", u"Nj", u"A", u"B", u"V", u"G", u"D", u"E", u"Ž", u"Z", u"I", u"J", u"K", u"L", u"M", u"N", u"O", u"P", u"R", u"S", u"T", u"Ć", u"U", u"F", u"H", u"C", u"Č", u"Š", u"a", u"b", u"v", u"g", u"dž", u"d", u"e", u"ž", u"z", u"i", u"j", u"k", u"lj", u"l", u"m", u"nj", u"n", u"o", u"p", u"r", u"s", u"t", u"ć", u"u", u"f", u"h", u"c", u"č", u"š", u"đ", u"Ð", u"ǌ", u"ﬂ", u"î", u"û")

# List (or better: tupple) of Cyrillic letters and ligatures
CIR_UTF_LIST = (u"Ђ", u"Џ", u"Џ", u"Љ", u"Љ", u"Њ", u"Њ", u"А", u"Б", u"В", u"Г", u"Д", u"Е", u"Ж", u"З", u"И", u"Ј", u"К", u"Л", u"М", u"Н", u"О", u"П", u"Р", u"С", u"Т", u"Ћ", u"У", u"Ф", u"Х", u"Ц", u"Ч", u"Ш", u"а", u"б", u"в", u"г", u"џ", u"д", u"е", u"ж", u"з", u"и", u"ј", u"к", u"љ", u"л", u"м", u"њ", u"н", u"о", u"п", u"р", u"с", u"т", u"ћ", u"у", u"ф", u"х", u"ц", u"ч", u"ш", u"ђ", u"Ђ", u"њ", u"фл", u"ӣ", u"ӯ")

def parse_args():
    parser = argparse.ArgumentParser(description='Processes file containing Serbian word corpus.')
    parser.add_argument('-b', '--base-dir',   default='/tmp')
    parser.add_argument('-d', '--debug',      action ='store_true', default=False)
    parser.add_argument('-i', '--input-file', default=None)
    parser.add_argument('-m', '--map-file', default=None)
    parser.add_argument('-n', '--first-n-lines', default=0, type=int)
    parser.add_argument('-r', '--regex',      default=None)
    global _args_, _logger_
    _args_ = parser.parse_args()
    logging.basicConfig(format=LOG_FORMAT)
    _logger_ = logging.getLogger("lex2lt")
    if _args_.debug:
        _logger_.setLevel( logging.DEBUG )
    else:
        _logger_.setLevel( logging.INFO )
    _logger_.debug( "Command-line arguments: {}".format(_args_) )
    if not _args_.input_file:
        _logger_.error("Input file (-i) was not specified, aborting ...")
        sys.exit(1)
    if not _args_.regex:
        _logger_.error("Regex expression (-r) was not specified, aborting ...")
        sys.exit(1)
    if not _args_.map_file:
        _logger_.error("Map file (-m) was not specified, aborting ...")
        sys.exit(1)
    if not os.path.exists(_args_.input_file):
        _logger_.error("Input file '{}' does not exist, aborting ...".format(_args_.input_file))
        sys.exit(1)
    if not os.path.exists(_args_.map_file):
        _logger_.error("Map file '{}' does not exist, aborting ...".format(_args_.map_file))
        sys.exit(1)


# Open files for writing words in directory specified with "-b" option
# Files are opened in append mode
def open_out_files():
    global WORD_FILES
    for cl, lett in CYR_LETTERS.items():
        out_dir = os.path.join(_args_.base_dir, lett)
        if not os.path.exists( out_dir ):
            os.makedirs( out_dir )
        WORD_FILES[ cl ] = list()
        _logger_.debug( "Opening file {}/{}-lex-words.txt ...".format(out_dir, lett) )
        WORD_FILES[ cl ].append( open( os.path.join(out_dir, lett + '-lex-words.txt'), 'wb' ) )
        _logger_.debug( "Opening file {}/{}-lex-names.txt ...".format(out_dir, lett) )
        WORD_FILES[ cl ].append( open( os.path.join(out_dir, lett + '-lex-names.txt'), 'wb' ) )


# Close all files containing words
def close_out_files():
    for cl, lett_files in WORD_FILES.items():
        _logger_.debug('Closing file {}-lex-words.txt ...'.format(CYR_LETTERS[ cl ]))
        lett_files[0].close()
        _logger_.debug('Closing file {}-lex-names.txt ...'.format(CYR_LETTERS[ cl ]))
        lett_files[1].close()


# Initialization
def init():
    global _l2conv_, _l2comp_, _ciregex_, _cirdict_
    # Create conversion dictionary for Latin to Cyrillic conversion
    _l2conv_ = dict(zip(LAT_LIST, CIR_UTF_LIST))
    _logger_.debug("Conversion dictionary Lat/Cir: {}".format(_l2conv_))
    _l2comp_ = re.compile('|'.join(_l2conv_))
    # Read map file and populate map dictionary
    with open(_args_.map_file) as infile:
        _cirdict_ = dict(x.strip().split(None, 1) for x in infile if x.strip())
    # Compile dictionary
    keys = sorted(_cirdict_.keys(), key=len, reverse=True)
    expression = []
    for item in keys:
        expression.append(re.escape(item))
    _logger_.debug("Replace map: {}".format(_cirdict_))
    # Create a regular expression  from the dictionary keys
    _ciregex_ = re.compile("(%s)" % "|".join(expression))


# Determine output file for word tripple
# based on first letter of lemma
def get_words_out_file( first_char ):
    if first_char.lower() in WORD_FILES:
        # Is this really a lower case?
        if first_char == first_char.lower():
            out_file = WORD_FILES[ first_char.lower() ][0]
        else:
            out_file = WORD_FILES[ first_char.lower() ][1]
    else:
        out_file = WORD_FILES[ 'misc' ][0]
    return out_file


# Go through input file, read word frequencies and prepare map file
# Map file will be used in dictionary creation process
def find_frequencies():
    global _freqs_
    cnt = 0
    matchcnt = 0
    _logger_.info("PASS 1: Started processing input file '{}', finding word frequencies ...".format(_args_.input_file))
    freq = list()

    with open(_args_.input_file) as f:
        for line in f:
            # Remove end of line
            line = line.strip()
            cnt += 1
            tokens = line.split('\t')
            if len(tokens) == 5:
                matchcnt  += 1
                posgr     = tokens[2]
                frequency = tokens[3]
                _logger_.debug('frequency={}'.format(frequency))
                # Do not take punctuation signs
                if int(frequency) not in freq and posgr != 'Z':
                    freq.append( int(frequency) )
            else:
                _logger_.warn("Unmatched line: {}".format(line))
            if cnt > _args_.first_n_lines > 0:
                break
        f.close()
    _logger_.info( "PASS 1: End processing input file '{}'.".format(_args_.input_file))
    _logger_.info( "PASS 1: Found {} different word frequencies.".format(len(freq)) )
    _freqs_ = sorted(freq)


# Maps list of word frequencies to numbers from 0 to 255
def distribute_word_frequencies():
    global _freqmap_
    _logger_.info( "Frequencies: first {}, last {}.".format(_freqs_[0], _freqs_[-1]) )
    # Special case
    _freqmap_[ 0 ] = 0
    len_freq = len(_freqs_)
    # Subtract frequency 0 and divide rest of the list in 255 buckets
    bucket_size = (len_freq - 1) // 255 + 1
    _logger_.debug( "Frequency list bucket size: {}".format(bucket_size) )

    for msb in list(range(0, 255)):
        #print( 'msb={}'.format(msb))
        for lsb in list(range(0, bucket_size)):
            ind = msb * bucket_size + lsb + 1
            if ind < len_freq:
                #print( 'lsb={} ind={} '.format(lsb, ind))
                _freqmap_[ _freqs_[ ind ] ] = msb + 1
        #print( " " )


def has_bad_letters(word):
    return any(x in word for x in BAD_GROUPS)


# Parse input file
def parse_file():
    cnt = 0
    matchcnt = 0
    _logger_.info("PASS 2: Started processing input file '{}' ...".format(_args_.input_file))
    freqfile = open("serbian-wordlist.xml", "wb")

    with open(_args_.input_file) as f:
        for line in f:
            # Remove end of line
            line = line.strip()
            cnt += 1
            tokens = line.split('\t')
            if len(tokens) == 5:
                matchcnt += 1
                flexform = tokens[0]
                lemma = tokens[1]
                posgr = tokens[2]
                frequency = tokens[3]
                # We need to do transliterating here in order to avoid transliterating POS tag :(
                flexform_lemma = "{}\t{}".format(flexform, lemma)
                if lemma.upper() not in ('I', 'II', 'III', 'IV', 'V', 'VI', 'VII', 'VIII', 'IX', 'X'):
                    # Transliterate all words in line, replacing Latin with Cyrillic characters
                    flexform_lemma = _l2comp_.sub(lambda m: _l2conv_[m.group()], flexform_lemma)
                    # Replace words according to replace map
                    flexform_lemma = _ciregex_.sub(lambda mo: _cirdict_[mo.string[mo.start():mo.end()]], flexform_lemma)
                    if has_bad_letters(flexform_lemma):
                        out_file = WORD_FILES[ 'bad' ][0]
                        out_file.write("{}\t{}\t{}\n".format(flexform_lemma, posgr, frequency).encode('utf-8'))
                        continue
                # Split pair again after transliteration
                tokens = flexform_lemma.split()
                flexform, lemma = tokens
                _logger_.debug('Converted flexform={}, lemma={}, posgr={}'.format(flexform, lemma, posgr))

                # Determine file to write line in ...
                out_file = get_words_out_file(lemma[0])
                # Create line for writing in file
                out_file.write("{}\t{}\t{}\t{}\n".format(
                    flexform, lemma, posgr, frequency).encode('utf-8'))
                # Write to frequency file
                if posgr != 'Z':
                    freqfile.write('<w f="{}" flags="">{}</w>\n'.format(_freqmap_[ int(frequency) ], flexform).encode('utf-8'))
            else:
                _logger_.warn("Unmatched line: {}".format(line))
            if cnt > _args_.first_n_lines > 0:
                break
        f.close()
    freqfile.close()
    _logger_.info("PASS 2: Finished processing input file '{}': total {} lines, {} matching lines.".format(
        _args_.input_file, cnt, matchcnt))


if __name__ == "__main__":
    parse_args()
    init()
    open_out_files()
    find_frequencies()
    distribute_word_frequencies()
    parse_file()
    close_out_files()
