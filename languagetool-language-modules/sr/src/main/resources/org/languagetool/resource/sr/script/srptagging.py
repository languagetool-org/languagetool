#!/usr/bin/env python3
# coding: utf-8

"""
This program makes POS tags for Serbian dictionary used in LanguageTool (LT).
"""

# PoS tags and their descriptions (in Serbian)
DDESC = {
    '0J' : 'једнина', # број
    '0M' : 'множина', # број
    '0'  : ' - ', # информација не постоји или није битна
    '1J' : 'прво лице једнине', # лице
    '1L' : 'прво лице', # заменица, информација о лицу посебно
    '1M' : 'прво лице множине', # лице
    '2J' : 'друго лице једнине', # лице
    '2L' : 'друго лице', # заменица, информација о лицу посебно
    '2M' : 'друго лице множине', # лице
    '3J' : 'треће лице једнине', # лице
    '3L' : 'треће лице', # заменица, информација о лицу посебно
    '3M' : 'треће лице множине', # лице
    'AK' : 'акузатив', # падеж
    'AO' : 'аорист', # глаголско време
    'BR' : 'број', # врста речи
    'CI' : 'цифрама', # начин писања броја
    'DA' : 'датив', # падеж
    'FU' : 'футур', # глаголско време
    'GE' : 'генитив', # падеж
    'GL' : 'глагол', # врста речи
    'GN' : 'општи', # прилог (општи?)
    'GG' : 'глаголски', # прилог
    'GR' : 'градивна', # именица
    'GV' : 'главни', # глагол
    'IM' : 'именица', # врста речи
    'IF' : 'имперфект', # глаголско време
    'IP' : 'императив', # глаголски начин
    'ID' : 'индикатив', # глаголски начин
    'IN' : 'инфинитив', # глаголски облик
    'IS' : 'инструментал', # падеж
    'IT' : 'интерпункција',
    'KM' : 'компаратив', # степен поређења
    'KN' : 'кондиционал', # глаголски начин
    'KO' : 'односни', # копула (глагол)
    'LI' : 'лична', # заменица
    'LO' : 'локатив', # падеж
    'MO' : 'модални/а', # глагол или речца
    'MU' : 'мушки род', # род
    'NG' : 'негативно',
    'NE' : 'неодређен/а', # придев или заменица
    'NP' : 'неправи', # прилог
    'NO' : 'номинатив', # падеж
    'OD' : 'одрична', # заменица или речца
    'OR' : 'одређени', # придев
    'OP' : 'описни/а', # придев или заменица
    'OS' : 'општи', # придев
    'ON' : 'основни', # врста броја
    'OT' : 'остало', # остале речи
    'PA' : 'прошло време', # глаголско време
    'PB' : 'посебан', # врста броја
    'PC' : 'радни глаголски', # придев?
    'PD' : 'глаголски придев трпни', # participle passive
    'PE' : 'предлог', # врста речи
    'PF' : 'плусквамперфект', # глаголско време
    'PI' : 'придевски', # прилог
    'PK' : 'показна', # заменица
    'PL' : 'прилог', # врста речи
    'PM' : 'помоћни', # глагол
    'PN' : 'глаголски садашњи', # прилог, present participle
    'PO' : 'позитив', # степен поређења
    'PP' : 'глаголски прошли', # прилог, past participle
    'PR' : 'придев', # врста речи
    'PS' : 'присвојни/а', # придев или заменица
    'PT' : 'потврдна', # речца
    'PV' : 'повратни/а', # заменица или глагол
    'PZ' : 'презент', # глаголско време
    'RA' : 'глаголски придев радни',
    'RE' : 'речца', # врста речи
    'RD' : 'редни', # придев или број
    'RL' : 'релативно', # заменица
    'RI' : 'римски', # начин писања броја
    'SA' : 'саставни', # везник
    'SD' : 'садашњи', # прилог
    'SK' : 'скраћеница', # као посебна "врста речи"
    'SV' : 'словима', # начин писања броја
    'SR' : 'средњи род', # род
    'ST' : 'неживо', # врста именице
    'SU' : 'суперлатив', # степен поређења
    'UP' : 'упитна', # заменица, прилог или речца
    'UZ' : 'узвик', # врста речи
    'VE' : 'везник', # врста речи
    'VS' : 'вишеструки', # број
    'VL' : 'властита', # именица
    'VO' : 'вокатив', # падеж
    'ZA' : 'заједничка', # именица
    'ZM' : 'заменица', # врста речи
    'ZB' : 'збирна', # именица
    'ZE' : 'женски род', # род
    'ZI' : 'живо биће', # врста именице
    'ZV' : 'зависни', # везник
}

DNOUN = dict(
    type    = dict(c='ZA', p='VL', o='ZB', m='GR'),
    # зај)едничка (common), вла)стита (proper), збирна (collective), градивна (mass)
    gender  = dict(m='MU', f='ZE', n='SR'),
    # муш)ки, зен)ски, сре)дњи
    number  = dict(s='0J', p='0M'),
    # јед)нина, мно)жина, зби)рно
    case    = {'n' : 'NO', 'g' : 'GE', 'd' : 'DA', 'a' : 'AK', 'v' : 'VO', 'i' : 'IN', 'l' : 'LO', '-': '0', 'f' : 'NO'},
    # There are cases of feminine names where case is "f". Those should be in nominative
    # ном)инатив, ген)итив, дат)ив, аку)затив, вок)атив, инс)трументал, лок)атив
    animate = dict(n='ST', y='ZI')
    # ств)ар, зив)о биће
)

# Tags starting with "N" - noun (именица)
def _get_noun_tag(msd, sep):
    if len(msd) < 5:
        return "ERROR: Incorrect noun tag '{}'".format(msd)
    ret = "IM"
    ret += sep + DNOUN[ 'type'   ][ msd[1] ] # Type
    ret += sep + DNOUN[ 'gender' ][ msd[2] ] # Gender
    ret += sep + DNOUN[ 'number' ][ msd[3] ] # Number
    ret += sep + DNOUN[ 'case'   ][ msd[4] ] # Case
    if len(msd) > 5: # Animate
        ret += sep + DNOUN[ 'animate' ][ msd[5] ]
    return ret

