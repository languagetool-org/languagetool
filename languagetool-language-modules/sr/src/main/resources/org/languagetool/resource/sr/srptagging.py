#!/usr/bin/env python3
# coding: utf-8

"""
This program makes POS tags for Serbian dictionary used in LanguageTool (LT).

"""

# PoS tags and their descriptions (in Serbian)
DDESC = {
    '0'   : ' - ', # нема
    '1LI' : 'прво лице', # лице
    '2LI' : 'друго лице', # лице
    '3LI' : 'треће лице', # лице
    'AKT' : 'активан',
    'AKU' : 'акузатив', # падеж
    'AOR' : 'аорист', # глаголско време
    'BRO' : 'број', # врста речи
    'CIF' : 'цифрама', # начин писања броја
    'DAT' : 'датив', # падеж
    'ELA' : 'елатив', # степен поређења
    'FUT' : 'футур', # глаголско време
    'GEN' : 'генитив', # падеж
    'GER' : 'герунд', # глаголски облик
    'GLA' : 'глагол', # врста речи
    'GLG' : 'глаголски', # прилог
    'GLV' : 'главни', # глагол
    'IME' : 'именица', # врста речи
    'IMF' : 'имперфект', # глаголско време
    'IMP' : 'императив', # глаголски начин
    'IND' : 'индикатив', # глаголски начин
    'INF' : 'инфинитив', # глаголски облик
    'INS' : 'инструментал', # падеж
    'JED' : 'једнина', # број
    'JEN' : 'једноставно', #
    'KOM' : 'компаратив', # степен поређења
    'KON' : 'кондиционал', # глаголски начин
    'KOP' : 'односни', # глагол
    'LIC' : 'лична', # заменица
    'LOK' : 'локатив', # падеж
    'MNO' : 'множина', # број
    'MOD' : 'модални/а', # глагол или речца
    'MUS' : 'мушки род', # род
    'NEG' : 'негативно',
    'NEO' : 'неодређен/а', # придев или заменица
    'NEP' : 'неправи', # прилог
    'NOM' : 'номинатив', # падеж
    'ODI' : 'одрична', # заменица или речца
    'ODR' : 'одређени', # придев
    'OPI' : 'описни/а', # придев или заменица
    'OSN' : 'основни', # врста броја
    'OST' : 'остало', # остале речи
    'PAR' : 'партицип', # глаголски облик
    'PAS' : 'прошло време', # глаголско време
    'PLP' : 'плусквамперфект', # глаголско време
    'POK' : 'показна', # заменица
    'POM' : 'помоћни', # глагол
    'POS' : 'посебан', # врста броја
    'POT' : 'потврдна', # речца
    'POV' : 'повратна', # заменица
    'POZ' : 'позитив', # степен поређења
    'PRA' : 'прави', # прилог
    'PRD' : 'придевски', # прилог
    'PRE' : 'предлог', # врста речи
    'PRI' : 'придев', # врста речи
    'PRL' : 'прилог', # врста речи
    'PRN' : 'презент', # глаголско време
    'PRO' : 'проста', # творба везника
    'PRS' : 'присвојни', # придев или заменица
    'PSV' : 'пасиван',
    'REC' : 'речца', # врста речи
    'RED' : 'редни', # придев или број
    'REL' : 'релативно', # заменица
    'RIM' : 'римски', # начин писања броја
    'SKR' : 'скраћеница', # као посебна "врста речи"
    'SLO' : 'сложено', # творба везника или предлог
    'SLV' : 'словима', # начин писања броја
    'SRE' : 'средњи род', # род
    'STV' : 'ствар', # врста именице
    'SUP' : 'суперлатив', # степен поређења
    'UPI' : 'упитна', # заменица, прилог или речца
    'UZV' : 'узвик', # врста речи
    'VEZ' : 'везник', # врста речи
    'VLA' : 'властита', # именица
    'VOK' : 'вокатив', # падеж
    'ZAJ' : 'заједничка', # именица
    'ZBI' : 'збирно', # број
    'ZEN' : 'женски род', # род
    'ZIV' : 'живо биће', # врста именице
}

