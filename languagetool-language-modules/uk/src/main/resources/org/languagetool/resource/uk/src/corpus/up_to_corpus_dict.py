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

re_vidm = re.compile("(v_...)(:alt|:rare|:coll)*")   # |:contr
v_tag_key_map = {
 "v_naz": "10",
 "v_rod": "20",
 "v_dav": "30",
 "v_zna": "40",
 "v_oru": "50",
 "v_mis": "60",
 "v_kly": "70"
}

re_gen = re.compile(":[mfnsp]:")
gen_tag_key_map = {
 ":m:": ":1",
 ":f:": ":2",
 ":n:": ":3",
 ":s:": ":4",
 ":p:": ":5",
}

re_verb = re.compile("(in[fz]|impr|pres|futr|past|impers)")
vb_tag_key_map = {
 "inf": "_1",
 "inz": "_2",
 "impr": "_3",
 "pres": "_4",
 "futr": "_5", 
 "past": "_6",
 "impers": "_7"
}


def key_for_tag(tags, word):
  if ":v_" in tags:
    vidm_match = re_vidm.search(tags)
    tg = vidm_match.group(1)
    vidm_order = v_tag_key_map[tg]
    if vidm_match.group(2):
        vidm_order = vidm_order.replace("0", "1")
    tags = re_vidm.sub(vidm_order, tags)
    
    if ":np" in tags or ":ns" in tags:
        tags = tags.replace(":np", "").replace(":ns", "")
    
    if tags.startswith("adj:"):
        if not ":comp" in tags and not ":supe" in tags:
            # make sure :contr without :combp sorts ok with adjective base that has compb
            if ":contr" in tags:
                tags = tags.replace(":contr", "").replace("adj:", "adj:compc")
            else:
                tags = tags.replace("adj:", "adj:compb:")
        elif ":super" in tags:
            if word.startswith("що"):
                tags = tags.replace(":super", ":supes")
            elif word.startswith("як"):
                tags = tags.replace(":super", ":supet")
#    if ":compb" in tags:
#        tags = tags.replace(":compb", ":0ompb")   # to put it before contr that does not have compb
#    elif ":super" in tags:
#        if word.startswith("що"):
#            tags = tags.replace(":super", ":supes")
#        elif word.startswith("як"):
#            tags = tags.replace(":super", ":supet")

  elif "verb" in tags:
    verb_match = re_verb.search(tags)
    if verb_match:
      tg = verb_match.group(0)
      tags = tags.replace(tg, vb_tag_key_map[tg])
    else:
      if not re.match("verb:(rev:)?(im)?perf:unknown", tags):
        print('no verb match', tags, file=sys.stderr)

  tag1 = tags + ':'
  gen_match = re_gen.search(tag1)
  if gen_match:
    tg = gen_match.group(0)
    tags = tag1.replace(tg, gen_tag_key_map[tg])

#  gen_match = re_gen.search(tags)
#  if gen_match:
#    tg = gen_match.group(0)
#    tags = tags.replace(tg, gen_tag_key_map[tg])
#
#  if ":&adj" in tags:
#    tags = tags.replace("adjp:", "adj:")

  if "noun" in tags and ":anim" in tags:
    re_name = re.search('(anim:)(.*)([fl]name|patr)', tags)
    if re_name:
        tags = re_name.re.sub(r'\1\3\2', tags)

  if ":nv" in tags:
    tags = tags.replace(":nv", "").replace("anim:", "anim:nv:")

  return tags


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

  # розмовна форма йде після
#  if ("verb" in tag or "advp" in tag) and "inf:coll" in tag:
#    tag = tag.replace('inf:', 'z')

  tag_key = key_for_tag(tag, word)
  
  # split lower and upper case
  if lemma[0].isupper():
    for c in lemma:
      if c.isupper():
        tag_key = "0" + tag_key
  # інфінітив на -сь має йти після -ся (леми)
  elif "verb:" in tag and ":inf" in tag and ":rev" in tag and word.endswith("сь"):
    tag_key += "z"

  if "adjp" in tag_key:
    tag_key = tag_key.replace("adjp", "adjz")
#  print('-2-', str, '-2-', tag_key, file=sys.stderr)

  srt_line = lemma + '0' + tag_key + '0' + word
  return locale.strxfrm(srt_line)
#   return locale.strxfrm(lemma) + '_' + tag_key + '_' + locale.strxfrm(word)

regex_map = {}



def re_sub(regex, repl, txt):
  if not regex in regex_map:
    regex_map[regex] = re.compile(regex)
    
  return regex_map[regex].sub(repl, txt)



re_key_tag = re.compile("^[^:]+(:(anim(:([fl]name|patr))?|inanim|(rev:)?(im)?perf))?")