def _print_noun_tags(sep, wrdesc):
    for type in DNOUN[ 'type' ].values():
        for gender in DNOUN[ 'gender' ].values():
            for number in DNOUN[ 'number' ].values():
                for case in DNOUN[ 'case' ].values():
                    st = "IM:{}:{}:{}:{}".format(type, gender, number, case)
                    if wrdesc:
                        descr = get_tag_desc(st, sep)
                        pprint(st, descr)
                    else:
                        pprint(st, None)
    for type in DNOUN[ 'type' ].values():
        for gender in DNOUN[ 'gender' ].values():
            for number in DNOUN[ 'number' ].values():
                for case in DNOUN[ 'case' ].values():
                    for animate in DNOUN[ 'animate' ].values():
                        st = "IM:{}:{}:{}:{}:{}".format(type, gender, number, case, animate)
                        if wrdesc:
                            descr = get_tag_desc(st, sep)
                            pprint(st, descr)
                        else:
                            pprint(st, None)

DVERB = dict(
    type     = dict(m='GV', a='PM', c='KO', r='PV'),
    # Врста: GV = main (главни), PM = auxiliarry (помоћни), KO = copula (односни), PV = reflexive (повратни)
    vform    = dict(n='IN', r='PZ', f='FU', m='IP', a='AO', e='IF', p='RA', q='PD', s='PN', t='PP'),
    # Облик: IN = инфинитив, PZ = презент, FU = футур
    # Партиципи:
    #   RA = participle active (глаголски придев радни)
    #   PD = participle passive (глаголски придев трпни)
    #   PN = participle present (глаголски прилог садашњи)
    #   PP = participle past (глаголски прилог прошли)
    # IP = императив, AO = аорист, IF = имперфект
    person   = {'1' : '1L', '2' : '2L', '3' : '3L', '-' : '0'},
    # Лице: 1 = прво, 2 = друго, 3 = треће
    number   = {'s' : '0J', 'p' : '0M', '-' : '0'},
    # Број: јед)нина, мно)жина
    gender   = {'-' : '0', 'm' : 'MU', 'f' : 'ZE', 'n' : 'SR'},
    # Род: мус)ки, зен)ски, сре)дњи
    negative = dict(y='NG', n='0')
)

# Tags starting with "V" - verb (глагол)
def _get_verb_tag(msd, sep):
    if len(msd) < 2:
        return "ERROR: Incorrect verb tag '{}'".format(msd)
    ret = 'GL'
    ret += sep + DVERB[ 'type'   ][ msd[1] ] # Type
    ret += sep + DVERB[ 'vform'  ][ msd[2] ] # VFrom
    if len(msd) > 3:
        ret += sep + DVERB[ 'person' ][ msd[3] ] # Person
        ret += sep + DVERB[ 'number' ][ msd[4] ] # Number
    if len(msd) > 5:
        ret += sep + DVERB[ 'gender' ][ msd[5] ] # Gender
    if len(msd) > 6:
        ret += sep + DVERB[ 'negative'  ][ msd[6] ] # Negative
    return ret

def _print_verb_tags(sep, wrdesc):
    for type in DVERB[ 'type' ].values():
        for vform in DVERB[ 'vform' ].values():
            st = "GL:{}:{}".format(
                type, vform)
            if wrdesc:
                descr = get_tag_desc(st, sep)
                pprint(st, descr)
            else:
                pprint(st, None)
    for type in DVERB[ 'type' ].values():
        for vform in DVERB[ 'vform' ].values():
            for person in DVERB[ 'person' ].values():
                st = "GL:{}:{}:{}".format(
                    type, vform, person)
                if wrdesc:
                    descr = get_tag_desc(st, sep)
                    pprint(st, descr)
                else:
                    pprint(st, None)
    for type in DVERB[ 'type' ].values():
        for vform in DVERB[ 'vform' ].values():
            for person in DVERB[ 'person' ].values():
                for number in DVERB[ 'number' ].values():
                    for gender in DVERB[ 'gender' ].values():
                        st = "GL:{}:{}:{}:{}:{}".format(
                            type, vform, person, number, gender)
                        if wrdesc:
                            descr = get_tag_desc(st, sep)
                            pprint(st, descr)
                        else:
                            pprint(st, None)
    for type in DVERB[ 'type' ].values():
        for vform in DVERB[ 'vform' ].values():
            for person in DVERB[ 'person' ].values():
                for number in DVERB[ 'number' ].values():
                    for gender in DVERB[ 'gender' ].values():
                        for negat in DVERB[ 'negative' ].values():
                            st = "GL:{}:{}:{}:{}:{}:{}".format(
                                type, vform, person, number, gender, negat)
                            if wrdesc:
                                descr = get_tag_desc(st, sep)
                                pprint(st, descr)
                            else:
                                pprint(st, None)


# Adjective (придев) attributes
DADJ = dict(
    type    = dict(g='OP', s='PS', p='PC'),
    # Врста: OP = описни, PS = присвојни, PC = радни глаголски
    degree  = dict(p='PO', c='KM', s='SU'),
    # Степен поређења: поз)итив, ком)паратив, суп)ерлатив
    gender  = dict(m='MU', f='ZE', n='SR'),
    # Род: мус)ки, зен)ски, сре)дњи
    number  = dict(s='0J', p='0M'),
    # Број: јед)нина, мно)жина, зби)ран
    case    = dict(n='NO', g='GE', d='DA', a='AK', v='VO', i='IS', l='LO'),
    # Падеж: ном)инатив, ген)итив, дат)ив, аку)затив, вок)атив, инс)трументал, лок)атив
    defin   = dict(n='NE', y='OR'),
    # Вид: нео)дређени, одр)еђени
    animate = { 'n' : 'ST', 'y' : 'ZI', '-' : '0'}
    # „Живост“: ств)ар, зив)о биће
)

