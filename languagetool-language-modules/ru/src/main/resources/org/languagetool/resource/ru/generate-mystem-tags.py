#!/usr/bin/env python3
# -*- coding: UTF-8 -*-
import os
from subprocess import call
import re
import enchant
d = enchant.Dict("ru_RU")
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
with open('final-tags.txt','w') as final:
    for word in mystems:
        final.write(word)
with open('final.txt','w') as final:
    for word in mystems:
        final.write(word.split("{",1)[0]+"\n")
