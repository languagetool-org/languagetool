# where.awk
#
# Helps to find the error location in longer text using languagetool.
#
# usage: 1, copy languagetool's output into the file xx
#        2. awk -f where.awk <your.txt >m.txt
# m.txt contains a file, where first you find the error list, then
# the sentence, that contains error, like:
#pos1 pos2 error1
#pos1 pos2 error2
# erronous line
# ....
#
BEGIN {
    pos = 0;
    i = 1;
    while(getline < "xx" > 0){
	 	if(substr($0,1, 7) != "<error ") continue;
		split($0,aa,"from=\"");
#		print "aa " aa[2];
		split(aa[2], aa1, "\"");
		startarray[i] = aa1[1];
		split($0,bb,"to=\"");
		split(bb[2], bb1, "\"");
		stoparray[i] = bb1[1];
		errline[i] = $0;
#		printedarray[i] = 0;
		i = i + 1;
	}
	imax = i;  
}

	{ prevpos = pos;
	  pos = prevpos + length($0)+1;
#	  print  prevpos, pos;
	  printedarray = 0;
	  for(i = 1; i < imax; i++){
	     if((startarray[i] >= prevpos) && (startarray[i] < pos)){
#		      print startarray[i]-prevpos+1, stoparray[i] - startarray[i]+1;
		      hely = substr($0, startarray[i]-prevpos+1, stoparray[i] - startarray[i]+1);
		      hely1 = substr($0, startarray[i]-prevpos+1, stoparray[i] - startarray[i]+3);
				if(index(hely,"\. ") > 1)
				  print "False Alarm: "startarray[i]-prevpos" " stoparray[i]-prevpos " |" hely1"| " errline[i];
				else {
		       print startarray[i]-prevpos" " stoparray[i]-prevpos " |" hely1"| " errline[i];
			    if(printedarray == 0){ 
#			    print $0; 
    				 printedarray = 1;
			    }
				}
		  }
		  if(i == imax -1 && printedarray)
		  	print $0;
		}
	}