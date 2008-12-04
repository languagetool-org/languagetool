#Script to sort rule matches from LanguageTool
#Usage: gawk -f stats.awk <file_created_by_LanguageTool>
#(c) 2008, Marcin Milkowski
#Licensed on the terms of LGPL 3.0

/[0-9]+\.\)/ {
gsub(/^.*ID: /,"")
rule_cnt[$0]++
current_rule=$0
rulematch=1
}
/^$/ {current_rule=""}
/^(Message: |Suggestion: |\.\.\.)/ {
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