# Tags starting with "A" - adjectives (придев)
def _get_adjective_tag(msd, sep):
    if len(msd) < 6:
        return "ERROR: Incorrect adjective tag '{}'".format(msd)
    ret = "PR"
    ret += sep + DADJ[ 'type'   ][ msd[1] ] # Type
    ret += sep + DADJ[ 'degree' ][ msd[2] ] # Degree
    ret += sep + DADJ[ 'gender' ][ msd[3] ] # Gender
    ret += sep + DADJ[ 'number' ][ msd[4] ] # Number
    ret += sep + DADJ[ 'case'   ][ msd[5] ] # Case
    if len(msd) == 7:
        ret += sep + DADJ[ 'defin'  ][ msd[6] ] # Definitiveness
    if len(msd) == 8: # Animate
        ret += sep + DADJ[ 'animate' ][ msd[7] ]
    return ret

def _print_adjective_tags(sep, wrdesc):
    for type in DADJ[ 'type' ].values():
        for degree in DADJ[ 'degree' ].values():
            for gender in DADJ[ 'gender' ].values():
                for number in DADJ[ 'number' ].values():
                    for case in DADJ[ 'case' ].values():
                        for defin in DADJ[ 'defin' ].values():
                            st = "PR:{}:{}:{}:{}:{}:{}".format(
                                type, degree, gender, number, case, defin)
                            if wrdesc:
                                descr = get_tag_desc(st, sep)
                                pprint(st, descr)
                            else:
                                pprint(st, None)
    for type in DADJ[ 'type' ].values():
        for degree in DADJ[ 'degree' ].values():
            for gender in DADJ[ 'gender' ].values():
                for number in DADJ[ 'number' ].values():
                    for case in DADJ[ 'case' ].values():
                        for defin in DADJ[ 'defin' ].values():
                            for animate in DADJ[ 'animate' ].values():
                                st = "PR:{}:{}:{}:{}:{}:{}:{}".format(
                                    type, degree, gender, number, case, defin, animate)
                                if wrdesc:
                                    descr = get_tag_desc(st, sep)
                                    pprint(st, descr)
                                else:
                                    pprint(st, None)


# Pronoun (заменица) attributes
DPRO = dict(
    type = dict(p='LI', d='PK', i='NE', s='PS', q='UP', r='RE', x='PV'),
    # Врста: LI = лична (personal), PK = показна (demonstrative), NE = неодређена (indefinite)
    # UP = упитна (interrogative), RE = релативна (relative), PV = повратна (reflexive)
    # PS = присвојна (possessive)
    person = {'1' : '1L', '2' : '2L', '3' : '3L', '-' : '0'},
    # Лице: 1 = прво, 2 = друго, 3 = треће
    gender = {'m' : 'MU', 'f' : 'ZE', 'n' : 'SR', '-' : '0'},
    # Род: мус)ки, зен)ски, сре)дњи
    number ={'s': '0J', 'p' : '0M', '-' : '0'},
    # Број: јед)нина, мно)жина
    case   = dict(n='NO', g='GE', d='DA', a='AK', v='VO', i='IS', l='LO'),
    # Падеж: ном)инатив, ген)итив, дат)ив, аку)затив, вок)атив, инс)трументал, лок)атив
    animate = dict(n='ST', y='ZI')
    # „Живост“: ств)ар, зив)о биће
)

# Tags starting with "P" - pronoun (заменица)
def _get_pronoun_tag(msd, sep):
    if len(msd) not in (2, 6, 7):
        return "ERROR: Incorrect pronoun tag '{}'".format(msd)
    ret = 'ZM'
    ret += sep + DPRO[ 'type'   ][ msd[1] ] # Type
    if len(msd) > 2:
        ret += sep + DPRO[ 'person' ][ msd[2] ] # Person
        ret += sep + DPRO[ 'gender' ][ msd[3] ] # Gender
        ret += sep + DPRO[ 'number' ][ msd[4] ] # Number
        ret += sep + DPRO[ 'case'   ][ msd[5] ] # Case
    if len(msd) > 6: # Animate
        ret += sep + DPRO[ 'animate' ][ msd[6] ]
    return ret

def _print_pronoun_tags(sep, wrdesc):
    for type in DPRO[ 'type' ].values():
        st = "ZM:{}".format(
            type)
        if wrdesc:
            descr = get_tag_desc(st, sep)
            pprint(st, descr)
        else:
            pprint(st, None)
    for type in DPRO[ 'type' ].values():
        for person in DPRO[ 'person' ].values():
            for gender in DPRO[ 'gender' ].values():
                for number in DPRO[ 'number' ].values():
                    for case in DPRO[ 'case' ].values():
                        st = "ZM:{}:{}:{}:{}:{}".format(
                            type, person, gender, number, case)
                        if wrdesc:
                            descr = get_tag_desc(st, sep)
                            pprint(st, descr)
                        else:
                            pprint(st, None)
    for type in DPRO[ 'type' ].values():
        for person in DPRO[ 'person' ].values():
            for gender in DPRO[ 'gender' ].values():
                for number in DPRO[ 'number' ].values():
                    for case in DPRO[ 'case' ].values():
                        for anim in DPRO[ 'animate' ].values():
                            st = "ZM:{}:{}:{}:{}:{}:{}".format(
                                type, person, gender, number, case, anim)
                            if wrdesc:
                                descr = get_tag_desc(st, sep)
                                pprint(st, descr)
                            else:
                                pprint(st, None)


DADV = dict(
    type = dict(g='GN', r='PN', p='PP'),
    # Врста: GN = општи, PN = глаголски садашњи, PP = глаголски прошли
    degree = dict(p='PO', c='KM', s='SU')
    # Степен: поз)итив, ком)паратив, суп)ерлатив
)

# Tags starting with "R" - adverb (прилог)
def _get_adverb_tag(msd, sep):
    if len(msd) not in (2, 3):
        return "ERROR: Incorrect adverb tag '{}'".format(msd)
    ret = 'PL' + sep + DADV[ 'type' ][ msd[1] ] # Type
    if len(msd) == 3:
        ret += sep + DADV[ 'degree' ][ msd[2] ] # Degree
    return ret

