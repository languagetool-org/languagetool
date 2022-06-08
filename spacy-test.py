import spacy
import json
import time
import sys
from flask import Flask
from flask import request
# see https://dzlab.github.io/ml/2021/08/21/spark-jep/
# see https://flask.palletsprojects.com/en/2.1.x/quickstart/#a-minimal-application
# start with:
#   export FLASK_APP=spacy-test
#   flask run
# https://gunicorn.org/

app = Flask(__name__)
nlp = spacy.load("en_core_web_sm")

@app.route("/", methods=['GET', 'POST'])
def chunking():
  t1 = time.time()
  if request.method == 'POST':
      text = request.form['text']
  else:
      text = request.args.get('text', '')
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

#if __name__=='__main__':
#    #text = "My red fox jumps over your lazy dog, but."
#    text = "Mary saw the man through the window."
#    #text = "St. Martin's Day"
#    res = chunking(text)
#    print(res)
