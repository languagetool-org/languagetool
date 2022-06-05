import spacy
# see https://dzlab.github.io/ml/2021/08/21/spark-jep/

nlp = spacy.load("en_core_web_sm")

def chunking(text):
  doc = nlp(text)
  result = []
  #for token in doc:
  #  result.append((token.text, token.pos_, token.dep_))
  for chunk in doc.noun_chunks:
      #print(chunk.text, chunk.root.text, chunk.root.dep_,
      #        chunk.root.head.text)
      #result.append(chunk.text + " " + chunk.start)
      #result.append(str(chunk.start)+"-"+str(chunk.end))
      result.append(str(doc[chunk.start].idx) + "-" + str(doc[chunk.end-1].idx))
      #print("*"+str(doc[chunk.start].idx))
  return ' '.join(result)

res = chunking("My red fox jumps over your lazy dog, but.")
print(res)