def _print_adverb_tags(sep, wrdesc):
    for type in DADV[ 'type' ].values():
        st = 'PL:{}'.format(type)
        if wrdesc:
            descr = get_tag_desc(st, sep)
            pprint(st, descr)
        else:
            pprint(st, None)
    for type in DADV[ 'type' ].values():
        for degree in DADV[ 'degree' ].values():
            st = 'PL:{}:{}'.format(type, degree)
            if wrdesc:
                descr = get_tag_desc(st, sep)
                pprint(st, descr)
            else:
                pprint(st, None)

DADP = dict(
    case = dict(g='GE', d='DA', a='AK', i='IS', l='LO')
    # Падеж уз који иде: ген)итив, дат)ив, аку)затив, вок)атив, инс)трументал, лок)атив
)

# Tags starting with "S" - adposition (предлог)
def _get_adposition_tag(msd, sep):
    if len(msd) != 2:
        return "ERROR: Incorrect preposition tag '{}'".format(msd)
    ret = 'PE' + sep + DADP[ 'case' ][ msd[1] ] # Case
    return ret

def _print_adposition_tags(sep, wrdesc):
    for case in DADP[ 'case' ].values():
        st = 'PE:{}'.format(case)
        if wrdesc:
            descr = get_tag_desc(st, sep)
            pprint(st, descr)
        else:
            pprint(st, None)

# Serbian word corpus has only 2 types of tags for conjunctions
# Cc and Cs. Hence we can simplify lookup
DCON = dict(
    Cc = 'SA', Cs = 'ZV'
    # Cc = coordinating (SA, саставни), Cs = subordinating (ZV, зависни)
)

# Tags starting with "C" - conjunction (везник)
def _get_conjunction_tag(msd, sep):
    if len(msd) != 2:
        return "ERROR: Incorrect conjunction tag '{}'".format(msd)
    ret = 'VE' + sep + DCON[ msd ]
    return ret

def _print_conjunction_tags(sep, wrdesc):
    for type in DCON.values():
        st = 'VE:{}'.format(type)
        if wrdesc:
            descr = get_tag_desc(st, sep)
            pprint(st, descr)
        else:
            pprint(st, None)


DNUM = dict(
    form = dict(d='CI', r='RI', l='SV'),
    # Облик: циф)рама, рим)ски, сло)вима
    type = dict(c='ON', o='RD', m='VS', s='PB'),
    # Врста: ON = cardinal (основни), RD = ordinal (редни), VS = multiple (вишеструки), PB = special (посебан)
    gender = {'m' : 'MU', 'f' : 'ZE', 'n' : 'SR', '-' : '0'},
    # Род: мус)ки, зен)ски, сре)дњи
    number = {'s' : '0J', 'p' : '0M', '-' : '0'},
    # Број: јед)нина, мно)жина, род није битан
    case = dict(n='NO', g='GE', d='DA', a='AK', i='IS', l='LO', v='VO'),
    # Падеж: ном)инатив, ген)итив, дат)ив, аку)затив, инс)трументал, лок)атив
    animate = { 'n' : 'ST', 'y' : 'ZI', '-' : '0'}
    # „Живост“: ств)ар, зив)о биће
)

# Tags starting with "M" - numeral (број)
def _get_numeral_tag(msd, sep):
    if len(msd) < 3:
        return "ERROR: Incorrect numeral tag '{}'".format(msd)
    ret = 'BR'
    ret += sep + DNUM[ 'form' ][ msd[1] ] # Form: digit, Roman or letter
    ret += sep + DNUM[ 'type' ][ msd[2] ] # Type
    if len(msd) > 3:
        ret += sep + DNUM[ 'gender' ][ msd[3] ] # Gender
        ret += sep + DNUM[ 'number' ][ msd[4] ] # Number
        ret += sep + DNUM[ 'case'   ][ msd[5] ] # Case
    if len(msd) > 6:
        ret += sep + DNUM[ 'animate'][ msd[6] ] # Animate
    return ret

def _print_numeral_tags(sep, wrdesc):
    for form in DNUM[ 'form' ].values():
        for type in DNUM[ 'type' ].values():
            st = "BR:{}:{}".format(form, type)
            if wrdesc:
                descr = get_tag_desc(st, sep)
                pprint(st, descr)
            else:
                pprint(st, None)
    for form in DNUM[ 'form' ].values():
        for type in DNUM[ 'type' ].values():
            for gender in DNUM[ 'gender' ].values():
                for number in DNUM[ 'number' ].values():
                    for case in DNUM[ 'case' ].values():
                        st = "BR:{}:{}:{}:{}:{}".format(
                            form, type, gender, number, case)
                        if wrdesc:
                            descr = get_tag_desc(st, sep)
                            pprint(st, descr)
                        else:
                            pprint(st, None)
    for form in DNUM[ 'form' ].values():
        for type in DNUM[ 'type' ].values():
            for gender in DNUM[ 'gender' ].values():
                for number in DNUM[ 'number' ].values():
                    for case in DNUM[ 'case' ].values():
                        for animate in DNUM[ 'animate' ].values():
                            st = "BR:{}:{}:{}:{}:{}:{}".format(
                                form, type, gender, number, case, animate)
                            if wrdesc:
                                descr = get_tag_desc(st, sep)
                                pprint(st, descr)
                            else:
                                pprint(st, None)


# Due to small number of particle keys, we will look values up directly
DPAR = dict(
    Qo='MO', Qq='UP', Qr='PT', Qz='OD'
    # Врста: MO = modal (модална), UP = interrogative (упитна),
    # PT = affirmative (потврдна), OD = negative (одрична)
)

# Tags starting with "Q" - particle (речца)
def _get_particle_tag(msd, sep):
    if len(msd) != 2:
        return "ERROR: Incorrect particle tag '{}'".format(msd)
    ret = 'RE' + sep + DPAR[ msd ]
    return ret


