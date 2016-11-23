#the script annotates uncountable nouns
BEGIN {FS="\t";
glosfile="2of12inf.txt"; #Kevin's file
while ((getline < glosfile)  > 0){ 
	if ($1~/%/) {gsub(/%/,"");
			tabela[$1]="uncount"
			}
		}
english_file="english.txt"; #created temporary file
while ((getline < english_file)  > 0){ 
		if (tabela[$1]=="uncount")
			lemma[$2]="uncount"
		if ($3=="VBG") 
			gerund[$1]="uncount"
		}
uncountables="uncountable.txt" #uncountable nouns
while ((getline < uncountables)  > 0)
	if ($0!~/^#/  && $0!="") {
		if ($0~/ /) {
			print "Entry " $0 " contains a space. Exiting."; exit(1)
			}
		lemma[$0]="uncount"
		}

partlycountable = "partlycountable.txt" #partly uncountable nouns
while ((getline < partlycountable )  > 0)
	if ($0!~/^#/ && $0!="") {
		if ($0~/ /) {
			print "Entry " $0 " contains a space. Exiting."; exit(1)
			}
		partly_noncount[$0]="uncount"
		}		

#title
partly_noncount["sri"]="uncount"

#this should be a pronoun but there ain't such
#tag in Penn Treebank
lemma["anything"]="uncount"
lemma["everybody"]="uncount"
lemma["everyone"]="uncount"
lemma["anyone"]="uncount"
lemma["anybody"]="uncount"
lemma["anyplace"]="uncount"
lemma["everything"]="uncount"
#lemma["everyplace"]="uncount"
lemma["someone"]="uncount"
lemma["somebody"]="uncount"
lemma["something"]="uncount"
#lemma["someplace"]="uncount"
lemma["nobody"]="uncount"
lemma["nothing"]="uncount"
lemma["none"]="uncount"
lemma["whatever"]="uncount"
}
{if ($3=="NN") {
word="__"$1":::"
split($1,maybe_gerund,"-")
if (lemma[$1]=="uncount")
	{print $1 FS $2 FS $3":U"}
else
if (partly_noncount[$1]=="uncount")
	{print $1 FS $2 FS $3":UN"}
else
	#fields of knowledge - used as uncountable, but also sometimes as countable
	{if (word~/logy:::/ && word!~/aetiology|anthology|apology|doxology|etiology|hagiology|trilogy/)
		print $1 FS $2 FS $3":UN"
	else
	if (word~/plasty:::/)
		print $1 FS $2 FS $3":UN"
	else
	if (word~/ity:::/ && word!~/acclivity|amenity|annuity|calamity|callosity|cavity|city|commodity|dacoity|declivity|eventuality|extremity|gratuity|laity|majority|municipality|muzzle-velocity|nativity|nonentity|principality|proclivity|sorority|speciality|trinity|university|varsity/)
		print $1 FS $2 FS $3":UN"
	else
	#doctrines
	if (tolower(word)==word && word~/ism:::/ && word!~/anachronism|anglicism|aphorism|atavism|colloqualism|euphuism|gallicism|__ism+++|malapropism|mannerism|micro-organism|organism|prism|solecism|spoonerism|specialism|syllogism|truism|witticism/)
	print $1 FS $2 FS $3":UN"
	else
	#disciplines etc., ending with -ics
	if (word~/ics:::/)
		print $1 FS $2 FS $3":U"
	else
	if (word~/tion:::/ && word!~/T-junction|abduction|abjection|ablution|abrogation|accentuation|acceptation|activation|adjudication|adoption|adulteration|aeration|afforestation|alleviation|alternation|amelioration|amortization|amplification|amputation|annunciation|apparition|appellation|ascription|asseveration|assignation|assumption|avocation|bastion|beatification|benediction|bifurcation|blood-relation|by-election|calcination|canalization|canonization|capitalization|capitation|caption|carnation|castration|circumnavigation|circumvention|coaling-station|codification|collation|collectivization|complication|condonation|confabulation|configuration|conflagration|conformation|confutation|congratulation|conjuration|connotation|constellation|contradistinction|contraption|conurbation|convolution|copulation|coronation|corporation|correlation|coruscation|counteraction|counterattraction|crepitation|cross-examination|cross-fertilization|cross-section|culmination|de-escalation|debarkation|decapitation|declination|deflection|defoliation|denomination|depiction|deprecation|depredation|deputation|destination|detonation|diffraction|disembarkation|disposition|disquisition|dissertation|dissimulation|edition|ejaculation|ejection|elicitation|elucidation|enunciation|equalization|eradication|evaluation|evocation|exacerbation|excoriation|execration|exhibition|exoneration|expurgation|extermination|felicitation|flagellation|fluoridation|fluoridization|fraction|fumigation|gas-station|genuflection|gestation|graduation|harmonization|idealization|idolization|implementation|imprecation|incarceration|incarnation|inception|incubation|induction|inebriation|injunction|inscription|instigation|instillation|interaction|interjection|involution|irruption|legation|libation|liberalization|loan-collection|love-potion|malediction|materialization|misapplication|misdirection|mispronunciation|nation|notion|oblation|obligation|oration|orchestration|outstation|ovation|palpitation|pay-station|perambulation|peroration|perpetration|perpetuation|personation|pigmentation|plantation|polarization|police-station|polling-station|population|potation|potion|power-station|precondition|predestination|predetermination|predilection|predisposition|prefabrication|premonition|preposition|procreation|proliferation|prorogation|protestation|putrefaction|radio-location|ramification|ratification|redisposition|rejuvenation|rendition|repudiation|reticulation|rogation|scintillation|section|segregation|signification|situation|subsection|substation|subvention|summation|superscription|syndication|titillation|ulceration|ululation|valediction|visitation|weather-station/)
		print $1 FS $2 FS $3":UN"
	else
	if (word~/ness:::/ && word!~/Guinness|baroness|deaconess|eyewitness|governess|harness|lioness|marchioness|ness|patroness|villainess|wilderness/)
		print $1 FS $2 FS $3":UN"
	else
	if (gerund[$1]=="uncount" || gerund[maybe_gerund[2]]=="uncount")
		print $1 FS $2 FS $3":UN"
	else 
	if (lemma[maybe_gerund[2]]=="uncount")
		{print $1 FS $2 FS $3":U"}
	else 
	if (partly_noncount[maybe_gerund[2]]=="uncount")
		{print $1 FS $2 FS $3":UN"}	
	else
		print $0}
	}
else 
	if ($0!="") print $0
}
