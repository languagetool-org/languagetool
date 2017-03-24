#!/usr/bin/env python3
# -*- coding: UTF-8 -*-
# 
# Usage:
#  1) Only python3 (for ease of UTF-8 support)
#  # Not used  2) pyenchant for spellcheck
#  #                 sudo pip3 install pyenchant
#  3) sudo pip3 install pymorphy2[fast]
#  4) copy script to the folder, that contains LT git repo with a build. So that path to "languagetool/languagetool-standalone/target/LanguageTool-3.5-SNAPSHOT/LanguageTool-3.5-SNAPSHOT/languagetool-commandline.jar" should be valid to run LT.
#  5) copy Russian tagset to "all_tags.txt"
#       cp languagetool/languagetool-language-modules/ru/src/main/resources/org/languagetool/resource/ru/tags_russian.txt all_tags.txt
#  6) put any text, which includes tagged and untagged words to "need-tag.txt" file. This can be, e.g. an outpuh of testing "Unkonwn_words" rule against wikipedia.
#  7) run the script, the output, ready to be included to LT should be written to final-tags-pymorph.txt
#
#  How it works:
#  1) Read the input need-tag.txt, split into words.
#  2) Remove truncated words from rule check (starting or ending with "...")
#  3) Remove words with non-cyrillic letters.
#  4) Remove duplicated words, save the list in "unique-tag.txt" file.
#  5) Run LT unkonwn_words rulecheck again.
#  6) Take only marked words from the output.
#  7) Run pymorph2 to get the grammar form of the word, convert it into LT format
#  8) Check against all_tags.txt, output tags to STDOUT that do not fit any valid tag
#  9) save result (all words with valid tags) in final-tags-pymorph.txt

import os
from subprocess import call
import re
import enchant

pos_LT = {
    'ADJF':'ADJ',#    прилагательное
    'ADJS':'ADJ_Short',#    краткая форма
    'COMP':'ADJ_Sup',#    сравнительная
    'ADVB':'ADV',#    наречие
    'GRND':'DPT',#  деепричастие
    'PRTF':'PT', # причастие (полное)
    'PRTS':'PT_Short', # краткое причастие - SHORT PARTICIPLE
    'PRED':'none',#    предикатив    некогда
    'INTJ':'INTERJECTION',#    междометие
    'NUMR':'NumC',#    числительное
    'NPRO':'PNN',#    местоимение-существительное
    'PREP':'PREP',#    предлог
    'CONJ':'CONJ',#    союз
    'PRCL':'PARTICLE',#    частица
    'NOUN':'NN',#    существительное
    'INFN':'VB',#   глагол (инфинитив)
    'VERB':'VB'}#    глагол

pos_other_LT=[
'ADJ',#    полная форма
'PADJ',#    притяжательные прилагательные
'ADJ_Comp',#    превосходная
'NNN',#    имя собственное
'NNP',#    отчество
'NNF'#    фамилия
              ]

tense_LT ={
    'pres':'Real',#    настоящее
    'futr':'Fut',#    непрошедшее
    'past':'Past',#    прошедшее
    'INFN':'INF',#    инфинитив
    'impr':'IMP'#    повелительное наклонение
}

case_LT={
    #Падеж
    'nomn':'Nom',#    именительный
    'gent':'R',#    родительный
    'datv':'D',#    дательный
    'accs':'V',#    винительный
    'ablt':'T',#    творительный
    'loct':'P',#    предложный
    'gen2':'R',#    партитив (второй родительный)
    'acc2':'V',#
    'loc2':'P',#    местный (второй предложный)
    'voct':'none'#    звательный
}

number_LT ={
    #Число
    'sing':'Sin',#    единственное число
    'plur':'PL'#    множественное число
}

person_LT={
    #Лицо глагола
    '1per':'P1',#    1-е лицо
    '2per':'P2',#    2-е лицо
    '3per':'P3'#    3-е лицо
}

gender_LT={
    #Род
    'masc':'Masc',#	мужской род
    'femn':'Fem',#	женский род
    'neut':'Neut'#	средний род
}
# PRDC predicate
# NumC - числительное количественное  - NUMERAL COUNTABLE
# Ord - числительное порядковое - ORDINAL
# PT_Short - краткое причастие - SHORT PARTICIPLE

other_LT={
#Репрезентация и наклонение глагола
'indic':'',#	изьявительное наклонение
#Форма прилагательных
'poss':'PADJ',#	притяжательные прилагательные
#Степень сравнения
'Supr':'ADJ_Comp',#	превосходная
#Вид
'ipf':'',#	несовершенный
'pf':'',#	совершенный
#Залог
'act':'',#	действительный залог
'pass':'',#	страдательный залог
#Одушевленность
'anim':'',#	одушевленное
'inan':'',#	неодушевленное
#Переходность
'tran':'',#	переходный глагол
'intr':'',#	непереходный глагол
#Прочие обозначения
'parenth':'',#	вводное слово
'geo':'',#	географическое название
'awkw':'',#	образование формы затруднено
'Name':'NNN',#	имя собственное
'dist':'',#	искаженная форма
'mf':'',#	общая форма мужского и женского рода
'obsc':'',#	обсценная лексика
'Patr':'NNP',#	отчество
'praed':'',#	предикатив
'Infr':'Talk',#	разговорная форма
'rare':'',#	редко встречающееся слово
'Abbr':'ABR',#	сокращение
'obsol':'',#	устаревшая форма
'Surn':'NNF'#	фамилия
}
all_tags=[]
with open('all_tags.txt', 'r') as data_file:
    for data_line in data_file:
        all_tags.append(data_line[:-1])