def _print_particle_tags(sep, wrdesc):
    for type in DPAR.values():
        st = 'RE:{}'.format(type)
        if wrdesc:
            descr = get_tag_desc(st, sep)
            pprint(st, descr)
        else:
            pprint(st, None)


# Tags starting with "I" - interjection (узвик)
def _get_interjection_tag(msd, sep):
    return 'UZ'

def _print_interjection_tags(sep, wrdesc):
    desc = get_tag_desc('UZ', sep)
    if wrdesc:
        pprint("UZ", desc)
    else:
        pprint("UZ", None)


# Tags starting with "Y" - abbreviation (скраћеница)
def _get_abbreviation_tag(msd, sep):
    return "SK"

def _print_abbreviation_tags(sep, wrdesc):
    desc = get_tag_desc('SK', sep)
    if wrdesc:
        pprint("SK", desc)
    else:
        pprint("SK", None)


# Tags starting with "X" - residual (остатак)
def _get_residual_tag(msd, sep):
    return "OT" # Остатак

def _print_residual_tags(sep, wrdesc):
    if wrdesc:
        pprint("OS", "Остатак речничког фонда")
    else:
        pprint("OS", None)

# Tags starting with "Z" - punctuation (интерпункција)
def _get_punctuation_tag(msd, sep):
    return "IT"

def _print_punctuation_tags(sep, wrdesc):
    if wrdesc:
        pprint("IT", "Знак интерпункције")
    else:
        pprint("IT", None)


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
    elif beglet == "Z": # Punctuation
        return _get_punctuation_tag(msd, sep)
    else: # We don't know how to tag
        return "Unknown word type: {}".format(beglet)


# Pretty printing PoS tag and description
def pprint(tag, desc):
    if desc is not None:
        print( "{:<33} {}".format(tag, desc) )
    else:
        print( "{}".format(tag) )


# Prints list of all tags that program is capable of constructing
def get_list(sep, wrdesc=False):
    if wrdesc:
        print("Serbian PoS tags used in LanguageTool")
        print("=====================================")
        print(" ")
    _print_noun_tags(sep, wrdesc)
    _print_verb_tags(sep, wrdesc)
    _print_adjective_tags(sep, wrdesc)
    _print_pronoun_tags(sep, wrdesc)
    _print_adverb_tags(sep, wrdesc)
    _print_adposition_tags(sep, wrdesc)
    _print_conjunction_tags(sep, wrdesc)
    _print_numeral_tags(sep, wrdesc)
    _print_particle_tags(sep, wrdesc)
    _print_interjection_tags(sep, wrdesc)
    _print_abbreviation_tags(sep, wrdesc)
    _print_residual_tags(sep, wrdesc)
    _print_punctuation_tags(sep, wrdesc)


def _test_noun_tags(sep):
    """
    Tests conversion of PoS noun ("N") tags to LT Serbian PoS tags
    :param sep: Tag separator
    """
    tags = """
Ncfpa
Ncfpd
Ncfpg
Ncfpi
Ncfpl
Ncfpn
Ncfpv
Ncfsa
Ncfsd
Ncfsg
Ncfsi
Ncfsl
Ncfsn
Ncfsv
Ncmpa
Ncmpd
Ncmpg
Ncmpi
Ncmpl
Ncmpn
Ncmpv
Ncmsan
Ncmsay
Ncmsd
Ncmsg
Ncmsi
Ncmsl
Ncmsn
Ncmsv
Ncnpa
Ncnpd
Ncnpg
Ncnpi
Ncnpl
Ncnpn
Ncnpv
Ncnsa
Ncnsd
Ncnsg
Ncnsi
Ncnsl
Ncnsn
Ncnsv
Npfpa
Npfpd
Npfpg
Npfpi
Npfpl
Npfpn
Npfpv
Npfs-
Npfsa
Npfsan
Npfsay
Npfsd
Npfsf
Npfsg
Npfsi
Npfsl
Npfsn
Npfsv
Npmpa
Npmpd
Npmpg
Npmpi
Npmpl
Npmpn
Npmpv
Npms-
Npmsa
Npmsan
Npmsay
Npmsd
Npmsf
Npmsg
Npmsi
Npmsl
Npmsn
Npmsv
Npnpa
Npnpd
Npnpg
Npnpi
Npnpl
Npnpn
Npnpv
Npnsa
Npnsay
Npnsd
Npnsg
Npnsi
Npnsl
Npnsn
Npnsv
"""
    _test_tag_list(tags, sep)