DNOUN = dict(
    type    = dict(c='ZAJ', p='VLA'),
    # зај)едничка, вла)стита
    gender  = dict(m='MUS', f='ZEN', n='SRE'),
    # мус)ки, зен)ски, сре)дњи
    number  = dict(s='JED', p='MNO', t='ZBI'),
    # јед)нина, мно)жина, зби)рно
    case    = dict(n='NOM', g='GEN', d='DAT', a='AKU', v='VOK', i='INS', l='LOK'),
    # ном)инатив, ген)итив, дат)ив, аку)затив, вок)атив, инс)трументал, лок)атив
    animate = dict(n='STV', y='ZIV')
    # ств)ар, зив)о биће
)

# Tags starting with "N" - noun (именица)
def _get_noun_tag(msd, sep):
    if len(msd) < 5:
        return "ERROR: Incorrect noun tag '{}'".format(msd)
    ret = "IME"
    ret += sep + DNOUN[ 'type'   ][ msd[1] ] # Type
    ret += sep + DNOUN[ 'gender' ][ msd[2] ] # Gender
    ret += sep + DNOUN[ 'number' ][ msd[3] ] # Number
    ret += sep + DNOUN[ 'case'   ][ msd[4] ] # Case
    if len(msd) == 6: # Animate
        ret += sep + DNOUN[ 'animate' ][ msd[5] ]
    else:
        ret += sep + '0'
    return ret


DVERB = dict(
    type     = dict(m='GLV', a='POM', o='MOD', c='KOP'),
    # Врста: GLA = гла)вни, POM = пом)оћни, MOD = мод)ални, KOP = коп)улативни
    vform    = dict(i='IND', m='ZAP', c='USL', n='INF', p='PAR', g='GIM'),
    # Облик: IND = инд)икатив, ZAP = имп)ератив, USL = кон)диционал, INF = инф)инитив,
    # PAR = пар)тицип, GIM = г)лаголска им)еница (gerund)
    tense    = dict(p='PRN', i='IMF', f='FUT', s='PAS', l='PLP', a='AOR'),
    # Време: PRN = present, IMF = imperfekt, FUT = future, PAS = past,
    # PVK = pluperfect (плусквамперфект), AOR = aorist
    person   = {'1' : '1LI', '2' : '2LI', '3' : '3LI', '-' : '0'},
    # Лице: 1 = прво, 2 = друго, 3 = треће
    number   = {'s' : 'JED', 'p' : 'MNO', '-' : '0'},
    # Број: јед)нина, мно)жина
    gender   = dict(m='MUS', f='ZEN', n='SRE'),
    # Род: мус)ки, зен)ски, сре)дњи
    voice    = dict(a='AKT', p='PAS'),
    # акт)иван, пас)иван
)

# Tags starting with "V" - verb (глагол)
def _get_verb_tag(msd, sep):
    if len(msd) < 2:
        return "ERROR: Incorrect verb tag '{}'".format(msd)
    ret = "GLA"
    ret += sep + DVERB[ 'type'   ][ msd[1] ] # Type
    ret += sep + DVERB[ 'vform'  ][ msd[2] ] # VFrom
    ret += sep + DVERB[ 'tense'  ][ msd[3] ] # Tense
    ret += sep + DVERB[ 'person' ][ msd[4] ] # Person
    ret += sep + DVERB[ 'number' ][ msd[5] ] # Number
    ret += sep + DVERB[ 'gender' ][ msd[6] ] # Gender
    ret += sep + DVERB[ 'voice'  ][ msd[7] ] # Voice
    return ret


DADJ = dict(
    type    = dict(f='OPI', s='PRS', o='RED'),
    # Врста: опи)сни, при)својни, ред)ни
    degree  = dict(p='POZ', c='KOM', s='SUP', e='ELA'),
    # Степен поређења: поз)итив, ком)паратив, суп)ерлатив, ела)тив
    gender  = dict(m='MUS', f='ZEN', n='SRE'),
    # Род: мус)ки, зен)ски, сре)дњи
    number  = dict(s='JED', p='MNO', t='ZBI'),
    # Број: јед)нина, мно)жина, зби)ран ?
    case    = dict(n='NOM', g='GEN', d='DAT', a='AKU', v='VOK', i='INS', l='LOK'),
    # Падеж: ном)инатив, ген)итив, дат)ив, аку)затив, вок)атив, инс)трументал, лок)атив
    defin   = dict(n='NEO', y='ODR'),
    # Вид: нео)дређени, одр)еђени
    animate = dict(n='STV', y='ZIV')
    # „Живост“: ств)ар, зив)о биће
)

