import sys, re

file = open(sys.argv[1], 'rb')
lines = file.readlines()

jointonext = ""
for line in lines:
    line = str(line.decode("unicode-escape"))
    line = line.strip()
    if line.startswith("#"):
        continue
    if "=" in line:
        line = re.sub(".* = (.*)", r"\1", line)
        line = line.replace("&", "")
        line = line.replace("''", "'")
        line = line.replace("\\n", " ")
        line = re.sub("<br */>", " ", line, flags=re.DOTALL)
        line = re.sub("<.*?>", "", line, flags=re.DOTALL)       
        print (line)