def _test_adjective_tags(sep):
    """
    Tests conversion of PoS adjective ("A") tags to LT Serbian PoS tags
    :param sep: Tag separator
    """
    tags = """
Agcfpay
Agcfpdy
Agcfpgy
Agcfpiy
Agcfply
Agcfpny
Agcfpvy
Agcfsay
Agcfsdy
Agcfsgy
Agcfsiy
Agcfsly
Agcfsny
Agcfsvy
Agcmpay
Agcmpdy
Agcmpgy
Agcmpiy
Agcmply
Agcmpny
Agcmpvy
Agcmsayn
Agcmsayy
Agcmsdy
Agcmsgy
Agcmsiy
Agcmsly
Agcmsny
Agcmsvy
Agcnpay
Agcnpdy
Agcnpgy
Agcnpiy
Agcnply
Agcnpny
Agcnpvy
Agcnsay
Agcnsdy
Agcnsgy
Agcnsiy
Agcnsly
Agcnsny
Agcnsvy
Agpfpay
Agpfpdy
Agpfpgy
Agpfpiy
Agpfply
Agpfpny
Agpfpvy
Agpfsay
Agpfsdy
Agpfsgy
Agpfsin
Agpfsiy
Agpfsly
Agpfsny
Agpfsvy
Agpmpay
Agpmpdy
Agpmpgy
Agpmpiy
Agpmply
Agpmpny
Agpmpvy
Agpmsann
Agpmsany
Agpmsayn
Agpmsayy
Agpmsdn
Agpmsdy
Agpmsgn
Agpmsgy
Agpmsiy
Agpmsln
Agpmsly
Agpmsnn
Agpmsny
Agpmsvn
Agpmsvy
Agpnpay
Agpnpdy
Agpnpgy
Agpnpiy
Agpnply
Agpnpny
Agpnpvy
Agpnsay
Agpnsdn
Agpnsdy
Agpnsgn
Agpnsgy
Agpnsiy
Agpnsln
Agpnsly
Agpnsny
Agpnsvy
Agsfpay
Agsfpdy
Agsfpgy
Agsfpiy
Agsfply
Agsfpny
Agsfpvy
Agsfsay
Agsfsdy
Agsfsgy
Agsfsiy
Agsfsly
Agsfsny
Agsfsvy
Agsmpay
Agsmpdy
Agsmpgy
Agsmpiy
Agsmply
Agsmpny
Agsmpvy
Agsmsayn
Agsmsayy
Agsmsdy
Agsmsgy
Agsmsiy
Agsmsly
Agsmsny
Agsmsvy
Agsnpay
Agsnpdy
Agsnpgy
Agsnpiy
Agsnply
Agsnpny
Agsnpvy
Agsnsay
Agsnsdy
Agsnsgy
Agsnsiy
Agsnsly
Agsnsny
Agsnsvy
Appfpay
Appfpdy
Appfpgy
Appfpiy
Appfply
Appfpny
Appfpvy
Appfsay
Appfsdy
Appfsgy
Appfsiy
Appfsly
Appfsny
Appfsvy
Appmpay
Appmpdy
Appmpgy
Appmpiy
Appmply
Appmpny
Appmpvy
Appmsann
Appmsany
Appmsayn
Appmsayy
Appmsdn
Appmsdy
Appmsgn
Appmsgy
Appmsiy
Appmsln
Appmsly
Appmsnn
Appmsny
Appmsvn
Appmsvy
Appnpay
Appnpdy
Appnpgy
Appnpiy
Appnply
Appnpny
Appnpvy
Appnsay
Appnsdn
Appnsdy
Appnsgn
Appnsgy
Appnsiy
Appnsln
Appnsly
Appnsny
Appnsvy
Aspfpay
Aspfpdy
Aspfpgy
Aspfpiy
Aspfply
Aspfpny
Aspfpvy
Aspfsay
Aspfsdy
Aspfsgy
Aspfsiy
Aspfsly
Aspfsny
Aspfsvy
Aspmpay
Aspmpdy
Aspmpgy
Aspmpiy
Aspmply
Aspmpny
Aspmpvy
Aspmsann
Aspmsany
Aspmsayy
Aspmsdn
Aspmsdy
Aspmsgn
Aspmsgy
Aspmsiy
Aspmsln
Aspmsly
Aspmsnn
Aspmsvn
Aspnpay
Aspnpdy
Aspnpgy
Aspnpiy
Aspnply
Aspnpny
Aspnpvy
Aspnsay
Aspnsdn
Aspnsdy
Aspnsgn
Aspnsgy
Aspnsiy
Aspnsln
Aspnsly
Aspnsny
Aspnsvy
    """
    _test_tag_list(tags, sep)


def _test_conjunction_tags(sep):
    """
    Tests conversion of PoS conjunction ("C") tags to LT Serbian PoS tags
    :param sep: Tag separator
    """
    tags = """
    Cc
    Cs
    """
    _test_tag_list(tags,sep)

def _test_interjection_tags(sep):
    """
    Tests conversion of PoS interjection ("I") tags to LT Serbian PoS tags
    :param sep: Tag separator
    """
    tags = """
    I
    """
    _test_tag_list(tags, sep)

def _test_numeral_tags(sep):
    """
    Tests conversion of PoS numeral ("M") tags to LT Serbian PoS tags
    :param sep: Tag separator
    """
    tags = """
Mlc
Mlc--a
Mlc--d
Mlcf-a
Mlcf-d
Mlcf-g
Mlcf-i
Mlcf-l
Mlcf-n
Mlcfpa
Mlcfpd
Mlcfpg
Mlcfpi
Mlcfpl
Mlcfpn
Mlcfpv
Mlcfsa
Mlcfsd
Mlcfsg
Mlcfsi
Mlcfsl
Mlcfsn
Mlcfsv
Mlcf-v
Mlc--g
Mlc--i
Mlc--l
Mlcm-a
Mlcm-d
Mlcm-g
Mlcm-i
Mlcm-l
Mlcm-n
Mlcmpa
Mlcmpd
Mlcmpg
Mlcmpi
Mlcmpl
Mlcmpn
Mlcmpv
Mlcmsan
Mlcmsay
Mlcmsd
Mlcmsg
Mlcmsi
Mlcmsl
Mlcmsn
Mlcmsv
Mlcm-v
Mlc--n
Mlcn-a
Mlcn-d
Mlcn-g
Mlcn-i
Mlcn-l
Mlcn-n
Mlcnpa
Mlcnpd
Mlcnpg
Mlcnpi
Mlcnpl
Mlcnpn
Mlcnpv
Mlcnsa
Mlcnsd
Mlcnsg
Mlcnsi
Mlcnsl
Mlcnsn
Mlcnsv
Mlcn-v
Mlc--v
Mlofpa
Mlofpd
Mlofpg
Mlofpi
Mlofpl
Mlofpn
Mlofpv
Mlofsa
Mlofsd
Mlofsg
Mlofsi
Mlofsl
Mlofsn
Mlofsv
Mlompa
Mlompd
Mlompg
Mlompi
Mlompl
Mlompn
Mlompv
Mlomsan
Mlomsay
Mlomsd
Mlomsg
Mlomsi
Mlomsl
Mlomsn
Mlomsv
Mlonpa
Mlonpd
Mlonpg
Mlonpi
Mlonpl
Mlonpn
Mlonpv
Mlonsa
Mlonsd
Mlonsg
Mlonsi
Mlonsl
Mlonsn
Mlonsv
Mls
Mlsn-a
Mlsn-d
Mlsn-g
Mlsn-i
Mlsn-l
Mlsn-n
Mlsn-v
Mrc
    """
    _test_tag_list(tags,sep)


