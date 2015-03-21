#!/usr/bin/python3
#
# Script to convert LT's pos tag dictionary to corpus format
#
# -*- coding: utf-8 -*-

import sys
import re
import locale
from collections import OrderedDict

#import PyICU

locale.setlocale(locale.LC_ALL, 'uk_UA.UTF-8')
#locale.setlocale(locale.LC_ALL, 'en_US.UTF-8')

#lines = ["Аякс 1", "аякс 2", "Аякс 3"]
#lines = ['аякс ', 'а ', 'б ']

#collator = PyICU.Collator.createInstance(PyICU.Locale('uk_UA.UTF-8'))
#print('icu', sorted(lines, key=collator.getSortKey) )
#print('loc', sorted(lines, key=locale.strxfrm) )

re_vidm = re.compile("v_...")
v_tag_key_map = {
 "v_naz": "1",
 "v_rod": "2",
 "v_dav": "3",
 "v_zna": "4",
 "v_oru": "5",
 "v_mis": "6",
 "v_kly": "7"
}

re_gen = re.compile(":[mfnsp]:")
gen_tag_key_map = {
 ":m:": ":1:",
 ":f:": ":2:",
 ":n:": ":3:",
 ":s:": ":4:",
 ":p:": ":5:",
}

re_verb = re.compile("(inf|impr|pres|futr|past|impers)")
vb_tag_key_map = {
 "inf": "_1",
 "impr": "_2",
 "pres": "_3",
 "futr": "_4", 
 "past": "_5",
 "impers": "_6"
}

def key_for_tag(tag):
  if ":v_" in tag:
    tg = re_vidm.search(tag).group(0)
    tag = tag.replace(tg, v_tag_key_map[tg])
  elif "verb" in tag:
    verb_match = re_verb.search(tag)
    if verb_match:
      tg = verb_match.group(0)
      tag = tag.replace(tg, vb_tag_key_map[tg])
    else:
      if tag != "verb:unknown":
        print('no verb match', tag, file=sys.stderr)

  gen_match = re_gen.search(tag)
  if gen_match:
    tg = gen_match.group(0)
    tag = tag.replace(tg, gen_tag_key_map[tg])

  gen_match = re_gen.search(tag)
  if gen_match:
    tg = gen_match.group(0)
    tag = tag.replace(tg, gen_tag_key_map[tg])

  if ":&adj" in tag:
    tag = tag.replace("adjp:", "adj:")

  return tag


# Сортуємо за лемою, потім за головними грамемами з тегу, потім словом
# після цього леми можна буде виокремити за початковим тегом

def sort_key(str):
 #  sort -k 1,1 -k 3,3 -k 2,2 -t ' '
  lemma, word, tag = str.split(' ')
  
  # дефіс та апостроф при сортуванні ігнорується, щоб леми, що різняться лише дефісом або апострофом
  # не змішувалися додаємо ще символи

  if "-" in lemma:
    lemma += "я"

  if "'" in lemma:
    lemma += "я"
  
  # коротка форма, напр. повен - це не лема, тож відсуваємо його після леми
  
  if word.endswith("ен") and "adj" in tag:
    word = word[:-2] + "яя"
    
  tag_key = key_for_tag(tag)
  
  # split lower and upper case
  if lemma[0].isupper():
    for c in lemma:
      if c.isupper():
        tag_key = "0" + tag_key
  # інфінітив на -сь має йти після -ся (леми)
  elif "verb:" in tag and ":inf" in tag and ":rev" in tag and word.endswith("сь"):
    tag_key += "z"
  
  srt_line = lemma + '0' + tag_key + '0' + word
  return locale.strxfrm(srt_line)

regex_map = {}



def re_sub(regex, repl, txt):
  if not regex in regex_map:
    regex_map[regex] = re.compile(regex)
    
  return regex_map[regex].sub(repl, txt)



re_key_tag = re.compile("^[^:]+(:(anim|inanim|perf|imperf))?")

prev_lemma_key = ""
def convert_to_indented(line):
  global prev_lemma_key

  lemma, word, tag = line.split(" ")
  
  key_tag_match = re_key_tag.search(tag)
  if not key_tag_match:
    print("no key", line, file=sys.stderr)
  else:
    tag_key = key_tag_match.group(0)
    if ":&adj" in tag:
      tag_key = tag_key.replace("adjp", "adj")
    lemma_key = lemma + tag_key
#    print("-", lemma_key)

  if lemma_key != prev_lemma_key:
    if lemma != word:
      line = lemma + ' --\n  ' + word + ' ' + tag
    else:
      line = lemma + ' ' + tag
    prev_lemma_key = lemma_key
  else:
    line = '  ' + word + ' ' + tag

  return line



lines = []

for line in sys.stdin:

 line = line.strip()
 new_lines = []
 
 if 'advp' in line:
   line = re_sub('(.*) .* (advp.*)', '\\1 \\1 \\2', line)
   if "сь advp" in line:
     other_line = re_sub("(.*)сь .* (advp.*)", "\\1ся \\1сь \\2:coll", line)
     new_lines.append(other_line)
 if "verb:inf" in line:
   other_line = re_sub("(.*)ти((ся)? .* verb:inf.*)", "\\1ть\\2:coll", line)
   new_lines.append(other_line)

 if " noun" in line:
   line = re_sub(":p:nv", "\g<0>:ns", line)
   if not ":anim" in line:
     line = line + ":inanim"

 if re.search(" (noun|adj|verb|pron)", line):
   line = re_sub(":[mnf]:", ":s\g<0>", line)


 new_lines.append(line)

 for line2 in new_lines:
   parts = line2.split(' ')
   tag = parts[2]

   if "verb" in line2:
     tag = re_sub("(verb(?::rev)?)(.*)(:(im)?perf)(.*)", "\\1\\3\\2\\5", tag)
   elif "noun" in line2 or "pron" in line2:
     tag = re_sub("(noun|pron)(.*)(:(in)?anim)(.*)", "\\1\\3\\2\\5", tag)
   elif "adj" in line2 and ("comp" in line2 or "super" in line2):
     tag = re_sub("(adjp?)(.*):(comp[br]|super)(.*)", "\\1:\\3\\2\\4", tag)

   line2 = parts[1] + ' ' + parts[0] + ' ' +  tag

   lines.append(line2)

# if len(lines)>100000:
#   break

#print('input count', len(lines), file=sys.stderr)

lines = sorted(lines, key=sort_key)
lines = list(OrderedDict.fromkeys(lines))

#print("-", "\n".join(lines))

#print('output count', len(lines), file=sys.stderr)

for line in lines:
  if not "-lt" in sys.argv:
    line = convert_to_indented(line)
  print(line)