prev_lemma_key = ""
def convert_to_indented(line):
  global prev_lemma_key

  lemma, word, tag = line.split(" ")
  
  key_tag_match = re_key_tag.search(tag)

  if not key_tag_match:
    print("no key", line, file=sys.stderr)
  else:
    tag_key = key_tag_match.group(0)
#    if ":&adj" in tag:
#      tag_key = tag_key.replace("adjp", "adj")

    if "noun" in tag and ":anim" in tag:
      re_name = re.search('(:[mfp]:).*(:([fl]name|patr))', tag)
      if re_name:
          genus = re_name.group(1)
          genus_base = ':m:'
          if 'Венера' in lemma and ':p:' in tag:
              genus_base = ':f:'
          genus = genus.replace(':p:', genus_base)
          tag_key += genus + re_name.group(2)

    if ":nv" in tag:
      tag_key += ":nv"

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



if "--t" in sys.argv:
    lines=[
'Вескі Вескі noun:m:v_naz:nv:np:anim:lname',
'Вескі Вескі noun:m:v_rod:nv:np:anim:lname',
'Вескі Вескі noun:m:v_dav:nv:np:anim:lname',
'Вескі Вескі noun:m:v_zna:nv:np:anim:lname',
'Вескі Вескі noun:m:v_oru:nv:np:anim:lname',
'Вескі Вескі noun:m:v_mis:nv:np:anim:lname',
'Вескі Вескі noun:f:v_naz:nv:np:anim:lname',
'Вескі Вескі noun:f:v_rod:nv:np:anim:lname',
'Вескі Вескі noun:f:v_dav:nv:np:anim:lname',
'Вескі Вескі noun:f:v_zna:nv:np:anim:lname',
'Вескі Вескі noun:f:v_oru:nv:np:anim:lname',
'Вескі Вескі noun:f:v_mis:nv:np:anim:lname'
    ]
    
    lines = [convert_to_indented(line) for line in lines ]
    print('\n'.join(lines))
    sys.exit(1)


print("Converting to corpus dict...", file=sys.stderr)


lines = []

for line in sys.stdin:

 line = line.strip()
 new_lines = []
 
 if 'advp' in line:
   line = re_sub('(.*) .* (advp.*)', '\\1 \\1 \\2', line)
# TODO extra :coll
#   if "сь advp" in line:
#     other_line = re_sub("(.*)сь .* (advp.*)", "\\1ся \\1сь \\2:coll", line)
#     new_lines.append(other_line)
# elif "verb:inf" in line:
#   other_line = re_sub("(.*)ти((ся)? .* verb:inf.*)", "\\1ть\\2:coll", line)
#   new_lines.append(other_line)
 elif " noun" in line and not "&pron" in line:
   line = re_sub(":p:nv", "\g<0>:ns", line)
   if not ":anim" in line:
     line = line + ":inanim"
 
   

 # this breaks dynamic tagger which only expects [mnfp] not [mnf]:s
# if re.search(" (noun|adj|verb|pron)", line):
#   line = re_sub(":[mnf]:", ":s\g<0>", line)


 new_lines.append(line)

 for line2 in new_lines:
   parts = line2.split(' ')
   tag = parts[2]

   if "verb" in line2:
     tag = re_sub("(verb(?::rev)?)(.*)(:(im)?perf)(.*)", "\\1\\3\\2\\5", tag)
   elif "noun" in line2 or "pron" in line2:
     tag = re_sub("(noun|pron)(.*)(:(in)?anim)(.*)", "\\1\\3\\2\\5", tag)
   elif "adj" in line2:
       if ("comp" in line2 or "super" in line2):
           tag = re_sub("(adjp?)(.*):(comp[br]|super)(.*)", "\\1:\\3\\2\\4", tag)


   if not "-lt" in sys.argv:
     line2 = parts[1] + ' ' + parts[0] + ' ' +  tag

   if ":&adjp" in line:
        adjp_line = re.sub(" (adj(?::compb|:compr|:super)?)(.*):&(adjp(?::pasv|:actv|:past|:pres|:perf|:imperf)+)(.*)", " \\3\\2\\4", line2)
        lines.append(adjp_line)

        line2 = re.sub(":&adjp(:pasv|:actv|:past|:pres|:perf|:imperf)+", "", line2)

   lines.append(line2)

# if len(lines)>100000:
#   break

print('sorting %s lines', len(lines), file=sys.stderr)

lines = sorted(lines, key=sort_key)
lines = list(OrderedDict.fromkeys(lines))

#print("-", "\n".join(lines), file=sys.stderr)

#print('output count', len(lines), file=sys.stderr)

for line in lines:
  if not "-lt" in sys.argv:
    line = convert_to_indented(line)
  print(line)