def _test_pronoun_tags(sep):
    """
    Tests conversion of PoS pronoun ("P") tags to LT Serbian PoS tags
    :param sep: Tag separator
    """
    tags = """
    Pd-fpa
Pd-fpd
Pd-fpg
Pd-fpi
Pd-fpl
Pd-fpn
Pd-fsa
Pd-fsd
Pd-fsg
Pd-fsi
Pd-fsl
Pd-fsn
Pd-mpa
Pd-mpd
Pd-mpg
Pd-mpi
Pd-mpl
Pd-mpn
Pd-msan
Pd-msay
Pd-msd
Pd-msg
Pd-msi
Pd-msl
Pd-msn
Pd-npa
Pd-npd
Pd-npg
Pd-npi
Pd-npl
Pd-npn
Pd-nsa
Pd-nsd
Pd-nsg
Pd-nsi
Pd-nsl
Pd-nsn
Pi3m-a
Pi3m-d
Pi3m-g
Pi3m-i
Pi3m-l
Pi3m-n
Pi3m-v
Pi3n-a
Pi3n-d
Pi3n-g
Pi3n-i
Pi3n-l
Pi3n-n
Pi3n-v
Pi-fpa
Pi-fpd
Pi-fpg
Pi-fpi
Pi-fpl
Pi-fpn
Pi-fpv
Pi-fsa
Pi-fsd
Pi-fsg
Pi-fsi
Pi-fsl
Pi-fsn
Pi-fsv
Pi-mpa
Pi-mpd
Pi-mpg
Pi-mpi
Pi-mpl
Pi-mpn
Pi-mpv
Pi-msan
Pi-msay
Pi-msd
Pi-msg
Pi-msi
Pi-msl
Pi-msn
Pi-msv
Pi-npa
Pi-npd
Pi-npg
Pi-npi
Pi-npl
Pi-npn
Pi-npv
Pi-nsa
Pi-nsd
Pi-nsg
Pi-nsi
Pi-nsl
Pi-nsn
Pi-nsv
Pi--sa
Pi--sd
Pi--sg
Pi--si
Pi--sl
Pi--sn
Pp1-pa
Pp1-pd
Pp1-pg
Pp1-pi
Pp1-pl
Pp1-pn
Pp1-pv
Pp1-sa
Pp1-sd
Pp1-sg
Pp1-si
Pp1-sl
Pp1-sn
Pp2-pa
Pp2-pd
Pp2-pg
Pp2-pi
Pp2-pl
Pp2-pn
Pp2-pv
Pp2-sa
Pp2-sd
Pp2-sg
Pp2-si
Pp2-sl
Pp2-sn
Pp2-sv
Pp3fpn
Pp3fsa
Pp3fsd
Pp3fsg
Pp3fsi
Pp3fsl
Pp3fsn
Pp3mpn
Pp3msa
Pp3msd
Pp3msg
Pp3msi
Pp3msl
Pp3msn
Pp3npn
Pp3nsa
Pp3nsd
Pp3nsg
Pp3nsi
Pp3nsl
Pp3nsn
Pp3-pa
Pp3-pd
Pp3-pg
Pp3-pi
Pp3-pl
Pq
Pq3m-a
Pq3m-d
Pq3m-g
Pq3m-i
Pq3m-l
Pq3m-n
Pq3n-a
Pq3n-d
Pq3n-g
Pq3n-i
Pq3n-l
Pq3n-n
Pq-fpa
Pq-fpd
Pq-fpg
Pq-fpi
Pq-fpl
Pq-fpn
Pq-fsa
Pq-fsd
Pq-fsg
Pq-fsi
Pq-fsl
Pq-fsn
Pq-mpa
Pq-mpd
Pq-mpg
Pq-mpi
Pq-mpl
Pq-mpn
Pq-msan
Pq-msay
Pq-msd
Pq-msg
Pq-msi
Pq-msl
Pq-msn
Pq-npa
Pq-npd
Pq-npg
Pq-npi
Pq-npl
Pq-npn
Pq-nsa
Pq-nsd
Pq-nsg
Pq-nsi
Pq-nsl
Pq-nsn
Ps1fpa
Ps1fpd
Ps1fpg
Ps1fpi
Ps1fpl
Ps1fpn
Ps1fpv
Ps1fsa
Ps1fsd
Ps1fsg
Ps1fsi
Ps1fsl
Ps1fsn
Ps1fsv
Ps1mpa
Ps1mpd
Ps1mpg
Ps1mpi
Ps1mpl
Ps1mpn
Ps1mpv
Ps1msan
Ps1msay
Ps1msd
Ps1msg
Ps1msi
Ps1msl
Ps1msn
Ps1msv
Ps1npa
Ps1npd
Ps1npg
Ps1npi
Ps1npl
Ps1npn
Ps1npv
Ps1nsa
Ps1nsd
Ps1nsg
Ps1nsi
Ps1nsl
Ps1nsn
Ps1nsv
Ps2fpa
Ps2fpd
Ps2fpg
Ps2fpi
Ps2fpl
Ps2fpn
Ps2fpv
Ps2fsa
Ps2fsd
Ps2fsg
Ps2fsi
Ps2fsl
Ps2fsn
Ps2fsv
Ps2mpa
Ps2mpd
Ps2mpg
Ps2mpi
Ps2mpl
Ps2mpn
Ps2mpv
Ps2msan
Ps2msay
Ps2msd
Ps2msg
Ps2msi
Ps2msl
Ps2msn
Ps2msv
Ps2npa
Ps2npd
Ps2npg
Ps2npi
Ps2npl
Ps2npn
Ps2npv
Ps2nsa
Ps2nsd
Ps2nsg
Ps2nsi
Ps2nsl
Ps2nsn
Ps2nsv
Ps3fpa
Ps3fpd
Ps3fpg
Ps3fpi
Ps3fpl
Ps3fpn
Ps3fpv
Ps3fsa
Ps3fsd
Ps3fsg
Ps3fsi
Ps3fsl
Ps3fsn
Ps3fsv
Ps3mpa
Ps3mpd
Ps3mpg
Ps3mpi
Ps3mpl
Ps3mpn
Ps3mpv
Ps3msan
Ps3msay
Ps3msd
Ps3msg
Ps3msi
Ps3msl
Ps3msn
Ps3msv
Ps3npa
Ps3npd
Ps3npg
Ps3npi
Ps3npl
Ps3npn
Ps3npv
Ps3nsa
Ps3nsd
Ps3nsg
Ps3nsi
Ps3nsl
Ps3nsn
Ps3nsv
Px-fpa
Px-fpd
Px-fpg
Px-fpi
Px-fpl
Px-fpn
Px-fsa
Px-fsd
Px-fsg
Px-fsi
Px-fsl
Px-fsn
Px-mpa
Px-mpd
Px-mpg
Px-mpi
Px-mpl
Px-mpn
Px-msan
Px-msay
Px-msd
Px-msg
Px-msi
Px-msl
Px-msn
Px-npa
Px-npd
Px-npg
Px-npi
Px-npl
Px-npn
Px-nsa
Px-nsd
Px-nsg
Px-nsi
Px-nsl
Px-nsn
Px--sa
Px--sd
Px--sg
Px--si
Px--sl
    """
    _test_tag_list(tags,sep)