#print(all_tags)
def find_list(split, dic):
    ret=[]
    for key in dic.keys():
        if key in split:
            ret.append(dic[key])
    if len(ret)==0: ret.append('none')
    return ret
def convert_gramma(gramma):
    gramma = str(gramma).replace(" ",",")
    split = re.split(",",gramma)
    pos = find_list(split, pos_LT)
    case = find_list(split, case_LT)
    number = find_list(split, number_LT)
    gender = find_list(split, gender_LT)
    tense = find_list(split, tense_LT)
    person = find_list(split, person_LT)
    other = find_list(split, other_LT)
    other_pos = set(other) & set(pos_other_LT)

    if len(pos) != 1 or len(case) > 1 or len(number) > 1 or len(gender) > 1 or len(tense) > 1 or len(person) > 1 or len(other_pos)>1:        
        print("ERROR! Too many pos tags in "+gramma)
        return 'UNKNOWN'
    pos = pos[0]
    if len(other_pos) != 0:
        pos=other_pos.pop()
    if  pos == 'none': return 'UNKNOWN'
      
    output = pos
    
    if pos in ['NN','NNN','NNF','NNP']:
        output+=":"+gender[0]+":"+number[0]+":"+case[0]
        if 'Talk' in other:
            output += ":Talk"
    if pos in ['ADJ','ADJ_Com','ADJ_Short','PADJ']:
        output+=":"+gender[0]
        if number[0] != 'Sin':
            output += ":"+number[0]
        output+=":"+case[0]
    if pos == 'DPT':
        output+=":"+tense[0]
    if pos == 'NumC':
        output+=":"+case[0]
    if pos in ['PT','PT_Short']:
        output+=":"+tense[0]+":"+gender[0]
        if number[0] != 'Sin':
            output += ":"+number[0]
        output+=":"+case[0]
    if pos in ['VB']:
        output+=":"+tense[0]
        if tense[0]=='Past':
            output+=":"+gender[0]
            if number[0] != 'Sin':
                output += ":"+number[0]
        else:
            output+=":"+number[0]+":"+person[0]
    
    output = output.replace('none','')
    if ('NN::' or '::Talk') not in output:
        output = output.replace('::',':')
        output = output.replace('::',':')
    if output[-1] == ":":
        output = output[:-1]
    return output #+" "+str(other)+" <=="+gramma



# d = enchant.Dict("ru_RU")
words = set()
i=0
with open('need-tag.txt', 'r') as data_file:
    for data_line in data_file:
        # i += 1
        # if i > 100: break
        if ' ' in data_line:
            if "..." in data_line[:3]:
                data_line = data_line.split(" ",1)[1]
            if "..." in data_line:
                data_line = data_line.rsplit(" ",1)[0]

        # split = re.split(' |\^|\n|[|]|<|>|\?|\!|,|\.|[0-9]|[a-zA-Z]|\[|\]|_|/|\(|\)|-|:|;| |—|«|»|χ|υ|μ|ε|ί|α|"|…|”|“',data_line)
        split = re.findall(u"[\u0400-\u0500]+", data_line)
        for word in split:
            if word != word.upper():
                words.add(word.lower())
i=0
with open('unique-tag.txt','w') as new_file:
    for word in words:
        # i += 1
        # if i > 100: break
        new_file.write("А "+word+" а.\n")
os.system("java -jar languagetool/languagetool-standalone/target/LanguageTool-3.5-SNAPSHOT/LanguageTool-3.5-SNAPSHOT/languagetool-commandline.jar -l ru -eo -e Unknown_words  unique-tag.txt > out.txt")            
news = set()
with open('out.txt','r') as new_file:
    prev_line = " "
    for line in new_file:
        if "^" in line:
            start = line.find("^")
            stop = line.rfind("^")+1
            news.add(prev_line[start:stop])
        prev_line = line
print(len(news))

# to install optimized version use:
# sudo pip install pymorphy2[fast]
import pymorphy2
morph = pymorphy2.MorphAnalyzer()
news = list(news)
# news.append("сапог")
news.sort()
pytags = []

missed_tags = 0
with open('final-tags-pymorph.txt','w') as final:
    for word in news:
        for data in morph.parse(word):
            if type(data.methods_stack[0][0]) is not pymorphy2.units.by_lookup.DictionaryAnalyzer:
                continue
            if len(data.methods_stack) > 1:
                if type(data.methods_stack[1][0]) is pymorphy2.units.by_analogy.UnknownPrefixAnalyzer:
                    continue
                if type(data.methods_stack[1][0]) is not pymorphy2.units.by_analogy.KnownPrefixAnalyzer:
                    print("Unexpected method stack: ", data.methods_stack)
                    continue
                if len(data.methods_stack) > 2:
                    if type(data.methods_stack[2][0]) is not pymorphy2.units.by_analogy.KnownPrefixAnalyzer:
                        print("Unexpected method stack: ", data.methods_stack)
                        continue
            token = data.word
            current_lemma = data.normal_form
            gramma = data.tag
            pos_tag = convert_gramma(gramma)
            if pos_tag in all_tags:
                final.write(token+"\t"+current_lemma+"\t"+pos_tag+"\n")
            else:
                print(token+"\t"+current_lemma+"\t"+pos_tag+" <== "+str(gramma))
                missed_tags += 1
# # mystems.sort()
# # with open('final-tags.txt','w') as final:
# #     for word in mystems:
# #         final.write(word)

# with open('final.txt','w') as final:
#     for word in mystems:
#         final.write(word.split("{",1)[0]+"\n")
print("Detected dictionary tags, missing in LT:", missed_tags)