# Tags starting with "A" - adjectives (придев)
def _get_adjective_tag(msd, sep):
    if len(msd) < 7:
        return "ERROR: Incorrect adjective tag '{}'".format(msd)
    ret = "PRI"
    ret += sep + DADJ[ 'type'   ][ msd[1] ] # Type
    ret += sep + DADJ[ 'degree' ][ msd[2] ] # Degree
    ret += sep + DADJ[ 'gender' ][ msd[3] ] # Gender
    ret += sep + DADJ[ 'number' ][ msd[4] ] # Number
    ret += sep + DADJ[ 'case'   ][ msd[5] ] # Case
    ret += sep + DADJ[ 'defin'  ][ msd[6] ] # Definitiveness
    if len(msd) == 8: # Animate
        ret += sep + DADJ[ 'animate' ][ msd[7] ]
    else:
        ret += sep + '0'
    return ret


DPRO = dict(
    type = dict(p='LIC', d='POK', i='NEO', s='PRS', q='UPI', r='REL', x='POV', z='ODI', g='OPI'),
    # Врста: LIC = personal (лична), POK = demonstrative (показна), NEO = indefinite (неодређена)
    # UPI = interrogative (упитна), ODI = negative (одрична), OPI = general (општа)
    person = {'1' : '1LI', '2' : '2LI', '3' : '3LI', '-' : '0'},
    # Лице: 1 = прво, 2 = друго, 3 = треће
    gender = {'m' : 'MUS', 'f' : 'ZEN', 'n' : 'SRE', '-' : '0'},
    # Род: мус)ки, зен)ски, сре)дњи
    number = dict(s='JED', p='MNO', d='DVO'),
    # Број: јед)нина, мно)жина, дво)јина
    case   = dict(n='NOM', g='GEN', d='DAT', a='AKU', v='VOK', i='INS', l='LOK'),
    # Падеж: ном)инатив, ген)итив, дат)ив, аку)затив, вок)атив, инс)трументал, лок)атив
    onum = dict(s='JED', p='MNO'),
    # Број власника:
    ogend = dict(m='MUS', f='ZEN', n='SRE'),
    # „Живост“: ств)ар, зив)о биће
    animate = dict(n='STV', y='ZIV')
)

# Tags starting with "P" - pronoun (заменица)
def _get_pronoun_tag(msd, sep):
    if len(msd) < 7:
        return "ERROR: Incorrect pronoun tag '{}'".format(msd)
    ret = "ZAM"
    ret += sep + DPRO[ 'type'   ][ msd[1] ] # Type
    ret += sep + DPRO[ 'person' ][ msd[2] ] # Person
    ret += sep + DPRO[ 'gender' ][ msd[3] ] # Gender
    ret += sep + DPRO[ 'number' ][ msd[4] ] # Number
    ret += sep + DPRO[ 'case'   ][ msd[5] ] # Case
    ret += sep + DPRO[ 'onum'   ][ msd[6] ] # Owner number
    ret += sep + DPRO[ 'ogend'  ][ msd[7] ] # Owner gender
    ret += sep + DPRO[ 'animate'][ msd[8] ] # Animate
    return ret


DADV = dict(
    type = dict(g='PRA', z='NEP', a='PRD', v='GLG', q='UPI'),
    # Врста: пра)ви, неп)рави, при)девски, гла)голски, упи)тни
    degree = dict(p='POZ', c='KOM', s='SUP', e='ELA')
    # Степен: поз)итив, ком)паратив, суп)ерлатив, ела)тив
)

# Tags starting with "R" - adverb (прилог)
def _get_adverb_tag(msd, sep):
    if len(msd) == 2:
        return "PRL:???:???"
    ret = "PRL" + sep + DADV[ 'type' ][ msd[1] ] # Type
    ret += sep + DADV[ 'degree' ][ msd[2] ] # Degree
    return ret