def _test_particle_tags(sep):
    """
    Tests conversion of PoS particle ("Q") tags to LT Serbian PoS tags
    :param sep: Tag separator
    """
    tags = """
Qo
Qq
Qr
Qz
    """
    _test_tag_list(tags, sep)

def _test_adverb_tags(sep):
    """
    Tests conversion of PoS adverb ("R") tags to LT Serbian PoS tags
    :param sep: Tag separator
    """
    tags = """
Rgc
Rgp
Rgs
Rr
Rp
    """
    _test_tag_list(tags,sep)


def _test_preposition_tags(sep):
    """
    Tests conversion of PoS preposition ("S") tags to LT Serbian PoS tags
    :param sep: Tag separator
    """
    tags = """
Sa
Sd
Sg
Si
Sl
    """
    _test_tag_list(tags,sep)

def _test_verb_tags(sep):
    """
    Tests conversion of PoS verb ("V") tags to LT Serbian PoS tags
    :param sep: Tag separator
    """
    tags = """
Vaa1p
Vaa1s
Vaa2p
Vaa2s
Vaa3p
Vaa3s
Vae1p
Vae1s
Vae2p
Vae2s
Vae3p
Vae3s
Vam1p
Vam2p
Vam2s
Van
Vap-pf
Vap-pm
Vap-pn
Vap-sf
Vap-sm
Vap-sn
Var1p-y
Var1s-y
Var2p-y
Var2s-y
Var3p-y
Var3s-y
Var1p
Var1s
Var2p
Var2s
Var3p
Var3s
Vma1p
Vma1s
Vma2p
Vma2s
Vma3p
Vma3s
Vme1p
Vme1s
Vme2p
Vme2s
Vme3p
Vme3s
Vmf1p
Vmf1s
Vmf2p
Vmf2s
Vmf3p
Vmf3s
Vmm1p
Vmm2p
Vmm2s
Vmn
Vmp-pf
Vmp-pm
Vmp-pn
Vmp-sf
Vmp-sm
Vmp-sn
Vmq-pf
Vmq-pm
Vmq-pn
Vmq-sf
Vmq-sm
Vmq-sn
Vmr1p
Vmr1s
Vmr2p
Vmr2s
Vmr3p
Vmr3s
Vms
Vmt
    """
    _test_tag_list(tags,sep)

def _test_abbreviation_tags(sep):
    """
    Tests conversion of PoS abbreviation ("Y") tags to LT Serbian PoS tags
    :param sep: Tag separator
    """
    tags = "Y"
    _test_tag_list(tags,sep)


def _test_punctuation_tags(sep):
    """
    Tests conversion of PoS punctuation ("Z") tags to LT Serbian PoS tags
    :param sep: Tag separator
    """
    tags = "Z"
    _test_tag_list(tags,sep)


def _test_tag_list(aTags, sep):
    for line in aTags.split():
        line = line.strip()
        tag = get_tag(line, sep)
        desc = get_tag_desc(tag, sep)
        print("POS = {:<8}, LT = {:<23} {}".format(line, tag, desc) )


# Returns human-readable description of LT PoS tag (in Serbian language)
def get_tag_desc(tag, sep):
    ret = ''
    tags = tag.split(sep)
    l1 = list(map(lambda x : DDESC[ x ], tags))
    # Make only first letter of first word uppercase
    fw = list(l1[0])
    fw[0] = fw[0].upper()
    l1[0] = "".join(fw)
    ret = "".join(" {} ".format(sep).join(l1))
    return ret


# Test program by running it directly instead of importing it
if __name__ == "__main__":
    import sys
    cmd = sys.argv[1]
    ssep = ':'
    if cmd == '-l':
        # List LT tags and their descriptions
        get_list(ssep, True)
    elif cmd == '-s':
        # List synthesizer (synth) LT tags without descriptions
        get_list(ssep, False)
    elif cmd == '-t':
        # Run tests, and creates PoS to LT tag list with tag descriptions
        _test_noun_tags(ssep) # Именице
        _test_pronoun_tags(ssep) # Заменице
        _test_adjective_tags(ssep) # Придеви
        _test_numeral_tags(ssep) # Бројеви
        _test_verb_tags(ssep) # Глаголи

        _test_adverb_tags(ssep) # Прилози
        _test_preposition_tags(ssep) # Предлози
        _test_conjunction_tags(ssep) # Везници
        _test_interjection_tags(ssep) # Узвици
        _test_particle_tags(ssep) # Речце

        _test_abbreviation_tags(ssep) # Скраћенице OK
        _test_punctuation_tags(ssep) # Интерпункција OK
