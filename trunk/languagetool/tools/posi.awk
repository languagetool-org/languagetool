# posi.awk
# used by TKLSpell (posi.sh)
# get position from languagetool output
# input: languagetool output with -x
# output: first_pos second_pos text
#  after sort -n all errors sequentially
#
#  usage: python TextChecker.py -l hu -x tools/mytext.txt >tools/mytextout.txt
#         awk -f <tools/mytextout-txt >tools/mytextout2.txt
#         sort -n tools/mytextout.txt >tools/mytextout3.txt
#
# assuming, yourtext.txt is languagetool/tools
# to simplify usage, use: sh posi.sh yourtext 
#
/from=/  { out = "";
          split($0, a, "from=\"");
			 split(a[2], b, "\"");
          split(b[3], c, "\"");
			 out = b[1] " " c[1];
          }
/<message>/ {split($0, aa, "<message>");
 				 split(aa[2], bb, "</message>");
             out = out " " bb[1];
				}
			{ if(bb[1] == ""){
			    split($0, aaa, "\">");
				 split(aaa[2], bbb, "</error>");
				 out = out " " bbb[1];
		     }
				print out;
			  bb[1] = "";
			  out = "";
			}