DADP = dict(
    type = dict(p='PRE'),
    # Тип: пре)длог
    form = dict(s='JED', c='SLO'),
    # Форма: јед)ноставна, сло)жена
    case = dict(g='GEN', d='DAT', a='AKU', i='INS', l='LOK')
    # Падеж уз који иде: ген)итив, дат)ив, аку)затив, вок)атив, инс)трументал, лок)атив
)

# Tags starting with "S" - adposition (предлог)
def _get_adposition_tag(msd, sep):
    # Here we do a small trick - all Serbian adpositions are of type
    # "preposition". Other elements in DADP dictionary are for the sake
    # of completeness
    if len(msd) != 2:
        return "ERROR: Incorrect preposition tag '{}'".format(msd)
    ret = "PRE" + sep + DADV[ 'case' ][ msd[1] ] # Case
    return ret


# Serbian word corpus has only 2 types of tags for conjunctions
# Cc and Cs. Hence we can simplify lookup
DCON = dict(
    Cc = 'SLO', Cs = 'PRO'
    # Творба: сло)жена, про)ста
)

# Tags starting with "C" - conjunction (везник)
def _get_conjunction_tag(msd, sep):
    if len(msd) != 2:
        return "ERROR: Incorrect conjunction tag '{}'".format(msd)
    ret = 'VEZ' + sep + DCON[ msd ]
    return ret


DNUM = dict(
    type = dict(c='OSN', o='RED', m='MUL', l='collect', s='POS'),
    # Врста: OSN = cardinal (основни), RED = ordinal (редни), POS = special (посебан)
    gender = dict(m='MUS', f='ZEN', n='SRE'),
    # Род: мус)ки, зен)ски, сре)дњи
    number = dict(s='JED', p='MNO'),
    # Број: јед)нина, мно)жина
    case = dict(n='NOM', g='GEN', d='DAT', a='AKU', i='INS', l='LOK', v='VOK'),
    # Падеж: ном)инатив, ген)итив, дат)ив, аку)затив, инс)трументал, лок)атив
    form = dict(d='CIF', r='RIM', l='SLV'),
    # Облик: циф)рама, рим)ски, сло)вима
    animate = { 'n' : 'STV', 'y' : 'ZIV', '-' : '0'}
    # „Живост“: ств)ар, зив)о биће
)

# Tags starting with "M" - numeral (број)
def _get_numeral_tag(msd, sep):
    if len(msd) < 4:
        return "ERROR: Incorrect numeral tag '{}'".format(msd)
    ret = "BRO" + sep
    return ret

# Due to small number of particle keys, we will look values up directly
DPAR = dict(
    Qo='MOD', Qq='UPI', Qr='POT', Qz='ODI'
    # Врста: MOD = modal (модална), UPI = interrogative (упитна),
    # POT = affirmative (потврдна), ODI = negative (одрична)
)

# Tags starting with "Q" - particle (речца)
def _get_particle_tag(msd, sep):
    if len(msd) != 2:
        return "ERROR: Incorrect particle tag '{}'".format(msd)
    ret = 'REC' + sep + DPAR[ msd ]
    return ret


# Tags starting with "I" - interjection (узвик)
def _get_interjection_tag(msd, sep):
    return 'UZV'


# Tags starting with "Y" - abbreviation (скраћеница)
def _get_abbreviation_tag(msd, sep):
    return "SKR"

# Tags starting with "X" - residual (остатак)
def _get_residual_tag(msd, sep):
    return "OST" # Остатак

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

# Pretty printing PoS tag and description
def pprint(tag, desc):
    print( "{:<33} {}".format(tag, desc) )


def _print_noun_tags():
    for type in ('ZAJ', 'VLA'):
    # зај)едничка, вла)стита
        for gender in ('MUS', 'ZEN', 'SRE'):
        # мус)ки, зен)ски, сре)дњи
            for number in ('JED', 'MNO', 'ZBI'):
            # јед)нина, мно)жина, зби)рно
                for case in ('NOM', 'GEN', 'DAT', 'AKU', 'VOK', 'INS', 'LOK'):
                # ном)инатив, ген)итив, дат)ив, аку)затив, вок)атив, инс)трументал, лок)атив
                    for animate in ('STV', 'ZIV', '0'):
                    # ств)ар, зив)о биће
                        st = "IME:{}:{}:{}:{}:{}".format(type, gender, number, case, animate)
                        descr = "Именица : {} : {} : {} : {} : {}".format(
                            DDESC[ type ],
                            DDESC[ gender],
                            DDESC[ number ],
                            DDESC[ case ],
                            DDESC[ animate ])
                        pprint(st, descr)


