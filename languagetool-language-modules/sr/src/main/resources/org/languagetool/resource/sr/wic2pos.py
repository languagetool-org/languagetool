#!/usr/bin/env python3
# coding: utf-8

# Program transforms Serbian word corpus into number of files.
# Each file has structure similar to main word corpus file
# with some differences:
#
# 1. Words in Latin alphabet are transliterated into Cyrillic
# 2. Each word is stored in file <XX>-words.txt, where <XX> is
# Croatian Latin equivalent of Cyrillic letters

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
    '0' : '0-9',
    '1' : '0-9',
    '2' : '0-9',
    '3' : '0-9',
    '4' : '0-9',
    '5' : '0-9',
    '6' : '0-9',
    '7' : '0-9',
    '8' : '0-9',
    '9' : '0-9',
    'misc' : 'misc', # For miscellaneous "words"
    'bad' : 'bad' # For words containing foreign letters
}

# Types of regex to match lines in input file
# Regex is selectable from command line using parameter "-r"
REGEX_TYPE = {
    "wic" : "^([!\"\'\(\),\-\.:;\?]|[a-zčćžšđâêîôûﬂǌüöäø’A-ZČĆŽŠĐ0-9_\-]+)\s+([!\"\'\(\),\-\.:;\?]|[a-zčćžšđâêîôûﬂǌüöäø’A-ZČĆŽŠĐ0-9_\-]+)\s+(a-zA-Z0-2_)+*"
}

BAD_GROUPS = ('ü', 'ö', 'ä', 'ø', 'аа', 'ии', 'уу', 'цх', 'тз', 'цз', 'q', 'w', 'x', 'y', 'Q', 'W', 'X', 'Y', 'Ä', 'Ü', 'Ö', 'è', 'à', 'фф', 'бб', 'зз', 'лл', 'мм', 'нн', 'пп', 'рр', 'сс', 'тт', 'гх', 'тх', 'хх')

# Map holding transliterated Serbian Cyrillic letters pointing to
# descriptors of opened files
WORD_FILES = {}

# List (or better: tupple) of Croatian Latin letters and ligatures
LAT_LIST = (u"Đ", u"Dž", u"DŽ", u"LJ", u"Lj", u"NJ", u"Nj", u"A", u"B", u"V", u"G", u"D", u"E", u"Ž", u"Z", u"I", u"J", u"K", u"L", u"M", u"N", u"O", u"P", u"R", u"S", u"T", u"Ć", u"U", u"F", u"H", u"C", u"Č", u"Š", u"a", u"b", u"v", u"g", u"dž", u"d", u"e", u"ž", u"z", u"i", u"j", u"k", u"lj", u"l", u"m", u"nj", u"n", u"o", u"p", u"r", u"s", u"t", u"ć", u"u", u"f", u"h", u"c", u"č", u"š", u"đ", u"Ð", u"ǌ", u"ﬂ", u"î", u"û")

# List (or better: tupple) of Serbian Cyrillic letters and ligatures
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
    _logger_ = logging.getLogger("wic2lt")
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
        _logger_.debug( "Opening file {}/{}-wic-words.txt ...".format(out_dir, lett) )
        WORD_FILES[ cl ].append( open( os.path.join(out_dir, lett + '-wic-words.txt'), 'wb' ) )
        _logger_.debug( "Opening file {}/{}-wic-names.txt ...".format(out_dir, lett) )
        WORD_FILES[ cl ].append( open( os.path.join(out_dir, lett + '-wic-names.txt'), 'wb' ) )


# Close all files containing words
def close_out_files():
    for cl, lett_files in WORD_FILES.items():
        _logger_.debug('Closing file {}-wic-words.txt ...'.format(CYR_LETTERS[ cl ]))
        lett_files[0].close()
        _logger_.debug('Closing file {}-wic-names.txt ...'.format(CYR_LETTERS[ cl ]))
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

numeral_map = tuple(zip(
    (1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1),
    ('M', 'CM', 'D', 'CD', 'C', 'XC', 'L', 'XL', 'X', 'IX', 'V', 'IV', 'I')
))

def int_to_roman(i):
    result = []
    for integer, numeral in numeral_map:
        count = i // integer
        result.append(numeral * count)
        i -= integer * count
    return ''.join(result)

noun_types = {
    'com'  : 'c',
    'prop' : 'p',
    'col'  : 'o',
    '0'    : '-'
}

number_types = {
    'sg' : 's',
    'pl' : 'p',
    '-'  : '-',
    '0'  : '-'
}

case_types = {
    'nom' : 'n',
    'gen' : 'g',
    'dat' : 'd',
    'acc' : 'a',
    'voc' : 'v',
    'ins' : 'i',
    'loc' : 'l',
    '-'   : '-',
    '0'   : '-'
}

gender_types = {
    'm' : 'm',
    'f' : 'f',
    'n' : 'n',
    '0' : '-',
    '-' : '-'
}

