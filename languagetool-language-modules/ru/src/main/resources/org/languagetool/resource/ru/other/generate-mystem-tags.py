#!/usr/bin/env python3
# -*- coding: UTF-8 -*-
# 
# Usage:
#    See pymorph-generate-tags.py script for details.

import os
from subprocess import call
import re
# import enchant



pos_LT = {
    'A':'ADJ',#	прилагательное
    'ADV':'ADV',#	наречие
    'ADVPRO':'none',#	местоименное наречие
    'ANUM':'none',#	числительное-прилагательное
    'APRO':'none',#	местоимение-прилагательное
    'COM':'none',#	часть композита - сложного слова
    'CONJ':'CONJ',#	союз
    'INTJ':'INTERJECTION',#	междометие
    'NUM':'NumC',#	числительное
    'PART':'PARTICLE',#	частица
    'PR':'PREP',#	предлог
    'S':'NN',#	существительное
    'SPRO':'PNN',#	местоимение-существительное
    'V':'VB'}#	глагол

pos_other_LT=['DPT',
'PT',
'ADJ_Short',#	краткая форма
'ADJ',#	полная форма
'PADJ',#	притяжательные прилагательные
'ADJ_Comp',#	превосходная
'ADJ_Sup',#	сравнительная
'NNN',#	имя собственное
'NNP',#	отчество
'NNF'#	фамилия
              ]

tense_LT ={
    'praes':'Real',#	настоящее
    'inpraes':'Fut',#	непрошедшее
    'praet':'Past',#	прошедшее
    'inf':'INF',#	инфинитив
    'imper':'IMP'#	повелительное наклонение
}

case_LT={
    #Падеж
    'nom':'Nom',#	именительный
    'gen':'R',#	родительный
    'dat':'D',#	дательный
    'acc':'V',#	винительный
    'ins':'T',#	творительный
    'abl':'P',#	предложный
    'part':'R',#	партитив (второй родительный)
    'loc':'P',#	местный (второй предложный)
    'voc':'none'#	звательный
}

number_LT ={
    #Число
    'sg':'Sin',#	единственное число
    'pl':'PL'#	множественное число
}

person_LT={
    #Лицо глагола
    '1p':'P1',#	1-е лицо
    '2p':'P2',#	2-е лицо
    '3p':'P3'#	3-е лицо
}

gender_LT={
    #Род
    'm':'Masc',#	мужской род
    'f':'Fem',#	женский род
    'n':'Neut'#	средний род
}
# PRDC predicate
# NumC - числительное количественное  - NUMERAL COUNTABLE
# Ord - числительное порядковое - ORDINAL
# PT_Short - краткое причастие - SHORT PARTICIPLE

other_LT={
#Репрезентация и наклонение глагола
'ger':'DPT',#	деепричастие
'partcp':'PT',#	причастие
'indic':'',#	изьявительное наклонение
#Форма прилагательных
'brev':'ADJ_Short',#	краткая форма
'plen':'ADJ',#	полная форма
'poss':'PADJ',#	притяжательные прилагательные
#Степень сравнения
'supr':'ADJ_Comp',#	превосходная
'comp':'ADJ_S',#	сравнительная
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
'persn':'NNN',#	имя собственное
'dist':'',#	искаженная форма
'mf':'',#	общая форма мужского и женского рода
'obsc':'',#	обсценная лексика
'patrn':'NNP',#	отчество
'praed':'',#	предикатив
'inform':'Talk',#	разговорная форма
'rare':'',#	редко встречающееся слово
'abbr':'ABR',#	сокращение
'obsol':'',#	устаревшая форма
'famn':'NNF'#	фамилия
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
    split = re.split(',|=',gramma)
    # pos = pos_LT[split[0]]
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
os.system("time java -jar languagetool/languagetool-standalone/target/LanguageTool-3.5-SNAPSHOT/LanguageTool-3.5-SNAPSHOT/languagetool-commandline.jar -l ru -eo -e Unknown_words  unique-tag.txt > out.txt")            
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
# news.add("сапог")
with open('new-tag.txt','w') as new_file:
    for word in news:
        new_file.write(word+"\n")

os.system("./mystem -nwi --eng-gr new-tag.txt mout.txt")

mystems = []
with open('mout.txt','r') as new_file:
    for line in new_file:
        if not "?" in line:
            mystems.append(line)
mystems.sort()
missed_tags = 0
with open('final-tags-mystem.txt','w') as final:
    for word in mystems:
        token = word.split("{",1)[0]
        descs = word.split("{",1)[1].split("|")
        current_lemma = ""
        # final.write("\n"+word)
        for desc in descs:
            if desc[0] != "=":
                current_lemma = desc.split("=",1)[0]
            # else: continue #TODO remove debug
            gramma = desc.split("=",1)[1]
            if gramma[-2] == "}": gramma = gramma[:-2]
            pos_tag = convert_gramma(gramma)
            if pos_tag in all_tags:
                final.write(token+"\t"+current_lemma+"\t"+pos_tag+"\n")
            else:
                print(token+"\t"+current_lemma+"\t"+pos_tag+" <== "+gramma)
                missed_tags += 1

with open('final-mystem.txt','w') as final:
    for word in mystems:
        final.write(word.split("{",1)[0]+"\n")
print("Detected dictionary tags, missing in LT:", missed_tags)