def _print_adjective_tags():
    for type in ('OPI', 'PRS', 'RED'):
    # Врста: опи)сни, при)својни, ред)ни
        for degree in ('POZ', 'KOM', 'SUP', 'ELA'):
        # Степен поређења: поз)итив, ком)паратив, суп)ерлатив, ела)тив
            for gender in ('MUS', 'ZEN', 'SRE'):
            # Род: мус)ки, зен)ски, сре)дњи
                for number in ('JED', 'MNO', 'ZBI'):
                # Број: јед)нина, мно)жина, зби)ран ?
                    for case in ('NOM', 'GEN', 'DAT', 'AKU', 'VOK', 'INS', 'LOK'):
                    # Падеж: ном)инатив, ген)итив, дат)ив, аку)затив, вок)атив, инс)трументал, лок)атив
                        for defin in ('NEO', 'ODR'):
                        # Вид: нео)дређени, одр)еђени
                            for animate in ('STV', 'ZIV', '0'):
                            # „Живост“: ств)ар, зив)о биће
                                st = "PRI:{}:{}:{}:{}:{}:{}:{}".format(
                                    type, degree, gender, number, case, defin, animate)
                                descr = "Придев : {} : {} : {} : {} : {} : {} : {}".format(
                                    DDESC[ type ],
                                    DDESC[ degree ],
                                    DDESC[ gender ],
                                    DDESC[ number ],
                                    DDESC[ case ],
                                    DDESC[ defin ],
                                    DDESC[ animate ])
                                pprint(st, descr)


def _print_pronoun_tags():
    for type in ('LIC', 'POK', 'NEO', 'PRS', 'UPI', 'REL', 'POV', 'ODI', 'OPI'):
    # Врста:
        for person in ('1LI', '2LI', '3LI', '0'):
        # Лице: 1 = прво, 2 = друго, 3 = треће
            for gender in ('MUS', 'ZEN', 'SRE'):
            # Род: мус)ки, зен)ски, сре)дњи
                for number in ('JED', 'MNO'):
                # Број: јед)нина, мно)жина, дво)јина
                    for case in ('NOM', 'GEN', 'DAT', 'AKU', 'VOK', 'INS', 'LOK'):
                    # Падеж: ном)инатив, ген)итив, дат)ив, аку)затив, вок)атив, инс)трументал, лок)атив
                        for onum in ('JED', 'MNO'):
                        # Број власника:
                            for ogend in ('MUS', 'ZEN', 'SRE'):
                                st = "ZAM:{}:{}:{}:{}:{}:{}:{}".format(
                                    type, person, gender, number, case, onum, ogend)
                                descr = "Заменица : {} : {} : {} : {} : {} : власник {} : власник {}".format(
                                    DDESC[ type ],
                                    DDESC[ person ],
                                    DDESC[ gender ],
                                    DDESC[ number ],
                                    DDESC[ case ],
                                    DDESC[ onum ],
                                    DDESC[ ogend ])
                                pprint(st, descr)


def _print_adverb_tags():
    for type in ('PRA', 'NEP', 'PRD', 'GLG', 'UPI'):
    # Врста: пра)ви, неп)рави, при)девски, гла)голски, упи)тни
        for degree in ('POZ', 'KOM', 'SUP', 'ELA'):
        # Степен: поз)итив, ком)паратив, суп)ерлатив, ела)тив
            st = "PRL:{}:{}".format(type, degree)
            descr = "Прилог : {} : {}".format(DDESC[ type ], DDESC[ degree ])
            pprint(st, descr)


def _print_adposition_tags():
    for form in ('JEN', 'SLO'):
    # Форма: јед)ноставна, сло)жена
        for case in ('GEN', 'DAT', 'AKU', 'INS', 'LOK'):
        # Падеж уз који иде: ген)итив, дат)ив, аку)затив, вок)атив, инс)трументал, лок)атив
            st = "PRE:{}:{}".format(form, case)
            descr = "Предлог : форма {} : иде уз падеж {}".format(DDESC[ form ], DDESC[ case ])
            pprint(st, descr)