adjective_types = {
    'qual'  : 's',
    'pos'   : 'p',
    'dem'   : '',
    'indef' : '',
    'inter' : '',
    'rel'   : '',
    '0'     : '-'
}

degree_types = {
    'pos'  : 'p',
    'comp' : 'c',
    'sup'  : 's',
    '-'    : '-',
    '0'    : '-'
}

pronoun_types = {
    'pers'  : 'p',
    'pos'   : 's',
    'dem'   : 'd',
    'indef' : 'i',
    'inter' : 'q',
    'rel'   : 'r',
    '0'     : '-'
}

verb_types = {
    'main' : 'm',
    'aux'  : 'a',
    '0'    : '-'
}

verb_form = {
    'pres'     : 'r',
    'aor'      : 'a',
    'fut'      : 'f',
    'imper'    : 'm',
    'impf'     : 'e',
    'inf'      : 'n',
    'partact'  : 'p', # Participle active  - глаголски придев радни
    'partpass' : 'q', # Participle passive - глаголски придев трпни (WARNING: This is not in specification)
    'partpres' : 's', # Participle present - глаголски прилог садашњи (WARNING: This is not in specification)
    'partpast' : 't'  # Participle past    - глаголски прилог прошли (WARNING: This is not in specification)
}

numeral_types = {
    'card' : 'c',
    'ord'  : 'o',
    'col'  : 'l', # Намерно стављено да се види колико је оваквих бројева у корпусу
    '0'    : '-'
}

adverb_types = {
    'gen'   : 'g',
    'indef' : 'x',
    'rel'   : 'x',
    'inter' : 'x',
    '0'     : '-'
}

conjunction_types = {
    'sub'  : 's',
    'coor' : 'c',
    '0'    : '-'
}

# Adjective (А) - придев
def getAdjectiveTag(flexform, parts):
    try:
        atype  = adjective_types[ parts[0] ]
        case   = case_types[ parts[1] ]
        number = number_types[ parts[2] ]
        gender = gender_types[ parts[3] ]
        degree = degree_types[ parts[4] ]
    except KeyError:
        _logger_.error("getAdjectiveTag: Error parsing tag '{}', flexform '{}'".format(parts, flexform))
        sys.exit(1)
    return "A" + atype + degree + gender + number + case

# Adverb (Adv) - прилог
def getAdverbTag(flexform, parts):
    try:
        atype = adverb_types[ parts[0] ]
        degree = degree_types[ parts[1] ]
    except KeyError:
        _logger_.error("getAdverbTag: Error parsing tag '{}', flexform '{}'".format(parts, flexform))
        sys.exit(1)
    return "R" + atype + degree

# Conjunction - везник
def getConjunctionTag(flexform, parts):
    try:
        ctype = conjunction_types[ parts[0] ]
    except KeyError:
        _logger_.error("getConjunctionTag: Error parsing tag '{}', flexform '{}'".format(parts, flexform))
        sys.exit(1)
    return "C" + ctype

# Interjection - узвик
def getInterjectionTag(flexform, parts):
    return "I"

# Noun - именица
def getNounTag(flexform, parts):
    try:
        ntype  = noun_types[ parts[0] ]
        case   = case_types[ parts[1] ]
        number = number_types[ parts[2] ]
        gender = gender_types[ parts[3] ]
    except KeyError:
        _logger_.error("getNounTag: Error parsing tag '{}', flexform '{}'".format(parts, flexform))
        sys.exit(1)
    return "N" + ntype + gender + number + case

# Numeral - број
def getNumeralTag(flexform, parts):
    try:
        ntype  = numeral_types[ parts[0] ]
        gender = gender_types[ parts[1] ]
        number = number_types[ parts[2] ]
        case   = case_types[ parts[3] ]
    except KeyError:
        _logger_.error("getNumeralTag: Error parsing tag '{}', flexform '{}'".format(parts, flexform))
        sys.exit(1)
    return "Ml" + ntype + gender + number + case

# Pronoun - заменица
def getPronounTag(flexform, parts):
    try:
        ptype  = pronoun_types[ parts[0] ]
        person = parts[1]
        number = number_types[ parts[2] ]
        gender = gender_types[ parts[3] ]
        case   = case_types[ parts[4] ]
    except KeyError:
        _logger_.error("getPronounTag: Error parsing tag '{}', flexform '{}'".format(parts, flexform))
        sys.exit(1)
    return "P" + ptype + person + gender + number + case

# Preposition - предлог
def getPrepositionTag(flexform, parts):
    return "S"

# Verb - глагол
def getVerbTag(flexform, parts):
    try:
        vtype    = verb_types[ parts[0] ]
        vform    = verb_form[ parts[1] ]
        person   = parts[2]
        number   = number_types[ parts[3] ]
        gender   = gender_types[ parts[4] ]
        negation = parts[5]
    except KeyError:
        _logger_.error("getVerbTag: Error parsing tag '{}', flexform '{}'".format(parts, flexform))
        sys.exit(1)
    return "V" + vtype + vform + person + number + gender + negation


