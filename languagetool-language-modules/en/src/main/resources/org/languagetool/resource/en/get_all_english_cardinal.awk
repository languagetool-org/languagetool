BEGIN {
main[1] = "twenty"
main[2] = "thirty"
main[3] = "forty"
main[4] = "fifty"
main[5] = "sixty"
main[6] = "seventy"
main[7] = "eighty"
main[8] = "ninety"

minor[1]="one"
minor[2]="two"
minor[3]="three"
minor[4]="four"
minor[5]="five"
minor[6]="six"
minor[7]="seven"
minor[8]="eight"
minor[9]="nine"

for (i = 1; i < 9; i++) {
	for (j = 1; j < 10; j++) {
		print main[i] "-" minor[j] "\t" main[i] "-" minor[j] "\tCD"
	}
	}

}