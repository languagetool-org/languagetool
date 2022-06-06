import spacy
import json
# see https://dzlab.github.io/ml/2021/08/21/spark-jep/

nlp = spacy.load("en_core_web_sm")

def chunking(text):
  doc = nlp(text)
  map = idxToToken(doc)
  result = []
  handledTokens = []
  for chunk in doc.noun_chunks:
      tmpList = []
      for i in range(doc[chunk.start].idx, doc[chunk.end].idx):
          try:
              if map[i] in handledTokens:
                  None
              else:
                  tmpList.append(str(map[i].idx) + "-" + str(map[i].idx + len(map[i])))
                  handledTokens.append(map[i])
          except KeyError:
              None
      result.append(tmpList)
  tokens = []
  for token in doc:
      tokens.append({"text": token.text, "pos": token.pos_, "from": token.idx, "to": token.idx + len(token.text)})
  return json.dumps({"noun_chunks": result, "tokens": tokens})

def idxToToken(doc):
    map = {}
    for token in doc:
        #print(token.text + ":" + str(token.idx) + " " + str(token.idx + len(token.text) - 1))
        for i in range(token.idx, token.idx + len(token.text)):  ## TODO: whitespace!
            map[i] = token
    return map

if __name__=='__main__':
    #text = "My red fox jumps over your lazy dog, but."
    text = "Mary saw the man through the window."
    res = chunking(text)
    print(res)