# Maps tags to "normal" POS tags
def getPOStag(lemma, wictag):
    # Pointers to functions ... last seen in C long time ago ...
    switch_word_type = {
        'A'    : getAdjectiveTag,    # Adjective - придев
        'Adv'  : getAdverbTag,       # Adverb - прилог
        'C'    : getConjunctionTag,  # Conjunction - везник
        'I'    : getInterjectionTag, # Interjection - узвик
        'N'    : getNounTag,         # Noun - именица
        'Num'  : getNumeralTag,      # Numeral - број
        'P'    : getPronounTag,      # Pronoun - заменица
        'Prep' : getPrepositionTag,  # Preposition - предлог
        'V'    : getVerbTag          # Verb - глагол
    }
    # Split tag to parts
    tagparts = wictag.split('_')
    wtype = tagparts[0]
    if wtype in switch_word_type:
        retval = switch_word_type[ wtype ](lemma, tagparts[1:]) # There is no "apply" global function in Python 3
        # Remove last 4, 3, 2 or 1 continuous series of dashes from the end of tag
        for i in range(4, 0, -1):
            ind = retval.rfind('-' * i)
            if ind > 0:
                retval = retval[:ind]
                break
        return retval
    else:
        return "0"


def has_bad_letters(word):
    return any(x in word for x in BAD_GROUPS)

# Parse input file
def parse_file():
    cnt = 0
    matchcnt = 0
    _logger_.info("Started processing input file '{}' ...".format(_args_.input_file))

    with open(_args_.input_file) as f:
        for line in f:
            # Remove end of line
            line = line.strip()
            cnt += 1
            tokens = line.split('\t')
            if len(tokens) == 3:
                matchcnt += 1
                flexform, lemma, wictag = tokens
                # Some tags seems fishy, so better skip them
                if wictag.startswith('A_pos') \
                or wictag.startswith('A_dem') \
                or wictag.startswith('A_indef') \
                or wictag.startswith('A_inter') \
                or wictag.startswith('A_rel'):
                    continue
                # We need to do transliterating here in order to avoid transliterating POS tag :(
                flexform_lemma = "{}\t{}".format(flexform, lemma)
                # Transliterate all words in line, replacing Latin with Cyrillic characters
                flexform_lemma = _l2comp_.sub(lambda m: _l2conv_[m.group()], flexform_lemma)
                # Replace words according to word replace map (Eiffel => Ајфел)
                flexform_lemma = _ciregex_.sub(lambda mo: _cirdict_[mo.string[mo.start():mo.end()]], flexform_lemma)
                # Check lemma for non-transliterated letters or some foreign letter combination
                # If they are found, write lemma in separate file
                # That will help in creating replacements
                if has_bad_letters(flexform_lemma):
                    out_file = WORD_FILES[ 'bad' ][0]
                    posgr = getPOStag(flexform, wictag)
                    out_file.write("{}\t{}\t0\n".format(flexform_lemma, posgr).encode('utf-8'))
                    continue
                # Split pair again after transliteration
                tokens = flexform_lemma.split()
                try:
                    flexform, lemma = tokens
                except ValueError:
                    _logger_.error("Too many values to unpack: tokens '{}', line '{}'".format(tokens, line))
                    continue
                posgr = getPOStag(flexform, wictag)
                _logger_.debug('Converted flexform={}, lemma={}, posgr={}'.format(flexform, lemma, posgr))
                if posgr[0] in ( 'M', 'S', '0' ) or (len(posgr) in (1,2) and posgr[0] in ('N', 'A', 'V')):
                    # We will skip:
                    # 1. prepositions because of lack of information about the case they go with
                    # 2. numerals because of lack of type
                    # 3. words where we could not generate postag
                    continue
                # Determine file to write line in ...
                out_file = get_words_out_file(lemma[0])
                # Create line for writing in file
                out_file.write("{}\t{}\t{}\t0\n".format(flexform, lemma, posgr).encode('utf-8'))
            else:
                _logger_.warn("Unmatched line: {}".format(line))
            if cnt > _args_.first_n_lines > 0:
                break
        f.close()
    # Generate Roman numerals from 11 to 1000 and add them as word types
    for i in range(11,1000):
        roman = int_to_roman(i)
        out_file = get_words_out_file(str(i % 10))
        out_file.write("{}\t{}\tMrc\t0\n".format(roman, roman).encode('utf-8'))
        roman = roman.lower()
        out_file.write("{}\t{}\tMrc\t0\n".format(roman, roman).encode('utf-8'))
    _logger_.info("Finished processing input file '{}': total {} lines, {} matching lines.".format(
        _args_.input_file, cnt, matchcnt))


if __name__ == "__main__":
    parse_args()
    init()
    open_out_files()
    parse_file()
    close_out_files()