def _print_conjunction_tags():
    for type in ('SLO', 'PRO'):
        st = "VEZ:{}".format(type)
        descr = "Везник : {}".format(DDESC[ type ])
        pprint(st, descr)


def _print_numeral_tags():
    for type in ('OSN', 'RED', 'POS'):
    # Врста: OSN = cardinal (основни), RED = ordinal (редни), POS = special (посебан)
        for gender in ('MUS', 'ZEN', 'SRE'):
        # Род: мус)ки, зен)ски, сре)дњи
            for number in ('JED', 'MNO'):
            # Број: јед)нина, мно)жина
                for case in ('NOM', 'GEN', 'DAT', 'AKU', 'INS', 'LOK', 'VOK'):
                # Падеж: ном)инатив, ген)итив, дат)ив, аку)затив, инс)трументал, лок)атив
                    for form in ('CIF', 'RIM', 'SLV'):
                    # Облик: циф)рама, рим)ски, сло)вима
                        for animate in ('STV', 'ZIV', '0'):
                        # „Живост“: ств)ар, зив)о биће
                            st = "BRO:{}:{}:{}:{}:{}:{}".format(
                                type, gender, number, case, form, animate)
                            descr = "Број : {} : {} : {} : {} : {} : {}".format(
                                DDESC[ type ],
                                DDESC[ gender ],
                                DDESC[ number ],
                                DDESC[ case ],
                                DDESC[ form ],
                                DDESC[ animate ])
                            pprint(st, descr)

def _print_verb_tags():
    for type in ('GLV', 'POM', 'MOD', 'KOP'):
    # Врста: GLA = гла)вни, POM = пом)оћни, MOD = мод)ални, KOP = коп)улативни
        for vform in ('IND', 'IMP', 'KON', 'INF', 'PAR', 'GER'):
        # Облик: IND = инд)икатив, ZAP = имп)ератив, USL = кон)диционал, INF = инф)инитив,
        # PAR = пар)тицип, GIM = г)лаголска им)еница (gerund)
            for tense in ('PRN', 'IMF', 'FUT', 'PAS', 'PLP', 'AOR'):
            # Време: PRE = present, IMF = imperfekt, FUT = future, PAS = past,
            # PVK = pluperfect (плусквамперфект), AOR = aorist
                for person in ('1LI', '2LI', '3LI', '0'):
                # Лице: 1 = прво, 2 = друго, 3 = треће
                    for number in ('JED', 'MNO', '0'):
                    # Број: јед)нина, мно)жина
                        for gender in ('MUS', 'ZEN', 'SRE'):
                        # Род: мус)ки, зен)ски, сре)дњи
                            st = "GLA:{}:{}:{}:{}:{}:{}".format(
                                type, vform, tense, person, number, gender)
                            descr = "Глагол : {} : {} : {} : {} : {} : {}".format(
                                DDESC[ type ],
                                DDESC[ vform ],
                                DDESC[ tense ],
                                DDESC[ person ],
                                DDESC[ number ],
                                DDESC[ gender ])
                            pprint(st, descr)

def _print_particle_tags():
    for type in ('MOD', 'UPI', 'POT', 'ODI'):
        st = "REC:{}".format(type)
        descr = "Речца : {}".format(DDESC[ type ])
        pprint(st, descr)

def _print_interjection_tags():
    pprint("UZV", "Узвик")

def _print_abbreviation_tags():
    pprint("SKR", "Скраћеница")

def _print_residual_tags():
    pprint("OST", "Остатак")

# Prints list of all tags that program is capable of constructing
def get_list():
    print("Serbian PoS tags used in LanguageTool")
    print("=====================================")
    print(" ")
    _print_noun_tags()
    _print_verb_tags()
    _print_adjective_tags()
    _print_pronoun_tags()
    _print_adverb_tags()
    _print_adposition_tags()
    _print_conjunction_tags()
    _print_numeral_tags()
    _print_particle_tags()
    _print_interjection_tags()
    _print_abbreviation_tags()
    _print_residual_tags()

# Test program by running it directly instead of importing it
if __name__ == "__main__":
    import sys
    cmd = sys.argv[1]
    if cmd == '-l':
        get_list()