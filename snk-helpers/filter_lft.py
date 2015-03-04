#!/usr/bin/python3

# upravi lemma-form-tag pre format vhodny na languagetool

import sys, re

for l in sys.stdin:
    lemma, form, tag = l.strip().split('\t')
    if lemma.startswith('*'):
        lemma = lemma[1:]
    if form.startswith('*'):
        # preskoci "zle" tvary
        # TODO - ako povedat, ze ma na ne upozornit?
        continue
    ntag = tag # upravime trosku tag
    if tag.startswith('VL'):
        if 'p' in tag:
            # neda sa urcit rod
            ntag = re.sub('[hmifno]', 'o', ntag)
        ntag = re.sub('[abc]', 'o', tag)
    ntag = ntag.replace('+', 'P')
    ntag = ntag.replace('-', 'N')
    print (form, lemma, ntag, sep='\t')
