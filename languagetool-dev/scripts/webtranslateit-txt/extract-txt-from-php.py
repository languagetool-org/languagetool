import sys, re

file = open(sys.argv[1], 'r')
lines = file.readlines()

jointonext = ""
for line in lines:
    line = jointonext + line.strip()
    if "=> '" in line:
        if not line.endswith("',"):
            jointonext = jointonext + line
        else:
            line = re.sub(".*=> '(.*)',", r"\1", line)
            line = line.replace("\\'", "'")
            line = line.replace("|", "; ")
            line = re.sub("<br */>", " ", line, flags=re.DOTALL)
            line = re.sub("<.*?>", "", line, flags=re.DOTALL)
            jointonext = ""
            print (line)

