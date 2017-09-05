#!/usr/bin/env python3
# coding: utf-8

"""
This program makes POS tags for Serbian dictionary used in LanguageTool (LT).
"""

DNOUN = dict(
    type    = dict(c='zaj', p='vla'),
    gender  = dict(m='mus', f='zen', n='sre'),
    number  = dict(s='jed', p='mno', t='zbi'),
    case    = dict(n='nom', g='gen', d='dat', a='aku', v='vok', i='ins', l='lok'),
    animate = dict(n='stv', y='ziv')
)

# Tags starting with "N" - noun (именица)
def _get_noun_tag(msd, sep):
    if len(msd) < 5:
        return "ERROR: Incorrect noun tag '{}'".format(msd)
    ret = "ime"
    ret += sep + DNOUN[ 'type'   ][ msd[1] ] # Type
    ret += sep + DNOUN[ 'gender' ][ msd[2] ] # Gender
    ret += sep + DNOUN[ 'number' ][ msd[3] ] # Number
    ret += sep + DNOUN[ 'case'   ][ msd[4] ] # Case
    if len(msd) == 6: # Animate
        ret += sep + DNOUN[ 'animate' ][ msd[5] ]
    return ret

DVERB = dict(
    type     = dict(m='', a='', o='', c=''),
    vform    = dict(i='', m='', c='', n='', p='', g=''),
    tense    = dict(p='', i='', f='', s='', l='', a=''),
    person   = {'1' : '1li', '2' : '2li', '3' : '3li', '-' : ''},
    number   = {'s' : 'jed', 'p' : 'mno', '-' : ''},
    gender   = dict(m='mus', f='zen', n='sre'),
    voice    = dict(a='akt', p='pas'),
    negative = dict(n='', y=''),
    clitic   = dict(n='', y=''),
    aspect   = dict(p='', e='')
)
# Tags starting with "V" - verb (глагол)
def _get_verb_tag(msd, sep):
    if len(msd) < 2:
        return "ERROR: Incorrect verb tag '{}'".format(msd)
    ret = "gla"
    ret += sep + DVERB[ 'type'   ][ msd[1] ] # Type
    ret += sep + DVERB[ 'gender' ][ msd[2] ] # Gender
    ret += sep + DVERB[ 'number' ][ msd[3] ] # Number
    ret += sep + DVERB[ 'case'   ][ msd[4] ] # Case

    return ret

DADJ = dict(
    type    = dict(f='opi', s='pri', o='red'),
    degree  = dict(p='poz', c='kom', s='sup', e='ela'),
    gender  = dict(m='mus', f='zen', n='sre'),
    number  = dict(s='jed', p='mno', t='zbi'),
    case    = dict(n='nom', g='gen', d='dat', a='aku', v='vok', i='ins', l='lok'),
    defin   = dict(n='neo', y='odr'),
    animate = dict(n='stv', y='ziv')
)

# Tags starting with "A" - adjectives (придев)
def _get_adjective_tag(msd, sep):
    if len(msd) < 7:
        return "ERROR: Incorrect adjective tag '{}'".format(msd)
    ret = "pri"
    ret += sep + DADJ[ 'type'   ][ msd[1] ] # Type
    ret += sep + DADJ[ 'degree' ][ msd[2] ] # Degree
    ret += sep + DADJ[ 'gender' ][ msd[3] ] # Gender
    ret += sep + DADJ[ 'number' ][ msd[4] ] # Number
    ret += sep + DADJ[ 'case'   ][ msd[5] ] # Case
    ret += sep + DADJ[ 'defin'  ][ msd[6] ] # Definitiveness
    if len(msd) == 8: # Animate
        ret += sep + DADJ[ 'animate' ][ msd[7] ]
    return ret

# Tags starting with "P" - pronoun (заменица)
def _get_pronoun_tag(msd, sep):
    pass

# Tags starting with "R" - adverb (прилог)
def _get_adverb_tag(msd, sep):
    pass

# Tags starting with "S" - adposition (предлог)
def _get_adposition_tag(msd, sep):
    pass

# Tags starting with "C" - conjunction (везник)
def _get_conjunction_tag(msd, sep):
    pass

# Tags starting with "M" - numeral (број)
def _get_numeral_tag(msd, sep):
    pass

# Tags starting with "Q" - particle (речца)
def _get_particle_tag(msd, sep):
    pass

# Tags starting with "I" - interjection (узвик)
def _get_interjection_tag(msd, sep):
    pass

# Tags starting with "Y" - abbreviation (скраћеница)
def _get_abbreviation_tag(msd, sep):
    pass

# Tags starting with "X" - residual (остатак)
def _get_residual_tag(msd, sep):
    return ""

# Publicly exposed function, the only callable from
# this package - calculates Serbian tag
# Param 1: MSD tag
# Param 2: separator

def get_tag(msd, sep):
    if msd in ("", None):
        return ""
    beglet = msd[0]
    if   beglet == "N": # Noun
        return _get_noun_tag(msd, sep)
    elif beglet == "V": # Verb
        return _get_verb_tag(msd, sep)
    elif beglet == "A": # Adjective
        return _get_adjective_tag(msd, sep)
    elif beglet == "P": # Pronoun
        return _get_pronoun_tag(msd,sep)
    elif beglet == "R": # Adverb
        return _get_adverb_tag(msd, sep)
    elif beglet == "S": # Adposition
        return _get_adposition_tag(msd, sep)
    elif beglet == "C": # Conjunction
        return _get_conjunction_tag(msd, sep)
    elif beglet == "M": # Numeral
        return _get_numeral_tag(msd, sep)
    elif beglet == "Q": # Particle
        return _get_particle_tag(msd, sep)
    elif beglet == "I": # Interjection
        return _get_interjection_tag(msd, sep)
    elif beglet == "Y": # Abbreviation
        return _get_abbreviation_tag(msd, sep)
    elif beglet == "X": # Residual
        return _get_residual_tag(msd, sep)
    else: # We don't know how to tag
        return "Unknown word type: {}".format(beglet)


# Test program by running it directly instead of importing it
if __name__ == "__main__":
    pass