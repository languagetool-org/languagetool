#Script to sort rule matches from LanguageTool
#Usage: gawk -f stats.awk <file_created_by_LanguageTool>
#(c) 2008, Marcin Milkowski
# This library is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License as published by the Free Software Foundation; either
# version 2.1 of the License, or (at your option) any later version.
#
# This library is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this library; if not, write to the Free Software
# Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
# USA

/^[0-9]+\.\)/ {
gsub(/^.*ID: /,"")
rule_cnt[$0]++
current_rule=$0
rulematch=1
linecnt=0
}
/^(Message: |Suggestion:)/ {
comments[current_rule]= comments[current_rule] "\n" $0
linecnt++
}
!/^($|Message: |Suggestion:|Time:)/ && !/ \^/ {
if (linecnt>0) 
comments[current_rule]= comments[current_rule] "\n" $0
}
/^ / && / \^/ {
comments[current_rule]= comments[current_rule] "\n" $0 "\n"
}
END {
if (rulematch==1) {
print "LanguageTool rule matches in descending order"
print "============================================="
print ""
}
z = asorti(rule_cnt, rule_names)
#for (i = 1; i <= z; i++)
 #   print i " " rule_names[i]
n = asort(rule_cnt, rules)

for (i = z; i >= 1; i--) {

	for (j = 1; j <= z; j++) {
#		print j " " rule_names[j] " => " rule_cnt[rule_names[j]]
		if (rule_cnt[rule_names[j]]==rules[i] \
			&& printed[rule_names[j]]!="done") {				
				printed[rule_names[j]]="done"				
				rule=rule_names[j]
	print "Rule ID: " rule ", matches: " rule_cnt[rule]
	print comments[rule]
	print "============="
		}
	}
}	
}