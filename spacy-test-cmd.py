# like spacy-test.py, but without a server

import spacy
import json
import time
import sys

nlp = spacy.load("en_core_web_sm")
total_time = 0

def chunking(text):
  global total_time
  t1 = time.time()
  doc = nlp(text)
  map = idxToToken(doc)
  result = []
  handledTokens = []
  #print("X: " + text)
  try:
      for chunk in doc.noun_chunks:
          tmpList = []
          end = chunk.end
          if len(doc) <= chunk.end:
            end = chunk.end-1  # why needed?
          for i in range(doc[chunk.start].idx, doc[end].idx):
              try:
                  if map[i] in handledTokens:
                      None
                  else:
                      tmpList.append(str(map[i].idx) + "-" + str(map[i].idx + len(map[i])))
                      handledTokens.append(map[i])
              except KeyError:
                  None
          result.append(tmpList)
  except IndexError as e:
    raise e
  tokens = []
  for token in doc:
      tokens.append({"text": token.text, "pos": token.pos_, "from": token.idx, "to": token.idx + len(token.text)})
  t2 = time.time()
  printf("-> %.0fms\n", (t2-t1)*1000)
  total_time = total_time + (t2-t1)
  return json.dumps({"noun_chunks": result, "tokens": tokens})

def printf(format, *args):
    sys.stdout.write(format % args)

def idxToToken(doc):
    map = {}
    for token in doc:
        #print(token.text + ":" + str(token.idx) + " " + str(token.idx + len(token.text) - 1))
        for i in range(token.idx, token.idx + len(token.text)):  ## TODO: whitespace!
            map[i] = token
    return map

lines = open("/home/dnaber/lt/en-examples-subset.txt", "r").readlines()
#lines = open("/home/dnaber/lt/en-examples.txt", "r").readlines()
for line in lines:
    res = chunking(line)
    print(res)
print("Total time s:", total_time)
print("Avg time per sentence ms:", total_time/len(lines)*1000)
